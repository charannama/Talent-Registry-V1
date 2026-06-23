package com.zencube.registry.auth.service;

import com.zencube.registry.common.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedirectServiceTest {

    private RedirectService redirectService;

    @BeforeEach
    void setUp() {
        redirectService = new RedirectService();
    }

    @Test
    void determineRedirectUrl_Admin_ShouldReturnAdminDashboard() {
        assertEquals("/admin/dashboard", redirectService.determineRedirectUrl(Collections.singletonList("ROLE_" + RoleType.ADMIN.name())));
        assertEquals("/admin/dashboard", redirectService.determineRedirectUrl(Collections.singletonList("ROLE_" + RoleType.SUPER_ADMIN.name())));
    }

    @Test
    void determineRedirectUrl_EnterpriseAdmin_ShouldReturnEnterpriseDashboard() {
        assertEquals("/enterprise/dashboard", redirectService.determineRedirectUrl(Collections.singletonList("ROLE_" + RoleType.ENTERPRISE_ADMIN.name())));
    }

    @Test
    void determineRedirectUrl_Recruiter_ShouldReturnRecruiterDashboard() {
        assertEquals("/recruiter/dashboard", redirectService.determineRedirectUrl(Collections.singletonList("ROLE_" + RoleType.RECRUITER.name())));
    }

    @Test
    void determineRedirectUrl_HiringManager_ShouldReturnManagerDashboard() {
        assertEquals("/manager/dashboard", redirectService.determineRedirectUrl(Collections.singletonList("ROLE_" + RoleType.HIRING_MANAGER.name())));
    }

    @Test
    void determineRedirectUrl_Student_ShouldReturnStudentDashboard() {
        assertEquals("/student/dashboard", redirectService.determineRedirectUrl(Collections.singletonList("ROLE_" + RoleType.STUDENT.name())));
    }

    @Test
    void determineRedirectUrl_MultipleRoles_ShouldReturnHighestPriority() {
        // Admin + Student should return admin dashboard
        assertEquals("/admin/dashboard", redirectService.determineRedirectUrl(Arrays.asList("ROLE_" + RoleType.STUDENT.name(), "ROLE_" + RoleType.ADMIN.name())));
    }

    @Test
    void determineRedirectUrl_UnknownRole_ShouldReturnDashboard() {
        assertEquals("/dashboard", redirectService.determineRedirectUrl(Collections.singletonList("ROLE_UNKNOWN")));
    }
}
