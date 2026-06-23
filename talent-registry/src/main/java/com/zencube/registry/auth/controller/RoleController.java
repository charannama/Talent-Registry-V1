package com.zencube.registry.auth.controller;

import com.zencube.registry.auth.dto.role.CreateRoleRequest;
import com.zencube.registry.auth.dto.role.RoleResponse;
import com.zencube.registry.auth.dto.role.UpdateRoleRequest;
import com.zencube.registry.auth.service.interfaces.RoleService;
import com.zencube.registry.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(
    name = "Role Management",
    description = "Role and Authorization Management APIs"
)
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(
        summary = "Create Role",
        description = "Creates a new role in the system"
    )
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Role created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get Role By ID",
        description = "Retrieve role details using role ID"
    )
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(
            @PathVariable UUID id) {
        RoleResponse response = roleService.getRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", response));
    }

    @GetMapping
    @Operation(
        summary = "Get All Roles",
        description = "Retrieve all available roles"
    )
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update Role",
        description = "Update an existing role"
    )
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Role",
        description = "Delete a role from the system"
    )
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
