-- Add audit columns to email_verification_tokens to match BaseEntity

ALTER TABLE email_verification_tokens ADD COLUMN created_by VARCHAR(255);
ALTER TABLE email_verification_tokens ADD COLUMN updated_by VARCHAR(255);
ALTER TABLE email_verification_tokens ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE email_verification_tokens ADD COLUMN deleted_by VARCHAR(255);
