package com.zencube.registry.comment.service;

import com.zencube.registry.comment.dto.CommentResponse;
import com.zencube.registry.comment.dto.CreateCommentRequest;
import com.zencube.registry.comment.dto.ReplyCommentRequest;
import com.zencube.registry.comment.dto.UpdateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentService {
    
    CommentResponse addComment(CreateCommentRequest request);
    
    CommentResponse replyToComment(ReplyCommentRequest request);
    
    CommentResponse editComment(UUID commentId, UpdateCommentRequest request);
    
    void deleteComment(UUID commentId);
    
    Page<CommentResponse> getCommentsForEntity(String commentableType, UUID commentableId, Pageable pageable);
}
