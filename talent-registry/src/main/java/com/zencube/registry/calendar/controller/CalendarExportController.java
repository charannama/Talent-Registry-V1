package com.zencube.registry.calendar.controller;

import com.zencube.registry.calendar.service.CalendarExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar/events")
@RequiredArgsConstructor
@Validated
@Tag(name = "Calendar Export", description = "Endpoints for exporting calendar data to external ICS formats")
@PreAuthorize("isAuthenticated()")
public class CalendarExportController {

    private final CalendarExportService exportService;

    @Operation(summary = "Export Single Event", description = "Downloads a specific calendar event as an RFC 5545 compliant ICS file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ICS file generated successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping(value = "/{id}/ics", produces = "text/calendar")
    public ResponseEntity<byte[]> exportEvent(
            @Parameter(description = "UUID of the event to export")
            @PathVariable UUID id) {
        
        byte[] icsData = exportService.exportEvent(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "event-" + id + ".ics");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(icsData);
    }

    @Operation(summary = "Export Multiple Events", description = "Downloads all calendar events in a date range as a single ICS file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ICS file generated successfully"),
            @ApiResponse(responseCode = "404", description = "No events found in date range")
    })
    @GetMapping(value = "/export", produces = "text/calendar")
    public ResponseEntity<byte[]> exportEvents(
            @Parameter(description = "Start of the date range (ISO-8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "End of the date range (ISO-8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        
        byte[] icsData = exportService.exportEvents(start, end);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "calendar-export.ics");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(icsData);
    }
}
