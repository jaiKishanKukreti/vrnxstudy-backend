package com.vrnxstudy.vrnxstudy_api.dto.attempt;

import com.vrnxstudy.vrnxstudy_api.entity.enums.AttemptStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AttemptSummaryResponse(
        UUID attemptId,
        UUID testId,
        String testTitle,
        AttemptStatus status,
        BigDecimal score,
        BigDecimal maxScore,
        int correctCount,
        int wrongCount,
        int unattemptedCount,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        OffsetDateTime deadlineAt
) {}
