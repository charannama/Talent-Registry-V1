package com.zencube.registry.admin.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoleDetailsResponse(
    UUID id,
    String name,
    String description,
    boolean isSystemRole,
    List<String> permissions,
    Instant createdAt
) {}
