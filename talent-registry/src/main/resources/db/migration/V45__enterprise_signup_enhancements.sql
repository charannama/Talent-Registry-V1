-- =============================================================================
-- V45__enterprise_signup_enhancements.sql
-- Add missing fields to enterprise_accounts for Signup Flow
-- =============================================================================

ALTER TABLE enterprise_accounts
  ADD COLUMN IF NOT EXISTS registration_number VARCHAR(255),
  ADD COLUMN IF NOT EXISTS industry VARCHAR(255),
  ADD COLUMN IF NOT EXISTS company_description TEXT,
  ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(255),
  ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(255),
  ADD COLUMN IF NOT EXISTS city VARCHAR(100),
  ADD COLUMN IF NOT EXISTS state VARCHAR(100),
  ADD COLUMN IF NOT EXISTS country VARCHAR(100),
  ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20),
  ADD COLUMN IF NOT EXISTS hiring_manager_phone VARCHAR(50);
