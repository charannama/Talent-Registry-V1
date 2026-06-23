package com.zencube.registry.auth.service.impl;

import com.zencube.registry.auth.dto.AuthResponse;
import com.zencube.registry.auth.dto.OAuth2ExchangeRequest;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.auth.service.OAuth2TokenExchangeService;
import com.zencube.registry.profile.service.ProfileSyncService;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.common.exception.ForbiddenException;
import com.zencube.registry.security.model.CustomUserDetails;
import com.zencube.registry.security.service.JwtService;
import com.zencube.registry.session.entity.Session;
import com.zencube.registry.session.repository.SessionRepository;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2TokenExchangeServiceImpl implements OAuth2TokenExchangeService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final SessionRepository sessionRepository;
    private final ProfileSyncService profileSyncService;

    @Value("${app.jwt.access-token-expiry-seconds:7200}")
    private long accessTokenExpirySeconds;

    @Value("${app.jwt.refresh-token-expiry-seconds:604800}")
    private long refreshTokenExpirySeconds;

    @Override
    @Transactional
    public AuthResponse exchangeToken(OAuth2ExchangeRequest request, HttpServletRequest httpRequest) {
        log.info("Starting OAuth2 Token Exchange with PKCE code verifier");

        // 1. Call ZenCube OAuth2 Server to exchange code for tokens (Mocked for now)
        // Expected request: grant_type=authorization_code, code, code_verifier, redirect_uri, client_id
        Map<String, Object> tokenResponse = mockTokenExchange(request.code(), request.codeVerifier());

        // 2. Call UserInfo Endpoint using the received Access Token (Mocked)
        Map<String, Object> userInfo = mockUserInfo(tokenResponse.get("access_token").toString());

        String email = (String) userInfo.get("email");
        String providerId = (String) userInfo.get("sub");
        String name = (String) userInfo.get("name");

        if (email == null) {
            throw new IllegalArgumentException("Email not provided by ZenCube SSO");
        }

        // 3. User Lookup & Creation
        Optional<User> userOptional = userRepository.findByEmailAndDeletedFalse(email);
        User user;
        boolean isNewUser = false;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            
            // Check restricted roles (ADMIN, HR, ENTERPRISE are not allowed)
            checkRestrictedRoles(user);

            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                user.setAuthProvider(AuthProvider.ZENCUBE_SSO);
                user.setProviderId(providerId);
                user.setEmailVerified(true);
            }
        } else {
            // Auto-create user
            user = User.builder()
                    .email(email)
                    .firstName(name != null ? name.split(" ")[0] : "")
                    .lastName(name != null && name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "")
                    .authProvider(AuthProvider.ZENCUBE_SSO)
                    .providerId(providerId)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .failedLoginAttempts(0)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;

            // Auto-assign STUDENT role
            Role studentRole = roleRepository.findByNameAndDeletedFalse(RoleType.STUDENT.name())
                    .orElseThrow(() -> new IllegalStateException("STUDENT role not found"));
            
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(studentRole);
            userRoleRepository.save(userRole);
        }

        // Reset tracking fields on successful login
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // 4. Generate Tokens
        List<UserRole> userRoles = userRoleRepository.findByUserAndDeletedFalse(user);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (UserRole ur : userRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName()));
        }

        CustomUserDetails userDetails = new CustomUserDetails(user, authorities);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // 5. Create Session
        createSession(user, refreshToken, httpRequest);

    // 6. Trigger Profile Sync
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        CompletableFuture.runAsync(() -> {
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                profileSyncService.syncProfile();
            } finally {
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            }
        });

        return AuthResponse.of(
                accessToken, 
                refreshToken, 
                accessTokenExpirySeconds,
                user.getId(), 
                user.getEmail(), 
                user.getDisplayName()
        );
    }

    private void checkRestrictedRoles(User user) {
        List<UserRole> roles = userRoleRepository.findByUserAndDeletedFalse(user);
        for (UserRole userRole : roles) {
            String roleName = userRole.getRole().getName();
            if ("ADMIN".equalsIgnoreCase(roleName) || 
                "HR".equalsIgnoreCase(roleName) || 
                "ENTERPRISE".equalsIgnoreCase(roleName)) {
                throw new ForbiddenException("OAuth2 login is restricted to students only");
            }
        }
    }

    private void createSession(User user, String refreshToken, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        Session session = Session.builder()
                .user(user)
                .refreshTokenHash(hashToken(refreshToken))
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpirySeconds))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        sessionRepository.save(session);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    // --- Mocks for External ZenCube API Calls ---
    
    private Map<String, Object> mockTokenExchange(String code, String codeVerifier) {
        log.info("Mocking ZenCube Token Exchange API call (grant_type=authorization_code, code={}, pkce={})", code, codeVerifier != null);
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", "mocked_zencube_access_token");
        return response;
    }

    private Map<String, Object> mockUserInfo(String accessToken) {
        log.info("Mocking ZenCube UserInfo API call");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", "student@zencube.edu");
        userInfo.put("sub", "zen_123456");
        userInfo.put("name", "Alice Student");
        userInfo.put("studentId", "STU-999");
        userInfo.put("program", "Computer Science");
        userInfo.put("branch", "Engineering");
        userInfo.put("graduationYear", "2027");
        return userInfo;
    }
}
