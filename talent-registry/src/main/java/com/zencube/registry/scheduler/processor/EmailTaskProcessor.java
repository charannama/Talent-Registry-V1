package com.zencube.registry.scheduler.processor;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.notification.email.EmailService;
import com.zencube.registry.notification.email.EmailTemplateService;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.service.NotificationPreferenceService;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.exception.TaskExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTaskProcessor implements TaskProcessor {

    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final UserRepository userRepository;

    private static final String TASK_TYPE = "EMAIL";

    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType) || "EMAIL_DELIVERY".equals(taskType);
    }

    @Override
    public void process(ScheduledTask task) {
        log.info("Processing Email Task {}", task.getId());
        
        Map<String, Object> payload = task.getPayload();
        
        try {
            String recipientIdStr = (String) payload.get("recipientId");
            if (recipientIdStr == null) {
                throw new TaskExecutionException("Missing recipientId in payload");
            }
            
            UUID recipientId = UUID.fromString(recipientIdStr);
            String eventTypeStr = (String) payload.get("eventType");
            
            if (eventTypeStr == null) {
                throw new TaskExecutionException("Missing eventType in payload");
            }
            
            NotificationEventType eventType = NotificationEventType.valueOf(eventTypeStr);

            // Double check preferences (Global & Event specific) before sending
            // Note: Mandatory emails like PASSWORD_RESET should bypass if needed, but for now we rely on the service
            if (!isMandatory(eventType) && !notificationPreferenceService.shouldSendEmail(recipientId, eventType)) {
                log.info("Email delivery skipped for task {} due to user preferences", task.getId());
                return; // Graceful stop, task will be marked as COMPLETED by the scheduler
            }

            User user = userRepository.findById(recipientId)
                    .orElseThrow(() -> new TaskExecutionException("User not found: " + recipientId));

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                throw new TaskExecutionException("User has no email address configured");
            }

            emailService.sendTemplateEmail(user.getEmail(), eventType, payload);
            
            log.info("Successfully sent email to {} for task {}", user.getEmail(), task.getId());
        } catch (Exception e) {
            log.error("Failed to execute Email Task {}: {}", task.getId(), e.getMessage());
            throw new TaskExecutionException("Email processing failed: " + e.getMessage(), e);
        }
    }

    private boolean isMandatory(NotificationEventType type) {
        return type == NotificationEventType.PASSWORD_RESET || type == NotificationEventType.EMAIL_VERIFIED;
    }
}
