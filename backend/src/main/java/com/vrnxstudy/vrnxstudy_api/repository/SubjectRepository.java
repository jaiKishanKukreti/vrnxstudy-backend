package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    Optional<Subject> findByIdAndDeletedAtIsNull(UUID id);

    Page<Subject> findAllByExam_IdAndDeletedAtIsNull(UUID examId, Pageable pageable);

    boolean existsByExam_IdAndNameIgnoreCaseAndDeletedAtIsNull(
            UUID examId,
            String name
    );

}
