
package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.dto.question.QuestionCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.question.QuestionOptionRequest;
import com.vrnxstudy.vrnxstudy_api.dto.question.QuestionResponse;
import com.vrnxstudy.vrnxstudy_api.dto.question.QuestionUpdateRequest;
import com.vrnxstudy.vrnxstudy_api.entity.Chapter;
import com.vrnxstudy.vrnxstudy_api.entity.Question;
import com.vrnxstudy.vrnxstudy_api.entity.QuestionOption;
import com.vrnxstudy.vrnxstudy_api.repository.ChapterRepository;
import com.vrnxstudy.vrnxstudy_api.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;

    public QuestionService(
            QuestionRepository questionRepository,
            ChapterRepository chapterRepository) {
        this.questionRepository = questionRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional
    public QuestionResponse create(QuestionCreateRequest request) {

        Chapter chapter = chapterRepository.findById(request.chapterId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Chapter not found"));

        String type = normalize(request.questionType());
        String difficulty = normalizeDefault(request.difficulty(), "MEDIUM");
        String status = normalizeDefault(request.status(), "DRAFT");

        validateQuestion(type, difficulty, status,
                request.marks(), request.negativeMarks(), request.options());

        Question question = new Question();
        question.setChapter(chapter);
        question.setQuestionType(type);
        question.setQuestionText(request.questionText().trim());
        question.setExplanation(request.explanation());
        question.setDifficulty(difficulty);
        question.setMarks(request.marks());
        question.setNegativeMarks(request.negativeMarks());
        question.setStatus(status);

        addOptions(question, request.options());

        return toResponse(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public QuestionResponse getById(UUID id) {
        return toResponse(getActiveQuestion(id));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getByChapter(UUID chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new EntityNotFoundException("Chapter not found");
        }

        return questionRepository
                .findByChapter_IdAndDeletedAtIsNull(chapterId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public QuestionResponse update(
            UUID id, QuestionUpdateRequest request) {

        Question question = getActiveQuestion(id);

        Chapter chapter = chapterRepository.findById(request.chapterId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Chapter not found"));

        String type = normalize(request.questionType());
        String difficulty = normalizeDefault(request.difficulty(), "MEDIUM");
        String status = normalize(request.status());

        validateQuestion(type, difficulty, status,
                request.marks(), request.negativeMarks(), request.options());

        question.setChapter(chapter);
        question.setQuestionType(type);
        question.setQuestionText(request.questionText().trim());
        question.setExplanation(request.explanation());
        question.setDifficulty(difficulty);
        question.setMarks(request.marks());
        question.setNegativeMarks(request.negativeMarks());
        question.setStatus(status);

        question.clearOptions();
        addOptions(question, request.options());

        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public void delete(UUID id) {
        Question question = getActiveQuestion(id);

        // Soft delete: row stays in the database.
        question.setDeletedAt(OffsetDateTime.now());
        questionRepository.save(question);
    }

    private Question getActiveQuestion(UUID id) {
        return questionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Question not found"));
    }

    private void validateQuestion(
            String type,
            String difficulty,
            String status,
            BigDecimal marks,
            BigDecimal negativeMarks,
            List<QuestionOptionRequest> options) {

        if (!Set.of("SINGLE_CHOICE", "MULTIPLE_CHOICE").contains(type)) {
            throw new IllegalArgumentException("Invalid question type");
        }

        if (!Set.of("EASY", "MEDIUM", "HARD").contains(difficulty)) {
            throw new IllegalArgumentException("Invalid difficulty");
        }

        if (!Set.of("DRAFT", "PUBLISHED", "ARCHIVED").contains(status)) {
            throw new IllegalArgumentException("Invalid question status");
        }

        if (marks == null || negativeMarks == null
                || marks.signum() < 0 || negativeMarks.signum() < 0) {
            throw new IllegalArgumentException(
                    "Marks and negative marks cannot be negative");
        }

        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException(
                    "At least two options are required");
        }

        Set<String> keys = new HashSet<>();
        Set<Integer> orders = new HashSet<>();
        int correctCount = 0;

        for (QuestionOptionRequest option : options) {
            String key = option.optionKey().trim().toUpperCase(Locale.ROOT);

            if (!keys.add(key)) {
                throw new IllegalArgumentException(
                        "Option keys must be unique");
            }

            if (!orders.add(option.displayOrder())) {
                throw new IllegalArgumentException(
                        "Option display orders must be unique");
            }

            if (Boolean.TRUE.equals(option.isCorrect())) {
                correctCount++;
            }
        }

        if (correctCount == 0) {
            throw new IllegalArgumentException(
                    "At least one option must be correct");
        }

        if ("SINGLE_CHOICE".equals(type) && correctCount != 1) {
            throw new IllegalArgumentException(
                    "SINGLE_CHOICE must have exactly one correct option");
        }
    }

    private void addOptions(
            Question question,
            List<QuestionOptionRequest> options) {

        for (QuestionOptionRequest request : options) {
            QuestionOption option = new QuestionOption();
            option.setOptionKey(
                    request.optionKey().trim().toUpperCase(Locale.ROOT));
            option.setOptionText(request.optionText().trim());
            option.setDisplayOrder(request.displayOrder());
            option.setIsCorrect(request.isCorrect());

            question.addOption(option);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required value is missing");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDefault(String value, String defaultValue) {
        return value == null || value.isBlank()
                ? defaultValue
                : normalize(value);
    }

    private QuestionResponse toResponse(Question question) {
        List<QuestionResponse.OptionResponse> options =
                question.getOptions().stream()
                        .sorted((a, b) ->
                                a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                        .map(option ->
                                new QuestionResponse.OptionResponse(
                                        option.getId(),
                                        option.getOptionKey(),
                                        option.getOptionText(),
                                        option.getDisplayOrder(),
                                        option.getIsCorrect()))
                        .toList();

        return new QuestionResponse(
                question.getId(),
                question.getChapter().getId(),
                question.getQuestionType(),
                question.getQuestionText(),
                question.getExplanation(),
                question.getDifficulty(),
                question.getMarks(),
                question.getNegativeMarks(),
                question.getStatus(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                options
        );
    }
}
