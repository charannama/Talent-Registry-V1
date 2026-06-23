package com.zencube.registry.talent.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SuspendProfileRequest {

    @NotBlank(message = "Suspension reason is required")
    private String reason;

}
