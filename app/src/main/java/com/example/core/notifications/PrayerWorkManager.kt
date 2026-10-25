package com.example.core.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work. Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.core.preferences.NotificationSoundMode
import com.example.core.preferences.UserPreferencesRepository
import com.example.core.preferences.UserPreferencesState
import com.example.core.preferences.salahMateDataStore
import com.example.domain.prayer.DailyPrayerType
import com.example.widget.SalahMateWidgetProvider
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Periodic offline worker that recalculates Today + Tomorrow solar prayer schedules every 6 hours,
 * heals any missed or cleared AlarmManager alarms across midnight rollovers, and refreshes widgets.
 * Requires zero internet connectivity (NetworkType.NOT_REQUIRED).
 */
class PrayerDailySyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefsRepo = UserPreferencesRepository(applicationContext.salahMateDataStore)
            val prefs = prefsRepo.preferencesFlow.first()

            PrayerNotificationScheduler.scheduleDailyPrayerAlarms(
                context = applicationContext,
                prefs = prefs,
                enqueueWorkManager = false
            )
            PrayerWorkScheduler.scheduleNextPrayerFallbackWork(applicationContext, prefs)
            SalahMateWidgetProvider.updateAllWidgets(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

/**
 * Targeted one-time offline worker scheduled for the next upcoming prayer or advance reminder.
 * Acts as a reliable WorkManager fallback if exact alarms are restricted by the OS or deferred,
 * while deduplicating with AlarmManager so notifications never fire twice for the same slot.
 */
class PrayerAlertFallbackWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefsRepo = UserPreferencesRepository(applicationContext.salahMateDataStore)
            val prefs = prefsRepo.preferencesFlow.first()

            val prayerTypeName = inputData.getString(KEY_PRAYER_TYPE) ?: DailyPrayerType.FAJR.name
            val prayerName = inputData.getString(KEY_PRAYER_NAME) ?: "Prayer"
            val prayerTimeText = inputData.getString(KEY_PRAYER_TIME_TEXT) ?: ""
            val isAdvance = inputData.getBoolean(KEY_IS_ADVANCE, false)
            val soundModeStr = inputData.getString(KEY_SOUND_MODE) ?: prefs.notificationSoundMode.name
            val soundMode = runCatching { NotificationSoundMode.valueOf(soundModeStr) }
                .getOrDefault(prefs.notificationSoundMode)

            val prayerType = runCatching { DailyPrayerType.valueOf(prayerTypeName) }
                .getOrDefault(DailyPrayerType.FAJR)

            if (PrayerNotificationScheduler.isPrayerNotificationEnabled(prefs, prayerType)) {
                PrayerNotificationScheduler.showPrayerNotification(
                    context = applicationContext,
                    prayerName = prayerName,
                    prayerTimeFormatted = prayerTimeText,
                    soundMode = soundMode,
                    isAdvanceReminder = isAdvance,
                    prayerType = prayerType
                )
            }

            // Re-sync AlarmManager & chain the next upcoming prayer worker
            PrayerNotificationScheduler.scheduleDailyPrayerAlarms(
                context = applicationContext,
                prefs = prefs,
                enqueueWorkManager = false
            )
            PrayerWorkScheduler.scheduleNextPrayerFallbackWork(applicationContext, prefs)
            SalahMateWidgetProvider.updateAllWidgets(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_PRAYER_TYPE = "key_prayer_type"
        const val KEY_PRAYER_NAME = "key_prayer_name"
        const val KEY_PRAYER_TIME_TEXT = "key_prayer_time_text"
        const val KEY_IS_ADVANCE = "key_is_advance"
        const val KEY_SOUND_MODE = "key_sound_mode"
    }
}

/**
 * Coordinates WorkManager periodic sync jobs and targeted next-prayer fallback jobs
 * with zero network dependencies so notifications work 100% offline.
 */
