package com.zencube.registry.chat.dto.request;

import com.zencube.registry.chat.enums.ThreadType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateThreadRequest {

    @NotNull(message = "Thread type is required")
    private ThreadType threadType;

    @NotEmpty(message = "Participants list cannot be empty")
    @Size(min = 1, message = "At least one participant is required")
    private List<UUID> participantIds;

    private String contextableType;

    private UUID contextableId;

}
