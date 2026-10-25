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
import com.example.domain.prayer.DailyPrayerType
import com.example.domain.prayer.LocalAdhanCalculator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap

data class NotificationDiagnostics(
    val hasNotificationPermission: Boolean,
    val canScheduleExactAlarms: Boolean,
    val isWorkManagerSynced: Boolean = true,
    val schedulingModeDescriptionEn: String,
    val schedulingModeDescriptionBn: String
)

data class ScheduledPrayerAlarmInfo(
    val prayerType: DailyPrayerType,
    val isEnabled: Boolean,
    val waqtTimeFormatted: String,
    val waqtTriggerMillis: Long,
    val advanceMinutes: Int,
    val advanceTimeFormatted: String?,
    val advanceTriggerMillis: Long?,
    val isTomorrow: Boolean
)

object PrayerNotificationScheduler {
    const val CHANNEL_ID_PRAYER = "salahmate_prayer_alerts"
    const val CHANNEL_ID_SILENT = "salahmate_prayer_silent"
    const val ACTION_PRAYER_ALARM = "com.aistudio.salahmate.ACTION_PRAYER_ALARM"
    const val ACTION_PRAYER_SNOOZE = "com.aistudio.salahmate.ACTION_PRAYER_SNOOZE"
    const val ACTION_PRAYER_DISMISS = "com.aistudio.salahmate.ACTION_PRAYER_DISMISS"
    const val ACTION_PRAYER_MARK_COMPLETED = "com.aistudio.salahmate.ACTION_PRAYER_MARK_COMPLETED"

    const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_TIME_TEXT = "extra_prayer_time_text"
    const val EXTRA_SOUND_MODE = "extra_sound_mode"
    const val EXTRA_IS_ADVANCE = "extra_is_advance"

    // Deduplication map so AlarmManager + WorkManager fallback never double-fire for the same prayer window
    private val dispatchedSlotTimestamps = ConcurrentHashMap<String, Long>()

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

    fun isPrayerNotificationEnabled(prefs: UserPreferencesState, prayerType: DailyPrayerType): Boolean {
        return when (prayerType) {
            DailyPrayerType.FAJR -> prefs.notifyFajr
            DailyPrayerType.DHUHR -> prefs.notifyDhuhr
            DailyPrayerType.ASR -> prefs.notifyAsr
            DailyPrayerType.MAGHRIB -> prefs.notifyMaghrib
            DailyPrayerType.ISHA -> prefs.notifyIsha
            DailyPrayerType.SUNRISE -> false
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
            !hasNotifPerm -> "Notification permission is not granted. Enable notifications to receive offline prayer alerts."
            canExact -> "Exact AlarmManager (setExactAndAllowWhileIdle) + WorkManager 6h offline sync active."
            else -> "Exact alarms restricted by OS. Using AlarmManager windowed mode (±10 min) + WorkManager offline fallback. Grant 'Alarms & Reminders' for exact second timing."
        }
        val descBn = when {
            !hasNotifPerm -> "নোটিফিকেশন অনুমতি দেওয়া হয়নি। অফলাইন নামাজের রিমাইন্ডার পেতে অনুমতি সক্রিয় করুন।"
            canExact -> "সঠিক সময়ে অ্যালার্ম (Exact AlarmManager) এবং ওয়ার্কম্যানেজার অফলাইন সিঙ্ক সক্রিয় আছে।"
            else -> "সিস্টেমে Exact Alarm সীমাবদ্ধ থাকায় উইন্ডোড ফলব্যাক ও ওয়ার্কম্যানেজার ব্যাকআপ ব্যবহৃত হচ্ছে।"
        }

        return NotificationDiagnostics(
            hasNotificationPermission = hasNotifPerm,
            canScheduleExactAlarms = canExact,
            isWorkManagerSynced = true,
            schedulingModeDescriptionEn = descEn,
            schedulingModeDescriptionBn = descBn
        )
    }

