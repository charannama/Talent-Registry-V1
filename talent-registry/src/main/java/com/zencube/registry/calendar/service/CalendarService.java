package com.zencube.registry.calendar.service;

import com.zencube.registry.calendar.dto.request.CreateCalendarEventRequest;
import com.zencube.registry.calendar.dto.request.UpdateCalendarEventRequest;
import com.zencube.registry.calendar.dto.response.CalendarEventResponse;
import com.zencube.registry.calendar.enums.EventCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CalendarService {

    CalendarEventResponse createEvent(CreateCalendarEventRequest request);

    CalendarEventResponse updateEvent(UUID eventId, UpdateCalendarEventRequest request);

    void deleteEvent(UUID eventId);

    CalendarEventResponse getEvent(UUID eventId);

    Page<CalendarEventResponse> listEvents(Instant start, Instant end, EventCategory category, String timezone, Pageable pageable);

    List<CalendarEventResponse> getEventsForEntity(String eventableType, UUID eventableId);

    List<CalendarEventResponse> getUpcomingEvents();

    boolean checkConflicts(String eventableType, UUID eventableId, Instant start, Instant end, UUID excludeEventId);
}
