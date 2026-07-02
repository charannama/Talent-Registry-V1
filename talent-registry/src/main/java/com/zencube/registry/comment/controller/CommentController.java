package com.zencube.registry.comment.controller;

import com.zencube.registry.comment.dto.CommentResponse;
import com.zencube.registry.comment.dto.CreateCommentRequest;
import com.zencube.registry.comment.dto.ReplyCommentRequest;
import com.zencube.registry.comment.dto.UpdateCommentRequest;
import com.zencube.registry.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Threaded discussion and comment APIs")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Add a root comment", description = "Creates a new top-level comment on an entity.")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(201).body(commentService.addComment(request));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "Reply to a comment", description = "Creates a nested reply to an existing comment.")
    public ResponseEntity<CommentResponse> replyToComment(
            @PathVariable UUID id, 
            @Valid @RequestBody ReplyCommentRequest request) {
        request.setParentCommentId(id);
        return ResponseEntity.status(201).body(commentService.replyToComment(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit a comment", description = "Updates the body of an existing comment. Requires Author or Admin.")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable UUID id, 
            @Valid @RequestBody UpdateCommentRequest request) {
        return ResponseEntity.ok(commentService.editComment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment", description = "Soft deletes a comment. Requires Author or Admin.")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entity/{type}/{id}")
    @Operation(summary = "Get comments for entity", description = "Retrieves all threaded comments for a specific entity.")
    public ResponseEntity<Page<CommentResponse>> getCommentsForEntity(
            @PathVariable String type,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
            
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(commentService.getCommentsForEntity(type, id, pageRequest));
    }
}
