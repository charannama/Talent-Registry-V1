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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCalendarEventRequest {

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
}
