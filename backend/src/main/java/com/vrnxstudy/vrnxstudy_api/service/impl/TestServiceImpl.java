
package com.vrnxstudy.vrnxstudy_api.service.impl;

import com.vrnxstudy.vrnxstudy_api.dto.test.*;
import com.vrnxstudy.vrnxstudy_api.entity.*;
import com.vrnxstudy.vrnxstudy_api.entity.enums.*;
import com.vrnxstudy.vrnxstudy_api.repository.*;
import com.vrnxstudy.vrnxstudy_api.service.TestService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final QuestionRepository questionRepository;

    @Override
    public TestResponse createTest(TestCreateRequest request, UUID creatorId) {

        validateScope(
                request.testType(),
                request.examId(),
                request.subjectId(),
                request.chapterId()
        );

        Test test = new Test();
        test.setTitle(request.title().trim());
        test.setTestType(request.testType());
        test.setExamId(request.examId());
        test.setSubjectId(request.subjectId());
        test.setChapterId(request.chapterId());
        test.setDurationMinutes(request.durationMinutes());
        test.setMaxAttempts(request.maxAttempts());
        test.setNegativeMarkingEnabled(
                request.negativeMarkingEnabled() == null
                        || request.negativeMarkingEnabled()
        );
        test.setCreatedBy(creatorId);
        test.setStatus(TestStatus.DRAFT);

        return toResponse(testRepository.save(test));
    }

    @Override
    @Transactional(readOnly = true)
    public TestResponse getTest(UUID testId) {
        return toResponse(findTest(testId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResponse> getMyTests(UUID creatorId) {
        return testRepository.findByCreatedByOrderByCreatedAtDesc(creatorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TestQuestionResponse addQuestion(
            UUID testId,
            AddTestQuestionRequest request,
            UUID creatorId
    ) {

        Test test = findTest(testId);

        ensureOwner(test, creatorId);
        ensureDraft(test);

        if (request.marks().compareTo(BigDecimal.ZERO) < 0
                || request.negativeMarks().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Marks cannot be negative"
            );
        }

        if (testQuestionRepository.existsByTestIdAndQuestionId(
                testId, request.questionId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Question already exists in this test"
            );
        }

        boolean orderTaken = testQuestionRepository
                .findByTestIdOrderByDisplayOrderAsc(testId)
                .stream()
                .anyMatch(q -> q.getDisplayOrder().equals(request.displayOrder()));

        if (orderTaken) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Display order is already used in this test"
            );
        }

        Question source = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Source question not found"
                ));

        // Question.status tumhari entity mein String hai.
        if (!"PUBLISHED".equals(source.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only published questions can be added to a test"
            );
        }

        if (source.getOptions() == null || source.getOptions().size() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Question must have at least two options"
            );
        }

        // QuestionOption.isCorrect Boolean field hai.
        long correctCount = source.getOptions().stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                .count();

        // Question.questionType tumhari entity mein String hai.
        if ("SINGLE_CHOICE".equals(source.getQuestionType())
                && correctCount != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Single-choice question must have exactly one correct option"
            );
        }

        if ("MULTIPLE_CHOICE".equals(source.getQuestionType())
                && correctCount < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Multiple-choice question must have at least one correct option"
            );
        }

        if (!"SINGLE_CHOICE".equals(source.getQuestionType())
                && !"MULTIPLE_CHOICE".equals(source.getQuestionType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported question type: " + source.getQuestionType()
            );
        }

        // Source question ka snapshot banate hain.
        TestQuestion snapshot = new TestQuestion();
        snapshot.setTest(test);
        snapshot.setQuestionId(source.getId());
        snapshot.setDisplayOrder(request.displayOrder());
        snapshot.setQuestionTextSnapshot(source.getQuestionText());
        snapshot.setExplanationSnapshot(source.getExplanation());

        // String hai, isliye .name() nahi lagana.
        snapshot.setQuestionTypeSnapshot(source.getQuestionType());

        snapshot.setMarks(request.marks());
        snapshot.setNegativeMarks(request.negativeMarks());

        // Options ki copy banate hain, taaki baad mein original question
        // edit hone par test ka snapshot change na ho.
        source.getOptions().forEach(sourceOption -> {
            TestQuestionOption option = new TestQuestionOption();

            option.setTestQuestion(snapshot);
            option.setSourceOptionId(sourceOption.getId());
            option.setOptionKey(sourceOption.getOptionKey());
            option.setOptionText(sourceOption.getOptionText());
            option.setDisplayOrder(sourceOption.getDisplayOrder());
            option.setCorrect(
                    Boolean.TRUE.equals(sourceOption.getIsCorrect())
            );

            snapshot.getOptions().add(option);
        });

        test.getQuestions().add(snapshot);

        // Test entity mein questions collection par cascade persist hona chahiye.
        Test savedTest = testRepository.save(test);

        TestQuestion savedQuestion = savedTest.getQuestions().stream()
                .filter(q -> q.getQuestionId().equals(source.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not retrieve saved test question"
                ));

        return toQuestionResponse(savedQuestion);
    }

    @Override
    public TestResponse publishTest(UUID testId, UUID creatorId) {

        Test test = findTest(testId);

        ensureOwner(test, creatorId);
        ensureDraft(test);

        List<TestQuestion> questions =
                testQuestionRepository.findByTestIdOrderByDisplayOrderAsc(testId);

        if (questions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Add at least one question before publishing"
            );
        }

        for (TestQuestion question : questions) {

            if (question.getOptions() == null
                    || question.getOptions().size() < 2) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Every question must have at least two options"
                );
            }

            long correctCount = question.getOptions().stream()
                    .filter(TestQuestionOption::isCorrect)
                    .count();

            if ("SINGLE_CHOICE".equals(question.getQuestionTypeSnapshot())
                    && correctCount != 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Single-choice question must have exactly one correct option"
                );
            }

            if ("MULTIPLE_CHOICE".equals(question.getQuestionTypeSnapshot())
                    && correctCount < 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Multiple-choice question must have at least one correct option"
                );
            }
        }

        test.setStatus(TestStatus.PUBLISHED);

        return toResponse(testRepository.save(test));
    }

    @Override
    public void removeQuestion(
            UUID testId,
            UUID testQuestionId,
            UUID creatorId
    ) {

        Test test = findTest(testId);

        ensureOwner(test, creatorId);
        ensureDraft(test);

        TestQuestion question = testQuestionRepository
                .findByIdAndTestId(testQuestionId, testId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Question does not belong to this test"
                ));

        test.getQuestions().remove(question);
        testQuestionRepository.delete(question);
    }

    private void validateScope(
            TestType type,
            UUID examId,
            UUID subjectId,
            UUID chapterId
    ) {

        boolean valid = switch (type) {
            case CHAPTER ->
                    examId != null && subjectId != null && chapterId != null;

            case SUBJECT ->
                    examId != null && subjectId != null && chapterId == null;

            case GLOBAL ->
                    examId == null && subjectId == null && chapterId == null;
        };

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Exam, subject and chapter IDs do not match the test type"
            );
        }
    }

    private Test findTest(UUID id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Test not found"
                ));
    }

    private void ensureDraft(Test test) {
        if (test.getStatus() != TestStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only draft tests can be modified"
            );
        }
    }

    private void ensureOwner(Test test, UUID creatorId) {
        if (creatorId == null || !test.getCreatedBy().equals(creatorId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not the creator of this test"
            );
        }
    }

    private TestResponse toResponse(Test test) {

        List<TestQuestionResponse> questions =
                testQuestionRepository
                        .findByTestIdOrderByDisplayOrderAsc(test.getId())
                        .stream()
                        .map(this::toQuestionResponse)
                        .toList();

        return new TestResponse(
                test.getId(),
                test.getTitle(),
                test.getTestType(),
                test.getExamId(),
                test.getSubjectId(),
                test.getChapterId(),
                test.getDurationMinutes(),
                test.getMaxAttempts(),
                test.isNegativeMarkingEnabled(),
                test.getStatus(),
                test.getCreatedBy(),
                test.getCreatedAt(),
                test.getUpdatedAt(),
                questions
        );
    }

    private TestQuestionResponse toQuestionResponse(TestQuestion question) {

        List<TestQuestionOptionResponse> options =
                question.getOptions().stream()
                        .map(option -> new TestQuestionOptionResponse(
                                option.getId(),
                                option.getOptionKey(),
                                option.getOptionText(),
                                option.getDisplayOrder()
                        ))
                        .toList();

        return new TestQuestionResponse(
                question.getId(),
                question.getQuestionId(),
                question.getDisplayOrder(),
                question.getQuestionTextSnapshot(),
                question.getExplanationSnapshot(),
                question.getQuestionTypeSnapshot(),
                question.getMarks(),
                question.getNegativeMarks(),
                options
        );
    }
}
