-- V56__add_unified_status_tracking.sql
ALTER TABLE enterprise_accounts
ADD COLUMN last_status_changed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE enterprise_accounts ADD COLUMN last_status_changed_by UUID REFERENCES users(id);
