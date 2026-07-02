package com.zencube.registry.notification.service;

import com.zencube.registry.notification.dto.*;
import com.zencube.registry.notification.dto.response.*;
import com.zencube.registry.notification.dto.request.*;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.event.NotificationEvent;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void processNotificationEvent(NotificationEvent event);

    NotificationResponse createNotification(
            UUID userId,
            NotificationEventType eventType,
            String resourceType,
            UUID resourceId,
            String title,
            String body
    );

    NotificationPageResponse listNotifications(UUID userId, Integer page, Boolean unreadOnly);

    PaginatedNotificationResponse getUserNotifications(UUID userId, int page, int size);

    PaginatedNotificationResponse getUnreadNotifications(UUID userId, int page, int size);

    NotificationResponse markAsRead(UUID notificationId);

    int markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId);

    UnreadCountResponse countUnreadNotifications(UUID userId);

    long getUnreadCount(UUID userId);
}
