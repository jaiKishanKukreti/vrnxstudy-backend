package com.vrnxstudy.vrnxstudy_api.service.impl;

import com.vrnxstudy.vrnxstudy_api.dto.exam.*;
import com.vrnxstudy.vrnxstudy_api.entity.Exam;
import com.vrnxstudy.vrnxstudy_api.exception.ResourceNotFoundException;
import com.vrnxstudy.vrnxstudy_api.repository.ExamRepository;
import com.vrnxstudy.vrnxstudy_api.service.ExamService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;



@Service
@Transactional
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;

    public ExamServiceImpl(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    public ExamResponse createExam(ExamCreateRequest request) {

        Exam exam = Exam.builder()
                .name(request.name().trim())
                .description(request.description())
                .status("DRAFT")
                .build();

        Exam savedExam = examRepository.saveAndFlush(exam);

        return toResponse(savedExam);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponse getExamById(UUID id) {

        Exam exam = findActiveExam(id);

        return toResponse(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamResponse> getAllExams(
            String search,
            Pageable pageable
    ) {

        Page<Exam> exams;

        if (search == null || search.isBlank()) {
            exams = examRepository.findAllByDeletedAtIsNull(pageable);
        } else {
            exams = examRepository
                    .findByNameContainingIgnoreCaseAndDeletedAtIsNull(
                            search.trim(),
                            pageable
                    );
        }

        return exams.map(this::toResponse);
    }

    @Override
    public ExamResponse updateExam(
            UUID id,
            ExamUpdateRequest request
    ) {

        Exam exam = findActiveExam(id);

        exam.setName(request.name().trim());
        exam.setDescription(request.description());

        Exam updatedExam = examRepository.save(exam);

        return toResponse(updatedExam);
    }

    @Override
    public ExamResponse updateExamStatus(
            UUID id,
            ExamStatusRequest request
    ) {

        Exam exam = findActiveExam(id);

        exam.setStatus(request.status());

        Exam updatedExam = examRepository.save(exam);

        return toResponse(updatedExam);
    }

    @Override
    public void archiveExam(UUID id) {

        Exam exam = findActiveExam(id);

        exam.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));

        examRepository.save(exam);
    }

    @Override
    public ExamResponse restoreExam(UUID id) {

        // findById also finds archived exams.
        Exam exam = examRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exam not found with id: " + id
                        )
                );

        exam.setDeletedAt(null);

        Exam restoredExam = examRepository.save(exam);

        return toResponse(restoredExam);
    }

    private Exam findActiveExam(UUID id) {

        return examRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exam not found with id: " + id
                        )
                );
    }

    private ExamResponse toResponse(Exam exam) {

        return new ExamResponse(
                exam.getId(),
                exam.getName(),
                exam.getDescription(),
                exam.getStatus(),
                exam.getCreatedAt(),
                exam.getUpdatedAt()
        );
    }
}