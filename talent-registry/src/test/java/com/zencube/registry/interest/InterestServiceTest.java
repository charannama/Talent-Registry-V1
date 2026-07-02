package com.zencube.registry.interest;

import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.expressinterest.dto.InterestResponse;
import com.zencube.registry.expressinterest.entity.ExpressInterest;
import com.zencube.registry.expressinterest.enums.InterestStage;
import com.zencube.registry.expressinterest.exception.InterestException;
import com.zencube.registry.expressinterest.mapper.InterestMapper;
import com.zencube.registry.expressinterest.repository.ExpressInterestRepository;
import com.zencube.registry.expressinterest.service.ExpressInterestServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterestServiceTest {

    @Mock private ExpressInterestRepository interestRepository;
    @Mock private EnterpriseAccountRepository enterpriseRepository;
    @Mock private StudentProfileRepository studentRepository;
    @Mock private OpeningRepository openingRepository;
    @Mock private InterestMapper interestMapper;
    @Mock private ActivityService activityService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ExpressInterestServiceImpl expressInterestService;

    private UUID enterpriseId;
    private UUID studentId;
    private UUID openingId;
    private EnterpriseAccount enterprise;
    private StudentProfile student;
    private Opening opening;

    @BeforeEach
    void setUp() {
        enterpriseId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        openingId = UUID.randomUUID();

        enterprise = new EnterpriseAccount();
        enterprise.setId(enterpriseId);

        User user = new User();
        user.setId(UUID.randomUUID());
        
        student = new StudentProfile();
        student.setId(studentId);
        student.setUser(user);

        opening = new Opening();
        opening.setId(openingId);
        opening.setEnterprise(enterprise);
    }

    @Test
    void bookmark_Success() {
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(openingRepository.findById(openingId)).thenReturn(Optional.of(opening));
        when(interestRepository.findByEnterpriseIdAndStudentIdAndOpeningId(enterpriseId, studentId, openingId)).thenReturn(Optional.empty());

        ExpressInterest savedInterest = new ExpressInterest();
        savedInterest.setId(UUID.randomUUID());
        savedInterest.setEnterprise(enterprise);
        savedInterest.setStudent(student);
        savedInterest.setOpening(opening);
        savedInterest.setStage(InterestStage.BOOKMARK);

        when(interestRepository.save(any(ExpressInterest.class))).thenReturn(savedInterest);
        when(interestMapper.toResponse(savedInterest)).thenReturn(new InterestResponse(savedInterest.getId(), enterpriseId, studentId, openingId, InterestStage.BOOKMARK, null, null));

        InterestResponse response = expressInterestService.bookmark(enterpriseId, studentId, openingId);

        assertThat(response.stage()).isEqualTo(InterestStage.BOOKMARK);
        verify(activityService).recordActivity(eq("EXPRESS_INTEREST"), anyString(), eq("STUDENT_PROFILE"), anyString(), eq(ActivityType.CANDIDATE_BOOKMARKED), anyString());
    }

    @Test
    void bookmark_Duplicate_ThrowsException() {
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(openingRepository.findById(openingId)).thenReturn(Optional.of(opening));
        when(interestRepository.findByEnterpriseIdAndStudentIdAndOpeningId(enterpriseId, studentId, openingId)).thenReturn(Optional.of(new ExpressInterest()));

        assertThrows(InterestException.class, () -> expressInterestService.bookmark(enterpriseId, studentId, openingId));
    }

    @Test
    void formalRequest_Success() {
        UUID interestId = UUID.randomUUID();
        ExpressInterest interest = new ExpressInterest();
        interest.setId(interestId);
        interest.setEnterprise(enterprise);
        interest.setStudent(student);
        interest.setStage(InterestStage.BOOKMARK);

        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(interestRepository.save(any(ExpressInterest.class))).thenReturn(interest);

        expressInterestService.formalRequest(enterpriseId, interestId);

        assertThat(interest.getStage()).isEqualTo(InterestStage.FORMAL_REQUEST);
        assertThat(interest.getRequestedAt()).isNotNull();

        verify(eventPublisher).publishEvent(any(com.zencube.registry.notification.event.NotificationEvent.class));
        verify(activityService).recordActivity(eq("EXPRESS_INTEREST"), anyString(), eq("STUDENT_PROFILE"), anyString(), eq(ActivityType.FORMAL_REQUEST_CREATED), anyString());
    }

    @Test
    void formalRequest_InvalidOwnership_ThrowsException() {
        UUID interestId = UUID.randomUUID();
        ExpressInterest interest = new ExpressInterest();
        interest.setId(interestId);
        EnterpriseAccount otherEnterprise = new EnterpriseAccount();
        otherEnterprise.setId(UUID.randomUUID());
        interest.setEnterprise(otherEnterprise);

        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

        assertThrows(InterestException.class, () -> expressInterestService.formalRequest(enterpriseId, interestId));
    }

    @Test
    void formalRequest_AlreadyEscalated_ThrowsException() {
        UUID interestId = UUID.randomUUID();
        ExpressInterest interest = new ExpressInterest();
        interest.setId(interestId);
        interest.setEnterprise(enterprise);
        interest.setStage(InterestStage.FORMAL_REQUEST);

        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

        assertThrows(InterestException.class, () -> expressInterestService.formalRequest(enterpriseId, interestId));
    }
}
