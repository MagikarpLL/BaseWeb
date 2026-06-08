param(
    [int]$TimeoutSeconds = 180,
    [switch]$SkipDockerBuild
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$logsDir = Join-Path $repoRoot ".harness\runs\local-integration-$runId\logs"
$reportPath = Join-Path $repoRoot ".harness\reports\integration-report.yaml"
$servicesStarted = $false
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path $reportPath) | Out-Null

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message"
}

function Write-Report {
    param([string]$Status, [string]$Summary)
    $timestamp = Get-Date -Format "yyyy-MM-ddTHH:mm:ssK"
    $safeSummary = $Summary.Replace('"', "'")
    $content = @"
version: 1
stage: integration
status: $Status
updated_at: "$timestamp"
summary: "$safeSummary"
logs_dir: ".harness/runs/local-integration-$runId/logs"
checks:
  - name: docker_compose
    required: true
  - name: backend_health
    required: true
  - name: nginx_frontend
    required: true
  - name: nginx_api_proxy
    required: true
"@
    Set-Content -LiteralPath $reportPath -Value $content -Encoding UTF8
}

function Invoke-Logged {
    param([string]$Name, [string]$Command)
    $logPath = Join-Path $logsDir "$Name.log"
    Write-Step "${Name}: $Command"
    Push-Location $repoRoot
    try {
        cmd.exe /c "$Command 2>&1" | Tee-Object -FilePath $logPath
        if ($LASTEXITCODE -ne 0) {
            throw "$Name failed with exit code $LASTEXITCODE. Log: $logPath"
        }
    }
    finally {
        Pop-Location
    }
}

function Assert-PortAvailableOrOwnedByDocker {
    param([int]$Port)

    $listeners = netstat -ano | Select-String ":$Port\s+.*LISTENING"
    if (-not $listeners) {
        return
    }

    $dockerPorts = docker ps --format "{{.Ports}}"
    $ownedByDocker = $dockerPorts | Select-String "0\.0\.0\.0:$Port->|\[::\]:$Port->"
    if ($ownedByDocker) {
        return
    }

    $pids = @()
    foreach ($line in $listeners) {
        $parts = ($line.ToString() -split '\s+') | Where-Object { $_ }
        if ($parts.Length -gt 0) {
            $pids += $parts[-1]
        }
    }
    $pids = $pids | Sort-Object -Unique
    $processes = foreach ($listenerPid in $pids) {
        Get-Process -Id ([int]$listenerPid) -ErrorAction SilentlyContinue |
            ForEach-Object { "$($_.ProcessName)($($_.Id))" }
    }

    throw "Port $Port is already used by non-Docker process(es): $($processes -join ', '). Stop them or change docker-compose port mappings before running integration."
}

function Wait-HttpOk {
    param([string]$Name, [string]$Url)
    Write-Step "Waiting for $Name at $Url"
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 10
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                Write-Host "$Name ready: HTTP $($response.StatusCode)"
                return
            }
            $lastError = "HTTP $($response.StatusCode)"
        }
        catch {
            $lastError = $_.Exception.Message
        }
        Start-Sleep -Seconds 5
    }

    throw "Timed out waiting for $Name at $Url. Last error: $lastError"
}

function Save-DockerLogs {
    Write-Step "Collecting docker compose logs"
    Push-Location $repoRoot
    try {
        docker compose ps *>&1 | Tee-Object -FilePath (Join-Path $logsDir "docker-ps.log")
        docker compose logs --no-color *>&1 | Tee-Object -FilePath (Join-Path $logsDir "docker-compose.log")
    }
    catch {
        Write-Warning "Could not collect docker compose logs: $($_.Exception.Message)"
    }
    finally {
        Pop-Location
    }
}

try {
    Assert-PortAvailableOrOwnedByDocker -Port 80
    Assert-PortAvailableOrOwnedByDocker -Port 8080

    if ($SkipDockerBuild) {
        Invoke-Logged -Name "docker-up" -Command "docker compose up -d"
    }
    else {
        Invoke-Logged -Name "docker-up" -Command "docker compose up -d --build"
    }
    $servicesStarted = $true

    Wait-HttpOk -Name "backend health" -Url "http://localhost:8080/api/actuator/health"
    Wait-HttpOk -Name "nginx frontend" -Url "http://localhost/"
    Wait-HttpOk -Name "nginx API proxy" -Url "http://localhost/api/public/home"

    Write-Report -Status "passed" -Summary "Integration verification passed through docker-compose, backend health, nginx frontend, and nginx API proxy."
    Write-Step "Integration verification passed"
}
catch {
    $message = ($_ | Out-String).Trim()
    Write-Report -Status "failed" -Summary $message
    Write-Error $_
    Save-DockerLogs
    exit 1
}
finally {
    if ($servicesStarted) {
        Write-Step "Services remain running for e2e or inspection. Stop them with: docker compose down"
    }
}
