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
import java.util.UUID
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
    const val ACTION_PRAYER_ALARM = "com.samiislam.salahmate.ACTION_PRAYER_ALARM"
    const val ACTION_PRAYER_SNOOZE = "com.samiislam.salahmate.ACTION_PRAYER_SNOOZE"
    const val ACTION_PRAYER_DISMISS = "com.samiislam.salahmate.ACTION_PRAYER_DISMISS"
    const val ACTION_PRAYER_MARK_COMPLETED = "com.samiislam.salahmate.ACTION_PRAYER_MARK_COMPLETED"
    const val ACTION_WORSHIP_REMINDER = "com.samiislam.salahmate.ACTION_WORSHIP_REMINDER"

    const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_TIME_TEXT = "extra_prayer_time_text"
    const val EXTRA_SOUND_MODE = "extra_sound_mode"
    const val EXTRA_IS_ADVANCE = "extra_is_advance"
    const val EXTRA_INTERNAL_TOKEN = "extra_internal_token"
    const val EXTRA_WORSHIP_REMINDER_TITLE = "extra_worship_reminder_title"
    const val EXTRA_WORSHIP_REMINDER_BODY = "extra_worship_reminder_body"
    const val EXTRA_WORSHIP_REMINDER_ID = "extra_worship_reminder_id"

    private const val DEDUP_PREFS_NAME = "salahmate_alarm_dedup"
    private const val KEY_INTERNAL_ALARM_TOKEN = "internal_alarm_secret_token"

    // In-memory fast cache backed by persistent SharedPreferences so deduplication survives process restarts
    private val dispatchedSlotTimestamps = ConcurrentHashMap<String, Long>()

    fun getOrCreateInternalAlarmToken(context: Context): String {
        val sp = context.getSharedPreferences(DEDUP_PREFS_NAME, Context.MODE_PRIVATE)
        val existing = sp.getString(KEY_INTERNAL_ALARM_TOKEN, null)
        if (!existing.isNullOrBlank()) return existing
        val generated = UUID.randomUUID().toString()
        sp.edit().putString(KEY_INTERNAL_ALARM_TOKEN, generated).apply()
        return generated
    }

    fun verifyInternalAlarmToken(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false
        val expected = getOrCreateInternalAlarmToken(context)
        val actual = intent.getStringExtra(EXTRA_INTERNAL_TOKEN)
        return !actual.isNullOrBlank() && actual == expected
    }

    /**
     * Checks whether the given slotKey was already dispatched within [windowMs] across either
     * in-memory state or persistent SharedPreferences. If not, marks it dispatched and returns false.
     */
    @Synchronized
    fun isDuplicateOrMarkDispatched(
        context: Context,
        slotKey: String,
        nowMillis: Long = System.currentTimeMillis(),
        windowMs: Long = 15 * 60_000L
    ): Boolean {
        val sp = context.getSharedPreferences(DEDUP_PREFS_NAME, Context.MODE_PRIVATE)
        val memLast = dispatchedSlotTimestamps[slotKey]
        val diskLast = sp.getLong(slotKey, 0L).takeIf { it > 0L }
        val lastFired = maxOf(memLast ?: 0L, diskLast ?: 0L)
        if (lastFired > 0L && (nowMillis - lastFired) in 0 until windowMs) {
            return true
        }
        dispatchedSlotTimestamps[slotKey] = nowMillis
        sp.edit().putLong(slotKey, nowMillis).apply()
        return false
    }

    fun clearDeduplicationCacheForTesting(context: Context) {
        dispatchedSlotTimestamps.clear()
        context.getSharedPreferences(DEDUP_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

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
     * Schedules both Exact Waqt alarms (1000 + ordinal), Advance Pre-Prayer Reminder alarms (1100 + ordinal),
     * and optional independent Worship Reminders (1401..1403) via AlarmManager, and coordinates WorkManager.
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
        val internalToken = getOrCreateInternalAlarmToken(context)

        if (alarmManager != null) {
            scheduledTable.forEach { row ->
                val prayerType = row.prayerType
                val waqtRequestCode = 1000 + prayerType.ordinal
                val advanceRequestCode = 1100 + prayerType.ordinal

                val waqtIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    setPackage(context.packageName)
                    action = ACTION_PRAYER_ALARM
                    putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
                    putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                    putExtra(EXTRA_PRAYER_NAME, prayerType.titleEn)
                    putExtra(EXTRA_PRAYER_TIME_TEXT, row.waqtTimeFormatted)
                    putExtra(EXTRA_SOUND_MODE, prefs.notificationSoundMode.name)
                    putExtra(EXTRA_IS_ADVANCE, false)
                }

                val advanceIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    setPackage(context.packageName)
                    action = ACTION_PRAYER_ALARM
                    putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
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

            // 3. Schedule or cancel independent optional Worship Reminders (Morning Adhkar, Evening Adhkar, Jumu'ah Surah Kahf)
            scheduleOptionalWorshipReminders(context, alarmManager, prefs, scheduledTable, internalToken, now)
        }

        if (enqueueWorkManager) {
            PrayerWorkScheduler.enqueueDailySyncAndNextPrayerWork(context, prefs)
        }
    }

    private fun scheduleOptionalWorshipReminders(
        context: Context,
        alarmManager: AlarmManager,
        prefs: UserPreferencesState,
        scheduledTable: List<ScheduledPrayerAlarmInfo>,
        internalToken: String,
        nowMillis: Long
    ) {
        val fajrRow = scheduledTable.find { it.prayerType == DailyPrayerType.FAJR }
        val asrRow = scheduledTable.find { it.prayerType == DailyPrayerType.ASR }
        val dhuhrRow = scheduledTable.find { it.prayerType == DailyPrayerType.DHUHR }

        // Morning Adhkar: 25 minutes after Fajr
        val morningIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            setPackage(context.packageName)
            action = ACTION_WORSHIP_REMINDER
            putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
            putExtra(EXTRA_WORSHIP_REMINDER_ID, 2401)
            putExtra(EXTRA_WORSHIP_REMINDER_TITLE, "Morning Adhkar Reminder • সকালের যিকির")
            putExtra(EXTRA_WORSHIP_REMINDER_BODY, "Take 3 minutes for post-Fajr Morning Adhkar (Ayatul Kursi, Sayyidul Istighfar & Three Quls).")
        }
        val morningPi = PendingIntent.getBroadcast(
            context, 1401, morningIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (prefs.morningAdhkarReminderEnabled && fajrRow != null) {
            val trigger = fajrRow.waqtTriggerMillis + 25 * 60_000L
            if (trigger > nowMillis) scheduleAlarmSafely(alarmManager, trigger, morningPi)
        } else {
            alarmManager.cancel(morningPi)
        }

        // Evening Adhkar: 20 minutes after Asr
        val eveningIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            setPackage(context.packageName)
            action = ACTION_WORSHIP_REMINDER
            putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
            putExtra(EXTRA_WORSHIP_REMINDER_ID, 2402)
            putExtra(EXTRA_WORSHIP_REMINDER_TITLE, "Evening Adhkar Reminder • সন্ধ্যার যিকির")
            putExtra(EXTRA_WORSHIP_REMINDER_BODY, "Pause for afternoon/evening Masnun Adhkar and protection supplications.")
        }
        val eveningPi = PendingIntent.getBroadcast(
            context, 1402, eveningIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (prefs.eveningAdhkarReminderEnabled && asrRow != null) {
            val trigger = asrRow.waqtTriggerMillis + 20 * 60_000L
            if (trigger > nowMillis) scheduleAlarmSafely(alarmManager, trigger, eveningPi)
        } else {
            alarmManager.cancel(eveningPi)
        }

        // Jumu'ah Surah Al-Kahf: 45 minutes before Dhuhr on Fridays
        val jumuahIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            setPackage(context.packageName)
            action = ACTION_WORSHIP_REMINDER
            putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
            putExtra(EXTRA_WORSHIP_REMINDER_ID, 2403)
            putExtra(EXTRA_WORSHIP_REMINDER_TITLE, "Jumu'ah Sunnah • সূরা কাহফ ও দরূদ পাঠ")
            putExtra(EXTRA_WORSHIP_REMINDER_BODY, "Recite Surah Al-Kahf and send abundant Salawat upon the Prophet (ﷺ) on this blessed Friday.")
        }
        val jumuahPi = PendingIntent.getBroadcast(
            context, 1403, jumuahIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        if (prefs.jumuahKahfReminderEnabled && dhuhrRow != null) {
            val cal = Calendar.getInstance(tz).apply {
                timeInMillis = dhuhrRow.waqtTriggerMillis - 45 * 60_000L
            }
            var daysAdded = 0
            while ((cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY || cal.timeInMillis <= nowMillis) && daysAdded < 8) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                daysAdded++
            }
            scheduleAlarmSafely(alarmManager, cal.timeInMillis, jumuahPi)
        } else {
            alarmManager.cancel(jumuahPi)
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
        val internalToken = getOrCreateInternalAlarmToken(context)
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            setPackage(context.packageName)
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
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

    fun showWorshipReminderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String
    ) {
        ensureChannelsCreated(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val slotKey = "WORSHIP|$notificationId|$todayIso"
        if (isDuplicateOrMarkDispatched(context, slotKey)) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRAYER)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
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

        // Cross-process persistent deduplication within a 15-minute window so AlarmManager + WorkManager never double-fire
        if (!isTestPreview) {
            val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val slotKey = "${resolvedPrayerType.name}|$isAdvanceReminder|$todayIso|$prayerName"
            if (isDuplicateOrMarkDispatched(context, slotKey)) {
                return
            }
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
        val internalToken = getOrCreateInternalAlarmToken(context)

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
            setPackage(context.packageName)
            action = ACTION_PRAYER_MARK_COMPLETED
            putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
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
            setPackage(context.packageName)
            action = ACTION_PRAYER_SNOOZE
            putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
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
            setPackage(context.packageName)
            action = ACTION_PRAYER_DISMISS
            putExtra(EXTRA_INTERNAL_TOKEN, internalToken)
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
