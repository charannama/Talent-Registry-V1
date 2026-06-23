package com.zencube.registry.auth.service;

import com.zencube.registry.common.enums.RoleType;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RedirectService {

    /**
     * Determines the correct dashboard redirect URL based on the user's highest priority role.
     */
    public String determineRedirectUrl(Collection<String> roles) {
        if (roles.contains("ROLE_" + RoleType.SUPER_ADMIN.name()) || roles.contains("ROLE_" + RoleType.ADMIN.name())) {
            return "/admin/dashboard";
        }
        if (roles.contains("ROLE_" + RoleType.ENTERPRISE_ADMIN.name())) {
            return "/enterprise/dashboard";
        }
        if (roles.contains("ROLE_" + RoleType.RECRUITER.name())) {
            return "/recruiter/dashboard";
        }
        if (roles.contains("ROLE_" + RoleType.HIRING_MANAGER.name())) {
            return "/manager/dashboard";
        }
        if (roles.contains("ROLE_" + RoleType.STUDENT.name())) {
            return "/student/dashboard";
        }
        if (roles.contains("ROLE_" + RoleType.SERVICE_ACCOUNT.name())) {
            return "/api";
        }
        // Fallback for VIEWER or unknown
        return "/dashboard";
    }

    /**
     * Returns the primary role as a string to be included in the DTO.
     */
    public String getPrimaryRole(Collection<String> roles) {
        if (roles.contains("ROLE_" + RoleType.SUPER_ADMIN.name())) return RoleType.SUPER_ADMIN.name();
        if (roles.contains("ROLE_" + RoleType.ADMIN.name())) return RoleType.ADMIN.name();
        if (roles.contains("ROLE_" + RoleType.ENTERPRISE_ADMIN.name())) return RoleType.ENTERPRISE_ADMIN.name();
        if (roles.contains("ROLE_" + RoleType.RECRUITER.name())) return RoleType.RECRUITER.name();
        if (roles.contains("ROLE_" + RoleType.HIRING_MANAGER.name())) return RoleType.HIRING_MANAGER.name();
        if (roles.contains("ROLE_" + RoleType.STUDENT.name())) return RoleType.STUDENT.name();
        if (roles.contains("ROLE_" + RoleType.SERVICE_ACCOUNT.name())) return RoleType.SERVICE_ACCOUNT.name();
        if (roles.contains("ROLE_" + RoleType.VIEWER.name())) return RoleType.VIEWER.name();
        return "UNKNOWN";
    }
}
