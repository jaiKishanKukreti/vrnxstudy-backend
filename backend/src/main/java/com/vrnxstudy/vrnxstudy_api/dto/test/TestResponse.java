package com.vrnxstudy.vrnxstudy_api.dto.test;

import com.vrnxstudy.vrnxstudy_api.entity.enums.TestStatus;
import com.vrnxstudy.vrnxstudy_api.entity.enums.TestType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TestResponse(
        UUID id,
        String title,
        TestType testType,
        UUID examId,
        UUID subjectId,
        UUID chapterId,
        Integer durationMinutes,
        Integer maxAttempts,
        boolean negativeMarkingEnabled,
        TestStatus status,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<TestQuestionResponse> questions
) {}