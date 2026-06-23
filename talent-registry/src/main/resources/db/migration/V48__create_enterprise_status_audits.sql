CREATE TABLE enterprise_status_audits (
    id UUID PRIMARY KEY,
    enterprise_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    reason TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_esa_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_esa_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_esa_enterprise_id ON enterprise_status_audits (enterprise_id);
CREATE INDEX idx_esa_created_at ON enterprise_status_audits (created_at DESC);
