package com.vrnxstudy.vrnxstudy_api.dto.attempt;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SaveAnswerRequest(@NotNull List<@NotNull UUID> selectedOptionIds) {}
