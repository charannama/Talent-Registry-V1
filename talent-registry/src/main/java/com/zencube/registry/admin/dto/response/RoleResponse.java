package com.zencube.registry.admin.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RoleResponse(
    UUID id,
    String name,
    String description,
    boolean isSystemRole,
    Instant createdAt
) {}
