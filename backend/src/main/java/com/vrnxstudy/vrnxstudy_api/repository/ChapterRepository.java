package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    Optional<Chapter> findByIdAndDeletedAtIsNull(UUID id);
    Page<Chapter> findAllBySubject_IdAndDeletedAtIsNull(UUID subjectId,
                                                        Pageable pageable);
    boolean existsBySubject_IdAndNameIgnoreCaseAndDeletedAtIsNull(
            UUID subjectId,
            String name
    );

}
