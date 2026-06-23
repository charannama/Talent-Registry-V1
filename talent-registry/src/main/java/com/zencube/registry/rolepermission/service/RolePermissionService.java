package com.zencube.registry.rolepermission.service;

import com.zencube.registry.rolepermission.dto.CreateRolePermissionRequest;
import com.zencube.registry.rolepermission.dto.RolePermissionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Role-Permission RBAC mapping operations.
 *
 * <p>This interface defines the complete lifecycle of a role-permission assignment:
 * assigning a permission to a role, querying mappings from both directions,
 * and revoking a specific mapping by its UUID.
 *
 * <h2>RBAC Integration</h2>
 * <p>When JWT security is enabled, the service implementation should be
 * protected with {@code @PreAuthorize("hasAuthority('MANAGE_ROLES')")}.
 * At that point, only users holding the MANAGE_ROLES permission can
 * create or delete mappings; read operations may be opened to ADMIN-level roles.
 */
public interface RolePermissionService {

    /**
     * Assigns a permission to a role by creating a new {@link com.zencube.registry.rolepermission.entity.RolePermission}
     * mapping record.
     *
     * <p>Business rules enforced:
     * <ol>
     *   <li>Role with given {@code roleId} must exist and not be soft-deleted.</li>
     *   <li>Permission with given {@code permissionId} must exist and not be soft-deleted.</li>
     *   <li>The (role, permission) pair must not already have an active mapping.</li>
     * </ol>
     *
     * @param request the request containing {@code roleId} and {@code permissionId}
     * @return the created mapping as a {@link RolePermissionResponse}
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if role or permission is not found
     * @throws com.zencube.registry.common.exception.ConflictException if the mapping already exists
     */
    RolePermissionResponse assignPermissionToRole(CreateRolePermissionRequest request);

    /**
     * Soft-deletes (revokes) a specific role-permission mapping by its UUID.
     *
     * <p>The mapping record is not physically deleted — it is marked as
     * {@code is_deleted = true} for audit trail purposes.
     *
     * @param id the UUID of the mapping to revoke
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if no active mapping exists with this ID
     */
    void removePermissionFromRole(UUID id);

    /**
     * Returns all active permission mappings for a specific role.
     *
     * @param roleId the UUID of the role
     * @return list of active role-permission mappings, possibly empty
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if the role does not exist
     */
    List<RolePermissionResponse> getPermissionsByRole(UUID roleId);

    /**
     * Returns all active role mappings for a specific permission.
     *
     * <p>Useful for auditing — e.g. "which roles currently hold VIEW_STUDENTS?"
     *
     * @param permissionId the UUID of the permission
     * @return list of active role-permission mappings, possibly empty
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if the permission does not exist
     */
    List<RolePermissionResponse> getRolesByPermission(UUID permissionId);

    /**
     * Returns all active role-permission mappings in the system.
     *
     * @return list of all active mappings, possibly empty
     */
    List<RolePermissionResponse> getAllMappings();

    /**
     * Retrieves a single active mapping by its UUID.
     *
     * @param id the UUID of the mapping
     * @return the mapping as a {@link RolePermissionResponse}
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if not found or soft-deleted
     */
    RolePermissionResponse getMappingById(UUID id);
}
