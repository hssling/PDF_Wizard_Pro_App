@echo off
echo PDF Wizard Pro - Keystore Generator
echo ===================================

if exist keystore\pdfwizard.jks (
    echo Keystore already exists at keystore\pdfwizard.jks
    echo Delete it first if you want to generate a new one
    pause
    exit /b 1
)

echo.
echo This will generate a keystore for signing your APK files.
echo Make sure you remember the passwords as you'll need them for releases.
echo.

set /p key_alias="Enter key alias (default: pdfwizard_key): "
if "%key_alias%"=="" set key_alias=pdfwizard_key

set /p store_pass="Enter keystore password (default: PdfWizard2024!): "
if "%store_pass%"=="" set store_pass=PdfWizard2024!

set /p key_pass="Enter key password (default: PdfWizard2024!): "
if "%key_pass%"=="" set key_pass=PdfWizard2024!

echo.
echo Generating keystore...
echo Command: keytool -genkey -v -keystore keystore/pdfwizard.jks -keyalg RSA -keysize 2048 -validity 10000 -alias %key_alias%
echo.

keytool -genkey -v -keystore keystore/pdfwizard.jks -keyalg RSA -keysize 2048 -validity 10000 -alias %key_alias% -storepass %store_pass% -keypass %key_pass% -dname "CN=PDF Wizard Pro, OU=Development, O=PDFWizard, L=Unknown, ST=Unknown, C=US"

if %errorlevel% equ 0 (
    echo.
    echo Keystore generated successfully!
    echo Location: keystore/pdfwizard.jks
    echo Alias: %key_alias%
    echo.
    echo IMPORTANT: Keep these passwords safe:
    echo - Keystore password: %store_pass%
    echo - Key password: %key_pass%
    echo.
    echo Update keystore.properties file with these values if needed.
    echo.
    pause
) else (
    echo.
    echo Failed to generate keystore!
    pause
    exit /b 1
)
