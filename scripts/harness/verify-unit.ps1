param(
    [switch]$FrontendOnly,
    [switch]$BackendOnly
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$logsDir = Join-Path $repoRoot ".harness\runs\local-unit-$runId\logs"
$reportPath = Join-Path $repoRoot ".harness\reports\unit-report.yaml"
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
stage: unit
status: $Status
updated_at: "$timestamp"
summary: "$safeSummary"
logs_dir: ".harness/runs/local-unit-$runId/logs"
"@
    Set-Content -LiteralPath $reportPath -Value $content -Encoding UTF8
}

function Invoke-Logged {
    param(
        [string]$Name,
        [string]$Command,
        [string]$WorkingDirectory
    )

    $logPath = Join-Path $logsDir "$Name.log"
    Write-Step "${Name}: $Command"
    Push-Location $WorkingDirectory
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

try {
    if (-not $FrontendOnly) {
        Invoke-Logged -Name "backend-unit" -Command "mvn test" -WorkingDirectory (Join-Path $repoRoot "backend")
    }

    if (-not $BackendOnly) {
        Invoke-Logged -Name "frontend-unit" -Command "npm run test:run" -WorkingDirectory (Join-Path $repoRoot "frontend")
    }

    Write-Report -Status "passed" -Summary "Unit verification passed."
    Write-Step "Unit verification passed"
}
catch {
    $message = ($_ | Out-String).Trim()
    Write-Report -Status "failed" -Summary $message
    Write-Error $_
    exit 1
}
