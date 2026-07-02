package com.zencube.registry.audit;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.TestDataFactory;
import com.zencube.registry.expressinterest.service.ExpressInterestService;
import com.zencube.registry.journal.entity.Journal;
import com.zencube.registry.journal.repository.JournalRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.security.model.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditIT extends IntegrationTestBase {

    @Autowired private ExpressInterestService expressInterestService;
    @Autowired private JournalRepository journalRepository;
    @Autowired private TestDataFactory testDataFactory;

    @Test
    void testAuditAspectTriggeredOnBookmark() {
        User enterpriseUser = testDataFactory.createUser("audit.ent@example.com");
        var enterprise = testDataFactory.createEnterprise(enterpriseUser, "Audit Corp");

        User studentUser = testDataFactory.createUser("audit.stud@example.com");
        StudentProfile student = testDataFactory.createStudent(studentUser);

        CustomUserDetails userDetails = new CustomUserDetails(
                enterpriseUser, 
                List.of(new SimpleGrantedAuthority("ROLE_ENTERPRISE"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        long journalCountBefore = journalRepository.count();

        // Calling a method annotated with @Audited
        expressInterestService.bookmark(enterprise.getId(), student.getId(), null);

        List<Journal> journals = journalRepository.findAll();
        assertThat(journals.size()).isGreaterThan((int) journalCountBefore);

        Journal latest = journals.get(journals.size() - 1);
        assertThat(latest.getJournableType()).isEqualTo("EXPRESS_INTEREST");
        assertThat(latest.getAction().name()).isEqualTo("CREATE");
    }
}
