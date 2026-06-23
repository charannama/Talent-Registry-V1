package com.zencube.registry.opening.controller;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.enums.WorkMode;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.security.service.JwtService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StudentOpeningControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpeningRepository openingRepository;

    @Autowired
    private EnterpriseAccountRepository enterpriseAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private JwtService jwtService;

    private String studentToken;
    private EnterpriseAccount approvedEnterprise;
    private EnterpriseAccount suspendedEnterprise;

    @BeforeEach
    void setUp() {
        // Setup Student User
        User student = new User();
        student.setId(UUID.randomUUID());
        student.setEmail("student@example.com");
        student.setPasswordHash("hash");
        student.setStatus(com.zencube.registry.common.enums.UserStatus.ACTIVE);
        userRepository.save(student);

        Role studentRole = roleRepository.findByNameAndDeletedFalse("STUDENT")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("STUDENT");
                    r.setId(UUID.randomUUID());
                    return roleRepository.save(r);
                });

        UserRole ur = new UserRole();
        ur.setId(UUID.randomUUID());
        ur.setUser(student);
        ur.setRole(studentRole);
        userRoleRepository.save(ur);

        org.springframework.security.core.userdetails.UserDetails studentDetails = org.springframework.security.core.userdetails.User.builder()
                .username("student@example.com")
                .password("hash")
                .authorities("ROLE_STUDENT")
                .build();
        studentToken = jwtService.generateAccessToken(studentDetails);

        // Setup Enterprise
        User entUser = new User();
        entUser.setId(UUID.randomUUID());
        entUser.setEmail("ent1@example.com");
        entUser.setPasswordHash("hash");
        entUser.setStatus(com.zencube.registry.common.enums.UserStatus.ACTIVE);
        userRepository.save(entUser);

        approvedEnterprise = new EnterpriseAccount();
        approvedEnterprise.setId(UUID.randomUUID());
        approvedEnterprise.setCompanyName("Tech Innovators");
        approvedEnterprise.setDomainEmail("hr@techinnovators.com");
        approvedEnterprise.setUser(entUser);
        approvedEnterprise.setAccountActive(true);
        approvedEnterprise.setOnboardingStatus(EnterpriseOnboardingStatus.APPROVED);
        enterpriseAccountRepository.save(approvedEnterprise);

        User entUser2 = new User();
        entUser2.setId(UUID.randomUUID());
        entUser2.setEmail("ent2@example.com");
        entUser2.setPasswordHash("hash");
        entUser2.setStatus(com.zencube.registry.common.enums.UserStatus.ACTIVE);
        userRepository.save(entUser2);

        suspendedEnterprise = new EnterpriseAccount();
        suspendedEnterprise.setId(UUID.randomUUID());
        suspendedEnterprise.setCompanyName("Suspended Corp");
        suspendedEnterprise.setDomainEmail("hr@suspended.com");
        suspendedEnterprise.setUser(entUser2);
        suspendedEnterprise.setAccountActive(false);
        suspendedEnterprise.setOnboardingStatus(EnterpriseOnboardingStatus.SUSPENDED);
        enterpriseAccountRepository.save(suspendedEnterprise);

        // Seed Openings
        Opening opening1 = new Opening();
        opening1.setId(UUID.randomUUID());
        opening1.setTitle("Java Developer");
        opening1.setEnterprise(approvedEnterprise);
        opening1.setStatus(OpeningStatus.LIVE);
        opening1.setDomain("software_engineering");
        opening1.setJobType(JobType.FULL_TIME);
        opening1.setWorkMode(WorkMode.REMOTE);
        opening1.setSalaryMin(new BigDecimal("100000"));
        opening1.setSalaryMax(new BigDecimal("150000"));
        opening1.setApplicationDeadline(Instant.now().plus(10, ChronoUnit.DAYS));
        opening1.setGraduationYears("2024,2025");
        opening1.setFeatured(true);
        openingRepository.save(opening1);

        Opening opening2 = new Opening();
        opening2.setId(UUID.randomUUID());
        opening2.setTitle("Data Scientist");
        opening2.setEnterprise(approvedEnterprise);
        opening2.setStatus(OpeningStatus.LIVE);
        opening2.setDomain("data_science");
        opening2.setJobType(JobType.INTERNSHIP);
        opening2.setWorkMode(WorkMode.ONSITE);
        opening2.setSalaryMin(new BigDecimal("60000"));
        opening2.setSalaryMax(new BigDecimal("80000"));
        opening2.setApplicationDeadline(Instant.now().plus(5, ChronoUnit.DAYS));
        opening2.setGraduationYears("2026");
        opening2.setFeatured(false);
        openingRepository.save(opening2);

        Opening opening3Expired = new Opening();
        opening3Expired.setId(UUID.randomUUID());
        opening3Expired.setTitle("Expired Job");
        opening3Expired.setEnterprise(approvedEnterprise);
        opening3Expired.setStatus(OpeningStatus.LIVE);
        opening3Expired.setApplicationDeadline(Instant.now().minus(1, ChronoUnit.DAYS)); // Expired
        openingRepository.save(opening3Expired);

        Opening opening4Suspended = new Opening();
        opening4Suspended.setId(UUID.randomUUID());
        opening4Suspended.setTitle("Suspended Job");
        opening4Suspended.setEnterprise(suspendedEnterprise);
        opening4Suspended.setStatus(OpeningStatus.LIVE);
        opening4Suspended.setApplicationDeadline(Instant.now().plus(10, ChronoUnit.DAYS));
        openingRepository.save(opening4Suspended);
    }

    @AfterEach
    void tearDown() {
        openingRepository.deleteAll();
        enterpriseAccountRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully search and filter openings with valid criteria")
    void searchOpenings_Success() throws Exception {
        mockMvc.perform(get("/api/v1/student/openings")
                        .param("domain", "software_engineering")
                        .param("jobType", "FULL_TIME")
                        .param("company", "Tech Innovators")
                        .param("salaryMin", "120000")
                        .param("salaryMax", "130000")
                        .param("workMode", "REMOTE")
                        .param("graduationYear", "2025")
                        .param("featured", "true")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Java Developer"));
    }

    @Test
    @DisplayName("Should hide expired and suspended openings inherently")
    void searchOpenings_HidesInvalidStates() throws Exception {
        mockMvc.perform(get("/api/v1/student/openings")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].title").value("Java Developer")) // Featured first
                .andExpect(jsonPath("$.data.content[1].title").value("Data Scientist"));
    }

    @Test
    @DisplayName("Should fail when page size is excessive")
    void searchOpenings_ExcessivePageSize() throws Exception {
        mockMvc.perform(get("/api/v1/student/openings")
                        .param("size", "101")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
