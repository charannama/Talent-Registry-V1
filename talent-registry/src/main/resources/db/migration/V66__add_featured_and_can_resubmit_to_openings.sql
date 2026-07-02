-- V66__add_featured_and_can_resubmit_to_openings.sql
-- Add featured and canResubmit fields

ALTER TABLE job_openings
ADD COLUMN featured BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE job_openings ADD COLUMN can_resubmit BOOLEAN;

CREATE INDEX idx_job_openings_featured ON job_openings(featured);
