package com.vrnxstudy.vrnxstudy_api.entity;

import com.vrnxstudy.vrnxstudy_api.entity.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_attempts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_attempt_user_idempotency", columnNames = {"user_id", "idempotency_key"})
})
@Getter
@Setter
public class TestAttempt {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "max_score", nullable = false, precision = 9, scale = 2)
    private BigDecimal maxScore = BigDecimal.ZERO;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "wrong_count", nullable = false)
    private int wrongCount;

    @Column(name = "unattempted_count", nullable = false)
    private int unattemptedCount;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "deadline_at", nullable = false)
    private OffsetDateTime deadlineAt;

    @PrePersist
    void beforeInsert() {
        if (startedAt == null) startedAt = OffsetDateTime.now();
        if (deadlineAt == null && test != null && test.getDurationMinutes() != null) {
            deadlineAt = startedAt.plusMinutes(test.getDurationMinutes());
        }
        if (score == null) score = BigDecimal.ZERO;
        if (maxScore == null) maxScore = BigDecimal.ZERO;
    }
}
