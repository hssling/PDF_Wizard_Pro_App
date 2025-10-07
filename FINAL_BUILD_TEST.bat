@echo off
echo ================================================
echo  FINAL APK BUILD TEST - PDF WIZARD PRO
echo ================================================

REM Set up environment
set ANDROID_HOME=%CD%\ANDROID_SDK
set ANDROID_SDK_ROOT=%ANDROID_HOME%
set PATH=%PATH%;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\build-tools\34.0.0;%ANDROID_HOME%\platforms\android-34

echo ✅ Environment configured
echo ANDROID_HOME: %ANDROID_HOME%

echo.
echo 🔧 Running final build test...

REM Run the actual Gradle build
call gradle assembleDebug --console=quiet --no-daemon

REM Check if real APK was generated
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo 🎉 SUCCESS! Real APK Generated
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do set size=%%~zA
    echo File size: !size! bytes

    if !size! gtr 1000000 (
        echo ✅ TRUE APK DETECTED (Size > 1MB)
        echo 🎊 THIS IS YOUR REAL FUNCTIONAL PDF EDITOR APK!
    ) else (
        echo ⚠️  Small file - may be placeholder
        echo ❗ Run in Android Studio for full build
    )

    echo.
    echo 📦 APK Location: %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo 🚀 Install: adb install "app\build\outputs\apk\debug\app-debug.apk"

    goto :complete
) else (
    echo.
    echo ❌ APK not generated
    echo ❌ Build failed - check error messages above
    goto :complete
)

:complete
echo.
echo ================================================
echo  BUILD TEST COMPLETE
echo ================================================

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✅ APK READY FOR TESTING!
) else (
    echo ❌ APK BUILD REQUIRES ANDROID STUDIO
)

echo.
echo Alternative: Open project in Android Studio for guaranteed APK build
pause
