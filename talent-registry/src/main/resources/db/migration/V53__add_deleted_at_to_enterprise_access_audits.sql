-- V53: Add missing deleted_at column inherited from BaseEntity

ALTER TABLE enterprise_access_audits
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
