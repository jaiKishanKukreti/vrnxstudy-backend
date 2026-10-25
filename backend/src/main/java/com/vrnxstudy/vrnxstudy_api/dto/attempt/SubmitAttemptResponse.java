package com.vrnxstudy.vrnxstudy_api.dto.attempt;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SubmitAttemptResponse(
        AttemptSummaryResponse attempt,
        List<QuestionResultResponse> questionResults
) {
    public record QuestionResultResponse(
            UUID testQuestionId,
            String questionText,
            List<UUID> selectedOptionIds,
            List<UUID> correctOptionIds,
            boolean correct,
            BigDecimal marksEarned,
            BigDecimal marks,
            BigDecimal negativeMarks,
            String explanation
    ) {}
}
