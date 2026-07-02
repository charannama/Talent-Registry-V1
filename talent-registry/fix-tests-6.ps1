$testDir = "C:\Users\layar\Downloads\talent-registry\talent-registry\src\test\java"

# Fix JwtService.createToken -> generateAccessToken
Get-ChildItem -Path $testDir -Recurse -Filter *.java | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw
    $newContent = $content -replace '\.createToken\(', '.generateAccessToken('
    if ($newContent -cne $content) { Set-Content -Path $_.FullName -Value $newContent }
}

# Fix ApplicationChatIT: replace STUDENT (but it uses HR_STAFF for enterprise context)
$file = "$testDir\com\zencube\registry\chat\integration\ApplicationChatIT.java"
if (Test-Path $file) {
    $content = Get-Content -Path $file -Raw
    # Check context - use HR_STAFF for enterprise-side chat
    $content = $content -replace 'RoleType\.STUDENT(?=.*enterprise)', 'RoleType.HR_STAFF'
    Set-Content -Path $file -Value $content
}

# The view showed ApplicationChatIT line 81 was the ENTERPRISE -> EMPLOYER fix. Now EMPLOYER -> let's use HR_STAFF
Get-ChildItem -Path $testDir -Recurse -Filter *.java | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw
    $newContent = $content -replace 'RoleType\.EMPLOYER', 'RoleType.HR_STAFF'
    if ($newContent -cne $content) { Set-Content -Path $_.FullName -Value $newContent }
}

