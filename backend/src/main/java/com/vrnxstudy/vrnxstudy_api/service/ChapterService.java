package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterResponse;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterStatusRequest;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChapterService {

ChapterResponse createChapter(UUID subjectId,
                              ChapterCreateRequest request);
ChapterResponse getChapterById(UUID chapterId);

Page<ChapterResponse> getChaptersBySubject(
        UUID subjectId,
        Pageable pageable
);
ChapterResponse updateChapter(
        UUID chapterId,
        ChapterUpdateRequest request
);
ChapterResponse updateChapterStatus(
        UUID id,
        ChapterStatusRequest request
);

void deleteChapter(UUID chapterId);

ChapterResponse restoreChapter(UUID chapterId);

}
