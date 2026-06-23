-- V50__create_enterprise_access_audits.sql

CREATE TABLE enterprise_access_audits (
    id UUID PRIMARY KEY,
    enterprise_id UUID NOT NULL REFERENCES enterprise_accounts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    endpoint VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_enterprise_access_audits_ent_id ON enterprise_access_audits(enterprise_id);
CREATE INDEX idx_enterprise_access_audits_user_id ON enterprise_access_audits(user_id);
