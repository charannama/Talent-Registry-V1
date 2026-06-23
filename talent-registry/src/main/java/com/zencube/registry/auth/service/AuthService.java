package com.zencube.registry.auth.service;

import com.zencube.registry.auth.dto.*;

/**
 * Contract for all authentication operations.
 *
 * <p>The implementation lives in
 * {@link com.zencube.registry.auth.service.impl.AuthServiceImpl}.
 */
public interface AuthService {

    /**
     * Registers a new user account.
     * Sends an email verification link after successful registration.
     *
     * @param request registration payload (name, email, password)
     * @return the persisted user as a public-safe DTO
     * @throws com.zencube.registry.common.exception.ConflictException if the email is already in use
     */
    UserResponse register(RegisterRequest request);

    /**
     * Registers a new HR Staff account.
     *
     * @param request registration payload
     * @return a success response indicating email verification is required
     */
    RegistrationResponse registerHr(RegisterRequest request);

    /**
     * Registers a new Enterprise Recruiter account.
     *
     * @param request registration payload (contains companyName)
     * @return a success response indicating email verification is required
     */
    RegistrationResponse registerEnterprise(EnterpriseRegisterRequest request);

    /**
     * Authenticates a user with email + password.
     *
     * @param request login credentials
     * @param httpRequest the HTTP request for capturing IP and User-Agent
     * @return a pair of access + refresh tokens alongside basic user info
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         if credentials are invalid
     * @throws com.zencube.registry.common.exception.AccountNotActiveException
     *         if the account is suspended or not yet verified
     */
    AuthResponse login(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest);

    /**
     * Exchanges a valid refresh token for a new access token (with rotation).
     * The old refresh token is revoked and a new one is issued.
     *
     * @param request the refresh-token payload
     * @param httpRequest the HTTP request for capturing IP and User-Agent
     * @return new tokens
     * @throws com.zencube.registry.common.exception.InvalidTokenException
     *         if the token is expired, revoked, or not found
     */
    AuthResponse refreshToken(TokenRefreshRequest request, jakarta.servlet.http.HttpServletRequest httpRequest);

    /**
     * Revokes all active refresh tokens for the calling user (full logout).
     *
     * @param userId the UUID of the user to log out
     */
    void logout(java.util.UUID userId);

    /**
     * Verifies a user's email address using the one-time token sent by email.
     *
     * @param token the raw email-verification token
     */
    void verifyEmail(String token);

    /**
     * Resends a verification email if the user is registered but not yet verified,
     * subject to rate limiting (max 3 per hour). Returns silently if not eligible.
     *
     * @param email the user's email address
     */
    void resendVerification(String email);

    /**
     * Retrieves the current authenticated user's profile and permissions.
     *
     * @param userDetails the current authenticated user details
     * @return the user's profile and permissions
     */
    MeResponse getMe(com.zencube.registry.security.model.CustomUserDetails userDetails);

}
