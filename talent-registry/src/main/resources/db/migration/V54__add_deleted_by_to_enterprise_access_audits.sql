-- V54: Add missing deleted_by column inherited from BaseEntity

ALTER TABLE enterprise_access_audits
ADD COLUMN deleted_by VARCHAR(255);
