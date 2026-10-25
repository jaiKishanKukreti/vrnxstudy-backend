package com.vrnxstudy.vrnxstudy_api.controller;

import com.vrnxstudy.vrnxstudy_api.dto.attempt.*;
import com.vrnxstudy.vrnxstudy_api.entity.User;
import com.vrnxstudy.vrnxstudy_api.exception.ResourceNotFoundException;
import com.vrnxstudy.vrnxstudy_api.repository.UserRepository;
import com.vrnxstudy.vrnxstudy_api.service.TestAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@PreAuthorize("hasRole('STUDENT')")
@RestController
@RequiredArgsConstructor
public class TestAttemptController {
    private final TestAttemptService attemptService;
    private final UserRepository userRepository; // Add this

    @PostMapping("/api/tests/{testId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public StartAttemptResponse start(
            @PathVariable UUID testId,
            JwtAuthenticationToken authentication, // Get token
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        String email = authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return attemptService.startAttempt(testId, user.getId(), idempotencyKey);
    }
    @GetMapping("/api/attempts/{attemptId}")
    public StartAttemptResponse getAttempt(@PathVariable UUID attemptId, @RequestParam UUID userId) {
        return attemptService.getAttempt(attemptId, userId);
    }

    @PutMapping("/api/attempts/{attemptId}/questions/{testQuestionId}/answer")
    public AttemptQuestionResponse saveAnswer(@PathVariable UUID attemptId,
                                              @PathVariable UUID testQuestionId,
                                              @RequestParam UUID userId,
                                              @Valid @RequestBody SaveAnswerRequest request) {
        return attemptService.saveAnswer(attemptId, testQuestionId, userId, request);
    }

    @PostMapping("/api/attempts/{attemptId}/submit")
    public SubmitAttemptResponse submit(@PathVariable UUID attemptId, @RequestParam UUID userId) {
        return attemptService.submitAttempt(attemptId, userId);
    }

    @GetMapping("/api/attempts/my")
    public List<AttemptSummaryResponse> history(@RequestParam UUID userId) {
        return attemptService.getHistory(userId);
    }
}
