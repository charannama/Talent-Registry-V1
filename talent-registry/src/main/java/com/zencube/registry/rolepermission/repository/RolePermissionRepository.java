package com.zencube.registry.rolepermission.repository;

import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.rolepermission.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RolePermission} mappings.
 *
 * <h2>Soft-Delete Strategy</h2>
 * <p>RolePermission mappings are soft-deleted via {@link com.zencube.registry.common.BaseEntity#softDelete(String)}.
 * All query methods include {@code AND rp.deleted = false} to exclude logically-deleted mappings.
 *
 * <h2>Entity-based Parameters</h2>
 * <p>{@link #existsByRoleAndPermissionAndDeletedFalse} and the {@code findBy*} methods
 * accept full entity objects rather than raw UUIDs. This lets Spring Data JPA generate
 * an optimised {@code WHERE role_id = ? AND ...} clause without an extra join, and it
 * keeps the service code explicit about what it is passing.
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    /**
     * Checks whether an active (non-deleted) mapping already exists for the
     * given role and permission pair.
     *
     * <p>Called <em>before</em> every {@code assignPermissionToRole} call to produce
     * a clean HTTP 409 response instead of a database constraint violation.
     *
     * @param role       the role entity
     * @param permission the permission entity
     * @return {@code true} if a non-deleted mapping exists
     */
    boolean existsByRoleAndPermissionAndDeletedFalse(Role role, Permission permission);

    /**
     * Returns all active (non-deleted) permission mappings for the given role.
     *
     * <p>Used by {@code GET /api/v1/role-permissions/role/{roleId}} to list
     * every permission currently assigned to a specific role.
     *
     * @param role the role entity whose permission mappings to retrieve
     * @return list of active mappings, possibly empty
     */
    List<RolePermission> findByRoleAndDeletedFalse(Role role);

    /**
     * Returns all active (non-deleted) role mappings for the given permission.
     *
     * <p>Used by {@code GET /api/v1/role-permissions/permission/{permissionId}} to list
     * every role that currently holds a specific permission — useful for auditing.
     *
     * @param permission the permission entity whose role mappings to retrieve
     * @return list of active mappings, possibly empty
     */
    List<RolePermission> findByPermissionAndDeletedFalse(Permission permission);

    /**
     * Returns all active (non-deleted) role-permission mappings in the system.
     *
     * <p>Uses a JPQL fetch join to load {@code role} and {@code permission}
     * in a single query, avoiding N+1 SELECT problems when the response list
     * is mapped and each relationship is accessed during serialisation.
     *
     * @return list of all active mappings, possibly empty
     */
    @Query("""
            SELECT rp FROM RolePermission rp
            JOIN FETCH rp.role
            JOIN FETCH rp.permission
            WHERE rp.deleted = false
            ORDER BY rp.createdAt DESC
            """)
    List<RolePermission> findAllActiveWithDetails();
}
