-- V65__add_opening_close_audit_fields.sql
-- Add audit fields for closing job openings

ALTER TABLE job_openings
ADD COLUMN closed_by UUID;
ALTER TABLE job_openings ADD COLUMN closed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE job_openings ADD COLUMN closure_reason TEXT;

CREATE INDEX idx_job_openings_closed_at ON job_openings(closed_at);
