package com.example.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.core.preferences.NotificationSoundMode
import com.example.core.preferences.UserPreferencesRepository
import com.example.core.preferences.salahMateDataStore
import com.example.widget.SalahMateWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = UserPreferencesRepository(context.salahMateDataStore)
                val prefs = prefsRepo.preferencesFlow.first()

                when (action) {
                    PrayerNotificationScheduler.ACTION_PRAYER_ALARM -> {
                        val prayerName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
                        val timeText = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_TIME_TEXT) ?: ""
                        val soundModeStr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_SOUND_MODE) ?: prefs.notificationSoundMode.name
                        val isAdvance = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_IS_ADVANCE, false)
                        val soundMode = runCatching { NotificationSoundMode.valueOf(soundModeStr) }
                            .getOrDefault(NotificationSoundMode.ADHAN_CHIME)

                        PrayerNotificationScheduler.showPrayerNotification(
                            context = context,
                            prayerName = prayerName,
                            prayerTimeFormatted = timeText,
                            soundMode = soundMode,
                            isAdvanceReminder = isAdvance
                        )
                        // Schedule next prayer / next day's alarms and refresh home-screen widget
                        PrayerNotificationScheduler.scheduleDailyPrayerAlarms(context, prefs)
                        SalahMateWidgetProvider.updateAllWidgets(context)
                    }

                    PrayerNotificationScheduler.ACTION_PRAYER_SNOOZE -> {
                        val notifId = intent.getIntExtra("notif_id", 0)
                        if (notifId != 0) NotificationManagerCompat.from(context).cancel(notifId)
                        val prayerName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
                        val soundModeStr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_SOUND_MODE) ?: prefs.notificationSoundMode.name
                        PrayerNotificationScheduler.scheduleSnoozeAlarm(context, prayerName, soundModeStr, 10)
                    }

                    PrayerNotificationScheduler.ACTION_PRAYER_DISMISS -> {
                        val notifId = intent.getIntExtra("notif_id", 0)
                        if (notifId != 0) NotificationManagerCompat.from(context).cancel(notifId)
                    }

                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED,
                    Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        // Reschedule all enabled prayer alarms after reboot, time-zone change, or update
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
