package com.zencube.registry.rolepermission.service.impl;

import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.repository.PermissionRepository;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.rolepermission.dto.CreateRolePermissionRequest;
import com.zencube.registry.rolepermission.dto.RolePermissionResponse;
import com.zencube.registry.rolepermission.entity.RolePermission;
import com.zencube.registry.rolepermission.mapper.RolePermissionMapper;
import com.zencube.registry.rolepermission.repository.RolePermissionRepository;
import com.zencube.registry.rolepermission.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link RolePermissionService}.
 *
 * <h2>Transaction Strategy</h2>
 * <ul>
 *   <li>Class-level {@code @Transactional(readOnly = true)} — all read methods
 *       participate in a read-only transaction; Hibernate skips dirty checking
 *       and the DB can use a read replica if configured.</li>
 *   <li>Method-level {@code @Transactional} — write methods (assign, remove) open
 *       a full read-write transaction scoped to the method call.</li>
 * </ul>
 *
 * <h2>Dependency Injection</h2>
 * <p>Constructor injection via {@code @RequiredArgsConstructor} (Lombok).
 * All dependencies are {@code final} fields — no field injection, no circular deps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolePermissionServiceImpl implements RolePermissionService {

    /** Resolves role UUIDs → Role entities. */
    private final RoleRepository roleRepository;

    /** Resolves permission UUIDs → Permission entities. */
    private final PermissionRepository permissionRepository;

    /** Persists and queries RolePermission join records. */
    private final RolePermissionRepository rolePermissionRepository;

    /** Converts RolePermission entities to response DTOs. */
    private final RolePermissionMapper rolePermissionMapper;

    // =========================================================================
    // ASSIGN  —  POST /api/v1/role-permissions
    // =========================================================================

    /**
     * Assigns a permission to a role.
     *
     * <p><b>Business logic (line by line):</b>
     * <ol>
     *   <li>Resolve {@code roleId} to an active {@link Role}; 404 if absent.</li>
     *   <li>Resolve {@code permissionId} to an active {@link Permission}; 404 if absent.</li>
     *   <li>Check {@code existsByRoleAndPermissionAndDeletedFalse} — 409 if duplicate.</li>
     *   <li>Build and persist the {@link RolePermission} entity.</li>
     *   <li>Map to DTO and return 201 payload.</li>
     * </ol>
     */
    @Override
    @Transactional
    public RolePermissionResponse assignPermissionToRole(CreateRolePermissionRequest request) {
        log.debug("Assigning permission '{}' to role '{}'", request.getPermissionId(), request.getRoleId());

        // Rule 1 — Role must exist and be active
        Role role = findActiveRole(request.getRoleId());

        // Rule 2 — Permission must exist and be active
        Permission permission = findActivePermission(request.getPermissionId());

        // Rule 3 — Duplicate mapping not allowed
        if (rolePermissionRepository.existsByRoleAndPermissionAndDeletedFalse(role, permission)) {
            throw new ConflictException(
                    String.format("Permission '%s' is already assigned to role '%s'",
                            permission.getCode(), role.getName())
            );
        }

        // Build the join entity — audit fields (createdAt, createdBy) populated by BaseEntity
        RolePermission mapping = RolePermission.builder()
                .role(role)
                .permission(permission)
                .build();

        RolePermission saved = rolePermissionRepository.save(mapping);

        log.info("Permission '{}' assigned to role '{}': mappingId='{}'",
                permission.getCode(), role.getName(), saved.getId());

        return rolePermissionMapper.toResponse(saved);
    }

    // =========================================================================
    // REVOKE  —  DELETE /api/v1/role-permissions/{id}
    // =========================================================================

    /**
     * Soft-deletes (revokes) a role-permission mapping.
     *
     * <p>The mapping is marked as deleted rather than physically removed,
     * preserving the audit trail of which permissions were granted and when.
     */
    @Override
    @Transactional
    public void removePermissionFromRole(UUID id) {
        log.debug("Revoking role-permission mapping id='{}'", id);
        RolePermission mapping = findActiveMapping(id);

        mapping.softDelete("system");
        rolePermissionRepository.save(mapping);

        log.info("Role-permission mapping soft-deleted: id='{}'", id);
    }

    // =========================================================================
    // QUERY — GET /api/v1/role-permissions/role/{roleId}
    // =========================================================================

    /**
     * Lists all active permissions assigned to a specific role.
     *
     * <p>First validates that the role exists (404 otherwise), then fetches
     * all non-deleted mappings for that role via the repository.
     */
    @Override
    public List<RolePermissionResponse> getPermissionsByRole(UUID roleId) {
        log.debug("Fetching permissions for roleId='{}'", roleId);
        Role role = findActiveRole(roleId);
        List<RolePermission> mappings = rolePermissionRepository.findByRoleAndDeletedFalse(role);
        return rolePermissionMapper.toResponseList(mappings);
    }

    // =========================================================================
    // QUERY — GET /api/v1/role-permissions/permission/{permissionId}
    // =========================================================================

    /**
     * Lists all active roles that hold a specific permission.
     *
     * <p>Validates that the permission exists first (404 otherwise),
     * then returns the reverse lookup — useful for auditing RBAC assignments.
     */
    @Override
    public List<RolePermissionResponse> getRolesByPermission(UUID permissionId) {
        log.debug("Fetching roles for permissionId='{}'", permissionId);
        Permission permission = findActivePermission(permissionId);
        List<RolePermission> mappings = rolePermissionRepository.findByPermissionAndDeletedFalse(permission);
        return rolePermissionMapper.toResponseList(mappings);
    }

    // =========================================================================
    // QUERY — GET /api/v1/role-permissions
    // =========================================================================

    /**
     * Returns all active role-permission mappings in the system.
     *
     * <p>Uses the JPQL fetch-join query ({@code findAllActiveWithDetails}) to
     * load both {@code role} and {@code permission} in a single DB round-trip,
     * eliminating N+1 SELECT problems.
     */
    @Override
    public List<RolePermissionResponse> getAllMappings() {
        log.debug("Fetching all active role-permission mappings");
        return rolePermissionMapper.toResponseList(
                rolePermissionRepository.findAllActiveWithDetails()
        );
    }

    // =========================================================================
    // QUERY — GET /api/v1/role-permissions/{id}
    // =========================================================================

    /**
     * Retrieves a single active mapping by its UUID.
     */
    @Override
    public RolePermissionResponse getMappingById(UUID id) {
        log.debug("Fetching role-permission mapping id='{}'", id);
        return rolePermissionMapper.toResponse(findActiveMapping(id));
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Resolves a role UUID to an active Role entity.
     *
     * @throws ResourceNotFoundException if not found or soft-deleted
     */
    private Role findActiveRole(UUID roleId) {
        return roleRepository.findById(roleId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
    }

    /**
     * Resolves a permission UUID to an active Permission entity.
     *
     * @throws ResourceNotFoundException if not found or soft-deleted
     */
    private Permission findActivePermission(UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));
    }

    /**
     * Resolves a mapping UUID to an active RolePermission entity.
     *
     * @throws ResourceNotFoundException if not found or soft-deleted
     */
    private RolePermission findActiveMapping(UUID id) {
        return rolePermissionRepository.findById(id)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("RolePermission", "id", id));
    }
}
