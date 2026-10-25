package com.vrnxstudy.vrnxstudy_api.service.impl;


import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectResponse;
import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectUpdateRequest;
import com.vrnxstudy.vrnxstudy_api.entity.Exam;
import com.vrnxstudy.vrnxstudy_api.entity.Subject;
import com.vrnxstudy.vrnxstudy_api.exception.ResourceNotFoundException;
import com.vrnxstudy.vrnxstudy_api.repository.ExamRepository;
import com.vrnxstudy.vrnxstudy_api.repository.SubjectRepository;
import com.vrnxstudy.vrnxstudy_api.service.SubjectService;
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
public class SubjectServiceImpl implements SubjectService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;


    @Override
    public SubjectResponse createSubject(UUID examId, SubjectCreateRequest request) {
        Exam exam= examRepository.findByIdAndDeletedAtIsNull(examId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Active exam not found: "+ examId
                                )
                );
        boolean duplicate =subjectRepository
                .existsByExam_IdAndNameIgnoreCaseAndDeletedAtIsNull(
                        examId,
                        request.name().trim()
                );
        if(duplicate){
            throw new IllegalArgumentException(
                    "Subject Already exists in this exam"
            );
        }

        Subject subject= Subject.builder()
                .name(request.name().trim())
                .exam(exam)
                .status("DRAFT")
                .build();
        Subject savedSubject= subjectRepository.saveAndFlush(subject);

        return mapToSubjectRespose(savedSubject);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectResponse> getSubjectsByExam(UUID examId, Pageable pageable) {
            if(!examRepository.existsById(examId)){
                throw new ResourceNotFoundException(
                        "Exam not found: "+examId
                );
            }
            return subjectRepository
                    .findAllByExam_IdAndDeletedAtIsNull(examId,pageable)
                    .map(this::mapToSubjectRespose);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(UUID subjectId) {
        Subject subject = subjectRepository
                .findByIdAndDeletedAtIsNull(subjectId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException("Subject not found: "+subjectId)
                );

        return mapToSubjectRespose(subject);

    }

    @Override
    public SubjectResponse updateSubject(UUID subjectId, SubjectUpdateRequest request) {
        Subject subject= subjectRepository
                .findByIdAndDeletedAtIsNull(subjectId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Subject not found: "+subjectId
                        )
                );
            String newName= request.name().trim();

            boolean duplicate= subjectRepository
                    .existsByExam_IdAndNameIgnoreCaseAndDeletedAtIsNull(
                            subject.getExam().getId(),
                            newName
                    );
            if(duplicate && !subject.getName().equalsIgnoreCase(newName)){
                throw new IllegalArgumentException(
                        "Another subject with this name already exists"
                );
            }
            subject.setName(newName);
            return mapToSubjectRespose(subjectRepository.save(subject));
    }

    @Override
    public void deleteSubject(UUID subjectId) {
        Subject subject =subjectRepository
                .findByIdAndDeletedAtIsNull(subjectId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Subject not found: "+subjectId
                                )
                );
        subject.setDeletedAt(OffsetDateTime.now());
        subjectRepository.save(subject);

    }

    @Override
    public SubjectResponse restoreSubject(UUID subjectId) {
        Subject subject=subjectRepository
                .findByIdAndDeletedAtIsNull(subjectId)
                .orElseThrow( ()->
                       new ResourceNotFoundException(
                               "Subject not found: "+ subjectId
                       )
                );
        if(subject.getDeletedAt()==null){
            throw new IllegalArgumentException(

                            "Subject is not deleted"
            );
        }

        if(subject.getExam().getDeletedAt()!=null){
            throw new IllegalArgumentException(
                    "Cannot restore subject while its exam is archived "
            );
        }

        boolean duplicate= subjectRepository
                .existsByExam_IdAndNameIgnoreCaseAndDeletedAtIsNull(
                        subject.getExam().getId(),
                        subject.getName()
                );
        if(duplicate){
            throw new IllegalArgumentException(
                    "Cannot restore: another active subject has the same name"
            );
        }
        subject.setDeletedAt(null);
        return mapToSubjectRespose(subjectRepository.save(subject));
    }
    private SubjectResponse mapToSubjectRespose(Subject subject){
        return new SubjectResponse(
                subject.getId(),
                subject.getExam().getId(),
                subject.getName(),
                subject.getStatus().toString(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
