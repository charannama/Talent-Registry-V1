package com.zencube.registry.enterprise.service;

import com.zencube.registry.enterprise.dto.request.CreateEnterpriseRequest;
import com.zencube.registry.enterprise.dto.request.UpdateEnterpriseRequest;
import com.zencube.registry.enterprise.dto.response.EnterpriseResponse;
import com.zencube.registry.enterprise.dto.request.EnterpriseSignupRequest;
import com.zencube.registry.enterprise.dto.response.EnterpriseSignupResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.dto.response.EnterpriseSummaryResponse;
import com.zencube.registry.enterprise.dto.response.HrEnterpriseDetailResponse;

public interface EnterpriseService {
    EnterpriseSignupResponse signup(EnterpriseSignupRequest request);
    EnterpriseResponse registerEnterprise(CreateEnterpriseRequest request);
    EnterpriseResponse getEnterprise(UUID enterpriseId);
    EnterpriseResponse updateEnterprise(UUID enterpriseId, UpdateEnterpriseRequest request);
    com.zencube.registry.enterprise.dto.response.EnterpriseApprovalResponse approveEnterprise(UUID enterpriseId);
    com.zencube.registry.enterprise.dto.response.EnterpriseRejectionResponse rejectEnterprise(UUID enterpriseId, String reason);
    com.zencube.registry.enterprise.dto.response.EnterpriseSuspensionResponse suspendEnterprise(UUID enterpriseId, String reason);
    com.zencube.registry.enterprise.dto.response.ReactivateEnterpriseResponse reactivateEnterprise(UUID enterpriseId);
    EnterpriseResponse updateMyProfile(UpdateEnterpriseRequest request);
    com.zencube.registry.enterprise.dto.response.EnterpriseStatusResponse getMyRegistrationStatus();
    Page<EnterpriseSummaryResponse> getEnterprises(EnterpriseOnboardingStatus status, String companyName, Pageable pageable);
    HrEnterpriseDetailResponse getEnterpriseDetailsForHr(UUID enterpriseId);
    com.zencube.registry.enterprise.dto.response.EnterpriseDashboardResponse getMyDashboard();
}
