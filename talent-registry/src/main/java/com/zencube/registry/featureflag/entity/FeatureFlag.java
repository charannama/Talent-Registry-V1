package com.zencube.registry.featureflag.entity;

import com.zencube.registry.featureflag.enums.FeatureAudience;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feature_flags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The unique string key checked by the @ConditionalOnFeature aspect (e.g. "INTERVIEW_MODULE")
    @Column(name = "flag_key", nullable = false, unique = true)
    private String flagKey;

    // Master switch. If false, the feature is disabled
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    // Explanation of what this feature flag governs
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Allows targeting specific audiences like HR or ENTERPRISE
    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", nullable = false, length = 50)
    @Builder.Default
    private FeatureAudience appliesTo = FeatureAudience.ALL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
