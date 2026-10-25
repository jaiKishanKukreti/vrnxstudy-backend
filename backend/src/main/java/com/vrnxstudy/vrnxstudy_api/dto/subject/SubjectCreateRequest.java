package com.vrnxstudy.vrnxstudy_api.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubjectCreateRequest(
        @NotBlank(message="Subject name is required")
        @Size(max=150,message="Subject name cannot exceed 150 characters")
        String name
) {
}
