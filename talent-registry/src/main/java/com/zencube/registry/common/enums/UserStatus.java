package com.zencube.registry.common.enums;

/**
 * Represents the lifecycle status of a user account.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED,
    PENDING_VERIFICATION,
    SUSPENDED;

    /**
     * Convenience check for an active, non‑deleted, non‑locked account.
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}
