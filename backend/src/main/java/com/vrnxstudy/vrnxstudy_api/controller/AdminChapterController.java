
package com.vrnxstudy.vrnxstudy_api.controller;

import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterResponse;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterStatusRequest;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterUpdateRequest;
import com.vrnxstudy.vrnxstudy_api.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminChapterController {

    private final ChapterService chapterService;

    // Create Chapter under a Subject
    @PostMapping("/subjects/{subjectId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public ChapterResponse createChapter(
            @PathVariable UUID subjectId,
            @Valid @RequestBody ChapterCreateRequest request
    ) {
        return chapterService.createChapter(subjectId, request);
    }

    // Get all Chapters under a Subject
    @GetMapping("/subjects/{subjectId}/chapters")
    public Page<ChapterResponse> getChaptersBySubject(
            @PathVariable UUID subjectId,

            @PageableDefault(
                    size=20,
                    sort="createdAt",
                    direction= Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return chapterService.getChaptersBySubject(subjectId, pageable);
    }

    // Get a single Chapter
    @GetMapping("/chapters/{chapterId}")
    public ChapterResponse getChapterById(
            @PathVariable UUID chapterId
    ) {
        return chapterService.getChapterById(chapterId);
    }

    // Update Chapter name/details
    @PutMapping("/chapters/{chapterId}")
    public ChapterResponse updateChapter(
            @PathVariable UUID chapterId,
            @Valid @RequestBody ChapterUpdateRequest request
    ) {
        return chapterService.updateChapter(chapterId, request);
    }

    // Update Chapter status
    @PatchMapping("/chapters/{chapterId}/status")
    public ChapterResponse updateChapterStatus(
            @PathVariable UUID chapterId,
            @Valid @RequestBody ChapterStatusRequest request
    ) {
        return chapterService.updateChapterStatus(chapterId, request);
    }

    // Soft-delete Chapter
    @DeleteMapping("/chapters/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChapter(
            @PathVariable UUID chapterId
    ) {
        chapterService.deleteChapter(chapterId);
    }

    // Restore soft-deleted Chapter
    @PatchMapping("/chapters/{chapterId}/restore")
    public ChapterResponse restoreChapter(
            @PathVariable UUID chapterId
    ) {
        return chapterService.restoreChapter(chapterId);
    }
}
