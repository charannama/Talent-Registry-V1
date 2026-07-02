package com.zencube.registry.notification.service;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.notification.dto.NotificationPreferenceResponse;
import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;
import com.zencube.registry.notification.dto.UpdateNotificationPreferenceRequest;
import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;
import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.entity.NotificationSettings;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.exception.NotificationPreferenceException;
import com.zencube.registry.notification.mapper.NotificationPreferenceMapper;
import com.zencube.registry.notification.repository.NotificationPreferenceRepository;
import com.zencube.registry.notification.repository.NotificationSettingsRepository;
import com.zencube.registry.notification.service.impl.NotificationPreferenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationSettingsRepository settingsRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationPreferenceMapper mapper;

    @Mock
    private NotificationAuditService auditService;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private NotificationPreferenceServiceImpl preferenceService;

    private UUID userId;
    private NotificationSettings settings;
    private NotificationPreference preference;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        settings = NotificationSettings.builder()
                .userId(userId)
                .emailEnabled(true)
                .pushEnabled(false)
                .inAppEnabled(true)
                .build();

        preference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventType(NotificationEventType.USER_REGISTERED)
                .emailEnabled(true)
                .pushEnabled(false)
                .inAppEnabled(true)
                .build();
    }

    @Test
    void getSettings_ShouldReturnExistingSettings() {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        
        NotificationSettingsResponse expectedResponse = new NotificationSettingsResponse(userId, true, false, true);
        when(mapper.toSettingsResponse(settings)).thenReturn(expectedResponse);

        NotificationSettingsResponse result = preferenceService.getSettings(userId);

        assertNotNull(result);
        assertEquals(true, result.getEmailEnabled());
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void getSettings_ShouldCreateDefaultIfMissing() {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(settingsRepository.existsByUserId(userId)).thenReturn(false);
        when(settingsRepository.save(any(NotificationSettings.class))).thenReturn(settings);
        
        NotificationSettingsResponse expectedResponse = new NotificationSettingsResponse(userId, true, false, true);
        when(mapper.toSettingsResponse(settings)).thenReturn(expectedResponse);

        NotificationSettingsResponse result = preferenceService.getSettings(userId);

        assertNotNull(result);
        verify(settingsRepository).save(any(NotificationSettings.class));
    }

    @Test
    void updateSettings_ShouldUpdateAndLogAudit() {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(NotificationSettings.class))).thenReturn(settings);
        
        NotificationSettingsResponse expectedResponse = new NotificationSettingsResponse(userId, false, true, true);
        when(mapper.toSettingsResponse(settings)).thenReturn(expectedResponse);

        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest(false, true, true);

        NotificationSettingsResponse result = preferenceService.updateSettings(userId, request);

        assertNotNull(result);
        verify(settingsRepository).save(settings);
        verify(auditService).logAction(null, userId, "NOTIFICATION_SETTINGS_UPDATED", "User updated global notification settings");
        verify(activityService).recordActivity(anyString(), anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void getPreferences_ShouldReturnPreferences() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(List.of(preference));
        
        NotificationPreferenceResponse expectedResponse = new NotificationPreferenceResponse(preference.getId(), userId, NotificationEventType.USER_REGISTERED, true, false, true);
        when(mapper.toPreferenceResponses(any())).thenReturn(List.of(expectedResponse));

        List<NotificationPreferenceResponse> results = preferenceService.getPreferences(userId);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void updatePreference_ShouldThrowExceptionIfNotFound() {
        when(preferenceRepository.findByUserIdAndEventType(userId, NotificationEventType.USER_REGISTERED))
                .thenReturn(Optional.empty());

        UpdateNotificationPreferenceRequest request = new UpdateNotificationPreferenceRequest(true, true, true);

        assertThrows(NotificationPreferenceException.class, () -> 
                preferenceService.updatePreference(userId, NotificationEventType.USER_REGISTERED, request));
    }

    @Test
    void createDefaultPreferences_ShouldSaveSettingsAndPreferences() {
        when(settingsRepository.existsByUserId(userId)).thenReturn(false);
        when(preferenceRepository.existsByUserIdAndEventType(eq(userId), any())).thenReturn(false);

        preferenceService.createDefaultPreferences(userId);

        verify(settingsRepository).save(any(NotificationSettings.class));
        verify(preferenceRepository, atLeastOnce()).save(any(NotificationPreference.class));
        verify(auditService).logAction(null, userId, "DEFAULT_NOTIFICATION_PREFERENCES_CREATED", "Default notification preferences created for new user");
    }

    @Test
    void shouldSendEmail_GlobalDisabled_ShouldReturnFalse() {
        settings.setEmailEnabled(false);
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

        boolean result = preferenceService.shouldSendEmail(userId, NotificationEventType.USER_REGISTERED);

        assertFalse(result);
        verify(preferenceRepository, never()).findByUserIdAndEventType(any(), any());
    }

    @Test
    void shouldSendEmail_GlobalEnabled_EventDisabled_ShouldReturnFalse() {
        settings.setEmailEnabled(true);
        preference.setEmailEnabled(false);
        
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(preferenceRepository.findByUserIdAndEventType(userId, NotificationEventType.USER_REGISTERED)).thenReturn(Optional.of(preference));

        boolean result = preferenceService.shouldSendEmail(userId, NotificationEventType.USER_REGISTERED);

        assertFalse(result);
    }

    @Test
    void shouldSendEmail_GlobalEnabled_EventEnabled_ShouldReturnTrue() {
        settings.setEmailEnabled(true);
        preference.setEmailEnabled(true);
        
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(preferenceRepository.findByUserIdAndEventType(userId, NotificationEventType.USER_REGISTERED)).thenReturn(Optional.of(preference));

        boolean result = preferenceService.shouldSendEmail(userId, NotificationEventType.USER_REGISTERED);

        assertTrue(result);
    }
}

