package com.zencube.registry.notification.repository;

import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.enums.NotificationEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByUserId(UUID userId);

    Optional<NotificationPreference> findByUserIdAndEventType(UUID userId, NotificationEventType eventType);

    boolean existsByUserIdAndEventType(UUID userId, NotificationEventType eventType);
}
