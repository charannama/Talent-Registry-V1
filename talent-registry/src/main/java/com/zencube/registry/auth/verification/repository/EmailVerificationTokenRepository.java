package com.zencube.registry.auth.verification.repository;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.verification.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenAndDeletedFalse(String token);
    
    long countByUserAndCreatedAtAfter(User user, Instant timestamp);
}
