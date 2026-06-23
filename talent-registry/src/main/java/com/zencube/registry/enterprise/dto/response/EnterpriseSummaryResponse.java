package com.zencube.registry.enterprise.dto.response;

import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary Response DTO for Enterprise Accounts")
public class EnterpriseSummaryResponse {

    @Schema(description = "Unique ID of the enterprise")
    private UUID id;

    @Schema(description = "Registered company name", example = "ZenCube Tech")
    private String companyName;

    @Schema(description = "Business domain email", example = "contact@zencube.com")
    private String domainEmail;

    @Schema(description = "URL to the company logo", example = "https://cdn.company.com/logo.png")
    private String logoUrl;

    @Schema(description = "Primary industry sector", example = "Technology")
    private String sector;

    @Schema(description = "Current HR onboarding status")
    private EnterpriseOnboardingStatus onboardingStatus;
}
