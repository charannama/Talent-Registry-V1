package com.zencube.registry.auth.dto.role;

import com.zencube.registry.common.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Role type is required")
    private RoleType roleType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
