package com.zencube.registry.profile.service;

import com.zencube.registry.profile.enums.AccessReason;
import com.zencube.registry.profile.enums.AccessResult;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.zencube.registry.profile.dto.ProfileAccessAuditDTO;

import java.time.Instant;
import java.util.UUID;

public interface ProfileAuditService {

    void logSuccessfulAccess(UUID viewerUserId, UUID targetUserId, AccessReason reason, HttpServletRequest request);

    void logDeniedAccess(UUID viewerUserId, UUID targetUserId, AccessReason reason, HttpServletRequest request);

    Page<ProfileAccessAuditDTO> searchAudits(UUID viewerUserId, UUID targetUserId, AccessResult accessResult, Instant startDate, Instant endDate, Pageable pageable);

}
