# Contributing to SalahMate

Thank you for your interest in contributing to **SalahMate**! Because SalahMate is an offline-first Islamic utility relied upon for daily worship, Quran reading, and religious reference, all contributions must adhere to both our engineering and religious-content accuracy standards.

---

## 1. Core Principles & Non-Negotiable Rules

1. **Sacred Text Integrity**:
   - **Never** modify, abbreviate, or strip diacritics from Quranic Arabic text (`quran-uthmani`).
   - **Never** invent Hadith narrations, alter authenticity gradings, or mix editorial commentary into primary Arabic/translated religious texts.
   - Every Hadith or Dua entry added to [`VerifiedIslamicContent.kt`](app/src/main/java/com/example/data/hadith/VerifiedIslamicContent.kt) **must** include an verifiable primary source citation (e.g., *Sahih al-Bukhari 6306*, *Sahih Muslim 2723*) and authentic grading where applicable.
2. **Prayer & Fasting Accuracy**:
   - Keep Calculation Authority (e.g., *Karachi Method 1*) strictly separate from Asr Juristic School (*Standard* vs. *Hanafi*).
   - Keep *Imsak*, *Fajr*, *Maghrib*, and *Iftar* clearly distinguished in both domain logic and UI labels.
3. **Offline-First & Privacy-First**:
   - Do not introduce Firebase, third-party analytics SDKs, advertising trackers, or mandatory cloud accounts.
   - Every new user record type must be persisted locally in Room (`SalahMateDatabase`) and supported by `UserDataBackupManager` for JSON export/import.
4. **No Dead-End UI**:
   - Never add placeholder buttons or unimplemented settings toggles. Every interactive element must have a working handler and `Modifier.testTag(...)`.

---

## 2. Development Setup

1. Fork the repository and clone your fork:
   ```bash
   git clone https://github.com/<your-username>/salahmate.git
   cd salahmate
   cp .env.example .env
   ```
2. Open the project in Android Studio (Meerkat / Ladybug or newer) with JDK 17/21 and Android SDK 36.
3. Run the build and unit/Robolectric test suite before submitting a Pull Request:
   ```bash
   gradle assembleDebug
   gradle :app:testDebugUnitTest
   ```

---

## 3. Pull Request Checklist

- [ ] Code compiles cleanly without warnings or errors (`gradle assembleDebug`).
- [ ] All existing and new unit/Robolectric tests pass (`gradle :app:testDebugUnitTest`).
- [ ] Both English (`en`) and Bengali (`bn`) strings are provided in `BilingualStrings` or screen labels.
- [ ] Interactive Compose elements include `Modifier.testTag("snake_case_tag")` and meet the `48.dp` minimum touch target size.
- [ ] Any religious content changes cite verified primary sources in the PR description.
