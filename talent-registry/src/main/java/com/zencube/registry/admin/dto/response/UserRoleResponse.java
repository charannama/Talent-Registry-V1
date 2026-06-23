package com.zencube.registry.admin.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserRoleResponse(
    UUID id,
    String email,
    String status,
    Instant createdAt
) {}
