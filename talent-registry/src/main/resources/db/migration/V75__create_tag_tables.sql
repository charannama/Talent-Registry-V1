-- ===========================================================================
-- V75__create_tag_tables.sql
-- ===========================================================================
-- Purpose:
--   Creates tags dictionary and polymorphic taggings junction table.
-- ===========================================================================

DROP TABLE IF EXISTS taggings CASCADE;
DROP TABLE IF EXISTS tags CASCADE;

CREATE TABLE tags (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE taggings (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    tag_id UUID NOT NULL,
    taggable_type VARCHAR(255) NOT NULL,
    taggable_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_taggings_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE,
    CONSTRAINT uq_tagging UNIQUE (tag_id, taggable_type, taggable_id)
);

-- Comments
COMMENT ON TABLE tags IS 'Dictionary of standard tags available in the system';
COMMENT ON TABLE taggings IS 'Polymorphic junction mapping tags to any entity';
COMMENT ON COLUMN taggings.taggable_id IS 'Stored as string to support both UUID and BIGINT entities';

-- Indexes
CREATE INDEX idx_tags_category ON tags(category);
CREATE INDEX idx_taggings_taggable ON taggings(taggable_type, taggable_id);
