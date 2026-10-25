package com.vrnxstudy.vrnxstudy_api.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExamCreateRequest(
        @NotBlank(message="Exam name is required")
        @Size(max=150,message="Exam name cannot exceed 150 characters")
        String name,

        @Size(max=5000,message="Description cannot exceed 5000 characters")
        String description
) {
}
