
package com.vrnxstudy.vrnxstudy_api.controller;

import com.vrnxstudy.vrnxstudy_api.dto.test.AddTestQuestionRequest;
import com.vrnxstudy.vrnxstudy_api.dto.test.TestCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.test.TestQuestionResponse;
import com.vrnxstudy.vrnxstudy_api.dto.test.TestResponse;
import com.vrnxstudy.vrnxstudy_api.service.TestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;



@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // 1. Create a new test
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestResponse createTest(
            @Valid @RequestBody TestCreateRequest request,
            @RequestParam UUID creatorId
    ) {
        return testService.createTest(request, creatorId);
    }

    // 2. Get test by ID
    @GetMapping("/{testId}")
    public TestResponse getTest(
            @PathVariable UUID testId
    ) {
        return testService.getTest(testId);
    }

    // 3. Get all tests created by a user
    @GetMapping("/my")
    public List<TestResponse> getMyTests(
            @RequestParam UUID creatorId
    ) {
        return testService.getMyTests(creatorId);
    }

    // 4. Add a question to a test
    @PostMapping("/{testId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public TestQuestionResponse addQuestion(
            @PathVariable UUID testId,
            @Valid @RequestBody AddTestQuestionRequest request,
            @RequestParam UUID creatorId
    ) {
        return testService.addQuestion(testId, request, creatorId);
    }

    // 5. Publish a test
    @PatchMapping("/{testId}/publish")
    public TestResponse publishTest(
            @PathVariable UUID testId,
            @RequestParam UUID creatorId
    ) {
        return testService.publishTest(testId, creatorId);
    }

    // 6. Remove a question from a draft test
    @DeleteMapping("/{testId}/questions/{testQuestionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeQuestion(
            @PathVariable UUID testId,
            @PathVariable UUID testQuestionId,
            @RequestParam UUID creatorId
    ) {
        testService.removeQuestion(
                testId,
                testQuestionId,
                creatorId
        );
    }
}
