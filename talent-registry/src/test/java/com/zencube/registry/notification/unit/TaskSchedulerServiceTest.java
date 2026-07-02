package com.zencube.registry.notification.unit;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.enums.TaskState;
import com.zencube.registry.scheduler.processor.TaskProcessor;
import com.zencube.registry.scheduler.repository.ScheduledTaskRepository;
import com.zencube.registry.scheduler.service.impl.TaskSchedulerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSchedulerServiceTest {

    @Mock
    private ScheduledTaskRepository taskRepository;

    @Mock
    private List<TaskProcessor> taskProcessors;

    @Mock
    private ConfigService configService;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private TaskSchedulerServiceImpl taskSchedulerService;

    @Test
    void enqueueTask_success() {
        TaskPayload payload = TaskPayload.builder().taskType("EMAIL_DELIVERY").build();
        when(configService.get("QUEUE.MAX_ATTEMPTS", Integer.class)).thenReturn(3);
        
        ScheduledTask savedTask = new ScheduledTask();
        savedTask.setId(UUID.randomUUID());
        savedTask.setTaskType("EMAIL_DELIVERY");
        when(taskRepository.save(any(ScheduledTask.class))).thenReturn(savedTask);

        UUID taskId = taskSchedulerService.enqueueTask(payload);

        assertNotNull(taskId);
        verify(taskRepository).save(any(ScheduledTask.class));
    }

    @Test
    void markTaskCompleted_success() {
        ScheduledTask task = new ScheduledTask();
        task.setId(UUID.randomUUID());
        task.setState(TaskState.PENDING);
        
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskSchedulerService.markTaskCompleted(task.getId());

        assertEquals(TaskState.COMPLETED, task.getState());
        verify(taskRepository).save(task);
    }

    @Test
    void retryTask_incrementsAttemptsAndSetsPending() {
        ScheduledTask task = new ScheduledTask();
        task.setId(UUID.randomUUID());
        task.setAttempts(1);
        task.setMaxAttempts(3);
        task.setState(TaskState.FAILED);
        
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskSchedulerService.retryTask(task.getId(), new RuntimeException("Timeout"));

        assertEquals(2, task.getAttempts());
        assertEquals(TaskState.PENDING, task.getState());
        assertEquals("Timeout", task.getLastError());
        assertNotNull(task.getNextAttemptAt());
        verify(taskRepository).save(task);
    }
}
