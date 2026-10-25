package com.vrnxstudy.vrnxstudy_api.dto.test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TestQuestionResponse(
        UUID id,
        UUID questionId,
        Integer displayOrder,
        String questionText,
        String explanation,
        String questionType,
        BigDecimal marks,
        BigDecimal negativeMarks,
        List<TestQuestionOptionResponse> options
) {}