package com.zencube.registry.passwordreset.service.impl;

import com.zencube.registry.auth.email.EmailService;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.InvalidTokenException;
import com.zencube.registry.passwordreset.entity.PasswordResetToken;
import com.zencube.registry.passwordreset.repository.PasswordResetTokenRepository;
import com.zencube.registry.passwordreset.service.PasswordResetService;
import com.zencube.registry.session.entity.Session;
import com.zencube.registry.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public void requestReset(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmailAndDeletedFalse(normalizedEmail);

        if (userOpt.isEmpty()) {
            log.info("Password reset requested for unknown email: {}", normalizedEmail);
            return;
        }

        User user = userOpt.get();

        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
        log.info("Password reset email sent to user id={}", user.getId());
    }

    @Override
    @Transactional
    public void confirmReset(String token, String newPassword) {
        String tokenHash = hashToken(token);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHashAndDeletedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token."));

        if (resetToken.isExpired() || resetToken.getUsedAt() != null) {
            throw new InvalidTokenException("Invalid or expired reset token.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        List<Session> activeSessions = sessionRepository.findActiveSessions(user);
        for (Session session : activeSessions) {
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);
        }

        log.info("Password successfully reset for user id={}. Revoked {} active sessions.", user.getId(), activeSessions.size());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash password reset token", e);
        }
    }
}
