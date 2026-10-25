package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.dto.student.*;
import com.vrnxstudy.vrnxstudy_api.entity.*;
import com.vrnxstudy.vrnxstudy_api.entity.enums.TestStatus;
import com.vrnxstudy.vrnxstudy_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // स्टूडेंट सिर्फ पढ़ेगा, लिखेगा नहीं
public class StudentService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final TestAttemptRepository attemptRepository;

    // 1. सारे PUBLISHED Exams
    public List<StudentExamResponse> getAllExams() {
        return examRepository.findAllByDeletedAtIsNull(Pageable.unpaged())
                .stream()
                .filter(exam -> "PUBLISHED".equalsIgnoreCase(exam.getStatus()))
                .map(e -> new StudentExamResponse(e.getId(), e.getName(), e.getDescription()))
                .toList();
    }

    // 2. Exam के अंदर PUBLISHED Subjects
    public List<StudentSubjectResponse> getSubjectsByExam(UUID examId) {
        return subjectRepository.findAllByExam_IdAndDeletedAtIsNull(examId, Pageable.unpaged())
                .stream()
                .filter(sub -> "PUBLISHED".equalsIgnoreCase(sub.getStatus()))
                .map(s -> new StudentSubjectResponse(s.getId(), s.getName()))
                .toList();
    }

    // 3. Subject के अंदर PUBLISHED Chapters
    public List<StudentChapterResponse> getChaptersBySubject(UUID subjectId) {
        return chapterRepository.findAllBySubject_IdAndDeletedAtIsNull(subjectId, Pageable.unpaged())
                .stream()
                .filter(ch -> "PUBLISHED".equalsIgnoreCase(ch.getStatus()))
                .map(c -> new StudentChapterResponse(c.getId(), c.getName()))
                .toList();
    }

    // 4. Chapter के Practice Questions (बिना सही जवाब के)
    public List<StudentQuestionResponse> getPracticeQuestions(UUID chapterId) {
        return questionRepository.findByChapter_IdAndDeletedAtIsNull(chapterId)
                .stream()
                .filter(q -> "PUBLISHED".equalsIgnoreCase(q.getStatus()))
                .map(q -> new StudentQuestionResponse(
                        q.getId(),
                        q.getQuestionText(),
                        q.getQuestionType(),
                        q.getOptions().stream()
                                .map(opt -> new StudentOptionResponse(opt.getId(), opt.getOptionKey(), opt.getOptionText()))
                                .toList()
                ))
                .toList();
    }

    // 5. Chapter के Tests + उस स्टूडेंट की Attempt History
    public List<StudentChapterTestResponse> getTestsWithHistory(UUID chapterId, UUID studentId) {

        // सारे PUBLISHED टेस्ट निकालो और इस चैप्टर वाले टेस्ट फिल्टर करो
        List<Test> chapterTests = testRepository.findByStatusOrderByCreatedAtDesc(TestStatus.PUBLISHED)
                .stream()
                .filter(test -> chapterId.equals(test.getChapterId()))
                .toList();

        // स्टूडेंट के सारे Attempts (हिस्ट्री) एक बार में निकाल लो
        List<TestAttempt> allStudentAttempts = attemptRepository.findByUserIdOrderByStartedAtDesc(studentId);

        return chapterTests.stream().map(test -> {

            // इस टेस्ट के लिए स्टूडेंट के जो attempts हैं, उन्हें फिल्टर करो
            List<StudentAttemptHistory> history = allStudentAttempts.stream()
                    .filter(attempt -> attempt.getTest().getId().equals(test.getId()))
                    .map(att -> new StudentAttemptHistory(
                            att.getId(),
                            att.getStatus().name(),
                            att.getScore(),
                            att.getMaxScore(),
                            att.getStartedAt(),
                            att.getSubmittedAt()
                    ))
                    .toList();

            return new StudentChapterTestResponse(
                    test.getId(),
                    test.getTitle(),
                    test.getDurationMinutes(),
                    test.getMaxAttempts(),
                    history
            );
        }).toList();
    }
}