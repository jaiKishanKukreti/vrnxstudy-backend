
package com.vrnxstudy.vrnxstudy_api.controller;

import com.vrnxstudy.vrnxstudy_api.dto.question.QuestionCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.question.QuestionResponse;
import com.vrnxstudy.vrnxstudy_api.dto.question.QuestionUpdateRequest;
import com.vrnxstudy.vrnxstudy_api.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;



@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(
            @Valid @RequestBody QuestionCreateRequest request) {

        QuestionResponse response = questionService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(questionService.getById(id));
    }

    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<List<QuestionResponse>> getByChapter(
            @PathVariable UUID chapterId) {

        return ResponseEntity.ok(
                questionService.getByChapter(chapterId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody QuestionUpdateRequest request) {

        return ResponseEntity.ok(
                questionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
