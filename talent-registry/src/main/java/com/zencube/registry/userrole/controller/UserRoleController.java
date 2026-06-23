package com.zencube.registry.userrole.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.userrole.dto.CreateUserRoleRequest;
import com.zencube.registry.userrole.dto.UserRoleResponse;
import com.zencube.registry.userrole.service.UserRoleService;
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

/**
 * 1. Purpose
 * Exposes REST endpoints for managing User-Role assignments.
 *
 * 2. Layer
 * Controller / API Layer.
 *
 * 4. Annotation Explanation
 * @RestController: Marks this as a REST endpoint handler returning JSON.
 * @RequestMapping: Base URL for these endpoints.
 * @Validated: Enables method-level validation.
 * @Tag: Swagger UI grouping.
 *
 * 5. Business Logic Explanation
 * Delegates all logic to the UserRoleService, wrapping outputs in standard ApiResponse payloads.
 *
 * 6. Best Practices
 * - No business logic resides in the controller.
 * - HTTP 201 Created used for successful assignments.
 * - HTTP 204 No Content used for successful revocations.
 */
@Validated
@RestController
@RequestMapping("/api/v1/user-roles")
@RequiredArgsConstructor
@Tag(name = "User Roles (RBAC)", description = "APIs for assigning roles to users")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping
    @Operation(summary = "Assign Role to User", description = "Creates a new mapping between a user and a role")
    public ResponseEntity<ApiResponse<UserRoleResponse>> assignRoleToUser(
            @Valid @RequestBody CreateUserRoleRequest request) {
        UserRoleResponse response = userRoleService.assignRoleToUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Role assigned successfully", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Roles by User", description = "Retrieves all roles currently assigned to a user")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getRolesByUser(
            @PathVariable UUID userId) {
        List<UserRoleResponse> responses = userRoleService.getRolesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", responses));
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Get Users by Role", description = "Retrieves all users holding a specific role")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getUsersByRole(
            @PathVariable UUID roleId) {
        List<UserRoleResponse> responses = userRoleService.getUsersByRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", responses));
    }

    @GetMapping
    @Operation(summary = "Get All Assignments", description = "Retrieves all active user-role assignments")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getAllMappings() {
        List<UserRoleResponse> responses = userRoleService.getAllMappings();
        return ResponseEntity.ok(ApiResponse.success("Mappings retrieved successfully", responses));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke Assignment", description = "Removes a specific user-role assignment")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable UUID id) {
        userRoleService.removeRoleFromUser(id);
        return ResponseEntity.noContent().build();
    }
}
