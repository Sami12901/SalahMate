package com.example.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.core.database.PrayerLogEntity
import com.example.core.database.SalahMateDatabase
import com.example.core.preferences.NotificationSoundMode
import com.example.core.preferences.UserPreferencesRepository
import com.example.core.preferences.salahMateDataStore
import com.example.domain.prayer.DailyPrayerType
import com.example.widget.SalahMateWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        // Verify internal token for custom application actions so exported receiver cannot be spoofed by third-party apps
        val isCustomAppAction = action == PrayerNotificationScheduler.ACTION_PRAYER_ALARM ||
            action == PrayerNotificationScheduler.ACTION_PRAYER_MARK_COMPLETED ||
            action == PrayerNotificationScheduler.ACTION_PRAYER_SNOOZE ||
            action == PrayerNotificationScheduler.ACTION_PRAYER_DISMISS ||
            action == PrayerNotificationScheduler.ACTION_WORSHIP_REMINDER

        if (isCustomAppAction && !PrayerNotificationScheduler.verifyInternalAlarmToken(context, intent)) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = UserPreferencesRepository(context.salahMateDataStore)
                val prefs = prefsRepo.preferencesFlow.first()

                when (action) {
                    PrayerNotificationScheduler.ACTION_PRAYER_ALARM -> {
                        val prayerTypeName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_TYPE)
                        val prayerName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
                        val timeText = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_TIME_TEXT) ?: ""
                        val soundModeStr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_SOUND_MODE) ?: prefs.notificationSoundMode.name
                        val isAdvance = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_IS_ADVANCE, false)
                        val soundMode = runCatching { NotificationSoundMode.valueOf(soundModeStr) }
                            .getOrDefault(NotificationSoundMode.ADHAN_CHIME)
                        val prayerType = prayerTypeName?.let {
                            runCatching { DailyPrayerType.valueOf(it) }.getOrNull()
                        }

                        PrayerNotificationScheduler.showPrayerNotification(
                            context = context,
                            prayerName = prayerName,
                            prayerTimeFormatted = timeText,
                            soundMode = soundMode,
                            isAdvanceReminder = isAdvance,
                            prayerType = prayerType
                        )
                        // Schedule next prayer / next day's alarms + WorkManager fallback and refresh home-screen widget
                        PrayerNotificationScheduler.scheduleDailyPrayerAlarms(context, prefs)
                        SalahMateWidgetProvider.updateAllWidgets(context)
                    }

                    PrayerNotificationScheduler.ACTION_WORSHIP_REMINDER -> {
                        val notifId = intent.getIntExtra(PrayerNotificationScheduler.EXTRA_WORSHIP_REMINDER_ID, 2401)
                        val title = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_WORSHIP_REMINDER_TITLE)
                            ?: "SalahMate Worship Reminder"
                        val body = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_WORSHIP_REMINDER_BODY)
                            ?: "Remember Allah with authentic Masnun Adhkar."
                        PrayerNotificationScheduler.showWorshipReminderNotification(
                            context = context,
                            notificationId = notifId,
                            title = title,
                            body = body
                        )
                        PrayerNotificationScheduler.scheduleDailyPrayerAlarms(context, prefs)
                    }

                    PrayerNotificationScheduler.ACTION_PRAYER_MARK_COMPLETED -> {
                        val notifId = intent.getIntExtra("notif_id", 0)
                        if (notifId != 0) NotificationManagerCompat.from(context).cancel(notifId)

                        val prayerTypeName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_TYPE)
                        val prayerName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: ""
                        val resolvedType = prayerTypeName?.let {
                            runCatching { DailyPrayerType.valueOf(it) }.getOrNull()
                        } ?: DailyPrayerType.entries.find {
                            prayerName.startsWith(it.titleEn, ignoreCase = true)
                        } ?: DailyPrayerType.FAJR

                        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
                        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                            timeZone = tz
                        }.format(Date())

                        val dao = SalahMateDatabase.getInstance(context).salahMateDao()
                        val existing = dao.getPrayerLogForDate(dateIso) ?: PrayerLogEntity(dateIso = dateIso)
                        val updated = when (resolvedType) {
                            DailyPrayerType.FAJR -> existing.copy(fajrCompleted = true)
                            DailyPrayerType.DHUHR -> existing.copy(dhuhrCompleted = true)
                            DailyPrayerType.ASR -> existing.copy(asrCompleted = true)
                            DailyPrayerType.MAGHRIB -> existing.copy(maghribCompleted = true)
                            DailyPrayerType.ISHA -> existing.copy(ishaCompleted = true)
                            DailyPrayerType.SUNRISE -> existing
                        }.copy(updatedAtMillis = System.currentTimeMillis())

                        dao.upsertPrayerLog(updated)
                        SalahMateWidgetProvider.updateAllWidgets(context)
                    }

                    PrayerNotificationScheduler.ACTION_PRAYER_SNOOZE -> {
                        val notifId = intent.getIntExtra("notif_id", 0)
                        if (notifId != 0) NotificationManagerCompat.from(context).cancel(notifId)
                        val prayerTypeName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_TYPE) ?: DailyPrayerType.FAJR.name
                        val prayerName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
                        val soundModeStr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_SOUND_MODE) ?: prefs.notificationSoundMode.name
                        PrayerNotificationScheduler.scheduleSnoozeAlarm(
                            context = context,
                            prayerName = prayerName,
                            soundModeName = soundModeStr,
                            delayMinutes = 10,
                            prayerTypeName = prayerTypeName
                        )
                    }

                    PrayerNotificationScheduler.ACTION_PRAYER_DISMISS -> {
                        val notifId = intent.getIntExtra("notif_id", 0)
                        if (notifId != 0) NotificationManagerCompat.from(context).cancel(notifId)
                    }

                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED,
                    Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        // Reschedule all enabled prayer alarms + WorkManager offline jobs after reboot, time-zone change, or update
                        PrayerNotificationScheduler.scheduleDailyPrayerAlarms(context, prefs)
                        SalahMateWidgetProvider.updateAllWidgets(context)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
