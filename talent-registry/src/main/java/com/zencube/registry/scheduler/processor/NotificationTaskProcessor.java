package com.zencube.registry.scheduler.processor;

import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.exception.TaskExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTaskProcessor implements TaskProcessor {

    private static final String TASK_TYPE = "NOTIFICATION";

    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType);
    }

    @Override
    public void process(ScheduledTask task) {
        log.info("Processing Notification Task {}", task.getId());
        
        // This is a placeholder for future generic notification tasks, like bulk dispatch
        // or delayed notifications that shouldn't block the main thread.
        try {
            // Extract payload and process
            log.info("Successfully processed Notification Task {}", task.getId());
        } catch (Exception e) {
            log.error("Failed to execute Notification Task {}: {}", task.getId(), e.getMessage());
            throw new TaskExecutionException("Notification processing failed: " + e.getMessage(), e);
        }
    }
}
