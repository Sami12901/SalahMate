package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SalahMateDao {

    // --- Cached Prayer Days (AlAdhan Fallback & Comparison) ---
    @Query("SELECT * FROM cached_prayer_days WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCachedPrayerDay(cacheKey: String): CachedPrayerDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedPrayerDay(entity: CachedPrayerDayEntity)

    // --- Cached Quran Ayahs & Downloads ---
    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumberInSurah ASC")
    fun observeSurahAyahs(surahNumber: Int): Flow<List<CachedAyahEntity>>

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumberInSurah ASC")
    suspend fun getSurahAyahsOnce(surahNumber: Int): List<CachedAyahEntity>

    @Query("SELECT * FROM cached_ayahs WHERE bengaliTranslation LIKE '%' || :query || '%' OR englishTranslation LIKE '%' || :query || '%' OR arabicUthmani LIKE '%' || :query || '%' LIMIT 60")
    suspend fun searchCachedAyahs(query: String): List<CachedAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedAyahs(ayahs: List<CachedAyahEntity>)

    @Query("DELETE FROM cached_ayahs WHERE surahNumber = :surahNumber")
    suspend fun deleteCachedSurah(surahNumber: Int)

    @Query("SELECT COUNT(*) FROM cached_ayahs")
    fun observeTotalCachedAyahsCount(): Flow<Int>

    @Query("SELECT * FROM surah_downloads ORDER BY surahNumber ASC")
    fun observeSurahDownloads(): Flow<List<SurahDownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSurahDownload(download: SurahDownloadEntity)

    @Query("DELETE FROM surah_downloads WHERE surahNumber = :surahNumber")
    suspend fun deleteSurahDownloadRecord(surahNumber: Int)

    // --- Quran Bookmarks & Reading History ---
    @Query("SELECT * FROM quran_bookmarks ORDER BY createdAtMillis DESC")
    fun observeQuranBookmarks(): Flow<List<QuranBookmarkEntity>>

    @Query("SELECT * FROM quran_bookmarks ORDER BY createdAtMillis DESC")
    suspend fun getAllBookmarksOnce(): List<QuranBookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranBookmark(bookmark: QuranBookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE id = :id")
    suspend fun deleteQuranBookmark(id: Long)

    @Query("SELECT * FROM reading_history ORDER BY dateIso DESC LIMIT 30")
    fun observeReadingHistory(): Flow<List<ReadingHistoryEntity>>

    @Query("SELECT * FROM reading_history ORDER BY dateIso DESC")
    suspend fun getAllReadingHistoryOnce(): List<ReadingHistoryEntity>

    @Query("SELECT * FROM reading_history WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getReadingHistoryForDate(dateIso: String): ReadingHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReadingHistory(history: ReadingHistoryEntity)

    // --- Hifz Tracker ---
    @Query("SELECT * FROM hifz_progress ORDER BY lastReviewedMillis DESC")
    fun observeHifzProgress(): Flow<List<HifzProgressEntity>>

    @Query("SELECT * FROM hifz_progress")
    suspend fun getAllHifzOnce(): List<HifzProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHifzProgress(progress: HifzProgressEntity)

    @Query("DELETE FROM hifz_progress WHERE surahNumber = :surahNumber")
    suspend fun deleteHifzProgress(surahNumber: Int)

    // --- Prayer Tracking ---
    @Query("SELECT * FROM prayer_logs ORDER BY dateIso DESC LIMIT 90")
    fun observePrayerLogs(): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs")
    suspend fun getAllPrayerLogsOnce(): List<PrayerLogEntity>

    @Query("SELECT * FROM prayer_logs WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getPrayerLogForDate(dateIso: String): PrayerLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerLog(log: PrayerLogEntity)

    // --- Digital Tasbih ---
    @Query("SELECT * FROM tasbih_sessions ORDER BY id ASC")
    fun observeTasbihSessions(): Flow<List<TasbihSessionEntity>>

    @Query("SELECT * FROM tasbih_sessions ORDER BY id ASC")
    suspend fun getAllTasbihSessionsOnce(): List<TasbihSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasbihSession(session: TasbihSessionEntity): Long

    @Query("DELETE FROM tasbih_sessions WHERE id = :id")
    suspend fun deleteTasbihSession(id: Long)

    // --- Favorites & Adhkar Completion ---
    @Query("SELECT * FROM favorite_items")
    fun observeFavoriteItems(): Flow<List<FavoriteItemEntity>>

    @Query("SELECT * FROM favorite_items")
    suspend fun getAllFavoriteItemsOnce(): List<FavoriteItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavoriteItem(item: FavoriteItemEntity)

    @Query("DELETE FROM favorite_items WHERE itemKey = :itemKey")
    suspend fun deleteFavoriteItem(itemKey: String)
}
