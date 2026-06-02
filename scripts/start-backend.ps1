param(
    [int]$MaxWaitSeconds = 50,
    [int]$HealthCheckIntervalSeconds = 2
)

$ErrorActionPreference = "Stop"

$backendDir = "F:\Workspace\VibeCoding\personal-painpoints\Base\backend"
$logFile = "$env:TEMP\backend_startup_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"
$env:JAVA_HOME = "D:\CodeProgram\jdk-21.0.1"
$env:PATH = "$env:JAVA_HOME\bin;C:\tools\apache-maven-3.9.6\bin;$env:PATH"

# Error patterns that indicate Java startup failure (but process doesn't exit)
$errorPatterns = @(
    "Exception in thread",
    "ERROR.*Spring",
    "ERROR.*Application",
    "BeanCreationException",
    "Port already in use",
    "Address already in use",
    "Connection refused",
    "Access denied",
    "No such file or directory",
    "Could not resolve",
    "Build failure",
    "Process terminated with exit code"
)

Write-Host "=========================================="
Write-Host "Java Backend Startup Script"
Write-Host "Timeout + Force Kill + Error Pattern Detection"
Write-Host "=========================================="
Write-Host ""

Write-Host "[1/6] Checking for existing processes on port 8080..."

try {
    $existingConn = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
    if ($existingConn) {
        Write-Host "    Found process, terminating..."
        $existingConn | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
        Start-Sleep -Seconds 2
    } else {
        Write-Host "    No existing process"
    }
} catch {
    Write-Host "    Could not check port 8080: $_"
}

Write-Host "[2/6] Starting Spring Boot backend..."
$startTime = Get-Date

# Start Maven - cmd.exe /c runs the command and exits immediately
# Maven runs in background as an independent process
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$backendDir`" && mvn spring-boot:run -Dspring-boot.run.profiles=dev >> `"$logFile`" 2>&1" -WindowStyle Hidden -PassThru

Write-Host "[3/6] Monitoring log file for error patterns..."
$startedAt = Get-Date

Write-Host "[4/6] Polling health endpoint (every $HealthCheckIntervalSeconds seconds)..."
# Note: context path is /api, so full path is /api/actuator/health
$healthEndpoint = "http://localhost:8080/api/actuator/health"
Write-Host "    URL: $healthEndpoint"
Write-Host "    Max wait: $MaxWaitSeconds seconds"
Write-Host ""

$ready = $false
$elapsedSeconds = 0
$lastError = ""
$logErrorDetected = $false

# Wait for initial startup
Start-Sleep -Seconds 5

while ($elapsedSeconds -lt $MaxWaitSeconds) {
    Start-Sleep -Seconds $HealthCheckIntervalSeconds
    $elapsedSeconds += $HealthCheckIntervalSeconds

    # Check if the cmd process exited (which is expected - maven runs as child)
    # Instead, check if port 8080 is now in use (maven spawned java)
    $portCheck = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

    # Check log file for error patterns (early detection)
    if (Test-Path $logFile) {
        $logContent = Get-Content $logFile -ErrorAction SilentlyContinue
        foreach ($line in $logContent) {
            foreach ($pattern in $errorPatterns) {
                if ($line -match $pattern) {
                    Write-Host "    [$elapsedSeconds sec] Detected error pattern: $pattern"
                    $logErrorDetected = $true
                    break
                }
            }
            if ($logErrorDetected) { break }
        }
    }

    if ($logErrorDetected) { break }

    # Try health endpoint
    try {
        $response = Invoke-RestMethod -Uri $healthEndpoint -Method GET -TimeoutSec 5 -ErrorAction Stop
        $status = $response.status
        Write-Host "    [$elapsedSeconds sec] Status: $status"

        if ($status -eq "UP") {
            $ready = $true
            Write-Host ""
            Write-Host "[SUCCESS] Backend is UP and ready!"
            break
        } else {
            $lastError = "Status: $status"
        }
    } catch {
        $lastError = $_.Exception.Message
        # Only show message every 10 seconds to reduce noise
        if ($elapsedSeconds % 10 -eq 0 -or $elapsedSeconds -lt 10) {
            Write-Host "    [$elapsedSeconds sec] Not ready yet..."
        }
    }
}

if (-not $ready) {
    Write-Host ""
    Write-Host "[INTERVENTION] Shutting down..."

    if ($logErrorDetected) {
        Write-Host "    Error pattern detected in log"
    } else {
        Write-Host "[TIMEOUT] Backend did not become ready within $MaxWaitSeconds seconds"
        Write-Host "Last error: $lastError"
    }

    Write-Host ""
    Write-Host "=========================================="
    Write-Host "[INTERVENTION] Force killing all related processes"
    Write-Host "=========================================="

    # Kill all processes using port 8080
    try {
        $conns = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
        foreach ($conn in $conns) {
            Write-Host "    [KILL] Port 8080 process: $($conn.OwningProcess)"
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    } catch {}

    # Kill processes spawned from this maven invocation (by command line matching)
    $currentJavaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
    if ($currentJavaProcs) {
        foreach ($proc in $currentJavaProcs) {
            try {
                $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($proc.Id)").CommandLine
                if ($cmdLine -match "spring-boot|maven|java.*personal-website|personal-website.*jar") {
                    Write-Host "    [KILL] Java process $($proc.Id)"
                    Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
                }
            } catch {}
        }
    }

    Write-Host ""
    Write-Host "[LOG] Last 30 lines of log:"
    if (Test-Path $logFile) {
        Get-Content $logFile | Select-Object -Last 30
    }

    Write-Host ""
    Write-Host "[ERRORS DETECTED] Problem analysis:"
    if (Test-Path $logFile) {
        foreach ($pattern in $errorPatterns) {
            $matches = Select-String -Path $logFile -Pattern $pattern -ErrorAction SilentlyContinue
            if ($matches) {
                Write-Host "    - Found: $pattern"
                $matches | Select-Object -First 3 | ForEach-Object { Write-Host "      $_" }
            }
        }
    }

    Write-Host ""
    Write-Host "[ACTION] All related processes terminated."
    Write-Host "Please fix the issue and retry."
    exit 1
}

Write-Host "[5/6] Verifying API endpoint..."
try {
    $apiResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 10 -ErrorAction Stop
    Write-Host "    API Response: SUCCESS"
} catch {
    Write-Host "    [WARNING] API test failed: $($_.Exception.Message)"
}

$totalTime = (Get-Date) - $startTime
Write-Host ""
Write-Host "[6/6] Startup completed in $($totalTime.TotalSeconds) seconds"
Write-Host "=========================================="
Write-Host "Backend running at: http://localhost:8080"
Write-Host "Health endpoint: http://localhost:8080/api/actuator/health"
Write-Host "API base: http://localhost:8080/api"
Write-Host "=========================================="
Write-Host ""
Write-Host "Server is running in background"
Write-Host "Close this window to exit"
Write-Host ""

# Exit immediately - server continues running independently
exit 0