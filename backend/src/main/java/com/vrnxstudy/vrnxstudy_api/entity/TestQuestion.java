package com.vrnxstudy.vrnxstudy_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "test_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_test_question",
                        columnNames = {"test_id", "question_id"}
                ),
                @UniqueConstraint(
                        name = "uq_test_question_order",
                        columnNames = {"test_id", "display_order"}
                ),
                @UniqueConstraint(
                        name = "uq_test_question_id_test",
                        columnNames = {"id", "test_id"}
                )
        }
)
@Getter
@Setter
public class TestQuestion {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "question_text_snapshot",
            nullable = false, columnDefinition = "TEXT")
    private String questionTextSnapshot;

    @Column(name = "explanation_snapshot", columnDefinition = "TEXT")
    private String explanationSnapshot;

    @Column(name = "question_type_snapshot", nullable = false, length = 30)
    private String questionTypeSnapshot;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal marks;

    @Column(name = "negative_marks", nullable = false, precision = 7, scale = 2)
    private BigDecimal negativeMarks;

    @OneToMany(mappedBy = "testQuestion",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<TestQuestionOption> options = new ArrayList<>();
}