package com.zencube.registry.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRedirectResponse {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String role;
    private String redirectUrl;
    private long expiresIn;
}
