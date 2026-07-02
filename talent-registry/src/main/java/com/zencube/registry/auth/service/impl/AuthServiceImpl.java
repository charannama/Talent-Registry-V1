package com.zencube.registry.auth.service.impl;

import com.zencube.registry.auth.dto.*;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.mapper.AuthMapper;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.auth.service.AuthService;
import com.zencube.registry.auth.verification.entity.EmailVerificationToken;
import com.zencube.registry.auth.verification.repository.EmailVerificationTokenRepository;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.common.exception.InvalidTokenException;
import com.zencube.registry.common.exception.ForbiddenException;
import com.zencube.registry.common.exception.UnauthorizedException;
import com.zencube.registry.common.exception.AccountLockedException;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.security.service.JwtService;
import com.zencube.registry.session.service.SessionService;
import com.zencube.registry.session.entity.Session;
import com.zencube.registry.session.repository.SessionRepository;
import com.zencube.registry.security.model.CustomUserDetails;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.zencube.registry.scheduler.service.TaskSchedulerService taskSchedulerService;
    private final SecurityProperties securityProperties;
    private final org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    @Value("${app.jwt.access-token-expiry-seconds:7200}")
    private long accessTokenExpirySeconds;

    @Value("${app.jwt.refresh-token-expiry-seconds:604800}")
    private long refreshTokenExpirySeconds;

    @Value("${app.registration.allow-duplicate-companies:false}")
    private boolean allowDuplicateCompanies;

    // ------------------------------------------------------------------
    // Register
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.email());

        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new com.zencube.registry.common.exception.ConflictException(
                    "An account with email '%s' already exists.".formatted(request.email()));
        }

        User user = authMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        // Assign STUDENT role
        Role studentRole = roleRepository.findByRoleTypeAndDeletedFalse(RoleType.STUDENT)
            .orElseThrow(() -> new IllegalStateException("STUDENT role not found"));
        UserRole userRole = new UserRole(user, studentRole);
        userRole = userRoleRepository.save(userRole);
        user.getUserRoles().add(userRole);

        log.info("User registered successfully with id={}", user.getId());

        String token = java.util.UUID.randomUUID().toString();
        String tokenHash = hashToken(token);
        com.zencube.registry.auth.verification.entity.EmailVerificationToken verificationToken = com.zencube.registry.auth.verification.entity.EmailVerificationToken.builder()
                .token(tokenHash)
                .user(user)
                .expiresAt(java.time.Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        taskSchedulerService.enqueueTask(
                com.zencube.registry.scheduler.dto.TaskPayload.builder()
                        .taskType("EMAIL_DELIVERY")
                        .data(java.util.Map.of(
                                "recipientId", user.getId().toString(),
                                "eventType", com.zencube.registry.notification.enums.NotificationEventType.EMAIL_VERIFIED.name(),
                                "verificationLink", "http://localhost:3000/verify-email?token=" + token,
                                "userName", user.getFirstName()
                        ))
                        .build()
        );

        applicationEventPublisher.publishEvent(
            com.zencube.registry.notification.event.NotificationEvent.builder()
                .eventType(com.zencube.registry.notification.enums.NotificationEventType.USER_REGISTERED)
                .recipientId(user.getId())
                .resourceType("User")
                .resourceId(user.getId())
                .title("Welcome to ZenCube!")
                .message("Your account has been registered successfully.")
                .build()
        );

        return authMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public RegistrationResponse registerHr(RegisterRequest request) {
        log.info("Registering new HR staff: {}", request.email());

        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new com.zencube.registry.common.exception.ConflictException(
                    "An account with email '%s' already exists.".formatted(request.email()));
        }

        User user = authMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user = userRepository.save(user);

        // Assign HR_STAFF role
        Role hrRole = roleRepository.findByRoleTypeAndDeletedFalse(RoleType.HR_STAFF)
            .orElseThrow(() -> new IllegalStateException("HR_STAFF role not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(hrRole);
        userRoleRepository.save(userRole);

        // Generate verification token (24 hours expiry)
        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        taskSchedulerService.enqueueTask(
                com.zencube.registry.scheduler.dto.TaskPayload.builder()
                        .taskType("EMAIL_DELIVERY")
                        .data(java.util.Map.of(
                                "recipientId", user.getId().toString(),
                                "eventType", com.zencube.registry.notification.enums.NotificationEventType.EMAIL_VERIFIED.name(),
                                "verificationLink", "http://localhost:3000/verify-email?token=" + token,
                                "userName", user.getFirstName()
                        ))
                        .build()
        );

        log.info("HR staff registered successfully with id={}", user.getId());
        return new RegistrationResponse(true, "Registration successful. Please verify your email.");
    }

    @Override
    @Transactional
    public RegistrationResponse registerEnterprise(EnterpriseRegisterRequest request) {
        log.info("Registering new enterprise recruiter: {}", request.email());

        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new ConflictException(
                    "An account with email '%s' already exists.".formatted(request.email()));
        }

        String trimmedCompany = request.companyName().trim();
        if (!allowDuplicateCompanies && userRepository.existsByCompanyNameIgnoreCaseAndDeletedFalse(trimmedCompany)) {
            throw new ConflictException(
                    "Company name '%s' is already registered.".formatted(trimmedCompany));
        }

        Role recruiterRole = roleRepository.findByRoleTypeAndDeletedFalse(RoleType.ENTERPRISE_RECRUITER)
                .orElseThrow(() -> new IllegalStateException("ENTERPRISE_RECRUITER role not found in system"));

        User user = User.builder()
                .email(request.email().toLowerCase().trim())
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .companyName(trimmedCompany)
                .passwordHash(passwordEncoder.encode(request.password()))
                .authProvider(AuthProvider.NATIVE)
                .emailVerified(false)
                .status(UserStatus.ACTIVE)
                .build();
        user = userRepository.save(user);

        if (!userRoleRepository.existsByUserAndRoleAndDeletedFalse(user, recruiterRole)) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(recruiterRole);
            userRoleRepository.save(userRole);
        }

        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        taskSchedulerService.enqueueTask(
                com.zencube.registry.scheduler.dto.TaskPayload.builder()
                        .taskType("EMAIL_DELIVERY")
                        .data(java.util.Map.of(
                                "recipientId", user.getId().toString(),
                                "eventType", com.zencube.registry.notification.enums.NotificationEventType.EMAIL_VERIFIED.name(),
                                "verificationLink", "http://localhost:3000/verify-email?token=" + token,
                                "userName", user.getFirstName()
                        ))
                        .build()
        );

        log.info("Enterprise recruiter registered successfully with id={}", user.getId());
        return new RegistrationResponse(true, "Registration successful. Please verify your email before logging in.");
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmailAndDeletedFalse(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        unlockIfExpired(user);

        if (isLocked(user)) {
            throw new AccountLockedException("Account locked due to multiple failed login attempts. Try again after " + user.getLockoutUntil() + ".");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordFailedLogin(user);
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email verification required");
        }

        if (!user.isActive()) {
            throw new ForbiddenException("Account is not active.");
        }

        recordSuccessfulLogin(user);

        CustomUserDetails userDetails = buildUserDetails(user);
        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(userDetails, jti);
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefresh = hashToken(rawRefreshToken);
        sessionService.createAndSaveSession(user, httpRequest, jti, hashedRefresh, refreshTokenExpirySeconds * 1000);

        log.info("Login successful for userId={}", user.getId());
        return AuthResponse.of(
                accessToken, rawRefreshToken, accessTokenExpirySeconds,
                user.getId(), user.getEmail(), user.getDisplayName());
    }

    // ------------------------------------------------------------------
    // Refresh Token
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String hash = hashToken(request.refreshToken());
        
        Session stored = sessionRepository.findByRefreshTokenHashAndDeletedFalse(hash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or revoked."));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token is expired or revoked.");
        }

        User user = stored.getUser();

        if (user.isDeleted() || !user.isActive()) {
            throw new InvalidTokenException("User account is inactive or deleted.");
        }
        
        unlockIfExpired(user);
        if (isLocked(user)) {
            throw new AccountLockedException("Account locked due to multiple failed login attempts. Try again after " + user.getLockoutUntil() + ".");
        }

        // Rotate: revoke old session
        stored.setRevokedAt(Instant.now());
        sessionRepository.save(stored);

        // Issue new token pair
        CustomUserDetails userDetails = buildUserDetails(user);
        String jti = UUID.randomUUID().toString();
        String newAccess = jwtService.generateAccessToken(userDetails, jti);
        String newRefresh = UUID.randomUUID().toString();
        String hashedNewRefresh = hashToken(newRefresh);
        sessionService.createAndSaveSession(user, httpRequest, jti, hashedNewRefresh, refreshTokenExpirySeconds * 1000);

        return AuthResponse.of(
                newAccess, newRefresh, accessTokenExpirySeconds,
                user.getId(), user.getEmail(), user.getDisplayName());
    }

    // ------------------------------------------------------------------
    // Logout
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        sessionService.logoutAllSessions(user);
    }

    // ------------------------------------------------------------------
    // Email Verification & Password Reset
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void verifyEmail(String token) {
        String hashedToken = hashToken(token);
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByTokenAndDeletedFalse(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token."));

        if (!verificationToken.isValid()) {
            throw new InvalidTokenException("Invalid or expired token.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        verificationToken.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmailAndDeletedFalse(normalizedEmail);

        if (userOpt.isEmpty() || userOpt.get().isEmailVerified()) {
            log.info("Resend verification skipped: user not found or already verified for email={}", normalizedEmail);
            return;
        }

        User user = userOpt.get();
        long count = emailVerificationTokenRepository.countByUserAndCreatedAtAfter(user, Instant.now().minus(1, ChronoUnit.HOURS));

        if (count >= 3) {
            log.warn("Resend verification rate limit exceeded for user id={}", user.getId());
            return;
        }

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(hashedToken)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        taskSchedulerService.enqueueTask(
                com.zencube.registry.scheduler.dto.TaskPayload.builder()
                        .taskType("EMAIL_DELIVERY")
                        .data(java.util.Map.of(
                                "recipientId", user.getId().toString(),
                                "eventType", com.zencube.registry.notification.enums.NotificationEventType.EMAIL_VERIFIED.name(),
                                "verificationLink", "http://localhost:3000/verify-email?token=" + rawToken,
                                "userName", user.getFirstName()
                        ))
                        .build()
        );
        log.info("Resent verification email to user id={}", user.getId());
    }


    // ------------------------------------------------------------------
    // Current User Profile
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public MeResponse getMe(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            String auth = authority.getAuthority();
            if (auth.startsWith("ROLE_")) {
                roles.add(auth.substring(5)); // Remove ROLE_ prefix
            } else {
                permissions.add(auth);
            }
        }
        
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getAuthProvider().name(),
                user.isEmailVerified(),
                user.getTimezone(),
                roles,
                permissions,
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void unlockIfExpired(User user) {
        if (user.getLockoutUntil() != null && user.getLockoutUntil().isBefore(Instant.now())) {
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            userRepository.save(user);
        }
    }

    private boolean isLocked(User user) {
        return user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(Instant.now());
    }

    private void recordFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= securityProperties.getLockout().getMaxAttempts()) {
            user.setLockoutUntil(Instant.now().plus(securityProperties.getLockout().getDurationMinutes(), ChronoUnit.MINUTES));
            log.warn("Account locked for user id={}", user.getId());
        }
        userRepository.save(user);
    }

    private void recordSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    private CustomUserDetails buildUserDetails(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserAndDeletedFalse(user);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (UserRole ur : userRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName()));
        }
        return new CustomUserDetails(user, authorities);
    }



    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