object PrayerWorkScheduler {
    const val UNIQUE_PERIODIC_SYNC_WORK = "salahmate_daily_prayer_sync"
    const val UNIQUE_NEXT_PRAYER_FALLBACK_WORK = "salahmate_next_prayer_fallback"

    fun enqueueDailySyncAndNextPrayerWork(context: Context, prefs: UserPreferencesState): Boolean {
        val periodicOk = enqueuePeriodicDailySync(context)
        val fallbackOk = scheduleNextPrayerFallbackWork(context, prefs)
        return periodicOk || fallbackOk
    }

    fun enqueuePeriodicDailySync(context: Context): Boolean {
        return try {
            val offlineConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<PrayerDailySyncWorker>(
                6, TimeUnit.HOURS
            )
                .setConstraints(offlineConstraints)
                .addTag(UNIQUE_PERIODIC_SYNC_WORK)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_SYNC_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun scheduleNextPrayerFallbackWork(
        context: Context,
        prefs: UserPreferencesState,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return try {
            val workManager = WorkManager.getInstance(context)
            val scheduledRows = PrayerNotificationScheduler.computeScheduledAlarmsTable(prefs, nowMillis)
                .filter { it.isEnabled }

            if (scheduledRows.isEmpty()) {
                workManager.cancelUniqueWork(UNIQUE_NEXT_PRAYER_FALLBACK_WORK)
                return true
            }

            // Find the earliest upcoming trigger (either an advance reminder or an exact waqt)
            data class CandidateTrigger(
                val prayerType: DailyPrayerType,
                val triggerMillis: Long,
                val timeFormatted: String,
                val isAdvance: Boolean
            )

            val candidates = mutableListOf<CandidateTrigger>()
            for (row in scheduledRows) {
                if (row.advanceTriggerMillis != null && row.advanceTriggerMillis > nowMillis) {
                    candidates.add(
                        CandidateTrigger(
                            prayerType = row.prayerType,
                            triggerMillis = row.advanceTriggerMillis,
                            timeFormatted = row.waqtTimeFormatted,
                            isAdvance = true
                        )
                    )
                }
                if (row.waqtTriggerMillis > nowMillis) {
                    candidates.add(
                        CandidateTrigger(
                            prayerType = row.prayerType,
                            triggerMillis = row.waqtTriggerMillis,
                            timeFormatted = row.waqtTimeFormatted,
                            isAdvance = false
                        )
                    )
                }
            }

            val earliest = candidates.minByOrNull { it.triggerMillis }
            if (earliest == null) {
                workManager.cancelUniqueWork(UNIQUE_NEXT_PRAYER_FALLBACK_WORK)
                return true
            }

            // Add a 15-second grace offset so exact AlarmManager fires first when permitted,
            // and WorkManager seamlessly catches it if AlarmManager was delayed or restricted.
            val delayMillis = (earliest.triggerMillis - nowMillis + 15_000L).coerceAtLeast(5_000L)

            val inputData = Data.Builder()
                .putString(PrayerAlertFallbackWorker.KEY_PRAYER_TYPE, earliest.prayerType.name)
                .putString(PrayerAlertFallbackWorker.KEY_PRAYER_NAME, earliest.prayerType.titleEn)
                .putString(PrayerAlertFallbackWorker.KEY_PRAYER_TIME_TEXT, earliest.timeFormatted)
                .putBoolean(PrayerAlertFallbackWorker.KEY_IS_ADVANCE, earliest.isAdvance)
                .putString(PrayerAlertFallbackWorker.KEY_SOUND_MODE, prefs.notificationSoundMode.name)
                .build()

            val offlineConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<PrayerAlertFallbackWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .setConstraints(offlineConstraints)
                .addTag(UNIQUE_NEXT_PRAYER_FALLBACK_WORK)
                .build()

            workManager.enqueueUniqueWork(
                UNIQUE_NEXT_PRAYER_FALLBACK_WORK,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}
