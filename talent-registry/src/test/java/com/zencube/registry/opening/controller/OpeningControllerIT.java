package com.zencube.registry.opening.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.opening.dto.request.CreateOpeningRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.enums.WorkMode;
import com.zencube.registry.opening.service.OpeningService;
import com.zencube.registry.enterprise.security.EnterpriseSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpeningController.class)
@AutoConfigureMockMvc(addFilters = true) // Run with filters enabled to test @PreAuthorize and endpoint security
@DisplayName("OpeningController Integration Tests")
class OpeningControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OpeningService openingService;

    @MockBean(name = "enterpriseSecurity")
    private EnterpriseSecurity enterpriseSecurity;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.zencube.registry.session.service.SessionService sessionService;

    private UUID enterpriseId;
    private CreateOpeningRequest validRequest;
    private OpeningResponse expectedResponse;

    @BeforeEach
    void setUp() {
        enterpriseId = UUID.randomUUID();
        validRequest = CreateOpeningRequest.builder()
                .enterpriseId(enterpriseId)
                .title("Software Developer")
                .jobType(JobType.FULL_TIME)
                .workMode(WorkMode.HYBRID)
                .positions(1)
                .deadline(Instant.now().plus(30, ChronoUnit.DAYS))
                .requiredSkills(List.of("Java"))
                .build();

        expectedResponse = OpeningResponse.builder()
                .id(UUID.randomUUID())
                .enterpriseId(enterpriseId)
                .title("Software Developer")
                .status(OpeningStatus.DRAFT)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/openings - 201 Created when owner and authorized")
    @WithMockUser(authorities = "OPENING_CREATE")
    void createOpening_returns201_whenAuthorizedOwner() throws Exception {
        // Arrange
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(true);
        when(openingService.createOpening(any(CreateOpeningRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/openings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.data.title").value("Software Developer"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        verify(openingService, times(1)).createOpening(any(CreateOpeningRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/openings - 403 Forbidden when not owner")
    @WithMockUser(authorities = "OPENING_CREATE")
    void createOpening_returns403_whenNotOwner() throws Exception {
        // Arrange
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1/openings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(openingService, never()).createOpening(any(CreateOpeningRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/openings - 403 Forbidden when owner but missing authority")
    @WithMockUser(authorities = "SOME_OTHER_PERMISSION")
    void createOpening_returns403_whenMissingPermission() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/openings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(openingService, never()).createOpening(any(CreateOpeningRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/openings - 401 Unauthorized when unauthenticated")
    void createOpening_returns401_whenUnauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/openings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/openings - 400 Bad Request on validation error")
    @WithMockUser(authorities = "OPENING_CREATE")
    void createOpening_returns400_onValidationError() throws Exception {
        // Arrange
        validRequest.setTitle(""); // Title is blank (violates @NotBlank)
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/openings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
}
