package com.zencube.registry.calendar.unit;

import com.zencube.registry.calendar.controller.CalendarExportController;
import com.zencube.registry.calendar.service.CalendarExportService;
import com.zencube.registry.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarExportController.class)
@AutoConfigureMockMvc(addFilters = false)
class CalendarExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarExportService exportService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser
    void exportSingleEvent_returnsIcsFile() throws Exception {
        UUID eventId = UUID.randomUUID();
        byte[] mockIcsData = "BEGIN:VCALENDAR\nEND:VCALENDAR".getBytes(StandardCharsets.UTF_8);

        when(exportService.exportEvent(eventId)).thenReturn(mockIcsData);

        mockMvc.perform(get("/api/v1/calendar/events/{id}/ics", eventId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/calendar"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"event-" + eventId + ".ics\""))
                .andExpect(content().bytes(mockIcsData));
    }

    @Test
    @WithMockUser
    void exportEventsRange_returnsIcsFile() throws Exception {
        byte[] mockIcsData = "BEGIN:VCALENDAR\nEND:VCALENDAR".getBytes(StandardCharsets.UTF_8);

        // mock any instant
        when(exportService.exportEvents(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockIcsData);

        mockMvc.perform(get("/api/v1/calendar/events/export")
                        .param("start", "2026-01-01T00:00:00Z")
                        .param("end", "2026-12-31T23:59:59Z")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/calendar"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"calendar-export.ics\""))
                .andExpect(content().bytes(mockIcsData));
    }
}

