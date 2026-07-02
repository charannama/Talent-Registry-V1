package com.zencube.registry.scheduler.service.impl;

import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.enums.TaskState;
import com.zencube.registry.scheduler.exception.TaskExecutionException;
import com.zencube.registry.scheduler.exception.TaskProcessorNotFoundException;
import com.zencube.registry.scheduler.exception.TaskRetryExceededException;
import com.zencube.registry.scheduler.processor.TaskProcessor;
import com.zencube.registry.scheduler.repository.ScheduledTaskRepository;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSchedulerServiceImpl implements TaskSchedulerService {

    private final ScheduledTaskRepository taskRepository;
    private final List<TaskProcessor> taskProcessors;
    private final ConfigService configService;
    private final ActivityService activityService;

    // Default configuration values
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final int DEFAULT_BATCH_SIZE = 50;
    
    @Override
    @Transactional
    public UUID enqueueTask(TaskPayload payload) {
        int maxAttempts = getMaxAttemptsFromConfig();
        
        ScheduledTask task = ScheduledTask.builder()
                .taskType(payload.getTaskType())
                .payload(payload.getData())
                .state(TaskState.PENDING)
                .attempts(0)
                .maxAttempts(maxAttempts)
                .scheduledAt(Instant.now())
                .build();
                
        task = taskRepository.save(task);
        log.debug("Enqueued task {} of type {}", task.getId(), task.getTaskType());
        
        return task.getId();
    }

    @Override
    @Transactional
    public UUID enqueueScheduledTask(TaskPayload payload, Instant scheduledAt) {
        int maxAttempts = getMaxAttemptsFromConfig();

        ScheduledTask task = ScheduledTask.builder()
                .taskType(payload.getTaskType())
                .payload(payload.getData())
                .state(TaskState.PENDING)
                .attempts(0)
                .maxAttempts(maxAttempts)
                .scheduledAt(scheduledAt)
                .build();

        task = taskRepository.save(task);
        log.debug("Enqueued task {} of type {} scheduled at {}", task.getId(), task.getTaskType(), scheduledAt);

        return task.getId();
    }

    @Override
    @Transactional
    public UUID enqueueEmailTask(TaskPayload payload) {
        payload.setTaskType("EMAIL_DELIVERY");
        return enqueueTask(payload);
    }

    @Override
    @Transactional
    public UUID enqueueNotificationTask(TaskPayload payload) {
        payload.setTaskType("NOTIFICATION");
        return enqueueTask(payload);
    }

    @Override
    @Transactional
    public void processPendingTasks() {
        int batchSize = getBatchSizeFromConfig();
        
        List<ScheduledTask> tasks = taskRepository.pollPendingTasks(batchSize);
        if (tasks.isEmpty()) {
            return;
        }

        log.debug("Polled {} pending tasks for processing", tasks.size());
        
        for (ScheduledTask task : tasks) {
            try {
                // Must route to processTask which locks it and processes it.
                // Since processPendingTasks is @Transactional, they are all in one transaction.
                // However, processTask should ideally be in a separate transaction so one failure doesn't rollback others.
                // For now, Spring handles the outer transaction. 
                processTaskDirectly(task);
            } catch (Exception e) {
                log.error("Error processing task {} in batch: {}", task.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processTask(UUID taskId) {
        ScheduledTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskExecutionException("Task not found: " + taskId));
        
        processTaskDirectly(task);
    }
    
    private void processTaskDirectly(ScheduledTask task) {
        try {
            task.markRunning();
            taskRepository.saveAndFlush(task);
            
            TaskProcessor processor = getProcessor(task.getTaskType());
            processor.process(task);
            
            markTaskCompleted(task.getId());
        } catch (TaskProcessorNotFoundException e) {
            log.error("No processor found for task {}", task.getId());
            markTaskFailed(task.getId(), e);
        } catch (Exception e) {
            log.error("Task {} execution failed: {}", task.getId(), e.getMessage());
            retryTask(task.getId(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retryTask(UUID taskId, Exception e) {
        ScheduledTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskExecutionException("Task not found for retry: " + taskId));
                
        int nextAttempt = task.getAttempts() + 1;
        task.setAttempts(nextAttempt);
        task.setLastError(e.getMessage() != null ? e.getMessage() : "Unknown error");

        if (nextAttempt >= task.getMaxAttempts()) {
            log.error("Task {} reached max attempts ({}). Marking as SKIPPED.", task.getId(), task.getMaxAttempts());
            task.markSkipped();
            taskRepository.save(task);
            
            // Record activity for skipped task
            recordTaskActivity(task, ActivityType.TASK_FAILED, "Task skipped after reaching max retries: " + task.getTaskType());
            throw new TaskRetryExceededException("Task reached max retries and was skipped: " + taskId);
        }

        // Exponential backoff
        // attempt 1 -> 5 min, attempt 2 -> 25 min, etc.
        long backoffMinutes = getBackoffMinutes(nextAttempt);
        Instant nextAttemptAt = Instant.now().plus(backoffMinutes, ChronoUnit.MINUTES);
        
        task.scheduleRetry(nextAttemptAt);
        taskRepository.save(task);
        log.warn("Task {} scheduled for retry {} at {}", task.getId(), nextAttempt, nextAttemptAt);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTaskCompleted(UUID taskId) {
        ScheduledTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskExecutionException("Task not found: " + taskId));
                
        task.markCompleted();
        task.setLastError(null);
        taskRepository.save(task);
        log.debug("Task {} marked as COMPLETED", taskId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTaskFailed(UUID taskId, Exception e) {
        ScheduledTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskExecutionException("Task not found: " + taskId));
                
        task.markFailed(e.getMessage() != null ? e.getMessage() : "Unknown error");
        taskRepository.save(task);
        log.error("Task {} marked as FAILED fatally", taskId);
        
        recordTaskActivity(task, ActivityType.TASK_FAILED, "Task failed fatally: " + task.getTaskType());
    }

    private TaskProcessor getProcessor(String taskType) {
        return taskProcessors.stream()
                .filter(p -> p.supports(taskType))
                .findFirst()
                .orElseThrow(() -> new TaskProcessorNotFoundException("No processor found for task type: " + taskType));
    }
    
    private void recordTaskActivity(ScheduledTask task, ActivityType type, String message) {
        try {
            activityService.recordActivity(
                    "ScheduledTask", task.getId().toString(),
                    "System", "System",
                    type,
                    message
            );
        } catch (Exception ex) {
            log.warn("Failed to record activity for task {}: {}", task.getId(), ex.getMessage());
        }
    }

    private int getMaxAttemptsFromConfig() {
        try {
            return configService.get("QUEUE.MAX_ATTEMPTS", Integer.class);
        } catch (Exception e) {
            return DEFAULT_MAX_ATTEMPTS;
        }
    }

    private int getBatchSizeFromConfig() {
        try {
            return configService.get("QUEUE.BATCH_SIZE", Integer.class);
        } catch (Exception e) {
            return DEFAULT_BATCH_SIZE;
        }
    }
    
    private long getBackoffMinutes(int attempt) {
        try {
            String strategy = configService.get("QUEUE.BACKOFF_STRATEGY", String.class);
            if ("EXPONENTIAL".equalsIgnoreCase(strategy)) {
                return (long) Math.pow(5, attempt);
            }
        } catch (Exception e) {
            // Default to exponential if config missing
        }
        return (long) Math.pow(5, attempt);
    }
}
