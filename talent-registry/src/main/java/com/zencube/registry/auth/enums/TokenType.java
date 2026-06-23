package com.zencube.registry.auth.enums;

/**
 * Distinguishes between short-lived access tokens and long-lived refresh tokens.
 */
public enum TokenType {

    /** Short-lived JWT used to authenticate API requests. */
    ACCESS,

    /** Long-lived token used to obtain a new access token without re-login. */
    REFRESH,

    /** One-time token sent via email to verify a user's address. */
    EMAIL_VERIFICATION,

    /** One-time token sent via email to reset a forgotten password. */
    PASSWORD_RESET
}
