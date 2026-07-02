package com.zencube.registry.passwordreset.service;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.InvalidTokenException;
import com.zencube.registry.passwordreset.entity.PasswordResetToken;
import com.zencube.registry.passwordreset.repository.PasswordResetTokenRepository;
import com.zencube.registry.passwordreset.service.impl.PasswordResetServiceImpl;
import com.zencube.registry.session.entity.Session;
import com.zencube.registry.session.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private com.zencube.registry.scheduler.service.TaskSchedulerService taskSchedulerService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = User.builder()
                .email("test@example.com")
                .firstName("John")
                .passwordHash("old_hash")
                .failedLoginAttempts(3)
                .lockoutUntil(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        savedUser.setId(java.util.UUID.randomUUID());
    }

    @Test
    void requestReset_Success() {
        when(userRepository.findByEmailAndDeletedFalse("test@example.com"))
                .thenReturn(Optional.of(savedUser));

        passwordResetService.requestReset("test@example.com");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(taskSchedulerService).enqueueTask(any());
    }

    @Test
    void requestReset_UserNotFound_SilentlyReturns() {
        when(userRepository.findByEmailAndDeletedFalse("unknown@example.com"))
                .thenReturn(Optional.empty());

        passwordResetService.requestReset("unknown@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(taskSchedulerService, never()).enqueueTask(any());
    }

    @Test
    void confirmReset_Success() {
        String newPassword = "NewPassword123!";
        PasswordResetToken token = PasswordResetToken.builder()
                .user(savedUser)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        Session activeSession = new Session();
        activeSession.setRevokedAt(null);

        when(passwordResetTokenRepository.findByTokenHashAndDeletedFalse(anyString()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode(newPassword)).thenReturn("new_hash");
        when(sessionRepository.findActiveSessions(savedUser)).thenReturn(List.of(activeSession));

        passwordResetService.confirmReset("raw_token", newPassword);

        assertEquals("new_hash", savedUser.getPasswordHash());
        assertNotNull(savedUser.getPasswordChangedAt());
        assertEquals(0, savedUser.getFailedLoginAttempts());
        assertNull(savedUser.getLockoutUntil());
        assertNotNull(token.getUsedAt());
        assertNotNull(activeSession.getRevokedAt());

        verify(userRepository).save(savedUser);
        verify(passwordResetTokenRepository).save(token);
        verify(sessionRepository).save(activeSession);
    }

    @Test
    void confirmReset_ExpiredToken_ThrowsException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(savedUser)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(passwordResetTokenRepository.findByTokenHashAndDeletedFalse(anyString()))
                .thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> passwordResetService.confirmReset("raw_token", "pass"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmReset_UsedToken_ThrowsException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(savedUser)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .usedAt(Instant.now().minusSeconds(10))
                .build();

        when(passwordResetTokenRepository.findByTokenHashAndDeletedFalse(anyString()))
                .thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> passwordResetService.confirmReset("raw_token", "pass"));
        verify(userRepository, never()).save(any());
    }
}
