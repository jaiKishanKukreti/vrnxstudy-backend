package com.vrnxstudy.vrnxstudy_api.dto.test;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record AddTestQuestionRequest(
        @NotNull UUID questionId,
        @NotNull @Positive Integer displayOrder,
        @NotNull BigDecimal marks,
        @NotNull BigDecimal negativeMarks
) {}