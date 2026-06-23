package com.zencube.registry.enterprise.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class EnterpriseOwnershipException extends BusinessException {
    public EnterpriseOwnershipException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ENTERPRISE_OWNERSHIP_DENIED");
    }
}
