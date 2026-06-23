package com.zencube.registry.enterprise.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "enterprise_access_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterpriseAccessAudit extends BaseEntity {

    @Column(name = "enterprise_id", nullable = false)
    private UUID enterpriseId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EnterpriseOnboardingStatus status;

    @Column(name = "endpoint")
    private String endpoint;
}
