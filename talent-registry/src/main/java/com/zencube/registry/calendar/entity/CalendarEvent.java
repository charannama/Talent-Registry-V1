package com.zencube.registry.calendar.entity;

import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calendar_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent extends BaseEntity {



    @NotBlank(message = "Title cannot be empty")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Start time is mandatory")
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @NotNull(message = "End time is mandatory")
    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @NotNull(message = "Timezone is mandatory")
    @Column(nullable = false, length = 100)
    private String timezone;

    @Column(length = 500)
    private String location;

    @Column(name = "all_day_event")
    private Boolean allDayEvent;

    @NotNull(message = "Event category is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_category", nullable = false, length = 50)
    private EventCategory eventCategory;

    @NotNull(message = "Eventable type is mandatory")
    @Column(name = "eventable_type", nullable = false, length = 100)
    private String eventableType;

    @NotNull(message = "Eventable ID is mandatory")
    @Column(name = "eventable_id", nullable = false)
    private UUID eventableId;



    public void addDuration(Duration duration) {
        if (this.endTime != null) {
            this.endTime = this.endTime.plus(duration);
        }
    }

    public boolean isUpcoming() {
        return startTime != null && Instant.now().isBefore(startTime);
    }

    public boolean isActive() {
        if (startTime == null || endTime == null) return false;
        Instant now = Instant.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    public boolean isPast() {
        return endTime != null && Instant.now().isAfter(endTime);
    }

    public boolean isAllDay() {
        return Boolean.TRUE.equals(allDayEvent);
    }

    public Duration getDuration() {
        if (startTime == null || endTime == null) return Duration.ZERO;
        return Duration.between(startTime, endTime);
    }

    public void validateTimeRange() {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
    
    @PrePersist
    @PreUpdate
    private void preSaveValidation() {
        validateTimeRange();
        if (allDayEvent == null) {
            allDayEvent = false;
        }
    }
}
