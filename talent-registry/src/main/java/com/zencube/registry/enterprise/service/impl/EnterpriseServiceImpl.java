package com.zencube.registry.enterprise.service.impl;

import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import com.zencube.registry.enterprise.dto.request.CreateEnterpriseRequest;
import com.zencube.registry.enterprise.dto.request.UpdateEnterpriseRequest;
import com.zencube.registry.enterprise.dto.request.EnterpriseSignupRequest;
import com.zencube.registry.enterprise.dto.response.EnterpriseResponse;
import com.zencube.registry.enterprise.dto.response.EnterpriseSignupResponse;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.mapper.EnterpriseMapper;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.enterprise.service.EnterpriseService;
import com.zencube.registry.enterprise.service.EnterpriseStatusAuditService;
import com.zencube.registry.enterprise.validation.EnterpriseDomainValidator;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.auth.verification.entity.EmailVerificationToken;
import com.zencube.registry.auth.verification.repository.EmailVerificationTokenRepository;
import com.zencube.registry.auth.verification.event.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseAccountRepository repository;
    private final EnterpriseStatusAuditService auditService;
    private final com.zencube.registry.enterprise.service.EnterpriseAccessAuditService accessAuditService;
    private final EnterpriseDomainValidator domainValidator;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final com.zencube.registry.enterprise.repository.EnterpriseProfileAuditRepository auditRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public EnterpriseSignupResponse signup(EnterpriseSignupRequest request) {
        
        // Step 1 & 2 (Email Required/Format), Step 4 (Company Required), Step 8 (Password) 
        // and Step 9 are handled by @Valid in Controller.

        // Step 3: Block Personal Domains
        if (!domainValidator.isValidEnterpriseDomain(request.getEmail())) {
            throw new BusinessException(
                    "Please use your company email address",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_ENTERPRISE_EMAIL"
            );
        }

        // Step 5: Duplicate Company
        if (repository.existsByCompanyNameIgnoreCase(request.getCompanyName())) {
            throw new BusinessException(
                    "Company already registered",
                    HttpStatus.CONFLICT,
                    "DUPLICATE_COMPANY"
            );
        }

        // Step 6: Duplicate Domain
        if (repository.existsByDomainEmail(request.getEmail())) {
            throw new BusinessException(
                    "Enterprise domain email already registered",
                    HttpStatus.CONFLICT,
                    "DUPLICATE_DOMAIN"
            );
        }

        // Step 7: Duplicate User
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "Email already registered as user",
                    HttpStatus.CONFLICT,
                    "DUPLICATE_USER"
            );
        }

        // 1. Create User
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        user = userRepository.save(user);

        // 2. Assign ENTERPRISE_RECRUITER Role
        Role role = roleRepository.findByNameAndDeletedFalse(RoleType.ENTERPRISE_RECRUITER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);

        // 3. Generate Verification Token
        String tokenStr = UUID.randomUUID().toString();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(tokenStr)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        tokenRepository.save(token);

        // 4. Publish Event
        eventPublisher.publishEvent(new EmailVerificationEvent(this, user.getId(), user.getEmail(), tokenStr));

        // 5. Create Enterprise Account
        EnterpriseAccount enterprise = EnterpriseAccount.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .domainEmail(request.getEmail())
                .registrationNumber(request.getRegistrationNumber())
                .industry(request.getIndustry())
                .companyWebsite(request.getWebsite())
                .companyDescription(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .hiringManagerName(request.getHiringManagerName())
                .hiringManagerEmail(request.getHiringManagerEmail())
                .hiringManagerPhone(request.getHiringManagerPhone())
                .onboardingStatus(EnterpriseOnboardingStatus.PENDING_HR_REVIEW)
                .accountActive(false)
                .build();
        enterprise = repository.save(enterprise);

        return EnterpriseSignupResponse.builder()
                .enterpriseId(enterprise.getId())
                .userId(user.getId())
                .companyName(enterprise.getCompanyName())
                .status(enterprise.getOnboardingStatus().name())
                .message("Enterprise registered successfully. Please check your email for verification.")
                .build();
    }

    @Override
    public EnterpriseResponse registerEnterprise(CreateEnterpriseRequest request) {

        if (repository.existsByDomainEmail(request.getDomainEmail())) {
            throw new ConflictException("Enterprise email already exists");
        }

        if (repository.existsByCompanyName(request.getCompanyName())) {
            throw new ConflictException("Company already registered");
        }

        EnterpriseAccount enterprise = EnterpriseMapper.toEntity(request);

        enterprise.setOnboardingStatus(EnterpriseOnboardingStatus.PENDING_HR_REVIEW);

        repository.save(enterprise);

        return EnterpriseMapper.toResponse(enterprise);
    }

    @Override
    public EnterpriseResponse getEnterprise(UUID enterpriseId) {
        EnterpriseAccount enterprise = repository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));
        return EnterpriseMapper.toResponse(enterprise);
    }

    @Override
    public EnterpriseResponse updateEnterprise(UUID enterpriseId, UpdateEnterpriseRequest request) {
        EnterpriseAccount enterprise = repository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));

        if (EnterpriseOnboardingStatus.REJECTED.equals(enterprise.getOnboardingStatus())) {
            throw new com.zencube.registry.common.exception.ForbiddenException("Rejected enterprise cannot update profile");
        }

        if (request.getWebsite() != null) {
            enterprise.setCompanyWebsite(request.getWebsite());
        }
        if (request.getSector() != null) {
            enterprise.setSector(request.getSector());
        }
        if (request.getCompanySize() != null) {
            enterprise.setCompanySize(request.getCompanySize());
        }
        if (request.getHiringManagerName() != null) {
            enterprise.setHiringManagerName(request.getHiringManagerName());
        }
        if (request.getHiringManagerEmail() != null) {
            enterprise.setHiringManagerEmail(request.getHiringManagerEmail());
        }


        repository.save(enterprise);
        return EnterpriseMapper.toResponse(enterprise);
    }

    @Override
    public EnterpriseResponse updateMyProfile(UpdateEnterpriseRequest request) {
        User user = (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        EnterpriseAccount enterprise = repository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));
                
        if (!enterprise.isApproved()) {
            throw new BusinessException("Enterprise is not approved", HttpStatus.FORBIDDEN, "ENTERPRISE_NOT_APPROVED");
        }

        if (repository.existsByCompanyNameIgnoreCaseAndIdNot(request.getCompanyName(), enterprise.getId())) {
            throw new BusinessException("Company name already registered", HttpStatus.CONFLICT, "DUPLICATE_COMPANY");
        }

        if (repository.existsByDomainEmailAndIdNot(request.getCompanyEmailDomain(), enterprise.getId())) {
            throw new BusinessException("Domain email already registered", HttpStatus.CONFLICT, "DUPLICATE_DOMAIN");
        }
        
        com.zencube.registry.enterprise.entity.EnterpriseProfileAudit audit = com.zencube.registry.enterprise.entity.EnterpriseProfileAudit.builder()
                .enterprise(enterprise)
                .previousCompanyName(enterprise.getCompanyName())
                .previousDomainEmail(enterprise.getDomainEmail())
                .changeSummary("Profile updated by owner")
                .updatedBy(user.getId())
                .build();
        auditRepository.save(audit);

        EnterpriseMapper.updateEntity(enterprise, request);
        
        repository.save(enterprise);
        return EnterpriseMapper.toResponse(enterprise);
    }

    @Override
    @Transactional
    public com.zencube.registry.enterprise.dto.response.EnterpriseApprovalResponse approveEnterprise(UUID enterpriseId) {
        User hrUser = (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        EnterpriseAccount enterprise = repository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));

        com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus previousStatus = enterprise.getOnboardingStatus();
        
        enterprise.approve(hrUser.getId());
        repository.save(enterprise);

        auditService.logStatusChange(
                enterprise.getId(),
                hrUser.getId(),
                previousStatus,
                enterprise.getOnboardingStatus(),
                "Approved by HR"
        );

        return EnterpriseMapper.toApprovalResponse(enterprise);
    }

    @Override
    @Transactional
    public com.zencube.registry.enterprise.dto.response.EnterpriseRejectionResponse rejectEnterprise(UUID enterpriseId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new com.zencube.registry.common.exception.BusinessException("Rejection reason is mandatory", org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_REASON");
        }

        User hrUser = (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        EnterpriseAccount enterprise = repository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));

        com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus previousStatus = enterprise.getOnboardingStatus();
        
        enterprise.reject(reason, hrUser.getId());
        repository.save(enterprise);

        auditService.logStatusChange(
                enterprise.getId(),
                hrUser.getId(),
                previousStatus,
                enterprise.getOnboardingStatus(),
                "Rejected: " + reason
        );

        return EnterpriseMapper.toRejectionResponse(enterprise);
    }

    @Override
    @Transactional
    public com.zencube.registry.enterprise.dto.response.EnterpriseSuspensionResponse suspendEnterprise(UUID enterpriseId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new com.zencube.registry.common.exception.BusinessException("Suspension reason is mandatory", org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_REASON");
        }

        User hrUser = (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        EnterpriseAccount enterprise = repository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));

        com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus previousStatus = enterprise.getOnboardingStatus();
        
        enterprise.suspend(reason, hrUser.getId());
        repository.save(enterprise);

        auditService.logStatusChange(
                enterprise.getId(),
                hrUser.getId(),
                previousStatus,
                enterprise.getOnboardingStatus(),
                "Suspended: " + reason
        );

        return EnterpriseMapper.toSuspensionResponse(enterprise);
    }

    @Override
    @Transactional
    public com.zencube.registry.enterprise.dto.response.ReactivateEnterpriseResponse reactivateEnterprise(UUID enterpriseId) {
        User hrUser = (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        EnterpriseAccount enterprise = repository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));

        com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus previousStatus = enterprise.getOnboardingStatus();
        
        enterprise.reinstate(hrUser.getId());
        repository.save(enterprise);

        auditService.logStatusChange(
                enterprise.getId(),
                hrUser.getId(),
                previousStatus,
                enterprise.getOnboardingStatus(),
                "Reactivated by HR"
        );

        return EnterpriseMapper.toReactivationResponse(enterprise);
    }

    @Override
    public com.zencube.registry.enterprise.dto.response.EnterpriseStatusResponse getMyRegistrationStatus() {
        User user = (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        EnterpriseAccount enterprise = repository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Enterprise account not found", HttpStatus.NOT_FOUND, "ENTERPRISE_NOT_FOUND"));

        return EnterpriseMapper.toStatusResponse(enterprise);
    }

    @Override
    public org.springframework.data.domain.Page<com.zencube.registry.enterprise.dto.response.EnterpriseSummaryResponse> getEnterprises(
            com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus status, 
            String companyName, 
            org.springframework.data.domain.Pageable pageable) {
        
        org.springframework.data.jpa.domain.Specification<EnterpriseAccount> spec = 
                com.zencube.registry.enterprise.repository.EnterpriseAccountSpecification.filterBy(status, companyName);
        
        return repository.findAll(spec, pageable).map(EnterpriseMapper::toSummaryResponse);
    }

    @Override
    public com.zencube.registry.enterprise.dto.response.HrEnterpriseDetailResponse getEnterpriseDetailsForHr(UUID enterpriseId) {
        EnterpriseAccount enterprise = repository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise account not found"));
                
        return EnterpriseMapper.toHrDetailResponse(enterprise);
    }

    @Override
    public com.zencube.registry.enterprise.dto.response.EnterpriseDashboardResponse getMyDashboard() {
        User user = (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        EnterpriseAccount enterprise = repository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Enterprise account not found", HttpStatus.NOT_FOUND, "ENTERPRISE_NOT_FOUND"));

        // Phase 9 Audit Event
        String eventType = "DashboardAccessed";
        if (enterprise.isRejected()) {
            eventType = "RejectedEnterpriseAccessAttempt";
        } else if (enterprise.isSuspended()) {
            eventType = "SuspendedEnterpriseAccessAttempt";
        } else if (!enterprise.isApproved()) {
            eventType = "EnterpriseBlocked";
        }

        accessAuditService.logAccessAttempt(enterprise.getId(), user.getId(), eventType, enterprise.getOnboardingStatus(), "/api/v1/enterprises/my/dashboard");
        
        return EnterpriseMapper.toDashboardResponse(enterprise);
    }
}
