package com.zencube.registry.enterprise.repository;

import com.zencube.registry.enterprise.entity.EnterpriseAccessAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnterpriseAccessAuditRepository extends JpaRepository<EnterpriseAccessAudit, UUID> {
}
