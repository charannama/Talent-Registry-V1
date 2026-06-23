package com.zencube.registry.session.service;

import com.zencube.registry.auth.dto.response.LogoutAllResponse;
import com.zencube.registry.auth.dto.response.SessionResponse;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.common.exception.InvalidSessionException;
import com.zencube.registry.common.exception.SessionExpiredException;
import com.zencube.registry.common.exception.SessionRevokedException;
import com.zencube.registry.session.entity.Session;
import com.zencube.registry.session.repository.SessionRepository;
import com.zencube.registry.session.service.impl.SessionServiceImpl;
import com.zencube.registry.userrole.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private User user;
    private Session session;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .build();
        user.setId(UUID.randomUUID());

        session = Session.builder()
                .user(user)
                .accessTokenJti("jti-123")
                .refreshTokenHash("hashed-refresh")
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla")
                .build();
        session.setId(UUID.randomUUID());
        
        request = new MockHttpServletRequest();
    }

    @Test
    void createAndSaveSession_Success() {
        request.addHeader("X-Forwarded-For", "203.0.113.10");
        request.addHeader("User-Agent", "Chrome");

        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        Session saved = sessionService.createAndSaveSession(user, request, "jti-123", "raw-token", 3600000L);

        assertNotNull(saved);
        verify(sessionRepository).save(argThat(s -> 
            s.getIpAddress().equals("203.0.113.10") && 
            s.getUserAgent().equals("Chrome") &&
            s.getAccessTokenJti().equals("jti-123")
        ));
    }

    @Test
    void validateCurrentSession_Valid() {
        Role role = new Role();
        role.setName("USER");
        UserRole userRole = new UserRole();
        userRole.setRole(role);

        when(sessionRepository.findByAccessTokenJtiAndDeletedFalse("jti-123")).thenReturn(Optional.of(session));
        when(userRoleRepository.findByUserAndDeletedFalse(user)).thenReturn(List.of(userRole));

        SessionResponse response = sessionService.validateCurrentSession("jti-123");

        assertEquals(user.getId(), response.userId());
        assertEquals("127.0.0.1", response.ipAddress());
        assertTrue(response.roles().contains("USER"));
    }

    @Test
    void validateCurrentSession_Revoked_ThrowsException() {
        session.setRevokedAt(Instant.now());
        when(sessionRepository.findByAccessTokenJtiAndDeletedFalse("jti-123")).thenReturn(Optional.of(session));

        assertThrows(SessionRevokedException.class, () -> sessionService.validateCurrentSession("jti-123"));
    }

    @Test
    void validateCurrentSession_Expired_ThrowsException() {
        session.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(sessionRepository.findByAccessTokenJtiAndDeletedFalse("jti-123")).thenReturn(Optional.of(session));

        assertThrows(SessionExpiredException.class, () -> sessionService.validateCurrentSession("jti-123"));
    }

    @Test
    void validateCurrentSession_NotFound_ThrowsException() {
        when(sessionRepository.findByAccessTokenJtiAndDeletedFalse("invalid")).thenReturn(Optional.empty());

        assertThrows(InvalidSessionException.class, () -> sessionService.validateCurrentSession("invalid"));
    }

    @Test
    void logoutCurrentSession_Success() {
        when(sessionRepository.findByAccessTokenJtiAndDeletedFalse("jti-123")).thenReturn(Optional.of(session));

        sessionService.logoutCurrentSession("jti-123");

        assertNotNull(session.getRevokedAt());
        verify(sessionRepository).save(session);
    }

    @Test
    void logoutAllSessions_Success() {
        Session session2 = Session.builder()
                .user(user)
                .accessTokenJti("jti-456")
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        when(sessionRepository.findActiveSessions(user)).thenReturn(List.of(session, session2));

        LogoutAllResponse response = sessionService.logoutAllSessions(user);

        assertTrue(response.success());
        assertEquals(2, response.revokedSessions());
        assertNotNull(session.getRevokedAt());
        assertNotNull(session2.getRevokedAt());
        verify(sessionRepository).saveAll(anyList());
    }
}
