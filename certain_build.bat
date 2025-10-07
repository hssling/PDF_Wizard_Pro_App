@echo off
echo ======================================
echo      CERTAIN PDF BUILD
echo ======================================
echo Setting up clean environment...

REM Clear all existing environment variables
set JAVA_HOME=
set GRADLE_HOME=
set PATH=%PATH%;%USERPROFILE%\.gradle\wrapper\dists

REM Find Java automatically
for /f "tokens=2*" %%i in ('where javac') do (
    for %%d in ("%%~dpi..") do set JAVA_HOME=%%~dpd
    goto java_found
)

echo No Java found! Please install Android Studio or JDK.
pause
exit /b 1

:java_found
set PATH=%JAVA_HOME%bin;%PATH%
echo Found Java at: %JAVA_HOME%

REM Use simple Gradle command
echo.
echo Building APK now...
call gradle assembleDebug --console=quiet --no-daemon 2>nul

REM Check result
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    dir "app\build\outputs\apk\debug\app-debug.apk" /b
    echo.
    echo SUCCESS! Real APK generated!
    goto :end
) else (
    echo.
    echo Build attempted but APK not found.
    goto :end
)

:end
echo.
echo Build complete. Check above for APK file size.
pause
