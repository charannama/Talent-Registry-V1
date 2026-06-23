-- =============================================================================
-- V1__initial_schema.sql
-- Initial schema: core domain tables for Talent Registry
-- Covers: users, students, enterprises, openings, applications,
--         pipeline, express_interest, chat, calendar, notifications,
--         attachments, comments, activity, tags, success_stories,
--         feature_flags, journal
-- =============================================================================

-- ---------------------------------------------------------------------------
-- ENUM TYPES  (PostgreSQL native enums matching Java enum classes)
-- ---------------------------------------------------------------------------

CREATE TYPE gender AS ENUM (
    'MALE', 'FEMALE', 'NON_BINARY', 'PREFER_NOT_TO_SAY', 'OTHER'
);

CREATE TYPE status AS ENUM (
    'ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'ARCHIVED', 'DELETED'
);

CREATE TYPE employment_type AS ENUM (
    'FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP',
    'FREELANCE', 'TEMPORARY', 'APPRENTICESHIP', 'VOLUNTEER'
);

CREATE TYPE work_mode AS ENUM (
    'ON_SITE', 'REMOTE', 'HYBRID'
);

CREATE TYPE application_status AS ENUM (
    'SUBMITTED', 'UNDER_REVIEW', 'SHORTLISTED',
    'ASSESSMENT_SENT', 'ASSESSMENT_COMPLETED',
    'INTERVIEW_SCHEDULED', 'INTERVIEW_COMPLETED',
    'OFFER_EXTENDED', 'OFFER_ACCEPTED', 'OFFER_REJECTED',
    'REJECTED', 'WITHDRAWN', 'ONBOARDED'
);

CREATE TYPE attachment_type AS ENUM (
    'RESUME', 'COVER_LETTER', 'PORTFOLIO', 'CERTIFICATE',
    'TRANSCRIPT', 'PROFILE_PHOTO', 'COMPANY_LOGO',
    'OFFER_LETTER', 'ASSESSMENT_FILE', 'OTHER'
);

CREATE TYPE notification_channel AS ENUM (
    'EMAIL', 'IN_APP', 'SMS', 'PUSH'
);

CREATE TYPE notification_type AS ENUM (
    'APPLICATION_RECEIVED', 'APPLICATION_STATUS_CHANGED', 'APPLICATION_WITHDRAWN',
    'INTERVIEW_SCHEDULED', 'INTERVIEW_REMINDER', 'INTERVIEW_CANCELLED',
    'ASSESSMENT_ASSIGNED', 'ASSESSMENT_REMINDER',
    'OFFER_EXTENDED', 'OFFER_ACCEPTED', 'OFFER_REJECTED',
    'EXPRESS_INTEREST_RECEIVED', 'PIPELINE_STAGE_CHANGED',
    'ACCOUNT_CREATED', 'ACCOUNT_VERIFIED', 'PASSWORD_RESET', 'PASSWORD_CHANGED',
    'PROFILE_INCOMPLETE', 'NEW_MESSAGE', 'MESSAGE_MENTION',
    'CALENDAR_EVENT_CREATED', 'CALENDAR_EVENT_UPDATED', 'CALENDAR_EVENT_CANCELLED',
    'SYSTEM_ANNOUNCEMENT', 'REMINDER', 'TAG_ADDED', 'COMMENT_ADDED'
);

CREATE TYPE calendar_event_type AS ENUM (
    'INTERVIEW', 'ASSESSMENT', 'MEETING', 'DEADLINE', 'REMINDER', 'OTHER'
);

CREATE TYPE priority AS ENUM (
    'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
);

-- ---------------------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------------------

