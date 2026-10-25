package com.vrnxstudy.vrnxstudy_api.dto.exam;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExamResponse(
        UUID id,
        String name,
        String description,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
