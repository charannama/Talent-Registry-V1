package com.zencube.registry.auth.service;

import com.zencube.registry.auth.dto.AuthResponse;
import com.zencube.registry.auth.dto.OAuth2ExchangeRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface OAuth2TokenExchangeService {
    AuthResponse exchangeToken(OAuth2ExchangeRequest request, HttpServletRequest httpRequest);
}
