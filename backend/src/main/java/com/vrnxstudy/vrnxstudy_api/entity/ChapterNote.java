package com.vrnxstudy.vrnxstudy_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "chapter_notes", // SQL: CREATE TABLE chapter_notes
        // SQL: CONSTRAINT uq_notes_chapter_order UNIQUE (chapter_id, display_order)
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"chapter_id", "display_order"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // SQL: FOREIGN KEY (chapter_id) REFERENCES chapters(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false) // SQL: content TEXT NOT NULL
    private String content;

    @Column(name = "display_order", nullable = false) // SQL: display_order INT
    private Integer displayOrder;

    @Column(nullable = false, length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}