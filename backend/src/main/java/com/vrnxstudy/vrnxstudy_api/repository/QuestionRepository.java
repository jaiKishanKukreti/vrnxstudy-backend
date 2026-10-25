package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByChapter_IdAndDeletedAtIsNull(UUID chapterId);

    Optional<Question> findByIdAndDeletedAtIsNull(UUID id);
}