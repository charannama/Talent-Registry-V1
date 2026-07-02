package com.zencube.registry.notification.service;

import com.zencube.registry.notification.dto.response.NotificationPageResponse;
import com.zencube.registry.notification.dto.response.NotificationResponse;
import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.entity.NotificationAudit;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.repository.NotificationAuditRepository;
import com.zencube.registry.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NotificationServiceIT {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationAuditRepository notificationAuditRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        
        // Ensure user exists for foreign key constraint
        jdbcTemplate.execute("INSERT INTO users (id, email, status) VALUES ('00000000-0000-0000-0000-000000000001', 'test@test.com', 'ACTIVE') ON CONFLICT (id) DO NOTHING");

        // Clean up repositories for isolated tests
        notificationRepository.deleteAll();
        notificationAuditRepository.deleteAll();
    }

    @Test
    void testCreateNotification_ShouldPersistAndLogAudit() {
        NotificationResponse response = notificationService.createNotification(
                userId,
                NotificationEventType.USER_REGISTERED,
                "User",
                null,
                "Welcome",
                "Welcome to ZenCube Registry"
        );

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Welcome");
        assertThat(response.isRead()).isFalse();

        // Verify Database Persistence
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getUserId()).isEqualTo(userId);

        // Verify Audit Integration
        List<NotificationAudit> audits = notificationAuditRepository.findAll();
        assertThat(audits).hasSize(1);
        assertThat(audits.get(0).getAction()).isEqualTo("NOTIFICATION_CREATED");
    }

    @Test
    void testListNotifications_PaginationAndUnreadFiltering() {
        // Create 3 notifications, 1 read, 2 unread
        notificationService.createNotification(userId, NotificationEventType.INTERVIEW_SCHEDULED, null, null, "1", "1");
        NotificationResponse readNotif = notificationService.createNotification(userId, NotificationEventType.INTERVIEW_SCHEDULED, null, null, "2", "2");
        notificationService.createNotification(userId, NotificationEventType.INTERVIEW_SCHEDULED, null, null, "3", "3");

        // Mark second as read (mock user using @WithMockUser won't work easily here, let's bypass by directly saving)
        Notification notification = notificationRepository.findById(readNotif.getId()).get();
        notification.setReadAt(java.time.Instant.now());
        notificationRepository.save(notification);

        // Test Unread only
        NotificationPageResponse unreadResponse = notificationService.listNotifications(userId, 0, true);
        assertThat(unreadResponse.getContent()).hasSize(2);
        assertThat(unreadResponse.getTotalElements()).isEqualTo(2);

        // Test All
        NotificationPageResponse allResponse = notificationService.listNotifications(userId, 0, false);
        assertThat(allResponse.getContent()).hasSize(3);
        assertThat(allResponse.getUnreadCount()).isEqualTo(2);
    }

    @Test
    void testMarkAllAsRead_BulkUpdate() {
        notificationService.createNotification(userId, NotificationEventType.INTERVIEW_SCHEDULED, null, null, "1", "1");
        notificationService.createNotification(userId, NotificationEventType.INTERVIEW_SCHEDULED, null, null, "2", "2");

        long initialUnreadCount = notificationService.getUnreadCount(userId);
        assertEquals(2, initialUnreadCount);

        int updated = notificationService.markAllAsRead(userId);
        assertEquals(2, updated);

        long finalUnreadCount = notificationService.getUnreadCount(userId);
        assertEquals(0, finalUnreadCount);

        List<NotificationAudit> audits = notificationAuditRepository.findAll();
        // 2 creations + 1 bulk read
        assertThat(audits).hasSize(3);
        assertThat(audits.stream().anyMatch(a -> a.getAction().equals("NOTIFICATIONS_MARKED_READ"))).isTrue();
    }
}

