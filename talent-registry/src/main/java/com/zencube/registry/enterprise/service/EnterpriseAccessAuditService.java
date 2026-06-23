package com.zencube.registry.enterprise.service;

import com.zencube.registry.enterprise.entity.EnterpriseAccessAudit;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccessAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnterpriseAccessAuditService {

    private final EnterpriseAccessAuditRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAccessAttempt(UUID enterpriseId, UUID userId, String eventType, EnterpriseOnboardingStatus status, String endpoint) {
        EnterpriseAccessAudit audit = EnterpriseAccessAudit.builder()
                .enterpriseId(enterpriseId)
                .userId(userId)
                .eventType(eventType)
                .status(status)
                .endpoint(endpoint)
                .build();
        repository.save(audit);
    }
}
