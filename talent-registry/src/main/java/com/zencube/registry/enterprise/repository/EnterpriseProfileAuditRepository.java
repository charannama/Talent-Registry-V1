package com.zencube.registry.enterprise.repository;

import com.zencube.registry.enterprise.entity.EnterpriseProfileAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnterpriseProfileAuditRepository extends JpaRepository<EnterpriseProfileAudit, UUID> {
}
