package com.zencube.registry.calendar.dto.request;

import com.zencube.registry.calendar.enums.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateCalendarEventRequest {

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull(message = "Start time is mandatory")
    private Instant startTime;

    @NotNull(message = "End time is mandatory")
    private Instant endTime;

    @NotBlank(message = "Timezone is mandatory")
    @Size(max = 100)
    private String timezone;

    @Size(max = 500)
    private String location;

    private Boolean allDayEvent;

    @NotNull(message = "Event category is mandatory")
    private EventCategory eventCategory;

    @NotBlank(message = "Eventable type is mandatory")
    @Size(max = 100)
    private String eventableType;

    @NotNull(message = "Eventable ID is mandatory")
    private UUID eventableId;
}
