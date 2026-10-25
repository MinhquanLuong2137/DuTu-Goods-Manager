@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "JAR_DIR=%PROJECT_DIR%target\jpackage-input"
set "JAR_PATH=%JAR_DIR%\DuTu Goods Manager.jar"
set "MAVEN="
set "ICON_PATH=%PROJECT_DIR%DuTu.ico"
set "OUTPUT_DIR=%PROJECT_DIR%out\artifacts\exe_output"
set "STAGING_DIR=%OUTPUT_DIR%\.build_staging"
set "APP_DIR=%OUTPUT_DIR%\DuTu Goods Manager"
set "STAGED_APP_DIR=%STAGING_DIR%\DuTu Goods Manager"
set "BACKUP_DIR=%OUTPUT_DIR%\.previous_build"
set "JPACKAGE="

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

for /f "delims=" %%M in ('where mvn.cmd 2^>nul') do if not defined MAVEN set "MAVEN=%%M"
if not defined MAVEN (
    echo ERROR: Maven was not found. Install Maven and add mvn.cmd to PATH.
    exit /b 1
)

pushd "%PROJECT_DIR%"
call "%MAVEN%" -q package dependency:copy-dependencies -DincludeScope=runtime "-DoutputDirectory=%JAR_DIR%"
if errorlevel 1 (
    popd
    echo ERROR: Maven build or dependency copy failed.
    exit /b 1
)
copy /y "%PROJECT_DIR%target\DuTu Goods Manager.jar" "%JAR_PATH%" >nul
if errorlevel 1 (
    popd
    echo ERROR: Could not copy the application JAR into the packaging directory.
    exit /b 1
)
popd

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

"%JPACKAGE%" --type app-image --input "%JAR_DIR%" --dest "%STAGING_DIR%" --name "DuTu Goods Manager" --main-jar "DuTu Goods Manager.jar" --main-class Main --icon "%ICON_PATH%"
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
