package com.zencube.registry.enterprise.mapper;

import com.zencube.registry.enterprise.dto.request.CreateEnterpriseRequest;
import com.zencube.registry.enterprise.dto.response.EnterpriseResponse;
import com.zencube.registry.enterprise.dto.response.EnterpriseSummaryResponse;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;

public class EnterpriseMapper {
    private EnterpriseMapper(){}

    public static EnterpriseAccount toEntity(CreateEnterpriseRequest request) {
        EnterpriseAccount enterprise = new EnterpriseAccount();
        enterprise.setCompanyName(request.getCompanyName());
        enterprise.setDomainEmail(request.getDomainEmail());
        enterprise.setCompanyWebsite(request.getCompanyWebsite());
        enterprise.setSector(request.getSector());
        enterprise.setCompanySize(request.getCompanySize());
        return enterprise;
    }

    public static void updateEntity(EnterpriseAccount enterprise, com.zencube.registry.enterprise.dto.request.UpdateEnterpriseRequest request) {
        enterprise.setCompanyName(request.getCompanyName());
        enterprise.setRegistrationNumber(request.getRegistrationNumber());
        enterprise.setIndustry(request.getIndustry());
        enterprise.setCompanyWebsite(request.getWebsite());
        enterprise.setCompanyDescription(request.getDescription());
        enterprise.setLogoUrl(request.getLogoUrl());
        enterprise.setAddressLine1(request.getAddressLine1());
        enterprise.setAddressLine2(request.getAddressLine2());
        enterprise.setCity(request.getCity());
        enterprise.setState(request.getState());
        enterprise.setCountry(request.getCountry());
        enterprise.setPostalCode(request.getPostalCode());
        enterprise.setCompanySize(request.getCompanySize());
        enterprise.setSector(request.getSector());
        enterprise.setHiringManagerName(request.getHiringManagerName());
        enterprise.setHiringManagerEmail(request.getHiringManagerEmail());
        enterprise.setDomainEmail(request.getCompanyEmailDomain());
    }

