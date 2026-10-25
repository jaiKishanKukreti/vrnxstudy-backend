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
@Table(name="chapters",
uniqueConstraints = {
        @UniqueConstraint(columnNames = {"subject_id","name"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false,length=150)
    private String name;

    @Column(nullable = false,length=20)
    private String status;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable=false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at",nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;

}
