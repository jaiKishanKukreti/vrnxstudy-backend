package com.vrnxstudy.vrnxstudy_api.controller;

import com.vrnxstudy.vrnxstudy_api.dto.student.*;
import com.vrnxstudy.vrnxstudy_api.entity.User;
import com.vrnxstudy.vrnxstudy_api.exception.ResourceNotFoundException;
import com.vrnxstudy.vrnxstudy_api.repository.UserRepository;
import com.vrnxstudy.vrnxstudy_api.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')") // सिर्फ स्टूडेंट ही एक्सेस कर सकता है
public class StudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    @GetMapping("/exams")
    public List<StudentExamResponse> getExams() {
        return studentService.getAllExams();
    }

    @GetMapping("/exams/{examId}/subjects")
    public List<StudentSubjectResponse> getSubjects(@PathVariable UUID examId) {
        return studentService.getSubjectsByExam(examId);
    }

    @GetMapping("/subjects/{subjectId}/chapters")
    public List<StudentChapterResponse> getChapters(@PathVariable UUID subjectId) {
        return studentService.getChaptersBySubject(subjectId);
    }

    @GetMapping("/chapters/{chapterId}/questions")
    public List<StudentQuestionResponse> getQuestions(@PathVariable UUID chapterId) {
        return studentService.getPracticeQuestions(chapterId);
    }

    // यह API स्टूडेंट को चैप्टर के टेस्ट और उसकी हिस्ट्री दोनों देगी
    @GetMapping("/chapters/{chapterId}/tests")
    public List<StudentChapterTestResponse> getTestsAndHistory(
            @PathVariable UUID chapterId,
            JwtAuthenticationToken authentication) { // <-- टोकन से स्टूडेंट की पहचान

        UUID studentId = getUserIdFromToken(authentication);
        return studentService.getTestsWithHistory(chapterId, studentId);
    }

    // टोकन से UUID निकालने का हेल्पर
    private UUID getUserIdFromToken(JwtAuthenticationToken authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}