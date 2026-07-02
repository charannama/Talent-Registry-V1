-- ============================================================================
-- V87: Create Calendar Events Table
-- ============================================================================

-- Drop the legacy tables from V1
DROP TABLE IF EXISTS calendar_event_attendees CASCADE;
DROP TABLE IF EXISTS calendar_events CASCADE;

CREATE TABLE IF NOT EXISTS calendar_events (
    id UUID PRIMARY KEY,
    
    -- Polymorphic Associations
    eventable_type VARCHAR(100) NOT NULL,
    eventable_id UUID NOT NULL,
    
    -- Event Data
    title VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Date/Time
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    
    -- Location
    location VARCHAR(500),
    
    -- Event Options
    all_day_event BOOLEAN DEFAULT FALSE,
    
    -- Category
    event_category VARCHAR(50) NOT NULL,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    
    -- Constraints
    CONSTRAINT chk_calendar_event_times CHECK (start_time < end_time),
    CONSTRAINT chk_calendar_event_title CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT chk_calendar_event_category CHECK (event_category IN ('INTERVIEW', 'ONBOARDING', 'FOLLOW_UP', 'INTERNAL'))
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_calendar_eventable ON calendar_events (eventable_type, eventable_id);
CREATE INDEX IF NOT EXISTS idx_calendar_start_time ON calendar_events (start_time);
CREATE INDEX IF NOT EXISTS idx_calendar_category ON calendar_events (event_category);
CREATE INDEX IF NOT EXISTS idx_calendar_timezone ON calendar_events (timezone);
