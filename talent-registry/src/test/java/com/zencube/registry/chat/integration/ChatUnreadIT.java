package com.zencube.registry.chat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.chat.dto.request.CreateThreadRequest;
import com.zencube.registry.chat.dto.request.SendMessageRequest;
import com.zencube.registry.chat.enums.ThreadType;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.security.facade.AuthenticationFacade;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatUnreadIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @BeforeEach
    void setUp() {
        hrUser = createUserWithRole("hr-unread@test.com", RoleType.HR_STAFF);
        studentUser = createUserWithRole("student-unread@test.com", RoleType.STUDENT);
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
    void testUnreadCountsAndReadReceipts() throws Exception {
        // 1. HR Creates a Thread
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());

        CreateThreadRequest createReq = new CreateThreadRequest(ThreadType.HR_STUDENT, List.of(studentUser.getId()), null, null);

        String createResStr = mockMvc.perform(post("/api/v1/chat/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String threadId = objectMapper.readTree(createResStr).get("id").asText();

        // 2. HR Sends 2 Messages
        SendMessageRequest msg1 = new SendMessageRequest("Hello Student!");
        SendMessageRequest msg2 = new SendMessageRequest("Please review the document.");
        
        mockMvc.perform(post("/api/v1/chat/threads/" + threadId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/chat/threads/" + threadId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg2)))
                .andExpect(status().isCreated());

        // 3. Student Lists Threads (Should see unread count = 2)
        when(authenticationFacade.getCurrentUser()).thenReturn(studentUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(studentUser.getId());

        mockMvc.perform(get("/api/v1/chat/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].unreadCount").value(2));

        // 4. Student Marks Thread as Read
        mockMvc.perform(post("/api/v1/chat/threads/" + threadId + "/read"))
                .andExpect(status().isNoContent());

        // 5. Student Lists Threads Again (Should see unread count = 0)
        mockMvc.perform(get("/api/v1/chat/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].unreadCount").value(0));

        // 6. HR lists threads (Should see unread count = 0, because you can't have unread counts for your own messages)
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());

        mockMvc.perform(get("/api/v1/chat/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].unreadCount").value(0));
    }
}