CREATE TABLE users (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    phone         VARCHAR(20),
    status        status      NOT NULL DEFAULT 'PENDING_VERIFICATION',
    gender        gender,
    avatar_url    VARCHAR(500),
    tenant_id     UUID,                          -- future multi-tenancy hook
    last_login_at TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- ---------------------------------------------------------------------------
-- STUDENTS  (profile extension of a user)
-- ---------------------------------------------------------------------------

CREATE TABLE students (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL,
    headline            VARCHAR(255),
    bio                 TEXT,
    date_of_birth       DATE,
    gender              gender,
    location            VARCHAR(255),
    linkedin_url        VARCHAR(500),
    github_url          VARCHAR(500),
    portfolio_url       VARCHAR(500),
    skills              TEXT[],                  -- simple array of skill strings
    languages           TEXT[],
    years_of_experience SMALLINT,
    availability        employment_type,
    preferred_work_mode work_mode,
    is_open_to_work     BOOLEAN     NOT NULL DEFAULT TRUE,
    profile_complete_pct SMALLINT  NOT NULL DEFAULT 0,
    CONSTRAINT pk_students PRIMARY KEY (id),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_students_user UNIQUE (user_id)
);

-- ---------------------------------------------------------------------------
-- ENTERPRISES  (company profiles)
-- ---------------------------------------------------------------------------

CREATE TABLE enterprises (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    slug          VARCHAR(100) NOT NULL,
    website_url   VARCHAR(500),
    logo_url      VARCHAR(500),
    industry      VARCHAR(100),
    size_range    VARCHAR(50),                   -- e.g. "51-200"
    hq_location   VARCHAR(255),
    description   TEXT,
    status        status      NOT NULL DEFAULT 'PENDING_VERIFICATION',
    verified_at   TIMESTAMPTZ,
    CONSTRAINT pk_enterprises PRIMARY KEY (id),
    CONSTRAINT uq_enterprises_slug UNIQUE (slug)
);

-- ---------------------------------------------------------------------------
-- OPENINGS  (job postings by enterprises)
-- ---------------------------------------------------------------------------

CREATE TABLE openings (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    enterprise_id   UUID            NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    requirements    TEXT,
    location        VARCHAR(255),
    employment_type employment_type NOT NULL DEFAULT 'FULL_TIME',
    work_mode       work_mode       NOT NULL DEFAULT 'ON_SITE',
    salary_min      NUMERIC(12, 2),
    salary_max      NUMERIC(12, 2),
    currency        CHAR(3)         NOT NULL DEFAULT 'USD',
    skills_required TEXT[],
    closes_at       TIMESTAMPTZ,
    status          status          NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT pk_openings PRIMARY KEY (id),
    CONSTRAINT fk_openings_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprises (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- APPLICATIONS  (student applies to an opening)
-- ---------------------------------------------------------------------------

CREATE TABLE applications (
    id               UUID               NOT NULL DEFAULT gen_random_uuid(),
    opening_id       UUID               NOT NULL,
    student_id       UUID               NOT NULL,
    cover_letter     TEXT,
    status           application_status NOT NULL DEFAULT 'SUBMITTED',
    status_changed_at TIMESTAMPTZ,
    notes            TEXT,
    CONSTRAINT pk_applications PRIMARY KEY (id),
    CONSTRAINT fk_applications_opening FOREIGN KEY (opening_id) REFERENCES openings (id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT uq_applications UNIQUE (opening_id, student_id)
);

-- ---------------------------------------------------------------------------
-- PIPELINE  (custom stages per enterprise)
-- ---------------------------------------------------------------------------

CREATE TABLE pipeline_stages (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    enterprise_id UUID        NOT NULL,
    name          VARCHAR(100) NOT NULL,
    display_order SMALLINT    NOT NULL DEFAULT 0,
    color         CHAR(7),                       -- hex colour e.g. #4A90E2
    CONSTRAINT pk_pipeline_stages PRIMARY KEY (id),
    CONSTRAINT fk_pipeline_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprises (id) ON DELETE CASCADE
);

CREATE TABLE pipeline_entries (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    application_id  UUID NOT NULL,
    stage_id        UUID NOT NULL,
    moved_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    moved_by        UUID,
    CONSTRAINT pk_pipeline_entries PRIMARY KEY (id),
    CONSTRAINT fk_pe_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_pe_stage       FOREIGN KEY (stage_id)       REFERENCES pipeline_stages (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- EXPRESS INTEREST  (student expresses interest before formal application)
-- ---------------------------------------------------------------------------

CREATE TABLE express_interests (
    id          UUID NOT NULL DEFAULT gen_random_uuid(),
    student_id  UUID NOT NULL,
    opening_id  UUID NOT NULL,
    message     TEXT,
    seen_at     TIMESTAMPTZ,
    CONSTRAINT pk_express_interests PRIMARY KEY (id),
    CONSTRAINT fk_ei_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_ei_opening FOREIGN KEY (opening_id) REFERENCES openings (id) ON DELETE CASCADE,
    CONSTRAINT uq_express_interests UNIQUE (student_id, opening_id)
);

-- ---------------------------------------------------------------------------
-- CHAT
-- ---------------------------------------------------------------------------

CREATE TABLE chat_threads (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    subject         VARCHAR(255),
    CONSTRAINT pk_chat_threads PRIMARY KEY (id)
);

CREATE TABLE chat_thread_participants (
    thread_id UUID NOT NULL,
    user_id   UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_chat_participants PRIMARY KEY (thread_id, user_id),
    CONSTRAINT fk_ctp_thread FOREIGN KEY (thread_id) REFERENCES chat_threads (id) ON DELETE CASCADE,
    CONSTRAINT fk_ctp_user   FOREIGN KEY (user_id)   REFERENCES users (id)         ON DELETE CASCADE
);

CREATE TABLE chat_messages (
    id        UUID NOT NULL DEFAULT gen_random_uuid(),
    thread_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    body      TEXT NOT NULL,
    sent_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at   TIMESTAMPTZ,
    CONSTRAINT pk_chat_messages PRIMARY KEY (id),
    CONSTRAINT fk_cm_thread FOREIGN KEY (thread_id) REFERENCES chat_threads (id) ON DELETE CASCADE,
    CONSTRAINT fk_cm_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- CALENDAR EVENTS
-- ---------------------------------------------------------------------------

CREATE TABLE calendar_events (
    id             UUID                NOT NULL DEFAULT gen_random_uuid(),
    organizer_id   UUID                NOT NULL,
    title          VARCHAR(255)        NOT NULL,
    description    TEXT,
    event_type     calendar_event_type NOT NULL DEFAULT 'MEETING',
    starts_at      TIMESTAMPTZ         NOT NULL,
    ends_at        TIMESTAMPTZ,
    location       VARCHAR(255),
    meeting_url    VARCHAR(500),
    application_id UUID,
    CONSTRAINT pk_calendar_events PRIMARY KEY (id),
    CONSTRAINT fk_ce_organizer    FOREIGN KEY (organizer_id)   REFERENCES users (id),
    CONSTRAINT fk_ce_application  FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE SET NULL
);

CREATE TABLE calendar_event_attendees (
    event_id  UUID NOT NULL,
    user_id   UUID NOT NULL,
    rsvp      VARCHAR(20) DEFAULT 'PENDING',   -- PENDING | ACCEPTED | DECLINED
    CONSTRAINT pk_cea PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_cea_event FOREIGN KEY (event_id) REFERENCES calendar_events (id) ON DELETE CASCADE,
    CONSTRAINT fk_cea_user  FOREIGN KEY (user_id)  REFERENCES users (id)           ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- NOTIFICATIONS
-- ---------------------------------------------------------------------------

CREATE TABLE notifications (
    id            UUID                 NOT NULL DEFAULT gen_random_uuid(),
    recipient_id  UUID                 NOT NULL,
    type          notification_type    NOT NULL,
    channel       notification_channel NOT NULL DEFAULT 'IN_APP',
    title         VARCHAR(255)         NOT NULL,
    body          TEXT,
    reference_id  UUID,                              -- generic FK to any entity
    reference_type VARCHAR(50),
    read_at       TIMESTAMPTZ,
    sent_at       TIMESTAMPTZ          NOT NULL DEFAULT now(),
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- ATTACHMENTS
-- ---------------------------------------------------------------------------

CREATE TABLE attachments (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    owner_id        UUID            NOT NULL,
    type            attachment_type NOT NULL DEFAULT 'OTHER',
    file_name       VARCHAR(255)    NOT NULL,
    file_url        VARCHAR(1000)   NOT NULL,
    content_type    VARCHAR(100),
    size_bytes      BIGINT,
    reference_id    UUID,
    reference_type  VARCHAR(50),
    CONSTRAINT pk_attachments PRIMARY KEY (id),
    CONSTRAINT fk_attachments_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- COMMENTS
-- ---------------------------------------------------------------------------

CREATE TABLE comments (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    author_id       UUID NOT NULL,
    body            TEXT NOT NULL,
    reference_id    UUID NOT NULL,
    reference_type  VARCHAR(50) NOT NULL,
    parent_id       UUID,                          -- for threaded replies
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- ACTIVITY LOG
-- ---------------------------------------------------------------------------

CREATE TABLE activity_logs (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    actor_id       UUID,
    action         VARCHAR(100) NOT NULL,
    entity_type    VARCHAR(50)  NOT NULL,
    entity_id      UUID,
    description    TEXT,
    ip_address     VARCHAR(45),
    user_agent     VARCHAR(500),
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_activity_logs PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------------
-- TAGS
-- ---------------------------------------------------------------------------

CREATE TABLE tags (
    id    UUID        NOT NULL DEFAULT gen_random_uuid(),
    name  VARCHAR(100) NOT NULL,
    color CHAR(7),
    CONSTRAINT pk_tags PRIMARY KEY (id),
    CONSTRAINT uq_tags_name UNIQUE (name)
);

CREATE TABLE taggings (
    tag_id         UUID        NOT NULL,
    reference_id   UUID        NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    CONSTRAINT pk_taggings PRIMARY KEY (tag_id, reference_id, reference_type),
    CONSTRAINT fk_taggings_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- SUCCESS STORIES
-- ---------------------------------------------------------------------------

CREATE TABLE success_stories (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    student_id    UUID        NOT NULL,
    enterprise_id UUID,
    title         VARCHAR(255) NOT NULL,
    body          TEXT,
    published_at  TIMESTAMPTZ,
    is_published  BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_success_stories PRIMARY KEY (id),
    CONSTRAINT fk_ss_student    FOREIGN KEY (student_id)    REFERENCES students (id)    ON DELETE CASCADE,
    CONSTRAINT fk_ss_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprises (id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------------------
-- FEATURE FLAGS
-- ---------------------------------------------------------------------------

CREATE TABLE feature_flags (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    enabled     BOOLEAN     NOT NULL DEFAULT FALSE,
    tenant_id   UUID,
    CONSTRAINT pk_feature_flags PRIMARY KEY (id),
    CONSTRAINT uq_feature_flags_name UNIQUE (name)
);

-- ---------------------------------------------------------------------------
-- JOURNAL  (private notes by users — students or recruiters)
-- ---------------------------------------------------------------------------

CREATE TABLE journal_entries (
    id         UUID NOT NULL DEFAULT gen_random_uuid(),
    author_id  UUID NOT NULL,
    title      VARCHAR(255),
    body       TEXT NOT NULL,
    tags       TEXT[],
    is_private BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_journal_entries PRIMARY KEY (id),
    CONSTRAINT fk_je_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- INDEXES  (query-critical columns)
-- ---------------------------------------------------------------------------

CREATE INDEX idx_users_email            ON users (email);
CREATE INDEX idx_users_status           ON users (status);
CREATE INDEX idx_students_user_id       ON students (user_id);
CREATE INDEX idx_openings_enterprise    ON openings (enterprise_id);
CREATE INDEX idx_openings_status        ON openings (status);
CREATE INDEX idx_applications_opening   ON applications (opening_id);
CREATE INDEX idx_applications_student   ON applications (student_id);
CREATE INDEX idx_applications_status    ON applications (status);
CREATE INDEX idx_notifications_recipient ON notifications (recipient_id);
CREATE INDEX idx_notifications_read_at  ON notifications (read_at) WHERE read_at IS NULL;
CREATE INDEX idx_activity_logs_actor    ON activity_logs (actor_id);
CREATE INDEX idx_activity_logs_entity   ON activity_logs (entity_type, entity_id);
CREATE INDEX idx_taggings_ref           ON taggings (reference_type, reference_id);
CREATE INDEX idx_comments_ref           ON comments (reference_type, reference_id);
CREATE INDEX idx_attachments_owner      ON attachments (owner_id);
CREATE INDEX idx_pipeline_entries_app   ON pipeline_entries (application_id);
