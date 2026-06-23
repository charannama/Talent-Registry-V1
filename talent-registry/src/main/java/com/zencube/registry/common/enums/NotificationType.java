package com.zencube.registry.common.enums;

/**
 * Types of notifications that can be dispatched to users.
 */
public enum NotificationType {
    // Application events
    APPLICATION_RECEIVED,
    APPLICATION_STATUS_CHANGED,
    APPLICATION_WITHDRAWN,

    // Interview & assessment
    INTERVIEW_SCHEDULED,
    INTERVIEW_REMINDER,
    INTERVIEW_CANCELLED,
    ASSESSMENT_ASSIGNED,
    ASSESSMENT_REMINDER,

    // Offer events
    OFFER_EXTENDED,
    OFFER_ACCEPTED,
    OFFER_REJECTED,

    // Expression of interest
    EXPRESS_INTEREST_RECEIVED,

    // Pipeline events
    PIPELINE_STAGE_CHANGED,

    // Account events
    ACCOUNT_CREATED,
    ACCOUNT_VERIFIED,
    PASSWORD_RESET,
    PASSWORD_CHANGED,
    PROFILE_INCOMPLETE,

    // Chat & communication
    NEW_MESSAGE,
    MESSAGE_MENTION,

    // Calendar
    CALENDAR_EVENT_CREATED,
    CALENDAR_EVENT_UPDATED,
    CALENDAR_EVENT_CANCELLED,

    // General
    SYSTEM_ANNOUNCEMENT,
    REMINDER,
    TAG_ADDED,
    COMMENT_ADDED
}
