package com.vrnxstudy.vrnxstudy_api.dto.attempt;

import com.vrnxstudy.vrnxstudy_api.entity.enums.AttemptStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StartAttemptResponse(
        UUID attemptId,
        UUID testId,
        String testTitle,
        AttemptStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime deadlineAt,
        List<AttemptQuestionResponse> questions
) {}
