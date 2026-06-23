package com.zencube.registry.opening.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reject a job opening")
public class RejectionRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 2000, message = "Reason must not exceed 2000 characters")
    @Schema(description = "Reason for rejecting the opening", example = "The salary range is too low for this role.")
    private String reason;

    @Schema(description = "Whether the enterprise can revise and resubmit", example = "true")
    private Boolean canResubmit;
}
