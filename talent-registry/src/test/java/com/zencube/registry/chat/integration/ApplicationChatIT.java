package com.zencube.registry.chat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.application.entity.Application;
import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import com.zencube.registry.security.facade.AuthenticationFacade;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationChatIT extends IntegrationTestBase {

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

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private EnterpriseAccountRepository enterpriseAccountRepository;

    @Autowired
    private OpeningRepository openingRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @MockBean
    private AuthenticationFacade authenticationFacade;

    private User hrUser;
    private User studentUser;
    private User enterpriseUser;
    private Application application;

    @BeforeEach
    void setUp() {
        hrUser = createUserWithRole("hr-appchat@test.com", RoleType.HR_STAFF);
        studentUser = createUserWithRole("student-appchat@test.com", RoleType.STUDENT);
        enterpriseUser = createUserWithRole("ent-appchat@test.com", RoleType.STUDENT);

        StudentProfile profile = new StudentProfile();
        profile.setUser(studentUser);
        profile = studentProfileRepository.save(profile);

        EnterpriseAccount enterpriseAccount = new EnterpriseAccount();
        enterpriseAccount.setUser(enterpriseUser);
        enterpriseAccount.setCompanyName("Test Corp");
        enterpriseAccount.setDomainEmail("hr@testcorp.com");
        enterpriseAccount.setOnboardingStatus(EnterpriseOnboardingStatus.APPROVED);
        enterpriseAccount.setAccountActive(true);
        enterpriseAccount = enterpriseAccountRepository.save(enterpriseAccount);

        Opening opening = new Opening();
        opening.setEnterprise(enterpriseAccount);
        opening.setTitle("Software Engineer");
        opening.setStatus(OpeningStatus.LIVE);
        opening = openingRepository.save(opening);

        application = new Application();
        application.setProfile(profile);
        application.setOpening(opening);
        application.setStatus(ApplicationStatus.FORWARDED);
        application.setCurrentHandlerId(hrUser.getId());
        application = applicationRepository.save(application);
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
    void testApplicationThreadCreationAndContext() throws Exception {
        // 1. HR Creates the Thread
        when(authenticationFacade.getCurrentUser()).thenReturn(hrUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(hrUser.getId());

        String createResStr = mockMvc.perform(post("/api/v1/chat/application/" + application.getId() + "/thread")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contextableType").value("Application"))
                .andExpect(jsonPath("$.contextableId").value(application.getId().toString()))
                .andReturn().getResponse().getContentAsString();

        // 2. Fetch thread directly and verify context
        mockMvc.perform(get("/api/v1/chat/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].application.applicationId").value(application.getId().toString()))
                .andExpect(jsonPath("$.content[0].application.openingTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.content[0].application.enterpriseName").value("Test Corp"));

        // 3. Prevent Student from creating
        when(authenticationFacade.getCurrentUser()).thenReturn(studentUser);
        when(authenticationFacade.getCurrentUserId()).thenReturn(studentUser.getId());

        mockMvc.perform(post("/api/v1/chat/application/" + application.getId() + "/thread"))
                .andExpect(status().isForbidden());
    }
}





