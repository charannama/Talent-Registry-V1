-- ===========================================================================
-- V91__seed_queue_configs.sql
-- ===========================================================================
-- Purpose:
--   Seeds the default queue configuration values to prevent ResourceNotFoundException
--   during scheduler polling.
-- ===========================================================================

INSERT INTO system_configs (id, config_key, config_value, data_type, description)
SELECT gen_random_uuid(), 'QUEUE.POLLING_ENABLED', 'true', 'BOOLEAN', 'Determines if the Task Scheduler should actively poll the database for pending jobs'
WHERE NOT EXISTS (SELECT 1 FROM system_configs WHERE config_key = 'QUEUE.POLLING_ENABLED');

INSERT INTO system_configs (id, config_key, config_value, data_type, description)
SELECT gen_random_uuid(), 'QUEUE.BATCH_SIZE', '50', 'INTEGER', 'The maximum number of tasks to pull from the queue in a single transaction'
WHERE NOT EXISTS (SELECT 1 FROM system_configs WHERE config_key = 'QUEUE.BATCH_SIZE');

INSERT INTO system_configs (id, config_key, config_value, data_type, description)
SELECT gen_random_uuid(), 'QUEUE.MAX_ATTEMPTS', '3', 'INTEGER', 'The maximum number of times a failing background task should be retried before marking it as permanently FAILED'
WHERE NOT EXISTS (SELECT 1 FROM system_configs WHERE config_key = 'QUEUE.MAX_ATTEMPTS');

INSERT INTO system_configs (id, config_key, config_value, data_type, description)
SELECT gen_random_uuid(), 'QUEUE.BACKOFF_STRATEGY', 'EXPONENTIAL', 'STRING', 'The backoff strategy for retrying tasks (EXPONENTIAL or FIXED)'
WHERE NOT EXISTS (SELECT 1 FROM system_configs WHERE config_key = 'QUEUE.BACKOFF_STRATEGY');
