package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectCreateRequest;
import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectResponse;
import com.vrnxstudy.vrnxstudy_api.dto.subject.SubjectUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SubjectService {

    SubjectResponse createSubject(UUID examId,SubjectCreateRequest request);
    Page<SubjectResponse> getSubjectsByExam(UUID id, Pageable pageable);

    SubjectResponse getSubjectById(UUID subjectId);

    SubjectResponse updateSubject(UUID subjectId, SubjectUpdateRequest request);
    void deleteSubject(UUID subjectId);
    SubjectResponse restoreSubject(UUID subjectId);
}
