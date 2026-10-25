package com.vrnxstudy.vrnxstudy_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "test_question_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_snapshot_option_key",
                        columnNames = {"test_question_id", "option_key"}
                ),
                @UniqueConstraint(
                        name = "uq_snapshot_option_order",
                        columnNames = {"test_question_id", "display_order"}
                ),
                @UniqueConstraint(
                        name = "uq_snapshot_option_id_question",
                        columnNames = {"id", "test_question_id"}
                )
        }
)
@Getter
@Setter
public class TestQuestionOption {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_question_id", nullable = false)
    private TestQuestion testQuestion;

    @Column(name = "source_option_id")
    private UUID sourceOptionId;

    @Column(name = "option_key", nullable = false, length = 10)
    private String optionKey;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;
}