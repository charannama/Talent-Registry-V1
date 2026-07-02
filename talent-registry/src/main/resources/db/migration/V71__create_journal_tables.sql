-- ===========================================================================
-- V9__create_journal_tables.sql
-- ===========================================================================
-- Purpose:
--   Creates the foundational audit persistence layer for the Talent Registry.
--   These tables form a generic audit trail system capable of tracking 
--   changes to any domain entity.
-- ===========================================================================

-- 1. Create journals table
-- Stores the high-level audit event (Who, What, When, Action)
CREATE TABLE journals (
    id BIGSERIAL PRIMARY KEY,
    journable_type VARCHAR(255) NOT NULL,
    journable_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_journal_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE'))
);

COMMENT ON TABLE journals IS 'Stores immutable audit events for all domain entities';
COMMENT ON COLUMN journals.id IS 'Primary key for the journal entry';
COMMENT ON COLUMN journals.journable_type IS 'Type of the entity being audited (e.g., User, Opening)';
COMMENT ON COLUMN journals.journable_id IS 'ID of the entity being audited';
COMMENT ON COLUMN journals.user_id IS 'UUID of the user who performed the action';
COMMENT ON COLUMN journals.action IS 'Action performed (CREATE, UPDATE, DELETE)';
COMMENT ON COLUMN journals.created_at IS 'Timestamp when the action occurred';

-- Indexes for journals
CREATE INDEX idx_journals_journable ON journals(journable_type, journable_id);
CREATE INDEX idx_journals_user_id ON journals(user_id);
CREATE INDEX idx_journals_created_at ON journals(created_at);

-- 2. Create journal_details table
-- Stores the field-level changes for a given audit event
CREATE TABLE journal_details (
    id BIGSERIAL PRIMARY KEY,
    journal_id BIGINT NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_journal_details_journal_id FOREIGN KEY (journal_id) REFERENCES journals(id) ON DELETE CASCADE
);

COMMENT ON TABLE journal_details IS 'Stores field-level changes for audit events';
COMMENT ON COLUMN journal_details.id IS 'Primary key for the journal detail entry';
COMMENT ON COLUMN journal_details.journal_id IS 'Foreign key to the journals table';
COMMENT ON COLUMN journal_details.field_name IS 'Name of the field that was modified';
COMMENT ON COLUMN journal_details.old_value IS 'Value of the field before the action';
COMMENT ON COLUMN journal_details.new_value IS 'Value of the field after the action';
COMMENT ON COLUMN journal_details.created_at IS 'Timestamp when the detail record was created';

-- Indexes for journal_details
CREATE INDEX idx_journal_details_journal_id ON journal_details(journal_id);
CREATE INDEX idx_journal_details_field_name ON journal_details(field_name);
CREATE INDEX idx_journal_details_created_at ON journal_details(created_at);
