package com.zencube.registry.enterprise.repository;

import com.zencube.registry.enterprise.entity.EnterpriseStatusAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnterpriseStatusAuditRepository extends JpaRepository<EnterpriseStatusAudit, UUID> {
    List<EnterpriseStatusAudit> findByEnterpriseIdOrderByCreatedAtDesc(UUID enterpriseId);
}
