package com.zencube.registry.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.notification.dto.response.NotificationPageResponse;
import com.zencube.registry.notification.dto.response.NotificationResponse;
import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.exception.NotificationAccessDeniedException;
import com.zencube.registry.notification.exception.NotificationNotFoundException;
import com.zencube.registry.notification.mapper.NotificationMapper;
import com.zencube.registry.notification.repository.NotificationRepository;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationPreferenceService notificationPreferenceService;
    @Mock
    private TaskSchedulerService taskSchedulerService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private NotificationAuditService notificationAuditService;
    @Mock
    private ActivityService activityService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID userId;
    private UUID notificationId;
    private Notification notification;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        notification = Notification.builder()
                .id(notificationId)
                .userId(userId)
                .eventType(NotificationEventType.INTERVIEW_SCHEDULED)
                .title("Test Title")
                .body("Test Body")
                .createdAt(Instant.now())
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(notificationId)
                .title("Test Title")
                .body("Test Body")
                .read(false)
                .build();
    }

    @Test
    void createNotification_ShouldCreateAndReturnNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.createNotification(
                userId, NotificationEventType.INTERVIEW_SCHEDULED, "Opening", UUID.randomUUID(), "Test Title", "Test Body"
        );

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(notificationAuditService).logAction(eq(notificationId), eq(userId), eq("NOTIFICATION_CREATED"), anyString());
        verify(activityService).recordActivity(anyString(), anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void listNotifications_ShouldReturnPaginatedNotifications() {
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class))).thenReturn(page);
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(1L);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationPageResponse result = notificationService.listNotifications(userId, 0, false);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getUnreadCount());
    }

    @Test
    void listUnreadNotifications_ShouldReturnUnreadOnly() {
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(eq(userId), any(Pageable.class))).thenReturn(page);
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(1L);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationPageResponse result = notificationService.listNotifications(userId, 0, true);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void markAsRead_ShouldMarkAsReadAndLogAudit() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userId.toString());

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.markAsRead(notificationId);

        assertNotNull(result);
        assertNotNull(notification.getReadAt());
        verify(notificationAuditService).logAction(eq(notificationId), eq(userId), eq("NOTIFICATION_READ"), anyString());
        verify(activityService).recordActivity(anyString(), anyString(), any(), any(), any(), anyString());
    }

    @Test
    void markAlreadyRead_ShouldNotUpdateReadAtAgain() {
        notification.setReadAt(Instant.now());
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userId.toString());

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.markAsRead(notificationId);

        assertNotNull(result);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationAuditService, never()).logAction(any(), any(), any(), any());
    }

    @Test
    void markAllAsRead_ShouldUpdateBulkAndLogAudit() {
        when(notificationRepository.markAllAsRead(eq(userId), any(Instant.class))).thenReturn(5);

        int count = notificationService.markAllAsRead(userId);

        assertEquals(5, count);
        verify(notificationAuditService).logAction(isNull(), eq(userId), eq("NOTIFICATIONS_MARKED_READ"), anyString());
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(10L);

        long count = notificationService.getUnreadCount(userId);

        assertEquals(10L, count);
    }

    @Test
    void ownershipValidation_ShouldThrowExceptionIfNotOwner() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(UUID.randomUUID().toString()); // Different user
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList()); // Not admin

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThrows(NotificationAccessDeniedException.class, () -> notificationService.markAsRead(notificationId));
    }

    @Test
    void notificationNotFound_ShouldThrowException() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.markAsRead(notificationId));
    }
}

