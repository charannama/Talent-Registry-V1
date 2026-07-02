package com.zencube.registry.notification.mapper;

import com.zencube.registry.notification.dto.response.NotificationResponse;
import com.zencube.registry.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .eventType(notification.getEventType())
                .resourceType(notification.getResourceType())
                .resourceId(notification.getResourceId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .read(notification.getReadAt() != null)
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
