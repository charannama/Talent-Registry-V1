package com.zencube.registry.enterprise.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class EnterpriseAccessDeniedException extends BusinessException {
    public EnterpriseAccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ENTERPRISE_ACCESS_DENIED");
    }
}
