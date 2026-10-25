package com.vrnxstudy.vrnxstudy_api.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExamStatusRequest(
        @NotBlank(message="Status  is required")
        @Pattern(
                regexp="DRAFT|PUBLISHED",
                message="Status must be DRAFT or PUBLISHED"
        )
        String status
) {
}
