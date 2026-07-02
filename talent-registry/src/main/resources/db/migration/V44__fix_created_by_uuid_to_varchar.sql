-- =============================================================================
-- V44__fix_created_by_uuid_to_varchar.sql
-- Fixes the data types for created_by and updated_by in student profile tables
-- to match the BaseEntity requirement of VARCHAR(255).
-- =============================================================================

ALTER TABLE student_profiles
  ALTER COLUMN created_by TYPE VARCHAR(255);
ALTER TABLE student_profiles ALTER COLUMN updated_by TYPE VARCHAR(255);

ALTER TABLE student_skills
  ALTER COLUMN created_by TYPE VARCHAR(255);
ALTER TABLE student_skills ALTER COLUMN updated_by TYPE VARCHAR(255);
