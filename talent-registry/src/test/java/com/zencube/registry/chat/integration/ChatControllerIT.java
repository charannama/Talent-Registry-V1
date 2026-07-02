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
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerIT extends IntegrationTestBase {

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
    private User enterpriseUser;

    @BeforeEach
    void setUp() {
        hrUser = createUserWithRole("hr-controller@test.com", RoleType.HR_STAFF);
        studentUser = createUserWithRole("student-controller@test.com", RoleType.STUDENT);
        enterpriseUser = createUserWithRole("enterprise-controller@test.com", RoleType.ENTERPRISE_RECRUITER);
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
    void testChatApi_EndToEnd() throws Exception {
        // 1. HR Creates a Thread
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());

        CreateThreadRequest createReq = new CreateThreadRequest(ThreadType.HR_STUDENT, List.of(studentUser.getId()), null, null);

        String createResStr = mockMvc.perform(post("/api/v1/chat/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        String threadId = objectMapper.readTree(createResStr).get("id").asText();

        // 2. HR Sends Message
        SendMessageRequest msgReq = new SendMessageRequest("Hello Student!");
        mockMvc.perform(post("/api/v1/chat/threads/" + threadId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello Student!"));

        // 3. Student Lists Threads (Should see unread count = 1)
        when(authenticationFacade.getCurrentUser()).thenReturn(studentUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(studentUser.getId());

        mockMvc.perform(get("/api/v1/chat/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].unreadCount").value(1))
                .andExpect(jsonPath("$.content[0].lastMessagePreview").value("Hello Student!"));

        // 4. Student gets messages
        mockMvc.perform(get("/api/v1/chat/threads/" + threadId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Hello Student!"));

        // 5. Enterprise tries to access thread -> Should be forbidden
        when(authenticationFacade.getCurrentUser()).thenReturn(enterpriseUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(enterpriseUser.getId());

        mockMvc.perform(get("/api/v1/chat/threads/" + threadId + "/messages"))
                .andExpect(status().isForbidden());
    }
}


