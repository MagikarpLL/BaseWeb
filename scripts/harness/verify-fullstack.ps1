param(
    [int]$TimeoutSeconds = 180,
    [switch]$SkipDockerBuild
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$reportPath = Join-Path $repoRoot ".harness\reports\verification-report.yaml"
New-Item -ItemType Directory -Force -Path (Split-Path $reportPath) | Out-Null

function Write-Stage {
    param([string]$Name)
    Write-Host ""
    Write-Host "========== Harness verify: $Name =========="
}

function Write-Report {
    param([string]$Status, [string]$Summary)
    $timestamp = Get-Date -Format "yyyy-MM-ddTHH:mm:ssK"
    $safeSummary = $Summary.Replace('"', "'")
    $content = @"
version: 1
status: $Status
adapter: staged-fullstack
updated_at: "$timestamp"
summary: "$safeSummary"
stage_reports:
  unit: ".harness/reports/unit-report.yaml"
  integration: ".harness/reports/integration-report.yaml"
  e2e: ".harness/reports/e2e-report.yaml"
"@
    Set-Content -LiteralPath $reportPath -Value $content -Encoding UTF8
}

try {
    Write-Stage "unit"
    & (Join-Path $PSScriptRoot "verify-unit.ps1")
    if ($LASTEXITCODE -ne 0) {
        throw "Unit stage failed"
    }

    Write-Stage "integration"
    if ($SkipDockerBuild) {
        & (Join-Path $PSScriptRoot "verify-integration.ps1") -TimeoutSeconds $TimeoutSeconds -SkipDockerBuild
    }
    else {
        & (Join-Path $PSScriptRoot "verify-integration.ps1") -TimeoutSeconds $TimeoutSeconds
    }
    if ($LASTEXITCODE -ne 0) {
        throw "Integration stage failed"
    }

    Write-Stage "e2e"
    & (Join-Path $PSScriptRoot "verify-e2e.ps1")
    if ($LASTEXITCODE -ne 0) {
        throw "E2E stage failed"
    }

    Write-Report -Status "passed" -Summary "All required Harness verification stages passed."
    Write-Host ""
    Write-Host "Harness staged verification passed. Services remain running for inspection; stop with: docker compose down"
}
catch {
    $message = ($_ | Out-String).Trim()
    Write-Report -Status "failed" -Summary $message
    Write-Error $_
    exit 1
}
