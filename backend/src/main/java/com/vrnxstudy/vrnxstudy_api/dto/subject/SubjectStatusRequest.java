package com.vrnxstudy.vrnxstudy_api.dto.subject;

import jakarta.validation.constraints.Pattern;

public record SubjectStatusRequest(

        @Pattern(
                regexp = "DRAFT|PUBLISHED",
                message="status must be DRAFT or PUBLISHED"
        )
        String status
) {
}
