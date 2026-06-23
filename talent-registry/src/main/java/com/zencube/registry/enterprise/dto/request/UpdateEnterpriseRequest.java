package com.zencube.registry.enterprise.dto.request;

import com.zencube.registry.enterprise.enums.CompanySize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating an Enterprise Profile")
public class UpdateEnterpriseRequest {

    @Schema(description = "Registered company name", example = "ZenCube Tech")
    @NotBlank(message = "Company name is required")
    private String companyName;

    @Schema(description = "Company registration or tax number", example = "REG-123456")
    private String registrationNumber;

    @Schema(description = "Primary industry", example = "Technology")
    private String industry;

    @Schema(description = "Corporate website URL", example = "https://www.zencube.com")
    private String website;

    @Schema(description = "Brief description of the company", example = "A leading software solutions provider.")
    private String description;

    @Schema(description = "URL to the company logo", example = "https://cdn.zencube.com/logo.png")
    private String logoUrl;

    // Address fields
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    @Schema(description = "Size category of the company")
    private CompanySize companySize;
    
    @Schema(description = "Primary industry sector", example = "Software")
    private String sector;

    @Schema(description = "Name of the primary hiring manager", example = "Jane Doe")
    private String hiringManagerName;

    @Schema(description = "Email of the primary hiring manager", example = "jane.doe@zencube.com")
    @Email(message = "Must be a valid email address")
    private String hiringManagerEmail;

    @Schema(description = "Business email domain", example = "zencube.com")
    @NotBlank(message = "Company email domain is required")
    private String companyEmailDomain;
}
