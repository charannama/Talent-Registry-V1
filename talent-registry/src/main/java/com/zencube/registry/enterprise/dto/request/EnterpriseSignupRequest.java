package com.zencube.registry.enterprise.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for Enterprise Registration")
public class EnterpriseSignupRequest {

    @Schema(description = "Business email address", example = "hr@company.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(description = "Secure password", example = "StrongP@ssw0rd")
    @NotBlank(message = "Password is required")
    private String password;

    @Schema(description = "Registered company name", example = "ZenCube Tech")
    @NotBlank(message = "Company name is required")
    private String companyName;

    @Schema(description = "Company registration or tax number", example = "REG-123456")
    private String registrationNumber;
    
    @Schema(description = "Primary industry", example = "Technology")
    private String industry;
    
    @Schema(description = "Corporate website URL", example = "https://www.company.com")
    private String website;
    
    @Schema(description = "Brief description of the company", example = "A leading software solutions provider.")
    private String description;
    
    @Schema(description = "URL to the company logo", example = "https://cdn.company.com/logo.png")
    private String logoUrl;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    private String hiringManagerName;
    private String hiringManagerEmail;
    private String hiringManagerPhone;
}
