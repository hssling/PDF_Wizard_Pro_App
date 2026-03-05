# PDF Wizard Pro - The Complete Suite

PDF Wizard Pro is a fully functional, professional-grade cross-platform PDF suite. It features everything required for personal and professional document manipulations - from reading, editing, and compressing, to merging and extracting.

This repository proudly houses two native environments:

1. **The Professional Android App** (`/app`) built beautifully with Jetpack Compose.
2. **The Modern Web App** (`/web-app`) built natively with React + TypeScript, running entirely client-side for maximum security and deployment via Netlify.

---

## 💻 1. The Modern Web Application (`/web-app`)

The Web client serves as a beautiful drag-and-drop workspace that acts as the command center for your documents directly in the browser using `pdf-lib`.

### ✨ Web Features

- **Client-Side Processing**: Highly secure, fast performance, entirely processed natively mapped via browser engines without sending sensitive files to backend servers.
- **Merge & Split PDF**: Combine multiple PDFs into a single continuous file or extract certain pages as distinct documents.
- **Advanced Tools**: Insert personalized image/text Watermarks seamlessly, rotate exact pages 90° or 180°, and inject PDF AES passwords.
- **AI Analyze (Next-Gen Labs)**: A futuristic preview integrating AI to summarize, extract, and chat with your document using a simulated local AI pipeline.

### 🚀 Web Deployment (CI/CD)

The Web app is strictly configured with a **GitHub Actions -> Netlify** deployment pipeline.

1. Within your GitHub repository's `Settings > Secrets and variables > Actions`, add:
   - `NETLIFY_AUTH_TOKEN`: Your Netlify Personal Access Token
   - `NETLIFY_SITE_ID`: Your Netlify App API ID
2. Any changes pushed to the `web-app` directory natively triggers `.github/workflows/netlify.yml`, instantly publishing your live site to Netlify.

---

## 📱 2. The Android Application (`/app`)

PDF Wizard Pro uses Jetpack Compose to focus on day-to-day document cleanup tasks such as deleting, rotating, reordering, duplicating, annotating, and exporting Play Store ready data natively.

### ✨ Android Features

- Navigate through high-quality previews rendered natively with `PdfRenderer`.
- Delete, rotate, reorder (left/right), duplicate, or insert blank pages.
- Add sticky-note style text annotations anywhere on the page.
- Persist document metadata (title, author, subject, keywords, etc.) using `PdfBox` directly on export securely via `androidx.core.content.FileProvider`.

### 🛠️ Android Build Instructions

Open the repository root inside **Android Studio** (Iguana or newer). Ensure Gradle finishes synchronizing and compile natively to your emulator.

Alternatively via Gradle local builds:

```bash
# Generate a debug build
./gradlew assembleDebug

# Generate a release build (requires signing config)
./gradlew assembleRelease
```

---

## 📂 Repository Architecture

```text
PDF_Wizard_Pro_App/
├── web-app/                    # React + Vite Client-side Web Platform
│   ├── src/                    # Web source set
│   ├── package.json
│   └── netlify.toml            # Netlify explicit routing configs
├── app/                        # Android App Module
│   ├── src/main/java...        # Android Source sets
│   ├── build.gradle            # Compose config
├── .github/workflows/          # CI/CD pipelines
│   └── netlify.yml             # Web app pipeline triggering Netlify
├── build.gradle                # Android Root script
└── settings.gradle             # Android settings config
```

## 🔒 Storage & Permissions

Both applications securely utilize isolated file management paths! The Android app relies natively on Storage Access Framework (SAF) without legacy wide-access permissions, while the Web app handles DOM-Blob generation strictly through the client lifecycle.

## 📄 License

This project is distributed under the MIT License. See `LICENSE` for details.
