package com.zencube.registry.common;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.expressinterest.entity.ExpressInterest;
import com.zencube.registry.expressinterest.enums.InterestStage;
import com.zencube.registry.expressinterest.repository.ExpressInterestRepository;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.repository.OpeningRepository;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class TestDataFactory {

    @Autowired private UserRepository userRepository;
    @Autowired private EnterpriseAccountRepository enterpriseAccountRepository;
    @Autowired private StudentProfileRepository studentProfileRepository;
    @Autowired private OpeningRepository openingRepository;
    @Autowired private ExpressInterestRepository expressInterestRepository;

    @Transactional
    public User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed_pass");
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public EnterpriseAccount createEnterprise(User user, String companyName) {
        EnterpriseAccount enterprise = new EnterpriseAccount();
        enterprise.setUser(user);
        enterprise.setCompanyName(companyName);
        enterprise.setDomainEmail(user.getEmail());
        // prePersist will set status to PENDING_HR_REVIEW
        enterprise = enterpriseAccountRepository.save(enterprise);
        // Approve it for tests
        enterprise.approve(UUID.randomUUID());
        return enterpriseAccountRepository.save(enterprise);
    }

    @Transactional
    public StudentProfile createStudent(User user) {
        StudentProfile student = new StudentProfile();
        student.setUser(user);
        student.setInstitution("Test University");
        student.setDiscipline("Computer Science");
        return studentProfileRepository.save(student);
    }

    @Transactional
    public Opening createOpening(EnterpriseAccount enterprise, String title) {
        Opening opening = new Opening();
        opening.setEnterprise(enterprise);
        opening.setTitle(title);
        return openingRepository.save(opening);
    }

    @Transactional
    public ExpressInterest createInterest(EnterpriseAccount enterprise, StudentProfile student, Opening opening) {
        ExpressInterest interest = ExpressInterest.builder()
                .enterprise(enterprise)
                .student(student)
                .opening(opening)
                .stage(InterestStage.BOOKMARK)
                .build();
        return expressInterestRepository.save(interest);
    }
}
