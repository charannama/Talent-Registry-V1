package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountLockedException extends AuthenticationException {
    public AccountLockedException(String message) {
        super(message);
    }
}
