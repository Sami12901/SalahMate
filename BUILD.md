# Build & Verification Guide (`BUILD.md`)

## Prerequisites
- **JDK**: Java 17 / 21 (configured to compile target `JavaVersion.VERSION_11` bytecode)
- **Android SDK**: `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`gradle/libs.versions.toml`)

## Building the Application
Run the standard debug build and unit/Robolectric test suite:
```bash
gradle assembleDebug
gradle :app:testDebugUnitTest
```

## Optional API Keys (`.env` / AI Studio Secrets)
- Core functionality (Adhan prayer calculation, Qibla, bundled Quran Surahs, AlQuran.cloud streaming/downloads, AlAdhan prayer comparison, Hadith, Dua, Tasbih, Hifz, Zakat) works **without any API key**.
- For optional live `Sunnah.com` API queries, set `SUNNAH_API_KEY` in the **AI Studio Secrets panel** (declared in `.env.example`).
