package com.vrnxstudy.vrnxstudy_api.dto.student;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StudentAttemptHistory(
        UUID attemptId,
        String status,       // SUBMITTED, EXPIRED, IN_PROGRESS
        BigDecimal score,
        BigDecimal maxScore,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt
) {}