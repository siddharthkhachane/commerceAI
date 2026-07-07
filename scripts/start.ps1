# CommerceAI local dev startup
# Usage: .\scripts\start.ps1   OR   npm start

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

function Write-Step($message) {
    Write-Host "`n==> $message" -ForegroundColor Cyan
}

function Test-Command($name) {
    return $null -ne (Get-Command $name -ErrorAction SilentlyContinue)
}

Write-Host "CommerceAI - starting local dev environment" -ForegroundColor Green
Write-Host "Project root: $Root"

# Prerequisites
Write-Step "Checking prerequisites"
foreach ($cmd in @("node", "npm", "docker")) {
    if (-not (Test-Command $cmd)) {
        Write-Error "$cmd is not installed or not on PATH."
    }
}

if (-not (Test-Command "java")) {
    Write-Warning "Java not found on PATH. Backend needs Java 21+."
} else {
    java -version
}

# Environment files
Write-Step "Ensuring environment files"
if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Created .env from .env.example"
}
if (-not (Test-Path "frontend\.env.local")) {
    Copy-Item "frontend\.env.example" "frontend\.env.local"
    Write-Host "Created frontend/.env.local from .env.example"
}

# Dependencies
Write-Step "Installing dependencies (if needed)"
if (-not (Test-Path "node_modules")) {
    npm install
}
if (-not (Test-Path "frontend\node_modules")) {
    npm install --prefix frontend
}

# PostgreSQL
Write-Step "Starting PostgreSQL (Docker Compose)"
docker compose up -d

Write-Host "Waiting for PostgreSQL to be ready..."
$maxAttempts = 30
$ready = $false
for ($i = 1; $i -le $maxAttempts; $i++) {
    $status = docker compose ps --format json 2>$null | ConvertFrom-Json -ErrorAction SilentlyContinue
    $health = docker inspect --format "{{.State.Health.Status}}" commerceai-postgres 2>$null
    if ($health -eq "healthy") {
        $ready = $true
        break
    }
    # Container may not have healthcheck label on first start
    $running = docker ps --filter "name=commerceai-postgres" --filter "status=running" -q
    if ($running -and ($health -eq "" -or $null -eq $health)) {
        Start-Sleep -Seconds 2
        $ready = $true
        break
    }
    Start-Sleep -Seconds 1
}

if (-not $ready) {
    Write-Warning "PostgreSQL may still be starting. Backend might retry on its own."
} else {
    Write-Host "PostgreSQL is ready." -ForegroundColor Green
}

# Dev servers
Write-Step "Starting frontend + backend"
$banner = @"

  Frontend:  http://localhost:3000
  Backend:   http://localhost:8080
  Health:    http://localhost:8080/api/health

  Admin:     admin@commerceai.com / admin12345
  Stop:      Ctrl+C (then run npm run docker:down to stop Postgres)

"@
Write-Host $banner -ForegroundColor Yellow

npm run dev
