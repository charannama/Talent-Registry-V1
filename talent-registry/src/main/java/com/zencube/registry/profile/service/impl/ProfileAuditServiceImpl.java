package com.zencube.registry.profile.service.impl;

import com.zencube.registry.profile.entity.ProfileAccessAudit;
import com.zencube.registry.profile.enums.AccessReason;
import com.zencube.registry.profile.enums.AccessResult;
import com.zencube.registry.profile.repository.ProfileAccessAuditRepository;
import com.zencube.registry.profile.service.ProfileAuditService;
import com.zencube.registry.profile.dto.ProfileAccessAuditDTO;
import com.zencube.registry.profile.mapper.ProfileAccessAuditMapper;
import com.zencube.registry.profile.specification.ProfileAuditSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileAuditServiceImpl implements ProfileAuditService {

    private final ProfileAccessAuditRepository auditRepository;
    private final ProfileAccessAuditMapper auditMapper;

    @Override
    @Async
    public void logSuccessfulAccess(UUID viewerUserId, UUID targetUserId, AccessReason reason, HttpServletRequest request) {
        logAccess(viewerUserId, targetUserId, reason, AccessResult.SUCCESS, request);
    }

    @Override
    @Async
    public void logDeniedAccess(UUID viewerUserId, UUID targetUserId, AccessReason reason, HttpServletRequest request) {
        logAccess(viewerUserId, targetUserId, reason, AccessResult.DENIED, request);
    }

    private void logAccess(UUID viewerUserId, UUID targetUserId, AccessReason reason, AccessResult result, HttpServletRequest request) {
        String ipAddress = request != null ? request.getRemoteAddr() : "UNKNOWN";
        String userAgent = request != null ? request.getHeader("User-Agent") : "UNKNOWN";

        ProfileAccessAudit audit = ProfileAccessAudit.builder()
                .viewerUserId(viewerUserId)
                .targetUserId(targetUserId)
                .accessReason(reason)
                .accessResult(result)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .accessedAt(Instant.now())
                .build();

        auditRepository.save(audit);
    }

    @Override
    public Page<ProfileAccessAuditDTO> searchAudits(UUID viewerUserId, UUID targetUserId, AccessResult accessResult, Instant startDate, Instant endDate, Pageable pageable) {
        Specification<ProfileAccessAudit> spec = Specification.where(ProfileAuditSpecification.isNotDeleted())
                .and(ProfileAuditSpecification.hasViewerUserId(viewerUserId))
                .and(ProfileAuditSpecification.hasTargetUserId(targetUserId))
                .and(ProfileAuditSpecification.hasAccessResult(accessResult))
                .and(ProfileAuditSpecification.createdAfter(startDate))
                .and(ProfileAuditSpecification.createdBefore(endDate));

        return auditRepository.findAll(spec, pageable)
                .map(auditMapper::toDto);
    }
}
