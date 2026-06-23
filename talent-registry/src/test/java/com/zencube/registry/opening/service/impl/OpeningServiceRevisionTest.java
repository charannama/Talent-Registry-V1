package com.zencube.registry.opening.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.security.EnterpriseSecurity;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.request.RequestRevisionRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.dto.response.ResubmitOpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.opening.validator.OpeningSubmissionValidator;
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
class OpeningServiceRevisionTest {

    @Mock
    private OpeningRepository openingRepository;

    @Mock
    private EnterpriseSecurity enterpriseSecurity;

    @Mock
    private OpeningSubmissionValidator submissionValidator;

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
    @DisplayName("Should successfully request a revision when status is PENDING_APPROVAL")
    void requestRevision_Success() {
        opening.setStatus(OpeningStatus.PENDING_APPROVAL);
        RequestRevisionRequest request = new RequestRevisionRequest("Please clarify salary.");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        OpeningResponse response = openingService.requestRevision(openingId, request);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.REVISION_REQUESTED);
        assertThat(response.getRevisionFeedback()).isEqualTo("Please clarify salary.");
        assertThat(response.getRevisionRequestedBy()).isEqualTo(mockUser.getId());
        assertThat(response.getRevisionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw exception if requesting revision on non-pending opening")
    void requestRevision_NotPending() {
        opening.setStatus(OpeningStatus.LIVE);
        RequestRevisionRequest request = new RequestRevisionRequest("Please clarify salary.");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));

        assertThrows(IllegalStateException.class, () -> openingService.requestRevision(openingId, request));
    }

    @Test
    @DisplayName("Should successfully resubmit an opening when status is REVISION_REQUESTED")
    void resubmitOpening_Success() {
        opening.setStatus(OpeningStatus.REVISION_REQUESTED);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        ResubmitOpeningResponse response = openingService.resubmitOpening(openingId);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.PENDING_APPROVAL);
        assertThat(opening.getLastResubmittedBy()).isEqualTo(mockUser.getId());
        verify(submissionValidator).validate(opening);
    }

    @Test
    @DisplayName("Should throw exception if resubmitting non-revision-requested opening")
    void resubmitOpening_InvalidState() {
        opening.setStatus(OpeningStatus.DRAFT);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterprise.getId())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> openingService.resubmitOpening(openingId));
    }
}
