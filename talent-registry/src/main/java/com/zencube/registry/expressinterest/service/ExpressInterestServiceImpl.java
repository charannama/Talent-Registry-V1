package com.zencube.registry.expressinterest.service;

import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.expressinterest.dto.FormalRequestResponse;
import com.zencube.registry.expressinterest.dto.InterestResponse;
import com.zencube.registry.expressinterest.entity.ExpressInterest;
import com.zencube.registry.expressinterest.enums.InterestStage;
import com.zencube.registry.expressinterest.exception.InterestException;
import com.zencube.registry.expressinterest.mapper.InterestMapper;
import com.zencube.registry.expressinterest.repository.ExpressInterestRepository;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressInterestServiceImpl implements ExpressInterestService {

    private final ExpressInterestRepository interestRepository;
    private final EnterpriseAccountRepository enterpriseRepository;
    private final StudentProfileRepository studentRepository;
    private final OpeningRepository openingRepository;
    private final InterestMapper interestMapper;
    private final ActivityService activityService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @Audited(action = JournalAction.CREATE, entityType = "EXPRESS_INTEREST", idParam = "none")
    public InterestResponse bookmark(UUID enterpriseId, UUID studentId, UUID openingId) {
        log.info("Creating bookmark for enterprise: {}, student: {}, opening: {}", enterpriseId, studentId, openingId);

        EnterpriseAccount enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new InterestException("Enterprise Account not found"));

        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new InterestException("Student Profile not found"));

        Opening opening = null;
        if (openingId != null) {
            opening = openingRepository.findById(openingId)
                    .orElseThrow(() -> new InterestException("Opening not found"));

            if (!opening.getEnterprise().getId().equals(enterpriseId)) {
                throw new InterestException("You cannot bookmark a student against an opening belonging to another enterprise.");
            }
        }

        if (interestRepository.findByEnterpriseIdAndStudentIdAndOpeningId(enterpriseId, studentId, openingId).isPresent()) {
            throw new InterestException("You have already bookmarked this candidate for this opening.");
        }

        ExpressInterest interest = ExpressInterest.builder()
                .enterprise(enterprise)
                .student(student)
                .opening(opening)
                .stage(InterestStage.BOOKMARK)
                .build();

        interest = interestRepository.save(interest);

        activityService.recordActivity(
                "EXPRESS_INTEREST",
                interest.getId().toString(),
                "STUDENT_PROFILE",
                student.getId().toString(),
                ActivityType.CANDIDATE_BOOKMARKED,
                enterprise.getCompanyName() + " bookmarked candidate"
        );

        return interestMapper.toResponse(interest);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.UPDATE, entityType = "EXPRESS_INTEREST", idParam = "interestId")
    public FormalRequestResponse formalRequest(UUID enterpriseId, UUID interestId) {
        log.info("Escalating interest to FORMAL_REQUEST: {}", interestId);

        ExpressInterest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestException("Express Interest not found"));

        if (!interest.getEnterprise().getId().equals(enterpriseId)) {
            throw new InterestException("You do not have permission to escalate this interest.");
        }

        if (interest.getStage() == InterestStage.FORMAL_REQUEST) {
            throw new InterestException("This interest has already been escalated to a formal request.");
        }

        if (interest.getStage() != InterestStage.BOOKMARK) {
            throw new InterestException("Only BOOKMARK stage interests can be escalated.");
        }

        interest.setStage(InterestStage.FORMAL_REQUEST);
        interest.setRequestedAt(Instant.now());
        interest = interestRepository.save(interest);

        // Notify Student
        eventPublisher.publishEvent(
            NotificationEvent.builder()
                .eventType(NotificationEventType.FORMAL_REQUEST_CREATED)
                .recipientId(interest.getStudent().getUser().getId())
                .resourceType("ExpressInterest")
                .resourceId(interest.getId())
                .title("Formal Request Received")
                .message("You have received a formal request from " + 
                    (interest.getEnterprise().getCompanyName() != null ? interest.getEnterprise().getCompanyName() : "An Enterprise"))
                .build()
        );

        // Activity Feed
        activityService.recordActivity(
                "EXPRESS_INTEREST",
                interest.getId().toString(),
                "STUDENT_PROFILE",
                interest.getStudent().getId().toString(),
                ActivityType.FORMAL_REQUEST_CREATED,
                "Formal request sent to candidate"
        );

        return interestMapper.toFormalRequestResponse(interest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterestResponse> getMyBookmarks(UUID enterpriseId, Pageable pageable) {
        return interestRepository.findByEnterpriseIdAndStage(enterpriseId, InterestStage.BOOKMARK, pageable)
                .map(interestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FormalRequestResponse> getMyFormalRequests(UUID enterpriseId, Pageable pageable) {
        return interestRepository.findByEnterpriseIdAndStage(enterpriseId, InterestStage.FORMAL_REQUEST, pageable)
                .map(interestMapper::toFormalRequestResponse);
    }
}
