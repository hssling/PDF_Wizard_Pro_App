@echo off
echo ==========================================================
echo      ANDROID STUDIO JDK FIX UTILITY
echo ==========================================================

echo Checking Java installations...

REM Check for Android Studio embedded JDK
set "ANDROID_STUDIO_JDK=C:\Program Files\Android\Android Studio2\jbr"
if exist "%ANDROID_STUDIO_JDK%\bin\java.exe" (
    echo ✅ Found Android Studio Embedded JDK: %ANDROID_STUDIO_JDK%
    goto :set_java_home
)

REM Check for JDK 17/18 in Program Files
for /f "tokens=*" %%i in ('dir /b "C:\Program Files\Java\" 2^>nul') do (
    echo Found JDK: %%i
    if "%%i"=="jdk-17" (
        set "ANDROID_STUDIO_JDK=C:\Program Files\Java\jdk-17"
        goto :set_java_home
    )
    if "%%i"=="jdk-18" (
        set "ANDROID_STUDIO_JDK=C:\Program Files\Java\jdk-18"
        goto :set_java_home
    )
    if "%%i"=="jdk-11" (
        set "ANDROID_STUDIO_JDK=C:\Program Files\Java\jdk-11"
        goto :set_java_home
    )
)

echo ⚠️  No compatible JDK found. Please install Android Studio or JDK 17/18.
goto :end

:set_java_home
echo Setting JAVA_HOME to: %ANDROID_STUDIO_JDK%
setx JAVA_HOME "%ANDROID_STUDIO_JDK%" /M

echo Updating PATH...
set PATH=%ANDROID_STUDIO_JDK%\bin;%PATH%

echo Verifying Java installation...
java -version 2>&3
if errorlevel 1 (
    echo ❌ Java verification failed
) else (
    echo ✅ Java verification successful
)

echo.
echo ==========================================================
echo        JDK CONFIGURATION COMPLETE
echo ==========================================================
echo.
echo Next Steps:
echo 1. RESTART Android Studio completely
echo 2. Open PDF Wizard Pro project
echo 3. Project should sync without JDK errors
echo 4. Build → Make Project to generate APK
echo.
echo If issues persist, check File → Settings → Build, Execution, Deployment → Build Tools → Gradle
echo and manually set "Gradle JDK" to the path displayed above.
echo.

:end
echo Press any key to continue...
pause > nul
