package com.example

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.HifzProgressEntity
import com.example.core.database.PrayerLogEntity
import com.example.core.database.QuranBookmarkEntity
import com.example.core.database.SalahMateDatabase
import com.example.core.database.TasbihSessionEntity
import com.example.core.database.UserDataBackupManager
import com.example.core.notifications.PrayerNotificationScheduler
import com.example.core.preferences.UserPreferencesState
import com.example.core.utils.BilingualStrings
import com.example.data.hadith.VerifiedIslamicContent
import com.example.data.quran.SurahCatalog
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: SalahMateDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PrayerNotificationScheduler.clearDeduplicationCacheForTesting(context)
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
    fun `room persistence and json backup export import roundtrip preserves user records and skips duplicate bookmarks`() = runBlocking {
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

        val exportedJson = UserDataBackupManager.exportToJsonString(dao = dao)
        assertTrue(exportedJson.contains("Al-Fatihah"))
        assertTrue(exportedJson.contains("Al-Ikhlas"))

        // Create second clean database and import inside Room transaction
        val secondDb = Room.inMemoryDatabaseBuilder(context, SalahMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val secondDao = secondDb.salahMateDao()

        val preview1 = UserDataBackupManager.inspectBackupPayload(secondDao, exportedJson)
        assertEquals(3, preview1.schemaVersion)
        assertEquals(1, preview1.bookmarksCount)
        assertEquals(0, preview1.duplicateBookmarksSkipped)

        val importedCount = UserDataBackupManager.importFromJsonString(
            db = secondDb,
            dao = secondDao,
            jsonString = exportedJson
        )
        // 1 bookmark + 1 hifz + 1 prayer log + 1 tasbih session = 4 records
        assertEquals(4, importedCount)

        // Importing the same payload a second time must detect and skip duplicate bookmark
        val preview2 = UserDataBackupManager.inspectBackupPayload(secondDao, exportedJson)
        assertEquals(1, preview2.duplicateBookmarksSkipped)

        val restoredBookmarks = secondDao.getAllBookmarksOnce()
        assertEquals(1, restoredBookmarks.size)
        assertEquals(5, restoredBookmarks.first().ayahNumber)

        val restoredLog = secondDao.getPrayerLogForDate("2026-10-24")
        assertEquals(5, restoredLog?.completedCount)
        secondDb.close()
    }

    @Test
    fun `encrypted backup with PBKDF2 and AES_256_GCM decrypts with valid password and rejects wrong password`() = runBlocking {
        val dao = db.salahMateDao()
        dao.insertQuranBookmark(
            QuranBookmarkEntity(
                surahNumber = 36,
                surahNameEn = "Ya-Sin",
                surahNameAr = "يس",
                ayahNumber = 1,
                arabicSnippet = "يس",
                translationSnippet = "ইয়া-সীন",
                personalNote = "Heart of the Quran"
            )
        )

        val password = "StrongSalahPassword#2026"
        val encryptedPayload = UserDataBackupManager.exportToJsonString(
            dao = dao,
            encryptionPassword = password
        )
        assertTrue(UserDataBackupManager.isEncryptedPayload(encryptedPayload))
        assertFalse(encryptedPayload.contains("Heart of the Quran"))

        // Wrong password must fail authentication check
        var wrongPasswordFailed = false
        try {
            UserDataBackupManager.decryptBackupPayload(encryptedPayload, "WrongPassword")
        } catch (_: IllegalArgumentException) {
            wrongPasswordFailed = true
        }
        assertTrue("AES-GCM authentication tag must reject wrong password", wrongPasswordFailed)

        // Correct password restores into clean DB
        val targetDb = Room.inMemoryDatabaseBuilder(context, SalahMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val targetDao = targetDb.salahMateDao()
        val count = UserDataBackupManager.importFromJsonString(
            db = targetDb,
            dao = targetDao,
            jsonString = encryptedPayload,
            password = password
        )
        assertEquals(1, count)
        assertEquals("Ya-Sin", targetDao.getAllBookmarksOnce().first().surahNameEn)
        targetDb.close()
    }

    @Test
    fun `cross_process notification deduplication and internal alarm token verification prevent duplicates and spoofing`() {
        val validToken = PrayerNotificationScheduler.getOrCreateInternalAlarmToken(context)
        assertTrue(validToken.isNotBlank())

        val validIntent = Intent(PrayerNotificationScheduler.ACTION_PRAYER_ALARM).apply {
            putExtra(PrayerNotificationScheduler.EXTRA_INTERNAL_TOKEN, validToken)
        }
        val forgedIntent = Intent(PrayerNotificationScheduler.ACTION_PRAYER_ALARM).apply {
            putExtra(PrayerNotificationScheduler.EXTRA_INTERNAL_TOKEN, "forged_external_token")
        }

        assertTrue(PrayerNotificationScheduler.verifyInternalAlarmToken(context, validIntent))
        assertFalse(PrayerNotificationScheduler.verifyInternalAlarmToken(context, forgedIntent))

        // First dispatch for (2026-10-24_FAJR_waqt) is allowed; immediate duplicate from fallback worker is blocked
        val slotKey = "2026-10-24_FAJR_waqt"
        val firstAttempt = PrayerNotificationScheduler.isDuplicateOrMarkDispatched(
            context = context,
            slotKey = slotKey
        )
        val duplicateAttempt = PrayerNotificationScheduler.isDuplicateOrMarkDispatched(
            context = context,
            slotKey = slotKey
        )
        assertFalse("First dispatch must not be marked duplicate", firstAttempt)
        assertTrue("Second dispatch for same slotKey must be suppressed", duplicateAttempt)
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
    fun `offline prayer notification scheduler computes advance exact waqt and worship reminder alarms`() {
        val prefs = UserPreferencesState(
            notifyFajr = true,
            notifyDhuhr = true,
            notifyAsr = true,
            notifyMaghrib = true,
            notifyIsha = true,
            advanceReminderMinutes = 15,
            morningAdhkarReminderEnabled = true,
            eveningAdhkarReminderEnabled = true,
            jumuahKahfReminderEnabled = true
        )
        val table = PrayerNotificationScheduler.computeScheduledAlarmsTable(prefs)
        assertEquals(5, table.size)
        table.forEach { row ->
            assertTrue(row.isEnabled)
            assertTrue(row.waqtTriggerMillis > 0L)
            assertEquals(15, row.advanceMinutes)
            assertTrue((row.advanceTriggerMillis ?: 0L) > 0L)
        }

        PrayerNotificationScheduler.scheduleDailyPrayerAlarms(
            context = context,
            prefs = prefs,
            enqueueWorkManager = false
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        // 5 exact waqt + 5 advance + 3 worship reminders (Morning Adhkar, Evening Adhkar, Jumu'ah Kahf) = 13 alarms
        assertEquals(13, shadowAlarmManager.scheduledAlarms.size)
    }
}
