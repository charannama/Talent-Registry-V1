package com.zencube.registry.profile.controller;

import com.zencube.registry.profile.dto.ProfileAccessAuditDTO;
import com.zencube.registry.profile.enums.AccessResult;
import com.zencube.registry.profile.service.ProfileAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/profile-access")
@RequiredArgsConstructor
@Tag(name = "Admin Profile Access", description = "Admin API for reviewing profile access audit logs")
public class AdminProfileAccessController {

    private final ProfileAuditService profileAuditService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROFILE_VIEW_ALL')")
    @Operation(summary = "Search profile access audit logs", description = "Allows administrators and HR to search audit logs of profile accesses")
    public Page<ProfileAccessAuditDTO> searchAudits(
            @RequestParam(required = false) UUID viewerUserId,
            @RequestParam(required = false) UUID targetUserId,
            @RequestParam(required = false) AccessResult result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Pageable pageable) {
        
        return profileAuditService.searchAudits(viewerUserId, targetUserId, result, startDate, endDate, pageable);
    }
}
