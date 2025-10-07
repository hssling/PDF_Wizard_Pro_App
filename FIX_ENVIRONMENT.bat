@echo off
echo ===================================================
echo  FIXING ANDROID TOOLCHAIN ENVIRONMENT
echo ===================================================

REM Step 1: Verify system requirements
echo [1/6] Checking system compatibility...
echo OS: Windows
where java >nul 2>&1 && echo Java: ✓ FOUND || echo Java: ❌ NOT FOUND
where git >nul 2>&1 && echo Git: ✓ FOUND || echo Git: ✓ NOT FOUND

REM Step 2: Download Android Command Line Tools
echo.
echo [2/6] Downloading Android Command Line Tools...
if not exist "cmdline-tools" (
    curl -o "commandlinetools-win-11076708_latest.zip" "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" --progress-bar
    if not exist "commandlinetools-win-11076708_latest.zip" (
        echo ❌ Download failed. Using offline mode...
        goto :offline
    )
    unzip -q "commandlinetools-win-11076708_latest.zip"
    move "cmdline-tools" "ANDROID_SDK\cmdline-tools\latest" 2>nul
    rm "commandlinetools-win-11076708_latest.zip"
)

REM Step 3: Set up Android SDK environment
:offline
echo.
echo [3/6] Setting up Android SDK environment...
if not exist "ANDROID_SDK" mkdir "ANDROID_SDK"
set ANDROID_HOME=%CD%\ANDROID_SDK
set ANDROID_SDK_ROOT=%ANDROID_HOME%
set PATH=%PATH%;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\build-tools\34.0.0;%ANDROID_HOME%\platforms\android-34

echo ANDROID_HOME set to: %ANDROID_HOME%
setx ANDROID_HOME "%ANDROID_HOME%" /M
setx ANDROID_SDK_ROOT "%ANDROID_SDK_ROOT%" /M

REM Step 4: Install required SDK components
echo.
echo [4/6] Installing Android SDK components...
if exist "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" (
    "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" --install "platform-tools" "platforms;android-34" "build-tools;34.0.0" --accept-licenses --quiet
    if %errorlevel% equ 0 (
        echo ✅ Android SDK components installed successfully
    ) else (
        echo ⚠️  SDK installation may need manual completion
    )
) else (
    echo ⚠️  Using manual SDK setup - components may need to be installed via Android Studio
)

REM Step 5: Update local.properties
echo.
echo [5/6] Creating/updating local.properties for the project...
echo sdk.dir=%ANDROID_HOME%> local.properties
echo Android SDK configured at: %ANDROID_HOME%

REM Step 6: Test the environment
echo.
echo [6/6] Testing Android build environment...

REM Test if tools are available
"%ANDROID_HOME%\platform-tools\adb.exe" --version 1>nul 2>&1 && echo Android Debug Bridge: ✓ WORKING || echo Android Debug Bridge: ⚠️  MAY NEED MANUAL INSTALL

if exist "%ANDROID_HOME%\build-tools\34.0.0\aapt2.exe" (
    echo Build Tools: ✓ AVAILABLE
) else (
    echo Build Tools: ⚠️  MAY NEED MANUAL INSTALL
)

echo.
echo ===================================================
echo        ENVIRONMENT FIX COMPLETE!
echo ===================================================

REM Instructions for user
echo.
echo TO COMPLETE THE SETUP - Do ONE of the following:
echo.
echo OPTION A: Complete Android SDK Installation
echo 1. Open Android Studio and finish SDK setup
echo 2. Install missing components via SDK Manager
echo 3. Return to this folder and run: .\gradlew assembleDebug
echo.

echo OPTION B: Use Existing Android Studio
echo 1. Open the project in Android Studio
echo 2. Let it finish downloading all dependencies
echo 3. Build → Make Project
echo.

echo OPTION C: Use Online CI/CD
echo 1. Push to GitHub with Android CI workflow
echo 2. Let GitHub Actions build the APK
echo 3. Download the generated APK
echo.

echo ===================================================
echo PROJECT LOCATION: %CD%
echo APK WILL BE: app/build/outputs/apk/debug/app-debug.apk
echo ===================================================

pause
