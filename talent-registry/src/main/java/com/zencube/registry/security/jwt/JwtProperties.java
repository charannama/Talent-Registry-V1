package com.zencube.registry.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Purpose:
 * Binds and validates JWT-related configuration properties from application context.
 *
 * Layer:
 * Configuration / Security
 *
 * Dependencies:
 * Spring Boot Configuration Properties, Bean Validation
 *
 * Code:
 * Uses @ConfigurationProperties to bind "jwt.*" to fields.
 * Uses @Validated to ensure required properties are present on startup.
 *
 * Annotation Explanation:
 * @Configuration: Marks as a Spring-managed configuration bean.
 * @ConfigurationProperties(prefix = "jwt"): Tells Spring to map properties starting with "jwt".
 * @Validated: Triggers validation constraints (@NotBlank, @NotNull) when properties are bound.
 *
 * Business Logic Explanation:
 * By externalizing configuration into a strongly-typed class, the application fails fast
 * during startup if critical security properties (like the signing secret) are missing.
 *
 * Security Considerations:
 * The `secret` must be a strong, base64-encoded key of at least 256 bits (for HS256).
 * Ensure `application.properties` does not hardcode the secret in production (use env vars).
 *
 * Best Practices:
 * - Fail fast: Validating properties at startup prevents runtime NullPointerExceptions later.
 * - Centralization: Keeps all JWT properties in one place rather than scattering @Value annotations.
 *
 * Common Mistakes:
 * - Forgetting @Validated, allowing the app to start with a null secret.
 * - Missing getters/setters, causing Spring to silently fail to bind the properties.
 *
 * Unit Test Coverage:
 * Tested implicitly via context loads or by injecting the bean and asserting its values.
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @NotBlank(message = "JWT Secret must be configured")
    private String secret;

    @NotBlank(message = "JWT Issuer must be configured")
    private String issuer;

    @NotNull(message = "JWT Access Token Expiration must be configured")
    private Long accessTokenExpiration;

    @NotNull(message = "JWT Refresh Token Expiration must be configured")
    private Long refreshTokenExpiration;
}
