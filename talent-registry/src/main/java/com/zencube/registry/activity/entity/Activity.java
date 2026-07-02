package com.zencube.registry.activity.entity;

import com.zencube.registry.activity.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The Actor (e.g., USER)
    @Column(name = "trackable_type", nullable = false, updatable = false)
    private String trackableType;

    @Column(name = "trackable_id", nullable = false, updatable = false)
    private String trackableId;

    // The Target Entity (e.g., APPLICATION, OPENING)
    @Column(name = "target_type", nullable = false, updatable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false, updatable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, updatable = false)
    private ActivityType activityType;

    @Column(name = "description", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
