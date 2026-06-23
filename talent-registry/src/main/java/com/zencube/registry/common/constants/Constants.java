package com.zencube.registry.common.constants;

/**
 * Application-wide constants.
 * Non-instantiable utility class — all members are static final.
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Constants is a utility class and cannot be instantiated.");
    }

    // -------------------------------------------------------------------------
    // API Base Paths
    // -------------------------------------------------------------------------
    public static final String API_V1                   = "/api/v1";
    public static final String AUTH_BASE                = API_V1 + "/auth";
    public static final String USERS_BASE               = API_V1 + "/users";
    public static final String ROLES_BASE               = API_V1 + "/roles";
    public static final String PERMISSIONS_BASE         = API_V1 + "/permissions";
    public static final String STUDENTS_BASE            = API_V1 + "/students";
    public static final String ENTERPRISES_BASE         = API_V1 + "/enterprises";
    public static final String OPENINGS_BASE            = API_V1 + "/openings";
    public static final String APPLICATIONS_BASE        = API_V1 + "/applications";
    public static final String PIPELINE_BASE            = API_V1 + "/pipeline";
    public static final String EXPRESS_INTEREST_BASE    = API_V1 + "/express-interest";
    public static final String CHAT_BASE                = API_V1 + "/chat";
    public static final String CALENDAR_BASE            = API_V1 + "/calendar";
    public static final String NOTIFICATIONS_BASE       = API_V1 + "/notifications";
    public static final String ATTACHMENTS_BASE         = API_V1 + "/attachments";
    public static final String COMMENTS_BASE            = API_V1 + "/comments";
    public static final String ACTIVITY_BASE            = API_V1 + "/activity";
    public static final String TAGS_BASE                = API_V1 + "/tags";
    public static final String SUCCESS_STORIES_BASE     = API_V1 + "/success-stories";
    public static final String FEATURE_FLAGS_BASE       = API_V1 + "/feature-flags";
    public static final String JOURNAL_BASE             = API_V1 + "/journal";

    // -------------------------------------------------------------------------
    // Security / JWT Claims
    // -------------------------------------------------------------------------
    public static final String JWT_CLAIM_USER_ID        = "userId";
    public static final String JWT_CLAIM_ROLES          = "roles";
    public static final String JWT_CLAIM_PERMISSIONS    = "permissions";
    public static final String JWT_CLAIM_EMAIL          = "email";
    public static final String JWT_CLAIM_TENANT_ID      = "tenantId";
    public static final String BEARER_PREFIX            = "Bearer ";
    public static final String AUTHORIZATION_HEADER     = "Authorization";

    // -------------------------------------------------------------------------
    // Regex Patterns
    // -------------------------------------------------------------------------
    public static final String EMAIL_REGEX              = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
    public static final String PHONE_REGEX              = "^\\+?[1-9]\\d{1,14}$";
    public static final String PASSWORD_REGEX           = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$";
    public static final String URL_REGEX                = "^(https?|ftp)://[^\\s/$.?#].\\S*$";
    public static final String UUID_REGEX               = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    // -------------------------------------------------------------------------
    // Pagination Defaults
    // -------------------------------------------------------------------------
    public static final int    DEFAULT_PAGE_NUMBER      = 0;
    public static final int    DEFAULT_PAGE_SIZE        = 20;
    public static final int    MAX_PAGE_SIZE            = 100;
    public static final String DEFAULT_SORT_FIELD       = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION   = "DESC";

    // -------------------------------------------------------------------------
    // Date / Time
    // -------------------------------------------------------------------------
    public static final String DATE_FORMAT              = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT          = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String TIMEZONE_UTC             = "UTC";

    // -------------------------------------------------------------------------
    // File / Attachment
    // -------------------------------------------------------------------------
    public static final long   MAX_FILE_SIZE_BYTES      = 10 * 1024 * 1024L;  // 10 MB
    public static final String[] ALLOWED_IMAGE_TYPES    = {"image/jpeg", "image/png", "image/webp", "image/gif"};
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};

    // -------------------------------------------------------------------------
    // Cache Key Prefixes
    // -------------------------------------------------------------------------
    public static final String CACHE_USER               = "user:";
    public static final String CACHE_ROLE               = "role:";
    public static final String CACHE_PERMISSION         = "permission:";
    public static final String CACHE_FEATURE_FLAG       = "feature-flag:";

    // -------------------------------------------------------------------------
    // Error Messages
    // -------------------------------------------------------------------------
    public static final String ERR_RESOURCE_NOT_FOUND   = "The requested resource was not found.";
    public static final String ERR_ACCESS_DENIED        = "Access denied. You do not have permission to perform this action.";
    public static final String ERR_UNAUTHORIZED         = "Authentication is required to access this resource.";
    public static final String ERR_VALIDATION_FAILED    = "Request validation failed. Please check the submitted data.";
    public static final String ERR_CONFLICT             = "A conflict occurred. The resource may already exist.";
    public static final String ERR_INTERNAL_SERVER      = "An unexpected error occurred. Please try again later.";

    // -------------------------------------------------------------------------
    // Audit / Activity
    // -------------------------------------------------------------------------
    public static final String SYSTEM_USER              = "SYSTEM";
    public static final String ANONYMOUS_USER           = "ANONYMOUS";

    // -------------------------------------------------------------------------
    // Feature Flag Defaults
    // -------------------------------------------------------------------------
    public static final boolean DEFAULT_FEATURE_ENABLED = false;
}
