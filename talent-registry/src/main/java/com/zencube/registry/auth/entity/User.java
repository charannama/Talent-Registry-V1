package com.zencube.registry.auth.entity;

import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a registered user account in the system.
 *
 * Maps to the {@code users} table. Authentication credentials and
 * provider information are stored here; profile-specific data
 * (bio, skills, etc.) lives in the {@code Student} or enterprise-user
 * relationship tables.
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /** Primary email used for login and notifications. */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt‑hashed password. Null for OAuth2‑only users. */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    /** Account lifecycle state. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /** Identity provider that created this account. */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    /** Provider's own user identifier (for OAuth2 accounts). Null for LOCAL accounts. */
    @Column(name = "provider_id", length = 255)
    private String providerId;

    /** URL to the user's avatar/profile picture. */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;


    /** Whether the user's email address has been verified. */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    /** Timestamp when the email was verified. */
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    /** Timestamp of the last successful login. */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /** User's timezone setting. */
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    /** Timestamp when the password was last changed. */
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    // ------------------------------------------------------------------
    // Convenience helpers
    // ------------------------------------------------------------------

    /** Returns the user's full name, or email if name fields are blank. */
    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName.trim() + " " + lastName.trim();
        }
        return email;
    }

    /** {@code true} when the account is fully operational. */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    @OneToMany(mappedBy = "user")
    private java.util.Set<com.zencube.registry.userrole.entity.UserRole> userRoles;

    @OneToMany(mappedBy = "user")
    private java.util.Set<com.zencube.registry.session.entity.Session> sessions;

    @OneToOne(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    private com.zencube.registry.profile.entity.StudentProfile studentProfile;
}
