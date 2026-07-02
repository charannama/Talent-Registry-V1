package com.zencube.registry.auth.service;

import com.zencube.registry.auth.dto.EnterpriseRegisterRequest;
import com.zencube.registry.auth.dto.RegistrationResponse;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.auth.service.impl.AuthServiceImpl;
import com.zencube.registry.auth.verification.repository.EmailVerificationTokenRepository;
import com.zencube.registry.auth.verification.entity.EmailVerificationToken;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.zencube.registry.scheduler.service.TaskSchedulerService taskSchedulerService;

    @InjectMocks
    private AuthServiceImpl authService;

    private EnterpriseRegisterRequest request;
    private Role recruiterRole;
    private User savedUser;

    @BeforeEach
    void setUp() {
        request = new EnterpriseRegisterRequest(
                "Jane",
                "Doe",
                "jane.doe@enterprise.com",
                "Secure@123",
                "ZenCube"
        );

        recruiterRole = Role.builder()
                .name(RoleType.ENTERPRISE_RECRUITER.name())
                .roleType(RoleType.ENTERPRISE_RECRUITER)
                .build();
        recruiterRole.setId(UUID.randomUUID());

        savedUser = User.builder()
                .email(request.email().toLowerCase().trim())
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .companyName(request.companyName().trim())
                .passwordHash("hashed-password")
                .authProvider(AuthProvider.NATIVE)
                .emailVerified(false)
                .status(UserStatus.ACTIVE)
                .build();
        savedUser.setId(UUID.randomUUID());

        ReflectionTestUtils.setField(authService, "allowDuplicateCompanies", false);
    }

    @Test
    void registerEnterprise_Success() {
        when(userRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(false);
        when(userRepository.existsByCompanyNameIgnoreCaseAndDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.findByNameAndDeletedFalse(RoleType.ENTERPRISE_RECRUITER.name())).thenReturn(Optional.of(recruiterRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRoleRepository.existsByUserAndRoleAndDeletedFalse(any(User.class), any(Role.class))).thenReturn(false);

        RegistrationResponse response = authService.registerEnterprise(request);

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("Registration successful. Please verify your email before logging in.", response.message());

        verify(userRepository).existsByEmailAndDeletedFalse(request.email());
        verify(userRepository).existsByCompanyNameIgnoreCaseAndDeletedFalse("ZenCube");
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).save(any());
        verify(emailVerificationTokenRepository).save(any());
        verify(taskSchedulerService).enqueueTask(any());
    }

    @Test
    void registerEnterprise_DuplicateEmail_ThrowsConflictException() {
        when(userRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registerEnterprise(request));

        verify(userRepository, never()).save(any(User.class));
        verify(taskSchedulerService, never()).enqueueTask(any());
    }

    @Test
    void registerEnterprise_DuplicateCompany_ThrowsConflictException() {
        when(userRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(false);
        when(userRepository.existsByCompanyNameIgnoreCaseAndDeletedFalse(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registerEnterprise(request));

        verify(userRepository, never()).save(any(User.class));
        verify(taskSchedulerService, never()).enqueueTask(any());
    }

    @Test
    void registerEnterprise_DuplicateCompanyAllowed_Success() {
        ReflectionTestUtils.setField(authService, "allowDuplicateCompanies", true);

        when(userRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.findByNameAndDeletedFalse(RoleType.ENTERPRISE_RECRUITER.name())).thenReturn(Optional.of(recruiterRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRoleRepository.existsByUserAndRoleAndDeletedFalse(any(User.class), any(Role.class))).thenReturn(false);

        RegistrationResponse response = authService.registerEnterprise(request);

        assertNotNull(response);
        assertTrue(response.success());

        verify(userRepository, never()).existsByCompanyNameIgnoreCaseAndDeletedFalse(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerEnterprise_MissingRole_ThrowsBusinessException() {
        when(userRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(false);
        when(userRepository.existsByCompanyNameIgnoreCaseAndDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.findByNameAndDeletedFalse(RoleType.ENTERPRISE_RECRUITER.name())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.registerEnterprise(request));

        verify(userRepository, never()).save(any(User.class));
        verify(taskSchedulerService, never()).enqueueTask(any());
    }

    @Test
    void verifyEmail_Success() {
        String rawToken = "my-token";
        EmailVerificationToken tokenEntity = EmailVerificationToken.builder()
                .token("hashed")
                .user(savedUser)
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
                
        when(emailVerificationTokenRepository.findByTokenAndDeletedFalse(anyString()))
                .thenReturn(Optional.of(tokenEntity));

        authService.verifyEmail(rawToken);

        assertTrue(savedUser.isEmailVerified());
        assertNotNull(savedUser.getEmailVerifiedAt());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertNotNull(tokenEntity.getUsedAt());

        verify(userRepository).save(savedUser);
        verify(emailVerificationTokenRepository).save(tokenEntity);
    }

    @Test
    void verifyEmail_InvalidToken_ThrowsException() {
        when(emailVerificationTokenRepository.findByTokenAndDeletedFalse(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(com.zencube.registry.common.exception.InvalidTokenException.class, () -> authService.verifyEmail("token"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendVerification_Success() {
        savedUser.setEmailVerified(false);
        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.of(savedUser));
        when(emailVerificationTokenRepository.countByUserAndCreatedAtAfter(eq(savedUser), any(Instant.class)))
                .thenReturn(0L);

        authService.resendVerification(savedUser.getEmail());

        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(taskSchedulerService).enqueueTask(any());
    }

    @Test
    void resendVerification_RateLimitExceeded_SilentlyReturns() {
        savedUser.setEmailVerified(false);
        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.of(savedUser));
        when(emailVerificationTokenRepository.countByUserAndCreatedAtAfter(eq(savedUser), any(Instant.class)))
                .thenReturn(3L);

        authService.resendVerification(savedUser.getEmail());

        verify(emailVerificationTokenRepository, never()).save(any());
        verify(taskSchedulerService, never()).enqueueTask(any());
    }

    @Test
    void resendVerification_UserNotFound_SilentlyReturns() {
        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.empty());

        authService.resendVerification("nonexistent@example.com");

        verify(emailVerificationTokenRepository, never()).save(any());
        verify(taskSchedulerService, never()).enqueueTask(any());
    }
}
