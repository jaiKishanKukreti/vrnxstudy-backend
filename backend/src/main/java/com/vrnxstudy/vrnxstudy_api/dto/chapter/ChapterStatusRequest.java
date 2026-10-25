package com.vrnxstudy.vrnxstudy_api.dto.chapter;

import jakarta.validation.constraints.Pattern;

public record ChapterStatusRequest(
        @Pattern(
               regexp = "DRAFT|PUBLISHED",
                message="status must be DRAFT or PUBLISHED"

        )
        String status
){
}
