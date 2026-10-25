package com.vrnxstudy.vrnxstudy_api.controller;


import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectResponse;
import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectUpdateRequest;
import com.vrnxstudy.vrnxstudy_api.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/admin")
@Tag(
        name = "Admin- Subject"

)
public class AdminSubjectController {
    private final SubjectService subjectService;

    public AdminSubjectController(SubjectService subjectService){
        this.subjectService=subjectService;
    }

    //1. Create Subject under an Exam
    @Operation(
            summary = "create a subject, using examId"
    )
    @PostMapping("/exams/{examId}/subjects")
    public ResponseEntity<SubjectResponse> createSubject(
            @PathVariable("examId") UUID examId,
            @Valid @RequestBody SubjectCreateRequest request
            ){
        SubjectResponse response=
                subjectService
                        .createSubject(examId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //2. Get all subjects of an exam
    @Operation(
            summary = "Get all the subject of an Exam, using examId"
    )
    @GetMapping("/exams/{examId}/subjects")
    public ResponseEntity<Page<SubjectResponse>> getSubjectsByExam(
            @PathVariable UUID examId,
            @PageableDefault(
                    size=20,
                    sort="createdAt",
                    direction= Sort.Direction.DESC
            ) Pageable pageable
    ){
        return ResponseEntity.ok(
                subjectService.getSubjectsByExam(examId,pageable)
        );
    }

    //3. Get Subject By ID
    @Operation(
            summary = "get a subject By subject Idr"
    )
    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<SubjectResponse> getSubjectById(
            @PathVariable UUID subjectId
    ){
     return ResponseEntity.ok(
             subjectService.getSubjectById(subjectId)
     );
    }

    //4. Update Subject
    @Operation(
            summary="Update subject using subject Id"
    )
    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable UUID subjectId,
            @Valid @RequestBody SubjectUpdateRequest request
    ){
        return ResponseEntity.ok(
                subjectService.updateSubject(subjectId,request)
        );
    }

    @Operation(
            summary = "restoring subject with subject Id"
    )
    @PatchMapping("/subjects/{subjectId}/restore")
    public ResponseEntity<SubjectResponse> restoreSubject(
            @PathVariable UUID subjectId
    ){
        return ResponseEntity.ok(
                subjectService.restoreSubject(subjectId)
        );
    }

}
