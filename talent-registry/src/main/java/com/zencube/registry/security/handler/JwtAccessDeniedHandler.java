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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Purpose:
 * Handles unauthorized access attempts by authenticated users lacking required permissions.
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
 * @Slf4j: For logging access denied attempts.
 *
 * Business Logic Explanation:
 * When an authenticated user attempts to access a protected resource they lack permissions for
 * (e.g. failing a @PreAuthorize check), Spring Security invokes this handler instead of
 * returning a generic 403. We write a JSON `ErrorResponse` directly to the response stream.
 *
 * Security Considerations:
 * - We do not expose stack traces.
 * - Always return 403 Forbidden (not 401 Unauthorized) since the user's identity is known,
 *   but their privileges are insufficient.
 *
 * Best Practices:
 * - Consistent Error Format: Ensures frontends receive the exact same error format.
 *
 * Common Mistakes:
 * - Returning 401 instead of 403, causing frontends to incorrectly attempt to refresh the token
 *   or redirect to the login page.
 *
 * Unit Test Coverage:
 * Mock HttpServletRequest and HttpServletResponse. Trigger the handle method
 * and verify the response status (403) and JSON body.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.error("Access denied error: {}", accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
