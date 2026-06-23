package com.zencube.registry.auth.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Payload for updating an existing {@link com.zencube.registry.auth.entity.Permission}.
 *
 * <p>Note: {@code code} is intentionally excluded — it is immutable after creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePermissionRequest {

    /** Updated human-readable label. */
    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name must not exceed 100 characters")
    private String name;

    /** Updated description (may be blank to clear it). */
    private String description;
}
