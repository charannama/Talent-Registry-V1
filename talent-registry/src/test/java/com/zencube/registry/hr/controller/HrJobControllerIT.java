package com.zencube.registry.hr.controller;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.repository.OpeningRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HrJobControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpeningRepository openingRepository;

    @Autowired
    private EnterpriseAccountRepository enterpriseAccountRepository;

    private UUID openingId;

    @BeforeEach
    void setUp() {
        EnterpriseAccount enterprise = new EnterpriseAccount();
        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.APPROVED);
        enterprise.setAccountActive(true);
        enterprise = enterpriseAccountRepository.save(enterprise);

        Opening opening = new Opening();
        opening.setEnterprise(enterprise);
        opening.setTitle("Test Job");
        opening.setStatus(OpeningStatus.PENDING_APPROVAL);
        opening.setApplicationDeadline(Instant.now().plus(7, ChronoUnit.DAYS));
        opening = openingRepository.save(opening);
        openingId = opening.getId();
    }

    @AfterEach
    void tearDown() {
        openingRepository.deleteAll();
        enterpriseAccountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully approve opening as HR")
    @WithMockUser(authorities = "OPENING_APPROVE")
    void approveJob_Success() throws Exception {
        mockMvc.perform(post("/api/v1/hr/jobs/{jobId}/approve", openingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Job opening approved and is now LIVE"))
                .andExpect(jsonPath("$.data.id").value(openingId.toString()))
                .andExpect(jsonPath("$.data.status").value("LIVE"))
                .andExpect(jsonPath("$.data.publishedAt").exists())
                .andExpect(jsonPath("$.data.approvedAt").exists());
    }

    @Test
    @DisplayName("Should forbid access if user lacks OPENING_APPROVE authority")
    @WithMockUser(authorities = "OPENING_VIEW_ALL")
    void approveJob_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/hr/jobs/{jobId}/approve", openingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
