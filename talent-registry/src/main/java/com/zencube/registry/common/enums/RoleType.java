package com.zencube.registry.common.enums;

/**
 * System-level role types that map to Spring Security authority hierarchies.
 */
public enum RoleType {
    /** Platform super-administrator with unrestricted access. */
    SUPER_ADMIN,
    /** Platform administrator managing overall platform settings. */
    ADMIN,
    /** Enterprise/company-level administrator. */
    ENTERPRISE_ADMIN,
    /** HR Staff member belonging to an enterprise. */
    HR_STAFF,
    /** HR or recruiter belonging to an enterprise. */
    RECRUITER,
    /** Enterprise recruiter role. */
    ENTERPRISE_RECRUITER,
    /** Hiring manager within an enterprise. */
    HIRING_MANAGER,
    /** Registered student / job-seeker. */
    STUDENT,
    /** Read-only viewer with minimal access. */
    VIEWER,
    /** External API consumer (machine-to-machine). */
    SERVICE_ACCOUNT,
    /** Custom role created by an admin. */
    CUSTOM
}
