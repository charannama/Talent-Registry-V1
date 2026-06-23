package com.zencube.registry.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.dto.ResendVerificationRequest;
import com.zencube.registry.auth.dto.VerifyEmailRequest;
import com.zencube.registry.auth.service.AuthService;
import com.zencube.registry.common.exception.InvalidTokenException;
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
class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.zencube.registry.auth.service.OAuth2TokenExchangeService oauth2ExchangeService;

    @MockBean
    private com.zencube.registry.passwordreset.service.PasswordResetService passwordResetService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void verifyEmail_Success() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("valid-token");

        doNothing().when(authService).verifyEmail(anyString());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified. You can now log in."));
    }

    @Test
    void verifyEmail_InvalidToken_ReturnsUnauthorized() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("invalid-token");

        doThrow(new InvalidTokenException("Invalid or expired token."))
                .when(authService).verifyEmail(anyString());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token."));
    }

    @Test
    void resendVerification_Success() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest("test@example.com");

        doNothing().when(authService).resendVerification(anyString());

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email is registered and not verified, a new verification link has been sent."));
    }

    @Test
    void resendVerification_ValidationFailure_InvalidEmail() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest("not-an-email");

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}


