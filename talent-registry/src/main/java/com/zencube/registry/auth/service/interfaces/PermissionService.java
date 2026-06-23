package com.zencube.registry.auth.service.interfaces;

import com.zencube.registry.auth.dto.permission.CreatePermissionRequest;
import com.zencube.registry.auth.dto.permission.PermissionResponse;
import com.zencube.registry.auth.dto.permission.UpdatePermissionRequest;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Permission CRUD operations.
 *
 * <p>Implementations are responsible for enforcing business rules
 * (uniqueness, soft-delete semantics, immutability of {@code code}).
 */
public interface PermissionService {

    /**
     * Creates a new permission.
     *
     * @param request the creation payload
     * @return the persisted permission as a response DTO
     * @throws com.zencube.registry.common.exception.ConflictException if a permission with the same
     *         {@code code} or {@code name} already exists
     */
    PermissionResponse createPermission(CreatePermissionRequest request);

    /**
     * Updates an existing permission's name and description.
     *
     * <p>The {@code code} field is immutable and cannot be changed.
     *
     * @param id      the UUID of the permission to update
     * @param request the update payload
     * @return the updated permission as a response DTO
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if no active
     *         permission exists with the given {@code id}
     */
    PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request);

    /**
     * Retrieves a single active permission by its UUID.
     *
     * @param id the UUID of the permission
     * @return the permission as a response DTO
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if not found or soft-deleted
     */
    PermissionResponse getPermission(UUID id);

    /**
     * Returns all active (non-deleted) permissions.
     *
     * @return list of permission response DTOs, possibly empty
     */
    List<PermissionResponse> getAllPermissions();

    /**
     * Soft-deletes a permission by marking it as deleted.
     *
     * <p>The record is not physically removed from the database.
     *
     * @param id the UUID of the permission to delete
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if no active
     *         permission exists with the given {@code id}
     */
    void deletePermission(UUID id);
}
