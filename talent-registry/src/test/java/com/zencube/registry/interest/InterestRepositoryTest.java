package com.zencube.registry.interest;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.TestDataFactory;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.expressinterest.entity.ExpressInterest;
import com.zencube.registry.expressinterest.enums.InterestStage;
import com.zencube.registry.expressinterest.repository.ExpressInterestRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class InterestRepositoryTest extends IntegrationTestBase {

    @Autowired private ExpressInterestRepository expressInterestRepository;
    @Autowired private TestDataFactory testDataFactory;

    @Test
    void testFindByEnterpriseIdAndStage() {
        User enterpriseUser = testDataFactory.createUser("ent.repo@example.com");
        EnterpriseAccount enterprise = testDataFactory.createEnterprise(enterpriseUser, "Repo Corp");

        User studentUser = testDataFactory.createUser("student.repo@example.com");
        StudentProfile student = testDataFactory.createStudent(studentUser);

        ExpressInterest interest = testDataFactory.createInterest(enterprise, student, null);

        Page<ExpressInterest> page = expressInterestRepository.findByEnterpriseIdAndStage(enterprise.getId(), InterestStage.BOOKMARK, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent().get(0).getEnterprise().getId()).isEqualTo(enterprise.getId());
    }
}
