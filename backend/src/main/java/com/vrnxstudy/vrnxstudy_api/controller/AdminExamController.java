package com.vrnxstudy.vrnxstudy_api.controller;

import com.vrnxstudy.vrnxstudy_api.dto.exam.*;
import com.vrnxstudy.vrnxstudy_api.service.ExamService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/exams")
public class AdminExamController {

    private final ExamService examService;

    public AdminExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    public ResponseEntity<ExamResponse> createExam(
            @Valid @RequestBody ExamCreateRequest request
    ) {
        ExamResponse response = examService.createExam(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> getExamById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                examService.getExamById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<ExamResponse>> getAllExams(
            @RequestParam(required = false) String search,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                examService.getAllExams(search, pageable)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> updateExam(
            @PathVariable UUID id,
            @Valid @RequestBody ExamUpdateRequest request
    ) {
        return ResponseEntity.ok(
                examService.updateExam(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ExamResponse> updateExamStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ExamStatusRequest request
    ) {
        return ResponseEntity.ok(
                examService.updateExamStatus(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveExam(
            @PathVariable UUID id
    ) {
        examService.archiveExam(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ExamResponse> restoreExam(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                examService.restoreExam(id)
        );
    }
}