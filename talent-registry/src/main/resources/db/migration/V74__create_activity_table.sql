-- ===========================================================================
-- V74__create_activity_table.sql
-- ===========================================================================
-- Purpose:
--   Creates the dual-polymorphic activity feed table.
-- ===========================================================================

DROP TABLE IF EXISTS activities CASCADE;

CREATE TABLE activities (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    
    -- Actor (Who did it)
    trackable_type VARCHAR(255) NOT NULL,
    trackable_id VARCHAR(255) NOT NULL,
    
    -- Target (What was affected)
    target_type VARCHAR(255) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    
    -- Event Definition
    activity_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    
    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Comments
COMMENT ON TABLE activities IS 'Dual polymorphic activity feed tracking business events';
COMMENT ON COLUMN activities.trackable_type IS 'Logical name of the actor entity (e.g., USER, SYSTEM)';
COMMENT ON COLUMN activities.trackable_id IS 'ID of the actor entity (stored as string to support both UUID and Long)';
COMMENT ON COLUMN activities.target_type IS 'Logical name of the affected entity (e.g., APPLICATION, COMMENT)';
COMMENT ON COLUMN activities.target_id IS 'ID of the affected entity (stored as string to support both UUID and Long)';
COMMENT ON COLUMN activities.activity_type IS 'Enum mapping of the event (e.g., APPLIED, REVIEWED)';
COMMENT ON COLUMN activities.description IS 'Pre-rendered description string to avoid joins on read';

-- Indexes for feed queries
CREATE INDEX idx_activities_created_at ON activities(created_at DESC);
CREATE INDEX idx_activities_trackable ON activities(trackable_type, trackable_id);
CREATE INDEX idx_activities_target ON activities(target_type, target_id);
