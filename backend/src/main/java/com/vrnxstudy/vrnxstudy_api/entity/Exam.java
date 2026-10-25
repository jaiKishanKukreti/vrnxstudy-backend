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
@Table(name="exams")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Exam {


    @Id //SQl: Primary Key
    @GeneratedValue(strategy = GenerationType.UUID) // SQl: Default gen_random_uuid()
    private UUID id;

    @Column(nullable = false) // SQL: NOt null
    private String name;

    @Column(columnDefinition ="TEXT")//
    private String description;


    @Column(nullable = false,length=20)
    private String status;

    @CreationTimestamp
    @Column(name="created_at",nullable = false, updatable = false)
    private OffsetDateTime createdAt;


    @UpdateTimestamp
    @Column(name="updated_at",nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;
}
