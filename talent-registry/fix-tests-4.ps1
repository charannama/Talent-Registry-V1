$testDir = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\test\java"

# Fix Optional<Role>
Get-ChildItem -Path $testDir -Recurse -Filter *.java | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw
    $newContent = $content -replace '(?<!orElseThrow\(\))roleRepository\.findByName\(([^)]+)\)(?!\.orElseThrow\(\))', 'roleRepository.findByName($1).orElseThrow()'
    if ($newContent -cne $content) { Set-Content -Path $_.FullName -Value $newContent }
}

# Fix ParticipantResponseStatus
Get-ChildItem -Path $testDir -Recurse -Filter *.java | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw
    $newContent = $content -replace '\.ParticipantResponseStatus\(', '.responseStatus('
    if ($newContent -cne $content) { Set-Content -Path $_.FullName -Value $newContent }
}

# Fix ApplicationChatIT ENTERPRISE
$file = "$testDir\com\zencube\registry\chat\integration\ApplicationChatIT.java"
if (Test-Path $file) {
    $content = Get-Content -Path $file -Raw
    $content = $content -replace 'RoleType\.ENTERPRISE', 'RoleType.EMPLOYER'
    Set-Content -Path $file -Value $content
}

# Fix NotificationTestDataFactory
$file = "$testDir\com\zencube\registry\notification\fixtures\NotificationTestDataFactory.java"
if (Test-Path $file) {
    $content = Get-Content -Path $file -Raw
    $content = $content -replace '\.referenceId\(', '.resourceId('
    Set-Content -Path $file -Value $content
}

# Fix NotificationPreferenceServiceTest
$file = "$testDir\com\zencube\registry\notification\unit\NotificationPreferenceServiceTest.java"
if (Test-Path $file) {
    $content = Get-Content -Path $file -Raw
    $content = $content -replace 'request\.setPreferences\(.*\);', 'request.setEmailEnabled(true); request.setInAppEnabled(true); request.setPushEnabled(true);'
    Set-Content -Path $file -Value $content
}

# Fix NotificationServiceTest
$file = "$testDir\com\zencube\registry\notification\unit\NotificationServiceTest.java"
if (Test-Path $file) {
    $content = Get-Content -Path $file -Raw
    $content = $content -replace 'findById\(notificationId, userId\)', 'findById(notificationId)'
    $content = $content -replace 'markAsRead\(userId, notificationId\)', 'markAsRead(notificationId)'
    $content = $content -replace 'thenReturn\(notifications\)', 'thenReturn(new org.springframework.data.domain.PageImpl<>(notifications))'
    $content = $content -replace 'anyInt\(\), anyInt\(\)', 'any(org.springframework.data.domain.Pageable.class)'
    $content = $content -replace 'any\(\)', 'any(org.springframework.data.domain.Pageable.class)'
    Set-Content -Path $file -Value $content
}

