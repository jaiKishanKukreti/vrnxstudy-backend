package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.TestQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TestQuestionOptionRepository extends JpaRepository<TestQuestionOption, UUID> {
    List<TestQuestionOption> findByTestQuestionIdOrderByDisplayOrderAsc(UUID testQuestionId);
}
