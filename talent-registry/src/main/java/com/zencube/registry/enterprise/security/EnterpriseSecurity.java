package com.zencube.registry.enterprise.security;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.enterprise.service.EnterpriseAccessAuditService;
import com.zencube.registry.enterprise.exception.EnterpriseOwnershipException;
import com.zencube.registry.enterprise.exception.EnterpriseAccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Slf4j
@Component("enterpriseSecurity")
@RequestScope
@RequiredArgsConstructor
public class EnterpriseSecurity {

    private final EnterpriseAccountRepository enterpriseRepository;
    private final EnterpriseAccessAuditService accessAuditService;
    
    // Request scope caching to avoid multiple DB lookups per request
    private EnterpriseAccount cachedEnterprise;
    private UUID cachedEnterpriseId;

    /**
     * Resolves the current authenticated user safely.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Checks if the user holds an administrative role (ADMIN or HR_STAFF).
     */
    private boolean isAdministrator(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_HR_STAFF"));
    }

    /**
     * Loads the enterprise with request-scope caching.
     */
    private EnterpriseAccount getEnterprise(UUID enterpriseId) {
        if (enterpriseId.equals(cachedEnterpriseId) && cachedEnterprise != null) {
            return cachedEnterprise;
        }
        EnterpriseAccount enterprise = enterpriseRepository.findById(enterpriseId).orElse(null);
        if (enterprise != null) {
            this.cachedEnterprise = enterprise;
            this.cachedEnterpriseId = enterpriseId;
        }
        return enterprise;
    }

    // ==========================================
    // PHASE 4: OWNERSHIP CHECK
    // ==========================================
    public boolean isOwner(UUID enterpriseId) {
        User user = getCurrentUser();
        if (user == null) return false;

        EnterpriseAccount enterprise = getEnterprise(enterpriseId);
        if (enterprise == null) return false;

        boolean isOwner = enterprise.isOwner(user.getId());
        
        if (isOwner) {
            logAudit(enterprise, user, "OwnershipCheckPassed", "/api/ownership");
        } else {
            logAudit(enterprise, user, "OwnershipCheckFailed", "/api/ownership");
        }
        
        return isOwner;
    }



    // ==========================================
    // PHASE 5: ADMINISTRATIVE BYPASS
    // ==========================================
    public boolean isOwnerOrAdmin(UUID enterpriseId) {
        User user = getCurrentUser();
        if (user == null) return false;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAdministrator(auth)) {
            EnterpriseAccount enterprise = getEnterprise(enterpriseId);
            if (enterprise != null) {
                logAudit(enterprise, user, "AdminBypassUsed", "/api/admin-bypass");
            }
            return true;
        }

        return isOwner(enterpriseId);
    }

    // ==========================================
    // PHASE 6: DASHBOARD ACCESS
    // ==========================================
    public boolean canAccessDashboard(UUID enterpriseId) {
        if (!isOwnerOrAdmin(enterpriseId)) {
            throw new EnterpriseOwnershipException("User does not own this enterprise account");
        }

        EnterpriseAccount enterprise = getEnterprise(enterpriseId);
        if (enterprise == null) return false;

        boolean canAccess = enterprise.isApproved();
        User user = getCurrentUser();
        
        if (canAccess) {
            logAudit(enterprise, user, "DashboardAccessGranted", "/api/dashboard");
        } else {
            logAudit(enterprise, user, "DashboardAccessDenied", "/api/dashboard");
            throw new EnterpriseAccessDeniedException("Enterprise is not approved for dashboard access.");
        }

        return canAccess;
    }

    // ==========================================
    // PHASE 7: JOB POSTING ACCESS
    // ==========================================
    public boolean canManageJobPosting(UUID enterpriseId) {
        if (!isOwnerOrAdmin(enterpriseId)) {
            throw new EnterpriseOwnershipException("User does not own this enterprise account");
        }

        EnterpriseAccount enterprise = getEnterprise(enterpriseId);
        if (enterprise == null) return false;

        boolean canPost = enterprise.canPostOpenings();
        User user = getCurrentUser();
        
        if (canPost) {
            logAudit(enterprise, user, "JobPostingAccessGranted", "/api/jobs");
        } else {
            logAudit(enterprise, user, "JobPostingAccessDenied", "/api/jobs");
            throw new EnterpriseAccessDeniedException("Enterprise does not have permission to post jobs.");
        }

        return canPost;
    }

    public boolean canSearchTalent(UUID enterpriseId) {
        if (!isOwnerOrAdmin(enterpriseId)) {
            throw new EnterpriseOwnershipException("User does not own this enterprise account");
        }

        EnterpriseAccount enterprise = getEnterprise(enterpriseId);
        if (enterprise == null) return false;

        return enterprise.canSearchTalent();
    }

    // ==========================================
    // PHASE 10: AUDIT LOGGING HELPER
    // ==========================================
    private void logAudit(EnterpriseAccount enterprise, User user, String eventType, String endpoint) {
        try {
            UUID userId = user != null ? user.getId() : null;
            accessAuditService.logAccessAttempt(
                    enterprise.getId(),
                    userId,
                    eventType,
                    enterprise.getOnboardingStatus(),
                    endpoint
            );
        } catch (Exception e) {
            log.warn("Failed to log enterprise security audit event: {}", e.getMessage());
        }
    }

    // ==========================================
    // OVERLOADS FOR /my ENDPOINTS (Implicit Enterprise Resolution)
    // ==========================================
    
    private EnterpriseAccount getMyEnterprise() {
        User user = getCurrentUser();
        if (user == null) return null;
        return enterpriseRepository.findByUserId(user.getId()).orElse(null);
    }
    
    public boolean isCurrentEnterpriseOwner() {
        return getMyEnterprise() != null;
    }
    
    public boolean canAccessMyDashboard() {
        EnterpriseAccount myEnterprise = getMyEnterprise();
        if (myEnterprise == null) return false;
        return canAccessDashboard(myEnterprise.getId());
    }
    
    public boolean canManageMyJobPosting() {
        EnterpriseAccount myEnterprise = getMyEnterprise();
        if (myEnterprise == null) return false;
        return canManageJobPosting(myEnterprise.getId());
    }
}
