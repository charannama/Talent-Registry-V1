package com.zencube.registry.opening.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.service.OpeningService;
import com.zencube.registry.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OpeningController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller test, or mock Security context
class OpeningControllerSubmitIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OpeningService openingService;

    // We also need to mock beans required by WebMvcTest or Security context if we don't disable filters
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/openings/{id}/submit - Success")
    @WithMockUser(authorities = "OPENING_SUBMIT")
    void submitOpening_Success() throws Exception {
        UUID openingId = UUID.randomUUID();
        OpeningResponse mockResponse = new OpeningResponse();
        mockResponse.setId(openingId);
        mockResponse.setStatus(OpeningStatus.PENDING_APPROVAL);

        when(openingService.submitOpening(openingId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/openings/{id}/submit", openingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Opening submitted successfully for HR approval"))
                .andExpect(jsonPath("$.data.id").value(openingId.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
    }
}
