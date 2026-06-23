-- V52: Add missing is_deleted column inherited from BaseEntity

ALTER TABLE enterprise_access_audits
ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE NOT NULL;
