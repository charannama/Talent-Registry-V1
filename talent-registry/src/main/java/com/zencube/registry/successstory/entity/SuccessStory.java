package com.zencube.registry.successstory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "success_stories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessStory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "application_id", nullable = false, unique = true, updatable = false)
    private UUID applicationId;

    // --- Historical Snapshots ---
    @Column(name = "student_name", nullable = false, updatable = false)
    private String studentName;

    @Column(name = "enterprise_name", nullable = false, updatable = false)
    private String enterpriseName;

    @Column(name = "opening_title", nullable = false, updatable = false)
    private String openingTitle;

    @Column(name = "selected_at", nullable = false, updatable = false)
    private Instant selectedAt;

    // --- Modifiable Business Data ---
    @Column(name = "testimonial", columnDefinition = "TEXT")
    private String testimonial;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    // --- Audit ---
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
