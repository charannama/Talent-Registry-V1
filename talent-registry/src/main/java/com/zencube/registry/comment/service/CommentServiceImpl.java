package com.zencube.registry.comment.service;

import com.zencube.registry.comment.dto.CommentResponse;
import com.zencube.registry.comment.dto.CreateCommentRequest;
import com.zencube.registry.comment.dto.ReplyCommentRequest;
import com.zencube.registry.comment.dto.UpdateCommentRequest;
import com.zencube.registry.comment.entity.Comment;
import com.zencube.registry.comment.mapper.CommentMapper;
import com.zencube.registry.comment.repository.CommentRepository;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    @Audited(action = JournalAction.CREATE, entityType = "COMMENT", idParam = "request.commentableId")
    public CommentResponse addComment(CreateCommentRequest request) {
        log.info("Adding root comment for {} #{}", request.getCommentableType(), request.getCommentableId());
        
        Comment comment = Comment.builder()
                .commentableType(request.getCommentableType())
                .commentableId(request.getCommentableId())
                .body(request.getBody())
                .authorId(getCurrentUserId())
                .isDeleted(false)
                .build();
                
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.CREATE, entityType = "COMMENT_REPLY", idParam = "request.parentCommentId")
    public CommentResponse replyToComment(ReplyCommentRequest request) {
        log.info("Replying to comment {}", request.getParentCommentId());
        
        Comment parent = findCommentOrThrow(request.getParentCommentId());
        
        Comment reply = Comment.builder()
                .commentableType(parent.getCommentableType())
                .commentableId(parent.getCommentableId())
                .body(request.getBody())
                .authorId(getCurrentUserId())
                .isDeleted(false)
                .build();
                
        parent.addReply(reply);
        commentRepository.save(parent); // cascades to reply
        
        // Return just the newly created reply for simplicity
        return commentMapper.toResponse(reply);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.UPDATE, entityType = "COMMENT")
    public CommentResponse editComment(UUID commentId, UpdateCommentRequest request) {
        log.info("Editing comment {}", commentId);
        
        Comment comment = findCommentOrThrow(commentId);
        verifyAuthorOrAdmin(comment.getAuthorId());
        
        if (comment.isDeleted()) {
            throw new IllegalStateException("Cannot edit a deleted comment");
        }
        
        comment.setBody(request.getBody());
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.DELETE, entityType = "COMMENT")
    public void deleteComment(UUID commentId) {
        log.info("Soft deleting comment {}", commentId);
        
        Comment comment = findCommentOrThrow(commentId);
        verifyAuthorOrAdmin(comment.getAuthorId());
        
        // Soft delete strategy
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForEntity(String commentableType, UUID commentableId, Pageable pageable) {
        return commentRepository.findByCommentableTypeAndCommentableIdAndParentIsNull(commentableType, commentableId, pageable)
                .map(commentMapper::toResponse);
    }
    
    private Comment findCommentOrThrow(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }
    
    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new SecurityException("Authenticated user required for comments");
        }
        return UUID.fromString(auth.getName());
    }
    
    private void verifyAuthorOrAdmin(UUID authorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isAuthor = auth.getName().equals(authorId.toString());
        
        if (!isAdmin && !isAuthor) {
            throw new AccessDeniedException("Only the author or an administrator can modify this comment");
        }
    }
}
