-- ===========================================================================
-- V72__create_attachment_table.sql
-- ===========================================================================
-- Purpose:
--   Creates the polymorphic attachments table for centralized file management.
-- ===========================================================================

DROP TABLE IF EXISTS attachments CASCADE;

CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    attachable_type VARCHAR(255) NOT NULL,
    attachable_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Comments
COMMENT ON TABLE attachments IS 'Centralized polymorphic table for all system file attachments';
COMMENT ON COLUMN attachments.id IS 'Primary key';
COMMENT ON COLUMN attachments.attachable_type IS 'Logical name of the parent entity (e.g., OPENING, APPLICATION)';
COMMENT ON COLUMN attachments.attachable_id IS 'ID of the parent entity';
COMMENT ON COLUMN attachments.filename IS 'Original name of the uploaded file';
COMMENT ON COLUMN attachments.content_type IS 'MIME type of the file';
COMMENT ON COLUMN attachments.size IS 'Size of the file in bytes';
COMMENT ON COLUMN attachments.storage_path IS 'Physical or logical path to the file in the storage backend';
COMMENT ON COLUMN attachments.uploaded_by IS 'UUID of the user who uploaded the file';
COMMENT ON COLUMN attachments.created_at IS 'Timestamp of upload';

-- Indexes
CREATE INDEX idx_attachments_attachable ON attachments(attachable_type, attachable_id);
CREATE INDEX idx_attachments_uploaded_by ON attachments(uploaded_by);
