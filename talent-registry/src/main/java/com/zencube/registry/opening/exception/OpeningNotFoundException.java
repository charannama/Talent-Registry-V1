package com.zencube.registry.opening.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OpeningNotFoundException extends RuntimeException {
    public OpeningNotFoundException(String message) {
        super(message);
    }
}
