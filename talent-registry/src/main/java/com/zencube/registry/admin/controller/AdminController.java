package com.zencube.registry.admin.controller;

import com.zencube.registry.admin.dto.AdminResponse;
import com.zencube.registry.admin.dto.ConfigResponse;
import com.zencube.registry.admin.dto.FeatureFlagResponse;
import com.zencube.registry.admin.dto.UpdateConfigRequest;
import com.zencube.registry.admin.mapper.AdminMapper;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.config.dto.SystemConfigResponse;
import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.featureflag.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Administrative Configuration and Feature Flag APIs")
public class AdminController {

    private final ConfigService configService;
    private final FeatureFlagService featureFlagService;
    private final AdminMapper adminMapper;

    // ==========================================
    // SYSTEM CONFIGURATION ENDPOINTS
    // ==========================================

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all configurations")
    public ResponseEntity<List<ConfigResponse>> getAllConfigs() {
        List<ConfigResponse> configs = configService.getAll().stream()
                .map(adminMapper::toConfigResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(configs);
    }

    @PutMapping("/config/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update configuration")
    public ResponseEntity<AdminResponse<ConfigResponse>> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody UpdateConfigRequest request) {
        
        // 1. Validate key exists and retrieve existing settings (dataType, description)
        SystemConfigResponse existing = configService.getAll().stream()
                .filter(c -> c.getConfigKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Config key not found: " + key));

        // 2. Delegate to ConfigService to perform Cache Eviction and Audit Tracking natively
        configService.set(key, request.value(), existing.getDataType(), existing.getDescription());

        // 3. Return updated response
        ConfigResponse updated = adminMapper.toConfigResponse(
            configService.getAll().stream()
                .filter(c -> c.getConfigKey().equals(key))
                .findFirst()
                .orElse(existing)
        );

        return ResponseEntity.ok(new AdminResponse<>(true, "Configuration updated successfully", updated));
    }

    // ==========================================
    // FEATURE FLAG ENDPOINTS
    // ==========================================

    @GetMapping("/flags")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get feature flags")
    public ResponseEntity<List<FeatureFlagResponse>> getAllFlags() {
        List<FeatureFlagResponse> flags = featureFlagService.getAll().stream()
                .map(adminMapper::toFeatureFlagResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(flags);
    }

    @PutMapping("/flags/{key}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable feature flag")
    public ResponseEntity<AdminResponse<FeatureFlagResponse>> enableFeature(@PathVariable String key) {
        var result = featureFlagService.enable(key);
        return ResponseEntity.ok(new AdminResponse<>(true, "Feature enabled", adminMapper.toFeatureFlagResponse(result)));
    }

    @PutMapping("/flags/{key}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable feature flag")
    public ResponseEntity<AdminResponse<FeatureFlagResponse>> disableFeature(@PathVariable String key) {
        var result = featureFlagService.disable(key);
        return ResponseEntity.ok(new AdminResponse<>(true, "Feature disabled", adminMapper.toFeatureFlagResponse(result)));
    }
}
