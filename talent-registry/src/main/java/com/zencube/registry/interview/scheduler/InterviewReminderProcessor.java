package com.zencube.registry.interview.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.interview.dto.InterviewReminderPayload;
import com.zencube.registry.interview.dto.InterviewReminderType;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.service.NotificationService;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.processor.TaskProcessor;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewReminderProcessor implements TaskProcessor {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private TaskSchedulerService taskSchedulerService;
    
    private final ConfigService configService;
    private final AuditService auditService;
    private final ActivityService activityService;

    @Override
    public boolean supports(String taskType) {
        return "INTERVIEW_REMINDER".equals(taskType);
    }

    @Override
    public void process(ScheduledTask task) {
        log.info("Processing INTERVIEW_REMINDER task {}", task.getId());
        InterviewReminderPayload payload = objectMapper.convertValue(task.getPayload(), InterviewReminderPayload.class);

        // Check if reminders for this type are enabled
        if (payload.getReminderType() == InterviewReminderType.REMINDER_24_HOURS) {
            boolean enabled24h = getConfigValue("INTERVIEW.REMINDER_24H_ENABLED", true);
            if (!enabled24h) {
                log.info("24h interview reminders are disabled globally. Skipping task.");
                return;
            }
        } else if (payload.getReminderType() == InterviewReminderType.REMINDER_1_HOUR) {
            boolean enabled1h = getConfigValue("INTERVIEW.REMINDER_1H_ENABLED", true);
            if (!enabled1h) {
                log.info("1h interview reminders are disabled globally. Skipping task.");
                return;
            }
        }

        // Validate interview is still active in real system (mocked logic here, usually would fetch from DB)
        // If interview was cancelled, the task should have been SKIPPED, but as a safety check we could verify.

        String formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.of(payload.getTimezone()))
                .format(payload.getInterviewTime());
                
        String formattedTime = DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.of(payload.getTimezone()))
                .format(payload.getInterviewTime());

        String reminderLabel = payload.getReminderType() == InterviewReminderType.REMINDER_24_HOURS ? "Tomorrow" : "In 1 Hour";

        // Create Notification
        boolean notifEnabled = getConfigValue("INTERVIEW.REMINDER_NOTIFICATION_ENABLED", true);
        if (notifEnabled) {
            String title = "Interview Reminder";
            String body = String.format("Reminder: Your interview '%s' is scheduled %s at %s (%s).",
                    payload.getInterviewTitle(), reminderLabel.toLowerCase(), formattedTime, payload.getTimezone());
            notificationService.createNotification(
                    payload.getStudentId(),
                    NotificationEventType.INTERVIEW_REMINDER,
                    "Interview",
                    payload.getInterviewId(),
                    title,
                    body
            );
        }

        // Queue Email
        boolean emailEnabled = getConfigValue("INTERVIEW.REMINDER_EMAIL_ENABLED", true);
        if (emailEnabled) {
            TaskPayload emailPayload = TaskPayload.builder()
                    .taskType("EMAIL_DELIVERY")
                    .data(Map.of(
                            "recipientId", payload.getStudentId().toString(),
                            "eventType", NotificationEventType.INTERVIEW_REMINDER.name(),
                            "templateName", "email/interview-reminder",
                            "studentName", "Student", // Would fetch actual profile in a real impl
                            "interviewDate", formattedDate,
                            "interviewTime", formattedTime,
                            "timezone", payload.getTimezone(),
                            "enterpriseName", "Enterprise", // Would fetch
                            "openingTitle", "Opening", // Would fetch
                            "meetingLink", "Link" // Would fetch
                    ))
                    .build();
            taskSchedulerService.enqueueEmailTask(emailPayload);
        }

        // Track Audit and Activity
        auditService.recordCustomEvent("INTERVIEW_REMINDER_SENT", "Interview", payload.getInterviewId().toString(), 
                "Reminder type: " + payload.getReminderType().name());
                
        activityService.recordActivity(
                "System", "System",
                "Interview", payload.getInterviewId().toString(),
                ActivityType.INTERVIEW_REMINDER_SENT,
                "Sent " + payload.getReminderType() + " reminder for interview."
        );
        
        log.info("Successfully processed INTERVIEW_REMINDER task {}", task.getId());
    }
    
    private boolean getConfigValue(String key, boolean defaultValue) {
        try {
            Boolean value = configService.get(key, Boolean.class);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
