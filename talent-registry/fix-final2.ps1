# 1. Add CHAT_MESSAGE_RECEIVED to NotificationEventType
$enumFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\notification\enums\NotificationEventType.java"
$content = Get-Content $enumFile -Raw
$content = $content -replace 'INTERVIEW_REMINDER', "INTERVIEW_REMINDER,
    CHAT_MESSAGE_RECEIVED"
Set-Content -Path $enumFile -Value $content

# 2. Fix ChatServiceImpl.java
$chatServiceFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\chat\service\impl\ChatServiceImpl.java"
$content = Get-Content $chatServiceFile -Raw
$content = $content -replace '\.createdBy\(', '.creator('
$content = $content -replace 'NotificationEventType\.SYSTEM_ALERT', 'com.zencube.registry.notification.enums.NotificationEventType.CHAT_MESSAGE_RECEIVED'

# Fix the missed recordActivity calls in ChatServiceImpl
$content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_THREAD_ARCHIVED",\s*"ChatThread",\s*thread\.getId\(\)\.toString\(\),\s*thread\.getContextableType\(\),\s*thread\.getContextableId\(\),\s*"Archived chat thread"\s*\);', 'activityService.recordActivity("ChatThread", thread.getId().toString(), thread.getContextableType(), thread.getContextableId() != null ? thread.getContextableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_ARCHIVED, "Archived chat thread");'

$content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_MESSAGE_SENT",\s*"ChatMessage",\s*savedMessage\.getId\(\)\.toString\(\),\s*"ChatThread",\s*thread\.getId\(\),\s*"Sent message in thread"\s*\);', 'activityService.recordActivity("ChatMessage", savedMessage.getId().toString(), "ChatThread", thread.getId().toString(), com.zencube.registry.activity.enums.ActivityType.CHAT_MESSAGE_SENT, "Sent message in thread");'

$content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_THREAD_CREATED",\s*"ChatThread",\s*thread\.getId\(\)\.toString\(\),\s*thread\.getContextableType\(\),\s*thread\.getContextableId\(\),\s*"Created chat thread"\s*\);', 'activityService.recordActivity("ChatThread", thread.getId().toString(), thread.getContextableType(), thread.getContextableId() != null ? thread.getContextableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_CREATED, "Created chat thread");'

Set-Content -Path $chatServiceFile -Value $content

# 3. Fix CalendarExportServiceImpl.java
$exportFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\calendar\service\impl\CalendarExportServiceImpl.java"
$content = Get-Content $exportFile -Raw
$content = $content -replace '(?s)activityService\.recordActivity\(\s*"CALENDAR_EXPORTED",\s*"CalendarEvent",\s*event\.getId\(\)\.toString\(\),\s*"Export",\s*event\.getId\(\),\s*"Calendar event exported "\s*\+\s*format\s*\);', 'activityService.recordActivity("CalendarEvent", event.getId().toString(), "Export", event.getId().toString(), com.zencube.registry.activity.enums.ActivityType.CALENDAR_EVENT_CREATED, "Calendar event exported " + format);'
Set-Content -Path $exportFile -Value $content

# 4. Fix ChatMapper.java
$chatMapperFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\chat\mapper\ChatMapper.java"
$content = Get-Content $chatMapperFile -Raw
$content = $content -replace 'thread\.getCreatedBy\(\)', 'thread.getCreator() != null ? thread.getCreator().getId().toString() : null'
Set-Content -Path $chatMapperFile -Value $content

