package com.zencube.registry.admin.service;

import com.zencube.registry.admin.dto.request.AssignRoleRequest;
import com.zencube.registry.admin.dto.request.CreateRoleRequest;
import com.zencube.registry.admin.dto.request.RemoveRoleRequest;
import com.zencube.registry.admin.dto.response.RoleDetailsResponse;
import com.zencube.registry.admin.dto.response.RoleResponse;
import com.zencube.registry.admin.dto.response.UserRoleResponse;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    void assignRole(AssignRoleRequest request);

    void removeRole(RemoveRoleRequest request);

    List<RoleResponse> listRoles();

    RoleDetailsResponse getRoleDetails(UUID roleId);

    RoleResponse createRole(CreateRoleRequest request);

    void deleteRole(UUID roleId);

    List<UserRoleResponse> listUsersByRole(String roleName);
}
