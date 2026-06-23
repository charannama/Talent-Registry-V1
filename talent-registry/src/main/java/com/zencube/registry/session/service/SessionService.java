package com.zencube.registry.session.service;

import com.zencube.registry.auth.dto.response.LogoutAllResponse;
import com.zencube.registry.auth.dto.response.SessionResponse;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.session.entity.Session;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

public interface SessionService {
    
    Session createAndSaveSession(User user, HttpServletRequest request, String jti, String hashedRefreshToken, long refreshTokenExpirySeconds);
    
    SessionResponse validateCurrentSession(String jti);
    
    void logoutCurrentSession(String jti);
    
    LogoutAllResponse logoutAllSessions(User user);

    List<Session> findActiveSessions(User user);

    void revokeSession(UUID id);

    void revokeAllUserSessions(User user);
    
    void updateLastActivity(String jti);
}
