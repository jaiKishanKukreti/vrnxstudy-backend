package com.vrnxstudy.vrnxstudy_api.dto.attempt;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AttemptQuestionResponse(
        UUID testQuestionId,
        Integer displayOrder,
        String questionText,
        String questionType,
        BigDecimal marks,
        List<AttemptOptionResponse> options,
        List<UUID> selectedOptionIds
) {}
