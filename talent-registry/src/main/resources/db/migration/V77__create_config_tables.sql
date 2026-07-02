-- ===========================================================================
-- V77__create_config_tables.sql
-- ===========================================================================
-- Purpose:
--   Creates system_configs table to act as a dynamic key-value store
--   for application settings.
-- ===========================================================================

DROP TABLE IF EXISTS system_configs CASCADE;

CREATE TABLE system_configs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    
    -- Config key (e.g., AUTH.MAX_LOGIN_ATTEMPTS)
    config_key VARCHAR(255) NOT NULL UNIQUE,
    
    -- Config value (Stored as TEXT, can hold JSON or strings)
    config_value TEXT NOT NULL,
    
    -- Config Type (STRING, INTEGER, BOOLEAN, JSON)
    data_type VARCHAR(50) NOT NULL,
    
    -- Optional human-readable description
    description TEXT,
    
    -- Audit Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Comments
COMMENT ON TABLE system_configs IS 'Centralized Key-Value runtime configuration store';
COMMENT ON COLUMN system_configs.config_key IS 'Unique dot-notation key representing the config (e.g. AUTH.MAX_LOGIN_ATTEMPTS)';
COMMENT ON COLUMN system_configs.config_value IS 'The physical value to be type-cast by the service layer';

-- Indexes
CREATE INDEX idx_system_configs_key ON system_configs(config_key);
-- Provide an index specifically optimized for prefix searching (e.g., LIKE 'AUTH.%')
CREATE INDEX idx_system_configs_key_prefix ON system_configs(config_key);
