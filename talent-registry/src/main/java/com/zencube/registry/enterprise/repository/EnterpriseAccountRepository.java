package com.zencube.registry.enterprise.repository;

import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EnterpriseAccountRepository extends JpaRepository<EnterpriseAccount, UUID>, JpaSpecificationExecutor<EnterpriseAccount> {
    Optional<EnterpriseAccount> findByUserId(UUID userId);
    Optional<EnterpriseAccount> findByDomainEmail(String domainEmail);
    boolean existsByDomainEmail(String domainEmail);
    boolean existsByCompanyName(String companyName);
    boolean existsByCompanyNameIgnoreCase(String companyName);
    boolean existsByUserId(UUID userId);
    
    boolean existsByCompanyNameIgnoreCaseAndIdNot(String companyName, UUID id);
    boolean existsByDomainEmailAndIdNot(String domainEmail, UUID id);
    
    Optional<EnterpriseAccount> findByCompanyNameIgnoreCase(String companyName);
    List<EnterpriseAccount> findByOnboardingStatus(EnterpriseOnboardingStatus status);
    List<EnterpriseAccount> findByCompanyNameContainingIgnoreCase(String companyName);
    Optional<EnterpriseAccount> findByUserIdAndOnboardingStatus(UUID userId, EnterpriseOnboardingStatus status);
    Optional<EnterpriseAccount> findByIdAndOnboardingStatus(UUID id, EnterpriseOnboardingStatus status);

    long countByOnboardingStatus(EnterpriseOnboardingStatus status);
    
    long countByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
    
    long countByOnboardingStatusAndCreatedAtBetween(EnterpriseOnboardingStatus status, java.time.Instant start, java.time.Instant end);

    @org.springframework.data.jpa.repository.Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (approved_at - created_at))) FROM enterprise_accounts WHERE onboarding_status = 'APPROVED' AND approved_at IS NOT NULL AND created_at IS NOT NULL", nativeQuery = true)
    Double findAverageApprovalTimeInSeconds();
}
