# ============================================================
# start.ps1 — Single-script runner for AI Text Summarizer
#
# Usage:
#   .\start.ps1                          # Build + run locally (Java)
#   .\start.ps1 -Token hf_xxx           # Build + run locally with token
#   .\start.ps1 -Docker                  # Build Docker image + run container
#   .\start.ps1 -Docker -Token hf_xxx   # Docker run with token
#   .\start.ps1 -DockerBuild -Token hf_xxx  # Force rebuild Docker image + run
# ============================================================
param(
    [switch]$Docker,
    [switch]$DockerBuild,
    [string]$Token = $env:HUGGINGFACE_TOKEN
)

$ErrorActionPreference = "Stop"
$SCRIPT_DIR  = Split-Path -Parent $MyInvocation.MyCommand.Definition
$BACKEND_DIR = Join-Path $SCRIPT_DIR "summarizer-backend"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   AI Text Summarizer — Start Script    " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ── Docker mode ────────────────────────────────────────────────────────────
if ($Docker -or $DockerBuild) {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Host "ERROR: Docker not found. Install Docker Desktop first." -ForegroundColor Red
        exit 1
    }

    Set-Location $BACKEND_DIR

    if ($DockerBuild -or -not (docker images -q summarizer-backend 2>$null)) {
        Write-Host "[1/2] Building Docker image 'summarizer-backend'..." -ForegroundColor Yellow
        docker build -t summarizer-backend .
        if ($LASTEXITCODE -ne 0) { Write-Host "Docker build failed." -ForegroundColor Red; exit 1 }
    }

    Write-Host "[2/2] Starting container on http://localhost:8080 ..." -ForegroundColor Green
    Write-Host "      Press Ctrl+C to stop." -ForegroundColor Gray
    Write-Host ""

    if ($Token) {
        docker run --rm -p 8080:8080 -e HUGGINGFACE_TOKEN="$Token" summarizer-backend
    } else {
        Write-Host "WARNING: No HUGGINGFACE_TOKEN set. Summarization will fail until token is provided." -ForegroundColor Yellow
        docker run --rm -p 8080:8080 summarizer-backend
    }
    exit 0
}

# ── Local Java mode ─────────────────────────────────────────────────────────
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Java not found. Install JDK 25+ or use -Docker mode." -ForegroundColor Red
    exit 1
}

Set-Location $BACKEND_DIR

# Create secrets.properties from token flag if not already present
$secretsFile = "src\main\resources\secrets.properties"
if ($Token -and -not (Test-Path $secretsFile)) {
    "huggingface.token=$Token" | Set-Content $secretsFile
    Write-Host "Created $secretsFile with provided token." -ForegroundColor Green
} elseif (-not (Test-Path $secretsFile) -and -not $Token) {
    Write-Host "WARNING: $secretsFile not found and no -Token provided." -ForegroundColor Yellow
    Write-Host "         Create it manually: echo 'huggingface.token=hf_xxx' > $secretsFile" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "[1/2] Building application (this may take ~30s first time)..." -ForegroundColor Yellow
.\mvnw.cmd clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) { Write-Host "Build failed. Run '.\mvnw.cmd clean package' to see errors." -ForegroundColor Red; exit 1 }

Write-Host "[2/2] Starting application on http://localhost:8080 ..." -ForegroundColor Green
Write-Host "      Press Ctrl+C to stop." -ForegroundColor Gray
Write-Host ""
java -jar target\summarizer-backend-0.0.1-SNAPSHOT.jar
