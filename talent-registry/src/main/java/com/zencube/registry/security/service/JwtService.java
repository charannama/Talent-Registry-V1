package com.zencube.registry.security.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Purpose:
 * Interface for generating and validating JSON Web Tokens (JWT).
 *
 * Layer:
 * Security / Service
 *
 * Dependencies:
 * Spring Security (UserDetails)
 *
 * Annotation Explanation:
 * None (pure interface).
 *
 * Business Logic Explanation:
 * Abstracts the JWT implementation away from the authentication and filter logic.
 * This allows swapping out the underlying JWT library without affecting core services.
 *
 * Security Considerations:
 * Tokens must be signed symmetrically or asymmetrically. This service handles the contract.
 *
 * Best Practices:
 * - Programming to an interface ensures decoupling.
 * - Pass `UserDetails` or custom principal objects to keep generation flexible.
 *
 * Common Mistakes:
 * - Coupling the AuthFilter directly to JJWT specific classes instead of this interface.
 */
public interface JwtService {

    String generateAccessToken(UserDetails userDetails);

    String generateAccessToken(UserDetails userDetails, String jti);

    String generateRefreshToken(UserDetails userDetails);

    String extractUsername(String token);

    String extractJti(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
