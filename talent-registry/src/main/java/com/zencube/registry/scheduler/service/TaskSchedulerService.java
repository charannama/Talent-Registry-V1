package com.zencube.registry.scheduler.service;

import com.zencube.registry.scheduler.dto.TaskPayload;
import java.util.UUID;

public interface TaskSchedulerService {
    
    UUID enqueueTask(TaskPayload payload);

    UUID enqueueScheduledTask(TaskPayload payload, java.time.Instant scheduledAt);
    
    UUID enqueueEmailTask(TaskPayload payload);
    
    UUID enqueueNotificationTask(TaskPayload payload);
    
    void processTask(UUID taskId);
    
    void processPendingTasks();
    
    void retryTask(UUID taskId, Exception e);
    
    void markTaskCompleted(UUID taskId);
    
    void markTaskFailed(UUID taskId, Exception e);
}
