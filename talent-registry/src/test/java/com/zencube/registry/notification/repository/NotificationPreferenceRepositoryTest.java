package com.zencube.registry.notification.repository;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.TestDataFactory;
import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.enums.NotificationEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPreferenceRepositoryTest extends IntegrationTestBase {

    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void findByUserId_ShouldReturnPreferences() {
        // Arrange
        User user = testDataFactory.createUser("pref.test@example.com");

        NotificationPreference pref = NotificationPreference.builder()
                .userId(user.getId())
                .eventType(NotificationEventType.USER_REGISTERED)
                .emailEnabled(true)
                .pushEnabled(false)
                .inAppEnabled(true)
                .build();
                
        notificationPreferenceRepository.save(pref);

        // Act
        List<NotificationPreference> found = notificationPreferenceRepository.findByUserId(user.getId());

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getUserId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserIdAndEventType_ShouldReturnPreference() {
        // Arrange
        User user = testDataFactory.createUser("pref.exist.test@example.com");

        NotificationPreference pref = NotificationPreference.builder()
                .userId(user.getId())
                .eventType(NotificationEventType.APPLICATION_SUBMITTED)
                .emailEnabled(false)
                .pushEnabled(true)
                .inAppEnabled(true)
                .build();
                
        notificationPreferenceRepository.save(pref);

        // Act
        Optional<NotificationPreference> found = notificationPreferenceRepository.findByUserIdAndEventType(user.getId(), NotificationEventType.APPLICATION_SUBMITTED);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEventType()).isEqualTo(NotificationEventType.APPLICATION_SUBMITTED);
        assertThat(found.get().getEmailEnabled()).isFalse();
    }
}
