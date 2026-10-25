# Build & Verification Guide (`BUILD.md`)

**Developer & Maintainer**: Mohammad Sami Islam  
**License**: MIT License (100% Free & Open-Source Software)  
**Application ID**: `com.samiislam.salahmate`

## Prerequisites
- **JDK**: Java 17 / 21 (configured to compile target `JavaVersion.VERSION_11` bytecode)
- **Android SDK**: `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`gradle/libs.versions.toml`)

## Building the Application
Run the standard debug build and unit/Robolectric test suite:
```bash
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest
```

## Optional API Keys (`.env`)
- Core functionality (Adhan prayer calculation, Qibla, bundled Quran Surahs, AlQuran.cloud streaming/downloads, AlAdhan prayer comparison, Hadith, Dua, Tasbih, Hifz, Zakat) works **100% without any API key**.
- For optional live `Sunnah.com` API queries, copy `.env.example` to `.env` and set `SUNNAH_API_KEY`.
