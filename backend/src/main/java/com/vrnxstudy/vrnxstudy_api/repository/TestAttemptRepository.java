package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.TestAttempt;
import com.vrnxstudy.vrnxstudy_api.entity.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, UUID> {
    Optional<TestAttempt> findByIdAndUserId(UUID id, UUID userId);
    Optional<TestAttempt> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
    List<TestAttempt> findByUserIdOrderByStartedAtDesc(UUID userId);
    long countByUserIdAndTest_IdAndStatusNot(UUID userId, UUID testId, AttemptStatus excludedStatus);

}
