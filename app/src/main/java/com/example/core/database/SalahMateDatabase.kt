package com.example.core.database

import android.content.Context
import android.util.Base64
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.preferences.UserPreferencesRepository
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

data class BackupPreviewSummary(
    val schemaVersion: Int,
    val isEncrypted: Boolean,
    val exportedAtMillis: Long,
    val bookmarksCount: Int,
    val duplicateBookmarksSkipped: Int,
    val hifzCount: Int,
    val prayerLogsCount: Int,
    val tasbihCount: Int,
    val favoritesCount: Int,
    val readingHistoryCount: Int,
    val hasUserPreferences: Boolean
) {
    val totalRecordsToImport: Int
        get() = (bookmarksCount - duplicateBookmarksSkipped).coerceAtLeast(0) +
            hifzCount + prayerLogsCount + tasbihCount + favoritesCount + readingHistoryCount +
            (if (hasUserPreferences) 1 else 0)
}

/**
 * Handles structured, versioned, transactional JSON export and import for user-owned application records
 * (bookmarks, Hifz progress, prayer logs, Tasbih sessions, favorites, reading history, and preferences),
 * with optional PBKDF2-HMAC-SHA256 + AES-256-GCM authenticated encryption and pre-restore preview inspection.
 */
object UserDataBackupManager {
    private const val PBKDF2_ITERATIONS = 65_536
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_BITS = 128
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12

    fun isEncryptedPayload(rawContent: String): Boolean {
        return runCatching {
            val obj = JSONObject(rawContent.trim())
            obj.optBoolean("encrypted", false) && obj.has("ciphertextBase64")
        }.getOrDefault(false)
    }

