# 1. CalendarServiceImpl.java
$calendarServiceFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\calendar\service\impl\CalendarServiceImpl.java"
$content = Get-Content $calendarServiceFile -Raw
$content = $content -replace 'import com.zencube.registry.calendar.exception.EventNotFoundException;', "import com.zencube.registry.calendar.exception.EventNotFoundException;
import com.zencube.registry.calendar.exception.InvalidCalendarEventException;"
Set-Content -Path $calendarServiceFile -Value $content

# 2. ChatThread.java
$chatThreadFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\chat\entity\ChatThread.java"
$content = Get-Content $chatThreadFile -Raw
$content = $content -replace 'private User createdBy;', 'private User creator;'
Set-Content -Path $chatThreadFile -Value $content

# 3. UserRepository.java
$userRepositoryFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\auth\repository\UserRepository.java"
$content = Get-Content $userRepositoryFile -Raw
$content = $content -replace 'boolean existsByEmail\(String email\);', "Optional<User> findByIdAndDeletedFalse(UUID id);

    boolean existsByEmail(String email);"
Set-Content -Path $userRepositoryFile -Value $content

# 4. ChatServiceImpl.java
$chatServiceFile = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\chat\service\impl\ChatServiceImpl.java"
$content = Get-Content $chatServiceFile -Raw
$content = $content -replace 'import java.util.UUID;', "import java.util.UUID;
import java.util.Optional;"
$content = $content -replace 'application\.getIsDeleted\(\)', 'application.isDeleted()'

$oldNotify = '            CreateNotificationRequest nr = CreateNotificationRequest.builder\(\)
                    \.userId\(cp\.getUser\(\)\.getId\(\)\)
                    \.title\(title\)
                    \.message\(body\)
                    \.type\(NotificationType\.SYSTEM_ALERT\)
                    \.referenceId\(message != null \? message\.getId\(\) : thread\.getId\(\)\)
                    \.referenceType\(message != null \? "ChatMessage" : "ChatThread"\)
                    \.build\(\);
            try \{
                notificationService\.createNotification\(nr\);'

$newNotify = '            try {
                notificationService.createNotification(
                    cp.getUser().getId(),
                    com.zencube.registry.notification.enums.NotificationEventType.SYSTEM_ALERT,
                    message != null ? "ChatMessage" : "ChatThread",
                    message != null ? message.getId() : thread.getId(),
                    title,
                    body
                );'
$content = $content -replace $oldNotify, $newNotify
Set-Content -Path $chatServiceFile -Value $content

