@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "INPUT_DIR=%PROJECT_DIR%out\artifacts\DuTu_Goods_Manager_jar"
set "ICON_PATH=%PROJECT_DIR%DuTu.ico"
set "OUTPUT_DIR=%PROJECT_DIR%out\artifacts\exe_output_app"
set "STAGING_DIR=%OUTPUT_DIR%\.build_staging"
set "APP_DIR=%OUTPUT_DIR%\DuTu Goods Manager"
set "STAGED_APP_DIR=%STAGING_DIR%\DuTu Goods Manager"
set "BACKUP_DIR=%OUTPUT_DIR%\.previous_build"
set "JPACKAGE="
set "MAVEN="
set "JDK_BIN="

if not exist "%INPUT_DIR%\DuTu Goods Manager.jar" (
    echo ERROR: Main JAR not found in "%INPUT_DIR%".
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
    for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~D\bin\jpackage.exe" if not defined JPACKAGE set "JPACKAGE=%%~D\bin\jpackage.exe"
)

if not defined JPACKAGE (
    echo ERROR: jpackage.exe was not found. Install a JDK or set JAVA_HOME.
    exit /b 1
)
for %%J in ("%JPACKAGE%") do set "JDK_BIN=%%~dpJ"

for /f "delims=" %%M in ('where mvn.cmd 2^>nul') do if not defined MAVEN set "MAVEN=%%M"
if defined MAVEN (
    pushd "%PROJECT_DIR%"
    call "%MAVEN%" -q package dependency:copy-dependencies -DincludeScope=runtime "-DoutputDirectory=%INPUT_DIR%"
    if errorlevel 1 (
        popd
        echo ERROR: Maven build or runtime dependency copy failed.
        exit /b 1
    )
    copy /y "%PROJECT_DIR%target\DuTu Goods Manager.jar" "%INPUT_DIR%\DuTu Goods Manager.jar" >nul
    if errorlevel 1 (
        popd
        echo ERROR: Could not copy the application JAR into the packaging directory.
        exit /b 1
    )
    popd
) else (
    if not exist "%JDK_BIN%javac.exe" (
        echo ERROR: javac.exe was not found beside jpackage.exe.
        exit /b 1
    )
    if not exist "%JDK_BIN%jar.exe" (
        echo ERROR: jar.exe was not found beside jpackage.exe.
        exit /b 1
    )
    if exist "%PROJECT_DIR%target\.portable-build-classes" rmdir /s /q "%PROJECT_DIR%target\.portable-build-classes"
    mkdir "%PROJECT_DIR%target\.portable-build-classes"
    if errorlevel 1 (
        echo ERROR: Could not create the temporary compilation directory.
        exit /b 1
    )
    "%JDK_BIN%javac.exe" --add-modules jdk.httpserver -cp "%INPUT_DIR%\javafx-base-21.0.2-win.jar;%INPUT_DIR%\javafx-controls-21.0.2-win.jar;%INPUT_DIR%\javafx-graphics-21.0.2-win.jar;%INPUT_DIR%\javafx-media-21.0.2-win.jar;%INPUT_DIR%\javafx-web-21.0.2-win.jar" -d "%PROJECT_DIR%target\.portable-build-classes" -sourcepath "%PROJECT_DIR%src" "%PROJECT_DIR%src\Main.java"
    if errorlevel 1 (
        rmdir /s /q "%PROJECT_DIR%target\.portable-build-classes"
        echo ERROR: Java compilation failed.
        exit /b 1
    )
    "%JDK_BIN%jar.exe" --create --file "%PROJECT_DIR%target\DuTu Goods Manager.jar" --main-class Main -C "%PROJECT_DIR%target\.portable-build-classes" .
    if errorlevel 1 (
        rmdir /s /q "%PROJECT_DIR%target\.portable-build-classes"
        echo ERROR: Could not create the application JAR.
        exit /b 1
    )
    copy /y "%PROJECT_DIR%target\DuTu Goods Manager.jar" "%INPUT_DIR%\DuTu Goods Manager.jar" >nul
    if errorlevel 1 (
        rmdir /s /q "%PROJECT_DIR%target\.portable-build-classes"
        echo ERROR: Could not copy the application JAR into the packaging directory.
        exit /b 1
    )
    rmdir /s /q "%PROJECT_DIR%target\.portable-build-classes"
)

for %%F in (base controls graphics media web) do (
    if not exist "%INPUT_DIR%\javafx-%%F-21.0.2-win.jar" (
        echo ERROR: JavaFX runtime library javafx-%%F is missing. Run this script with Maven or copy runtime dependencies into "%INPUT_DIR%".
        exit /b 1
    )
)

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if errorlevel 1 (
    echo ERROR: Could not create output directory: "%OUTPUT_DIR%"
    exit /b 1
)

if exist "%STAGING_DIR%" rmdir /s /q "%STAGING_DIR%"
if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"
mkdir "%STAGING_DIR%"
if errorlevel 1 (
    echo ERROR: Could not create the staging directory.
    exit /b 1
)

"%JPACKAGE%" --type app-image --input "%INPUT_DIR%" --dest "%STAGING_DIR%" --name "DuTu Goods Manager" --main-jar "DuTu Goods Manager.jar" --main-class Main --icon "%ICON_PATH%"
if errorlevel 1 (
    rmdir /s /q "%STAGING_DIR%"
    echo ERROR: jpackage failed.
    exit /b 1
)

if exist "%APP_DIR%" (
    move "%APP_DIR%" "%BACKUP_DIR%" >nul
    if errorlevel 1 (
        rmdir /s /q "%STAGING_DIR%"
        echo ERROR: Could not back up the existing application image.
        exit /b 1
    )
)

move "%STAGED_APP_DIR%" "%APP_DIR%" >nul
if errorlevel 1 (
    if exist "%BACKUP_DIR%" move "%BACKUP_DIR%" "%APP_DIR%" >nul
    rmdir /s /q "%STAGING_DIR%"
    echo ERROR: Could not install the new application image.
    exit /b 1
)

if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"
rmdir /s /q "%STAGING_DIR%"

echo Created "%OUTPUT_DIR%\DuTu Goods Manager\DuTu Goods Manager.exe"
exit /b 0
