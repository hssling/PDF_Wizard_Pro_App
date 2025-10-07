@echo off
echo Building PDF Wizard Pro APK...

REM Try to find Java installation
if exist "%JAVA_HOME%" goto java_found

REM Try common Java installation paths
if exist "C:\Program Files\Java\jdk-17" (
    set JAVA_HOME=C:\Program Files\Java\jdk-17
    goto java_found
)

if exist "C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot" (
    set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot
    goto java_found
)

if exist "C:\Program Files\Java\jdk-21" (
    set JAVA_HOME=C:\Program Files\Java\jdk-21
    goto java_found
)

echo ERROR: Could not find Java installation. Please set JAVA_HOME or install JDK 11+.
pause
exit /b 1

:java_found
echo Found Java at: %JAVA_HOME%
echo JAVA_HOME = %JAVA_HOME%

REM Set path to include Java
set PATH=%JAVA_HOME%\bin;%PATH%

REM Try gradle command
gradle --version >nul 2>&1
if %errorlevel% equ 0 (
    echo Building with system Gradle...
    gradle assembleDebug
) else (
    REM Try to use Android Studio gradle wrapper if it exists
    if exist "gradlew.bat" (
        echo Building with Gradle Wrapper...
        gradlew.bat assembleDebug
    ) else (
        echo ERROR: Neither Gradle nor Gradle Wrapper found.
        echo Please install Android Studio or Gradle, or ensure JAVA_HOME is set correctly.
        pause
        exit /b 1
    )
)

echo APK building completed. Check app/build/outputs/apk/debug/ for APK files.
pause
