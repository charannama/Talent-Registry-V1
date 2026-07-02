-- ===========================================================================
-- V94__fix_polymorphic_ids_to_uuid.sql
-- ===========================================================================
-- Purpose:
--   Convert attachable_id and commentable_id from BIGINT to UUID.
--   Also converts Attachment id from BIGINT to UUID.
--   Drops applications fk, updates column type, and recreates fk.
-- ===========================================================================

-- 1. Drop existing applications foreign key since attachments is changing its PK type
ALTER TABLE applications DROP CONSTRAINT IF EXISTS fk_applications_resume;

-- 2. Drop the tables completely since we are fundamentally changing the types and don't care about seeded test data
DROP TABLE IF EXISTS attachments CASCADE;
DROP TABLE IF EXISTS comments CASCADE;

-- 3. Recreate attachments with UUIDs
CREATE TABLE attachments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    attachable_type VARCHAR(255) NOT NULL,
    attachable_id UUID NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for attachments
CREATE INDEX idx_attachments_attachable ON attachments(attachable_type, attachable_id);
CREATE INDEX idx_attachments_uploaded_by ON attachments(uploaded_by);

-- Restore applications FK
ALTER TABLE applications
    ALTER COLUMN resume_attachment_id TYPE UUID USING resume_attachment_id::text::uuid;
    
ALTER TABLE applications
    ADD CONSTRAINT fk_applications_resume FOREIGN KEY (resume_attachment_id) REFERENCES attachments(id);

-- 4. Recreate comments with UUIDs
CREATE TABLE comments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    commentable_type VARCHAR(255) NOT NULL,
    commentable_id UUID NOT NULL,
    author_id UUID NOT NULL,
    body TEXT NOT NULL,
    parent_id UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE CASCADE
);

-- Indexes for comments
CREATE INDEX idx_comments_commentable ON comments(commentable_type, commentable_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
CREATE INDEX idx_comments_author ON comments(author_id);
