package com.zencube.registry.enterprise.dto.response;

import com.zencube.registry.enterprise.enums.CompanySize;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO for Enterprise Account Details")
public class EnterpriseResponse {
    @Schema(description = "Unique ID of the enterprise")
    private UUID id;
    
    @Schema(description = "Registered company name", example = "ZenCube Tech")
    private String companyName;
    
    @Schema(description = "Business domain email", example = "contact@zencube.com")
    private String domainEmail;
    
    @Schema(description = "Corporate website URL", example = "https://www.zencube.com")
    private String companyWebsite;
    
    @Schema(description = "Primary industry sector", example = "Technology")
    private String sector;
    
    @Schema(description = "Size category of the company")
    private CompanySize companySize;
    
    @Schema(description = "Current HR onboarding status")
    private EnterpriseOnboardingStatus onboardingStatus;
}
