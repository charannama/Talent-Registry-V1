package com.zencube.registry.calendar.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.calendar.dto.request.CreateCalendarEventRequest;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendarControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CalendarEventRepository calendarRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    private String validJwt;

    @BeforeEach
    void setUp() {
        // Setup a user and generate JWT
        User user = new User();
        user.setEmail("admin@zencube.com");
        user.setPasswordHash("encoded_password");
        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@zencube.com");
        validJwt = jwtService.generateAccessToken(userDetails);
        calendarRepository.deleteAll();
    }

    @Test
    void createEvent_authenticatedUser_success() throws Exception {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("Integration Test Event")
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("INTERVIEW")
                .eventableId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/calendar/events")
                        .header("Authorization", "Bearer " + validJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Event"));
    }

    @Test
    void createEvent_unauthenticated_forbidden() throws Exception {
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("Unauthorized Test Event")
                .build();

        mockMvc.perform(post("/api/v1/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}




