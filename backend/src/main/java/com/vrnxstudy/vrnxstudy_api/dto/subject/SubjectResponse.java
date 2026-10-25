package com.vrnxstudy.vrnxstudy_api.dto.subject;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubjectResponse(
        UUID id,
        UUID examId,
        String name,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
