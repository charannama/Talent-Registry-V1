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
import java.util.UUID;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.entity.CalendarParticipant;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.enums.ParticipantType;
import com.zencube.registry.calendar.enums.ParticipantResponseStatus;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.calendar.repository.CalendarParticipantRepository;
import com.zencube.registry.calendar.exception.EventConflictException;
import com.zencube.registry.application.exception.InvalidInterviewStateException;
import com.zencube.registry.application.event.InterviewScheduledEvent;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.activity.service.ActivityService;
import org.springframework.context.annotation.Lazy;
import com.zencube.registry.successstory.service.SuccessStoryService;
import com.zencube.registry.application.event.ApplicationStatusChangedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final EnterpriseAccountRepository enterpriseAccountRepository;
    private final OpeningRepository openingRepository;
    private final StudentProfileRepository studentProfileRepository;
    @Lazy private final SuccessStoryService successStoryService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    private final CalendarEventRepository calendarEventRepository;
    private final CalendarParticipantRepository calendarParticipantRepository;
    private final AuditService auditService;
    private final ActivityService activityService;

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

            // Domain Event Integration
            try {
                eventPublisher.publishEvent(
                    ApplicationStatusChangedEvent.builder()
                        .applicationId(savedApplication.getId())
                        .studentId(profile.getUser().getId())
                        .enterpriseId(opening.getEnterprise().getId())
                        .openingId(opening.getId())
                        .oldStatus(null)
                        .newStatus(ApplicationStatus.APPLIED)
                        .actorId(currentUserId)
                        .actorType("Student")
                        .occurredAt(java.time.Instant.now())
                        .build()
                );
            } catch(Exception e) {
                log.warn("Failed to publish ApplicationStatusChangedEvent for APPLIED transition: {}", e.getMessage());
            }

            return savedApplication;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Race condition blocked by database unique constraint. User {}, Opening {}", currentUserId, openingId);
            throw new DuplicateApplicationException("You have already applied to this opening");
        }
    }

    @Override
    @Transactional
    public void updateApplicationStatus(UUID applicationId, ApplicationStatus newStatus) {
        log.info("Updating application {} to status {}", applicationId, newStatus);
        
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));
                
        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        application.setLastStageUpdatedAt(java.time.Instant.now());
        
        applicationRepository.save(application);

        // Domain Event Integration
        try {
            eventPublisher.publishEvent(
                ApplicationStatusChangedEvent.builder()
                    .applicationId(application.getId())
                    .studentId(application.getProfile().getUser().getId())
                    .enterpriseId(application.getOpening().getEnterprise().getId())
                    .openingId(application.getOpening().getId())
                    .oldStatus(oldStatus) 
                    .newStatus(newStatus)
                    .actorId(getCurrentUserId())
                    .actorType("User") // Or determine based on role
                    .occurredAt(java.time.Instant.now())
                    .build()
            );
        } catch(Exception e) {
            log.warn("Failed to publish ApplicationStatusChangedEvent: {}", e.getMessage());
        }
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000"); 
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000"); 
        }
    }

    @Override
    @Transactional
    public void assignHandler(UUID applicationId, UUID handlerId) {
        log.info("Assigning handler {} to application {}", handlerId, applicationId);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));
                
        application.assignHandler(handlerId);
        applicationRepository.save(application);
    }

    @Override
    @Transactional
    public void unassignHandler(UUID applicationId) {
        log.info("Unassigning handler from application {}", applicationId);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));
                
        application.unassignHandler();
        applicationRepository.save(application);
    }

    @Override
    @Transactional
    public Application scheduleInterview(UUID applicationId, java.time.Instant startTime, java.time.Instant endTime, String timezone, String location, String interviewNotes, UUID currentUserId) {
        log.info("Scheduling interview for application {} by user {}", applicationId, currentUserId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));

        if (!ApplicationStatus.FORWARDED.equals(application.getStatus()) && !ApplicationStatus.INTERVIEW_SCHEDULED.equals(application.getStatus())) {
            throw new InvalidInterviewStateException("Interview can only be scheduled for applications in FORWARDED or INTERVIEW_SCHEDULED status. Current status: " + application.getStatus());
        }

        boolean conflict = calendarEventRepository.existsConflictingEvent("APPLICATION", application.getId(), startTime, endTime, application.hasInterviewScheduled() ? application.getInterviewEvent().getId() : null);
        if (conflict) {
            throw new EventConflictException("Calendar conflict detected for this application time slot.");
        }

        StudentProfile profile = application.getProfile();
        Opening opening = application.getOpening();
        EnterpriseAccount enterprise = opening.getEnterprise();

        String title = "Interview - " + profile.getUser().getFirstName() + " " + profile.getUser().getLastName() + " - " + opening.getTitle();
        String description = String.format("Application ID: %s\nStudent: %s %s\nOpening: %s\nEnterprise: %s\nNotes: %s",
                application.getId(), profile.getUser().getFirstName(), profile.getUser().getLastName(),
                opening.getTitle(), enterprise.getCompanyName(), interviewNotes != null ? interviewNotes : "");

        CalendarEvent calendarEvent = CalendarEvent.builder()
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .timezone(timezone)
                .location(location)
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("APPLICATION")
                .eventableId(application.getId())
                .build();

        calendarEvent = calendarEventRepository.save(calendarEvent);

        CalendarParticipant studentParticipant = CalendarParticipant.builder()
                .event(calendarEvent)
                .participantType(ParticipantType.INTERNAL)
                .user(profile.getUser())
                .responseStatus(ParticipantResponseStatus.PENDING)
                .build();

        com.zencube.registry.auth.entity.User hrUser = new com.zencube.registry.auth.entity.User();
        hrUser.setId(currentUserId);
        CalendarParticipant hrParticipant = CalendarParticipant.builder()
                .event(calendarEvent)
                .participantType(ParticipantType.INTERNAL)
                .user(hrUser)
                .responseStatus(ParticipantResponseStatus.ACCEPTED)
                .build();

        CalendarParticipant enterpriseParticipant = CalendarParticipant.builder()
                .event(calendarEvent)
                .participantType(ParticipantType.INTERNAL)
                .user(enterprise.getUser())
                .responseStatus(ParticipantResponseStatus.ACCEPTED)
                .build();

        calendarParticipantRepository.saveAll(List.of(studentParticipant, hrParticipant, enterpriseParticipant));

        ApplicationStatus oldStatus = application.getStatus();
        
        application.assignInterviewEvent(calendarEvent);
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        application.setLastStageUpdatedAt(java.time.Instant.now());
        if (application.getCurrentHandlerId() == null) {
            application.assignHandler(currentUserId);
        }
        
        applicationRepository.save(application);

        auditService.recordCustomEvent(
                oldStatus == ApplicationStatus.FORWARDED ? "INTERVIEW_CREATED" : "INTERVIEW_RESCHEDULED",
                "Application",
                application.getId().toString(),
                "Event ID: " + calendarEvent.getId()
        );

        activityService.recordActivity("Application", application.getId().toString(), "APPLICATION", application.getId().toString(), com.zencube.registry.activity.enums.ActivityType.INTERVIEW_SCHEDULED, "Interview scheduled for " + profile.getUser().getEmail());

        try {
            eventPublisher.publishEvent(
                InterviewScheduledEvent.builder()
                    .applicationId(application.getId())
                    .calendarEventId(calendarEvent.getId())
                    .studentId(profile.getUser().getId())
                    .enterpriseId(enterprise.getId())
                    .scheduledTime(startTime)
                    .scheduledBy(currentUserId)
                    .build()
            );
        } catch(Exception e) {
            log.warn("Failed to publish InterviewScheduledEvent: {}", e.getMessage());
        }

        return application;
    }
}

