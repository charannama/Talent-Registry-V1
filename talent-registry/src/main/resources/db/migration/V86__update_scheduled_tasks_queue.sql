-- ===========================================================================
-- V86__update_scheduled_tasks_queue.sql
-- ===========================================================================

-- 1. Drop existing indexes to recreate them correctly
DROP INDEX IF EXISTS idx_scheduled_tasks_status;

-- Redundant: V84 already creates scheduled_tasks with the correct schema
-- ALTER TABLE scheduled_tasks RENAME COLUMN job_type TO task_type;
-- ALTER TABLE scheduled_tasks RENAME COLUMN status TO state;
-- ALTER TABLE scheduled_tasks RENAME COLUMN processed_at TO completed_at;
-- ALTER TABLE scheduled_tasks ADD COLUMN started_at TIMESTAMP WITH TIME ZONE;
-- ALTER TABLE scheduled_tasks ADD COLUMN max_attempts INTEGER NOT NULL DEFAULT 3;
-- ALTER TABLE scheduled_tasks ADD COLUMN next_attempt_at TIMESTAMP WITH TIME ZONE;

-- UPDATE scheduled_tasks SET next_attempt_at = scheduled_at WHERE next_attempt_at IS NULL;

-- 4. Create optimized indexes for the polling query
CREATE INDEX IF NOT EXISTS idx_scheduled_tasks_polling 
ON scheduled_tasks(state, scheduled_at, next_attempt_at);

-- CREATE INDEX idx_scheduled_tasks_state ON scheduled_tasks(state);
CREATE INDEX IF NOT EXISTS idx_scheduled_tasks_task_type ON scheduled_tasks(task_type);
CREATE INDEX IF NOT EXISTS idx_scheduled_tasks_created ON scheduled_tasks(created_at DESC);
