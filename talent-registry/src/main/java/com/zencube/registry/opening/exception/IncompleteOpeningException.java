package com.zencube.registry.opening.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class IncompleteOpeningException extends BusinessException {
    public IncompleteOpeningException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INCOMPLETE_OPENING");
    }
}
