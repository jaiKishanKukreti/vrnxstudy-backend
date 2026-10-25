package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestQuestionRepository
        extends JpaRepository<TestQuestion, UUID> {

    List<TestQuestion> findByTestIdOrderByDisplayOrderAsc(UUID testId);

    boolean existsByTestIdAndQuestionId(UUID testId, UUID questionId);

    Optional<TestQuestion> findByIdAndTestId(UUID id, UUID testId);

    long countByTestId(UUID testId);
}