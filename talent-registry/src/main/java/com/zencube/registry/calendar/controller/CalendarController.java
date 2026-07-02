package com.zencube.registry.calendar.controller;

import com.zencube.registry.calendar.dto.request.CreateCalendarEventRequest;
import com.zencube.registry.calendar.dto.request.UpdateCalendarEventRequest;
import com.zencube.registry.calendar.dto.response.CalendarEventResponse;
import com.zencube.registry.calendar.dto.response.EventSummaryResponse;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.mapper.CalendarMapper;
import com.zencube.registry.calendar.service.CalendarService;
import com.zencube.registry.journal.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar/events")
@RequiredArgsConstructor
@Validated
@Tag(name = "Calendar", description = "Calendar event management endpoints")
@PreAuthorize("isAuthenticated()")
public class CalendarController {

    private final CalendarService calendarService;
    private final AuditService auditService;
    private final CalendarMapper calendarMapper;

    @Operation(summary = "Create Calendar Event", description = "Creates a new calendar event securely")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "409", description = "Conflicting event time")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarEventResponse createEvent(@Valid @RequestBody CreateCalendarEventRequest request) {
        return calendarService.createEvent(request);
    }

    @Operation(summary = "List Calendar Events", description = "Retrieves a paginated list of calendar events with filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events successfully retrieved")
    })
    @GetMapping
    public Page<EventSummaryResponse> listEvents(
            @Parameter(description = "Start of the date range filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End of the date range filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) EventCategory category,
            @Parameter(description = "Filter by timezone")
            @RequestParam(required = false) String timezone,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {

        // For summary DTO, we will map the CalendarEventResponse Page output manually, 
        // since service currently returns Page<CalendarEventResponse>
        Page<CalendarEventResponse> fullResponses = calendarService.listEvents(startDate, endDate, category, timezone, pageable);
        
        return fullResponses.map(full -> EventSummaryResponse.builder()
                .id(full.getId())
                .title(full.getTitle())
                .startTime(full.getStartTime())
                .endTime(full.getEndTime())
                .eventCategory(full.getEventCategory())
                .participantCount(full.getParticipantCount())
                .location(full.getLocation())
                .build());
    }

    @Operation(summary = "Get Event Details", description = "Retrieves full details of a single event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event details retrieved"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{id}")
    public CalendarEventResponse getEvent(
            @Parameter(description = "UUID of the event")
            @PathVariable UUID id) {
        CalendarEventResponse response = calendarService.getEvent(id);
        auditService.recordCustomEvent("EVENT_VIEWED", "CalendarEvent", id.toString(), null);
        return response;
    }

    @Operation(summary = "Update Event", description = "Updates an existing calendar event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Conflicting event time")
    })
    @PutMapping("/{id}")
    public CalendarEventResponse updateEvent(
            @Parameter(description = "UUID of the event")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCalendarEventRequest request) {
        return calendarService.updateEvent(id, request);
    }

    @Operation(summary = "Delete Event", description = "Permanently deletes an event")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(
            @Parameter(description = "UUID of the event")
            @PathVariable UUID id) {
        calendarService.deleteEvent(id);
    }
}
