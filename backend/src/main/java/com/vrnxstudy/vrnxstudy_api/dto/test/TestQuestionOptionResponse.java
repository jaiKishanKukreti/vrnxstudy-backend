package com.vrnxstudy.vrnxstudy_api.dto.test;

import java.util.UUID;

public record TestQuestionOptionResponse(
        UUID id,
        String optionKey,
        String optionText,
        Integer displayOrder
) {}