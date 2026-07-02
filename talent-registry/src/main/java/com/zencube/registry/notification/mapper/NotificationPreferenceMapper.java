package com.zencube.registry.notification.mapper;

import com.zencube.registry.notification.dto.NotificationPreferenceResponse;
import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;
import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.entity.NotificationSettings;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationPreferenceMapper {

    public NotificationSettingsResponse toSettingsResponse(NotificationSettings settings) {
        if (settings == null) {
            return null;
        }

        return NotificationSettingsResponse.builder()
                .userId(settings.getUserId())
                .emailEnabled(settings.getEmailEnabled())
                .pushEnabled(settings.getPushEnabled())
                .inAppEnabled(settings.getInAppEnabled())
                .build();
    }

    public NotificationPreferenceResponse toPreferenceResponse(NotificationPreference preference) {
        if (preference == null) {
            return null;
        }

        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .eventType(preference.getEventType())
                .emailEnabled(preference.getEmailEnabled())
                .pushEnabled(preference.getPushEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .build();
    }

    public List<NotificationPreferenceResponse> toPreferenceResponses(List<NotificationPreference> preferences) {
        if (preferences == null) {
            return null;
        }
        return preferences.stream()
                .map(this::toPreferenceResponse)
                .collect(Collectors.toList());
    }
}
