package com.zencube.registry.chat.service;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.chat.entity.ChatParticipant;
import com.zencube.registry.chat.exception.ParticipantNotFoundException;
import com.zencube.registry.chat.repository.ChatMessageRepository;
import com.zencube.registry.chat.repository.ChatParticipantRepository;
import com.zencube.registry.chat.service.impl.ChatServiceImpl;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.security.facade.AuthenticationFacade;
import com.zencube.registry.userrole.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatUnreadServiceTest {

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ChatServiceImpl chatService;

    private User hrUser;
    private UUID threadId;
    private UUID participantId;

    @BeforeEach
    void setUp() {
        hrUser = new User();
        hrUser.setId(UUID.randomUUID());
        Role role = new Role();
        role.setName("ROLE_" + RoleType.HR_STAFF.name());
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        hrUser.setUserRoles(Set.of(userRole));
        
        threadId = UUID.randomUUID();
        participantId = UUID.randomUUID();
    }

    @Test
    void markThreadAsRead_success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());
        
        ChatParticipant cp = new ChatParticipant();
        cp.setId(participantId);
        when(chatParticipantRepository.findByThreadIdAndUserId(threadId, hrUser.getId())).thenReturn(Optional.of(cp));

        chatService.markThreadAsRead(threadId);

        verify(chatParticipantRepository).updateLastReadAt(eq(participantId), any(Instant.class));
        verify(auditService).recordCustomEvent(eq("THREAD_READ"), eq("ChatThread"), eq(threadId.toString()), eq(null));
    }

    @Test
    void markThreadAsRead_nonParticipant_throwsException() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());
        when(chatParticipantRepository.findByThreadIdAndUserId(threadId, hrUser.getId())).thenReturn(Optional.empty());

        assertThrows(ParticipantNotFoundException.class, () -> chatService.markThreadAsRead(threadId));
    }

    @Test
    void getUnreadCount_firstVisit_callsCountAll() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());
        
        ChatParticipant cp = new ChatParticipant();
        cp.setLastReadAt(null);
        when(chatParticipantRepository.findByThreadIdAndUserId(threadId, hrUser.getId())).thenReturn(Optional.of(cp));
        
        when(chatMessageRepository.countAllUnreadMessages(threadId, hrUser.getId())).thenReturn(5L);

        long count = chatService.getUnreadCount(threadId);
        assertEquals(5L, count);
        verify(chatMessageRepository).countAllUnreadMessages(threadId, hrUser.getId());
    }

    @Test
    void getUnreadCount_afterRead_callsCountUnread() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());
        
        ChatParticipant cp = new ChatParticipant();
        Instant lastReadAt = Instant.now().minusSeconds(3600);
        cp.setLastReadAt(lastReadAt);
        when(chatParticipantRepository.findByThreadIdAndUserId(threadId, hrUser.getId())).thenReturn(Optional.of(cp));
        
        when(chatMessageRepository.countUnreadMessages(threadId, hrUser.getId(), lastReadAt)).thenReturn(2L);

        long count = chatService.getUnreadCount(threadId);
        assertEquals(2L, count);
        verify(chatMessageRepository).countUnreadMessages(threadId, hrUser.getId(), lastReadAt);
    }
}
