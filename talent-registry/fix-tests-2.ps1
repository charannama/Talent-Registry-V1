$testDir = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\test\java"

# 1. NotificationIT.java
$file = "$testDir\com\zencube\registry\notification\integration\NotificationIT.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'countByUserIdAndIsReadFalse', 'countByUserIdAndReadAtIsNull'
Set-Content -Path $file -Value $content

# 2. NotificationEventIT.java
$file = "$testDir\com\zencube\registry\notification\integration\NotificationEventIT.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace '\.changedBy\(', '.triggeredBy('
Set-Content -Path $file -Value $content

# 3. NotificationPreferenceIT.java
$file = "$testDir\com\zencube\registry\notification\integration\NotificationPreferenceIT.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'isEmailEnabled', 'getEmailEnabled'
$content = $content -replace 'request\.setPreferences\(.*\)', 'request.setEmailEnabled(true); request.setInAppEnabled(true); request.setPushEnabled(true);'
Set-Content -Path $file -Value $content

# 4. EmailServiceTest.java
$file = "$testDir\com\zencube\registry\notification\unit\EmailServiceTest.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'emailService\.sendHtmlEmail\((.*?), (.*?), (.*?), (.*?)\)', 'emailService.sendTemplateEmail($1, com.zencube.registry.notification.enums.NotificationEventType.USER_REGISTERED, $4)'
Set-Content -Path $file -Value $content

# 5. NotificationPreferenceServiceTest.java
$file = "$testDir\com\zencube\registry\notification\unit\NotificationPreferenceServiceTest.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'isEmailEnabled', 'getEmailEnabled'
$content = $content -replace 'new UpdateNotificationPreferenceRequest\([^)]+\)', 'com.zencube.registry.notification.dto.UpdateNotificationPreferenceRequest.builder().emailEnabled(true).inAppEnabled(true).pushEnabled(false).build()'
Set-Content -Path $file -Value $content

# 6. NotificationServiceTest.java
$file = "$testDir\com\zencube\registry\notification\unit\NotificationServiceTest.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'countByUserIdAndIsReadFalse', 'countByUserIdAndReadAtIsNull'
$content = $content -replace 'findByIdAndUserId', 'findById'
$content = $content -replace 'findByUserIdAndIsReadFalse', 'findByUserIdAndReadAtIsNullOrderByCreatedAtDesc'
$content = $content -replace 'markAsRead\(userId, notificationId\)', 'markAsRead(notificationId)'
$content = $content -replace 'notification\.getIsRead\(\)', '(notification.getReadAt() != null)'
$content = $content -replace 'any\(Pageable\.class\)', 'anyInt(), anyInt()' # Wait, this might be a problem. Let's just fix PageRequest.
$content = $content -replace 'notificationRepository\.findByUserIdOrderByCreatedAtDesc\(eq\(userId\), any\(Pageable\.class\)\)', 'notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any())'
Set-Content -Path $file -Value $content

