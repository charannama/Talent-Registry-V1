package com.zencube.registry.security.service.impl;

import com.zencube.registry.security.jwt.JwtProperties;
import com.zencube.registry.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Purpose:
 * Concrete implementation of JwtService using the JJWT library.
 *
 * Layer:
 * Security / Service Implementation
 *
 * Dependencies:
 * JJWT API, JwtProperties, Spring Security
 *
 * Annotation Explanation:
 * @Service: Registers this class as a Spring service bean.
 * @RequiredArgsConstructor: Generates constructor for final fields, allowing constructor injection.
 *
 * Business Logic Explanation:
 * - Generates tokens with specific claims (roles, permissions, userId).
 * - Distinguishes between Roles and Permissions by looking for the "ROLE_" prefix in authorities.
 * - Extracts username (subject) and verifies expiration to ensure token validity.
 *
 * Security Considerations:
 * - Secret must be at least 256 bits. We decode it from Base64 string config.
 * - Signs tokens using HMAC-SHA256 (Keys.hmacShaKeyFor).
 *
 * Best Practices:
 * - Refresh tokens are bare-bones (only 'sub' and dates) to keep payload small.
 * - Access tokens carry authorization payload (roles, permissions) to stay stateless.
 * - Secrets are injected, never hardcoded.
 *
 * Common Mistakes:
 * - Putting too much data in the JWT, bloating the HTTP headers.
 * - Using a weak secret that can be easily brute-forced offline.
 *
 * Unit Test Coverage:
 * Should cover token generation, parsing, expiration checking, and invalid signature rejection.
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(userDetails, null);
    }

    @Override
    public String generateAccessToken(UserDetails userDetails, String jti) {
        return generateAccessToken(new HashMap<>(), userDetails, jti);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails, String jti) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .collect(Collectors.toList());

        List<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        return buildToken(claims, userDetails, jwtProperties.getAccessTokenExpiration(), jti);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshTokenExpiration(), null);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationMillis, String jti) {
        var builder = Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSignInKey(), Jwts.SIG.HS256);
        
        if (jti != null) {
            builder.id(jti);
        }
        
        return builder.compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
