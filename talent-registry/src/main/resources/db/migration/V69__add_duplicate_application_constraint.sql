-- Add unique constraint to applications table to prevent duplicate applications for the same opening
ALTER TABLE applications
ADD CONSTRAINT uk_application_profile_opening
UNIQUE (profile_id, opening_id);
