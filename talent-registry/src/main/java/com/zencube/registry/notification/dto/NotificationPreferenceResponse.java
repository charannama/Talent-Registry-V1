package com.zencube.registry.notification.dto;

import com.zencube.registry.notification.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {
    private UUID id;
    private UUID userId;
    private NotificationEventType eventType;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;
}
