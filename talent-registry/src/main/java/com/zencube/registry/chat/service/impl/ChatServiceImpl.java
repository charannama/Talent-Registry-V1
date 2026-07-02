package com.zencube.registry.chat.service.impl;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.application.entity.Application;
import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.chat.dto.request.CreateThreadRequest;
import com.zencube.registry.chat.dto.request.SendMessageRequest;
import com.zencube.registry.chat.dto.response.ChatMessageResponse;
import com.zencube.registry.chat.dto.response.ChatThreadResponse;
import com.zencube.registry.chat.entity.ChatMessage;
import com.zencube.registry.chat.entity.ChatParticipant;
import com.zencube.registry.chat.entity.ChatThread;
import com.zencube.registry.chat.exception.DirectCommunicationNotAllowedException;
import com.zencube.registry.chat.exception.ParticipantNotFoundException;
import com.zencube.registry.chat.exception.ThreadArchivedException;
import com.zencube.registry.chat.mapper.ChatMapper;
import com.zencube.registry.chat.repository.ChatMessageRepository;
import com.zencube.registry.chat.repository.ChatParticipantRepository;
import com.zencube.registry.chat.repository.ChatThreadRepository;
import com.zencube.registry.chat.service.ChatService;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.dto.request.CreateNotificationRequest;
import com.zencube.registry.notification.enums.NotificationType;
import com.zencube.registry.notification.service.NotificationService;
import com.zencube.registry.security.facade.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditService auditService;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final AuthenticationFacade authenticationFacade;
    private final ChatMapper chatMapper;

    @Override
    @PreAuthorize("hasAnyRole('HR_STAFF', 'ADMIN', 'SUPER_ADMIN')")
    public ChatThreadResponse createThread(CreateThreadRequest request) {
        User currentUser = authenticationFacade.getCurrentUser();
        
        List<User> participants = new ArrayList<>();
        participants.add(currentUser);
        
        for (UUID participantId : request.getParticipantIds()) {
            if (!participantId.equals(currentUser.getId())) {
                User p = userRepository.findByIdAndDeletedFalse(participantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Participant not found: " + participantId));
                participants.add(p);
            }
        }

        validateCommunicationRules(participants);

        ChatThread thread = ChatThread.builder()
                .threadType(request.getThreadType())
                .contextableType(request.getContextableType())
                .contextableId(request.getContextableId())
                .creator(currentUser)
                .isArchived(false)
                .build();
        
        thread = chatThreadRepository.save(thread);

        List<ChatParticipant> savedParticipants = new ArrayList<>();
        Instant now = Instant.now();
        for (User u : participants) {
            ChatParticipant cp = ChatParticipant.builder()
                    .thread(thread)
                    .user(u)
                    .joinedAt(now)
                    .build();
            savedParticipants.add(chatParticipantRepository.save(cp));
        }

        auditService.recordCustomEvent("THREAD_CREATED", "ChatThread", thread.getId().toString(), null);
        activityService.recordActivity(
            "ChatThread", 
            thread.getId().toString(), 
            request.getContextableType() != null ? request.getContextableType() : "System", 
            request.getContextableId() != null ? request.getContextableId().toString() : "0", 
            com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_CREATED, 
            "Created new chat thread"
        );

        return chatMapper.toThreadResponse(thread, savedParticipants, null, 0, null);
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_STAFF', 'ADMIN', 'SUPER_ADMIN')")
    public ChatThreadResponse createApplicationThread(UUID applicationId) {
        User currentUser = authenticationFacade.getCurrentUser();

        // 1. Check if thread already exists
        Optional<ChatThread> existingThread = chatThreadRepository.findByContextableTypeAndContextableId("Application", applicationId);
        if (existingThread.isPresent()) {
            return getThread(existingThread.get().getId());
        }

        // 2. Load Application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (application.isDeleted()) {
            throw new com.zencube.registry.common.exception.BusinessException("Application is deleted.", org.springframework.http.HttpStatus.BAD_REQUEST, "APPLICATION_DELETED");
        }

        if (application.getCurrentHandlerId() == null) {
            throw new com.zencube.registry.common.exception.BusinessException("Application must have an assigned HR handler to create a thread", org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_HR_HANDLER");
        }

        // 3. Resolve Participants
        User student = application.getProfile().getUser();
        User enterprise = application.getOpening().getEnterprise().getUser();
        User hr = userRepository.findByIdAndDeletedFalse(application.getCurrentHandlerId())
                .orElseThrow(() -> new ResourceNotFoundException("HR Handler not found"));

        if (student == null || enterprise == null) {
             throw new com.zencube.registry.common.exception.BusinessException("Application is missing valid student or enterprise", org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_PARTICIPANTS");
        }

        List<User> participants = List.of(student, enterprise, hr);

        // 4. Create Thread
        ChatThread thread = ChatThread.builder()
                .threadType(com.zencube.registry.chat.enums.ThreadType.GROUP)
                .contextableType("Application")
                .contextableId(applicationId)
                .creator(currentUser)
                .isArchived(false)
                .build();

        thread = chatThreadRepository.save(thread);

        // 5. Create ChatParticipants
        List<ChatParticipant> savedParticipants = new ArrayList<>();
        Instant now = Instant.now();
        for (User u : participants) {
            ChatParticipant cp = ChatParticipant.builder()
                    .thread(thread)
                    .user(u)
                    .joinedAt(now)
                    .build();
            savedParticipants.add(chatParticipantRepository.save(cp));
        }

        // 6. Audit & Activity
        auditService.recordCustomEvent("APPLICATION_CHAT_CREATED", "ChatThread", thread.getId().toString(), null);
        activityService.recordActivity("ChatThread", thread.getId().toString(), "Application", applicationId != null ? applicationId.toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_CREATED, "Created new application chat thread");

        // 7. Notify
        notifyParticipants(thread, null, currentUser, "Application Discussion Started", "A discussion thread has been created for your application.");

        return getThread(thread.getId());
    }

    @Override
    public ChatMessageResponse sendMessage(UUID threadId, SendMessageRequest request) {
        User currentUser = authenticationFacade.getCurrentUser();
        
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat thread not found"));

        if (thread.getIsArchived()) {
            throw new ThreadArchivedException("Cannot send message to an archived thread");
        }

        ChatParticipant senderParticipant = chatParticipantRepository.findByThreadIdAndUserId(threadId, currentUser.getId())
                .orElseThrow(() -> new ParticipantNotFoundException("User is not a participant of this thread"));

        ChatMessage message = ChatMessage.builder()
                .thread(thread)
                .sender(currentUser)
                .content(request.getContent())
                .sentAt(Instant.now())
                .build();

        message = chatMessageRepository.save(message);

        // Update last read for sender
        senderParticipant.setLastReadAt(message.getSentAt());
        chatParticipantRepository.save(senderParticipant);

        auditService.recordCustomEvent("MESSAGE_SENT", "ChatMessage", message.getId().toString(), null);
        activityService.recordActivity(
            "ChatThread", 
            thread.getId().toString(), 
            thread.getContextableType() != null ? thread.getContextableType() : "System", 
            thread.getContextableId() != null ? thread.getContextableId().toString() : "0", 
            com.zencube.registry.activity.enums.ActivityType.CHAT_MESSAGE_SENT, 
            "Sent a message"
        );

        notifyParticipants(thread, message, currentUser);

        return chatMapper.toMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatThreadResponse> listThreads(Pageable pageable) {
        UUID currentUserId = authenticationFacade.getCurrentUserId();
        Page<ChatThread> threads = chatThreadRepository.findThreadsForUser(currentUserId, pageable);
        
        if (threads.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> threadIds = threads.stream().map(ChatThread::getId).collect(Collectors.toList());
        Map<UUID, Long> unreadCounts = getUnreadCounts(threadIds);

        List<UUID> applicationIds = threads.stream()
                .filter(t -> "Application".equals(t.getContextableType()) && t.getContextableId() != null)
                .map(ChatThread::getContextableId)
                .collect(Collectors.toList());
                
        Map<UUID, com.zencube.registry.chat.dto.response.ApplicationContextResponse> appContexts = getBatchApplicationContexts(applicationIds);

        return threads.map(thread -> {
            List<ChatParticipant> participants = chatParticipantRepository.findByThreadId(thread.getId());
            
            int unreadCount = unreadCounts.getOrDefault(thread.getId(), 0L).intValue();
            
            com.zencube.registry.chat.dto.response.ApplicationContextResponse appContext = null;
            if ("Application".equals(thread.getContextableType()) && thread.getContextableId() != null) {
                appContext = appContexts.get(thread.getContextableId());
            }

            Page<ChatMessage> lastMessages = chatMessageRepository.findByThreadIdOrderBySentAtAsc(
                    thread.getId(), 
                    PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "sentAt"))
            );
            ChatMessage lastMessage = lastMessages.isEmpty() ? null : lastMessages.getContent().get(0);
            return chatMapper.toThreadResponse(thread, participants, lastMessage, unreadCount, appContext);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID threadId, Pageable pageable) {
        UUID currentUserId = authenticationFacade.getCurrentUserId();
        if (!chatParticipantRepository.existsByThreadIdAndUserId(threadId, currentUserId)) {
            throw new ParticipantNotFoundException("Access denied: Not a participant");
        }
        
        return chatMessageRepository.findByThreadIdOrderBySentAtAsc(threadId, pageable)
                .map(chatMapper::toMessageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatThreadResponse getThread(UUID threadId) {
        UUID currentUserId = authenticationFacade.getCurrentUserId();
        if (!chatParticipantRepository.existsByThreadIdAndUserId(threadId, currentUserId)) {
            throw new ParticipantNotFoundException("Access denied: Not a participant");
        }

        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found"));
                
        List<ChatParticipant> participants = chatParticipantRepository.findByThreadId(thread.getId());
        int unreadCount = (int) getUnreadCount(thread.getId());
        
        Page<ChatMessage> lastMessages = chatMessageRepository.findByThreadIdOrderBySentAtAsc(
                thread.getId(), 
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "sentAt"))
        );
        ChatMessage lastMessage = lastMessages.isEmpty() ? null : lastMessages.getContent().get(0);
        
        com.zencube.registry.chat.dto.response.ApplicationContextResponse appContext = null;
        if ("Application".equals(thread.getContextableType()) && thread.getContextableId() != null) {
            Application app = applicationRepository.findById(thread.getContextableId()).orElse(null);
            if (app != null) {
                appContext = buildApplicationContext(app);
            }
        }
        
        return chatMapper.toThreadResponse(thread, participants, lastMessage, unreadCount, appContext);
    }

    @Override
    public void markThreadAsRead(UUID threadId) {
        UUID currentUserId = authenticationFacade.getCurrentUserId();
        ChatParticipant participant = chatParticipantRepository.findByThreadIdAndUserId(threadId, currentUserId)
                .orElseThrow(() -> new ParticipantNotFoundException("Access denied: Not a participant"));

        chatParticipantRepository.updateLastReadAt(participant.getId(), Instant.now());

        auditService.recordCustomEvent("THREAD_READ", "ChatThread", threadId.toString(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID threadId) {
        UUID currentUserId = authenticationFacade.getCurrentUserId();
        ChatParticipant participant = chatParticipantRepository.findByThreadIdAndUserId(threadId, currentUserId)
                .orElseThrow(() -> new ParticipantNotFoundException("Access denied: Not a participant"));

        if (participant.getLastReadAt() == null) {
            return chatMessageRepository.countAllUnreadMessages(threadId, currentUserId);
        } else {
            return chatMessageRepository.countUnreadMessages(threadId, currentUserId, participant.getLastReadAt());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Long> getUnreadCounts(java.util.Collection<UUID> threadIds) {
        if (threadIds == null || threadIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        UUID currentUserId = authenticationFacade.getCurrentUserId();
        List<Object[]> results = chatMessageRepository.countUnreadMessagesBatch(currentUserId, threadIds);

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

    private void validateCommunicationRules(List<User> participants) {
        boolean hasStudent = false;
        boolean hasEnterprise = false;
        boolean hasHr = false;

        for (User u : participants) {
            boolean isStudent = u.getUserRoles().stream().anyMatch(r -> r.getRole().getName().equals("ROLE_" + RoleType.STUDENT.name()));
            boolean isEnterprise = u.getUserRoles().stream().anyMatch(r -> 
                r.getRole().getName().equals("ROLE_" + RoleType.ENTERPRISE_RECRUITER.name()) ||
                r.getRole().getName().equals("ROLE_" + RoleType.ENTERPRISE_ADMIN.name())
            );
            boolean isHr = u.getUserRoles().stream().anyMatch(r -> 
                r.getRole().getName().equals("ROLE_" + RoleType.HR_STAFF.name()) ||
                r.getRole().getName().equals("ROLE_" + RoleType.ADMIN.name()) ||
                r.getRole().getName().equals("ROLE_" + RoleType.SUPER_ADMIN.name())
            );

            if (isStudent) hasStudent = true;
            if (isEnterprise) hasEnterprise = true;
            if (isHr) hasHr = true;
        }

        if (hasStudent && hasEnterprise && !hasHr) {
            throw new DirectCommunicationNotAllowedException("Direct communication between students and enterprises is prohibited. HR mediation is required.");
        }
    }

    private void notifyParticipants(ChatThread thread, ChatMessage message, User sender) {
        notifyParticipants(thread, message, sender, "New Message", "You received a new message in thread: " + (thread.getThreadType() != null ? thread.getThreadType().name() : thread.getId().toString()));
    }
    
    private void notifyParticipants(ChatThread thread, ChatMessage message, User sender, String title, String body) {
        List<ChatParticipant> participants = chatParticipantRepository.findByThreadIdAndUserIdNot(thread.getId(), sender.getId());
        for (ChatParticipant cp : participants) {
            try {
                notificationService.createNotification(
                    cp.getUser().getId(),
                    com.zencube.registry.notification.enums.NotificationEventType.CHAT_MESSAGE_RECEIVED,
                    message != null ? "ChatMessage" : "ChatThread",
                    message != null ? message.getId() : thread.getId(),
                    title,
                    body
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to user {}", cp.getUser().getId(), e);
            }
        }
    }

    private Map<UUID, com.zencube.registry.chat.dto.response.ApplicationContextResponse> getBatchApplicationContexts(List<UUID> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) return java.util.Collections.emptyMap();
        List<Application> apps = applicationRepository.findAllById(applicationIds);
        return apps.stream().collect(Collectors.toMap(
                Application::getId,
                this::buildApplicationContext
        ));
    }

    private com.zencube.registry.chat.dto.response.ApplicationContextResponse buildApplicationContext(Application app) {
        return com.zencube.registry.chat.dto.response.ApplicationContextResponse.builder()
                .applicationId(app.getId())
                .candidateName(app.getProfile().getUser() != null ? app.getProfile().getUser().getDisplayName() : null)
                .candidateId(app.getProfile().getUser() != null ? app.getProfile().getUser().getId() : null)
                .openingTitle(app.getOpening() != null ? app.getOpening().getTitle() : null)
                .openingId(app.getOpening() != null ? app.getOpening().getId() : null)
                .enterpriseName(app.getOpening() != null && app.getOpening().getEnterprise() != null ? app.getOpening().getEnterprise().getCompanyName() : null)
                .enterpriseId(app.getOpening() != null && app.getOpening().getEnterprise() != null ? app.getOpening().getEnterprise().getId() : null)
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }
}





