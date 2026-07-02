package com.zencube.registry.calendar.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.calendar.controller.CalendarController;
import com.zencube.registry.calendar.dto.request.CreateCalendarEventRequest;
import com.zencube.registry.calendar.dto.response.CalendarEventResponse;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.mapper.CalendarMapper;
import com.zencube.registry.calendar.service.CalendarService;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for basic unit test
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private CalendarMapper calendarMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser
    void createEvent_returns201() throws Exception {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("API Test Event")
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("INTERVIEW")
                .eventableId(UUID.randomUUID())
                .build();

        CalendarEventResponse response = CalendarEventResponse.builder()
                .id(UUID.randomUUID())
                .title("API Test Event")
                .build();

        when(calendarService.createEvent(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/calendar/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("API Test Event"));
    }

    @Test
    @WithMockUser
    void getEvent_returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        CalendarEventResponse response = CalendarEventResponse.builder()
                .id(eventId)
                .title("API Test Event")
                .build();

        when(calendarService.getEvent(eventId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/calendar/events/{id}", eventId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("API Test Event"));

        verify(auditService).recordCustomEvent("EVENT_VIEWED", "CalendarEvent", eventId.toString(), null);
    }

    @Test
    @WithMockUser
    void deleteEvent_returns204() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/calendar/events/{id}", eventId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(calendarService).deleteEvent(eventId);
    }
}

