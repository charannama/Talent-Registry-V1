package com.zencube.registry.opening.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OpeningNotDraftException extends BusinessException {
    public OpeningNotDraftException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "OPENING_NOT_DRAFT");
    }
}
