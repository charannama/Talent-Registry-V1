package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class SessionExpiredException extends AuthenticationException {
    public SessionExpiredException(String message) {
        super(message);
    }
}
