package com.zencube.registry.auth.controller;

import com.zencube.registry.auth.dto.permission.CreatePermissionRequest;
import com.zencube.registry.auth.dto.permission.PermissionResponse;
import com.zencube.registry.auth.dto.permission.UpdatePermissionRequest;
import com.zencube.registry.auth.service.interfaces.PermissionService;
import com.zencube.registry.common.response.ApiResponse;
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
 * REST controller for Permission management.
 *
 * <p>Base URL: {@code /api/v1/permissions}
 *
 * <p>Security: currently open for development and testing purposes.
 * {@code @PreAuthorize} annotations are prepared as comments on each method
 * and should be enabled when JWT authentication is fully wired.
 */
@Validated
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(
    name = "Permission Management",
    description = "APIs for creating, retrieving, updating, and deleting permissions used in RBAC"
)
public class PermissionController {

    private final PermissionService permissionService;

    // ------------------------------------------------------------------
    // POST /api/v1/permissions
    // ------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "Create Permission",
        description = "Creates a new permission in the system. The 'code' field must be unique " +
                      "and is immutable after creation (e.g. VIEW_STUDENTS, MANAGE_ROLES)."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Permission created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed on request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Permission with the same code or name already exists")
    })
    // @PreAuthorize("hasAuthority('MANAGE_PERMISSIONS')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Permission created successfully", response));
    }

    // ------------------------------------------------------------------
    // GET /api/v1/permissions
    // ------------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "Get All Permissions",
        description = "Retrieves a list of all active (non-deleted) permissions in the system."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
    })
    // @PreAuthorize("hasAuthority('VIEW_PERMISSIONS')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> response = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", response));
    }

    // ------------------------------------------------------------------
    // GET /api/v1/permissions/{id}
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(
        summary = "Get Permission By ID",
        description = "Retrieves the details of a single active permission using its UUID."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found")
    })
    // @PreAuthorize("hasAuthority('VIEW_PERMISSIONS')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(
            @Parameter(description = "UUID of the permission to retrieve", required = true)
            @PathVariable UUID id) {
        PermissionResponse response = permissionService.getPermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission retrieved successfully", response));
    }

    // ------------------------------------------------------------------
    // PUT /api/v1/permissions/{id}
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @Operation(
        summary = "Update Permission",
        description = "Updates an existing permission's name and description. " +
                      "The 'code' field is immutable and will not be changed by this operation."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed on request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found")
    })
    // @PreAuthorize("hasAuthority('MANAGE_PERMISSIONS')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @Parameter(description = "UUID of the permission to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", response));
    }

    // ------------------------------------------------------------------
    // DELETE /api/v1/permissions/{id}
    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Permission",
        description = "Soft-deletes a permission by marking it as deleted. " +
                      "The record is retained in the database for audit purposes."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Permission deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found")
    })
    // @PreAuthorize("hasAuthority('MANAGE_PERMISSIONS')")
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "UUID of the permission to delete", required = true)
            @PathVariable UUID id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
