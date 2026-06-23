package com.zencube.registry.session.service.impl;

import com.zencube.registry.auth.dto.response.LogoutAllResponse;
import com.zencube.registry.auth.dto.response.SessionResponse;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.common.exception.InvalidSessionException;
import com.zencube.registry.common.exception.SessionExpiredException;
import com.zencube.registry.common.exception.SessionRevokedException;
import com.zencube.registry.session.entity.Session;
import com.zencube.registry.session.repository.SessionRepository;
import com.zencube.registry.session.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public Session createAndSaveSession(User user, HttpServletRequest request, String jti, String hashedRefreshToken, long refreshTokenExpirySeconds) {
        
        Session session = Session.builder()
                .user(user)
                .refreshTokenHash(hashedRefreshToken)
                .accessTokenJti(jti)
                .expiresAt(Instant.now().plus(refreshTokenExpirySeconds, ChronoUnit.MILLIS))
                .ipAddress(extractIpAddress(request))
                .userAgent(extractUserAgent(request))
                .lastActivityAt(Instant.now())
                .build();
                
        return sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse validateCurrentSession(String jti) {
        Session session = sessionRepository.findByAccessTokenJtiAndDeletedFalse(jti)
                .orElseThrow(() -> new InvalidSessionException("Session not found or invalid"));

        if (session.isRevoked()) {
            throw new SessionRevokedException("Session has been revoked");
        }
        
        if (session.isExpired()) {
            throw new SessionExpiredException("Session has expired");
        }

        User user = session.getUser();
        List<String> roles = userRoleRepository.findByUserAndDeletedFalse(user)
                .stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        return new SessionResponse(
                user.getId(),
                user.getEmail(),
                roles,
                session.getId().toString(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
    }

    @Override
    @Transactional
    public void logoutCurrentSession(String jti) {
        Session session = sessionRepository.findByAccessTokenJtiAndDeletedFalse(jti)
                .orElseThrow(() -> new InvalidSessionException("Session not found"));

        if (!session.isRevoked()) {
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);
            log.info("Session {} revoked for user {}", session.getId(), session.getUser().getId());
        }
    }

    @Override
    @Transactional
    public LogoutAllResponse logoutAllSessions(User user) {
        List<Session> activeSessions = findActiveSessions(user);
        
        int count = 0;
        Instant now = Instant.now();
        for (Session session : activeSessions) {
            session.setRevokedAt(now);
            count++;
        }
        
        sessionRepository.saveAll(activeSessions);
        log.info("Revoked {} sessions for user {}", count, user.getId());
        
        return new LogoutAllResponse(true, count);
    }

    @Override
    public List<Session> findActiveSessions(User user) {
        return sessionRepository.findActiveSessions(user);
    }

    @Override
    @Transactional
    public void revokeSession(UUID id) {
        sessionRepository.findById(id).ifPresent(session -> {
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);
        });
    }

    @Override
    @Transactional
    public void revokeAllUserSessions(User user) {
        logoutAllSessions(user);
    }

    @Override
    @Transactional
    public void updateLastActivity(String jti) {
        sessionRepository.findByAccessTokenJtiAndDeletedFalse(jti).ifPresent(session -> {
            session.setLastActivityAt(Instant.now());
            sessionRepository.save(session);
        });
    }

    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            // Can contain multiple IPs, the first one is the client
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader("User-Agent");
    }
}
