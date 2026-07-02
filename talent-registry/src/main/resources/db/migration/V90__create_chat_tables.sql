-- V90__create_chat_tables.sql
-- Chat Module Foundation

-- Drop legacy chat tables from V1
DROP TABLE IF EXISTS chat_messages CASCADE;
DROP TABLE IF EXISTS chat_thread_participants CASCADE;
DROP TABLE IF EXISTS chat_threads CASCADE;

-- 1. chat_threads table
CREATE TABLE chat_threads (
    id UUID PRIMARY KEY,
    thread_type VARCHAR(50) NOT NULL,
    contextable_type VARCHAR(100),
    contextable_id UUID,
    creator_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_chat_thread_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_thread_type ON chat_threads(thread_type);
CREATE INDEX idx_chat_thread_context ON chat_threads(contextable_type, contextable_id);
CREATE INDEX idx_chat_thread_creator ON chat_threads(creator_id);

-- 2. chat_participants table
CREATE TABLE chat_participants (
    id UUID PRIMARY KEY,
    thread_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    last_read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_chat_participant_thread FOREIGN KEY (thread_id) REFERENCES chat_threads(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_participant_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_chat_participant_thread_user UNIQUE (thread_id, user_id)
);

CREATE INDEX idx_chat_participant_thread ON chat_participants(thread_id);
CREATE INDEX idx_chat_participant_user ON chat_participants(user_id);
CREATE INDEX idx_chat_participant_last_read ON chat_participants(last_read_at);

-- 3. chat_messages table
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    thread_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_chat_message_thread FOREIGN KEY (thread_id) REFERENCES chat_threads(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_message_thread ON chat_messages(thread_id);
CREATE INDEX idx_chat_message_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_message_sent_at ON chat_messages(sent_at DESC);
CREATE INDEX idx_chat_message_deleted ON chat_messages(deleted_at);
