-- V61__opening_approval_fields.sql
-- Add audit and visibility fields to job openings, and migrate statuses

ALTER TABLE job_openings
ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE job_openings ADD COLUMN approved_by UUID;
ALTER TABLE job_openings ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE;

-- Migrate existing statuses to match new names
UPDATE job_openings SET status = 'PENDING_APPROVAL' WHERE status = 'PENDING_HR_APPROVAL';
UPDATE job_openings SET status = 'LIVE' WHERE status = 'APPROVED';

-- Indexes for performance
CREATE INDEX idx_job_openings_published_at ON job_openings(published_at) ;
CREATE INDEX idx_job_openings_created_at ON job_openings(created_at) ;
