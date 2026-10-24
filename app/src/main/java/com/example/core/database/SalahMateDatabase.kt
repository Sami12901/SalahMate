package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

@Database(
    entities = [
        CachedPrayerDayEntity::class,
        CachedAyahEntity::class,
        SurahDownloadEntity::class,
        QuranBookmarkEntity::class,
        ReadingHistoryEntity::class,
        HifzProgressEntity::class,
        PrayerLogEntity::class,
        TasbihSessionEntity::class,
        FavoriteItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SalahMateDatabase : RoomDatabase() {
    abstract fun salahMateDao(): SalahMateDao

    companion object {
        @Volatile
        private var INSTANCE: SalahMateDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe forward migration preserving all user-owned tables
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favorite_items` (`itemKey` TEXT NOT NULL, `itemType` TEXT NOT NULL, `title` TEXT NOT NULL, `isCompletedToday` INTEGER NOT NULL, `lastCompletedDateIso` TEXT NOT NULL, `savedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`itemKey`))"
                )
            }
        }

        fun getInstance(context: Context): SalahMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalahMateDatabase::class.java,
                    "salahmate_offline.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Handles structured JSON export and import for user-owned application records
 * (bookmarks, Hifz progress, prayer logs, Tasbih sessions, and favorites).
 */
object UserDataBackupManager {

    suspend fun exportToJsonString(dao: SalahMateDao): String {
        val root = JSONObject()
        root.put("app", "SalahMate")
        root.put("schemaVersion", 2)
        root.put("exportedAtMillis", System.currentTimeMillis())

        val bookmarksArray = JSONArray()
        dao.getAllBookmarksOnce().forEach { b ->
            bookmarksArray.put(
                JSONObject().apply {
                    put("surahNumber", b.surahNumber)
                    put("surahNameEn", b.surahNameEn)
                    put("surahNameAr", b.surahNameAr)
                    put("ayahNumber", b.ayahNumber)
                    put("arabicSnippet", b.arabicSnippet)
                    put("translationSnippet", b.translationSnippet)
                    put("personalNote", b.personalNote)
                    put("createdAtMillis", b.createdAtMillis)
                }
            )
        }
        root.put("bookmarks", bookmarksArray)

        val hifzArray = JSONArray()
        dao.getAllHifzOnce().forEach { h ->
            hifzArray.put(
                JSONObject().apply {
                    put("surahNumber", h.surahNumber)
                    put("surahNameEn", h.surahNameEn)
                    put("totalAyahs", h.totalAyahs)
                    put("memorizedAyahs", h.memorizedAyahs)
                    put("startAyah", h.startAyah)
                    put("targetAyah", h.targetAyah)
                    put("status", h.status)
                    put("personalNotes", h.personalNotes)
                    put("nextReviewDateIso", h.nextReviewDateIso)
                    put("lastReviewedMillis", h.lastReviewedMillis)
                }
            )
        }
        root.put("hifzProgress", hifzArray)

        val logsArray = JSONArray()
        dao.getAllPrayerLogsOnce().forEach { l ->
            logsArray.put(
                JSONObject().apply {
                    put("dateIso", l.dateIso)
                    put("fajrCompleted", l.fajrCompleted)
                    put("dhuhrCompleted", l.dhuhrCompleted)
                    put("asrCompleted", l.asrCompleted)
                    put("maghribCompleted", l.maghribCompleted)
                    put("ishaCompleted", l.ishaCompleted)
                    put("personalReflection", l.personalReflection)
                }
            )
        }
        root.put("prayerLogs", logsArray)

        val tasbihArray = JSONArray()
        dao.getAllTasbihSessionsOnce().forEach { t ->
            tasbihArray.put(
                JSONObject().apply {
                    put("phraseArabic", t.phraseArabic)
                    put("phraseTransliteration", t.phraseTransliteration)
                    put("phraseMeaningEn", t.phraseMeaningEn)
                    put("phraseMeaningBn", t.phraseMeaningBn)
                    put("currentCount", t.currentCount)
                    put("targetCount", t.targetCount)
                    put("totalLifetimeCount", t.totalLifetimeCount)
                    put("personalNote", t.personalNote)
                }
            )
        }
        root.put("tasbihSessions", tasbihArray)

        return root.toString(2)
    }

    suspend fun importFromJsonString(dao: SalahMateDao, jsonString: String): Int {
        val root = JSONObject(jsonString)
        var importedRecords = 0

        root.optJSONArray("bookmarks")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                dao.insertQuranBookmark(
                    QuranBookmarkEntity(
                        surahNumber = obj.optInt("surahNumber", 1),
                        surahNameEn = obj.optString("surahNameEn", "Al-Fatihah"),
                        surahNameAr = obj.optString("surahNameAr", ""),
                        ayahNumber = obj.optInt("ayahNumber", 1),
                        arabicSnippet = obj.optString("arabicSnippet", ""),
                        translationSnippet = obj.optString("translationSnippet", ""),
                        personalNote = obj.optString("personalNote", ""),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                    )
                )
                importedRecords++
            }
        }

        root.optJSONArray("hifzProgress")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                dao.upsertHifzProgress(
                    HifzProgressEntity(
                        surahNumber = obj.optInt("surahNumber", 1),
                        surahNameEn = obj.optString("surahNameEn", "Al-Fatihah"),
                        totalAyahs = obj.optInt("totalAyahs", 7),
                        memorizedAyahs = obj.optInt("memorizedAyahs", 0),
                        startAyah = obj.optInt("startAyah", 1),
                        targetAyah = obj.optInt("targetAyah", 7),
                        status = obj.optString("status", "LEARNING"),
                        personalNotes = obj.optString("personalNotes", ""),
                        nextReviewDateIso = obj.optString("nextReviewDateIso", ""),
                        lastReviewedMillis = obj.optLong("lastReviewedMillis", System.currentTimeMillis())
                    )
                )
                importedRecords++
            }
        }

        root.optJSONArray("prayerLogs")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val dateIso = obj.optString("dateIso", "")
                if (dateIso.isNotBlank()) {
                    dao.upsertPrayerLog(
                        PrayerLogEntity(
                            dateIso = dateIso,
                            fajrCompleted = obj.optBoolean("fajrCompleted", false),
                            dhuhrCompleted = obj.optBoolean("dhuhrCompleted", false),
                            asrCompleted = obj.optBoolean("asrCompleted", false),
                            maghribCompleted = obj.optBoolean("maghribCompleted", false),
                            ishaCompleted = obj.optBoolean("ishaCompleted", false),
                            personalReflection = obj.optString("personalReflection", "")
                        )
                    )
                    importedRecords++
                }
            }
        }

        return importedRecords
    }
}
