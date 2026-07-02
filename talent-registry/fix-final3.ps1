$f = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\chat\service\impl\ChatServiceImpl.java"
$content = Get-Content $f -Raw

$content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_THREAD_CREATED",\s*"ChatThread",\s*thread\.getId\(\)\.toString\(\),\s*request\.getContextableType\(\),\s*request\.getContextableId\(\),\s*"Created new chat thread"\s*\);', 'activityService.recordActivity("ChatThread", thread.getId().toString(), request.getContextableType(), request.getContextableId() != null ? request.getContextableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_CREATED, "Created new chat thread");'

$content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_THREAD_CREATED",\s*"ChatThread",\s*thread\.getId\(\)\.toString\(\),\s*"Application",\s*applicationId,\s*"Created new application chat thread"\s*\);', 'activityService.recordActivity("ChatThread", thread.getId().toString(), "Application", applicationId != null ? applicationId.toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_THREAD_CREATED, "Created new application chat thread");'

$content = $content -replace '(?s)activityService\.recordActivity\(\s*"CHAT_MESSAGE_SENT",\s*"ChatThread",\s*thread\.getId\(\)\.toString\(\),\s*thread\.getContextableType\(\),\s*thread\.getContextableId\(\),\s*"Sent a message"\s*\);', 'activityService.recordActivity("ChatThread", thread.getId().toString(), thread.getContextableType(), thread.getContextableId() != null ? thread.getContextableId().toString() : null, com.zencube.registry.activity.enums.ActivityType.CHAT_MESSAGE_SENT, "Sent a message");'

Set-Content -Path $f -Value $content
