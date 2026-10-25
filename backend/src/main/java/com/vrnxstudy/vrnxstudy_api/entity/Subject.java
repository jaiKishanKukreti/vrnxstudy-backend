package com.vrnxstudy.vrnxstudy_api.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="subjects",
uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id","name"})
}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,length=20)
    private String status;

    @CreationTimestamp
    @Column(name="created_at",nullable = false,updatable = false)
    private OffsetDateTime createdAt;


    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;


}
