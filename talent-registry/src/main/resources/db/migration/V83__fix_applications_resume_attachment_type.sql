-- ===========================================================================
-- V83__fix_applications_resume_attachment_type.sql
-- ===========================================================================
-- Purpose:
--   V72 dropped and recreated attachments table with BIGINT id,
--   but applications.resume_attachment_id remained UUID from V70.
--   This migration aligns the column type and restores the foreign key.
-- ===========================================================================

ALTER TABLE applications DROP CONSTRAINT IF EXISTS fk_applications_resume;

ALTER TABLE applications
    ALTER COLUMN resume_attachment_id TYPE BIGINT USING NULL;

ALTER TABLE applications
    ADD CONSTRAINT fk_applications_resume FOREIGN KEY (resume_attachment_id) REFERENCES attachments(id);
