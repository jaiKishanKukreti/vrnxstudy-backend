
package com.vrnxstudy.vrnxstudy_api.dto.question;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        UUID chapterId,
        String questionType,
        String questionText,
        String explanation,
        String difficulty,
        BigDecimal marks,
        BigDecimal negativeMarks,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<OptionResponse> options
) {
    public record OptionResponse(
            UUID id,
            String optionKey,
            String optionText,
            Integer displayOrder,
            Boolean isCorrect
    ) {}
}
