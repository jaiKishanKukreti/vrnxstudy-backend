package com.vrnxstudy.vrnxstudy_api.service.impl;

import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterResponse;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterStatusRequest;
import com.vrnxstudy.vrnxstudy_api.dto.chapter.ChapterUpdateRequest;
import com.vrnxstudy.vrnxstudy_api.entity.Chapter;
import com.vrnxstudy.vrnxstudy_api.entity.Subject;
import com.vrnxstudy.vrnxstudy_api.exception.ResourceNotFoundException;
import com.vrnxstudy.vrnxstudy_api.repository.ChapterRepository;
import com.vrnxstudy.vrnxstudy_api.repository.SubjectRepository;
import com.vrnxstudy.vrnxstudy_api.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class ChapterServiceImpl implements ChapterService{

    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public ChapterResponse createChapter(UUID subjectId, ChapterCreateRequest request) {
        Subject subject= subjectRepository
                .findByIdAndDeletedAtIsNull(
                        subjectId
                )
                .orElseThrow(()->
                        new IllegalArgumentException("Subject not found: "+subjectId));
        String name= request.name().trim();
        if(chapterRepository
                .existsBySubject_IdAndNameIgnoreCaseAndDeletedAtIsNull(subjectId,name)){
            throw new IllegalStateException(
                    "Chapter already exisits in this subject"
            );
        }
        Chapter chapter= Chapter.builder()
                .subject(subject)
                .name(name)
                .status("DRAFT")
                .build();

        return mapToChapterResponse(chapterRepository.saveAndFlush(chapter));
    }

    @Override
    @Transactional(readOnly=true)
    public ChapterResponse getChapterById(UUID chapterId) {
        Chapter chapter= findActiveChapter(chapterId);

        return mapToChapterResponse(chapter);
    }

    @Override
    @Transactional(readOnly=true)
    public Page<ChapterResponse> getChaptersBySubject(UUID subjectId, Pageable pageable) {
        subjectRepository.findByIdAndDeletedAtIsNull(subjectId)
                .orElseThrow(()->
                        new IllegalArgumentException("Subject not found"));

        return chapterRepository
                .findAllBySubject_IdAndDeletedAtIsNull(
                        subjectId,pageable
                ).map(this::mapToChapterResponse);
    }

    @Override
    public ChapterResponse updateChapter(UUID chapterId, ChapterUpdateRequest request) {
        Chapter chapter = findActiveChapter(chapterId);
        String name= request.name().trim();

        if(!chapter.getName().equalsIgnoreCase(name)
        && chapterRepository
                .existsBySubject_IdAndNameIgnoreCaseAndDeletedAtIsNull(
                        chapter.getSubject().getId(),name
                )){
            throw new IllegalStateException(
                    "Another chapter with this name already exists"
            );
        }
        chapter.setName(name);
        return mapToChapterResponse(chapterRepository.save(chapter));
    }

    @Override
    public ChapterResponse updateChapterStatus(UUID chapterId, ChapterStatusRequest request) {
        Chapter chapter = findActiveChapter(chapterId);
        chapter.setStatus(request.status());
        return mapToChapterResponse(chapterRepository.save(chapter));

    }

    @Override
    public void deleteChapter(UUID chapterId) {
        Chapter chapter =findActiveChapter(chapterId);

        chapter.setDeletedAt(OffsetDateTime.now());

        chapterRepository.save(chapter);

    }

    @Override
    public ChapterResponse restoreChapter(UUID chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(()->
                        new IllegalArgumentException("Chapter not found: "+chapterId));
        if( chapter.getDeletedAt()==null){
            throw new IllegalStateException(
                    "Chapter is not deleted"
            );
        }
        UUID subjectId= chapter.getSubject().getId();

        subjectRepository.findByIdAndDeletedAtIsNull(
                subjectId
        ).orElseThrow(()->
                new IllegalStateException(
                        "Cannot restore Chapter: subject is deleted"
                ));
        if(chapterRepository.existsBySubject_IdAndNameIgnoreCaseAndDeletedAtIsNull(
                subjectId,chapter.getName()
        )){
            throw new IllegalStateException(
                    "Cannot restore: another active chapter has the same name"
            );
        }

        chapter.setDeletedAt(null);
        return mapToChapterResponse(chapterRepository.save(chapter));
    }

    private ChapterResponse mapToChapterResponse(Chapter chapter){
        return new ChapterResponse(
                chapter.getId(),
                chapter.getSubject().getId(),
                chapter.getName(),
                chapter.getStatus(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt()
        );
    }
    private Chapter findActiveChapter(UUID chapterId){
        return chapterRepository.findByIdAndDeletedAtIsNull(chapterId)
                .orElseThrow(
                        ()->
                                new IllegalArgumentException("Chapter not found: "+chapterId)
                );
    }
}
