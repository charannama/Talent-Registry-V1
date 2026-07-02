package com.zencube.registry.scheduler.repository;

import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.enums.TaskState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledTaskRepositoryTest extends IntegrationTestBase {

    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;

    @Test
    void pollPendingTasks_ShouldReturnTasksMatchingCriteria() {
        ScheduledTask pendingReady = new ScheduledTask();
        pendingReady.setTaskType("EMAIL_DELIVERY");
        pendingReady.setPayload(Map.of("id", "1"));
        pendingReady.setState(TaskState.PENDING);
        pendingReady.setScheduledAt(Instant.now().minusSeconds(60));
        scheduledTaskRepository.save(pendingReady);

        ScheduledTask pendingFuture = new ScheduledTask();
        pendingFuture.setTaskType("EMAIL_DELIVERY");
        pendingFuture.setPayload(Map.of("id", "2"));
        pendingFuture.setState(TaskState.PENDING);
        pendingFuture.setScheduledAt(Instant.now().plusSeconds(3600));
        scheduledTaskRepository.save(pendingFuture);
        
        ScheduledTask completed = new ScheduledTask();
        completed.setTaskType("EMAIL_DELIVERY");
        completed.setPayload(Map.of("id", "3"));
        completed.setState(TaskState.COMPLETED);
        completed.setScheduledAt(Instant.now().minusSeconds(60));
        scheduledTaskRepository.save(completed);

        List<ScheduledTask> tasks = scheduledTaskRepository.pollPendingTasks(10);
        
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getId()).isEqualTo(pendingReady.getId());
    }
}
