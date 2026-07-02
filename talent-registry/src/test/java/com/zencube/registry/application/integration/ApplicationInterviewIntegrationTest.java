package com.zencube.registry.application.integration;

import com.zencube.registry.application.entity.Application;
import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.application.service.ApplicationService;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.calendar.repository.CalendarParticipantRepository;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationInterviewIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private CalendarParticipantRepository calendarParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private EnterpriseAccountRepository enterpriseAccountRepository;

    @Autowired
    private OpeningRepository openingRepository;

    private Application testApplication;
    private User hrUser;

    @BeforeEach
    void setUp() {
        hrUser = new User();
        hrUser.setEmail("hr@zencube.com");
        hrUser.setPasswordHash("password");
        hrUser = userRepository.save(hrUser);

        User studentUser = new User();
        studentUser.setEmail("student@test.com");
        studentUser.setPasswordHash("password");
        studentUser.setFirstName("Alice");
        studentUser.setLastName("Smith");
        studentUser = userRepository.save(studentUser);

        StudentProfile profile = new StudentProfile();
        profile.setUser(studentUser);
        profile = studentProfileRepository.save(profile);

        User enterpriseUser = new User();
        enterpriseUser.setEmail("recruiter@tech.com");
        enterpriseUser.setPasswordHash("password");
        enterpriseUser = userRepository.save(enterpriseUser);

        EnterpriseAccount enterprise = new EnterpriseAccount();
        enterprise.setUser(enterpriseUser);
        enterprise.setCompanyName("Tech Corp");
        enterprise.setDomainEmail("recruiter@tech.com");
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.APPROVED);
        enterprise.setAccountActive(true);
        enterprise = enterpriseAccountRepository.save(enterprise);

        Opening opening = new Opening();
        opening.setEnterprise(enterprise);
        opening.setTitle("Frontend Dev");
        opening.setDescription("React Dev");
        opening = openingRepository.save(opening);

        testApplication = new Application();
        testApplication.setProfile(profile);
        testApplication.setOpening(opening);
        testApplication.setStatus(ApplicationStatus.FORWARDED);
        testApplication = applicationRepository.save(testApplication);
    }

    @Test
    void scheduleInterview_persistsDataAndLinksEntities() {
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = Instant.now().plusSeconds(7200);

        Application updatedApp = applicationService.scheduleInterview(
                testApplication.getId(),
                start,
                end,
                "UTC",
                "Zoom Link",
                "Prepare whiteboard",
                hrUser.getId()
        );

        assertNotNull(updatedApp.getInterviewEvent());
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, updatedApp.getStatus());
        assertEquals(hrUser.getId(), updatedApp.getCurrentHandlerId());

        Application dbApp = applicationRepository.findById(testApplication.getId()).orElseThrow();
        assertNotNull(dbApp.getInterviewEvent());
        assertEquals("Zoom Link", dbApp.getInterviewEvent().getLocation());

        long participantCount = calendarParticipantRepository.countByEventId(dbApp.getInterviewEvent().getId());
        assertEquals(3, participantCount); // Student, HR, Enterprise
    }
}

