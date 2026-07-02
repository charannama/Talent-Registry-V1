-- =============================================================================
-- V47__create_enterprise_profile_audits.sql
-- Create audit table for tracking enterprise profile modifications
-- =============================================================================

CREATE TABLE enterprise_profile_audits (
    id UUID PRIMARY KEY,
    enterprise_id UUID NOT NULL,
    previous_company_name VARCHAR(255),
    previous_domain_email VARCHAR(255),
    change_summary TEXT,
    updated_by UUID,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_accounts(id) ON DELETE CASCADE
);
