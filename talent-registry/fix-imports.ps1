$files = Get-ChildItem -Path "C:\Users\layar\Downloads\talent-registry\talent-registry\src\main\java\com\zencube\registry\notification" -Recurse -Filter *.java
foreach ($f in $files) {
    $content = Get-Content -Path $f.FullName
    $modified = $false
    $newContent = @()
    
    foreach ($line in $content) {
        $orig = $line
        
        if ($line -match "^import com\.zencube\.registry\.notification\.dto\.\*;") {
            $newContent += $line
            $newContent += "import com.zencube.registry.notification.dto.response.*;"
            $newContent += "import com.zencube.registry.notification.dto.request.*;"
            $modified = $true
            continue
        }
        
        $line = $line -replace "import com\.zencube\.registry\.notification\.dto\.NotificationResponse;", "import com.zencube.registry.notification.dto.response.NotificationResponse;"
        $line = $line -replace "import com\.zencube\.registry\.notification\.dto\.NotificationSettingsResponse;", "import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;"
        $line = $line -replace "import com\.zencube\.registry\.notification\.dto\.UpdateNotificationSettingsRequest;", "import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;"
        $line = $line -replace "import com\.zencube\.registry\.notification\.dto\.NotificationPageResponse;", "import com.zencube.registry.notification.dto.response.NotificationPageResponse;"
        $line = $line -replace "import com\.zencube\.registry\.notification\.dto\.UnreadNotificationCountResponse;", "import com.zencube.registry.notification.dto.response.UnreadCountResponse;"
        $line = $line -replace "import com\.zencube\.registry\.notification\.dto\.UnreadCountResponse;", "import com.zencube.registry.notification.dto.response.UnreadCountResponse;"
        
        $line = $line -replace "UnreadNotificationCountResponse", "UnreadCountResponse"
        
        $newContent += $line
        if ($orig -ne $line) {
            $modified = $true
        }
    }
    
    if ($modified) {
        Set-Content -Path $f.FullName -Value $newContent
    }
}