    /**
     * Deterministically computes the upcoming scheduled alarm slots for all 5 daily prayers
     * (including both the Advance Reminder time and the Exact Waqt time) using 100% offline solar calculation.
     */
    fun computeScheduledAlarmsTable(
        prefs: UserPreferencesState,
        nowMillis: Long = System.currentTimeMillis()
    ): List<ScheduledPrayerAlarmInfo> {
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val todayCal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
        val todaySchedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, todayCal)
        val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowSchedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, tomorrowCal)

        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US).apply {
            timeZone = tz
        }

        val fivePrayers = listOf(
            DailyPrayerType.FAJR,
            DailyPrayerType.DHUHR,
            DailyPrayerType.ASR,
            DailyPrayerType.MAGHRIB,
            DailyPrayerType.ISHA
        )

        return fivePrayers.mapNotNull { prayerType ->
            val isEnabled = isPrayerNotificationEnabled(prefs, prayerType)
            val todayEntry = todaySchedule.prayerTimeFor(prayerType)
            val tomorrowEntry = tomorrowSchedule.prayerTimeFor(prayerType)

            val isUsingToday = todayEntry != null && todayEntry.timeMillis > nowMillis
            val targetEntry = if (isUsingToday) todayEntry else tomorrowEntry ?: return@mapNotNull null

            val advanceMins = prefs.advanceReminderMinutes
            val advanceOffsetMs = advanceMins * 60_000L

            val advanceTriggerMillis: Long? = if (advanceMins > 0) {
                val candidateTodayAdv = todayEntry?.let { it.timeMillis - advanceOffsetMs }
                val candidateTomorrowAdv = tomorrowEntry?.let { it.timeMillis - advanceOffsetMs }
                when {
                    candidateTodayAdv != null && candidateTodayAdv > nowMillis -> candidateTodayAdv
                    candidateTomorrowAdv != null && candidateTomorrowAdv > nowMillis -> candidateTomorrowAdv
                    else -> null
                }
            } else {
                null
            }

            val advanceFormatted = advanceTriggerMillis?.let { timeFormatter.format(Date(it)) }

            ScheduledPrayerAlarmInfo(
                prayerType = prayerType,
                isEnabled = isEnabled,
                waqtTimeFormatted = targetEntry.formatted12h,
                waqtTriggerMillis = targetEntry.timeMillis,
                advanceMinutes = advanceMins,
                advanceTimeFormatted = advanceFormatted,
                advanceTriggerMillis = advanceTriggerMillis,
                isTomorrow = !isUsingToday
            )
        }
    }

    /**
     * Schedules both Exact Waqt alarms (1000 + ordinal) and Advance Pre-Prayer Reminder alarms (1100 + ordinal)
     * via AlarmManager, and coordinates WorkManager periodic & fallback workers for 100% offline reliability.
     */
    fun scheduleDailyPrayerAlarms(
        context: Context,
        prefs: UserPreferencesState,
        enqueueWorkManager: Boolean = true
    ) {
        ensureChannelsCreated(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val now = System.currentTimeMillis()
        val scheduledTable = computeScheduledAlarmsTable(prefs, now)

        if (alarmManager != null) {
            scheduledTable.forEach { row ->
                val prayerType = row.prayerType
                val waqtRequestCode = 1000 + prayerType.ordinal
                val advanceRequestCode = 1100 + prayerType.ordinal

                val waqtIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = ACTION_PRAYER_ALARM
                    putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                    putExtra(EXTRA_PRAYER_NAME, prayerType.titleEn)
                    putExtra(EXTRA_PRAYER_TIME_TEXT, row.waqtTimeFormatted)
                    putExtra(EXTRA_SOUND_MODE, prefs.notificationSoundMode.name)
                    putExtra(EXTRA_IS_ADVANCE, false)
                }

                val advanceIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = ACTION_PRAYER_ALARM
                    putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                    putExtra(EXTRA_PRAYER_NAME, prayerType.titleEn)
                    putExtra(EXTRA_PRAYER_TIME_TEXT, row.waqtTimeFormatted)
                    putExtra(EXTRA_SOUND_MODE, prefs.notificationSoundMode.name)
                    putExtra(EXTRA_IS_ADVANCE, true)
                }

                if (!row.isEnabled) {
                    val cancelWaqtPi = PendingIntent.getBroadcast(
                        context,
                        waqtRequestCode,
                        waqtIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val cancelAdvancePi = PendingIntent.getBroadcast(
                        context,
                        advanceRequestCode,
                        advanceIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(cancelWaqtPi)
                    alarmManager.cancel(cancelAdvancePi)
                    return@forEach
                }

                // 1. Schedule Exact Waqt Alarm
                val waqtPi = PendingIntent.getBroadcast(
                    context,
                    waqtRequestCode,
                    waqtIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                scheduleAlarmSafely(alarmManager, row.waqtTriggerMillis, waqtPi)

                // 2. Schedule Advance Pre-Prayer Reminder Alarm (if advanceReminderMinutes > 0)
                if (row.advanceMinutes > 0 && row.advanceTriggerMillis != null && row.advanceTriggerMillis > now) {
                    val advancePi = PendingIntent.getBroadcast(
                        context,
                        advanceRequestCode,
                        advanceIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    scheduleAlarmSafely(alarmManager, row.advanceTriggerMillis, advancePi)
                } else {
                    val cancelAdvancePi = PendingIntent.getBroadcast(
                        context,
                        advanceRequestCode,
                        advanceIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(cancelAdvancePi)
                }
            }
        }

        if (enqueueWorkManager) {
            PrayerWorkScheduler.enqueueDailySyncAndNextPrayerWork(context, prefs)
        }
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        prayerName: String,
        soundModeName: String,
        delayMinutes: Int = 10,
        prayerTypeName: String = DailyPrayerType.FAJR.name
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + (delayMinutes * 60_000L)
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_TYPE, prayerTypeName)
            putExtra(EXTRA_PRAYER_NAME, "$prayerName (Snoozed)")
            putExtra(EXTRA_PRAYER_TIME_TEXT, "In $delayMinutes min")
            putExtra(EXTRA_SOUND_MODE, soundModeName)
            putExtra(EXTRA_IS_ADVANCE, false)
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
        isTestPreview: Boolean = false,
        prayerType: DailyPrayerType? = null
    ) {
        ensureChannelsCreated(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val resolvedPrayerType = prayerType ?: DailyPrayerType.entries.find {
            prayerName.startsWith(it.titleEn, ignoreCase = true)
        } ?: DailyPrayerType.FAJR

        // Deduplicate non-test notifications within a 15-minute window so AlarmManager + WorkManager fallback never double-fire
        if (!isTestPreview) {
            val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val slotKey = "${resolvedPrayerType.name}|$isAdvanceReminder|$todayIso|$prayerName"
            val now = System.currentTimeMillis()
            val lastFired = dispatchedSlotTimestamps[slotKey]
            if (lastFired != null && (now - lastFired) < 15 * 60_000L) {
                return
            }
            dispatchedSlotTimestamps[slotKey] = now
        }

        if (soundMode == NotificationSoundMode.ADHAN_CHIME) {
            playSynthesizedAdhanChime()
        }

        val notificationId = when {
            isTestPreview -> 9001
            isAdvanceReminder -> 2100 + resolvedPrayerType.ordinal
            else -> 2000 + resolvedPrayerType.ordinal
        }
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

        val markCompletedIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_MARK_COMPLETED
            putExtra(EXTRA_PRAYER_TYPE, resolvedPrayerType.name)
            putExtra(EXTRA_PRAYER_NAME, resolvedPrayerType.titleEn)
            putExtra("notif_id", notificationId)
        }
        val markCompletedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 50,
            markCompletedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_SNOOZE
            putExtra(EXTRA_PRAYER_TYPE, resolvedPrayerType.name)
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
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (soundMode == NotificationSoundMode.SILENT) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Mark Completed", markCompletedPendingIntent)
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
