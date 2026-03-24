@echo off
setlocal enabledelayedexpansion

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Define POI jar locations (from Gradle cache)
set GRADLE_CACHE=%USERPROFILE%\.gradle\caches\modules-2\files-2.1

REM Build classpath with POI jars
set CLASSPATH=
for /f "tokens=*" %%A in ('dir /b /s "%GRADLE_CACHE%\org.apache.poi\poi\5.2.3\*.jar" 2^>nul') do (
    if "!CLASSPATH!"=="" (
        set "CLASSPATH=%%A"
    ) else (
        set "CLASSPATH=!CLASSPATH!;%%A"
    )
)

for /f "tokens=*" %%A in ('dir /b /s "%GRADLE_CACHE%\org.apache.poi\poi-ooxml\5.2.3\*.jar" 2^>nul') do (
    if "!CLASSPATH!"=="" (
        set "CLASSPATH=%%A"
    ) else (
        set "CLASSPATH=!CLASSPATH!;%%A"
    )
)

for /f "tokens=*" %%A in ('dir /b /s "%GRADLE_CACHE%\org.apache.poi\poi-ooxml-schemas\4.1.2\*.jar" 2^>nul') do (
    if "!CLASSPATH!"=="" (
        set "CLASSPATH=%%A"
    ) else (
        set "CLASSPATH=!CLASSPATH!;%%A"
    )
)

if "!CLASSPATH!"=="" (
    echo.
    echo ERROR: POI jars not found. Please run the Excel calculator via Gradle first:
    echo.
    echo   cd "%SCRIPT_DIR%..\..\Kotlin prototype 1"
    echo   gradle runExcelGradeCalculator
    echo.
    exit /b 1
)

REM Compile and run the Kotlin file
set SOURCE="%SCRIPT_DIR%ExcelGradeCalculatorStandalone.kt"
set TEMP_CLASS="%TEMP%\ExcelGradeCalculator"

echo Compiling Excel Grade Calculator...
kotlinc -cp "!CLASSPATH!" -d "!TEMP_CLASS!" %SOURCE%

if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)

echo Running Excel Grade Calculator...
kotlin -cp "!CLASSPATH!;!TEMP_CLASS!" ExcelGradeCalculatorStandaloneKt %*
