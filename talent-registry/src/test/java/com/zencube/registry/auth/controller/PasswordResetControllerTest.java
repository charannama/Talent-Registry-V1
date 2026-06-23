package com.zencube.registry.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.dto.request.PasswordResetConfirmDto;
import com.zencube.registry.auth.dto.request.PasswordResetRequestDto;
import com.zencube.registry.auth.service.AuthService;
import com.zencube.registry.common.exception.InvalidTokenException;
import com.zencube.registry.passwordreset.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private com.zencube.registry.auth.service.OAuth2TokenExchangeService oauth2ExchangeService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void requestReset_ValidEmail_ReturnsOk() throws Exception {
        PasswordResetRequestDto request = new PasswordResetRequestDto("test@example.com");

        doNothing().when(passwordResetService).requestReset(anyString());

        mockMvc.perform(post("/api/v1/auth/password/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account with that email exists, password reset instructions have been sent."));
    }

    @Test
    void requestReset_InvalidEmail_ReturnsBadRequest() throws Exception {
        PasswordResetRequestDto request = new PasswordResetRequestDto("not-an-email");

        mockMvc.perform(post("/api/v1/auth/password/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmReset_ValidRequest_ReturnsOk() throws Exception {
        PasswordResetConfirmDto request = new PasswordResetConfirmDto("valid-token", "StrongPass123!");

        doNothing().when(passwordResetService).confirmReset(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully. Please log in."));
    }

    @Test
    void confirmReset_WeakPassword_ReturnsBadRequest() throws Exception {
        PasswordResetConfirmDto request = new PasswordResetConfirmDto("valid-token", "weakpass");

        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmReset_InvalidToken_ReturnsUnauthorized() throws Exception {
        PasswordResetConfirmDto request = new PasswordResetConfirmDto("invalid-token", "StrongPass123!");

        doThrow(new InvalidTokenException("Invalid or expired token."))
                .when(passwordResetService).confirmReset(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token."));
    }
}


