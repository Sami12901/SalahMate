# Offline Prayer Push Notifications (AlarmManager + WorkManager)

Implement a resilient, 100% offline prayer notification system that pairs Android's `AlarmManager` (for exact waqt and pre-prayer wakeups during Doze mode) with Jetpack `WorkManager` (for periodic daily recalculation, midnight rollover resilience, and fallback notification delivery when exact alarms are restricted).

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices incorporate your confirmed answers from Phase 1:

- **Confirmed Decision 1 — Dual Engine (`AlarmManager` + `WorkManager`)**: Use `AlarmManager` (`setExactAndAllowWhileIdle` with graceful `setWindow` fallback) as the primary real-time wakeup trigger, paired with `WorkManager` (`PeriodicWorkRequest` + `OneTimeWorkRequest`) for daily offline schedule recalculation, missed-schedule self-healing, and fallback notification execution without requiring internet connectivity.
- **Confirmed Decision 2 — Dual Alert Cadence (Pre-Prayer + Exact Waqt)**: When an advance reminder offset (e.g., 5, 10, 15, or 30 minutes before prayer) is configured, schedule and fire **both** the advance pre-prayer reminder and the exact on-time waqt notification independently for each enabled prayer.
- **Confirmed Decision 3 — Rich Notification Actions & Live Status Table**:
  - Add three direct notification action buttons: **Mark Completed** (logs the prayer directly into the local Room database while the app is closed/offline), **Snooze 10m**, and **Dismiss**.
  - Add a **Live Scheduled Alarms Status Table** and **System Permission & Exact Alarm Shortcut** in the Prayer Notification Settings screen so users can inspect upcoming scheduled trigger times for all 5 daily prayers and grant permissions in one tap.

---

## 1. Overview & Core Concept

- **What It Does**: Calculates daily prayer times locally on-device using solar coordinates (`LocalAdhanCalculator`) and schedules reliable local push notifications for Fajr, Dhuhr, Asr, Maghrib, and Isha—even when the phone has zero internet connectivity, is in Doze mode, or has just rebooted.
- **Target Audience / Persona**: Worshippers relying on timely, offline-first prayer reminders who need confidence that their next waqt alert is queued and who want to log their completed prayers directly from the lock-screen notification shade.
- **Key Value**: Eliminates silent alarm drops caused by midnight date rollovers, timezone travel, or OS battery restrictions by combining exact hardware alarms with persistent `WorkManager` background workers.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Configuring Prayer Alerts & Advance Reminders**: In Prayer & Calculation Settings, the user toggles individual prayers (Fajr, Dhuhr, Asr, Maghrib, Isha), picks a sound mode (Adhan Chime, Default Tone, or Silent), and selects an optional Advance Reminder (On Time, 5m, 10m, 15m, or 30m before).
  2. **Inspecting the Live Scheduled Alarms Status Table**: Below the toggles, a glassmorphic status card displays the real-time queue for all 5 daily prayers—showing whether each prayer is enabled, the exact next trigger time (both Advance Reminder time and On-Time Waqt time), whether `AlarmManager` Exact Mode and `WorkManager` Daily Sync are active, and a one-tap shortcut to grant `POST_NOTIFICATIONS` or `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` if restricted.
  3. **Interacting with Lock-Screen / Heads-Up Notifications**:
     - When the advance window arrives, a notification announces: *"Upcoming Prayer: [Prayer] in X min ([Time])"*.
     - When the exact waqt arrives, a high-priority notification announces: *"It is time for [Prayer] ([Time])"* with optional Adhan chime audio.
     - Tapping **Mark Completed** records the prayer as completed for today in the local Room database, cancels the notification, refreshes the Home Screen widget, and shows a brief confirmation update.
     - Tapping **Snooze 10m** schedules a 10-minute follow-up reminder.
     - Tapping **Dismiss** clears the notification immediately.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Matches SalahMate's **Rich Islamic Geometric & Glassmorphic** design language with subtle Rub el Hizb accents and clean status pills.
  - *Color Palette & Mood*: Deep Midnight Sapphire (`#071B34`), Illuminated Gold (`#D4AF37`) for advance reminder badges, Verified Emerald (`#10B981`) for active exact alarms, and Caution Amber (`#F59E0B`) when OS permissions need attention.
  - *Component Styling & Layout*: Glassmorphic status table with structured rows for each of the 5 prayers, status indicator pills (`EXACT ALARM`, `ADVANCE + WAQT`, `WORKMANAGER SYNCED`), and accessible 48dp action buttons.

