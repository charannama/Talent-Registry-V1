-- ===========================================================================
-- V73__create_comment_table.sql
-- ===========================================================================
-- Purpose:
--   Creates the polymorphic comments table for threaded discussions.
-- ===========================================================================

DROP TABLE IF EXISTS comments CASCADE;

CREATE TABLE comments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    commentable_type VARCHAR(255) NOT NULL,
    commentable_id BIGINT NOT NULL,
    author_id UUID NOT NULL,
    body TEXT NOT NULL,
    parent_id UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE CASCADE
);

-- Comments
COMMENT ON TABLE comments IS 'Centralized polymorphic table for threaded comments';
COMMENT ON COLUMN comments.commentable_type IS 'Logical name of the parent entity (e.g., OPENING, APPLICATION)';
COMMENT ON COLUMN comments.commentable_id IS 'ID of the parent entity';
COMMENT ON COLUMN comments.author_id IS 'UUID of the user who authored the comment';
COMMENT ON COLUMN comments.parent_id IS 'Self-referencing foreign key for threaded replies';
COMMENT ON COLUMN comments.is_deleted IS 'Soft delete flag for moderation';

-- Indexes
CREATE INDEX idx_comments_commentable ON comments(commentable_type, commentable_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
CREATE INDEX idx_comments_author ON comments(author_id);
