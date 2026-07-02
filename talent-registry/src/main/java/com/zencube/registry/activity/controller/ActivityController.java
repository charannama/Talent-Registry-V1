package com.zencube.registry.activity.controller;

import com.zencube.registry.activity.dto.ActivityResponse;
import com.zencube.registry.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Feed", description = "Centralized Activity Tracking and Feeds")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Global Feed", description = "Returns the system-wide activity timeline. Requires ADMIN.")
    public ResponseEntity<Page<ActivityResponse>> getGlobalFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(activityService.getGlobalFeed(PageRequest.of(page, size)));
    }

    @GetMapping("/feed")
    @Operation(summary = "Get My Feed", description = "Returns a personalized timeline of events relevant to the currently authenticated user.")
    public ResponseEntity<Page<ActivityResponse>> getMyFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(activityService.getMyFeed(PageRequest.of(page, size)));
    }

    @GetMapping("/entity/{type}/{id}")
    @Operation(summary = "Get Entity Feed", description = "Returns the timeline of events affecting a specific entity.")
    public ResponseEntity<Page<ActivityResponse>> getEntityFeed(
            @PathVariable String type,
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(activityService.getEntityFeed(type, id, PageRequest.of(page, size)));
    }
}
