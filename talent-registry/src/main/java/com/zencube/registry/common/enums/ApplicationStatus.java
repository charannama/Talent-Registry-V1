package com.zencube.registry.common.enums;

/**
 * Lifecycle states of a job/opportunity application.
 */
public enum ApplicationStatus {
    /** Application has been applied for. */
    APPLIED,
    /** Application has been submitted but not yet reviewed. */
    SUBMITTED,
    /** Application is under review by the recruiter. */
    UNDER_REVIEW,
    /** Application has been forwarded to next stage. */
    FORWARDED,
    /** Application has been shortlisted for further evaluation. */
    SHORTLISTED,
    /** Candidate has been invited to an assessment. */
    ASSESSMENT_SENT,
    /** Assessment has been completed by the candidate. */
    ASSESSMENT_COMPLETED,
    /** Interview has been scheduled. */
    INTERVIEW_SCHEDULED,
    /** Interview has been completed. */
    INTERVIEW_COMPLETED,
    /** Candidate has been selected. */
    SELECTED,
    /** Offer has been extended to the candidate. */
    OFFER_EXTENDED,
    /** Offer has been accepted by the candidate. */
    OFFER_ACCEPTED,
    /** Offer has been rejected by the candidate. */
    OFFER_REJECTED,
    /** Application has been rejected by the recruiter. */
    REJECTED,
    /** Application has been withdrawn by the applicant. */
    WITHDRAWN,
    /** Candidate has been onboarded successfully. */
    ONBOARDED
}
