$testDir = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\test\java"

# 1. ChatUnreadIT.java
$file = "$testDir\com\zencube\registry\chat\integration\ChatUnreadIT.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'roleRepository\.findByName\(RoleType\.CANDIDATE\)', 'roleRepository.findByName(RoleType.CANDIDATE).orElseThrow()'
Set-Content -Path $file -Value $content

# 2. ApplicationChatServiceTest.java
$file = "$testDir\com\zencube\registry\chat\unit\ApplicationChatServiceTest.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'application\.setIsDeleted\(', 'application.setDeleted('
Set-Content -Path $file -Value $content

# 3. ChatRepositoryTest.java
$file = "$testDir\com\zencube\registry\chat\unit\ChatRepositoryTest.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'roleRepository\.findByName\(RoleType\.CANDIDATE\)', 'roleRepository.findByName(RoleType.CANDIDATE).orElseThrow()'
$content = $content -replace '\.createdBy\(', '.creator('
Set-Content -Path $file -Value $content

# 4. NotificationAssertions.java
$file = "$testDir\com\zencube\registry\notification\fixtures\NotificationAssertions.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'notification\.getIsRead\(\)', '(notification.getReadAt() != null)'
$content = $content -replace 'isEmailEnabled', 'getEmailEnabled'
$content = $content -replace 'isPushEnabled', 'getPushEnabled'
Set-Content -Path $file -Value $content

# 5. NotificationTestDataFactory.java
$file = "$testDir\com\zencube\registry\notification\fixtures\NotificationTestDataFactory.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace '\.referenceType\(', '.resourceType('
$content = $content -replace 'settings\.setId\(', '// settings.setId('
$content = $content -replace '\.changedBy\(', '.actorId('
Set-Content -Path $file -Value $content

# 6. NotificationEventIT.java
$file = "$testDir\com\zencube\registry\notification\integration\NotificationEventIT.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'countByUserIdAndIsReadFalse', 'countByUserIdAndReadAtIsNull'
$content = $content -replace '\.triggeredBy\(', '.actorId('
Set-Content -Path $file -Value $content

# 7. NotificationPreferenceServiceTest.java
$file = "$testDir\com\zencube\registry\notification\unit\NotificationPreferenceServiceTest.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'request\.setPreferences\(.*\)', 'request.setEmailEnabled(true); request.setInAppEnabled(true); request.setPushEnabled(true);'
Set-Content -Path $file -Value $content

# 8. NotificationServiceTest.java
$file = "$testDir\com\zencube\registry\notification\unit\NotificationServiceTest.java"
$content = Get-Content -Path $file -Raw
$content = $content -replace 'findById\(notificationId, userId\)', 'findById(notificationId)'
$content = $content -replace 'markAsRead\(userId, notificationId\)', 'markAsRead(notificationId)'
$content = $content -replace 'findByUserIdAndReadAtIsNullOrderByCreatedAtDesc\(userId\)', 'findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(eq(userId), any())'
$content = $content -replace 'any\(Pageable\.class\)', 'any()'
Set-Content -Path $file -Value $content

