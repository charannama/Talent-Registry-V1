package com.zencube.registry.chat.service;

import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.chat.dto.request.CreateThreadRequest;
import com.zencube.registry.chat.dto.request.SendMessageRequest;
import com.zencube.registry.chat.dto.response.ChatMessageResponse;
import com.zencube.registry.chat.dto.response.ChatThreadResponse;
import com.zencube.registry.chat.entity.ChatMessage;
import com.zencube.registry.chat.entity.ChatParticipant;
import com.zencube.registry.chat.entity.ChatThread;
import com.zencube.registry.chat.enums.ThreadType;
import com.zencube.registry.chat.exception.DirectCommunicationNotAllowedException;
import com.zencube.registry.chat.exception.ParticipantNotFoundException;
import com.zencube.registry.chat.exception.ThreadArchivedException;
import com.zencube.registry.chat.mapper.ChatMapper;
import com.zencube.registry.chat.repository.ChatMessageRepository;
import com.zencube.registry.chat.repository.ChatParticipantRepository;
import com.zencube.registry.chat.repository.ChatThreadRepository;
import com.zencube.registry.chat.service.impl.ChatServiceImpl;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.service.NotificationService;
import com.zencube.registry.security.facade.AuthenticationFacade;
import com.zencube.registry.userrole.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatThreadRepository chatThreadRepository;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ActivityService activityService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private User hrUser;
    private User studentUser;
    private User enterpriseUser;

    @BeforeEach
    void setUp() {
        hrUser = createUserWithRole(RoleType.HR_STAFF);
        studentUser = createUserWithRole(RoleType.STUDENT);
        enterpriseUser = createUserWithRole(RoleType.ENTERPRISE_RECRUITER);
    }

    private User createUserWithRole(RoleType roleType) {
        User user = new User();
        user.setId(UUID.randomUUID());
        Role role = new Role();
        role.setName("ROLE_" + roleType.name());
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        user.setUserRoles(Set.of(userRole));
        return user;
    }

    @Test
    void createThread_hrStudent_success() {
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(userRepository.findByIdAndDeletedFalse(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(chatThreadRepository.save(any(ChatThread.class))).thenAnswer(i -> {
            ChatThread t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        CreateThreadRequest req = new CreateThreadRequest(ThreadType.HR_STUDENT, List.of(studentUser.getId()), "TEST", null);
        
        chatService.createThread(req);
        
        verify(chatThreadRepository, times(1)).save(any());
        verify(chatParticipantRepository, times(2)).save(any());
    }

    @Test
    void createThread_hrEnterprise_success() {
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(userRepository.findByIdAndDeletedFalse(enterpriseUser.getId())).thenReturn(Optional.of(enterpriseUser));
        when(chatThreadRepository.save(any(ChatThread.class))).thenAnswer(i -> {
            ChatThread t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        CreateThreadRequest req = new CreateThreadRequest(ThreadType.HR_ENTERPRISE, List.of(enterpriseUser.getId()), "TEST", null);
        
        chatService.createThread(req);
        
        verify(chatThreadRepository, times(1)).save(any());
    }

    @Test
    void createThread_group_success() {
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(userRepository.findByIdAndDeletedFalse(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(userRepository.findByIdAndDeletedFalse(enterpriseUser.getId())).thenReturn(Optional.of(enterpriseUser));
        when(chatThreadRepository.save(any(ChatThread.class))).thenAnswer(i -> {
            ChatThread t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        CreateThreadRequest req = new CreateThreadRequest(ThreadType.GROUP, List.of(studentUser.getId(), enterpriseUser.getId()), "TEST", null);
        
        chatService.createThread(req);
        
        verify(chatThreadRepository, times(1)).save(any());
        verify(chatParticipantRepository, times(3)).save(any());
    }

    @Test
    void createThread_studentEnterprise_blocked() {
        // Technically this should also be blocked by @PreAuthorize on the controller or service method,
        // but let's test the validateCommunicationRules logic directly.
        // Assuming Student calls it and tries to add Enterprise
        when(authenticationFacade.getCurrentUser()).thenReturn(studentUser);
        when(userRepository.findByIdAndDeletedFalse(enterpriseUser.getId())).thenReturn(Optional.of(enterpriseUser));

        CreateThreadRequest req = new CreateThreadRequest(ThreadType.GROUP, List.of(enterpriseUser.getId()), "TEST", null);
        
        assertThrows(DirectCommunicationNotAllowedException.class, () -> chatService.createThread(req));
    }

    @Test
    void sendMessage_success() {
        UUID threadId = UUID.randomUUID();
        ChatThread thread = new ChatThread();
        thread.setId(threadId);
        thread.setIsArchived(false);

        ChatParticipant senderParticipant = new ChatParticipant();
        senderParticipant.setUser(hrUser);

        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(chatThreadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(chatParticipantRepository.findByThreadIdAndUserId(threadId, hrUser.getId())).thenReturn(Optional.of(senderParticipant));
        when(chatMessageRepository.save(any())).thenAnswer(i -> {
            ChatMessage msg = i.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });

        SendMessageRequest req = new SendMessageRequest("Hello");
        chatService.sendMessage(threadId, req);

        verify(chatMessageRepository).save(any());
    }

    @Test
    void sendMessage_nonParticipant_fails() {
        UUID threadId = UUID.randomUUID();
        ChatThread thread = new ChatThread();
        thread.setId(threadId);
        thread.setIsArchived(false);

        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(chatThreadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(chatParticipantRepository.findByThreadIdAndUserId(threadId, hrUser.getId())).thenReturn(Optional.empty());

        SendMessageRequest req = new SendMessageRequest("Hello");
        assertThrows(ParticipantNotFoundException.class, () -> chatService.sendMessage(threadId, req));
    }

    @Test
    void sendMessage_archivedThread_fails() {
        UUID threadId = UUID.randomUUID();
        ChatThread thread = new ChatThread();
        thread.setId(threadId);
        thread.setIsArchived(true);

        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(chatThreadRepository.findById(threadId)).thenReturn(Optional.of(thread));

        SendMessageRequest req = new SendMessageRequest("Hello");
        assertThrows(ThreadArchivedException.class, () -> chatService.sendMessage(threadId, req));
    }

    @Test
    void listThreads_success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());
        ChatThread thread = new ChatThread();
        thread.setId(UUID.randomUUID());
        Page<ChatThread> page = new PageImpl<>(List.of(thread));

        when(chatThreadRepository.findThreadsForUser(eq(hrUser.getId()), any())).thenReturn(page);
        when(chatParticipantRepository.findByThreadId(thread.getId())).thenReturn(Collections.emptyList());
        when(chatMessageRepository.findByThreadIdOrderBySentAtAsc(eq(thread.getId()), any())).thenReturn(Page.empty());

        Page<ChatThreadResponse> res = chatService.listThreads(PageRequest.of(0, 10));
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void getMessages_success() {
        UUID threadId = UUID.randomUUID();
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());
        when(chatParticipantRepository.existsByThreadIdAndUserId(threadId, hrUser.getId())).thenReturn(true);
        when(chatMessageRepository.findByThreadIdOrderBySentAtAsc(eq(threadId), any())).thenReturn(Page.empty());

        Page<ChatMessageResponse> res = chatService.getMessages(threadId, PageRequest.of(0, 10));
        assertNotNull(res);
    }
}
