-- =============================================================================
-- V46__enterprise_registration_alignment.sql
-- Align enterprise_accounts table with RFC specifications
-- =============================================================================

-- 1. Alter Column Types and Lengths
ALTER TABLE enterprise_accounts
    ALTER COLUMN company_website TYPE VARCHAR(500);
ALTER TABLE enterprise_accounts ALTER COLUMN company_size TYPE VARCHAR(30);
ALTER TABLE enterprise_accounts ALTER COLUMN gst_number TYPE VARCHAR(20);
ALTER TABLE enterprise_accounts ALTER COLUMN sector TYPE VARCHAR(100);
ALTER TABLE enterprise_accounts ALTER COLUMN onboarding_status TYPE VARCHAR(30);

-- Cast onboarded_by and rejected_by from VARCHAR to UUID
ALTER TABLE enterprise_accounts
    ALTER COLUMN onboarded_by TYPE UUID USING onboarded_by::uuid;
ALTER TABLE enterprise_accounts ALTER COLUMN rejected_by TYPE UUID USING rejected_by::uuid;

-- 2. Add Missing Indexes
CREATE INDEX IF NOT EXISTS idx_enterprise_accounts_company_name 
    ON enterprise_accounts (company_name);

CREATE INDEX IF NOT EXISTS idx_enterprise_accounts_domain_email 
    ON enterprise_accounts (domain_email);
