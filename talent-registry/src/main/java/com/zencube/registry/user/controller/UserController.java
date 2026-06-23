package com.zencube.registry.user.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.user.dto.CreateUserRequest;
import com.zencube.registry.user.dto.UpdateUserRequest;
import com.zencube.registry.user.dto.UserAdminResponse;
import com.zencube.registry.user.service.UserService;
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
 * REST controller for admin-facing User Management.
 * Provides full CRUD capabilities over user accounts.
 */
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
    name = "User Management",
    description = "Admin APIs for managing user accounts and lifecycles"
)
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
        summary = "Create User",
        description = "Creates a new user account programmatically (Admin only)"
    )
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserAdminResponse response = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("User created successfully", response));
    }

    @GetMapping
    @Operation(
        summary = "Get All Users",
        description = "Retrieves all active user accounts in the system"
    )
    public ResponseEntity<ApiResponse<List<UserAdminResponse>>> getAllUsers() {
        List<UserAdminResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get User By ID",
        description = "Retrieves details of a specific user account"
    )
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUser(
            @PathVariable UUID id) {
        UserAdminResponse response = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update User",
        description = "Partially updates an existing user account. Only non-null fields are applied."
    )
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserAdminResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete User",
        description = "Soft-deletes a user account. The account is deactivated and removed from active queries."
    )
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
