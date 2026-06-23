package com.zencube.registry.auth.enums;

/**
 * The OAuth2 / identity provider through which a user authenticated.
 * LOCAL means standard email-and-password registration.
 */
public enum AuthProvider {

    /** Standard username/password registration managed by this application. */
    LOCAL,

    /** Authentication via Google OAuth2. */
    GOOGLE,

    /** Authentication via GitHub OAuth2. */
    GITHUB,

    /** Authentication via LinkedIn OAuth2. */
    LINKEDIN,

    /** Authentication via ZenCube SSO OAuth2. */
    ZENCUBE_SSO,

    /** Native registry credentials. */
    NATIVE
}
