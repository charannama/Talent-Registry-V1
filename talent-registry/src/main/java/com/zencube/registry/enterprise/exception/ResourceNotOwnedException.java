package com.zencube.registry.enterprise.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ResourceNotOwnedException extends BusinessException {
    public ResourceNotOwnedException(String resourceType) {
        super("You do not have ownership rights to this " + resourceType, HttpStatus.FORBIDDEN, "RESOURCE_NOT_OWNED");
    }
}
