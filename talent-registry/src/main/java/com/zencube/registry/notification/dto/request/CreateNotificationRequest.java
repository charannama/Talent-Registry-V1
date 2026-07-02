package com.zencube.registry.notification.dto.request;

import com.zencube.registry.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    private UUID recipientUserId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private Map<String, Object> metadata;
}
