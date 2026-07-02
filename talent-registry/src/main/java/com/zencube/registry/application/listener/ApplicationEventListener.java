package com.zencube.registry.application.listener;

import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.application.event.ApplicationStatusChangedEvent;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.event.NotificationEvent;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventListener {

    private final ApplicationEventPublisher eventPublisher;
    private final TaskSchedulerService taskSchedulerService;
    private final ActivityService activityService;
    private final AuditService auditService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        log.info("Received ApplicationStatusChangedEvent for application {} ({} -> {})", 
                event.getApplicationId(), event.getOldStatus(), event.getNewStatus());

        try {
            generateAuditLog(event);
        } catch (Exception e) {
            log.error("Failed to generate audit log for application event", e);
        }

        try {
            generateNotifications(event);
        } catch (Exception e) {
            log.error("Failed to generate notifications for application event", e);
        }

        try {
            generateActivityRecords(event);
        } catch (Exception e) {
            log.error("Failed to generate activity records for application event", e);
        }
    }

    private void generateAuditLog(ApplicationStatusChangedEvent event) {
        auditService.recordCustomEvent(
                "APPLICATION_STATUS_CHANGED",
                "Application",
                event.getApplicationId().toString(),
                String.format("Status changed from %s to %s", event.getOldStatus(), event.getNewStatus())
        );
    }

    private void generateNotifications(ApplicationStatusChangedEvent event) {
        ApplicationStatus status = event.getNewStatus();
        
        // 1. Student Notification & Email
        String studentTitle = "Application Update";
        String studentMessage = null;
        String studentTemplate = null;
        NotificationEventType studentEventType = null;

        switch (status) {
            case UNDER_REVIEW -> {
                studentMessage = "Your application is under review.";
                studentTemplate = "email/application-under-review";
                studentEventType = NotificationEventType.APPLICATION_REVIEWED;
            }
            case FORWARDED -> {
                studentMessage = "Your application has been forwarded.";
                studentTemplate = "email/application-forwarded";
                studentEventType = NotificationEventType.APPLICATION_FORWARDED;
            }
            case INTERVIEW_SCHEDULED -> {
                studentMessage = "Your interview has been scheduled.";
                studentTemplate = "email/interview-scheduled";
                studentEventType = NotificationEventType.INTERVIEW_SCHEDULED;
            }
            case SELECTED -> {
                studentMessage = "Congratulations! You have been selected.";
                studentTemplate = "email/candidate-selected";
                studentEventType = NotificationEventType.APPLICATION_SELECTED;
            }
            case REJECTED -> {
                studentMessage = "Your application was not selected.";
                studentTemplate = "email/candidate-rejected";
                // Assuming APPLICATION_REVIEWED or similar for rejected
                studentEventType = NotificationEventType.APPLICATION_REVIEWED; 
            }
            default -> {}
        }

        if (studentMessage != null) {
            sendNotification(event.getStudentId(), event.getApplicationId(), studentTitle, studentMessage, studentEventType);
            enqueueEmail(event.getStudentId(), studentEventType, studentTemplate, studentMessage);
        }

        // 2. Enterprise Notification & Email
        if (status == ApplicationStatus.FORWARDED || status == ApplicationStatus.INTERVIEW_SCHEDULED || status == ApplicationStatus.SELECTED) {
            String entMessage = switch (status) {
                case FORWARDED -> "You have received a new forwarded candidate.";
                case INTERVIEW_SCHEDULED -> "An interview has been scheduled with a candidate.";
                case SELECTED -> "A candidate has been selected for your opening.";
                default -> "Candidate status updated.";
            };
            sendNotification(event.getEnterpriseId(), event.getApplicationId(), "Candidate Update", entMessage, studentEventType);
            enqueueEmail(event.getEnterpriseId(), studentEventType, studentTemplate, entMessage);
        }
    }

    private void sendNotification(java.util.UUID recipientId, java.util.UUID applicationId, String title, String message, NotificationEventType type) {
        eventPublisher.publishEvent(
            NotificationEvent.builder()
                .eventType(type != null ? type : NotificationEventType.APPLICATION_REVIEWED)
                .recipientId(recipientId)
                .resourceType("Application")
                .resourceId(applicationId)
                .title(title)
                .message(message)
                .build()
        );
        auditService.recordCustomEvent("APPLICATION_NOTIFICATION_CREATED", "Application", applicationId.toString(), "Notification created for user " + recipientId);
    }

    private void enqueueEmail(java.util.UUID recipientId, NotificationEventType type, String template, String message) {
        taskSchedulerService.enqueueTask(
                TaskPayload.builder()
                        .taskType("EMAIL_DELIVERY")
                        .data(Map.of(
                                "recipientId", recipientId.toString(),
                                "eventType", type != null ? type.name() : NotificationEventType.APPLICATION_REVIEWED.name(),
                                "templateName", template != null ? template : "email/application-status",
                                "message", message
                        ))
                        .build()
        );
        auditService.recordCustomEvent("APPLICATION_EMAIL_QUEUED", "User", recipientId.toString(), "Email task queued for template " + template);
    }

    private void generateActivityRecords(ApplicationStatusChangedEvent event) {
        ActivityType activityType = switch (event.getNewStatus()) {
            case UNDER_REVIEW -> ActivityType.REVIEWED;
            case FORWARDED -> ActivityType.FORWARDED;
            case INTERVIEW_SCHEDULED -> ActivityType.INTERVIEW_SCHEDULED;
            case SELECTED -> ActivityType.SELECTED;
            case REJECTED -> ActivityType.REJECTED;
            default -> null;
        };

        if (activityType != null) {
            activityService.recordActivity(
                    event.getActorType(),
                    event.getActorId().toString(),
                    "Application",
                    event.getApplicationId().toString(),
                    activityType,
                    "Application status changed to " + event.getNewStatus().name()
            );
        }
    }
}
