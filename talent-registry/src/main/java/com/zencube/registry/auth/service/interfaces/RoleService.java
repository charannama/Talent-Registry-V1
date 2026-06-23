package com.zencube.registry.auth.service.interfaces;

import com.zencube.registry.auth.dto.role.CreateRoleRequest;
import com.zencube.registry.auth.dto.role.RoleResponse;
import com.zencube.registry.auth.dto.role.UpdateRoleRequest;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleResponse createRole(CreateRoleRequest request);
    RoleResponse getRole(UUID id);
    List<RoleResponse> getAllRoles();
    RoleResponse updateRole(UUID id, UpdateRoleRequest request);
    void deleteRole(UUID id);
}
