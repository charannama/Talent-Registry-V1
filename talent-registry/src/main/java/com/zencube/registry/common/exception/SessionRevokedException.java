package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class SessionRevokedException extends AuthenticationException {
    public SessionRevokedException(String message) {
        super(message);
    }
}
