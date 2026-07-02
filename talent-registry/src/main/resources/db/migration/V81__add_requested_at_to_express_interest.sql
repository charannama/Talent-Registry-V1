-- ===========================================================================
-- V81__add_requested_at_to_express_interest.sql
-- ===========================================================================

ALTER TABLE express_interests ADD COLUMN requested_at TIMESTAMP WITH TIME ZONE;
