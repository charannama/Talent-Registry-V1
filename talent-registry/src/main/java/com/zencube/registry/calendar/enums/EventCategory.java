package com.zencube.registry.calendar.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categories of events that can be scheduled in the Calendar Module")
public enum EventCategory {
    
    @Schema(description = "Candidate interview sessions")
    INTERVIEW("Interview"),
    
    @Schema(description = "New hire onboarding workflows")
    ONBOARDING("Onboarding"),
    
    @Schema(description = "Follow-up tasks and syncs")
    FOLLOW_UP("Follow Up"),
    
    @Schema(description = "Internal enterprise meetings")
    INTERNAL("Internal");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
