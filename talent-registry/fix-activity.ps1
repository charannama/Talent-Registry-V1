$files = @(
    "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\application\service\impl\ApplicationServiceImpl.java",
    "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\calendar\service\impl\CalendarExportServiceImpl.java",
    "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\calendar\service\impl\CalendarServiceImpl.java",
    "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\chat\service\impl\ChatServiceImpl.java"
)

foreach ($f in $files) {
    if (-not (Test-Path $f)) { continue }
    $content = Get-Content $f -Raw
    
    # 1. ApplicationServiceImpl.java
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"INTERVIEW_SCHEDULED",\s*"Application",\s*application\.getId\(\)\.toString\(\),\s*"APPLICATION",\s*application\.getId\(\),\s*"Interview scheduled for "\s*\+\s*profile\.getUser\(\)\.getEmail\(\)\s*\);', 'activityService.recordActivity("Application", application.getId().toString(), "APPLICATION", application.getId().toString(), com.zencube.registry.activity.enums.ActivityType.INTERVIEW_SCHEDULED, "Interview scheduled for " + profile.getUser().getEmail());'
    
    # 2. CalendarExportServiceImpl.java
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"CALENDAR_EXPORTED",\s*"CalendarEvent",\s*event\.getId\(\)\.toString\(\),\s*"Export",\s*event\.getId\(\),\s*"Calendar event exported "\s*\+\s*format\s*\);', 'activityService.recordActivity("CalendarEvent", event.getId().toString(), "Export", event.getId().toString(), com.zencube.registry.activity.enums.ActivityType.CALENDAR_EVENT_CREATED, "Calendar event exported " + format);'
    
    # 3. CalendarServiceImpl.java - CREATE
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"CALENDAR_EVENT_CREATED",\s*"CalendarEvent",\s*event\.getId\(\)\.toString\(\),\s*event\.getEventableType\(\),\s*event\.getEventableId\(\),\s*"Created calendar event: "\s*\+\s*event\.getTitle\(\)\s*\);', 'activityService.recordActivity("CalendarEvent", event.getId().toString(), event.getEventableType(), event.getEventableId() != null ? event.getEventableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CALENDAR_EVENT_CREATED, "Created calendar event: " + event.getTitle());'

    # 4. CalendarServiceImpl.java - UPDATE
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"CALENDAR_EVENT_UPDATED",\s*"CalendarEvent",\s*event\.getId\(\)\.toString\(\),\s*event\.getEventableType\(\),\s*event\.getEventableId\(\),\s*"Updated calendar event: "\s*\+\s*event\.getTitle\(\)\s*\);', 'activityService.recordActivity("CalendarEvent", event.getId().toString(), event.getEventableType(), event.getEventableId() != null ? event.getEventableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CALENDAR_EVENT_UPDATED, "Updated calendar event: " + event.getTitle());'

    # 5. CalendarServiceImpl.java - DELETE
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"CALENDAR_EVENT_DELETED",\s*"CalendarEvent",\s*event\.getId\(\)\.toString\(\),\s*event\.getEventableType\(\),\s*event\.getEventableId\(\),\s*"Deleted calendar event: "\s*\+\s*event\.getTitle\(\)\s*\);', 'activityService.recordActivity("CalendarEvent", event.getId().toString(), event.getEventableType(), event.getEventableId() != null ? event.getEventableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CALENDAR_EVENT_DELETED, "Deleted calendar event: " + event.getTitle());'

    # 6. ChatServiceImpl.java - Thread Created
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_THREAD_CREATED",\s*"ChatThread",\s*thread\.getId\(\)\.toString\(\),\s*thread\.getContextableType\(\),\s*thread\.getContextableId\(\),\s*"Created chat thread"\s*\);', 'activityService.recordActivity("ChatThread", thread.getId().toString(), thread.getContextableType(), thread.getContextableId() != null ? thread.getContextableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_CREATED, "Created chat thread");'

    # 7. ChatServiceImpl.java - Message Sent
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_MESSAGE_SENT",\s*"ChatMessage",\s*savedMessage\.getId\(\)\.toString\(\),\s*"ChatThread",\s*thread\.getId\(\),\s*"Sent message in thread"\s*\);', 'activityService.recordActivity("ChatMessage", savedMessage.getId().toString(), "ChatThread", thread.getId().toString(), com.zencube.registry.activity.enums.ActivityType.CHAT_MESSAGE_SENT, "Sent message in thread");'
    
    # 8. ChatServiceImpl.java - Thread Archived
    $content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_THREAD_ARCHIVED",\s*"ChatThread",\s*thread\.getId\(\)\.toString\(\),\s*thread\.getContextableType\(\),\s*thread\.getContextableId\(\),\s*"Archived chat thread"\s*\);', 'activityService.recordActivity("ChatThread", thread.getId().toString(), thread.getContextableType(), thread.getContextableId() != null ? thread.getContextableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_ARCHIVED, "Archived chat thread");'

    Set-Content -Path $f -Value $content
}
