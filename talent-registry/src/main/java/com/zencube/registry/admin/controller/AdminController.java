package com.zencube.registry.admin.controller;

import com.zencube.registry.admin.service.AdminUserService;
import com.zencube.registry.common.constants.Constants;
import com.zencube.registry.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(Constants.API_V1 + "/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Administrative operations for users")
public class AdminController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Unlock account", description = "Manually unlocks a locked user account.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID userId) {
        adminUserService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked successfully."));
    }
}
