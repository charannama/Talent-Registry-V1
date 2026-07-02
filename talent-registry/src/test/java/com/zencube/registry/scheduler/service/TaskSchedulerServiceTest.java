package com.zencube.registry.scheduler.service;

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
import com.zencube.registry.scheduler.service.impl.TaskSchedulerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSchedulerServiceTest {

    @Mock
    private ScheduledTaskRepository taskRepository;

    @Mock
    private ConfigService configService;

    @Mock
    private ActivityService activityService;

    @Mock
    private TaskProcessor taskProcessor;

    private TaskSchedulerServiceImpl taskSchedulerService;

    @BeforeEach
    void setUp() {
        taskSchedulerService = new TaskSchedulerServiceImpl(
                taskRepository, List.of(taskProcessor), configService, activityService
        );
    }

    @Test
    void enqueueTask_ShouldPersistAndReturnId() {
        when(configService.get("QUEUE.MAX_ATTEMPTS", Integer.class)).thenReturn(5);
        ScheduledTask savedTask = new ScheduledTask();
        savedTask.setId(UUID.randomUUID());
        when(taskRepository.save(any())).thenReturn(savedTask);

        TaskPayload payload = new TaskPayload("EMAIL_DELIVERY", Map.of("key", "value"));
        UUID id = taskSchedulerService.enqueueTask(payload);

        assertThat(id).isEqualTo(savedTask.getId());
        verify(taskRepository).save(any(ScheduledTask.class));
    }

    @Test
    void processTask_ShouldMarkRunningAndExecute() {
        UUID taskId = UUID.randomUUID();
        ScheduledTask task = new ScheduledTask();
        task.setId(taskId);
        task.setTaskType("EMAIL_DELIVERY");
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskProcessor.supports("EMAIL_DELIVERY")).thenReturn(true);
        
        taskSchedulerService.processTask(taskId);
        
        verify(taskProcessor).process(task);
        verify(taskRepository, atLeastOnce()).saveAndFlush(task);
        verify(taskRepository, atLeastOnce()).save(task);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
    }

    @Test
    void processTask_ProcessorNotFound_ShouldFailFatally() {
        UUID taskId = UUID.randomUUID();
        ScheduledTask task = new ScheduledTask();
        task.setId(taskId);
        task.setTaskType("UNKNOWN_TASK");
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        taskSchedulerService.processTask(taskId);
        
        assertThat(task.getState()).isEqualTo(TaskState.FAILED);
        verify(activityService).recordActivity(anyString(), anyString(), anyString(), anyString(), eq(ActivityType.TASK_FAILED), anyString());
    }

    @Test
    void retryTask_UnderMaxAttempts_ShouldScheduleNextAttempt() {
        UUID taskId = UUID.randomUUID();
        ScheduledTask task = new ScheduledTask();
        task.setId(taskId);
        task.setAttempts(1);
        task.setMaxAttempts(3);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(configService.get("QUEUE.BACKOFF_STRATEGY", String.class)).thenReturn("EXPONENTIAL");
        
        taskSchedulerService.retryTask(taskId, new RuntimeException("Error"));
        
        assertThat(task.getState()).isEqualTo(TaskState.PENDING);
        assertThat(task.getAttempts()).isEqualTo(2);
        assertThat(task.getNextAttemptAt()).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void retryTask_MaxAttemptsReached_ShouldSkip() {
        UUID taskId = UUID.randomUUID();
        ScheduledTask task = new ScheduledTask();
        task.setId(taskId);
        task.setAttempts(2);
        task.setMaxAttempts(3);
        task.setTaskType("EMAIL_DELIVERY");
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        assertThrows(TaskRetryExceededException.class, () -> {
            taskSchedulerService.retryTask(taskId, new RuntimeException("Final Error"));
        });
        
        assertThat(task.getState()).isEqualTo(TaskState.SKIPPED);
        verify(activityService).recordActivity(anyString(), anyString(), anyString(), anyString(), eq(ActivityType.TASK_FAILED), anyString());
    }
}
