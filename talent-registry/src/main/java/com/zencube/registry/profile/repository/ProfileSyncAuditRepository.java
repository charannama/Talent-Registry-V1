package com.zencube.registry.profile.repository;

import com.zencube.registry.profile.entity.ProfileSyncAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileSyncAuditRepository extends JpaRepository<ProfileSyncAudit, UUID> {
    List<ProfileSyncAudit> findByProfileIdOrderBySyncStartTimeDesc(UUID profileId);
}
