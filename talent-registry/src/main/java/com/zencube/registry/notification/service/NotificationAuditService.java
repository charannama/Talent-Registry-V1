package com.zencube.registry.notification.service;

import com.zencube.registry.notification.entity.NotificationAudit;
import com.zencube.registry.notification.repository.NotificationAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationAuditService {

    private final NotificationAuditRepository auditRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(UUID notificationId, UUID actorId, String action, String details) {
        NotificationAudit audit = NotificationAudit.builder()
                .notificationId(notificationId)
                .actorId(actorId)
                .action(action)
                .details(details)
                .build();
        auditRepository.save(audit);
    }
}
