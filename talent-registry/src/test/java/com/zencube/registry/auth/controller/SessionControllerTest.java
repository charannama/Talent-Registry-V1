package com.zencube.registry.auth.controller;

import com.zencube.registry.auth.dto.response.LogoutAllResponse;
import com.zencube.registry.auth.dto.response.SessionResponse;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.security.model.CustomUserDetails;
import com.zencube.registry.security.service.JwtService;
import com.zencube.registry.session.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller testing
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.zencube.registry.auth.service.AuthService authService;
    
    @MockBean
    private com.zencube.registry.auth.service.OAuth2TokenExchangeService oauth2ExchangeService;
    
    @MockBean
    private com.zencube.registry.passwordreset.service.PasswordResetService passwordResetService;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private User testUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        userDetails = new CustomUserDetails(testUser, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getSession_Success() throws Exception {
        SessionResponse sessionResponse = new SessionResponse(
                testUser.getId(),
                "test@example.com",
                List.of("USER"),
                "session-123",
                "127.0.0.1",
                "Chrome",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        when(jwtService.extractJti(anyString())).thenReturn("jti-123");
        when(sessionService.validateCurrentSession("jti-123")).thenReturn(sessionResponse);

        mockMvc.perform(get("/api/v1/auth/session")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session details retrieved"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void logoutCurrentSession_Success() throws Exception {
        when(jwtService.extractJti(anyString())).thenReturn("jti-123");
        doNothing().when(sessionService).logoutCurrentSession("jti-123");

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Current session logged out successfully"));

        verify(sessionService).logoutCurrentSession("jti-123");
    }

    @Test
    void logoutAll_Success() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
        try {
            when(sessionService.logoutAllSessions(any(User.class)))
                    .thenReturn(new LogoutAllResponse(true, 3));

        mockMvc.perform(post("/api/v1/auth/logout/all")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.revokedSessions").value(3));

        verify(sessionService).logoutAllSessions(testUser);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
