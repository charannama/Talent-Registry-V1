package com.zencube.registry.notification.repository;

import com.zencube.registry.notification.entity.NotificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, UUID> {
}
