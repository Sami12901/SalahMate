# Security Policy (`SECURITY.md`)

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

---

## Privacy & Security Architecture

1. **Zero Cloud Storage of Personal Data**: SalahMate stores all user-authored data (prayer logs, Hifz progress, Quran bookmarks, Tasbih counters, and personal notes) locally on the device in SQLite (`Room`) and `DataStore`.
2. **Secret & API Key Management**:
   - Never commit `.env`, `local.properties`, `*.jks`, or `*.keystore` files to version control.
   - Only `.env.example` (containing safe placeholder values) is tracked in Git.
   - **Important APK Decompilation Warning**: Android APKs can be decompiled, and any API keys injected into `BuildConfig` (such as a personal `SUNNAH_API_KEY`) can be extracted from the binary. Do **not** bundle private production API keys into publicly distributed APKs on GitHub Releases. Leave `SUNNAH_API_KEY=OPTIONAL_SUNNAH_API_KEY` as the placeholder when building public release APKs.

---

## Reporting a Vulnerability

If you discover a security or privacy vulnerability in SalahMate:
1. **Do not open a public issue** if the vulnerability could put user data or credentials at risk.
2. Use **GitHub Private Vulnerability Reporting** (under the repository's **Security > Advisories** tab) to submit a detailed report including:
   - Affected component or file path
   - Steps to reproduce
   - Potential impact and suggested fix
3. We aim to acknowledge vulnerability reports within **72 hours** and publish a verified fix as quickly as possible.
