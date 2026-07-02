package com.zencube.registry.notification.repository;

import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.enums.NotificationEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(UUID userId);

    List<Notification> findByUserIdAndEventTypeOrderByCreatedAtDesc(UUID userId, NotificationEventType eventType);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.userId = :userId AND n.readAt IS NULL")
    int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") Instant readAt);
}
