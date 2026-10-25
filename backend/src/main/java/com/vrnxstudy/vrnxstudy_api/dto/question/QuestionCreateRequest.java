
package com.vrnxstudy.vrnxstudy_api.dto.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record QuestionCreateRequest(

        @NotNull
        UUID chapterId,

        @NotBlank
        String questionType,

        @NotBlank
        String questionText,

        String explanation,

        String difficulty,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 5, fraction = 2)
        BigDecimal marks,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 5, fraction = 2)
        BigDecimal negativeMarks,

        String status,

        @NotEmpty
        List<@Valid QuestionOptionRequest> options

) {}
