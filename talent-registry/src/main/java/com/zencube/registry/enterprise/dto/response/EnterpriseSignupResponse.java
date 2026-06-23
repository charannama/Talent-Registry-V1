package com.zencube.registry.enterprise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for Enterprise Signup")
public class EnterpriseSignupResponse {
    @Schema(description = "Unique ID of the enterprise account")
    private UUID enterpriseId;
    
    @Schema(description = "Unique ID of the created user")
    private UUID userId;
    
    @Schema(description = "Registered company name")
    private String companyName;
    
    @Schema(description = "Current onboarding status")
    private String status;
    
    @Schema(description = "Success or info message")
    private String message;
}
