package com.zencube.registry.expressinterest.entity;

import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.expressinterest.enums.InterestStage;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.profile.entity.StudentProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "express_interests",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"enterprise_id", "student_id", "opening_id"})
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpressInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private EnterpriseAccount enterprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opening_id")
    private Opening opening;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 50)
    private InterestStage stage;

    @Column(name = "requested_at")
    private Instant requestedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
