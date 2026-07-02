package com.zencube.registry.calendar.service.impl;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.entity.CalendarParticipant;
import com.zencube.registry.calendar.exception.EventNotFoundException;
import com.zencube.registry.calendar.export.ICSGenerator;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.calendar.repository.CalendarParticipantRepository;
import com.zencube.registry.calendar.service.CalendarExportService;
import com.zencube.registry.journal.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarExportServiceImpl implements CalendarExportService {

    private final CalendarEventRepository eventRepository;
    private final CalendarParticipantRepository participantRepository;
    private final ICSGenerator icsGenerator;
    private final AuditService auditService;
    private final ActivityService activityService;

    @Override
    public byte[] exportEvent(UUID eventId) {
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Calendar event not found with ID: " + eventId));

        List<CalendarParticipant> participants = participantRepository.findByEventId(eventId);
        
        String icsContent = icsGenerator.generateICS(event, participants);

        auditService.recordCustomEvent("ICS_EXPORTED", "CalendarEvent", event.getId().toString(), "Single Event Export");
        activityService.recordActivity(
                "CalendarEvent",
                event.getId().toString(),
                event.getEventableType(),
                event.getEventableId() != null ? event.getEventableId().toString() : null,
                com.zencube.registry.activity.enums.ActivityType.CALENDAR_EVENT_CREATED,
                "Exported calendar event to ICS"
        );

        return icsContent.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportEvents(Instant start, Instant end) {
        List<CalendarEvent> events = eventRepository.findEventsBetween(start, end);
        
        if (events.isEmpty()) {
            throw new EventNotFoundException("No calendar events found in the specified date range.");
        }

        List<List<CalendarParticipant>> participantsList = events.stream()
                .map(e -> participantRepository.findByEventId(e.getId()))
                .collect(Collectors.toList());

        String icsContent = icsGenerator.generateICS(events, participantsList);

        auditService.recordCustomEvent("ICS_EXPORTED", "CalendarEvent", "BULK", events.size() + " events exported");

        return icsContent.getBytes(StandardCharsets.UTF_8);
    }
}


