package com.zencube.registry.opening.service.impl;

import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.request.CreateOpeningRequest;
import com.zencube.registry.opening.dto.request.UpdateOpeningRequest;
import com.zencube.registry.opening.dto.request.RejectionRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.dto.response.PaginatedOpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.mapper.OpeningMapper;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.enterprise.exception.OwnershipViolationException;
import com.zencube.registry.enterprise.security.EnterpriseSecurity;
import com.zencube.registry.opening.exception.DeadlineExpiredException;
import com.zencube.registry.opening.exception.IncompleteOpeningException;
import com.zencube.registry.opening.exception.OpeningNotDraftException;
import com.zencube.registry.opening.exception.OpeningNotPendingApprovalException;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.opening.service.OpeningService;
import com.zencube.registry.opening.validator.OpeningSubmissionValidator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpeningServiceImpl implements OpeningService {

    private final OpeningRepository openingRepository;
    private final EnterpriseAccountRepository enterpriseRepository;
    private final EnterpriseSecurity enterpriseSecurity;
    private final OpeningSubmissionValidator submissionValidator;
    private final com.zencube.registry.eligibility.service.EligibilityService eligibilityService;

    @Override
    @Transactional
    public OpeningResponse createOpening(CreateOpeningRequest request) {
        log.info("Creating job opening with title: {} for enterprise ID: {}", request.getTitle(), request.getEnterpriseId());

        // 1. Fetch and load enterprise
        EnterpriseAccount enterprise = enterpriseRepository.findById(request.getEnterpriseId())
                .orElseThrow(() -> new ResourceNotFoundException("EnterpriseAccount", "id", request.getEnterpriseId()));

        // 2. Validate enterprise state
        if (enterprise.isSuspended()) {
            throw new BusinessException("Enterprise is suspended", HttpStatus.BAD_REQUEST, "ENTERPRISE_SUSPENDED");
        }
        if (!enterprise.isApproved()) {
            throw new BusinessException("Enterprise is not approved", HttpStatus.BAD_REQUEST, "ENTERPRISE_NOT_APPROVED");
        }

        // 3. Validate request: Duplicate title check (only within active/non-deleted openings of same enterprise)
        if (openingRepository.existsByEnterpriseIdAndTitleIgnoreCaseAndDeletedFalse(request.getEnterpriseId(), request.getTitle())) {
            throw new ConflictException("Opening", "title", request.getTitle());
        }

        // 4. Validate request: Salary range validation
        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            if (request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
                throw new BusinessException("Minimum salary cannot be greater than maximum salary", 
                        HttpStatus.BAD_REQUEST, "INVALID_SALARY_RANGE");
            }
        }

        // 5. Validate request: Application deadline validation (programmatic check)
        if (request.getDeadline() != null && request.getDeadline().isBefore(Instant.now())) {
            throw new BusinessException("Application deadline must be in the future", 
                        HttpStatus.BAD_REQUEST, "INVALID_DEADLINE");
        }

        // 6. Map and Save Opening
        Opening opening = OpeningMapper.toEntity(request, enterprise);
        opening.setStatus(OpeningStatus.DRAFT); // Explicitly ensure status is DRAFT

        Opening savedOpening = openingRepository.save(opening);
        log.info("Successfully created job opening ID: {} in DRAFT status", savedOpening.getId());

        return OpeningMapper.toResponse(savedOpening);
    }

    @Override
    @Transactional(readOnly = true)
    public OpeningResponse getOpening(UUID id) {
        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));
        return OpeningMapper.toResponse(opening);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpeningResponse> listOpenings(UUID enterpriseId) {
        if (!enterpriseRepository.existsById(enterpriseId)) {
            throw new ResourceNotFoundException("EnterpriseAccount", "id", enterpriseId);
        }
        List<Opening> openings = openingRepository.findByEnterpriseIdAndDeletedFalse(enterpriseId);
        return openings.stream()
                .map(OpeningMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OpeningResponse submitOpening(UUID id) {
        log.info("Submitting opening ID: {} for HR approval", id);

        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));

        // Ownership Verification
        if (!enterpriseSecurity.isOwner(opening.getEnterprise().getId())) {
            throw new OwnershipViolationException("You do not have permission to submit this opening.");
        }

        // Enterprise State Verification
        EnterpriseAccount enterprise = opening.getEnterprise();
        if (!enterprise.isApproved() || enterprise.isSuspended()) {
            throw new BusinessException("Enterprise must be APPROVED to submit openings", HttpStatus.BAD_REQUEST, "ENTERPRISE_NOT_APPROVED");
        }

        // Validate Status
        if (opening.getStatus() != OpeningStatus.DRAFT) {
            throw new OpeningNotDraftException("Only DRAFT openings can be submitted.");
        }

        // Validate Completeness
        submissionValidator.validate(opening);

        // Validate Deadline
        if (opening.getApplicationDeadline().isBefore(Instant.now())) {
            throw new DeadlineExpiredException("Application deadline must be a future date.");
        }

        // Submit (State Machine)
        opening.submit();

        Opening savedOpening = openingRepository.save(opening);
        log.info("Successfully submitted job opening ID: {}", savedOpening.getId());

        return OpeningMapper.toResponse(savedOpening);
    }

    @Override
    @Transactional
    public OpeningResponse approveOpening(UUID openingId) {
        log.info("HR is approving opening ID: {}", openingId);
        
        // 1. Get authenticated HR user
        User hrUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2 & 3. Load opening & verify exists
        Opening opening = openingRepository.findByIdAndDeletedFalse(openingId)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", openingId));

        // 4. Verify status
        if (opening.getStatus() != OpeningStatus.PENDING_APPROVAL) {
            throw new OpeningNotPendingApprovalException("Only PENDING_APPROVAL openings can be approved.");
        }

        // 5. Execute domain approve logic
        opening.approve(hrUser.getId());

        // 6. Persist entity
        Opening savedOpening = openingRepository.save(opening);
        log.info("Opening ID: {} successfully approved and made LIVE by HR User: {}", savedOpening.getId(), hrUser.getId());

        // 7. Return response
        return OpeningMapper.toResponse(savedOpening);
    }

    @Override
    @Transactional
    public OpeningResponse updateDraft(UUID id, UpdateOpeningRequest request) {
        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));

        if (!enterpriseSecurity.isOwner(opening.getEnterprise().getId())) {
            throw new OwnershipViolationException("You do not have permission to update this job opening.");
        }

        if (opening.getStatus() != OpeningStatus.DRAFT && opening.getStatus() != OpeningStatus.REVISION_REQUESTED) {
            throw new OpeningNotDraftException("Only DRAFT or REVISION_REQUESTED openings can be updated.");
        }

        opening.setTitle(request.getTitle());
        opening.setDescription(request.getDescription());
        opening.setRequirements(request.getRequirements());
        opening.setLocation(request.getLocation());
        opening.setJobType(request.getJobType());
        opening.setDomain(request.getDomain());
        opening.setSalaryMin(request.getSalaryMin());
        opening.setSalaryMax(request.getSalaryMax());
        opening.setWorkMode(request.getWorkMode());
        opening.setPositions(request.getPositions());
        opening.setApplicationDeadline(request.getDeadline());

        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            opening.setRequiredSkills(String.join(",", request.getRequiredSkills()));
        } else {
            opening.setRequiredSkills(null);
        }

        if (request.getGraduationYears() != null && !request.getGraduationYears().isEmpty()) {
            opening.setGraduationYears(String.join(",", request.getGraduationYears()));
        } else {
            opening.setGraduationYears(null);
        }

        Opening savedOpening = openingRepository.save(opening);
        return OpeningMapper.toResponse(savedOpening);
    }

    @Override
    @Transactional
    public OpeningResponse rejectOpening(UUID id, RejectionRequest request) {
        log.info("Rejecting opening ID: {}", id);
        User hrUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));
        
        opening.reject(hrUser.getId(), request.getReason(), request.getCanResubmit());
        Opening savedOpening = openingRepository.save(opening);
        return OpeningMapper.toResponse(savedOpening);
    }

    @Override
    @Transactional
    public OpeningResponse requestRevision(UUID id, com.zencube.registry.opening.dto.request.RequestRevisionRequest request) {
        log.info("HR is requesting revision for opening ID: {}", id);
        User hrUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));

        opening.requestRevision(hrUser.getId(), request.getFeedback());
        Opening savedOpening = openingRepository.save(opening);
        return OpeningMapper.toResponse(savedOpening);
    }

    @Override
    @Transactional
    public com.zencube.registry.opening.dto.response.ResubmitOpeningResponse resubmitOpening(UUID id) {
        log.info("Enterprise is resubmitting opening ID: {}", id);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));

        if (!enterpriseSecurity.isOwner(opening.getEnterprise().getId())) {
            throw new OwnershipViolationException("You do not have permission to resubmit this job opening.");
        }
        
        if (!opening.getEnterprise().isApproved()) {
            throw new BusinessException("Enterprise is not approved", HttpStatus.BAD_REQUEST, "ENTERPRISE_NOT_APPROVED");
        }

        submissionValidator.validate(opening);

        opening.resubmit(user.getId());
        Opening savedOpening = openingRepository.save(opening);
        
        return com.zencube.registry.opening.dto.response.ResubmitOpeningResponse.builder()
                .openingId(savedOpening.getId())
                .status(savedOpening.getStatus())
                .submittedAt(savedOpening.getLastResubmittedAt())
                .message("Opening resubmitted successfully")
                .build();
    }

    @Override
    @Transactional
    public com.zencube.registry.opening.dto.response.CloseOpeningResponse closeOpening(UUID id, com.zencube.registry.opening.dto.request.CloseOpeningRequest request) {
        log.info("Closing opening ID: {}", id);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));

        // Enterprise Rules: Must own opening, Must own enterprise, Enterprise must be APPROVED
        // HR Rules: Can close any opening, Requires OPENING_MANAGE permission (or OPENING_CLOSE which HR has)
        boolean isOwner = enterpriseSecurity.isOwner(opening.getEnterprise().getId());
        boolean hasAdminAccess = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("OPENING_MANAGE") || a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR_STAFF"));

        if (!isOwner && !hasAdminAccess) {
            throw new OwnershipViolationException("You do not have permission to close this job opening.");
        }

        if (isOwner && !opening.getEnterprise().isApproved()) {
            throw new BusinessException("Enterprise is not approved", HttpStatus.BAD_REQUEST, "ENTERPRISE_NOT_APPROVED");
        }

        String reason = request != null ? request.getReason() : null;
        opening.close(user.getId(), reason);
        Opening savedOpening = openingRepository.save(opening);
        
        return com.zencube.registry.opening.dto.response.CloseOpeningResponse.builder()
                .id(savedOpening.getId())
                .status(savedOpening.getStatus())
                .closedBy(savedOpening.getClosedBy())
                .closedAt(savedOpening.getClosedAt())
                .message("Opening closed successfully")
                .build();
    }

    @Override
    @Transactional
    public OpeningResponse archiveOpening(UUID id) {
        Opening opening = openingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opening", "id", id));

        if (!enterpriseSecurity.isOwner(opening.getEnterprise().getId())) {
            throw new OwnershipViolationException("You do not have permission to archive this job opening.");
        }

        opening.archive();
        Opening savedOpening = openingRepository.save(opening);
        return OpeningMapper.toResponse(savedOpening);
    }

    @Override
    public PaginatedOpeningResponse listEnterpriseOpenings(UUID enterpriseId, Pageable pageable) {
        Page<Opening> page = openingRepository.findByEnterpriseIdAndDeletedFalse(enterpriseId, pageable);
        return mapToPaginatedResponse(page);
    }

    @Override
    public PaginatedOpeningResponse listPendingOpenings(Pageable pageable) {
        Page<Opening> page = openingRepository.findByStatusAndDeletedFalse(OpeningStatus.PENDING_APPROVAL, pageable);
        return mapToPaginatedResponse(page);
    }

    @Override
    public PaginatedOpeningResponse listMyEnterpriseOpenings(Pageable pageable) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        EnterpriseAccount enterprise = enterpriseRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Enterprise account not found for current user", HttpStatus.NOT_FOUND, "ENTERPRISE_NOT_FOUND"));
        
        Page<Opening> page = openingRepository.findByEnterpriseIdAndDeletedFalse(enterprise.getId(), pageable);
        return mapToPaginatedResponse(page);
    }

    private PaginatedOpeningResponse mapToPaginatedResponse(Page<Opening> page) {
        List<OpeningResponse> content = page.getContent().stream()
                .map(OpeningMapper::toResponse)
                .toList();

        return PaginatedOpeningResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse browseOpenings(org.springframework.data.domain.Pageable pageable) {
        log.info("Browsing live job openings. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        org.springframework.data.domain.Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "featured")
                        .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "publishedAt"))
        );

        org.springframework.data.domain.Page<Opening> openingPage = openingRepository.findActiveLiveOpenings(Instant.now(), sortedPageable);
        
        List<com.zencube.registry.opening.dto.response.OpeningSummaryResponse> content = openingPage.getContent().stream()
                .map(OpeningMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse.builder()
                .content(content)
                .pageNumber(openingPage.getNumber())
                .pageSize(openingPage.getSize())
                .totalElements(openingPage.getTotalElements())
                .totalPages(openingPage.getTotalPages())
                .last(openingPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse searchOpenings(
            com.zencube.registry.opening.dto.request.OpeningSearchCriteria criteria,
            org.springframework.data.domain.Pageable pageable) {

        log.info("Student searching job openings with criteria: {}, Page: {}, Size: {}", criteria, pageable.getPageNumber(), pageable.getPageSize());

        if (pageable.getPageSize() > 100) {
            throw new com.zencube.registry.common.exception.BusinessException("Page size must not exceed 100", org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE");
        }
        if (pageable.getPageSize() < 1) {
            throw new com.zencube.registry.common.exception.BusinessException("Page size must be at least 1", org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE");
        }

        org.springframework.data.jpa.domain.Specification<Opening> spec;
        
        if (Boolean.TRUE.equals(criteria.getEligibleOnly())) {
            User studentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            com.zencube.registry.eligibility.dto.StudentEligibilityResponse eligibility = eligibilityService.getStudentEligibility(studentUser.getId());
            spec = com.zencube.registry.opening.specification.OpeningSpecification.search(criteria, eligibility, eligibility.getProfileId());
        } else {
            spec = com.zencube.registry.opening.specification.OpeningSpecification.search(criteria);
        }
        
        org.springframework.data.domain.Page<Opening> openingPage = openingRepository.findAll(spec, pageable);

        List<com.zencube.registry.opening.dto.response.OpeningSummaryResponse> content = openingPage.getContent().stream()
                .map(com.zencube.registry.opening.mapper.OpeningMapper::toSummaryResponse)
                .collect(java.util.stream.Collectors.toList());

        return com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse.builder()
                .content(content)
                .pageNumber(openingPage.getNumber())
                .pageSize(openingPage.getSize())
                .totalElements(openingPage.getTotalElements())
                .totalPages(openingPage.getTotalPages())
                .last(openingPage.isLast())
                .build();
    }
}
