package com.zencube.registry.opening.service.impl;

import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.request.CreateOpeningRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.enums.WorkMode;
import com.zencube.registry.opening.repository.OpeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpeningServiceImplTest {

    @Mock
    private OpeningRepository openingRepository;

    @Mock
    private EnterpriseAccountRepository enterpriseRepository;

    @InjectMocks
    private OpeningServiceImpl openingService;

    private UUID enterpriseId;
    private EnterpriseAccount enterprise;
    private CreateOpeningRequest validRequest;

    @BeforeEach
    void setUp() {
        enterpriseId = UUID.randomUUID();
        enterprise = new EnterpriseAccount();
        enterprise.setId(enterpriseId);
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.APPROVED);
        enterprise.setAccountActive(true);

        validRequest = CreateOpeningRequest.builder()
                .enterpriseId(enterpriseId)
                .title("Software Engineer")
                .description("Write good code")
                .requirements("Java 21")
                .location("Remote")
                .jobType(JobType.FULL_TIME)
                .domain("Engineering")
                .salaryMin(new BigDecimal("80000.00"))
                .salaryMax(new BigDecimal("120000.00"))
                .workMode(WorkMode.REMOTE)
                .positions(1)
                .deadline(Instant.now().plus(30, ChronoUnit.DAYS))
                .requiredSkills(List.of("Java", "Spring"))
                .graduationYears(List.of("2024"))
                .build();
    }

    @Test
    void createOpening_Success() {
        // Arrange
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));
        when(openingRepository.existsByEnterpriseIdAndTitleIgnoreCaseAndDeletedFalse(enterpriseId, "Software Engineer"))
                .thenReturn(false);

        Opening savedOpening = Opening.builder()
                .title(validRequest.getTitle())
                .enterprise(enterprise)
                .status(OpeningStatus.DRAFT)
                .requiredSkills("Java,Spring")
                .graduationYears("2024")
                .build();
        savedOpening.setId(UUID.randomUUID());

        when(openingRepository.save(any(Opening.class))).thenReturn(savedOpening);

        // Act
        OpeningResponse response = openingService.createOpening(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(savedOpening.getId(), response.getId());
        assertEquals(OpeningStatus.DRAFT, response.getStatus());
        assertEquals(enterpriseId, response.getEnterpriseId());
        assertEquals(List.of("Java", "Spring"), response.getRequiredSkills());
        verify(openingRepository, times(1)).save(any(Opening.class));
    }

    @Test
    void createOpening_EnterpriseNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> openingService.createOpening(validRequest));
        verify(openingRepository, never()).save(any(Opening.class));
    }

    @Test
    void createOpening_EnterpriseNotApproved_ThrowsBusinessException() {
        // Arrange
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.PENDING_HR_REVIEW);
        enterprise.setAccountActive(false);
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> openingService.createOpening(validRequest));
        assertEquals("ENTERPRISE_NOT_APPROVED", ex.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        verify(openingRepository, never()).save(any(Opening.class));
    }

    @Test
    void createOpening_EnterpriseSuspended_ThrowsBusinessException() {
        // Arrange
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.SUSPENDED);
        enterprise.setAccountActive(false);
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> openingService.createOpening(validRequest));
        assertEquals("ENTERPRISE_SUSPENDED", ex.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        verify(openingRepository, never()).save(any(Opening.class));
    }

    @Test
    void createOpening_DuplicateTitle_ThrowsConflictException() {
        // Arrange
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));
        when(openingRepository.existsByEnterpriseIdAndTitleIgnoreCaseAndDeletedFalse(enterpriseId, "Software Engineer"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> openingService.createOpening(validRequest));
        verify(openingRepository, never()).save(any(Opening.class));
    }

    @Test
    void createOpening_InvalidSalaryRange_ThrowsBusinessException() {
        // Arrange
        validRequest.setSalaryMin(new BigDecimal("150000.00"));
        validRequest.setSalaryMax(new BigDecimal("100000.00"));
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> openingService.createOpening(validRequest));
        assertEquals("INVALID_SALARY_RANGE", ex.getErrorCode());
        verify(openingRepository, never()).save(any(Opening.class));
    }

    @Test
    void createOpening_PastDeadline_ThrowsBusinessException() {
        // Arrange
        validRequest.setDeadline(Instant.now().minus(1, ChronoUnit.DAYS));
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> openingService.createOpening(validRequest));
        assertEquals("INVALID_DEADLINE", ex.getErrorCode());
        verify(openingRepository, never()).save(any(Opening.class));
    }

    @Test
    void getOpening_Success() {
        // Arrange
        UUID openingId = UUID.randomUUID();
        Opening opening = Opening.builder().title("Engineer").enterprise(enterprise).status(OpeningStatus.DRAFT).build();
        opening.setId(openingId);

        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));

        // Act
        OpeningResponse response = openingService.getOpening(openingId);

        // Assert
        assertNotNull(response);
        assertEquals(openingId, response.getId());
        assertEquals("Engineer", response.getTitle());
    }

    @Test
    void getOpening_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        UUID openingId = UUID.randomUUID();
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> openingService.getOpening(openingId));
    }
}
