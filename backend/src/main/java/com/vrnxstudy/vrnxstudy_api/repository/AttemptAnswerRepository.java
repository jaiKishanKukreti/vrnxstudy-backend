package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, UUID> {
    List<AttemptAnswer> findByAttempt_IdOrderByTestQuestion_DisplayOrderAsc(UUID attemptId);
    Optional<AttemptAnswer> findByAttempt_IdAndTestQuestion_Id(UUID attemptId, UUID testQuestionId);
}
