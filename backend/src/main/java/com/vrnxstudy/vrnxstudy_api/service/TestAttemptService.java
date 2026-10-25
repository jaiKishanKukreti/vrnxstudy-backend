package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.dto.attempt.*;
import java.util.List;
import java.util.UUID;

public interface TestAttemptService {
    StartAttemptResponse startAttempt(UUID testId, UUID userId, String idempotencyKey);
    StartAttemptResponse getAttempt(UUID attemptId, UUID userId);
    AttemptQuestionResponse saveAnswer(UUID attemptId, UUID testQuestionId, UUID userId, SaveAnswerRequest request);
    SubmitAttemptResponse submitAttempt(UUID attemptId, UUID userId);
    List<AttemptSummaryResponse> getHistory(UUID userId);
}
