package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.preferences.UserPreferencesRepository
import com.example.core.preferences.salahMateDataStore
import com.example.domain.prayer.DailyPrayerType
import com.example.domain.prayer.LocalAdhanCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SalahMateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesRepository(context.salahMateDataStore).preferencesFlow.first()
                for (widgetId in appWidgetIds) {
                    val options = appWidgetManager.getAppWidgetOptions(widgetId)
                    val views = buildRemoteViews(context, prefs, options)
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesRepository(context.salahMateDataStore).preferencesFlow.first()
                val views = buildRemoteViews(context, prefs, newOptions)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val manager = AppWidgetManager.getInstance(context)
                val component = ComponentName(context, SalahMateWidgetProvider::class.java)
                val ids = manager.getAppWidgetIds(component)
                if (ids.isNotEmpty()) {
                    val prefs = UserPreferencesRepository(context.salahMateDataStore).preferencesFlow.first()
                    for (id in ids) {
                        val options = manager.getAppWidgetOptions(id)
                        manager.updateAppWidget(id, buildRemoteViews(context, prefs, options))
                    }
                }
            }
        }

        private fun buildRemoteViews(
            context: Context,
            prefs: com.example.core.preferences.UserPreferencesState,
            options: Bundle?
        ): RemoteViews {
            val schedule = LocalAdhanCalculator.calculateLocalSchedule(prefs)
            val nextStatus = LocalAdhanCalculator.computeNextPrayerStatus(schedule, prefs)
            val isBn = prefs.languageCode == "bn"

            val views = RemoteViews(context.packageName, R.layout.widget_salahmate)
            val currentName = nextStatus.currentPrayer?.let { if (isBn) it.titleBn else it.titleEn }
                ?: if (isBn) "ইশরাক/চাশত" else "Post-Sunrise"
            val nextName = if (isBn) nextStatus.nextPrayer.titleBn else nextStatus.nextPrayer.titleEn

            views.setTextViewText(R.id.widget_city_title, "SalahMate • ${prefs.cityName}")
            views.setTextViewText(
                R.id.widget_hijri_date,
                if (isBn) schedule.hijriDate.formattedBn else schedule.hijriDate.formattedEn
            )
            views.setTextViewText(
                R.id.widget_next_prayer_label,
                if (isBn) "পরবর্তী: $nextName" else "Next: $nextName"
            )
            views.setTextViewText(R.id.widget_next_prayer_time, nextStatus.nextPrayerTimeFormatted)
            views.setTextViewText(
                R.id.widget_countdown_note,
                if (isBn) "বর্তমান: $currentName • বাকি ~${nextStatus.remainingHours}ঘ ${nextStatus.remainingMinutes}মি"
                else "Current: $currentName • In ~${nextStatus.remainingHours}h ${nextStatus.remainingMinutes}m"
            )

            views.setTextViewText(R.id.widget_time_fajr, schedule.prayerTimeFor(DailyPrayerType.FAJR)?.formatted24h ?: "--:--")
            views.setTextViewText(R.id.widget_time_dhuhr, schedule.prayerTimeFor(DailyPrayerType.DHUHR)?.formatted24h ?: "--:--")
            views.setTextViewText(R.id.widget_time_asr, schedule.prayerTimeFor(DailyPrayerType.ASR)?.formatted24h ?: "--:--")
            views.setTextViewText(R.id.widget_time_maghrib, schedule.prayerTimeFor(DailyPrayerType.MAGHRIB)?.formatted24h ?: "--:--")
            views.setTextViewText(R.id.widget_time_isha, schedule.prayerTimeFor(DailyPrayerType.ISHA)?.formatted24h ?: "--:--")

            // Compact vs Expanded layout adaptation based on widget minHeight
            val minHeightDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110) ?: 110
            views.setViewVisibility(
                R.id.widget_schedule_row,
                if (minHeightDp < 90) View.GONE else View.VISIBLE
            )

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            return views
        }
    }
}
