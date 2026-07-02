package com.zencube.registry.scheduler.scheduler;

import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueuePollingSchedulerTest {

    @Mock
    private TaskSchedulerService taskSchedulerService;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private QueuePollingScheduler queuePollingScheduler;

    @Test
    void pollQueue_ShouldCallProcessPendingTasks() {
        when(configService.get("QUEUE.POLLING_ENABLED", Boolean.class)).thenReturn(true);
        
        queuePollingScheduler.pollQueue();
        
        verify(taskSchedulerService).processPendingTasks();
    }

    @Test
    void pollQueue_ShouldNotCallIfDisabled() {
        when(configService.get("QUEUE.POLLING_ENABLED", Boolean.class)).thenReturn(false);
        
        queuePollingScheduler.pollQueue();
        
        verify(taskSchedulerService, never()).processPendingTasks();
    }
}
