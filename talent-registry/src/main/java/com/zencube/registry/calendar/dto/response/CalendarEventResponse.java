package com.zencube.registry.calendar.dto.response;

import com.zencube.registry.calendar.enums.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventResponse {
    
    private UUID id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private String timezone;
    private String location;
    private Boolean allDayEvent;
    private EventCategory eventCategory;
    private String eventableType;
    private UUID eventableId;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    private Long participantCount;
    private String status;
}
