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
public class EventSummaryResponse {
    private UUID id;
    private String title;
    private Instant startTime;
    private Instant endTime;
    private EventCategory eventCategory;
    private Long participantCount;
    private String location;
}
