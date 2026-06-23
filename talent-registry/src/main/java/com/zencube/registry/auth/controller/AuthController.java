package com.zencube.registry.auth.controller;

import com.zencube.registry.auth.dto.*;
import com.zencube.registry.auth.dto.response.SessionResponse;
import com.zencube.registry.auth.dto.response.LogoutAllResponse;
import com.zencube.registry.auth.dto.request.PasswordResetConfirmDto;
import com.zencube.registry.auth.dto.request.PasswordResetRequestDto;
import com.zencube.registry.auth.service.AuthService;
import com.zencube.registry.passwordreset.service.PasswordResetService;
import com.zencube.registry.session.service.SessionService;
import com.zencube.registry.security.service.JwtService;
import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.common.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller exposing all authentication endpoints.
 *
 * <p>Base path: {@code /api/v1/auth}
 *
 * <table border="1">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/register</td><td>Create a new account</td></tr>
 *   <tr><td>POST</td><td>/login</td><td>Authenticate and receive tokens</td></tr>
 *   <tr><td>POST</td><td>/refresh</td><td>Rotate refresh token</td></tr>
 *   <tr><td>POST</td><td>/logout</td><td>Revoke all sessions</td></tr>
 *   <tr><td>GET</td><td>/verify-email</td><td>Confirm email address</td></tr>
 *   <tr><td>POST</td><td>/forgot-password</td><td>Send reset link</td></tr>
 *   <tr><td>POST</td><td>/reset-password</td><td>Set a new password</td></tr>
 * </table>
 */
@Validated
@RestController
@RequestMapping(Constants.AUTH_BASE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, token management, and password operations")
public class AuthController {

    private final AuthService authService;
    private final com.zencube.registry.auth.service.OAuth2TokenExchangeService oauth2ExchangeService;
    private final PasswordResetService passwordResetService;
    private final SessionService sessionService;
    private final JwtService jwtService;

    // ------------------------------------------------------------------
    // POST /oauth2/exchange
    // ------------------------------------------------------------------

    @Operation(summary = "OAuth2 Token Exchange",
               description = "Exchanges a ZenCube authorization code for Talent Registry access tokens.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully exchanged token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid code or request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User role not allowed")
    })
    @PostMapping("/oauth2/exchange")
    public ResponseEntity<ApiResponse<AuthResponse>> exchangeToken(
            @Valid @RequestBody OAuth2ExchangeRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        
        AuthResponse response = oauth2ExchangeService.exchangeToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("OAuth2 Login successful", response));
    }

    // ------------------------------------------------------------------
    // POST /register
    // ------------------------------------------------------------------

    @Operation(summary = "Register a new user account",
               description = "Creates a new user and sends an email verification link.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse user = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Account created. Please verify your email.", user));
    }

    // ------------------------------------------------------------------
    // POST /register/hr
    // ------------------------------------------------------------------

    @Operation(summary = "Register a new HR Staff account",
               description = "Creates a new HR user and sends an email verification link.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "HR Account created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/register/hr")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerHr(
            @Valid @RequestBody RegisterRequest request) {

        RegistrationResponse response = authService.registerHr(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response.message(), response));
    }

    // ------------------------------------------------------------------
    // POST /register/enterprise
    // ------------------------------------------------------------------

    @Operation(summary = "Register a new Enterprise Recruiter account",
               description = "Creates a new Enterprise Recruiter account, captures company name, and sends email verification.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recruiter account registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or company name already registered")
    })
    @PostMapping("/register/enterprise")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerEnterprise(
            @Valid @RequestBody EnterpriseRegisterRequest request) {

        RegistrationResponse response = authService.registerEnterprise(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response.message(), response));
    }


    // ------------------------------------------------------------------
    // POST /login
    // ------------------------------------------------------------------

    @Operation(summary = "Login",
               description = "Authenticates user with email and password.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully logged in"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ------------------------------------------------------------------
    // POST /refresh
    // ------------------------------------------------------------------

    @Operation(summary = "Refresh Token",
               description = "Exchanges a refresh token for a new token pair.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tokens refreshed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        AuthResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Tokens refreshed", response));
    }

    // ------------------------------------------------------------------
    // GET /session
    // ------------------------------------------------------------------

    @Operation(summary = "Get Session Details",
               description = "Returns the details of the currently active session.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Session details retrieved")
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            jakarta.servlet.http.HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String jwt = authHeader.substring(7);
        String jti = jwtService.extractJti(jwt);
        
        SessionResponse response = sessionService.validateCurrentSession(jti);
        return ResponseEntity.ok(ApiResponse.success("Session details retrieved", response));
    }

    // ------------------------------------------------------------------
    // GET /me
    // ------------------------------------------------------------------

    @Operation(summary = "Get Current User",
               description = "Returns the complete profile and permissions of the currently authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> getMe(
            @AuthenticationPrincipal com.zencube.registry.security.model.CustomUserDetails userDetails) {

        MeResponse response = authService.getMe(userDetails);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }

    // ------------------------------------------------------------------
    // POST /logout
    // ------------------------------------------------------------------

    @Operation(summary = "Logout Current Session",
               description = "Revokes the current session for the authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            jakarta.servlet.http.HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String jti = jwtService.extractJti(jwt);
            sessionService.logoutCurrentSession(jti);
        }
        return ResponseEntity.ok(ApiResponse.success("Current session logged out successfully"));
    }

    // ------------------------------------------------------------------
    // POST /logout/all
    // ------------------------------------------------------------------

    @Operation(summary = "Logout All Sessions",
               description = "Revokes all active sessions for the authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All sessions logged out successfully")
    @PostMapping("/logout/all")
    public ResponseEntity<ApiResponse<LogoutAllResponse>> logoutAll(
            @AuthenticationPrincipal com.zencube.registry.security.model.CustomUserDetails userDetails) {

        LogoutAllResponse response = sessionService.logoutAllSessions(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success("All sessions logged out successfully", response));
    }

    // ------------------------------------------------------------------
    // POST /verify-email
    // ------------------------------------------------------------------

    @Operation(summary = "Verify email address",
               description = "Confirms the user's email using the token sent by email.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        authService.verifyEmail(request.token());
        return ResponseEntity.ok(ApiResponse.success("Email verified. You can now log in."));
    }

    // ------------------------------------------------------------------
    // POST /resend-verification
    // ------------------------------------------------------------------

    @Operation(summary = "Resend email verification token",
               description = "Sends a new verification link if the email exists and rate limits are respected.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resend attempt complete (always returns success)")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {

        authService.resendVerification(request.email());
        return ResponseEntity.ok(ApiResponse.success("If the email is registered and not verified, a new verification link has been sent."));
    }

    // ------------------------------------------------------------------
    // POST /password/reset-request
    // ------------------------------------------------------------------

    @Operation(summary = "Request a password reset email",
               description = "Sends a password-reset link to the given email if an account exists.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset email sent (if account exists)")
    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request) {

        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(ApiResponse.success(
                "If an account with that email exists, password reset instructions have been sent."));
    }

    // ------------------------------------------------------------------
    // POST /password/reset
    // ------------------------------------------------------------------

    @Operation(summary = "Reset password",
               description = "Sets a new password using the one-time token from the reset email.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmDto request) {

        passwordResetService.confirmReset(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please log in."));
    }
}
