package com.zencube.registry.scheduler.repository;

import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.enums.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, UUID> {

    @Query(value = """
            SELECT * FROM scheduled_tasks 
            WHERE state = 'PENDING' 
              AND scheduled_at <= CURRENT_TIMESTAMP 
              AND (next_attempt_at IS NULL OR next_attempt_at <= CURRENT_TIMESTAMP)
            ORDER BY scheduled_at ASC 
            LIMIT :limit 
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<ScheduledTask> pollPendingTasks(@Param("limit") int limit);

    List<ScheduledTask> findByState(TaskState state);

    @Query("SELECT t FROM ScheduledTask t WHERE t.state = 'FAILED'")
    List<ScheduledTask> findFailedTasks();

    long countByState(TaskState state);

    default long countPendingTasks() {
        return countByState(TaskState.PENDING);
    }

    default long countFailedTasks() {
        return countByState(TaskState.FAILED);
    }

    default long countCompletedTasks() {
        return countByState(TaskState.COMPLETED);
    }

    @Query(value = "SELECT * FROM scheduled_tasks WHERE task_type = 'INTERVIEW_REMINDER' AND state = 'PENDING' AND payload->>'interviewId' = :interviewId", nativeQuery = true)
    List<ScheduledTask> findPendingReminderTasks(@Param("interviewId") String interviewId);
}
