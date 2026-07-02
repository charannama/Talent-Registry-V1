package com.zencube.registry.journal.controller;

import com.zencube.registry.journal.dto.JournalResponse;
import com.zencube.registry.journal.service.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Admin APIs for viewing enterprise audit logs")
public class AuditController {

    private final AuditQueryService auditQueryService;

    @GetMapping("/entity/{type}/{id}")
    @Operation(summary = "Get entity audit history", description = "Retrieves all audit events for a specific entity. Only accessible by ADMIN.")
    public ResponseEntity<Page<JournalResponse>> getEntityHistory(
            @PathVariable String type,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(auditQueryService.getEntityHistory(type, id, pageRequest));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user audit history", description = "Retrieves all audit events performed by a specific user. Only accessible by ADMIN.")
    public ResponseEntity<Page<JournalResponse>> getUserHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(auditQueryService.getUserHistory(userId, pageRequest));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent system activity", description = "Retrieves the latest audit events across the platform. Only accessible by ADMIN.")
    public ResponseEntity<Page<JournalResponse>> getRecentActivities(
            @RequestParam(defaultValue = "50") int limit) {
        
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(auditQueryService.getRecentActivities(pageRequest));
    }
}
