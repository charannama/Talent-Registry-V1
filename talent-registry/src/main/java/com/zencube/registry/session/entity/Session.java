package com.zencube.registry.session.entity;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 1. Purpose
 * Tracks an active user session based on an issued refresh token.
 * Maps to the `sessions` database table.
 *
 * 2. Layer
 * Entity / Persistence Layer.
 *
 * 4. Annotation Explanation
 * @Entity & @Table: Designates this class as a JPA entity mapped to "sessions".
 * @ManyToOne: Connects the session to exactly one User.
 *
 * 5. Business Logic Explanation
 * The session is considered active if `revokedAt` is null, `isDeleted` is false,
 * and `expiresAt` is in the future. We store `refresh_token_hash` rather than the
 * raw token to mitigate token theft if the database is compromised.
 *
 * 6. Best Practices
 * - Secure Token Storage: Only the SHA-256 hash of the refresh token is stored.
 * - Device Fingerprinting: `ipAddress` and `userAgent` assist in anomaly detection
 *   and allowing users to audit their active sessions.
 *
 * 7. Common Mistakes
 * - Storing raw refresh tokens in the database.
 * - Not indexing the hash column, making token lookups during rotation O(N).
 */
@Entity
@Table(
    name = "sessions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_sessions_token", columnNames = {"refresh_token_hash"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 255)
    private String refreshTokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "access_token_jti", length = 255)
    private String accessTokenJti;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;
    
    // ------------------------------------------------------------------
    // Convenience helpers
    // ------------------------------------------------------------------

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired() && !isDeleted();
    }
}
