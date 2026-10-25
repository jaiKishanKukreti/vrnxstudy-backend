package com.vrnxstudy.vrnxstudy_api.service.impl;

import com.vrnxstudy.vrnxstudy_api.dto.attempt.*;
import com.vrnxstudy.vrnxstudy_api.entity.*;
import com.vrnxstudy.vrnxstudy_api.entity.enums.AttemptStatus;
import com.vrnxstudy.vrnxstudy_api.entity.enums.TestStatus;
import com.vrnxstudy.vrnxstudy_api.exception.ResourceNotFoundException;
import com.vrnxstudy.vrnxstudy_api.repository.*;
import com.vrnxstudy.vrnxstudy_api.service.TestAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TestAttemptServiceImpl implements TestAttemptService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final TestAttemptRepository attemptRepository;
    private final AttemptAnswerRepository answerRepository;
    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestQuestionOptionRepository optionRepository;

    @Override
    public StartAttemptResponse startAttempt(UUID testId, UUID userId, String idempotencyKey) {
        requireUser(userId);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String key = idempotencyKey.trim();
            if (key.length() > 255) badRequest("Idempotency-Key must be at most 255 characters");
            Optional<TestAttempt> existing = attemptRepository.findByUserIdAndIdempotencyKey(userId, key);
            if (existing.isPresent()) {
                TestAttempt attempt = existing.get();
                if (!attempt.getTest().getId().equals(testId)) badRequest("Idempotency-Key was already used for another test");
                return toStartResponse(attempt);
            }
            idempotencyKey = key;
        } else {
            idempotencyKey = null;
        }

        Test test = testRepository.lockById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));
        // Recheck after locking the test so concurrent requests with the same key are idempotent.
        if (idempotencyKey != null) {
            Optional<TestAttempt> existing = attemptRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
            if (existing.isPresent()) {
                TestAttempt attempt = existing.get();
                if (!attempt.getTest().getId().equals(testId)) badRequest("Idempotency-Key was already used for another test");
                return toStartResponse(attempt);
            }
        }
        if (test.getStatus() != TestStatus.PUBLISHED) conflict("Only published tests can be attempted");
        if (test.getDurationMinutes() == null || test.getDurationMinutes() <= 0) badRequest("Test duration must be greater than zero");
        List<TestQuestion> questions = testQuestionRepository.findByTestIdOrderByDisplayOrderAsc(testId);
        if (questions.isEmpty()) badRequest("This test has no questions");

        long usedAttempts = attemptRepository.countByUserIdAndTest_IdAndStatusNot(userId, testId, AttemptStatus.CANCELLED);
        if (test.getMaxAttempts() != null && usedAttempts >= test.getMaxAttempts()) conflict("Maximum number of attempts reached");

        OffsetDateTime now = OffsetDateTime.now();
        TestAttempt attempt = new TestAttempt();
        attempt.setUserId(userId);
        attempt.setTest(test);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setIdempotencyKey(idempotencyKey);
        attempt.setStartedAt(now);
        attempt.setDeadlineAt(now.plusMinutes(test.getDurationMinutes()));
        attempt.setScore(ZERO);
        attempt.setMaxScore(questions.stream().map(TestQuestion::getMarks).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        attempt.setUnattemptedCount(questions.size());
        attempt = attemptRepository.saveAndFlush(attempt);
        return toStartResponse(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public StartAttemptResponse getAttempt(UUID attemptId, UUID userId) {
        return toStartResponse(findOwnedAttempt(attemptId, userId));
    }

    @Override
    public AttemptQuestionResponse saveAnswer(UUID attemptId, UUID testQuestionId, UUID userId, SaveAnswerRequest request) {
        TestAttempt attempt = findOwnedAttempt(attemptId, userId);
        ensureInProgress(attempt);
        if (OffsetDateTime.now().isAfter(attempt.getDeadlineAt())) conflict("Attempt deadline has passed; submit the attempt to finalize it");
        if (request == null || request.selectedOptionIds() == null) badRequest("selectedOptionIds is required");

        TestQuestion question = testQuestionRepository.findByIdAndTestId(testQuestionId, attempt.getTest().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Question does not belong to this attempt's test"));
        List<UUID> selected = request.selectedOptionIds().stream().filter(Objects::nonNull).distinct().toList();
        if (selected.size() != request.selectedOptionIds().size()) badRequest("Duplicate or null option IDs are not allowed");
        if ("SINGLE_CHOICE".equals(question.getQuestionTypeSnapshot()) && selected.size() > 1) badRequest("Single-choice questions accept at most one option");
        Set<UUID> available = optionRepository.findByTestQuestionIdOrderByDisplayOrderAsc(question.getId()).stream()
                .map(TestQuestionOption::getId).collect(Collectors.toSet());
        if (!available.containsAll(selected)) badRequest("One or more selected options do not belong to this question");

        AttemptAnswer answer = answerRepository.findByAttempt_IdAndTestQuestion_Id(attemptId, testQuestionId).orElseGet(() -> {
            AttemptAnswer a = new AttemptAnswer(); a.setAttempt(attempt); a.setTestQuestion(question); return a;
        });
        answer.setSelectedOptionIds(new ArrayList<>(selected));
        answer.setCorrect(null);
        answer.setMarksEarned(ZERO);
        answerRepository.save(answer);
        return toAttemptQuestion(question, selected);
    }

    @Override
    public SubmitAttemptResponse submitAttempt(UUID attemptId, UUID userId) {
        TestAttempt attempt = findOwnedAttempt(attemptId, userId);
        if (attempt.getStatus() == AttemptStatus.SUBMITTED || attempt.getStatus() == AttemptStatus.EXPIRED) {
            return toSubmitResponse(attempt);
        }
        ensureInProgress(attempt);
        OffsetDateTime now = OffsetDateTime.now();
        boolean deadlinePassed = now.isAfter(attempt.getDeadlineAt());
        List<TestQuestion> questions = testQuestionRepository.findByTestIdOrderByDisplayOrderAsc(attempt.getTest().getId());
        Map<UUID, AttemptAnswer> answers = answerRepository.findByAttempt_IdOrderByTestQuestion_DisplayOrderAsc(attemptId)
                .stream().collect(Collectors.toMap(a -> a.getTestQuestion().getId(), a -> a));
        BigDecimal total = BigDecimal.ZERO;
        int correctCount = 0, wrongCount = 0, unattemptedCount = 0;
        for (TestQuestion question : questions) {
            AttemptAnswer answer = answers.get(question.getId());
            List<UUID> selected = answer == null || answer.getSelectedOptionIds() == null ? List.of() : answer.getSelectedOptionIds();
            if (selected.isEmpty()) {
                unattemptedCount++;
                if (answer != null) { answer.setCorrect(false); answer.setMarksEarned(ZERO); }
                continue;
            }
            Set<UUID> correctIds = optionRepository.findByTestQuestionIdOrderByDisplayOrderAsc(question.getId()).stream()
                    .filter(TestQuestionOption::isCorrect).map(TestQuestionOption::getId).collect(Collectors.toSet());
            boolean isCorrect = correctIds.equals(new HashSet<>(selected));
            BigDecimal earned;
            if (isCorrect) { correctCount++; earned = safe(question.getMarks()); }
            else {
                wrongCount++;
                earned = attempt.getTest().isNegativeMarkingEnabled() ? safe(question.getNegativeMarks()).negate() : BigDecimal.ZERO;
            }
            total = total.add(earned);
            answer.setCorrect(isCorrect);
            answer.setMarksEarned(earned);
        }
        answerRepository.saveAll(answers.values());
        attempt.setScore(total.setScale(2, java.math.RoundingMode.HALF_UP));
        attempt.setCorrectCount(correctCount);
        attempt.setWrongCount(wrongCount);
        attempt.setUnattemptedCount(unattemptedCount);
        attempt.setSubmittedAt(now);
        attempt.setStatus(deadlinePassed ? AttemptStatus.EXPIRED : AttemptStatus.SUBMITTED);
        attemptRepository.save(attempt);
        return toSubmitResponse(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttemptSummaryResponse> getHistory(UUID userId) {
        requireUser(userId);
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId).stream().map(this::toSummary).toList();
    }

    private StartAttemptResponse toStartResponse(TestAttempt attempt) {
        List<TestQuestion> questions = testQuestionRepository.findByTestIdOrderByDisplayOrderAsc(attempt.getTest().getId());
        Map<UUID, List<UUID>> selected = answerRepository.findByAttempt_IdOrderByTestQuestion_DisplayOrderAsc(attempt.getId()).stream()
                .collect(Collectors.toMap(a -> a.getTestQuestion().getId(), a -> a.getSelectedOptionIds() == null ? List.of() : a.getSelectedOptionIds()));
        return new StartAttemptResponse(attempt.getId(), attempt.getTest().getId(), attempt.getTest().getTitle(), attempt.getStatus(),
                attempt.getStartedAt(), attempt.getDeadlineAt(), questions.stream().map(q -> toAttemptQuestion(q, selected.getOrDefault(q.getId(), List.of()))).toList());
    }

    private AttemptQuestionResponse toAttemptQuestion(TestQuestion question, List<UUID> selectedIds) {
        List<AttemptOptionResponse> options = optionRepository.findByTestQuestionIdOrderByDisplayOrderAsc(question.getId()).stream()
                .map(o -> new AttemptOptionResponse(o.getId(), o.getOptionKey(), o.getOptionText(), o.getDisplayOrder())).toList();
        return new AttemptQuestionResponse(question.getId(), question.getDisplayOrder(), question.getQuestionTextSnapshot(),
                question.getQuestionTypeSnapshot(), question.getMarks(), options, selectedIds);
    }

    private SubmitAttemptResponse toSubmitResponse(TestAttempt attempt) {
        List<AttemptAnswer> answers = answerRepository.findByAttempt_IdOrderByTestQuestion_DisplayOrderAsc(attempt.getId());
        Map<UUID, AttemptAnswer> byQuestion = answers.stream().collect(Collectors.toMap(a -> a.getTestQuestion().getId(), a -> a));
        List<SubmitAttemptResponse.QuestionResultResponse> results = testQuestionRepository.findByTestIdOrderByDisplayOrderAsc(attempt.getTest().getId()).stream().map(q -> {
            AttemptAnswer a = byQuestion.get(q.getId());
            List<UUID> selected = a == null || a.getSelectedOptionIds() == null ? List.of() : a.getSelectedOptionIds();
            List<UUID> correctIds = optionRepository.findByTestQuestionIdOrderByDisplayOrderAsc(q.getId()).stream().filter(TestQuestionOption::isCorrect).map(TestQuestionOption::getId).toList();
            return new SubmitAttemptResponse.QuestionResultResponse(q.getId(), q.getQuestionTextSnapshot(), selected, correctIds,
                    Boolean.TRUE.equals(a == null ? null : a.getCorrect()), a == null ? ZERO : safe(a.getMarksEarned()), safe(q.getMarks()), safe(q.getNegativeMarks()), q.getExplanationSnapshot());
        }).toList();
        return new SubmitAttemptResponse(toSummary(attempt), results);
    }

    private AttemptSummaryResponse toSummary(TestAttempt a) {
        return new AttemptSummaryResponse(a.getId(), a.getTest().getId(), a.getTest().getTitle(), a.getStatus(), safe(a.getScore()), safe(a.getMaxScore()),
                a.getCorrectCount(), a.getWrongCount(), a.getUnattemptedCount(), a.getStartedAt(), a.getSubmittedAt(), a.getDeadlineAt());
    }

    private TestAttempt findOwnedAttempt(UUID attemptId, UUID userId) {
        requireUser(userId);
        return attemptRepository.findByIdAndUserId(attemptId, userId).orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
    }
    private void ensureInProgress(TestAttempt attempt) { if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) conflict("Attempt is no longer in progress"); }
    private void requireUser(UUID userId) { if (userId == null) badRequest("userId is required"); }
    private BigDecimal safe(BigDecimal n) { return n == null ? ZERO : n; }
    private void badRequest(String m) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private void notFound(String m) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
    private void conflict(String m) { throw new ResponseStatusException(HttpStatus.CONFLICT, m); }
}
