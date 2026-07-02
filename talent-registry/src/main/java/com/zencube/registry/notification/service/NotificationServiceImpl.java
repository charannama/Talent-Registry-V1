package com.zencube.registry.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.notification.dto.*;
import com.zencube.registry.notification.dto.response.*;
import com.zencube.registry.notification.dto.request.*;
import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.event.NotificationEvent;
import com.zencube.registry.notification.repository.NotificationPreferenceRepository;
import com.zencube.registry.notification.repository.NotificationRepository;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService notificationPreferenceService;
    
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private TaskSchedulerService taskSchedulerService;

    private final ObjectMapper objectMapper;
    private final com.zencube.registry.notification.mapper.NotificationMapper notificationMapper;
    private final com.zencube.registry.notification.service.NotificationAuditService notificationAuditService;
    private final com.zencube.registry.activity.service.ActivityService activityService;
    private final com.zencube.registry.security.facade.AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public void processNotificationEvent(NotificationEvent event) {
        log.info("Processing notification event: {} for user: {}", event.getEventType(), event.getRecipientId());

        boolean shouldSendInApp = notificationPreferenceService.shouldSendInApp(event.getRecipientId(), event.getEventType());
        boolean shouldSendEmail = notificationPreferenceService.shouldSendEmail(event.getRecipientId(), event.getEventType());
        boolean shouldSendPush = notificationPreferenceService.shouldSendPush(event.getRecipientId(), event.getEventType());

        if (shouldSendInApp) {
            createInAppNotification(event);
        }

        if (shouldSendEmail) {
            scheduleEmailTask(event);
        }

        if (shouldSendPush) {
            schedulePushTask(event);
        }
    }

    private void createInAppNotification(NotificationEvent event) {
        Notification notification = Notification.builder()
                .userId(event.getRecipientId())
                .eventType(event.getEventType())
                .resourceType(event.getResourceType())
                .resourceId(event.getResourceId())
                .title(event.getTitle())
                .body(event.getMessage())
                .build();
        notificationRepository.save(notification);
        log.debug("Created in-app notification for event: {}", event.getEventType());
    }

    private void scheduleEmailTask(NotificationEvent event) {
        Map<String, Object> payloadMap = objectMapper.convertValue(event, Map.class);
        TaskPayload payload = TaskPayload.builder()
                .taskType("EMAIL_DELIVERY")
                .data(payloadMap)
                .build();
                
        taskSchedulerService.enqueueEmailTask(payload);
        log.debug("Scheduled email task for event: {}", event.getEventType());
    }

    private void schedulePushTask(NotificationEvent event) {
        // Future push notification support
        log.debug("Push notifications are not yet implemented. Skipping for event: {}", event.getEventType());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedNotificationResponse getUserNotifications(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> pageResult = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return mapToPaginatedResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedNotificationResponse getUnreadNotifications(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> pageResult = notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable);
        return mapToPaginatedResponse(pageResult);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(UUID userId, NotificationEventType eventType, String resourceType, UUID resourceId, String title, String body) {
        Notification notification = Notification.builder()
                .userId(userId)
                .eventType(eventType)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .title(title)
                .body(body)
                .build();
        
        notification = notificationRepository.save(notification);
        
        notificationAuditService.logAction(notification.getId(), userId, "NOTIFICATION_CREATED", "Notification created for user: " + userId);
        
        activityService.recordActivity(
                "Notification", notification.getId().toString(),
                resourceType != null ? resourceType : "User", 
                resourceId != null ? resourceId.toString() : userId.toString(),
                com.zencube.registry.activity.enums.ActivityType.NOTIFICATION_RECEIVED,
                "User received a new notification: " + title
        );
        
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse listNotifications(UUID userId, Integer page, Boolean unreadOnly) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        
        Page<Notification> notificationPage;
        if (Boolean.TRUE.equals(unreadOnly)) {
            notificationPage = notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        long unreadCount = notificationRepository.countByUserIdAndReadAtIsNull(userId);
        
        return NotificationPageResponse.builder()
                .content(notificationPage.getContent().stream().map(notificationMapper::toResponse).collect(Collectors.toList()))
                .page(notificationPage.getNumber())
                .size(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new com.zencube.registry.notification.exception.NotificationNotFoundException("Notification not found"));
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = authenticationFacade.getCurrentUserId();
        if (currentUserId != null && !notification.getUserId().equals(currentUserId)) {
            // Wait, maybe admin is allowed? For now, validate ownership strictly.
            // Check if admin:
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new com.zencube.registry.notification.exception.NotificationAccessDeniedException("You can only mark your own notifications as read.");
            }
        }
        
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
            
            notificationAuditService.logAction(notification.getId(), currentUserId != null ? currentUserId : notification.getUserId(), "NOTIFICATION_READ", "Notification marked as read");
            
            activityService.recordActivity(
                    "Notification", notification.getId().toString(),
                    notification.getResourceType() != null ? notification.getResourceType() : "User", 
                    notification.getResourceId() != null ? notification.getResourceId().toString() : notification.getUserId().toString(),
                    com.zencube.registry.activity.enums.ActivityType.NOTIFICATION_READ,
                    "User read notification: " + notification.getTitle()
            );
        }
        
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(UUID userId) {
        int updatedCount = notificationRepository.markAllAsRead(userId, Instant.now());
        
        if (updatedCount > 0) {
            notificationAuditService.logAction(null, userId, "NOTIFICATIONS_MARKED_READ", updatedCount + " notifications marked as read");
        }
        
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.DELETE, entityType = "NOTIFICATION", idParam = "none")
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse countUnreadNotifications(UUID userId) {
        long count = notificationRepository.countByUserIdAndReadAtIsNull(userId);
        return new UnreadCountResponse(count);
    }

    private PaginatedNotificationResponse mapToPaginatedResponse(Page<Notification> page) {
        List<NotificationResponse> content = page.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return PaginatedNotificationResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
