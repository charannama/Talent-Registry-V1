package com.zencube.registry.notification.service.impl;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.notification.dto.NotificationPreferenceResponse;
import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;
import com.zencube.registry.notification.dto.UpdateNotificationPreferenceRequest;
import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;
import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.entity.NotificationSettings;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.event.NotificationEvent;
import com.zencube.registry.notification.exception.NotificationPreferenceException;
import com.zencube.registry.notification.mapper.NotificationPreferenceMapper;
import com.zencube.registry.notification.repository.NotificationPreferenceRepository;
import com.zencube.registry.notification.repository.NotificationSettingsRepository;
import com.zencube.registry.notification.service.NotificationAuditService;
import com.zencube.registry.notification.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationSettingsRepository settingsRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationPreferenceMapper mapper;
    private final NotificationAuditService auditService;
    private final ActivityService activityService;

    @Override
    @Transactional(readOnly = true)
    public NotificationSettingsResponse getSettings(UUID userId) {
        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettingsInternal(userId));
        return mapper.toSettingsResponse(settings);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.UPDATE, entityType = "NOTIFICATION_SETTINGS", idParam = "none")
    public NotificationSettingsResponse updateSettings(UUID userId, UpdateNotificationSettingsRequest request) {
        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettingsInternal(userId));

        settings.setEmailEnabled(request.getEmailEnabled());
        settings.setPushEnabled(request.getPushEnabled());
        settings.setInAppEnabled(request.getInAppEnabled());

        settings = settingsRepository.save(settings);

        auditService.logAction(null, userId, "NOTIFICATION_SETTINGS_UPDATED", "User updated global notification settings");
        activityService.recordActivity(
                "NotificationSettings", userId.toString(),
                "User", userId.toString(),
                com.zencube.registry.activity.enums.ActivityType.PROFILE_UPDATED,
                "User updated global notification settings"
        );

        return mapper.toSettingsResponse(settings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getPreferences(UUID userId) {
        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        if (preferences.isEmpty()) {
            createDefaultPreferencesInternal(userId);
            preferences = preferenceRepository.findByUserId(userId);
        }
        return mapper.toPreferenceResponses(preferences);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.UPDATE, entityType = "NOTIFICATION_PREFERENCE", idParam = "none")
    public NotificationPreferenceResponse updatePreference(UUID userId, NotificationEventType eventType, UpdateNotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserIdAndEventType(userId, eventType)
                .orElseThrow(() -> new NotificationPreferenceException("Preference not found for event type: " + eventType));

        preference.setEmailEnabled(request.getEmailEnabled());
        preference.setPushEnabled(request.getPushEnabled());
        preference.setInAppEnabled(request.getInAppEnabled());

        preference = preferenceRepository.save(preference);

        auditService.logAction(null, userId, "NOTIFICATION_PREFERENCE_UPDATED", "User updated preference for event: " + eventType);
        activityService.recordActivity(
                "NotificationPreference", preference.getId().toString(),
                "User", userId.toString(),
                com.zencube.registry.activity.enums.ActivityType.PROFILE_UPDATED,
                "User updated notification preference for " + eventType
        );

        return mapper.toPreferenceResponse(preference);
    }

    @Override
    @Transactional
    public void createDefaultPreferences(UUID userId) {
        createDefaultSettingsInternal(userId);
        createDefaultPreferencesInternal(userId);
        
        auditService.logAction(null, userId, "DEFAULT_NOTIFICATION_PREFERENCES_CREATED", "Default notification preferences created for new user");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean shouldSendEmail(UUID userId, NotificationEventType eventType) {
        return isChannelEnabled(userId, eventType, Channel.EMAIL);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean shouldSendPush(UUID userId, NotificationEventType eventType) {
        return isChannelEnabled(userId, eventType, Channel.PUSH);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean shouldSendInApp(UUID userId, NotificationEventType eventType) {
        return isChannelEnabled(userId, eventType, Channel.IN_APP);
    }

    @Async
    @EventListener
    @Transactional
    public void handleUserRegistration(NotificationEvent event) {
        if (event.getEventType() == NotificationEventType.USER_REGISTERED) {
            log.info("Intercepted USER_REGISTERED event for user {}. Generating default preferences.", event.getRecipientId());
            createDefaultPreferences(event.getRecipientId());
        }
    }

    private boolean isChannelEnabled(UUID userId, NotificationEventType eventType, Channel channel) {
        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettingsInternal(userId));

        boolean globalEnabled = switch (channel) {
            case EMAIL -> settings.getEmailEnabled();
            case PUSH -> settings.getPushEnabled();
            case IN_APP -> settings.getInAppEnabled();
        };

        if (!globalEnabled) {
            return false;
        }

        NotificationPreference preference = preferenceRepository.findByUserIdAndEventType(userId, eventType)
                .orElseGet(() -> createSingleDefaultPreference(userId, eventType));

        return switch (channel) {
            case EMAIL -> preference.getEmailEnabled();
            case PUSH -> preference.getPushEnabled();
            case IN_APP -> preference.getInAppEnabled();
        };
    }

    private NotificationSettings createDefaultSettingsInternal(UUID userId) {
        if (settingsRepository.existsByUserId(userId)) {
            return settingsRepository.findByUserId(userId).get();
        }
        NotificationSettings settings = NotificationSettings.builder()
                .userId(userId)
                .emailEnabled(true)
                .pushEnabled(false)
                .inAppEnabled(true)
                .build();
        return settingsRepository.save(settings);
    }

    private void createDefaultPreferencesInternal(UUID userId) {
        Arrays.stream(NotificationEventType.values()).forEach(eventType -> {
            if (!preferenceRepository.existsByUserIdAndEventType(userId, eventType)) {
                createSingleDefaultPreference(userId, eventType);
            }
        });
    }

    private NotificationPreference createSingleDefaultPreference(UUID userId, NotificationEventType eventType) {
        NotificationPreference pref = NotificationPreference.builder()
                .userId(userId)
                .eventType(eventType)
                .emailEnabled(true)
                .pushEnabled(false)
                .inAppEnabled(true)
                .build();
        return preferenceRepository.save(pref);
    }

    private enum Channel {
        EMAIL, PUSH, IN_APP
    }
}
