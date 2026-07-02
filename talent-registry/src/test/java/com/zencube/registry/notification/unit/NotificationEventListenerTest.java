package com.zencube.registry.notification.unit;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.application.event.ApplicationStatusChangedEvent;
import com.zencube.registry.application.listener.ApplicationEventListener;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.fixtures.NotificationTestDataFactory;
import com.zencube.registry.notification.service.NotificationService;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private TaskSchedulerService taskSchedulerService;

    @Mock
    private ActivityService activityService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ApplicationEventListener listener;

    @Test
    void listenerHandlesSelectedStatus() {
        UUID studentId = UUID.randomUUID();
        ApplicationStatusChangedEvent event = NotificationTestDataFactory.createApplicationEvent(studentId, ApplicationStatus.SELECTED);

        listener.onApplicationStatusChanged(event);

        verify(notificationService).createNotification(
                eq(studentId),
                eq(NotificationEventType.APPLICATION_SELECTED),
                eq("Application"),
                eq(event.getApplicationId()),
                anyString(),
                anyString()
        );
        verify(taskSchedulerService).enqueueEmailTask(any());
        verify(activityService).recordActivity(anyString(), anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void listenerHandlesRejectedStatus() {
        UUID studentId = UUID.randomUUID();
        ApplicationStatusChangedEvent event = NotificationTestDataFactory.createApplicationEvent(studentId, ApplicationStatus.REJECTED);

        listener.onApplicationStatusChanged(event);

        verify(taskSchedulerService).enqueueEmailTask(any());
    }
}

