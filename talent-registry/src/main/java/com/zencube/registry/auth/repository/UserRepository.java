package com.zencube.registry.auth.repository;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 * All soft-deleted records ({@code is_deleted = true}) are excluded
 * by default — add explicit queries when you need to include them.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Looks up an active (non-deleted) user by email address.
     * Used during login and duplicate-email checks.
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * Checks whether an email is already registered (including soft-deleted records).
     * Use this before registration to prevent re-use of a deleted account's email.
     */
    Optional<User> findByIdAndDeletedFalse(UUID id);

    boolean existsByEmail(String email);

    /**
     * Checks whether an active account with this email already exists.
     */
    boolean existsByEmailAndDeletedFalse(String email);

    /**
     * Checks whether a user with this company name exists and is not soft-deleted.
     */
    boolean existsByCompanyNameIgnoreCaseAndDeletedFalse(String companyName);

    /**
     * Looks up a user by their OAuth2 provider + provider-specific user ID.
     * Used during OAuth2 login to find or create the local account.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.providerId = :providerId
          AND u.authProvider = :provider
          AND u.deleted = false
        """)
    Optional<User> findByProviderIdAndAuthProvider(
            @Param("providerId") String providerId,
            @Param("provider") com.zencube.registry.auth.enums.AuthProvider provider);

    /**
     * Bulk-updates the status of a single user.
     * Used for account activation, suspension, etc.
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") Status status);

    /**
     * Marks the user's email as verified.
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :id")
    void markEmailVerified(@Param("id") UUID id);
}

