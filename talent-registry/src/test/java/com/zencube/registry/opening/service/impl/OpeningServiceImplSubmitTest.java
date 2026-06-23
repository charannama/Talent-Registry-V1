package com.zencube.registry.opening.service.impl;

import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.exception.OwnershipViolationException;
import com.zencube.registry.enterprise.security.EnterpriseSecurity;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.exception.DeadlineExpiredException;
import com.zencube.registry.opening.exception.IncompleteOpeningException;
import com.zencube.registry.opening.exception.OpeningNotDraftException;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.opening.validator.OpeningSubmissionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpeningServiceImplSubmitTest {

    @Mock
    private OpeningRepository openingRepository;

    @Mock
    private EnterpriseSecurity enterpriseSecurity;

    @Mock
    private OpeningSubmissionValidator submissionValidator;

    @InjectMocks
    private OpeningServiceImpl openingService;

    private UUID openingId;
    private UUID enterpriseId;
    private EnterpriseAccount enterprise;
    private Opening opening;

    @BeforeEach
    void setUp() {
        openingId = UUID.randomUUID();
        enterpriseId = UUID.randomUUID();

        enterprise = new EnterpriseAccount();
        enterprise.setId(enterpriseId);
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.APPROVED);

        opening = new Opening();
        opening.setId(openingId);
        opening.setEnterprise(enterprise);
        opening.setStatus(OpeningStatus.DRAFT);
        opening.setApplicationDeadline(Instant.now().plus(7, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("Should successfully submit a valid draft opening")
    void submitOpening_Success() {
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(true);
        doNothing().when(submissionValidator).validate(opening);
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        OpeningResponse response = openingService.submitOpening(openingId);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.PENDING_APPROVAL);
        verify(openingRepository).save(opening);
    }

    @Test
    @DisplayName("Should throw exception if opening not found")
    void submitOpening_NotFound() {
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> openingService.submitOpening(openingId));
    }

    @Test
    @DisplayName("Should throw exception if enterprise not owned by user")
    void submitOpening_OwnershipViolation() {
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(false);

        assertThrows(OwnershipViolationException.class, () -> openingService.submitOpening(openingId));
    }

    @Test
    @DisplayName("Should throw exception if enterprise not approved")
    void submitOpening_EnterpriseNotApproved() {
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.PENDING_HR_REVIEW);
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> openingService.submitOpening(openingId));
        assertThat(exception.getErrorCode()).isEqualTo("ENTERPRISE_NOT_APPROVED");
    }

    @Test
    @DisplayName("Should throw exception if opening is not DRAFT")
    void submitOpening_NotDraft() {
        opening.setStatus(OpeningStatus.LIVE);
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(true);

        assertThrows(OpeningNotDraftException.class, () -> openingService.submitOpening(openingId));
    }

    @Test
    @DisplayName("Should throw exception if validation fails for missing fields")
    void submitOpening_Incomplete() {
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(true);
        doThrow(new IncompleteOpeningException("Missing title")).when(submissionValidator).validate(opening);

        assertThrows(IncompleteOpeningException.class, () -> openingService.submitOpening(openingId));
    }

    @Test
    @DisplayName("Should throw exception if deadline is in the past")
    void submitOpening_DeadlineExpired() {
        opening.setApplicationDeadline(Instant.now().minus(1, ChronoUnit.DAYS));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(enterpriseSecurity.isOwner(enterpriseId)).thenReturn(true);
        doNothing().when(submissionValidator).validate(opening);

        assertThrows(DeadlineExpiredException.class, () -> openingService.submitOpening(openingId));
    }
}
