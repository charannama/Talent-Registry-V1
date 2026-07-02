$testDir = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\test\java"
$files = Get-ChildItem -Path $testDir -Recurse -Filter *.java

foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    $original = $content
    
    # Imports
    $content = $content -replace 'import com.zencube.registry.security.jwt.JwtService;', 'import com.zencube.registry.security.service.JwtService;'
    $content = $content -replace 'import com.zencube.registry.security.jwt.JwtAuthenticationFilter;', 'import com.zencube.registry.security.filter.JwtAuthenticationFilter;'
    $content = $content -replace 'import com.zencube.registry.calendar.enums.ResponseStatus;', 'import com.zencube.registry.calendar.enums.ParticipantResponseStatus;'
    $content = $content -replace 'import com.zencube.registry.application.enums.ApplicationStatus;', 'import com.zencube.registry.common.enums.ApplicationStatus;'
    $content = $content -replace 'import com.zencube.registry.notification.dto.request.NotificationSettingsRequest;', 'import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;'
    $content = $content -replace 'import com.zencube.registry.notification.dto.NotificationSettingsResponse;', 'import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;'
    $content = $content -replace 'import com.zencube.registry.notification.dto.NotificationPageResponse;', 'import com.zencube.registry.notification.dto.response.NotificationPageResponse;'
    $content = $content -replace 'import com.zencube.registry.notification.dto.NotificationResponse;', 'import com.zencube.registry.notification.dto.response.NotificationResponse;'
    $content = $content -replace 'import com.zencube.registry.email.service.impl.EmailServiceImpl;', 'import com.zencube.registry.notification.email.impl.EmailServiceImpl;'
    $content = $content -replace 'import com.zencube.registry.notification.dto.request.NotificationPreferenceRequest;', 'import com.zencube.registry.notification.dto.request.UpdateNotificationPreferenceRequest;'
    $content = $content -replace 'import com.zencube.registry.notification.dto.UpdateNotificationSettingsRequest;', 'import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;'
    
    # Usages
    $content = $content -replace '\bResponseStatus\.', 'ParticipantResponseStatus.'
    $content = $content -replace '\bResponseStatus\b', 'ParticipantResponseStatus'
    $content = $content -replace '\bNotificationSettingsRequest\b', 'UpdateNotificationSettingsRequest'
    $content = $content -replace '\bNotificationPreferenceRequest\b', 'UpdateNotificationPreferenceRequest'
    
    if ($content -cne $original) {
        Set-Content -Path $file.FullName -Value $content
    }
}
