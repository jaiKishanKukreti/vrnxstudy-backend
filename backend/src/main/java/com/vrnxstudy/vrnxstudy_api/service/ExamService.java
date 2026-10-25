package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.dto.exam.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ExamService {

    ExamResponse createExam(ExamCreateRequest request);

    ExamResponse getExamById(UUID id);

    Page<ExamResponse> getAllExams(String search, Pageable pageable);

    ExamResponse updateExam(UUID id, ExamUpdateRequest request);

    ExamResponse updateExamStatus(UUID id, ExamStatusRequest request);

    void archiveExam(UUID id);

    ExamResponse restoreExam(UUID id);
}