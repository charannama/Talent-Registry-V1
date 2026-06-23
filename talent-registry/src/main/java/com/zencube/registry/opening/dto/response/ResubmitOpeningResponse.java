package com.zencube.registry.opening.dto.response;

import com.zencube.registry.opening.enums.OpeningStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after resubmitting an opening")
public class ResubmitOpeningResponse {

    @Schema(description = "UUID of the opening", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID openingId;

    @Schema(description = "Lifecycle status of the opening", example = "PENDING_APPROVAL")
    private OpeningStatus status;

    @Schema(description = "UTC ISO-8601 timestamp when opening was resubmitted", example = "2026-06-18T10:00:00Z")
    private Instant submittedAt;

    @Schema(description = "Message regarding the resubmission", example = "Opening resubmitted successfully")
    private String message;
}
