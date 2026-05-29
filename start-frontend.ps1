param(
    [int]$MaxWaitSeconds = 60,
    [int]$HealthCheckIntervalSeconds = 2
)

$ErrorActionPreference = "Stop"
$frontendDir = "F:\Workspace\VibeCoding\personal-painpoints\Base\frontend"
$port = 5173
$healthUrl = "http://localhost:$port"
$stdoutLog = "$env:TEMP\vite_frontend_stdout.log"
$stderrLog = "$env:TEMP\vite_frontend_stderr.log"

# Error patterns that indicate Vite startup failure
$errorPatterns = @(
    "Error:",
    "ERR_",
    "failed to fetch",
    "cannot find module",
    "Module not found",
    "SyntaxError:",
    "Vite crashed",
    "EADDRINUSE",
    "Port already in use",
    "Access denied",
    "ENOENT"
)

Write-Host "=========================================="
Write-Host "Vue Frontend Startup Script"
Write-Host "Health Check + Auto Exit on Failure"
Write-Host "=========================================="
Write-Host ""

# Cleanup old logs
Remove-Item $stdoutLog -Force -ErrorAction SilentlyContinue
Remove-Item $stderrLog -Force -ErrorAction SilentlyContinue

Write-Host "[1/5] Checking for existing processes on port $port..."
try {
    $existingConn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($existingConn) {
        Write-Host "    Found process, terminating..."
        $existingConn | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
        Start-Sleep -Seconds 2
    } else {
        Write-Host "    No existing process"
    }
} catch {
    $errMsg = $_.Exception.Message
    Write-Host "    Could not check port ${port}: ${errMsg}"
}

Write-Host "[2/5] Starting Vite dev server..."
$startTime = Get-Date

# Start vite - cmd.exe /c runs npm and exits immediately
# npm runs in background as an independent process
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$frontendDir`" && npm run dev >> `"$stdoutLog`" 2>&1" -WindowStyle Hidden -PassThru
Write-Host "    Process started"

Write-Host "[3/5] Polling health endpoint..."
Write-Host "    URL: $healthUrl"
Write-Host "    Max wait: $MaxWaitSeconds seconds"
Write-Host ""

$ready = $false
$elapsedSeconds = 0
$fatalError = ""
$logErrorDetected = $false

# Wait for initial startup
Start-Sleep -Seconds 5

while ($elapsedSeconds -lt $MaxWaitSeconds) {
    Start-Sleep -Seconds $HealthCheckIntervalSeconds
    $elapsedSeconds += $HealthCheckIntervalSeconds

    # Check log file for error patterns (early detection)
    if (Test-Path $stdoutLog) {
        $logContent = Get-Content $stdoutLog -ErrorAction SilentlyContinue
        foreach ($line in $logContent) {
            foreach ($pattern in $errorPatterns) {
                if ($line -match $pattern) {
                    Write-Host ""
                    Write-Host "    [$elapsedSeconds sec] Detected error pattern: $pattern"
                    $fatalError = "$pattern found in log: $line"
                    $logErrorDetected = $true
                    break
                }
            }
            if ($logErrorDetected) { break }
        }
    }

    if ($logErrorDetected) { break }

    # Check if port is now in use (vite started)
    $portCheck = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue

    try {
        $response = Invoke-WebRequest -Uri $healthUrl -Method GET -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $ready = $true
            Write-Host ""
            Write-Host "[SUCCESS] Frontend is ready!"
            break
        }
    } catch {
        # Only show message every 10 seconds to reduce noise
        if ($elapsedSeconds % 10 -eq 0 -or $elapsedSeconds -lt 10) {
            Write-Host "    [$elapsedSeconds sec] Not ready yet..."
        }
    }
}

if (-not $ready) {
    Write-Host ""
    Write-Host "[FAILURE] Frontend did not become ready"

    if ($logErrorDetected) {
        Write-Host "    Error detected in startup log"
    }

    # Show last 30 lines of log BEFORE cleanup
    if (Test-Path $stdoutLog) {
        Write-Host ""
        Write-Host "[LOG] Last 30 lines of stdout:"
        Get-Content $stdoutLog | Select-Object -Last 30
    }

    if (Test-Path $stderrLog) {
        Write-Host ""
        Write-Host "[LOG] Last 30 lines of stderr:"
        Get-Content $stderrLog | Select-Object -Last 30
    }

    # Kill port occupants
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        foreach ($conn in $conns) {
            Write-Host "    [KILL] Port $port process: $($conn.OwningProcess)"
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    } catch {}

    Write-Host ""
    if ($fatalError) {
        Write-Host "Error: $fatalError"
    } else {
        Write-Host "Error: Frontend failed to start within $MaxWaitSeconds seconds"
    }
    Write-Host ""
    Write-Host "Please fix the issue and retry."

    exit 1
}

$totalTime = (Get-Date) - $startTime
Write-Host ""
Write-Host "[4/5] Verifying page loads..."
try {
    $response = Invoke-WebRequest -Uri $healthUrl -Method GET -TimeoutSec 10 -UseBasicParsing
    Write-Host "    Page Status: $($response.StatusCode)"
} catch {
    Write-Host "    [WARNING] Page check failed: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[5/5] Startup completed in $($totalTime.TotalSeconds) seconds"
Write-Host "=========================================="
Write-Host "Frontend running at: $healthUrl"
Write-Host "=========================================="
Write-Host ""
Write-Host "Server is running in background"
Write-Host "Close this window to exit"
Write-Host ""

# Cleanup temp logs
Remove-Item $stdoutLog -Force -ErrorAction SilentlyContinue
Remove-Item $stderrLog -Force -ErrorAction SilentlyContinue

# Exit immediately - server continues running independently
exit 0