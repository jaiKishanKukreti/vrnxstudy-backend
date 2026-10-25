package com.vrnxstudy.vrnxstudy_api.dto.attempt;

import java.util.UUID;

public record AttemptOptionResponse(UUID id, String optionKey, String optionText, Integer displayOrder) {}
