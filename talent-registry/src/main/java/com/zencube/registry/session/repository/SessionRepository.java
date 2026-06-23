package com.zencube.registry.session.repository;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 1. Purpose
 * Spring Data JPA repository for the Session entity.
 *
 * 2. Layer
 * Repository / Data Access Layer.
 *
 * 5. Business Logic Explanation
 * Provides methods to find sessions by token hash, fetch all sessions for a user,
 * and fetch only active sessions (ignoring revoked, expired, or deleted ones).
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenHashAndDeletedFalse(String refreshTokenHash);

    Optional<Session> findByAccessTokenJtiAndDeletedFalse(String accessTokenJti);

    @Query("SELECT s FROM Session s JOIN FETCH s.user WHERE s.user = :user AND s.deleted = false ORDER BY s.createdAt DESC")
    List<Session> findByUserAndDeletedFalse(User user);

    @Query("SELECT s FROM Session s JOIN FETCH s.user WHERE s.user = :user AND s.deleted = false AND s.revokedAt IS NULL AND s.expiresAt > CURRENT_TIMESTAMP ORDER BY s.createdAt DESC")
    List<Session> findActiveSessions(User user);
}
