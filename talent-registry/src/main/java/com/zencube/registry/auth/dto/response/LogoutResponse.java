package com.zencube.registry.auth.dto.response;

public record LogoutResponse(
        boolean success,
        String message
) {
}