    fun encryptBackupJson(plainJson: String, password: String): String {
        require(password.isNotBlank()) { "Backup encryption password cannot be empty." }
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also { random.nextBytes(it) }
        val iv = ByteArray(IV_BYTES).also { random.nextBytes(it) }

        val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        keySpec.clearPassword()
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))

        return JSONObject().apply {
            put("app", "SalahMate")
            put("schemaVersion", 3)
            put("encrypted", true)
            put("kdf", "PBKDF2WithHmacSHA256")
            put("iterations", PBKDF2_ITERATIONS)
            put("cipher", "AES/GCM/NoPadding")
            put("exportedAtMillis", System.currentTimeMillis())
            put("saltBase64", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("ivBase64", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("ciphertextBase64", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
        }.toString(2)
    }

    fun decryptBackupPayload(envelopeJson: String, password: String): String {
        require(password.isNotBlank()) { "Password is required to unlock this encrypted SalahMate backup." }
        val envelope = try {
            JSONObject(envelopeJson.trim())
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid JSON format in backup file.", e)
        }
        if (envelope.optString("app") != "SalahMate" || !envelope.optBoolean("encrypted", false)) {
            throw IllegalArgumentException("Not a valid encrypted SalahMate backup envelope.")
        }

        val iterations = envelope.optInt("iterations", PBKDF2_ITERATIONS).coerceIn(10_000, 500_000)
        val salt = Base64.decode(envelope.getString("saltBase64"), Base64.NO_WRAP)
        val iv = Base64.decode(envelope.getString("ivBase64"), Base64.NO_WRAP)
        val ciphertext = Base64.decode(envelope.getString("ciphertextBase64"), Base64.NO_WRAP)

        return try {
            val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
            keySpec.clearPassword()
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            val plainBytes = cipher.doFinal(ciphertext)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalArgumentException("Incorrect password or tampered encrypted backup file (GCM authentication failed).", e)
        }
    }

    private fun parseAndValidateRootJson(rawContent: String, password: String? = null): Pair<JSONObject, Boolean> {
        val trimmed = rawContent.trim()
        require(trimmed.isNotEmpty()) { "Backup content is empty." }
        val isEncrypted = isEncryptedPayload(trimmed)
        val unwrappedJson = if (isEncrypted) {
            decryptBackupPayload(trimmed, password.orEmpty())
        } else {
            trimmed
        }
        val root = try {
            JSONObject(unwrappedJson)
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed JSON backup structure.", e)
        }
        val appId = root.optString("app", "")
        require(appId == "SalahMate") { "Unrecognized backup source ('$appId'). Expected a 'SalahMate' backup file." }
        val version = root.optInt("schemaVersion", 1)
        require(version in 1..3) { "Unsupported backup schemaVersion ($version). Supported versions: 1, 2, 3." }
        return root to isEncrypted
    }

    suspend fun inspectBackupPayload(
        dao: SalahMateDao,
        rawContent: String,
        password: String? = null
    ): BackupPreviewSummary {
        val (root, isEncrypted) = parseAndValidateRootJson(rawContent, password)
        val schemaVersion = root.optInt("schemaVersion", 2)
        val exportedAt = root.optLong("exportedAtMillis", 0L)

        val existingBookmarkKeys = dao.getAllBookmarksOnce()
            .map { "${it.surahNumber}:${it.ayahNumber}" }
            .toHashSet()

        val bookmarksArr = root.optJSONArray("bookmarks")
        var bookmarksTotal = 0
        var duplicateBookmarks = 0
        val seenInPayload = HashSet<String>()
        if (bookmarksArr != null) {
            for (i in 0 until bookmarksArr.length()) {
                val obj = bookmarksArr.optJSONObject(i) ?: continue
                val s = obj.optInt("surahNumber", -1)
                val a = obj.optInt("ayahNumber", -1)
                if (s in 1..114 && a >= 1) {
                    bookmarksTotal++
                    val key = "$s:$a"
                    if (key in existingBookmarkKeys || !seenInPayload.add(key)) {
                        duplicateBookmarks++
                    }
                }
            }
        }

        val hifzCount = root.optJSONArray("hifzProgress")?.length() ?: 0
        val prayerLogsCount = root.optJSONArray("prayerLogs")?.length() ?: 0
        val tasbihCount = root.optJSONArray("tasbihSessions")?.length() ?: 0
        val favoritesCount = root.optJSONArray("favoriteItems")?.length() ?: 0
        val readingHistoryCount = root.optJSONArray("readingHistory")?.length() ?: 0
        val hasPrefs = root.optJSONObject("userPreferences") != null

        return BackupPreviewSummary(
            schemaVersion = schemaVersion,
            isEncrypted = isEncrypted,
            exportedAtMillis = exportedAt,
            bookmarksCount = bookmarksTotal,
            duplicateBookmarksSkipped = duplicateBookmarks,
            hifzCount = hifzCount,
            prayerLogsCount = prayerLogsCount,
            tasbihCount = tasbihCount,
            favoritesCount = favoritesCount,
            readingHistoryCount = readingHistoryCount,
            hasUserPreferences = hasPrefs
        )
    }

    suspend fun exportToJsonString(
        dao: SalahMateDao,
        prefsRepo: UserPreferencesRepository? = null,
        encryptionPassword: String? = null
    ): String {
        val root = JSONObject()
        root.put("app", "SalahMate")
        root.put("schemaVersion", 3)
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
                    put("fajrJamaah", l.fajrJamaah)
                    put("dhuhrJamaah", l.dhuhrJamaah)
                    put("asrJamaah", l.asrJamaah)
                    put("maghribJamaah", l.maghribJamaah)
                    put("ishaJamaah", l.ishaJamaah)
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

        val favoritesArray = JSONArray()
        dao.getAllFavoriteItemsOnce().forEach { f ->
            favoritesArray.put(
                JSONObject().apply {
                    put("itemKey", f.itemKey)
                    put("itemType", f.itemType)
                    put("title", f.title)
                    put("isCompletedToday", f.isCompletedToday)
                    put("lastCompletedDateIso", f.lastCompletedDateIso)
                    put("savedAtMillis", f.savedAtMillis)
                }
            )
        }
        root.put("favoriteItems", favoritesArray)

        val historyArray = JSONArray()
        dao.getAllReadingHistoryOnce().forEach { r ->
            historyArray.put(
                JSONObject().apply {
                    put("dateIso", r.dateIso)
                    put("ayahsReadCount", r.ayahsReadCount)
                    put("minutesSpent", r.minutesSpent)
                    put("lastSurahNumber", r.lastSurahNumber)
                    put("lastAyahNumber", r.lastAyahNumber)
                    put("updatedAtMillis", r.updatedAtMillis)
                }
            )
        }
        root.put("readingHistory", historyArray)

        if (prefsRepo != null) {
            root.put("userPreferences", prefsRepo.exportPreferencesToJson())
        }

        val plainJson = root.toString(2)
        return if (!encryptionPassword.isNullOrBlank()) {
            encryptBackupJson(plainJson, encryptionPassword)
        } else {
            plainJson
        }
    }

    /**
     * Transactional restore that validates schema & integrity before writing,
     * skips duplicate bookmarks, and rolls back all Room writes if any record fails.
     */
    suspend fun importFromJsonString(
        db: SalahMateDatabase,
        dao: SalahMateDao,
        jsonString: String,
        prefsRepo: UserPreferencesRepository? = null,
        password: String? = null
    ): Int {
        val (root, _) = parseAndValidateRootJson(jsonString, password)
        var importedRecords = 0

        db.withTransaction {
            importedRecords = importValidatedRootIntoDao(dao, root)
        }

        root.optJSONObject("userPreferences")?.let { prefsObj ->
            if (prefsRepo != null) {
                prefsRepo.importPreferencesFromJson(prefsObj)
                importedRecords++
            }
        }

        return importedRecords
    }

    /**
     * Overload for direct DAO callers/unit tests without a RoomDatabase wrapper.
     */
    suspend fun importFromJsonString(
        dao: SalahMateDao,
        jsonString: String,
        password: String? = null
    ): Int {
        val (root, _) = parseAndValidateRootJson(jsonString, password)
        return importValidatedRootIntoDao(dao, root)
    }

    private suspend fun importValidatedRootIntoDao(dao: SalahMateDao, root: JSONObject): Int {
        var importedRecords = 0

        val existingBookmarkKeys = dao.getAllBookmarksOnce()
            .map { "${it.surahNumber}:${it.ayahNumber}" }
            .toMutableSet()

        root.optJSONArray("bookmarks")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val surahNumber = obj.optInt("surahNumber", 1).coerceIn(1, 114)
                val ayahNumber = obj.optInt("ayahNumber", 1).coerceAtLeast(1)
                val key = "$surahNumber:$ayahNumber"
                if (existingBookmarkKeys.add(key)) {
                    dao.insertQuranBookmark(
                        QuranBookmarkEntity(
                            surahNumber = surahNumber,
                            surahNameEn = obj.optString("surahNameEn", "Al-Fatihah"),
                            surahNameAr = obj.optString("surahNameAr", ""),
                            ayahNumber = ayahNumber,
                            arabicSnippet = obj.optString("arabicSnippet", ""),
                            translationSnippet = obj.optString("translationSnippet", ""),
                            personalNote = obj.optString("personalNote", ""),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                        )
                    )
                    importedRecords++
                }
            }
        }

        root.optJSONArray("hifzProgress")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                dao.upsertHifzProgress(
                    HifzProgressEntity(
                        surahNumber = obj.optInt("surahNumber", 1).coerceIn(1, 114),
                        surahNameEn = obj.optString("surahNameEn", "Al-Fatihah"),
                        totalAyahs = obj.optInt("totalAyahs", 7).coerceAtLeast(1),
                        memorizedAyahs = obj.optInt("memorizedAyahs", 0).coerceAtLeast(0),
                        startAyah = obj.optInt("startAyah", 1).coerceAtLeast(1),
                        targetAyah = obj.optInt("targetAyah", 7).coerceAtLeast(1),
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
                            fajrJamaah = obj.optBoolean("fajrJamaah", false),
                            dhuhrJamaah = obj.optBoolean("dhuhrJamaah", false),
                            asrJamaah = obj.optBoolean("asrJamaah", false),
                            maghribJamaah = obj.optBoolean("maghribJamaah", false),
                            ishaJamaah = obj.optBoolean("ishaJamaah", false),
                            personalReflection = obj.optString("personalReflection", "")
                        )
                    )
                    importedRecords++
                }
            }
        }

        val existingTasbihPhrases = dao.getAllTasbihSessionsOnce()
            .associateBy { it.phraseTransliteration }
            .toMutableMap()

        root.optJSONArray("tasbihSessions")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val translit = obj.optString("phraseTransliteration", "")
                if (translit.isNotBlank()) {
                    val existing = existingTasbihPhrases[translit]
                    val entity = TasbihSessionEntity(
                        id = existing?.id ?: 0L,
                        phraseArabic = obj.optString("phraseArabic", existing?.phraseArabic ?: ""),
                        phraseTransliteration = translit,
                        phraseMeaningEn = obj.optString("phraseMeaningEn", existing?.phraseMeaningEn ?: ""),
                        phraseMeaningBn = obj.optString("phraseMeaningBn", existing?.phraseMeaningBn ?: ""),
                        currentCount = obj.optInt("currentCount", 0).coerceAtLeast(0),
                        targetCount = obj.optInt("targetCount", 33).coerceAtLeast(1),
                        totalLifetimeCount = maxOf(
                            existing?.totalLifetimeCount ?: 0,
                            obj.optInt("totalLifetimeCount", 0)
                        ),
                        personalNote = obj.optString("personalNote", existing?.personalNote ?: "")
                    )
                    dao.upsertTasbihSession(entity)
                    importedRecords++
                }
            }
        }

        root.optJSONArray("favoriteItems")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val itemKey = obj.optString("itemKey", "")
                if (itemKey.isNotBlank()) {
                    dao.upsertFavoriteItem(
                        FavoriteItemEntity(
                            itemKey = itemKey,
                            itemType = obj.optString("itemType", "DUA"),
                            title = obj.optString("title", ""),
                            isCompletedToday = obj.optBoolean("isCompletedToday", false),
                            lastCompletedDateIso = obj.optString("lastCompletedDateIso", ""),
                            savedAtMillis = obj.optLong("savedAtMillis", System.currentTimeMillis())
                        )
                    )
                    importedRecords++
                }
            }
        }

        root.optJSONArray("readingHistory")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val dateIso = obj.optString("dateIso", "")
                if (dateIso.isNotBlank()) {
                    dao.upsertReadingHistory(
                        ReadingHistoryEntity(
                            dateIso = dateIso,
                            ayahsReadCount = obj.optInt("ayahsReadCount", 0).coerceAtLeast(0),
                            minutesSpent = obj.optInt("minutesSpent", 0).coerceAtLeast(0),
                            lastSurahNumber = obj.optInt("lastSurahNumber", 1).coerceIn(1, 114),
                            lastAyahNumber = obj.optInt("lastAyahNumber", 1).coerceAtLeast(1),
                            updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
                        )
                    )
                    importedRecords++
                }
            }
        }

        return importedRecords
    }
}
