-- V58__add_enterprise_analytics_indexes.sql
-- Add composite index for rapid status and date range filtering
CREATE INDEX idx_enterprise_status_created_at ON enterprise_accounts(onboarding_status, created_at);

-- Add index for average approval time calculations
CREATE INDEX idx_enterprise_approved_at ON enterprise_accounts(approved_at);
