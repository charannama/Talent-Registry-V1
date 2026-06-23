-- V55: Add missing version column inherited from BaseEntity

ALTER TABLE enterprise_access_audits
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
