package com.zencube.registry.application.unit;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.application.entity.Application;
import com.zencube.registry.application.exception.InvalidInterviewStateException;
import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.application.service.impl.ApplicationServiceImpl;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.exception.EventConflictException;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.calendar.repository.CalendarParticipantRepository;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.profile.entity.StudentProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceInterviewTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private CalendarParticipantRepository calendarParticipantRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ActivityService activityService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Application mockApplication;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();

        User studentUser = new User();
        studentUser.setId(UUID.randomUUID());
        studentUser.setFirstName("John");
        studentUser.setLastName("Doe");

        StudentProfile profile = new StudentProfile();
        profile.setUser(studentUser);

        EnterpriseAccount enterprise = new EnterpriseAccount();
        enterprise.setId(UUID.randomUUID());
        enterprise.setCompanyName("ZenCube Tech");
        enterprise.setUser(new User());

        Opening opening = new Opening();
        opening.setId(UUID.randomUUID());
        opening.setTitle("Backend Engineer");
        opening.setEnterprise(enterprise);

        mockApplication = new Application();
        mockApplication.setId(UUID.randomUUID());
        mockApplication.setStatus(ApplicationStatus.FORWARDED);
        mockApplication.setProfile(profile);
        mockApplication.setOpening(opening);
    }

    @Test
    void scheduleInterview_success() {
        when(applicationRepository.findById(mockApplication.getId())).thenReturn(Optional.of(mockApplication));
        when(calendarEventRepository.existsConflictingEvent(anyString(), any(), any(), any(), any())).thenReturn(false);
        when(calendarEventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> {
            CalendarEvent event = invocation.getArgument(0);
            event.setId(UUID.randomUUID());
            return event;
        });

        Application result = applicationService.scheduleInterview(
                mockApplication.getId(),
                Instant.now().plusSeconds(3600),
                Instant.now().plusSeconds(7200),
                "UTC",
                "Virtual Zoom",
                "Please prepare your portfolio.",
                currentUserId
        );

        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, result.getStatus());
        assertNotNull(result.getInterviewEvent());
        assertEquals(currentUserId, result.getCurrentHandlerId());

        verify(calendarParticipantRepository, times(1)).saveAll(anyList());
        verify(auditService, times(1)).recordCustomEvent(eq("INTERVIEW_CREATED"), anyString(), anyString(), anyString());
        verify(activityService, times(1)).recordActivity(eq("INTERVIEW_SCHEDULED"), anyString(), anyString(), anyString(), any(), anyString());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void scheduleInterview_invalidStatus_throwsException() {
        mockApplication.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.findById(mockApplication.getId())).thenReturn(Optional.of(mockApplication));

        assertThrows(InvalidInterviewStateException.class, () -> {
            applicationService.scheduleInterview(
                    mockApplication.getId(),
                    Instant.now().plusSeconds(3600),
                    Instant.now().plusSeconds(7200),
                    "UTC",
                    "Virtual Zoom",
                    "",
                    currentUserId
            );
        });

        verify(calendarEventRepository, never()).save(any());
    }

    @Test
    void scheduleInterview_withConflict_throwsException() {
        when(applicationRepository.findById(mockApplication.getId())).thenReturn(Optional.of(mockApplication));
        when(calendarEventRepository.existsConflictingEvent(anyString(), any(), any(), any(), any())).thenReturn(true);

        assertThrows(EventConflictException.class, () -> {
            applicationService.scheduleInterview(
                    mockApplication.getId(),
                    Instant.now().plusSeconds(3600),
                    Instant.now().plusSeconds(7200),
                    "UTC",
                    "Virtual Zoom",
                    "",
                    currentUserId
            );
        });

        verify(calendarEventRepository, never()).save(any());
    }
}
