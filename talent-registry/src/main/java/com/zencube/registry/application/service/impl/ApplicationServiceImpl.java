package com.zencube.registry.application.service.impl;

import com.zencube.registry.application.dto.response.ApplicationPageResponse;
import com.zencube.registry.application.dto.response.PendingApplicationResponse;
import com.zencube.registry.application.entity.Application;
import com.zencube.registry.application.exception.InvalidApplicationStatusException;
import com.zencube.registry.application.exception.InvalidPaginationException;
import com.zencube.registry.application.mapper.ApplicationMapper;
import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.application.service.ApplicationService;
import com.zencube.registry.application.specification.ApplicationSpecification;
import com.zencube.registry.common.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zencube.registry.application.dto.response.EnterpriseApplicationPageResponse;
import com.zencube.registry.application.dto.response.EnterpriseApplicationResponse;
import com.zencube.registry.application.exception.UnauthorizedEnterpriseAccessException;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.exception.EnterpriseNotFoundException;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.exception.OpeningNotFoundException;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.application.exception.DuplicateApplicationException;
import org.springframework.dao.DataIntegrityViolationException;
import com.zencube.registry.opening.exception.OpeningNotFoundException;
import com.zencube.registry.opening.repository.OpeningRepository;

import java.util.UUID;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final EnterpriseAccountRepository enterpriseAccountRepository;
    private final OpeningRepository openingRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public EnterpriseApplicationPageResponse<EnterpriseApplicationResponse> getForwardedApplicationsForEnterprise(UUID openingId, String search, String status, int page, int size, String sort, String direction, UUID currentUserId) {
        log.info("Fetching enterprise applications for opening: {}, Status: {}, Search: {}", openingId, status, search);

        // 1. Validate Enterprise Account exists for user
        EnterpriseAccount enterprise = enterpriseAccountRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new EnterpriseNotFoundException("Enterprise account not found for current user"));

        // 2. Validate Opening exists
        Opening opening = openingRepository.findById(openingId)
                .orElseThrow(() -> new OpeningNotFoundException("Opening not found: " + openingId));

        // 3. Validate Opening Ownership
        if (opening.getEnterprise() == null || !opening.getEnterprise().getId().equals(enterprise.getId())) {
            log.warn("Enterprise {} attempted to access opening {} which belongs to another enterprise", enterprise.getId(), opening.getId());
            throw new UnauthorizedEnterpriseAccessException("You do not have permission to view applications for this opening");
        }

        // 4. Validate Requested Status (Cannot request APPLIED, UNDER_REVIEW, WITHDRAWN)
        Set<ApplicationStatus> allowedStatuses = Set.of(ApplicationStatus.FORWARDED, ApplicationStatus.INTERVIEW_SCHEDULED, ApplicationStatus.SELECTED, ApplicationStatus.REJECTED);
        if (status != null && !status.trim().isEmpty()) {
            try {
                ApplicationStatus reqStatus = ApplicationStatus.valueOf(status.toUpperCase());
                if (!allowedStatuses.contains(reqStatus)) {
                    throw new InvalidApplicationStatusException("Enterprise cannot view applications with status: " + status);
                }
            } catch (IllegalArgumentException e) {
                throw new InvalidApplicationStatusException("Invalid status: " + status);
            }
        }

        // 5. Pagination and Sorting
        if (page < 0) throw new InvalidPaginationException("Page index must not be less than zero");
        if (size < 1 || size > 100) throw new InvalidPaginationException("Page size must not be less than one or greater than 100");
        
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPaginationException("Invalid sort direction: " + direction);
        }
        Sort pageSort = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, pageSort);

        // 6. Fetch Applications
        Page<Application> applicationPage = applicationRepository.findAll(
                ApplicationSpecification.getEnterpriseApplications(openingId, search, status),
                pageable
        );

        List<EnterpriseApplicationResponse> content = applicationPage.getContent().stream()
                .map(applicationMapper::toEnterpriseResponse)
                .collect(Collectors.toList());

        log.info("Successfully fetched {} forwarded applications for enterprise queue", content.size());

        return EnterpriseApplicationPageResponse.<EnterpriseApplicationResponse>builder()
                .content(content)
                .pageNumber(applicationPage.getNumber())
                .pageSize(applicationPage.getSize())
                .totalElements(applicationPage.getTotalElements())
                .totalPages(applicationPage.getTotalPages())
                .last(applicationPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationPageResponse<PendingApplicationResponse> getPendingReviewQueue(String status, String search, int page, int size, String sort, String direction) {
        log.info("Fetching pending review queue. Status: {}, Search: {}, Page: {}, Size: {}", status, search, page, size);

        // Validate Status
        if (status == null || !ApplicationStatus.APPLIED.name().equalsIgnoreCase(status)) {
            throw new InvalidApplicationStatusException("Only APPLIED status is supported for this queue");
        }

        // Validate Pagination Limits
        if (page < 0) {
            throw new InvalidPaginationException("Page index must not be less than zero");
        }
        if (size < 1 || size > 100) {
            throw new InvalidPaginationException("Page size must not be less than one or greater than 100");
        }

        // Validate Sort Direction
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPaginationException("Invalid sort direction: " + direction);
        }
        
        Sort pageSort = Sort.by(sortDirection, sort);

        Pageable pageable = PageRequest.of(page, size, pageSort);

        Page<Application> applicationPage = applicationRepository.findAll(
                ApplicationSpecification.getPendingApplications(search),
                pageable
        );

        List<PendingApplicationResponse> content = applicationPage.getContent().stream()
                .map(applicationMapper::toPendingResponse)
                .collect(Collectors.toList());

        log.info("Successfully fetched {} applications for pending review queue", content.size());

        return ApplicationPageResponse.<PendingApplicationResponse>builder()
                .content(content)
                .pageNumber(applicationPage.getNumber())
                .pageSize(applicationPage.getSize())
                .totalElements(applicationPage.getTotalElements())
                .totalPages(applicationPage.getTotalPages())
                .last(applicationPage.isLast())
                .build();
    }
    @Override
    @Transactional
    public Application applyToOpening(UUID openingId, UUID currentUserId) {
        log.info("Attempting application for user {} to opening {}", currentUserId, openingId);

        // Validate Opening exists and is active
        Opening opening = openingRepository.findById(openingId)
                .orElseThrow(() -> new OpeningNotFoundException("Opening not found: " + openingId));

        // Validate Student Profile exists
        StudentProfile profile = studentProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BusinessException("Student profile not found. You must create a profile before applying."));

        // Layer 1 Protection: Application level check
        if (applicationRepository.existsByProfileIdAndOpeningId(profile.getId(), openingId)) {
            log.warn("Duplicate application detected at validation layer. User {}, Opening {}", currentUserId, openingId);
            throw new DuplicateApplicationException("You have already applied to this opening");
        }

        try {
            Application newApplication = Application.builder()
                    .profile(profile)
                    .opening(opening)
                    .status(ApplicationStatus.APPLIED)
                    .appliedAt(java.time.Instant.now())
                    .build();

            // Layer 2 Protection: Database level unique constraint
            Application savedApplication = applicationRepository.saveAndFlush(newApplication);
            log.info("Successfully created application {} for user {} to opening {}", savedApplication.getId(), currentUserId, openingId);
            return savedApplication;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Race condition blocked by database unique constraint. User {}, Opening {}", currentUserId, openingId);
            throw new DuplicateApplicationException("You have already applied to this opening");
        }
    }
}
