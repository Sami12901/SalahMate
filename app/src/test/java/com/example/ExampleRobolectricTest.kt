package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.HifzProgressEntity
import com.example.core.database.PrayerLogEntity
import com.example.core.database.QuranBookmarkEntity
import com.example.core.database.SalahMateDatabase
import com.example.core.database.TasbihSessionEntity
import com.example.core.database.UserDataBackupManager
import com.example.core.utils.BilingualStrings
import com.example.data.hadith.VerifiedIslamicContent
import com.example.data.quran.SurahCatalog
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: SalahMateDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, SalahMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app_name in strings xml matches SalahMate`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("SalahMate", appName)
    }

    @Test
    fun `room persistence and json backup export import roundtrip preserves user records`() = runBlocking {
        val dao = db.salahMateDao()

        dao.insertQuranBookmark(
            QuranBookmarkEntity(
                surahNumber = 1,
                surahNameEn = "Al-Fatihah",
                surahNameAr = "الفاتحة",
                ayahNumber = 5,
                arabicSnippet = "إِيَّاكَ نَعْبُدُ",
                translationSnippet = "আমরা কেবল তোমারই ইবাদত করি",
                personalNote = "Core verse of worship"
            )
        )
        dao.upsertHifzProgress(
            HifzProgressEntity(
                surahNumber = 112,
                surahNameEn = "Al-Ikhlas",
                totalAyahs = 4,
                memorizedAyahs = 4,
                status = "MEMORIZED",
                personalNotes = "Solid revision"
            )
        )
        dao.upsertPrayerLog(
            PrayerLogEntity(
                dateIso = "2026-10-24",
                fajrCompleted = true,
                dhuhrCompleted = true,
                asrCompleted = true,
                maghribCompleted = true,
                ishaCompleted = true,
                personalReflection = "Alhamdulillah all 5 on time"
            )
        )
        dao.upsertTasbihSession(
            TasbihSessionEntity(
                phraseArabic = "سُبْحَانَ ٱللَّٰهِ",
                phraseTransliteration = "SubhanAllah",
                phraseMeaningEn = "Glory be to Allah",
                phraseMeaningBn = "আল্লাহ পবিত্র",
                currentCount = 33,
                targetCount = 33,
                totalLifetimeCount = 330
            )
        )

        val exportedJson = UserDataBackupManager.exportToJsonString(dao)
        assertTrue(exportedJson.contains("Al-Fatihah"))
        assertTrue(exportedJson.contains("Al-Ikhlas"))

        // Create second clean database and import
        val secondDb = Room.inMemoryDatabaseBuilder(context, SalahMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val secondDao = secondDb.salahMateDao()
        val importedCount = UserDataBackupManager.importFromJsonString(secondDao, exportedJson)
        assertEquals(3, importedCount) // 1 bookmark + 1 hifz + 1 prayer log

        val restoredBookmarks = secondDao.getAllBookmarksOnce()
        assertEquals(1, restoredBookmarks.size)
        assertEquals(5, restoredBookmarks.first().ayahNumber)

        val restoredLog = secondDao.getPrayerLogForDate("2026-10-24")
        assertEquals(5, restoredLog?.completedCount)
        secondDb.close()
    }

    @Test
    fun `surah catalog and verified content preserve arabic and bengali scripts`() {
        assertEquals(114, SurahCatalog.ALL_SURAHS.size)
        val fatihahAyahs = SurahCatalog.BUNDLED_OFFLINE_AYAHS[1].orEmpty()
        assertEquals(7, fatihahAyahs.size)
        assertTrue(fatihahAyahs.first().arabicUthmani.contains("ٱللَّهِ"))
        assertTrue(fatihahAyahs.first().bengaliTranslation.contains("আল্লাহ"))

        assertTrue(VerifiedIslamicContent.VERIFIED_HADITHS.isNotEmpty())
        assertTrue(VerifiedIslamicContent.VERIFIED_DUAS.isNotEmpty())

        val bnStrings = BilingualStrings(isBn = true)
        assertEquals("সালাহমেট", bnStrings.appTitle)
    }

    @Test
    fun `offline prayer notification scheduler computes advance and exact waqt alarms for all 5 prayers`() {
        val prefs = com.example.core.preferences.UserPreferencesState(
            notifyFajr = true,
            notifyDhuhr = true,
            notifyAsr = true,
            notifyMaghrib = true,
            notifyIsha = true,
            advanceReminderMinutes = 15
        )
        val table = com.example.core.notifications.PrayerNotificationScheduler.computeScheduledAlarmsTable(prefs)
        assertEquals(5, table.size)
        table.forEach { row ->
            assertTrue(row.isEnabled)
            assertTrue(row.waqtTriggerMillis > 0L)
            assertEquals(15, row.advanceMinutes)
            assertTrue((row.advanceTriggerMillis ?: 0L) > 0L)
        }

        com.example.core.notifications.PrayerNotificationScheduler.scheduleDailyPrayerAlarms(
            context = context,
            prefs = prefs,
            enqueueWorkManager = false
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val shadowAlarmManager = org.robolectric.Shadows.shadowOf(alarmManager)
        // 5 exact waqt alarms + 5 advance reminder alarms = 10 scheduled alarms
        assertEquals(10, shadowAlarmManager.scheduledAlarms.size)
    }
}

