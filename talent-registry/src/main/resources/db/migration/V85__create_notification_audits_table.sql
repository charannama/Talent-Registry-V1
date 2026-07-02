CREATE TABLE notification_audits (
    id UUID PRIMARY KEY,
    notification_id UUID,
    actor_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    details VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_audits_notification_id ON notification_audits(notification_id);
CREATE INDEX idx_notification_audits_actor_id ON notification_audits(actor_id);
