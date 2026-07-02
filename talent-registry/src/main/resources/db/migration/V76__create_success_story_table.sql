-- ===========================================================================
-- V76__create_success_story_table.sql
-- ===========================================================================
-- Purpose:
--   Creates success_stories table to store historical placement records.
-- ===========================================================================

DROP TABLE IF EXISTS success_stories CASCADE;

CREATE TABLE success_stories (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    
    -- Link to original application (ensures 1:1 mapping)
    application_id UUID NOT NULL UNIQUE,
    
    -- Historical Snapshots
    student_name VARCHAR(255) NOT NULL,
    enterprise_name VARCHAR(255) NOT NULL,
    opening_title VARCHAR(255) NOT NULL,
    
    -- Business Data
    selected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    testimonial TEXT,
    
    -- Visibility Flags
    is_featured BOOLEAN DEFAULT FALSE NOT NULL,
    is_public BOOLEAN DEFAULT TRUE NOT NULL,
    
    -- Audit Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Comments
COMMENT ON TABLE success_stories IS 'Historical snapshot of a successful candidate placement';
COMMENT ON COLUMN success_stories.application_id IS 'Unique link back to the originating application';
COMMENT ON COLUMN success_stories.student_name IS 'Immutable snapshot of the student name at time of selection';
COMMENT ON COLUMN success_stories.enterprise_name IS 'Immutable snapshot of the enterprise name at time of selection';
COMMENT ON COLUMN success_stories.opening_title IS 'Immutable snapshot of the opening title at time of selection';

-- Indexes
CREATE INDEX idx_success_story_featured ON success_stories(is_featured);
CREATE INDEX idx_success_story_public ON success_stories(is_public);