    public static EnterpriseResponse toResponse(EnterpriseAccount enterprise) {
        return EnterpriseResponse.builder()
                .id(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .domainEmail(enterprise.getDomainEmail())
                .companyWebsite(enterprise.getCompanyWebsite())
                .sector(enterprise.getSector())
                .companySize(enterprise.getCompanySize())
                .onboardingStatus(enterprise.getOnboardingStatus())
                .build();
    }

    public static EnterpriseSummaryResponse toSummaryResponse(EnterpriseAccount enterprise) {
        return EnterpriseSummaryResponse.builder()
                .id(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .domainEmail(enterprise.getDomainEmail())
                .logoUrl(enterprise.getLogoUrl())
                .sector(enterprise.getSector())
                .onboardingStatus(enterprise.getOnboardingStatus())
                .build();
    }

    public static com.zencube.registry.enterprise.dto.response.EnterpriseStatusResponse toStatusResponse(EnterpriseAccount enterprise) {
        String status = enterprise.getOnboardingStatus().name();
        String message = "";
        
        switch (enterprise.getOnboardingStatus()) {
            case PENDING_HR_REVIEW:
                status = "PENDING";
                message = "Your application is under review (1-3 business days)";
                break;
            case APPROVED:
                status = "APPROVED";
                message = "Your account is active";
                break;
            case REJECTED:
                status = "REJECTED";
                message = "Your application was rejected";
                break;
            case SUSPENDED:
                status = "SUSPENDED";
                message = "Your account is suspended";
                break;
        }

        return com.zencube.registry.enterprise.dto.response.EnterpriseStatusResponse.builder()
                .enterpriseId(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .status(status)
                .statusMessage(message)
                .accountActive(enterprise.getAccountActive())
                .approvedAt(enterprise.getApprovedAt())
                .rejectedAt(enterprise.getRejectedAt())
                .rejectionReason(enterprise.getRejectionReason())
                .suspendedAt(enterprise.getSuspendedAt())
                .suspensionReason(enterprise.getSuspensionReason())
                .createdAt(enterprise.getCreatedAt())
                .build();
    }

    public static com.zencube.registry.enterprise.dto.response.EnterpriseApprovalResponse toApprovalResponse(EnterpriseAccount enterprise) {
        return com.zencube.registry.enterprise.dto.response.EnterpriseApprovalResponse.builder()
                .enterpriseId(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .status(enterprise.getOnboardingStatus().name())
                .accountActive(enterprise.getAccountActive())
                .approvedAt(enterprise.getApprovedAt())
                .approvedBy(enterprise.getOnboardedBy())
                .message("Enterprise successfully approved")
                .build();
    }

    public static com.zencube.registry.enterprise.dto.response.EnterpriseRejectionResponse toRejectionResponse(EnterpriseAccount enterprise) {
        return com.zencube.registry.enterprise.dto.response.EnterpriseRejectionResponse.builder()
                .enterpriseId(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .status(enterprise.getOnboardingStatus().name())
                .accountActive(enterprise.getAccountActive())
                .rejectionReason(enterprise.getRejectionReason())
                .rejectedAt(enterprise.getRejectedAt())
                .rejectedBy(enterprise.getRejectedBy())
                .message("Enterprise successfully rejected")
                .build();
    }

    public static com.zencube.registry.enterprise.dto.response.EnterpriseSuspensionResponse toSuspensionResponse(EnterpriseAccount enterprise) {
        return com.zencube.registry.enterprise.dto.response.EnterpriseSuspensionResponse.builder()
                .enterpriseId(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .status(enterprise.getOnboardingStatus().name())
                .accountActive(enterprise.getAccountActive())
                .suspensionReason(enterprise.getSuspensionReason())
                .suspendedAt(enterprise.getSuspendedAt())
                .message("Enterprise successfully suspended")
                .build();
    }

    public static com.zencube.registry.enterprise.dto.response.ReactivateEnterpriseResponse toReactivationResponse(EnterpriseAccount enterprise) {
        return com.zencube.registry.enterprise.dto.response.ReactivateEnterpriseResponse.builder()
                .enterpriseId(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .status(enterprise.getOnboardingStatus().name())
                .accountActive(enterprise.getAccountActive())
                .reactivatedAt(java.time.Instant.now()) // Extracted at time of event
                .reactivatedBy(enterprise.getOnboardedBy())
                .message("Enterprise successfully reactivated")
                .build();
    }

    public static com.zencube.registry.enterprise.dto.response.HrEnterpriseDetailResponse toHrDetailResponse(EnterpriseAccount enterprise) {
        return com.zencube.registry.enterprise.dto.response.HrEnterpriseDetailResponse.builder()
                .id(enterprise.getId())
                .userId(enterprise.getUser().getId())
                .companyName(enterprise.getCompanyName())
                .domainEmail(enterprise.getDomainEmail())
                .companyWebsite(enterprise.getCompanyWebsite())
                .registrationNumber(enterprise.getRegistrationNumber())
                .industry(enterprise.getIndustry())
                .companyDescription(enterprise.getCompanyDescription())
                .addressLine1(enterprise.getAddressLine1())
                .addressLine2(enterprise.getAddressLine2())
                .city(enterprise.getCity())
                .state(enterprise.getState())
                .country(enterprise.getCountry())
                .postalCode(enterprise.getPostalCode())
                .companySize(enterprise.getCompanySize())
                .logoUrl(enterprise.getLogoUrl())
                .gstNumber(enterprise.getGstNumber())
                .sector(enterprise.getSector())
                .hiringManagerName(enterprise.getHiringManagerName())
                .hiringManagerEmail(enterprise.getHiringManagerEmail())
                .hiringManagerPhone(enterprise.getHiringManagerPhone())
                .onboardingStatus(enterprise.getOnboardingStatus())
                .accountActive(enterprise.getAccountActive())
                .onboardedBy(enterprise.getOnboardedBy())
                .approvedAt(enterprise.getApprovedAt())
                .rejectedBy(enterprise.getRejectedBy())
                .rejectedAt(enterprise.getRejectedAt())
                .rejectionReason(enterprise.getRejectionReason())
                .suspendedBy(enterprise.getSuspendedBy())
                .suspendedAt(enterprise.getSuspendedAt())
                .suspensionReason(enterprise.getSuspensionReason())
                .reactivatedBy(enterprise.getReactivatedBy())
                .reactivatedAt(enterprise.getReactivatedAt())
                .lastStatusChangedBy(enterprise.getLastStatusChangedBy())
                .lastStatusChangedAt(enterprise.getLastStatusChangedAt())
                .createdAt(enterprise.getCreatedAt())
                .createdBy(enterprise.getCreatedBy())
                .updatedAt(enterprise.getUpdatedAt())
                .updatedBy(enterprise.getUpdatedBy())
                .build();
    }

    public static com.zencube.registry.enterprise.dto.response.EnterpriseDashboardResponse toDashboardResponse(EnterpriseAccount enterprise) {
        return com.zencube.registry.enterprise.dto.response.EnterpriseDashboardResponse.builder()
                .enterpriseId(enterprise.getId())
                .companyName(enterprise.getCompanyName())
                .status(enterprise.getOnboardingStatus())
                .statusMessage(enterprise.getDashboardMessage())
                .canPostOpenings(enterprise.canPostOpenings())
                .canSearchTalent(enterprise.canSearchTalent())
                .canManageJobs(enterprise.canManageJobs())
                .approvedAt(enterprise.getApprovedAt())
                .rejectedAt(enterprise.getRejectedAt())
                .rejectionReason(enterprise.getRejectionReason())
                .suspendedAt(enterprise.getSuspendedAt())
                .suspensionReason(enterprise.getSuspensionReason())
                .createdAt(enterprise.getCreatedAt())
                .updatedAt(enterprise.getUpdatedAt())
                .build();
    }
}
