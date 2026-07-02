-- V39__add_profile_suspension_audit.sql

ALTER TABLE student_profiles
ADD COLUMN suspension_reason TEXT;
ALTER TABLE student_profiles ADD COLUMN suspended_at TIMESTAMP;
ALTER TABLE student_profiles ADD COLUMN suspended_by VARCHAR(255);
