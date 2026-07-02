package com.zencube.registry.notification.email.model;

import lombok.Builder;
import java.util.Map;

@Builder
public record EmailTemplate(
        String templateName,
        Map<String, Object> variables
) {}
