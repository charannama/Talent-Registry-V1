package com.zencube.registry.enterprise.service;

import com.zencube.registry.enterprise.entity.EnterpriseStatusAudit;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseStatusAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnterpriseStatusAuditService {

    private final EnterpriseStatusAuditRepository repository;

    @Transactional
    public void logStatusChange(UUID enterpriseId, UUID actorId, EnterpriseOnboardingStatus previousStatus, EnterpriseOnboardingStatus newStatus, String reason) {
        // In a real application, IP address could be retrieved from a RequestContextHolder helper.
        EnterpriseStatusAudit audit = EnterpriseStatusAudit.builder()
                .enterpriseId(enterpriseId)
                .actorId(actorId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
        repository.save(audit);
    }
}
