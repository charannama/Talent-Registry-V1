-- V64__opening_revision_workflow.sql
-- Add revision workflow columns to job_openings

ALTER TABLE job_openings
ADD COLUMN revision_requested_by UUID,
ADD COLUMN revision_requested_at TIMESTAMPTZ,
ADD COLUMN revision_feedback TEXT,
ADD COLUMN revision_count INTEGER NOT NULL DEFAULT 0,
ADD COLUMN last_resubmitted_at TIMESTAMPTZ,
ADD COLUMN last_resubmitted_by UUID;

CREATE INDEX idx_job_openings_revision_count ON job_openings(revision_count);
