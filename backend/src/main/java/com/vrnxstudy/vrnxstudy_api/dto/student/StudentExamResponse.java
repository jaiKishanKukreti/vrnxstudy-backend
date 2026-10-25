// 1. ExamResponse.java
package com.vrnxstudy.vrnxstudy_api.dto.student;
import java.util.UUID;
public record StudentExamResponse(UUID id, String name, String description) {}