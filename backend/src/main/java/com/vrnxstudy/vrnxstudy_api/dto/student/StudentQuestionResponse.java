package com.vrnxstudy.vrnxstudy_api.dto.student;
import java.util.List;
import java.util.UUID;
public record StudentQuestionResponse(
        UUID id,
        String questionText,
        String questionType,
        List<StudentOptionResponse> options
) {}