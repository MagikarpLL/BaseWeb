param()

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$logsDir = Join-Path $repoRoot ".harness\runs\local-e2e-$runId\logs"
$reportPath = Join-Path $repoRoot ".harness\reports\e2e-report.yaml"
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
stage: e2e
status: $Status
updated_at: "$timestamp"
summary: "$safeSummary"
logs_dir: ".harness/runs/local-e2e-$runId/logs"
"@
    Set-Content -LiteralPath $reportPath -Value $content -Encoding UTF8
}

try {
    Write-Step "Playwright e2e through nginx and backend API"
    Push-Location (Join-Path $repoRoot "frontend")
    try {
        $env:BASE_URL = "http://localhost"
        $env:API_BASE = "http://localhost/api"
        npx playwright test --config=playwright.harness.config.ts --project=chromium *>&1 |
            Tee-Object -FilePath (Join-Path $logsDir "frontend-e2e.log")
        if ($LASTEXITCODE -ne 0) {
            throw "frontend-e2e failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }

    Write-Report -Status "passed" -Summary "Playwright e2e verification passed through nginx and backend API."
    Write-Step "E2E verification passed"
}
catch {
    $message = ($_ | Out-String).Trim()
    Write-Report -Status "failed" -Summary $message
    Write-Error $_
    exit 1
}
