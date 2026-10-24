@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "JAR_DIR=%PROJECT_DIR%out\artifacts\DuTu_Goods_Manager_jar"
set "JAR_PATH=%JAR_DIR%\DuTu Goods Manager.jar"
set "ICON_PATH=%PROJECT_DIR%DuTu.ico"
set "OUTPUT_DIR=%PROJECT_DIR%out\artifacts\exe_output"
set "STAGING_DIR=%OUTPUT_DIR%\.build_staging"
set "APP_DIR=%OUTPUT_DIR%\DuTu Goods Manager"
set "STAGED_APP_DIR=%STAGING_DIR%\DuTu Goods Manager"
set "BACKUP_DIR=%OUTPUT_DIR%\.previous_build"
set "JPACKAGE="

if not exist "%JAR_PATH%" (
    echo ERROR: JAR not found: "%JAR_PATH%"
    exit /b 1
)

if not exist "%ICON_PATH%" (
    echo ERROR: Icon not found: "%ICON_PATH%"
    exit /b 1
)

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
if not defined JPACKAGE (
    for /f "delims=" %%J in ('where jpackage.exe 2^>nul') do if not defined JPACKAGE set "JPACKAGE=%%J"
)
if not defined JPACKAGE (
    for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~D\bin\jpackage.exe" set "JPACKAGE=%%~D\bin\jpackage.exe"
)

if not defined JPACKAGE (
    echo ERROR: jpackage.exe was not found. Install a JDK or set JAVA_HOME.
    exit /b 1
)

if not exist "%OUTPUT_DIR%" (
    mkdir "%OUTPUT_DIR%"
    if errorlevel 1 (
        echo ERROR: Could not create output directory: "%OUTPUT_DIR%"
        exit /b 1
    )
)

if exist "%STAGING_DIR%" rmdir /s /q "%STAGING_DIR%"
if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"
mkdir "%STAGING_DIR%"
if errorlevel 1 (
    echo ERROR: Could not create staging directory: "%STAGING_DIR%"
    exit /b 1
)

"%JPACKAGE%" --type app-image --input "%JAR_DIR%" --dest "%STAGING_DIR%" --name "DuTu Goods Manager" --main-jar "DuTu Goods Manager.jar" --main-class Main --icon "%ICON_PATH%" --win-console
if errorlevel 1 (
    echo ERROR: jpackage failed.
    rmdir /s /q "%STAGING_DIR%"
    exit /b 1
)

if exist "%APP_DIR%" (
    move "%APP_DIR%" "%BACKUP_DIR%" >nul
    if errorlevel 1 (
        echo ERROR: Could not move the existing application image.
        rmdir /s /q "%STAGING_DIR%"
        exit /b 1
    )
)

move "%STAGED_APP_DIR%" "%APP_DIR%" >nul
if errorlevel 1 (
    echo ERROR: Could not install the new application image.
    if exist "%BACKUP_DIR%" move "%BACKUP_DIR%" "%APP_DIR%" >nul
    rmdir /s /q "%STAGING_DIR%"
    exit /b 1
)

if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"
rmdir /s /q "%STAGING_DIR%"

echo Created "%OUTPUT_DIR%\DuTu Goods Manager\DuTu Goods Manager.exe"
exit /b 0
