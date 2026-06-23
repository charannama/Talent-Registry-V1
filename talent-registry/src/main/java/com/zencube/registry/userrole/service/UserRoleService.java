package com.zencube.registry.userrole.service;

import com.zencube.registry.userrole.dto.CreateUserRoleRequest;
import com.zencube.registry.userrole.dto.UserRoleResponse;

import java.util.List;
import java.util.UUID;

/**
 * 1. Purpose
 * Service interface defining the contract for User-Role assignments (RBAC).
 *
 * 2. Layer
 * Service Interface Layer.
 *
 * 5. Business Logic Explanation
 * Methods to assign roles to users, list assignments, and revoke them.
 * Once assigned, mappings cannot be "updated", only revoked (soft-deleted).
 */
public interface UserRoleService {

    UserRoleResponse assignRoleToUser(CreateUserRoleRequest request);

    List<UserRoleResponse> getRolesByUser(UUID userId);

    List<UserRoleResponse> getUsersByRole(UUID roleId);

    List<UserRoleResponse> getAllMappings();

    void removeRoleFromUser(UUID id);
}
