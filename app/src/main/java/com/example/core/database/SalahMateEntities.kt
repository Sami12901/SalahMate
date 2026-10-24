package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_prayer_days")
data class CachedPrayerDayEntity(
    @PrimaryKey val cacheKey: String, // e.g., "2026-10-24_Dhaka_1_1"
    val dateIso: String,
    val city: String,
    val country: String,
    val methodId: Int,
    val schoolId: Int,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val sunset: String,
    val maghrib: String,
    val isha: String,
    val imsak: String,
    val midnight: String,
    val hijriDateText: String,
    val fetchedAtMillis: Long
)

@Entity(tableName = "cached_ayahs", primaryKeys = ["surahNumber", "ayahNumberInSurah"])
data class CachedAyahEntity(
    val surahNumber: Int,
    val ayahNumberInSurah: Int,
    val globalAyahNumber: Int,
    val juzNumber: Int,
    val pageNumber: Int,
    val arabicUthmani: String,
    val bengaliTranslation: String,
    val englishTranslation: String,
    val audioUrl: String,
    val localAudioPath: String? = null,
    val editionAttribution: String = "Arabic: Tanzil Uthmani • BN: Muhiuddin Khan • EN: Saheeh International",
    val cachedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "surah_downloads")
data class SurahDownloadEntity(
    @PrimaryKey val surahNumber: Int,
    val surahNameEn: String,
    val ayahCount: Int,
    val status: String, // NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED, FAILED, CANCELLED
    val progressPercent: Int = 0,
    val bytesUsed: Long = 0L,
    val includeAudio: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val surahNameEn: String,
    val surahNameAr: String,
    val ayahNumber: Int,
    val arabicSnippet: String,
    val translationSnippet: String,
    val personalNote: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val dateIso: String, // YYYY-MM-DD
    val ayahsReadCount: Int = 0,
    val minutesSpent: Int = 0,
    val lastSurahNumber: Int = 1,
    val lastAyahNumber: Int = 1,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "hifz_progress")
data class HifzProgressEntity(
    @PrimaryKey val surahNumber: Int,
    val surahNameEn: String,
    val totalAyahs: Int,
    val memorizedAyahs: Int = 0,
    val startAyah: Int = 1,
    val targetAyah: Int = 1,
    val status: String = "LEARNING", // LEARNING, NEEDS_REVISION, MEMORIZED
    val personalNotes: String = "",
    val nextReviewDateIso: String = "",
    val lastReviewedMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey val dateIso: String, // YYYY-MM-DD
    val fajrCompleted: Boolean = false,
    val dhuhrCompleted: Boolean = false,
    val asrCompleted: Boolean = false,
    val maghribCompleted: Boolean = false,
    val ishaCompleted: Boolean = false,
    val fajrJamaah: Boolean = false,
    val dhuhrJamaah: Boolean = false,
    val asrJamaah: Boolean = false,
    val maghribJamaah: Boolean = false,
    val ishaJamaah: Boolean = false,
    val personalReflection: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val completedCount: Int
        get() = listOf(fajrCompleted, dhuhrCompleted, asrCompleted, maghribCompleted, ishaCompleted).count { it }
}

@Entity(tableName = "tasbih_sessions")
data class TasbihSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phraseArabic: String,
    val phraseTransliteration: String,
    val phraseMeaningEn: String,
    val phraseMeaningBn: String,
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val totalLifetimeCount: Int = 0,
    val personalNote: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_items")
data class FavoriteItemEntity(
    @PrimaryKey val itemKey: String, // e.g., "dua_morning_1" or "hadith_bukhari_1"
    val itemType: String, // "DUA" or "HADITH"
    val title: String,
    val isCompletedToday: Boolean = false,
    val lastCompletedDateIso: String = "",
    val savedAtMillis: Long = System.currentTimeMillis()
)
