package com.zencube.registry.notification.unit;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.dto.PaginatedNotificationResponse;
import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.fixtures.NotificationTestDataFactory;
import com.zencube.registry.notification.repository.NotificationPreferenceRepository;
import com.zencube.registry.notification.repository.NotificationRepository;
import com.zencube.registry.notification.repository.NotificationSettingsRepository;
import com.zencube.registry.notification.service.NotificationServiceImpl;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private TaskSchedulerService taskSchedulerService;

    @Mock
    private AuditService auditService;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_success() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.createNotification(userId, NotificationEventType.APPLICATION_SELECTED, "App", UUID.randomUUID(), "Title", "Body");

        verify(notificationRepository).save(any(Notification.class));
        verify(auditService).recordCustomEvent(eq("NOTIFICATION_CREATED"), anyString(), anyString(), anyString());
    }

    @Test
    void getUnreadCount_success() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount(userId);

        assertEquals(5L, count);
    }

    @Test
    void markAsRead_success() {
        UUID userId = UUID.randomUUID();
        Notification notification = NotificationTestDataFactory.createUnreadNotification(userId);
        
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.markAsRead(notification.getId());

        assertTrue((notification.getReadAt() != null));
        assertNotNull(notification.getReadAt());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_alreadyRead() {
        UUID userId = UUID.randomUUID();
        Notification notification = NotificationTestDataFactory.createReadNotification(userId);
        
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notification.getId());

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_success() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.markAllAsRead(eq(userId), any())).thenReturn(2);

        notificationService.markAllAsRead(userId);

        verify(notificationRepository).markAllAsRead(eq(userId), any());
    }

    @Test
    void listNotifications_success() {
        UUID userId = UUID.randomUUID();
        Notification n1 = NotificationTestDataFactory.createUnreadNotification(userId);
        Page<Notification> page = new PageImpl<>(List.of(n1));
        
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class))).thenReturn(page);

        PaginatedNotificationResponse result = notificationService.getUserNotifications(userId, 0, 10);

        assertEquals(1, result.getTotalElements());
    }
}