- **Interactive Feedback & Motion**:
  - Toggling any prayer or advance reminder immediately recalculates the schedule, updates `AlarmManager` and `WorkManager` queues, and animates the updated trigger timestamps in the Live Scheduled Alarms Status Table.
  - One-tap **"Sync & Verify Schedule Now"** and **"Preview & Test Notification Now"** buttons provide instant tactile and visual feedback.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Hybrid `AlarmManager` + `WorkManager` Orchestration**:
  - *Chosen Approach*: `AlarmManager` handles exact millisecond wakeups (`setExactAndAllowWhileIdle`) for both the pre-prayer reminder (request code `1100 + ordinal`) and the on-time waqt alert (request code `1000 + ordinal`). Simultaneously, `WorkManager` runs a periodic offline worker (`PeriodicWorkRequest` every 6 hours with zero network constraints) plus targeted one-time delayed workers (`OneTimeWorkRequest`) to refresh the 48-hour rolling alarm window, update the home-screen widget, and act as a safety net if exact alarms are revoked.
  - *Why*: `WorkManager` alone has a 15-minute minimum periodic interval and can be deferred slightly by Doze mode, whereas `AlarmManager` alone does not automatically self-heal if an alarm is missed while the device is powered off across days. Combining both gives exact waqt precision plus guaranteed background persistence.
  - *Alternatives Considered*: Relying solely on `WorkManager` (rejected because prayer times require minute-level precision) or relying solely on `AlarmManager` (rejected because periodic self-healing and background task guarantees are stronger with `WorkManager`).

- **Decision 2 — Direct Room Database Mutation from Notification Action**:
  - *Chosen Approach*: Tapping **Mark Completed** on a prayer notification invokes `PrayerAlarmReceiver` with `ACTION_PRAYER_MARK_COMPLETED`, which uses `goAsync()` on an IO coroutine to mark the corresponding prayer (`FAJR`, `DHUHR`, `ASR`, `MAGHRIB`, or `ISHA`) completed for today's ISO date in `SalahMateDatabase` (`PrayerLogEntity`) and refreshes `SalahMateWidgetProvider`.
  - *Why*: Allows users to log their daily prayers in a single tap from the notification shade without opening the full app UI.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌────────────────────────────────────────────────────────────────────────────┐
│                   User Preferences & Local Adhan Engine                    │
│  • DataStore Preferences (Per-Waqt toggles, Advance mins, Sound mode)      │
│  • LocalAdhanCalculator (100% offline solar calculation for Today + Tmw)   │
└───────────────────┬────────────────────────────────────┬───────────────────┘
                    │                                    │
                    ▼                                    ▼
┌───────────────────────────────────────┐  ┌─────────────────────────────────┐
│     AlarmManager Exact Scheduler      │  │   WorkManager Offline Engine    │
│  • Waqt Alarm (Req 1000 + ordinal)    │  │  • Periodic Sync (Every 6h,     │
│  • Advance Alarm (Req 1100 + ordinal) │  │    NetworkType.NOT_REQUIRED)    │
│  • setExactAndAllowWhileIdle()        │  │  • OneTime Fallback / Rollover  │
│  • setWindow() 10m graceful fallback  │  │    Worker for next prayer       │
└───────────────────┬───────────────────┘  └─────────────────┬───────────────┘
                    │                                        │
                    └───────────────────┬────────────────────┘
                                        ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                PrayerAlarmReceiver & Notification Dispatcher               │
│  • Displays High-Importance / Silent Notification with Bilingual Copy      │
│  • Action 1: Mark Completed ──► Updates Room PrayerLogEntity + Widget      │
│  • Action 2: Snooze 10m     ──► Schedules +10m Exact Snooze Alarm          │
│  • Action 3: Dismiss        ──► Cancels Notification                       │
│  • Auto-Chains Next Day's Alarms on Fire, Reboot, or Timezone Change       │
└────────────────────────────────────────────────────────────────────────────┘
```

- **Data Model & State**:
  - `ScheduledPrayerAlarmInfo`: Represents the computed live schedule for each of the 5 prayers (`prayerType`, `isEnabled`, `waqtFormatted`, `waqtTriggerMillis`, `advanceTriggerMillis`, `advanceFormatted`, `isTomorrow`), exposed as a reactive `StateFlow` in `SalahMateViewModel` for the UI status table.
  - `NotificationDiagnostics`: Tracks `hasNotificationPermission`, `canScheduleExactAlarms`, `isWorkManagerScheduled`, and bilingual status guidance.

- **Interactive Component & State Mapping**:
  - **Per-Prayer Switches & Advance Reminder Chips**: Updating any toggle persists to DataStore, triggers `PrayerNotificationScheduler.scheduleDailyPrayerAlarms(context, prefs)` and `PrayerWorkScheduler.enqueueDailySyncAndNextPrayerWork(context, prefs)`, and refreshes the live scheduled alarms list in `SalahMateViewModel`.
  - **Permission & Exact Alarm Shortcut Buttons**: Requests `Manifest.permission.POST_NOTIFICATIONS` on Android 13+ or launches `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` on Android 12+ when exact alarms are disabled, refreshing diagnostics upon return.
  - **Mark Completed Notification Button**: Dispatches `ACTION_PRAYER_MARK_COMPLETED` with `EXTRA_PRAYER_TYPE`, updates `PrayerLogEntity` for today's date in `SalahMateDao`, cancels the active notification, and updates the Home Screen widget.
