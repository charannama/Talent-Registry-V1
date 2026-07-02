package com.zencube.registry.interest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.TestDataFactory;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.expressinterest.dto.BookmarkRequest;
import com.zencube.registry.expressinterest.entity.ExpressInterest;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.security.model.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InterestControllerIT extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDataFactory testDataFactory;

    @Test
    void testBookmarkCandidate() throws Exception {
        User enterpriseUser = testDataFactory.createUser("it.ent@example.com");
        testDataFactory.createEnterprise(enterpriseUser, "IT Corp");

        User studentUser = testDataFactory.createUser("it.stud@example.com");
        StudentProfile student = testDataFactory.createStudent(studentUser);

        CustomUserDetails userDetails = new CustomUserDetails(
                enterpriseUser,
                List.of(new SimpleGrantedAuthority("ROLE_ENTERPRISE"))
        );

        BookmarkRequest request = new BookmarkRequest(student.getId(), null);

        mockMvc.perform(post("/api/v1/interests/bookmark")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testFormalRequestEscalation() throws Exception {
        User enterpriseUser = testDataFactory.createUser("it.ent2@example.com");
        EnterpriseAccount enterprise = testDataFactory.createEnterprise(enterpriseUser, "IT Corp 2");

        User studentUser = testDataFactory.createUser("it.stud2@example.com");
        StudentProfile student = testDataFactory.createStudent(studentUser);

        ExpressInterest interest = testDataFactory.createInterest(enterprise, student, null);

        CustomUserDetails userDetails = new CustomUserDetails(
                enterpriseUser,
                List.of(new SimpleGrantedAuthority("ROLE_ENTERPRISE"))
        );

        mockMvc.perform(post("/api/v1/interests/" + interest.getId() + "/formal-request")
                .with(user(userDetails)))
                .andExpect(status().isOk());
    }
}
