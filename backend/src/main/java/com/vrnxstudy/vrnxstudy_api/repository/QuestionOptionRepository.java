package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionOptionRepository
        extends JpaRepository<QuestionOption, UUID> {

    List<QuestionOption> findByQuestion_Id(UUID questionId);
}