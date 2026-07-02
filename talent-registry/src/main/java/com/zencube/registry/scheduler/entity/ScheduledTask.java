package com.zencube.registry.scheduler.entity;

import com.zencube.registry.scheduler.enums.TaskState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "scheduled_tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "task_type", nullable = false, length = 100)
    private String taskType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    @Builder.Default
    private TaskState state = TaskState.PENDING;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Helper methods for state transitions
    public void markRunning() {
        this.state = TaskState.RUNNING;
        this.startedAt = Instant.now();
    }

    public void markCompleted() {
        this.state = TaskState.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.state = TaskState.FAILED;
        this.lastError = error;
    }

    public void markSkipped() {
        this.state = TaskState.SKIPPED;
        this.completedAt = Instant.now();
    }

    public void scheduleRetry(Instant nextAttemptAt) {
        this.state = TaskState.PENDING;
        this.nextAttemptAt = nextAttemptAt;
    }

    public boolean canRetry() {
        return this.attempts < this.maxAttempts;
    }

    public boolean isPending() {
        return this.state == TaskState.PENDING;
    }

    public boolean isCompleted() {
        return this.state == TaskState.COMPLETED;
    }
}
