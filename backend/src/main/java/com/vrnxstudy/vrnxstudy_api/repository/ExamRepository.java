package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {

        Optional<Exam> findByIdAndDeletedAtIsNull(UUID id);
        Page<Exam> findAllByDeletedAtIsNull(Pageable pageable);

        Page<Exam> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name,Pageable pageable);


}
