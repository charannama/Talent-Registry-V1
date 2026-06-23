package com.zencube.registry.enterprise.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enterprise_profile_audits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseProfileAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private EnterpriseAccount enterprise;

    @Column(name = "previous_company_name")
    private String previousCompanyName;

    @Column(name = "previous_domain_email")
    private String previousDomainEmail;

    @Column(name = "change_summary")
    private String changeSummary;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at", updatable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
}
