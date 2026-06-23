-- V39__add_profile_suspension_audit.sql

ALTER TABLE student_profiles
ADD COLUMN suspension_reason TEXT,
ADD COLUMN suspended_at TIMESTAMP,
ADD COLUMN suspended_by VARCHAR(255);
