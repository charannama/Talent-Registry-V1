-- V64__opening_revision_workflow.sql
-- Add revision workflow columns to job_openings

ALTER TABLE job_openings
ADD COLUMN revision_requested_by UUID;
ALTER TABLE job_openings ADD COLUMN revision_requested_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE job_openings ADD COLUMN revision_feedback TEXT;
ALTER TABLE job_openings ADD COLUMN revision_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE job_openings ADD COLUMN last_resubmitted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE job_openings ADD COLUMN last_resubmitted_by UUID;

CREATE INDEX idx_job_openings_revision_count ON job_openings(revision_count);
