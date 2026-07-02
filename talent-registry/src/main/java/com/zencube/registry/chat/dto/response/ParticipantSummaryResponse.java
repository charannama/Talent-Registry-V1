package com.zencube.registry.chat.dto.response;

import com.zencube.registry.common.enums.RoleType;
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
public class ParticipantSummaryResponse {

    private UUID id;
    private String name;
    private String email;
    private RoleType role;
    private Boolean active;
    private Instant joinedAt;

}
