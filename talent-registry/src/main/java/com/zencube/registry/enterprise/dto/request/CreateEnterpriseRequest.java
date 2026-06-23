package com.zencube.registry.enterprise.dto.request;

import com.zencube.registry.enterprise.enums.CompanySize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEnterpriseRequest {
    @NotBlank
    private String companyName;
    @Email
    private String domainEmail;
    private String companyWebsite;
    private String sector;
    private CompanySize companySize;
    private String gstNumber;
    private String hiringManagerName;
    @Email
    private String hiringManagerEmail;
}
