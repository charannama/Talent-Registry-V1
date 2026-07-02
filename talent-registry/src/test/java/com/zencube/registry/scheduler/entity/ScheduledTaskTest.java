package com.zencube.registry.scheduler.entity;

import com.zencube.registry.scheduler.enums.TaskState;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledTaskTest {

    @Test
    void testStateTransitions() {
        ScheduledTask task = new ScheduledTask();
        task.setAttempts(0);
        task.setMaxAttempts(3);
        
        assertThat(task.isPending()).isTrue();
        
        task.markRunning();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStartedAt()).isNotNull();
        
        task.markCompleted();
        assertThat(task.isCompleted()).isTrue();
        assertThat(task.getCompletedAt()).isNotNull();
    }

    @Test
    void testFailureAndRetry() {
        ScheduledTask task = new ScheduledTask();
        task.setAttempts(0);
        task.setMaxAttempts(3);
        
        task.markFailed("Some error");
        assertThat(task.getState()).isEqualTo(TaskState.FAILED);
        assertThat(task.getLastError()).isEqualTo("Some error");
        
        assertThat(task.canRetry()).isTrue();
        
        Instant nextAttempt = Instant.now().plusSeconds(60);
        task.scheduleRetry(nextAttempt);
        
        assertThat(task.isPending()).isTrue();
        assertThat(task.getNextAttemptAt()).isEqualTo(nextAttempt);
    }
}
