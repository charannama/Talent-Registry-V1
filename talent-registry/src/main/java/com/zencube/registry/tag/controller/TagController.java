package com.zencube.registry.tag.controller;

import com.zencube.registry.tag.dto.CreateTagRequest;
import com.zencube.registry.tag.dto.TagResponse;
import com.zencube.registry.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Global classification and taxonomy APIs")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Tag", description = "Creates a new reusable tag. Requires ADMIN.")
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(201).body(tagService.createTag(request));
    }

    @GetMapping
    @Operation(summary = "List Tags", description = "Retrieves all system tags. Optionally filter by category.")
    public ResponseEntity<List<TagResponse>> getTags(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(tagService.getTagsByCategory(category));
        }
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping("/entity/{type}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign Tag", description = "Attaches a tag to a target entity. Requires ADMIN.")
    public ResponseEntity<Void> tagEntity(
            @PathVariable String type,
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
            
        UUID tagId = UUID.fromString(payload.get("tagId"));
        tagService.tagEntity(type, id, tagId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/entity/{type}/{id}/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove Tag", description = "Removes a tag from a target entity. Requires ADMIN.")
    public ResponseEntity<Void> untagEntity(
            @PathVariable String type,
            @PathVariable String id,
            @PathVariable UUID tagId) {
            
        tagService.untagEntity(type, id, tagId);
        return ResponseEntity.ok().build(); // Standard 200 OK as per prompt requirements
    }

    @GetMapping("/entity/{type}/{id}")
    @Operation(summary = "Get Entity Tags", description = "Retrieves all tags attached to a specific entity.")
    public ResponseEntity<List<TagResponse>> getEntityTags(
            @PathVariable String type,
            @PathVariable String id) {
            
        return ResponseEntity.ok(tagService.getTagsForEntity(type, id));
    }
}
