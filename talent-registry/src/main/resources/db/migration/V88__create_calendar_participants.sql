-- ============================================================================
-- V88: Create Calendar Participants Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS calendar_participants (
    id UUID PRIMARY KEY,
    
    -- Relationships
    event_id UUID NOT NULL,
    user_id UUID,
    
    -- External Reference
    external_email VARCHAR(255),
    
    -- Status and Types
    participant_type VARCHAR(50) NOT NULL,
    response_status VARCHAR(50) NOT NULL,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    
    -- Foreign Keys
    CONSTRAINT fk_calendar_participants_event FOREIGN KEY (event_id) REFERENCES calendar_events(id) ON DELETE CASCADE,
    CONSTRAINT fk_calendar_participants_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Check Constraints
    CONSTRAINT chk_calendar_participant_exclusive CHECK (
        (participant_type = 'INTERNAL' AND user_id IS NOT NULL AND external_email IS NULL) OR
        (participant_type = 'EXTERNAL' AND external_email IS NOT NULL AND user_id IS NULL)
    ),
    CONSTRAINT chk_calendar_participant_email CHECK (
        external_email IS NULL OR external_email LIKE '%@%.%'
    )
);

-- Unique Constraints to prevent duplicate invites
CREATE UNIQUE INDEX IF NOT EXISTS uq_calendar_participant_internal ON calendar_participants (event_id, user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_calendar_participant_external ON calendar_participants (event_id, external_email);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_calendar_participant_event ON calendar_participants (event_id);
CREATE INDEX IF NOT EXISTS idx_calendar_participant_user ON calendar_participants (user_id);
CREATE INDEX IF NOT EXISTS idx_calendar_participant_status ON calendar_participants (response_status);
CREATE INDEX IF NOT EXISTS idx_calendar_participant_type ON calendar_participants (participant_type);
