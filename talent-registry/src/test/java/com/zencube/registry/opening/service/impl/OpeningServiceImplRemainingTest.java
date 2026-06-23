package com.zencube.registry.opening.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.security.EnterpriseSecurity;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.request.RejectionRequest;
import com.zencube.registry.opening.dto.request.UpdateOpeningRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.exception.OpeningNotDraftException;
import com.zencube.registry.opening.repository.OpeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpeningServiceImplRemainingTest {

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
    private User hrUser;

    @BeforeEach
    void setUp() {
        openingId = UUID.randomUUID();
        enterprise = new EnterpriseAccount();
        enterprise.setId(UUID.randomUUID());

        hrUser = new User();
        hrUser.setId(UUID.randomUUID());

        opening = new Opening();
        opening.setId(openingId);
        opening.setEnterprise(enterprise);
        opening.setStatus(OpeningStatus.DRAFT);
    }

    @Test
    @DisplayName("Should successfully update a draft opening")
    void updateDraft_Success() {
        UpdateOpeningRequest request = new UpdateOpeningRequest();
        request.setTitle("Updated Title");

        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        OpeningResponse response = openingService.updateDraft(openingId, request);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        verify(openingRepository).save(opening);
    }

    @Test
    @DisplayName("Should fail update if not owner")
    void updateDraft_NotOwner() {
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(false);

        assertThrows(com.zencube.registry.enterprise.exception.OwnershipViolationException.class, () -> openingService.updateDraft(openingId, new UpdateOpeningRequest()));
    }

    @Test
    @DisplayName("Should fail update if not draft")
    void updateDraft_NotDraft() {
        opening.setStatus(OpeningStatus.LIVE);
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);

        assertThrows(OpeningNotDraftException.class, () -> openingService.updateDraft(openingId, new UpdateOpeningRequest()));
    }

    @Test
    @DisplayName("Should successfully reject an opening")
    void rejectOpening_Success() {
        opening.setStatus(OpeningStatus.PENDING_APPROVAL);
        RejectionRequest request = new RejectionRequest("Salary too low", true);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(hrUser, null));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        OpeningResponse response = openingService.rejectOpening(openingId, request);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.REJECTED);
        assertThat(response.getRejectionReason()).isEqualTo("Salary too low");
        assertThat(response.getRejectedBy()).isEqualTo(hrUser.getId());
        verify(openingRepository).save(opening);
    }

    @Test
    @DisplayName("Should successfully close a live opening")
    void closeOpening_Success() {
        opening.setStatus(OpeningStatus.LIVE);
        enterprise.setOnboardingStatus(com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus.APPROVED);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(hrUser, null, java.util.List.of()));

        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        com.zencube.registry.opening.dto.response.CloseOpeningResponse response = openingService.closeOpening(openingId, new com.zencube.registry.opening.dto.request.CloseOpeningRequest("Closed"));

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.CLOSED);
        verify(openingRepository).save(opening);
    }

    @Test
    @DisplayName("Should successfully archive a closed opening")
    void archiveOpening_Success() {
        opening.setStatus(OpeningStatus.CLOSED);
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        OpeningResponse response = openingService.archiveOpening(openingId);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.ARCHIVED);
        verify(openingRepository).save(opening);
    }
}
