package com.zencube.registry.enterprise.enums;

import java.util.Collections;
import java.util.Set;

/**
 * Represents the onboarding status of an enterprise account.
 */
public enum EnterpriseOnboardingStatus {
    /** Enterprise submitted application */
    PENDING_HR_REVIEW,
    
    /** HR approved account */
    APPROVED,
    
    /** HR rejected account */
    REJECTED,
    
    /** HR suspended account */
    SUSPENDED;

    static {
        PENDING_HR_REVIEW.allowedTransitions = Set.of(APPROVED, REJECTED);
        APPROVED.allowedTransitions = Set.of(SUSPENDED);
        REJECTED.allowedTransitions = Collections.emptySet();
        SUSPENDED.allowedTransitions = Set.of(APPROVED);
    }

    private Set<EnterpriseOnboardingStatus> allowedTransitions;

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isPending() {
        return this == PENDING_HR_REVIEW;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean canTransitionTo(EnterpriseOnboardingStatus target) {
        return allowedTransitions != null && allowedTransitions.contains(target);
    }
}
