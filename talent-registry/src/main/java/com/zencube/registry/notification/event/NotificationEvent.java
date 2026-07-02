package com.zencube.registry.notification.event;

import com.zencube.registry.notification.enums.NotificationEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class NotificationEvent {
    private final NotificationEventType eventType;
    private final UUID recipientId;
    private final String resourceType;
    private final UUID resourceId;
    private final String title;
    private final String message;
    private final Map<String, Object> metadata;
    
    @Builder.Default
    private final Instant timestamp = Instant.now();
}
