package com.zencube.registry.chat.integration;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.chat.dto.request.CreateThreadRequest;
import com.zencube.registry.chat.dto.request.SendMessageRequest;
import com.zencube.registry.chat.dto.response.ChatMessageResponse;
import com.zencube.registry.chat.dto.response.ChatThreadResponse;
import com.zencube.registry.chat.enums.ThreadType;
import com.zencube.registry.chat.exception.DirectCommunicationNotAllowedException;
import com.zencube.registry.chat.service.ChatService;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.security.facade.AuthenticationFacade;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ChatIT extends IntegrationTestBase {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    @MockBean
    private AuthenticationFacade authenticationFacade;

    private User hrUser;
    private User studentUser;
    private User enterpriseUser;

    @BeforeEach
    void setUp() {
        hrUser = createUserWithRole("hr@test.com", RoleType.HR_STAFF);
        studentUser = createUserWithRole("student@test.com", RoleType.STUDENT);
        enterpriseUser = createUserWithRole("enterprise@test.com", RoleType.ENTERPRISE_RECRUITER);
    }

    private User createUserWithRole(String email, RoleType roleType) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Role role = roleRepository.findByNameAndDeletedFalse(roleType.name()).orElse(null);
        if (role == null) {
            role = new Role();
            role.setName(roleType.name());
            role.setRoleType(roleType);
            role = roleRepository.save(role);
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        
        user.setUserRoles(Set.of(userRole));
        return userRepository.save(user);
    }

    @Test
    void testChatModule_EndToEnd_MediationEnforced() {
        // 1. HR creates a thread with Student
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());

        CreateThreadRequest hrStudentReq = CreateThreadRequest.builder()
                .threadType(ThreadType.HR_STUDENT)
                .participantIds(List.of(studentUser.getId()))
                .build();
        
        ChatThreadResponse threadResponse = chatService.createThread(hrStudentReq);
        assertNotNull(threadResponse.getId());
        assertEquals(2, threadResponse.getParticipants().size());

        // 2. HR sends a message
        SendMessageRequest msgReq = new SendMessageRequest("Hello Student!");
        ChatMessageResponse msgRes = chatService.sendMessage(threadResponse.getId(), msgReq);
        assertNotNull(msgRes.getId());
        assertEquals("Hello Student!", msgRes.getContent());

        // 3. Student tries to create a thread directly with Enterprise -> Should Fail
        when(authenticationFacade.getCurrentUser()).thenReturn(studentUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(studentUser.getId());

        CreateThreadRequest studentEnterpriseReq = CreateThreadRequest.builder()
                .threadType(ThreadType.GROUP)
                .participantIds(List.of(enterpriseUser.getId()))
                .build();

        assertThrows(DirectCommunicationNotAllowedException.class, () -> {
            chatService.createThread(studentEnterpriseReq);
        });
        
        // 4. HR creates a Group thread with Student and Enterprise -> Should Succeed
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());
        
        CreateThreadRequest groupReq = CreateThreadRequest.builder()
                .threadType(ThreadType.GROUP)
                .participantIds(List.of(studentUser.getId(), enterpriseUser.getId()))
                .build();
                
        ChatThreadResponse groupRes = chatService.createThread(groupReq);
        assertNotNull(groupRes.getId());
        assertEquals(3, groupRes.getParticipants().size());
    }

    @Test
    void testChatModule_PaginationAndBulk() {
        // 1. HR creates a thread
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());

        CreateThreadRequest hrStudentReq = CreateThreadRequest.builder()
                .threadType(ThreadType.HR_STUDENT)
                .participantIds(List.of(studentUser.getId()))
                .build();
        
        ChatThreadResponse thread = chatService.createThread(hrStudentReq);

        // 2. HR sends 100 messages
        for (int i = 0; i < 100; i++) {
            SendMessageRequest msgReq = new SendMessageRequest("Bulk Message " + i);
            chatService.sendMessage(thread.getId(), msgReq);
        }

        // 3. Student fetches Page 0, Size 20
        when(authenticationFacade.getCurrentUser()).thenReturn(studentUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(studentUser.getId());

        var page0 = chatService.getMessages(thread.getId(), org.springframework.data.domain.PageRequest.of(0, 20));
        assertEquals(20, page0.getContent().size());
        assertEquals(100, page0.getTotalElements());

        // 4. Student fetches Page 4, Size 20
        var page4 = chatService.getMessages(thread.getId(), org.springframework.data.domain.PageRequest.of(4, 20));
        assertEquals(20, page4.getContent().size());
    }
}


