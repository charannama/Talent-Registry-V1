package com.zencube.registry.calendar.integration;

import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarRepositoryTest extends IntegrationTestBase {

    @Autowired
    private CalendarEventRepository calendarRepository;

    @Test
    @Transactional
    void findByEventableTypeAndEventableId_returnsCorrectEvents() {
        UUID eventableId = UUID.randomUUID();
        
        CalendarEvent event = CalendarEvent.builder()
                .title("Repository Test")
                .startTime(Instant.now())
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("INTERVIEW")
                .eventableId(eventableId)
                .build();
        
        calendarRepository.save(event);

        List<CalendarEvent> results = calendarRepository.findByEventableTypeAndEventableId("INTERVIEW", eventableId);
        
        assertFalse(results.isEmpty());
        assertEquals("Repository Test", results.get(0).getTitle());
    }

    @Test
    @Transactional
    void existsConflictingEvent_detectsOverlap() {
        UUID eventableId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        
        CalendarEvent event = CalendarEvent.builder()
                .title("Existing Event")
                .startTime(start)
                .endTime(end)
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("INTERVIEW")
                .eventableId(eventableId)
                .build();
        
        calendarRepository.save(event);

        // Conflict: starts inside the existing event
        boolean conflict1 = calendarRepository.existsConflictingEvent(
                "INTERVIEW", eventableId, start.plus(1, ChronoUnit.HOURS), start.plus(3, ChronoUnit.HOURS), null);
        assertTrue(conflict1);

        // No Conflict: starts after the existing event
        boolean conflict2 = calendarRepository.existsConflictingEvent(
                "INTERVIEW", eventableId, end.plus(1, ChronoUnit.MINUTES), end.plus(1, ChronoUnit.HOURS), null);
        assertFalse(conflict2);
    }
}
