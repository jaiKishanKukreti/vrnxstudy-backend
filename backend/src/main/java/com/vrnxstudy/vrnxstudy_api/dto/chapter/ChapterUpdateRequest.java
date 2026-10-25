package com.vrnxstudy.vrnxstudy_api.dto.chapter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChapterUpdateRequest(

        @NotBlank(message="Chapter name is required")
                @Size(max=150, message="Chapter name cannot exceed 150 characters")
        String name
) {
}
