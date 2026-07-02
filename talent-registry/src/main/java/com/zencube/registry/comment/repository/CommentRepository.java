package com.zencube.registry.comment.repository;

import com.zencube.registry.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Fetch root comments for an entity (parent is null)
    Page<Comment> findByCommentableTypeAndCommentableIdAndParentIsNull(String commentableType, UUID commentableId, Pageable pageable);

    // Fetch replies for a specific comment
    List<Comment> findByParentId(UUID parentId);

    // Fetch all comments by a specific author
    Page<Comment> findByAuthorId(UUID authorId, Pageable pageable);
}
