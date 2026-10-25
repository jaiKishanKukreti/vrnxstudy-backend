
package com.vrnxstudy.vrnxstudy_api.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuestionOptionRequest(

        @NotBlank
        String optionKey,

        @NotBlank
        String optionText,

        @NotNull
        @Positive
        Integer displayOrder,

        @NotNull
        Boolean isCorrect

) {}
