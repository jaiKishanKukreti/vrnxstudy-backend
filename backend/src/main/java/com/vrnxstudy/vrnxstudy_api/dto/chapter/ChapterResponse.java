package com.vrnxstudy.vrnxstudy_api.dto.chapter;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChapterResponse(
        UUID id,
        UUID subjectId,
        String name,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
