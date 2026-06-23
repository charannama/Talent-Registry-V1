package com.zencube.registry.security.filter;

import com.zencube.registry.security.service.JwtService;
import com.zencube.registry.session.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Purpose:
 * Intercepts incoming HTTP requests to validate JWT access tokens in the Authorization header.
 *
 * Layer:
 * Security / Filter
 *
 * Dependencies:
 * JwtService, UserDetailsService, Spring Web Filter
 *
 * Annotation Explanation:
 * @Component: Registers as a Spring Bean so it can be injected into SecurityConfig.
 * @RequiredArgsConstructor: Injects final fields.
 * @Slf4j: Provides logging capabilities.
 *
 * Business Logic Explanation:
 * 1. Skips processing if the request is to a public auth endpoint (e.g., /api/v1/auth).
 * 2. Extracts the Bearer token from the Authorization header.
 * 3. Uses JwtService to extract the username and validate the token.
 * 4. Loads the UserDetails via UserDetailsService to construct the authorities.
 * 5. Populates the SecurityContextHolder if validation is successful.
 *
 * Security Considerations:
 * - We verify the token signature and expiration via JwtService.
 * - Checking if SecurityContextHolder.getContext().getAuthentication() == null ensures we don't
 *   overwrite an already authenticated context unnecessarily.
 * - Using WebAuthenticationDetailsSource adds the request IP and session ID (if any) to the Auth object.
 *
 * Best Practices:
 * - Extend OncePerRequestFilter to guarantee single execution per request dispatch.
 * - Fast fail: If no header is present, just continue the chain without doing expensive crypto.
 *
 * Common Mistakes:
 * - Forgetting to call filterChain.doFilter(), which halts the entire request lifecycle.
 * - Throwing exceptions directly here instead of letting the AuthenticationEntryPoint handle
 *   unauthenticated states later in the filter chain.
 *
 * Unit Test Coverage:
 * Tests should mock request, response, and chain. Verify behaviour with no header,
 * invalid header format, valid token (should populate context), and invalid/expired token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String requestURI = request.getRequestURI();

        // Skip JWT validation for auth endpoints
        if (requestURI.startsWith("/api/v1/auth/") || requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            // Check if header is present and starts with Bearer
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract token
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);
            String jti = jwtService.extractJti(jwt);

            // If user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validate token against user details
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    if (jti != null) {
                        sessionService.validateCurrentSession(jti);
                        sessionService.updateLastActivity(jti);
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Update Security Context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage());
            // We do not throw the exception here. We let the request proceed to the endpoint
            // without populating the SecurityContext. The endpoint will be protected by
            // Spring Security which will eventually trigger the AuthenticationEntryPoint (401).
        }

        filterChain.doFilter(request, response);
    }
}
