param(
    [string]$InputPath,
    [string]$OutputPath
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Join-Path (Split-Path -Parent $scriptDir) 'Kotlin prototype 1'

if (-not (Test-Path $projectDir)) {
    throw "Could not find the Kotlin console project at: $projectDir"
}

$appArgs = @()
if ($InputPath) {
    $appArgs += '"' + $InputPath + '"'
}
if ($OutputPath) {
    $appArgs += '"' + $OutputPath + '"'
}

$gradleArgs = @('runExcelGradeCalculator')
if ($appArgs.Count -gt 0) {
    $gradleArgs += ('-PappArgs=' + ($appArgs -join ' '))
}

Push-Location $projectDir
try {
    if (Get-Command gradle -ErrorAction SilentlyContinue) {
        & gradle @gradleArgs
    } else {
        throw 'Gradle is not available on PATH.'
    }
} finally {
    Pop-Location
}