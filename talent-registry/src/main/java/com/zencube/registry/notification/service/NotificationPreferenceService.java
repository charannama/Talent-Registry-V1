package com.zencube.registry.notification.service;

import com.zencube.registry.notification.dto.NotificationPreferenceResponse;
import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;
import com.zencube.registry.notification.dto.UpdateNotificationPreferenceRequest;
import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;
import com.zencube.registry.notification.enums.NotificationEventType;

import java.util.List;
import java.util.UUID;

public interface NotificationPreferenceService {

    NotificationSettingsResponse getSettings(UUID userId);

    NotificationSettingsResponse updateSettings(UUID userId, UpdateNotificationSettingsRequest request);

    List<NotificationPreferenceResponse> getPreferences(UUID userId);

    NotificationPreferenceResponse updatePreference(UUID userId, NotificationEventType eventType, UpdateNotificationPreferenceRequest request);

    void createDefaultPreferences(UUID userId);
    
    boolean shouldSendEmail(UUID userId, NotificationEventType eventType);
    
    boolean shouldSendPush(UUID userId, NotificationEventType eventType);
    
    boolean shouldSendInApp(UUID userId, NotificationEventType eventType);
}
