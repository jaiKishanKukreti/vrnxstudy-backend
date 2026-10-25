package com.vrnxstudy.vrnxstudy_api.dto.test;

import com.vrnxstudy.vrnxstudy_api.entity.enums.TestType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record TestCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull TestType testType,
        UUID examId,
        UUID subjectId,
        UUID chapterId,
        @NotNull @Positive Integer durationMinutes,
        @NotNull @Positive Integer maxAttempts,
        Boolean negativeMarkingEnabled
) {}