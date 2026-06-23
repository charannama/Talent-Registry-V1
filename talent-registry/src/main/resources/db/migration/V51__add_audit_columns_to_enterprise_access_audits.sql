-- V51: Add missing audit columns inherited from BaseEntity

ALTER TABLE enterprise_access_audits
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255);
