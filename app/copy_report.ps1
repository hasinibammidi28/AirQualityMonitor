$adb = "C:\Users\Bammidi Hasini\AppData\Local\Android\Sdk\platform-tools\adb.exe"

$projectFolder = Split-Path $PSScriptRoot -Parent
$destination = Join-Path $projectFolder "air_quality_report.txt"

Write-Host "Air Quality Monitor Report Copier Started!"
Write-Host "Saving report to: $destination"

while ($true) {
    try {
        $report = & $adb exec-out run-as com.example.airqualitymonitor cat files/air_quality_report.txt 2>$null
        $exitCode = $LASTEXITCODE

        if ($exitCode -eq 0 -and $report) {
            $report | Set-Content -Path.\app\copy_report.ps1 $destination -Encoding utf8
            Write-Host "Report updated: $(Get-Date -Format 'HH:mm:ss')"
        }
        else {
            Write-Host "Waiting for a new report..."
        }
    }
    catch {
        Write-Host "Error: $_"
    }

    Start-Sleep -Seconds 2
}