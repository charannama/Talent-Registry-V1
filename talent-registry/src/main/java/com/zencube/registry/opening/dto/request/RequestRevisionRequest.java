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
@Schema(description = "Request to ask the enterprise to revise the job opening")
public class RequestRevisionRequest {

    @NotBlank(message = "Revision feedback is required")
    @Size(max = 5000, message = "Revision feedback must not exceed 5000 characters")
    @Schema(description = "Feedback for the enterprise detailing what needs to be revised", example = "Please provide a more detailed salary range and clarify the remote work policy.")
    private String feedback;
}
