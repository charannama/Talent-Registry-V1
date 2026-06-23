package com.zencube.registry.opening.controller;

import com.zencube.registry.enterprise.entity.EnterpriseAccount;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OpeningControllerPublicIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpeningRepository openingRepository;

    @Autowired
    private EnterpriseAccountRepository enterpriseAccountRepository;

    private EnterpriseAccount enterprise;

    @BeforeEach
    void setUp() {
        enterprise = new EnterpriseAccount();
        enterprise.setId(UUID.randomUUID());
        enterprise.setCompanyName("Test Enterprise");
        enterprise.setAccountActive(true);
        enterpriseAccountRepository.save(enterprise);
    }

    @AfterEach
    void tearDown() {
        openingRepository.deleteAll();
        enterpriseAccountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should retrieve public live openings without authentication")
    void browseLiveOpenings_Success() throws Exception {
        Opening liveOpening = new Opening();
        liveOpening.setId(UUID.randomUUID());
        liveOpening.setTitle("Live Software Engineer");
        liveOpening.setEnterprise(enterprise);
        liveOpening.setStatus(OpeningStatus.LIVE);
        liveOpening.setPublishedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        liveOpening.setApplicationDeadline(Instant.now().plus(10, ChronoUnit.DAYS));
        liveOpening.setFeatured(true);
        openingRepository.save(liveOpening);

        Opening draftOpening = new Opening();
        draftOpening.setId(UUID.randomUUID());
        draftOpening.setTitle("Draft Software Engineer");
        draftOpening.setEnterprise(enterprise);
        draftOpening.setStatus(OpeningStatus.DRAFT);
        draftOpening.setFeatured(false);
        openingRepository.save(draftOpening);

        mockMvc.perform(get("/api/v1/openings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Live Software Engineer"))
                .andExpect(jsonPath("$.data.content[0].featured").value(true));
    }
}
