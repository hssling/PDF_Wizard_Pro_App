@echo off
echo 🧙‍♂️ PDF WIZARD PRO - APK INSTALL TEST

echo Checking APK location...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✅ APK found: app\build\outputs\apk\debug\app-debug.apk
    echo 📏 Size check...
    dir "app\build\outputs\apk\debug\app-debug.apk" | find "app-debug.apk"
) else (
    echo ❌ APK not found. Run fast_build.bat first.
    goto :end
)

echo.
echo 📱 TO INSTALL ON ANDROID DEVICE:
echo ======================================

echo STEP 1: Connect Android device via USB
echo - Enable "Developer Options" (tap Build Number 7 times)
echo - Enable "USB Debugging" in Developer Options
echo - Connect device via USB cable

echo.
echo STEP 2: Install APK
echo - Command: adb install app\build\outputs\apk\debug\app-debug.apk
echo - Alternative: File Browser → app\build\outputs\apk\debug\ → app-debug.apk → Install

echo.
echo 🎉 PDF Wizard Pro will install on your Android device!
echo Features: PDF text editing, image insertion, page operations, search & more

echo.
echo 🌟 PRO TIP: Enable "Install via USB" in Developer Options for adb

:end
pause
