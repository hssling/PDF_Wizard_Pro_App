# PDF Wizard Pro App

A professional Android PDF editor application that allows users to view, edit, and manipulate PDF files with advanced features.

## 🚀 Features

### Core PDF Operations
- **PDF Viewer**: High-quality PDF rendering with smooth navigation
- **Text Annotations**: Add text annotations to PDF pages
- **Page Management**: Delete unwanted pages from PDFs
- **Page Rotation**: Rotate PDF pages by 90, 180, or 270 degrees
- **Image Insertion**: Insert images into PDF documents
- **File Sharing**: Share edited PDFs via various platforms

### Advanced Features
- **Intent Integration**: Open PDFs directly from other apps
- **File Provider**: Secure file sharing using FileProvider
- **Modern UI**: Material Design 3 with Jetpack Compose
- **Responsive Design**: Optimized for various screen sizes

## 📱 Screenshots

*Add screenshots of your app here*

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **PDF Processing**: iText7
- **PDF Viewer**: Android PDF Viewer library
- **Architecture**: MVVM (Model-View-ViewModel)
- **Build System**: Gradle
- **Minimum SDK**: API 21 (Android 5.0)

## 📋 Requirements

- Android Studio Arctic Fox or later
- Minimum Android SDK API 21
- Android SDK Build Tools
- Kotlin plugin

## 🔧 Installation & Setup

### Prerequisites
1. Install Android Studio
2. Install Android SDK (API 21+)
3. Enable USB Debugging on your Android device

### Build Instructions

#### Option 1: Using Batch Files (Windows)
```bash
# Quick build
./fast_build.bat

# Professional build with optimizations
./final_professional_build.bat

# Direct APK generation
./direct_build.bat

# Test installation
./test_install.bat
```

#### Option 2: Using Gradle (Cross-platform)
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean and build
./gradlew clean build
```

#### Option 3: Android Studio
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click "Run" or "Build" → "Build APK(s)"

## 📦 Project Structure

```
pdf-wizard-pro-app/
├── app/                          # Main app module
│   ├── src/main/
│   │   ├── java/com/pdfwizard/pro/
│   │   │   ├── MainActivity.kt   # Main activity
│   │   │   └── PdfEditorApp.kt   # Main composable
│   │   ├── res/
│   │   │   ├── drawable-mdpi/     # App icons
│   │   │   ├── values/           # Colors, strings, themes
│   │   │   └── xml/              # File paths, backup rules
│   │   └── AndroidManifest.xml   # App manifest
│   └── build.gradle              # App-level build config
├── gradle/                       # Gradle wrapper
├── build/                        # Build outputs
├── publishing/                   # Publishing scripts
├── *.bat                         # Build scripts
└── README.md                     # This file
```

## 🔐 Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE`: Read PDF files from device storage
- `WRITE_EXTERNAL_STORAGE`: Save edited PDFs (API < 29)
- `READ_MEDIA_IMAGES/VIDEO/AUDIO`: Read media files for insertion
- `INTERNET`: Network operations (if needed)
- `ACCESS_NETWORK_STATE`: Check network connectivity

## 🚀 CI/CD Pipeline

This project includes automated CI/CD using GitHub Actions:

### Features
- **Automated APK Building**: Builds debug and release APKs on every push
- **Artifact Upload**: Uploads APK files for download
- **Multi-variant Builds**: Supports different build configurations
- **Automated Testing**: Runs unit tests and lint checks

### Workflow Triggers
- Push to `main` branch
- Pull requests to `main` branch
- Manual workflow dispatch

### Download Latest APK
After successful CI/CD build, download the latest APK from:
- **Debug APK**: Available in Actions artifacts
- **Release APK**: Available in Actions artifacts (signed)

## 📱 Usage

### Opening a PDF
1. Launch the app
2. Tap "Open PDF File"
3. Select a PDF from your device storage
4. The PDF will open in the viewer

### Editing Operations
1. **Add Text**: Tap the edit icon → Enter text → Select page
2. **Delete Page**: Use page controls → Select "Delete Page"
3. **Rotate Page**: Use page controls → Select "Rotate Page"
4. **Insert Image**: Tap "Insert Image" → Select image from gallery

### Saving and Sharing
1. Edit your PDF as needed
2. Tap the share icon
3. Choose "Save PDF" to save locally
4. Or share directly via other apps

## 🔧 Development

### Code Structure
- `MainActivity.kt`: Main activity handling intents and PDF loading
- `PdfEditorApp.kt`: Main composable with UI logic
- `PdfViewModel.kt`: ViewModel for PDF operations

### Adding New Features
1. Add new functions to `PdfViewModel.kt`
2. Update UI in `PdfEditorApp.kt`
3. Add new menu items in the UI composables

### Testing
```bash
# Run all tests
./gradlew test

# Run connected device tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "*.YourTestClass"
```

## 🐛 Troubleshooting

### Common Issues

**Build Fails**
- Ensure Android SDK is properly installed
- Check JDK version (JDK 8 or 11 recommended)
- Clear Gradle cache: `./gradlew clean`

**APK Installation Fails**
- Enable "Unknown Sources" on your device
- Check if another version is already installed
- Try uninstalling and reinstalling

**PDF Loading Issues**
- Ensure PDF is not corrupted
- Check file permissions
- Try a different PDF file

**Memory Issues**
- Close other apps while using PDF Wizard Pro
- Use smaller PDF files for better performance

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For support and questions:
- Create an issue in the GitHub repository
- Check the troubleshooting section above
- Review the FAQ in the Wiki

## 🔄 Updates

- **Version 1.0.0**: Initial release with core PDF editing features
- **Version 1.1.0**: Enhanced UI and performance improvements
- **Version 1.2.0**: Added image insertion and advanced annotations

## 🙏 Acknowledgments

- **iText7**: PDF processing library
- **Android PDF Viewer**: PDF rendering component
- **Jetpack Compose**: Modern Android UI toolkit
- **Material Design**: UI/UX design system

---

**Made with ❤️ for PDF enthusiasts everywhere!**
