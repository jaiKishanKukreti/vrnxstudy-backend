package com.vrnxstudy.vrnxstudy_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attempt_answers", uniqueConstraints = {
        @UniqueConstraint(name = "uq_answer_attempt_question", columnNames = {"attempt_id", "test_question_id"})
})
@Getter
@Setter
public class AttemptAnswer {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_question_id", nullable = false)
    private TestQuestion testQuestion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_option_ids", nullable = false, columnDefinition = "jsonb")
    private List<UUID> selectedOptionIds = new ArrayList<>();

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "marks_earned", nullable = false, precision = 9, scale = 2)
    private BigDecimal marksEarned = BigDecimal.ZERO;

    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt;

    @PrePersist
    @PreUpdate
    void updateSavedAt() {
        savedAt = OffsetDateTime.now();
        if (selectedOptionIds == null) selectedOptionIds = new ArrayList<>();
        if (marksEarned == null) marksEarned = BigDecimal.ZERO;
    }
}
