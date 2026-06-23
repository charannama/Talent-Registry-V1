package com.zencube.registry.opening.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to close a job opening")
public class CloseOpeningRequest {

    @Size(max = 5000, message = "Closure reason must not exceed 5000 characters")
    @Schema(description = "Optional reason for closing the opening", example = "Position filled successfully")
    private String reason;
}
