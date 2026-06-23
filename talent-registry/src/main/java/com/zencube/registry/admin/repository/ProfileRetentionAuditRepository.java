package com.zencube.registry.admin.repository;

import com.zencube.registry.admin.entity.ProfileRetentionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileRetentionAuditRepository extends JpaRepository<ProfileRetentionAudit, UUID> {
}
