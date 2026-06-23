package com.zencube.registry.profile.repository;

import com.zencube.registry.profile.entity.ProfileAccessAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileAccessAuditRepository extends JpaRepository<ProfileAccessAudit, UUID>, JpaSpecificationExecutor<ProfileAccessAudit> {

    List<ProfileAccessAudit> findByViewerUserIdOrderByAccessedAtDesc(UUID viewerUserId);

    List<ProfileAccessAudit> findByTargetUserIdOrderByAccessedAtDesc(UUID targetUserId);

    long countByTargetUserId(UUID targetUserId);
}
