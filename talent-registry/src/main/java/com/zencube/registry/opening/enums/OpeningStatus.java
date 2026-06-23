package com.zencube.registry.opening.enums;

/**
 * Status of a job opening in its lifecycle.
 */
public enum OpeningStatus {
    DRAFT,
    PENDING_APPROVAL,
    REVISION_REQUESTED,
    LIVE,
    REJECTED,
    CLOSED,
    ARCHIVED
}
