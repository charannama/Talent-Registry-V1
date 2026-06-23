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
@Schema(description = "Response after closing an opening")
public class CloseOpeningResponse {

    @Schema(description = "UUID of the opening", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID id;

    @Schema(description = "Lifecycle status of the opening", example = "CLOSED")
    private OpeningStatus status;

    @Schema(description = "UUID of the user who closed the opening", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID closedBy;

    @Schema(description = "UTC ISO-8601 timestamp when opening was closed", example = "2026-06-18T10:00:00Z")
    private Instant closedAt;

    @Schema(description = "Message regarding the closure", example = "Opening closed successfully")
    private String message;
}
