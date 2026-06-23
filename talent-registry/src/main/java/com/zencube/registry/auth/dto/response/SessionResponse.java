package com.zencube.registry.auth.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SessionResponse(
        UUID userId,
        String email,
        List<String> roles,
        String sessionId,
        String ipAddress,
        String userAgent,
        Instant createdAt,
        Instant expiresAt
) {
}
