package com.zencube.registry.opening.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.security.EnterpriseSecurity;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.request.CloseOpeningRequest;
import com.zencube.registry.opening.dto.response.CloseOpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.repository.OpeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpeningServiceCloseTest {

    @Mock
    private OpeningRepository openingRepository;

    @Mock
    private EnterpriseSecurity enterpriseSecurity;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private OpeningServiceImpl openingService;

    private UUID openingId;
    private EnterpriseAccount enterprise;
    private Opening opening;
    private User mockUser;

    @BeforeEach
    void setUp() {
        openingId = UUID.randomUUID();
        enterprise = new EnterpriseAccount();
        enterprise.setId(UUID.randomUUID());
        enterprise.setAccountActive(true);
        enterprise.setOnboardingStatus(com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus.APPROVED);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        opening = new Opening();
        opening.setId(openingId);
        opening.setEnterprise(enterprise);
    }

    @Test
    @DisplayName("Should successfully close an opening when status is LIVE and user is enterprise owner")
    void closeOpening_Success_EnterpriseOwner() {
        opening.setStatus(OpeningStatus.LIVE);
        CloseOpeningRequest request = new CloseOpeningRequest("Position filled.");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null, List.of()));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        CloseOpeningResponse response = openingService.closeOpening(openingId, request);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.CLOSED);
        assertThat(opening.getClosureReason()).isEqualTo("Position filled.");
        assertThat(opening.getClosedBy()).isEqualTo(mockUser.getId());
    }

    @Test
    @DisplayName("Should successfully close an opening when user has OPENING_MANAGE permission (Admin/HR)")
    void closeOpening_Success_Admin() {
        opening.setStatus(OpeningStatus.LIVE);
        CloseOpeningRequest request = new CloseOpeningRequest("Closed administratively.");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("OPENING_MANAGE"))));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(false);
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        CloseOpeningResponse response = openingService.closeOpening(openingId, request);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.CLOSED);
        assertThat(opening.getClosureReason()).isEqualTo("Closed administratively.");
    }

    @Test
    @DisplayName("Should throw exception if closing opening without ownership or admin rights")
    void closeOpening_OwnershipViolation() {
        opening.setStatus(OpeningStatus.LIVE);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null, List.of()));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(false);

        assertThrows(com.zencube.registry.enterprise.exception.OwnershipViolationException.class, () -> openingService.closeOpening(openingId, new CloseOpeningRequest()));
    }

    @Test
    @DisplayName("Should throw exception if closing opening with invalid status")
    void closeOpening_InvalidStatus() {
        opening.setStatus(OpeningStatus.DRAFT);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null, List.of()));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> openingService.closeOpening(openingId, new CloseOpeningRequest()));
    }
}
