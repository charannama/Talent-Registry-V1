package com.zencube.registry.auth.service;

import com.zencube.registry.auth.dto.LoginRequest;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.auth.service.impl.AuthServiceImpl;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.common.exception.AccountLockedException;
import com.zencube.registry.config.SecurityProperties;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.security.service.JwtService;
import com.zencube.registry.session.repository.SessionRepository;
import com.zencube.registry.session.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountLockoutTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private SecurityProperties securityProperties;
    @Mock private SessionService sessionService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private SecurityProperties.Lockout lockoutProps;
    @Mock private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        lockoutProps = new SecurityProperties.Lockout();
        lockoutProps.setMaxAttempts(5);
        lockoutProps.setDurationMinutes(30);

        user = User.builder()
                .email("test@example.com")
                .passwordHash("hashed")
                .emailVerified(true)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void login_InvalidPassword_IncrementsFailedAttempts() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpass");

        when(userRepository.findByEmailAndDeletedFalse("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);
        when(securityProperties.getLockout()).thenReturn(lockoutProps);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, request));

        assertEquals(1, user.getFailedLoginAttempts());
        assertNull(user.getLockoutUntil());
        verify(userRepository).save(user);
    }

    @Test
    void login_FifthFailedAttempt_LocksAccount() {
        user.setFailedLoginAttempts(4);
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpass");

        when(userRepository.findByEmailAndDeletedFalse("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);
        when(securityProperties.getLockout()).thenReturn(lockoutProps);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, request));

        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockoutUntil());
        verify(userRepository).save(user);
    }

    @Test
    void login_LockedAccount_ThrowsAccountLockedException() {
        user.setLockoutUntil(Instant.now().plus(10, ChronoUnit.MINUTES));
        LoginRequest loginRequest = new LoginRequest("test@example.com", "anypass");

        when(userRepository.findByEmailAndDeletedFalse("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class, () -> authService.login(loginRequest, request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_LockoutExpired_UnlocksAccount() {
        user.setLockoutUntil(Instant.now().minus(5, ChronoUnit.MINUTES));
        user.setFailedLoginAttempts(5);
        LoginRequest loginRequest = new LoginRequest("test@example.com", "correctpass");

        when(userRepository.findByEmailAndDeletedFalse("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctpass", "hashed")).thenReturn(true);
        when(userRoleRepository.findByUserAndDeletedFalse(user)).thenReturn(java.util.Collections.emptyList());

        authService.login(loginRequest, request);

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockoutUntil());
        assertNotNull(user.getLastLoginAt());
    }
}
