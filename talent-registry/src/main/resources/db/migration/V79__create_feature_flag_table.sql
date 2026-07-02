-- ===========================================================================
-- V79__create_feature_flag_table.sql
-- ===========================================================================

DROP TABLE IF EXISTS feature_flags CASCADE;

CREATE TABLE feature_flags (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    flag_key VARCHAR(255) UNIQUE NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    applies_to VARCHAR(50) NOT NULL DEFAULT 'ALL',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_feature_flags_key ON feature_flags(flag_key);
CREATE INDEX idx_feature_flags_enabled ON feature_flags(enabled);
CREATE INDEX idx_feature_flags_applies ON feature_flags(applies_to);

-- Seed Data (as requested)
INSERT INTO feature_flags (flag_key, enabled, applies_to, description) VALUES
('AUTH_MODULE', true, 'ALL', 'Core Authentication Flow'),
('ENTERPRISE_MODULE', true, 'ALL', 'Enterprise Account Management'),
('OPENING_MODULE', true, 'ALL', 'Job Opening Postings'),
('APPLICATION_MODULE', true, 'ALL', 'Application Processing'),
('NOTIFICATION_MODULE', true, 'ALL', 'System Notification Engine'),
('SUCCESS_STORY_MODULE', true, 'ALL', 'Success Story Placements'),
('AUDIT_MODULE', true, 'ALL', 'Journal System Audits'),
('COMMENT_MODULE', true, 'ALL', 'Entity Collaboration Comments'),
('ATTACHMENT_MODULE', true, 'ALL', 'File Attachments (S3/Local)'),
('TAG_MODULE', true, 'ALL', 'Global Tagging & Taxonomies'),
('ACTIVITY_FEED_MODULE', true, 'ALL', 'Timeline and Activity History'),
('INTERVIEW_MODULE', false, 'HR', 'Interview Scheduling and Scoring'),
('AI_MATCHING_MODULE', false, 'ENTERPRISE', 'Automated Resume AI Matching');
