package com.zencube.registry.notification.integration;

import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.repository.NotificationRepository;
import com.zencube.registry.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationIT extends IntegrationTestBase {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @Transactional
    void createNotification_persistsInDatabase() {
        UUID userId = UUID.randomUUID();
        notificationService.createNotification(userId, NotificationEventType.APPLICATION_SELECTED, "App", UUID.randomUUID(), "Test", "Body");

        long count = notificationRepository.countByUserIdAndReadAtIsNull(userId);
        assertEquals(1, count);
    }

    @Test
    @Transactional
    void markAllNotificationsRead_updatesDatabase() {
        UUID userId = UUID.randomUUID();
        notificationService.createNotification(userId, NotificationEventType.APPLICATION_SELECTED, "App", UUID.randomUUID(), "Test1", "Body1");
        notificationService.createNotification(userId, NotificationEventType.APPLICATION_SELECTED, "App", UUID.randomUUID(), "Test2", "Body2");

        notificationService.markAllAsRead(userId);

        long unreadCount = notificationRepository.countByUserIdAndReadAtIsNull(userId);
        assertEquals(0, unreadCount);
    }
}

