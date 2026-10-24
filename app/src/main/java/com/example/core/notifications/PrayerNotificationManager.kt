package com.example.core.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.core.preferences.NotificationSoundMode
import com.example.core.preferences.UserPreferencesState
import com.example.domain.prayer.DailyPrayerSchedule
import com.example.domain.prayer.DailyPrayerType
import com.example.domain.prayer.LocalAdhanCalculator
import java.util.Calendar
import java.util.TimeZone

data class NotificationDiagnostics(
    val hasNotificationPermission: Boolean,
    val canScheduleExactAlarms: Boolean,
    val schedulingModeDescriptionEn: String,
    val schedulingModeDescriptionBn: String
)

object PrayerNotificationScheduler {
    const val CHANNEL_ID_PRAYER = "salahmate_prayer_alerts"
    const val CHANNEL_ID_SILENT = "salahmate_prayer_silent"
    const val ACTION_PRAYER_ALARM = "com.aistudio.salahmate.ACTION_PRAYER_ALARM"
    const val ACTION_PRAYER_SNOOZE = "com.aistudio.salahmate.ACTION_PRAYER_SNOOZE"
    const val ACTION_PRAYER_DISMISS = "com.aistudio.salahmate.ACTION_PRAYER_DISMISS"

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_TIME_TEXT = "extra_prayer_time_text"
    const val EXTRA_SOUND_MODE = "extra_sound_mode"
    const val EXTRA_IS_ADVANCE = "extra_is_advance"

    fun ensureChannelsCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val alertChannel = NotificationChannel(
                CHANNEL_ID_PRAYER,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(true)
            }
            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "SalahMate Silent Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent visual prayer time reminders"
                setSound(null, null)
                enableVibration(false)
            }
            nm.createNotificationChannel(alertChannel)
            nm.createNotificationChannel(silentChannel)
        }
    }

    fun getDiagnostics(context: Context): NotificationDiagnostics {
        val hasNotifPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }

        val descEn = when {
            !hasNotifPerm -> "Notification permission is not granted. Enable notifications to receive prayer alerts."
            canExact -> "Exact Alarm scheduling active (setExactAndAllowWhileIdle). Note: Extreme battery savers can still delay alarms."
            else -> "Exact alarms restricted by Android OS. Using windowed fallback (±10 min window). Grant 'Alarms & Reminders' in system settings for exact timing."
        }
        val descBn = when {
            !hasNotifPerm -> "নোটিফিকেশন অনুমতি দেওয়া হয়নি। নামাজের রিমাইন্ডার পেতে অনুমতি সক্রিয় করুন।"
            canExact -> "সঠিক সময়ে অ্যালার্ম শিডিউল সক্রিয় আছে (Exact Alarm)।"
            else -> "সিস্টেমে Exact Alarm সীমাবদ্ধ থাকায় উইন্ডোড ফলব্যাক ব্যবহৃত হচ্ছে।"
        }

        return NotificationDiagnostics(
            hasNotificationPermission = hasNotifPerm,
            canScheduleExactAlarms = canExact,
            schedulingModeDescriptionEn = descEn,
            schedulingModeDescriptionBn = descBn
        )
    }

    fun scheduleDailyPrayerAlarms(context: Context, prefs: UserPreferencesState) {
        ensureChannelsCreated(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val todayCal = Calendar.getInstance(tz)
        val todaySchedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, todayCal)
        val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowSchedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, tomorrowCal)

        val prayerToggleMap = mapOf(
            DailyPrayerType.FAJR to prefs.notifyFajr,
            DailyPrayerType.DHUHR to prefs.notifyDhuhr,
            DailyPrayerType.ASR to prefs.notifyAsr,
            DailyPrayerType.MAGHRIB to prefs.notifyMaghrib,
            DailyPrayerType.ISHA to prefs.notifyIsha
        )

        prayerToggleMap.forEach { (prayerType, isEnabled) ->
            val requestCode = 1000 + prayerType.ordinal
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALARM
                putExtra(EXTRA_PRAYER_NAME, prayerType.titleEn)
                putExtra(EXTRA_SOUND_MODE, prefs.notificationSoundMode.name)
            }

            if (!isEnabled) {
                val cancelPi = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(cancelPi)
                return@forEach
            }

            val todayEntry = todaySchedule.prayerTimeFor(prayerType)
            val tomorrowEntry = tomorrowSchedule.prayerTimeFor(prayerType)
            val targetEntry = if (todayEntry != null && todayEntry.timeMillis > now) {
                todayEntry
            } else {
                tomorrowEntry ?: return@forEach
            }

            val triggerMillis = (targetEntry.timeMillis - (prefs.advanceReminderMinutes * 60_000L)).let {
                if (it > now) it else targetEntry.timeMillis
            }

            intent.putExtra(EXTRA_PRAYER_TIME_TEXT, targetEntry.formatted12h)
            intent.putExtra(EXTRA_IS_ADVANCE, prefs.advanceReminderMinutes > 0 && triggerMillis < targetEntry.timeMillis)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarmSafely(alarmManager, triggerMillis, pendingIntent)
        }
    }

    fun scheduleSnoozeAlarm(context: Context, prayerName: String, soundModeName: String, delayMinutes: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + (delayMinutes * 60_000L)
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, "$prayerName (Snoozed)")
            putExtra(EXTRA_PRAYER_TIME_TEXT, "In $delayMinutes min")
            putExtra(EXTRA_SOUND_MODE, soundModeName)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            2999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleAlarmSafely(alarmManager, triggerMillis, pi)
    }

    private fun scheduleAlarmSafely(alarmManager: AlarmManager, triggerAtMillis: Long, operation: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
                } else {
                    // Graceful 10-minute window fallback when exact alarms are restricted
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 10 * 60_000L, operation)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    fun showPrayerNotification(
        context: Context,
        prayerName: String,
        prayerTimeFormatted: String,
        soundMode: NotificationSoundMode,
        isAdvanceReminder: Boolean = false,
        isTestPreview: Boolean = false
    ) {
        ensureChannelsCreated(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        if (soundMode == NotificationSoundMode.ADHAN_CHIME) {
            playSynthesizedAdhanChime()
        }

        val notificationId = if (isTestPreview) 9001 else (2000 + prayerName.hashCode().and(0xFF))
        val channelId = if (soundMode == NotificationSoundMode.SILENT) CHANNEL_ID_SILENT else CHANNEL_ID_PRAYER

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_SNOOZE
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_SOUND_MODE, soundMode.name)
            putExtra("notif_id", notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 100,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_DISMISS
            putExtra("notif_id", notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 200,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            isTestPreview -> "SalahMate Preview • $prayerName ($prayerTimeFormatted)"
            isAdvanceReminder -> "Upcoming Prayer: $prayerName at $prayerTimeFormatted"
            else -> "It is time for $prayerName ($prayerTimeFormatted)"
        }
        val body = when (soundMode) {
            NotificationSoundMode.ADHAN_CHIME -> "Hayya 'alas-Salah • Come to prayer ($prayerTimeFormatted)"
            NotificationSoundMode.SILENT -> "Silent prayer time reminder ($prayerTimeFormatted)"
            NotificationSoundMode.DEFAULT_TONE -> "Scheduled prayer reminder for $prayerName ($prayerTimeFormatted)"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(if (soundMode == NotificationSoundMode.SILENT) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 10m", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Plays a brief, dignified royalty-free synthesized melodic chime for Adhan preview/alerts
     * without requiring external proprietary audio files.
     */
    fun playSynthesizedAdhanChime() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 450)
        } catch (_: Exception) { }
    }
}
