package com.zencube.registry.notification.fixtures;

import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.application.event.ApplicationStatusChangedEvent;
import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.entity.NotificationSettings;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.enums.TaskState;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class NotificationTestDataFactory {

    public static Notification createNotification(UUID userId, boolean isRead) {
        Notification notification = Notification.builder()
                .userId(userId)
                .eventType(NotificationEventType.APPLICATION_SELECTED)
                .resourceType("Application")
                .resourceId(UUID.randomUUID())
                .title("Test Notification")
                .body("This is a test notification")
                
                .build();
        notification.setId(UUID.randomUUID());
        return notification;
    }

    public static Notification createUnreadNotification(UUID userId) {
        return createNotification(userId, false);
    }

    public static Notification createReadNotification(UUID userId) {
        Notification n = createNotification(userId, true);
        n.setReadAt(Instant.now());
        return n;
    }

    public static NotificationSettings createPreferences(UUID userId, boolean emailEnabled, boolean pushEnabled, boolean inAppEnabled) {
        NotificationSettings settings = NotificationSettings.builder()
                .userId(userId)
                .emailEnabled(emailEnabled)
                .pushEnabled(pushEnabled)
                .inAppEnabled(inAppEnabled)
                .build();
        // settings.setId(UUID.randomUUID());
        return settings;
    }

    public static ScheduledTask createEmailTask() {
        ScheduledTask task = ScheduledTask.builder()
                .taskType("EMAIL_DELIVERY")
                .payload(Map.of("recipientId", UUID.randomUUID().toString(), "templateName", "email/test"))
                .state(TaskState.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .scheduledAt(Instant.now())
                .build();
        task.setId(UUID.randomUUID());
        return task;
    }

    public static TaskPayload createTaskPayload(String type) {
        return TaskPayload.builder()
                .taskType(type)
                .data(Map.of("testKey", "testValue"))
                .build();
    }

    public static ApplicationStatusChangedEvent createApplicationEvent(UUID studentId, ApplicationStatus status) {
        return ApplicationStatusChangedEvent.builder()
                .applicationId(UUID.randomUUID())
                .studentId(studentId)
                .enterpriseId(UUID.randomUUID())
                .openingId(UUID.randomUUID())
                .oldStatus(ApplicationStatus.APPLIED)
                .newStatus(status)
                .actorId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .build();
    }
}




