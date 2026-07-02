-- Add tracking fields to sessions table
ALTER TABLE sessions ADD COLUMN access_token_jti VARCHAR(255);
ALTER TABLE sessions ADD COLUMN last_activity_at TIMESTAMP WITH TIME ZONE;

-- Create indexes for session lookup and validation
CREATE INDEX idx_sessions_access_token_jti ON sessions(access_token_jti);
CREATE INDEX idx_sessions_is_deleted ON sessions(is_deleted);
CREATE INDEX idx_sessions_revoked_at ON sessions(revoked_at);
