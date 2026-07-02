package com.zencube.registry.calendar.unit;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.calendar.dto.request.CreateCalendarEventRequest;
import com.zencube.registry.calendar.dto.request.UpdateCalendarEventRequest;
import com.zencube.registry.calendar.dto.response.CalendarEventResponse;
import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.exception.EventConflictException;
import com.zencube.registry.calendar.exception.InvalidCalendarEventException;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.calendar.service.impl.CalendarServiceImpl;
import com.zencube.registry.journal.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarEventRepository calendarRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @Test
    void createEvent_success() {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("Test Event")
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("INTERVIEW")
                .eventableId(UUID.randomUUID())
                .build();

        when(calendarRepository.existsConflictingEvent(any(), any(), any(), any(), isNull())).thenReturn(false);
        
        CalendarEvent mockSavedEvent = new CalendarEvent();
        mockSavedEvent.setId(UUID.randomUUID());
        mockSavedEvent.setTitle(request.getTitle());
        when(calendarRepository.save(any(CalendarEvent.class))).thenReturn(mockSavedEvent);

        CalendarEventResponse response = calendarService.createEvent(request);

        assertNotNull(response);
        assertEquals("Test Event", response.getTitle());
        verify(auditService).recordCustomEvent(eq("EVENT_CREATED"), anyString(), anyString(), any());
    }

    @Test
    void createEvent_conflictDetected_throwsException() {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("Conflict Event")
                .startTime(Instant.now())
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("INTERVIEW")
                .eventableId(UUID.randomUUID())
                .build();

        when(calendarRepository.existsConflictingEvent(any(), any(), any(), any(), isNull())).thenReturn(true);

        assertThrows(EventConflictException.class, () -> calendarService.createEvent(request));
    }

    @Test
    void createEvent_invalidTimezone_throwsException() {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .timezone("INVALID/ZONE")
                .build();

        assertThrows(InvalidCalendarEventException.class, () -> calendarService.createEvent(request));
    }

    @Test
    void updateEvent_success() {
        UUID eventId = UUID.randomUUID();
        UpdateCalendarEventRequest request = UpdateCalendarEventRequest.builder()
                .title("Updated Event")
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .build();

        CalendarEvent existingEvent = new CalendarEvent();
        existingEvent.setId(eventId);
        existingEvent.setEventableType("INTERVIEW");
        existingEvent.setEventableId(UUID.randomUUID());

        when(calendarRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(calendarRepository.existsConflictingEvent(any(), any(), any(), any(), eq(eventId))).thenReturn(false);
        when(calendarRepository.save(any(CalendarEvent.class))).thenReturn(existingEvent);

        CalendarEventResponse response = calendarService.updateEvent(eventId, request);

        assertNotNull(response);
        verify(auditService).recordCustomEvent(eq("EVENT_UPDATED"), anyString(), anyString(), any());
    }

    @Test
    void deleteEvent_success() {
        UUID eventId = UUID.randomUUID();
        CalendarEvent existingEvent = new CalendarEvent();
        existingEvent.setId(eventId);

        when(calendarRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        calendarService.deleteEvent(eventId);

        verify(calendarRepository).delete(existingEvent);
        verify(auditService).recordCustomEvent(eq("EVENT_DELETED"), anyString(), anyString(), any());
    }
}
