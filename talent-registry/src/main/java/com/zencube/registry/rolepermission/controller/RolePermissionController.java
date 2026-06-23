package com.zencube.registry.rolepermission.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.rolepermission.dto.CreateRolePermissionRequest;
import com.zencube.registry.rolepermission.dto.RolePermissionResponse;
import com.zencube.registry.rolepermission.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for RBAC Role-Permission assignment management.
 *
 * <p><b>Base URL:</b> {@code /api/v1/role-permissions}
 *
 * <h2>Security</h2>
 * <p>Currently open for development/testing (see {@link com.zencube.registry.security.config.SecurityConfig}).
 * Each method carries a commented {@code @PreAuthorize} annotation that must be
 * uncommented once JWT authentication is wired. All write operations require
 * {@code MANAGE_ROLES}; read operations are accessible to any {@code ADMIN}.
 *
 * <h2>RBAC Integration Strategy</h2>
 * <p>Once the JWT filter chain is enabled:
 * <ol>
 *   <li>The filter extracts the authenticated user's granted authorities from the token.</li>
 *   <li>{@code @EnableMethodSecurity} (already in {@code SecurityConfig}) activates
 *       method-level evaluation of {@code @PreAuthorize}.</li>
 *   <li>Only users whose token contains {@code MANAGE_ROLES} can mutate mappings.</li>
 * </ol>
 */
@Validated
@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
@Tag(
    name = "Role-Permission Management",
    description = "APIs for assigning and revoking permissions on roles (RBAC bridge)"
)
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    // -------------------------------------------------------------------------
    // POST /api/v1/role-permissions
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "Assign Permission to Role",
        description = "Creates a new role-permission mapping. " +
                      "Both roleId and permissionId must refer to existing, non-deleted entities. " +
                      "Duplicate assignments are rejected with HTTP 409."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Permission successfully assigned to role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed — roleId or permissionId missing"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role or Permission not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Mapping already exists")
    })
    // @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> assignPermissionToRole(
            @Valid @RequestBody CreateRolePermissionRequest request) {
        RolePermissionResponse response = rolePermissionService.assignPermissionToRole(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Permission assigned to role successfully", response));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "Get All Role-Permission Mappings",
        description = "Retrieves a complete list of all active role-permission assignments in the system. " +
                      "Results are ordered by creation date (newest first)."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mappings retrieved successfully")
    })
    // @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> getAllMappings() {
        List<RolePermissionResponse> response = rolePermissionService.getAllMappings();
        return ResponseEntity.ok(ApiResponse.success("Role-permission mappings retrieved successfully", response));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions/{id}
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(
        summary = "Get Mapping By ID",
        description = "Retrieves a single active role-permission mapping by its UUID."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mapping retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mapping not found or already revoked")
    })
    // @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> getMappingById(
            @Parameter(description = "UUID of the role-permission mapping", required = true)
            @PathVariable UUID id) {
        RolePermissionResponse response = rolePermissionService.getMappingById(id);
        return ResponseEntity.ok(ApiResponse.success("Role-permission mapping retrieved successfully", response));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions/role/{roleId}
    // -------------------------------------------------------------------------

    @GetMapping("/role/{roleId}")
    @Operation(
        summary = "Get Permissions By Role",
        description = "Returns all permissions currently assigned to the specified role. " +
                      "Returns 404 if the role does not exist."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    // @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> getPermissionsByRole(
            @Parameter(description = "UUID of the role to query permissions for", required = true)
            @PathVariable UUID roleId) {
        List<RolePermissionResponse> response = rolePermissionService.getPermissionsByRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Permissions for role retrieved successfully", response));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions/permission/{permissionId}
    // -------------------------------------------------------------------------

    @GetMapping("/permission/{permissionId}")
    @Operation(
        summary = "Get Roles By Permission",
        description = "Returns all roles that are currently assigned the specified permission. " +
                      "Useful for auditing RBAC assignments. Returns 404 if the permission does not exist."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found")
    })
    // @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> getRolesByPermission(
            @Parameter(description = "UUID of the permission to query roles for", required = true)
            @PathVariable UUID permissionId) {
        List<RolePermissionResponse> response = rolePermissionService.getRolesByPermission(permissionId);
        return ResponseEntity.ok(ApiResponse.success("Roles for permission retrieved successfully", response));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/role-permissions/{id}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Revoke Permission from Role",
        description = "Soft-deletes a role-permission mapping, effectively revoking the permission from the role. " +
                      "The record is retained in the database for audit trail purposes. " +
                      "Returns 404 if no active mapping exists with the given ID."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Permission revoked successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mapping not found or already revoked")
    })
    // @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<Void> removePermissionFromRole(
            @Parameter(description = "UUID of the role-permission mapping to revoke", required = true)
            @PathVariable UUID id) {
        rolePermissionService.removePermissionFromRole(id);
        return ResponseEntity.noContent().build();
    }
}
