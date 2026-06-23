package com.zencube.registry.auth.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Payload for creating a new {@link com.zencube.registry.auth.entity.Permission}.
 *
 * <p>The {@code code} field is immutable after creation;
 * it cannot be changed via {@link UpdatePermissionRequest}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePermissionRequest {

    /** Human-readable label, e.g. "View Students". */
    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name must not exceed 100 characters")
    private String name;

    /**
     * Unique machine-readable code, e.g. "VIEW_STUDENTS".
     * Must be unique across the system and cannot be updated after creation.
     */
    @NotBlank(message = "Permission code is required")
    @Size(max = 100, message = "Permission code must not exceed 100 characters")
    private String code;

    /** Optional human-readable description of what this permission grants. */
    private String description;
}
