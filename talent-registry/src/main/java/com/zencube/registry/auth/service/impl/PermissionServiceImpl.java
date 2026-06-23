package com.zencube.registry.auth.service.impl;

import com.zencube.registry.auth.dto.permission.CreatePermissionRequest;
import com.zencube.registry.auth.dto.permission.PermissionResponse;
import com.zencube.registry.auth.dto.permission.UpdatePermissionRequest;
import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.mapper.PermissionMapper;
import com.zencube.registry.auth.repository.PermissionRepository;
import com.zencube.registry.auth.service.interfaces.PermissionService;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link PermissionService}.
 *
 * <p>All read operations run within a read-only transaction for performance.
 * Write operations open a read-write transaction via the method-level {@code @Transactional}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        log.debug("Creating permission with code='{}' name='{}'", request.getCode(), request.getName());

        // Validate uniqueness of code
        if (permissionRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new ConflictException("Permission", "code", request.getCode());
        }

        // Validate uniqueness of name
        if (permissionRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new ConflictException("Permission", "name", request.getName());
        }

        Permission permission = permissionMapper.toEntity(request);
        Permission savedPermission = permissionRepository.save(permission);

        log.info("Permission created: id='{}' code='{}'", savedPermission.getId(), savedPermission.getCode());
        return permissionMapper.toResponse(savedPermission);
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    @Override
    public PermissionResponse getPermission(UUID id) {
        log.debug("Fetching permission with id='{}'", id);
        Permission permission = findActivePermissionById(id);
        return permissionMapper.toResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        log.debug("Fetching all active permissions");
        return permissionRepository.findAllByDeletedFalse()
                .stream()
                .map(permissionMapper::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request) {
        log.debug("Updating permission id='{}'", id);
        Permission permission = findActivePermissionById(id);

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        // Note: code is intentionally NOT updated (immutable after creation)

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission updated: id='{}' code='{}'", savedPermission.getId(), savedPermission.getCode());
        return permissionMapper.toResponse(savedPermission);
    }

    // ------------------------------------------------------------------
    // DELETE (soft)
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void deletePermission(UUID id) {
        log.debug("Soft-deleting permission id='{}'", id);
        Permission permission = findActivePermissionById(id);

        permission.softDelete("system");
        permissionRepository.save(permission);
        log.info("Permission soft-deleted: id='{}'", id);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Retrieves an active (non-deleted) permission by ID or throws a 404.
     *
     * @param id the permission UUID
     * @return the found, non-deleted {@link Permission}
     * @throws ResourceNotFoundException if not found or soft-deleted
     */
    private Permission findActivePermissionById(UUID id) {
        return permissionRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }
}
