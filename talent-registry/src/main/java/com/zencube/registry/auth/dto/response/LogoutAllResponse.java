package com.zencube.registry.auth.dto.response;

public record LogoutAllResponse(
        boolean success,
        int revokedSessions
) {
}
