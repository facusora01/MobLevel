# Script para ejecutar tests y capturar logs
# Uso: .\run-tests-with-logs.ps1 [-OpenReport] [-Rerun] [-NoDaemon]

param(
    [switch]$OpenReport,
    [switch]$Rerun,
    [switch]$NoDaemon
)

$projectRoot = (Get-Item -Path $PSScriptRoot).Parent.Parent.Parent.FullName
$logDir = "$projectRoot\build\test-logs"

# Crear directorio si no existe
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$logFile = "$logDir\test-run_$timestamp.log"

Write-Host "Ejecutando tests..." -ForegroundColor Cyan
Write-Host "Logs: $logFile" -ForegroundColor Gray

# Construir argumentos de gradle
$gradleArgs = @('test')
if ($Rerun) { $gradleArgs += '--rerun-tasks' }
if ($NoDaemon) { $gradleArgs += '--no-daemon' }

# Ejecutar tests y capturar salida
Push-Location $projectRoot
try {
    & .\gradlew @gradleArgs 2>&1 | Tee-Object -FilePath $logFile
    $testSuccess = $LASTEXITCODE -eq 0
} finally {
    Pop-Location
}

# Resumen
Write-Host ""
$resultColor = if ($testSuccess) { 'Green' } else { 'Red' }
Write-Host "=== RESULTADO ===" -ForegroundColor $resultColor
if ($testSuccess) {
    Write-Host "[OK] Tests pasaron exitosamente" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Algunos tests fallaron" -ForegroundColor Red
}
Write-Host ""
Write-Host "Logs:  $logFile"
Write-Host "HTML:  $projectRoot\build\reports\tests\test\index.html"
Write-Host ""

# Abrir reporte si se especificó -OpenReport
if ($OpenReport) {
    Write-Host "Abriendo reporte HTML..." -ForegroundColor Cyan
    Start-Process "$projectRoot\build\reports\tests\test\index.html"
}
