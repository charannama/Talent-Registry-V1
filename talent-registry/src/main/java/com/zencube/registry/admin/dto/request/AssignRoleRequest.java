package com.zencube.registry.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignRoleRequest(
    @NotNull(message = "User ID is required")
    UUID userId,
    
    @NotNull(message = "Role ID is required")
    UUID roleId
) {}
