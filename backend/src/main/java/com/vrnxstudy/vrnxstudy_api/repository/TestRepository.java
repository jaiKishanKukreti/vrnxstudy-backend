package com.vrnxstudy.vrnxstudy_api.repository;

import com.vrnxstudy.vrnxstudy_api.entity.Test;
import com.vrnxstudy.vrnxstudy_api.entity.enums.TestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestRepository extends JpaRepository<Test, UUID> {
    List<Test> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);
    List<Test> findByStatusOrderByCreatedAtDesc(TestStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Test t where t.id = :id")
    Optional<Test> lockById(@Param("id") UUID id);
}
