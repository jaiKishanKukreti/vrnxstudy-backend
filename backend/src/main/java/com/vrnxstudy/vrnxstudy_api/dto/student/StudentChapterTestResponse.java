package com.vrnxstudy.vrnxstudy_api.dto.student;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StudentChapterTestResponse(
        UUID testId,
        String title,
        Integer durationMinutes,
        Integer maxAttempts,
        List<StudentAttemptHistory> attemptHistory // <-- इस स्टूडेंट के पिछले रिजल्ट्स
) {}