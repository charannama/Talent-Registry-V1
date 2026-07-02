package com.zencube.registry.comment.mapper;

import com.zencube.registry.comment.dto.CommentResponse;
import com.zencube.registry.comment.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    private static final String DELETED_MESSAGE = "This comment has been removed.";

    /**
     * Maps a Comment entity to a CommentResponse recursively handling replies.
     */
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .body(comment.isDeleted() ? DELETED_MESSAGE : comment.getBody())
                .authorId(comment.getAuthorId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .deleted(comment.isDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(mapReplies(comment.getReplies()))
                .build();
    }

    private List<CommentResponse> mapReplies(List<Comment> replies) {
        if (replies == null || replies.isEmpty()) {
            return Collections.emptyList();
        }
        return replies.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
