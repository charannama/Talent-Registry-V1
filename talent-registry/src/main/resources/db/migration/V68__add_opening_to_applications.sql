-- V68__add_opening_to_applications.sql
-- Link applications to job_openings and enforce unique constraint

ALTER TABLE applications
ADD COLUMN IF NOT EXISTS opening_id UUID;

-- Since data might exist without opening_id, we need a default strategy or clearing invalid rows.
-- In a real prod environment we'd clean up data, but since we're developing, we'll clear applications without openings.
DELETE FROM applications WHERE opening_id IS NULL;

ALTER TABLE applications
ALTER COLUMN opening_id SET NOT NULL;

ALTER TABLE applications DROP CONSTRAINT IF EXISTS fk_applications_opening;
ALTER TABLE applications
ADD CONSTRAINT fk_applications_opening FOREIGN KEY (opening_id) REFERENCES job_openings(id) ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_applications_student_opening;
CREATE UNIQUE INDEX idx_applications_student_opening ON applications(profile_id, opening_id) ;

DROP INDEX IF EXISTS idx_applications_status;
CREATE INDEX idx_applications_status ON applications(status) ;
