package com.zencube.registry.calendar.mapper;

import com.zencube.registry.calendar.dto.response.CalendarEventResponse;
import com.zencube.registry.calendar.dto.response.EventSummaryResponse;
import com.zencube.registry.calendar.entity.CalendarEvent;
import org.springframework.stereotype.Component;

@Component
public class CalendarMapper {

    public CalendarEventResponse toResponse(CalendarEvent event, long participantCount) {
        if (event == null) {
            return null;
        }

        return CalendarEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .timezone(event.getTimezone())
                .location(event.getLocation())
                .allDayEvent(event.getAllDayEvent())
                .eventCategory(event.getEventCategory())
                .eventableType(event.getEventableType())
                .eventableId(event.getEventableId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .version(event.getVersion())
                .participantCount(participantCount)
                .status(event.isActive() ? "ACTIVE" : (event.isPast() ? "PAST" : "UPCOMING"))
                .build();
    }

    public EventSummaryResponse toSummaryResponse(CalendarEvent event, long participantCount) {
        if (event == null) {
            return null;
        }

        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .eventCategory(event.getEventCategory())
                .participantCount(participantCount)
                .location(event.getLocation())
                .build();
    }
}
