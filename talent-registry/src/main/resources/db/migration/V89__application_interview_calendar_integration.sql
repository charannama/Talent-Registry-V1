-- Adds foreign key to link applications directly to their active interview calendar event

ALTER TABLE applications ADD COLUMN IF NOT EXISTS interview_event_id UUID;

ALTER TABLE applications
ADD CONSTRAINT fk_application_interview_event
FOREIGN KEY (interview_event_id)
REFERENCES calendar_events(id)
ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_application_interview_event ON applications(interview_event_id);
CREATE INDEX IF NOT EXISTS idx_application_status_event ON applications(status, interview_event_id);