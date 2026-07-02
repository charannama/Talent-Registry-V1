package com.zencube.registry.admin.controller;

import com.zencube.registry.admin.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // Filters disabled to test controller mapping locally
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.zencube.registry.config.service.ConfigService configService;

    @MockBean
    private com.zencube.registry.featureflag.service.FeatureFlagService featureFlagService;

    @MockBean
    private com.zencube.registry.journal.service.AuditQueryService auditQueryService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void unlockUser_ReturnsOk() throws Exception {
        UUID userId = UUID.randomUUID();

        doNothing().when(adminUserService).unlockUser(userId);

        mockMvc.perform(post("/api/v1/admin/users/{userId}/unlock", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account unlocked successfully."));

        verify(adminUserService).unlockUser(userId);
    }
}


