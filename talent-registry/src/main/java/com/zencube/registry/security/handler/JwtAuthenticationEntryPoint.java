package com.zencube.registry.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.common.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Purpose:
 * Handles unauthorized access attempts by returning a structured 401 response.
 *
 * Layer:
 * Security / Handler
 *
 * Dependencies:
 * Spring Security, Jackson ObjectMapper
 *
 * Annotation Explanation:
 * @Component: Registers as a Spring Bean so it can be injected.
 * @RequiredArgsConstructor: For injecting ObjectMapper.
 * @Slf4j: For logging unauthorized access attempts.
 *
 * Business Logic Explanation:
 * When an unauthenticated user attempts to access a protected resource, Spring Security
 * invokes this entry point instead of returning a generic 403 or redirecting to a login page.
 * We write a JSON `ErrorResponse` directly to the response output stream.
 *
 * Security Considerations:
 * - We do not expose stack traces.
 * - Always return 401 Unauthorized (not 403 Forbidden) for missing or invalid credentials.
 *
 * Best Practices:
 * - Consistent Error Format: Ensures frontends receive the exact same error format
 *   for authentication failures as they do for business logic failures handled by @ControllerAdvice.
 *
 * Common Mistakes:
 * - Forgetting to set `response.setContentType(MediaType.APPLICATION_JSON_VALUE)`, causing
 *   the client to incorrectly parse the response.
 *
 * Unit Test Coverage:
 * Mock HttpServletRequest and HttpServletResponse. Trigger the commence method
 * and verify the response status and JSON body.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Full authentication is required to access this resource",
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
