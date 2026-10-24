## Summary of Changes
Briefly describe what this Pull Request implements or fixes.

## Related Issue
Closes #

## Verification & Quality Checklist
- [ ] `gradle assembleDebug` compiles with zero errors.
- [ ] `gradle :app:testDebugUnitTest` passes all unit and Robolectric tests.
- [ ] Offline behavior verified (handles airplane mode / network failure gracefully without fake data).
- [ ] Both English (`en`) and Bengali (`bn`) UI labels updated where applicable.
- [ ] Vocalized Arabic script (`quran-uthmani`), Hadith gradings, and primary source references are preserved verbatim without alteration.
- [ ] No API keys, `.env` secrets, or keystores are included in this commit.
