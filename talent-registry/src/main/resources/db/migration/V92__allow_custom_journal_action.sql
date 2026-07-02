-- ===========================================================================
-- V92__allow_custom_journal_action.sql
-- ===========================================================================
-- Purpose:
--   Update the chk_journal_action constraint to allow 'CUSTOM' action
--   for JournalAction.CUSTOM enum in AuditServiceImpl.
-- ===========================================================================

ALTER TABLE journals DROP CONSTRAINT IF EXISTS chk_journal_action;

ALTER TABLE journals ADD CONSTRAINT chk_journal_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'CUSTOM'));
