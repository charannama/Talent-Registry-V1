package com.zencube.registry.enterprise.security;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.exception.EnterpriseAccessDeniedException;
import com.zencube.registry.enterprise.exception.EnterpriseOwnershipException;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.enterprise.service.EnterpriseAccessAuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseSecurityTest {

    @Mock
    private EnterpriseAccountRepository repository;

    @Mock
    private EnterpriseAccessAuditService auditService;

    @InjectMocks
    private EnterpriseSecurity security;

    private User ownerUser;
    private User otherUser;
    private EnterpriseAccount enterprise;
    private UUID enterpriseId;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        enterpriseId = UUID.randomUUID();
        enterprise = new EnterpriseAccount();
        enterprise.setId(enterpriseId);
        enterprise.setUser(ownerUser);
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.APPROVED);
        enterprise.setAccountActive(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(User user, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void authenticateAnonymous() {
        SecurityContextHolder.clearContext();
    }

    // ===============================================
    // isOwner Tests
    // ===============================================

    @Test
    void isOwner_WhenOwner_ReturnsTrue() {
        authenticate(ownerUser, "ROLE_ENTERPRISE");
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        assertTrue(security.isOwner(enterpriseId));
        verify(auditService).logAccessAttempt(eq(enterpriseId), eq(ownerUser.getId()), eq("OwnershipCheckPassed"), any(), any());
    }

    @Test
    void isOwner_WhenNotOwner_ReturnsFalse() {
        authenticate(otherUser, "ROLE_ENTERPRISE");
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        assertFalse(security.isOwner(enterpriseId));
        verify(auditService).logAccessAttempt(eq(enterpriseId), eq(otherUser.getId()), eq("OwnershipCheckFailed"), any(), any());
    }

    @Test
    void isOwner_WhenAnonymous_ReturnsFalse() {
        authenticateAnonymous();
        assertFalse(security.isOwner(enterpriseId));
        verifyNoInteractions(repository);
    }

    // ===============================================
    // isOwnerOrAdmin Tests
    // ===============================================

    @Test
    void isOwnerOrAdmin_WhenAdmin_ReturnsTrueAndBypassesOwnerCheck() {
        authenticate(otherUser, "ROLE_ADMIN");
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        assertTrue(security.isOwnerOrAdmin(enterpriseId));
        verify(auditService).logAccessAttempt(eq(enterpriseId), eq(otherUser.getId()), eq("AdminBypassUsed"), any(), any());
    }

    @Test
    void isOwnerOrAdmin_WhenHrStaff_ReturnsTrue() {
        authenticate(otherUser, "ROLE_HR_STAFF");
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        assertTrue(security.isOwnerOrAdmin(enterpriseId));
        verify(auditService).logAccessAttempt(eq(enterpriseId), eq(otherUser.getId()), eq("AdminBypassUsed"), any(), any());
    }

    // ===============================================
    // canAccessDashboard Tests
    // ===============================================

    @Test
    void canAccessDashboard_WhenOwnerAndApproved_ReturnsTrue() {
        authenticate(ownerUser, "ROLE_ENTERPRISE");
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        assertTrue(security.canAccessDashboard(enterpriseId));
        verify(auditService).logAccessAttempt(eq(enterpriseId), eq(ownerUser.getId()), eq("DashboardAccessGranted"), any(), any());
    }

    @Test
    void canAccessDashboard_WhenNotOwner_ThrowsException() {
        authenticate(otherUser, "ROLE_ENTERPRISE");
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        assertThrows(EnterpriseOwnershipException.class, () -> security.canAccessDashboard(enterpriseId));
    }

    @Test
    void canAccessDashboard_WhenOwnerButSuspended_ThrowsAccessDenied() {
        authenticate(ownerUser, "ROLE_ENTERPRISE");
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.SUSPENDED);
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        assertThrows(EnterpriseAccessDeniedException.class, () -> security.canAccessDashboard(enterpriseId));
        verify(auditService).logAccessAttempt(eq(enterpriseId), eq(ownerUser.getId()), eq("DashboardAccessDenied"), any(), any());
    }

    // ===============================================
    // Caching Tests
    // ===============================================

    @Test
    void getEnterprise_UsesCacheForSubsequentCalls() {
        authenticate(ownerUser, "ROLE_ENTERPRISE");
        when(repository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        // First call hits DB
        security.isOwner(enterpriseId);
        
        // Second call should use cache
        security.canAccessDashboard(enterpriseId);
        
        // Verify DB was only hit once
        verify(repository, times(1)).findById(enterpriseId);
    }
}
