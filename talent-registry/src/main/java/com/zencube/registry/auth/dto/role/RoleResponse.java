package com.zencube.registry.auth.dto.role;

import com.zencube.registry.common.enums.RoleType;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private UUID id;
    private String name;
    private RoleType roleType;
    private String description;
    private boolean isSystem;
}
