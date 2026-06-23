package com.zencube.registry.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuth2ExchangeRequest(
        @NotBlank(message = "Authorization code is required")
        String code,

        @NotBlank(message = "PKCE code verifier is required")
        String codeVerifier,

        @NotBlank(message = "Redirect URI is required")
        String redirectUri
) {}
