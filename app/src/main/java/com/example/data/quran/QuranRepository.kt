package com.example.data.quran

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.core.database.CachedAyahEntity
import com.example.core.database.SalahMateDao
import com.example.core.database.SurahDownloadEntity
import com.example.core.network.AlQuranApiService
import com.example.core.network.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class SurahLoadResult {
    data object Loading : SurahLoadResult()
    data class Success(
        val surahMeta: SurahMeta,
        val ayahs: List<CachedAyahEntity>,
        val isFromOfflineCache: Boolean,
        val attributionNotice: String
    ) : SurahLoadResult()
    data class Error(val message: String, val surahMeta: SurahMeta) : SurahLoadResult()
}

data class QuranAudioPlaybackState(
    val isPlaying: Boolean = false,
    val isPreparing: Boolean = false,
    val currentSurahNumber: Int = 1,
    val currentAyahNumber: Int = 1,
    val totalAyahsInSurah: Int = 7,
    val continuousSurahPlayback: Boolean = true,
    val repeatCountPerAyah: Int = 1, // 1 = 1x, 3 = 3x, 5 = 5x, 0 = Infinite
    val currentRepeatIteration: Int = 1,
    val rangeStartAyah: Int = 1,
    val rangeEndAyah: Int = 7,
    val isRangeLoopEnabled: Boolean = false,
    val reciterLabel: String = "Mishary Rashid Alafasy (128kbps)",
    val errorMessage: String? = null
)

class QuranRepository(
    private val context: Context,
    private val dao: SalahMateDao,
    private val api: AlQuranApiService = NetworkModule.alQuranApi
) {
    private var mediaPlayer: MediaPlayer? = null
    private val _audioState = MutableStateFlow(QuranAudioPlaybackState())
    val audioState: StateFlow<QuranAudioPlaybackState> = _audioState.asStateFlow()

    private val activeDownloadJobs = mutableMapOf<Int, Job>()

    /**
     * Seeds pre-bundled Surahs into Room on first launch so offline reading & search work out of the box.
     */
    suspend fun ensureBundledSurahsSeeded() {
        val existingFatihah = dao.getSurahAyahsOnce(1)
        if (existingFatihah.isEmpty()) {
            SurahCatalog.BUNDLED_OFFLINE_AYAHS.forEach { (surahNum, ayahs) ->
                dao.insertCachedAyahs(ayahs)
                val meta = SurahCatalog.getSurahMeta(surahNum)
                dao.upsertSurahDownload(
                    SurahDownloadEntity(
                        surahNumber = surahNum,
                        surahNameEn = meta.nameEn,
                        ayahCount = meta.ayahCount,
                        status = "DOWNLOADED",
                        progressPercent = 100,
                        bytesUsed = ayahs.sumOf { (it.arabicUthmani.length + it.bengaliTranslation.length + it.englishTranslation.length).toLong() * 2L }
                    )
                )
            }
        }
    }

    /**
     * Loads a Surah from Room offline cache first; if not present, fetches Arabic (`quran-uthmani`),
     * Bengali (`bn.bengali`), English (`en.sahih`), and Audio (`ar.alafasy`) from AlQuran.cloud API
     * and persists into Room.
     */
    suspend fun loadSurah(surahNumber: Int, forceRefresh: Boolean = false): SurahLoadResult =
        withContext(Dispatchers.IO) {
            val meta = SurahCatalog.getSurahMeta(surahNumber)
            val cached = dao.getSurahAyahsOnce(surahNumber)
            if (cached.isNotEmpty() && !forceRefresh) {
                return@withContext SurahLoadResult.Success(
                    surahMeta = meta,
                    ayahs = cached,
                    isFromOfflineCache = true,
                    attributionNotice = cached.first().editionAttribution
                )
            }

            val responseResult = NetworkModule.withRetry {
                api.getSurahMultiEditions(surahNumber, "quran-uthmani,bn.bengali,en.sahih,ar.alafasy")
            }

            val payloads = responseResult.getOrNull()?.data
            if (payloads != null && payloads.isNotEmpty()) {
                val arabicPayload = payloads.find { it.edition?.identifier == "quran-uthmani" } ?: payloads.first()
                val bnPayload = payloads.find { it.edition?.identifier == "bn.bengali" }
                val enPayload = payloads.find { it.edition?.identifier == "en.sahih" }
                val audioPayload = payloads.find { it.edition?.identifier == "ar.alafasy" }

                val bnName = bnPayload?.edition?.name ?: "Muhiuddin Khan"
                val enName = enPayload?.edition?.englishName ?: "Saheeh International"
                val attribution = "Arabic: Tanzil Uthmani • BN: $bnName • EN: $enName • Audio: Alafasy (AlQuran.cloud)"

                val mergedAyahs = arabicPayload.ayahs.mapIndexed { idx, arAyah ->
                    val bnText = bnPayload?.ayahs?.getOrNull(idx)?.text ?: ""
                    val enText = enPayload?.ayahs?.getOrNull(idx)?.text ?: ""
                    val audioUrl = audioPayload?.ayahs?.getOrNull(idx)?.audio
                        ?: "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${arAyah.number}.mp3"

                    CachedAyahEntity(
                        surahNumber = surahNumber,
                        ayahNumberInSurah = arAyah.numberInSurah,
                        globalAyahNumber = arAyah.number,
                        juzNumber = arAyah.juz,
                        pageNumber = arAyah.page,
                        arabicUthmani = arAyah.text,
                        bengaliTranslation = bnText,
                        englishTranslation = enText,
                        audioUrl = audioUrl,
                        editionAttribution = attribution
                    )
                }

                if (mergedAyahs.isNotEmpty()) {
                    dao.insertCachedAyahs(mergedAyahs)
                    dao.upsertSurahDownload(
                        SurahDownloadEntity(
                            surahNumber = surahNumber,
                            surahNameEn = meta.nameEn,
                            ayahCount = mergedAyahs.size,
                            status = "DOWNLOADED",
                            progressPercent = 100,
                            bytesUsed = mergedAyahs.sumOf { (it.arabicUthmani.length + it.bengaliTranslation.length + it.englishTranslation.length).toLong() * 2L }
                        )
                    )
                    return@withContext SurahLoadResult.Success(
                        surahMeta = meta,
                        ayahs = mergedAyahs,
                        isFromOfflineCache = false,
                        attributionNotice = attribution
                    )
                }
            }

            // Network failed -> check bundled or cached fallback
            val fallback = cached.ifEmpty { SurahCatalog.BUNDLED_OFFLINE_AYAHS[surahNumber].orEmpty() }
            if (fallback.isNotEmpty()) {
                return@withContext SurahLoadResult.Success(
                    surahMeta = meta,
                    ayahs = fallback,
                    isFromOfflineCache = true,
                    attributionNotice = fallback.first().editionAttribution
                )
            }

            return@withContext SurahLoadResult.Error(
                message = "Unable to connect to AlQuran.cloud and Surah ${meta.nameEn} is not yet cached offline. Connect to the internet once to download this Surah.",
                surahMeta = meta
            )
        }

    fun startOfflineSurahDownload(scope: CoroutineScope, surahNumber: Int) {
        if (activeDownloadJobs[surahNumber]?.isActive == true) return
        val meta = SurahCatalog.getSurahMeta(surahNumber)
        activeDownloadJobs[surahNumber] = scope.launch(Dispatchers.IO) {
            dao.upsertSurahDownload(
                SurahDownloadEntity(
                    surahNumber = surahNumber,
                    surahNameEn = meta.nameEn,
                    ayahCount = meta.ayahCount,
                    status = "DOWNLOADING",
                    progressPercent = 25
                )
            )
            when (val res = loadSurah(surahNumber, forceRefresh = true)) {
                is SurahLoadResult.Success -> {
                    val bytes = res.ayahs.sumOf {
                        (it.arabicUthmani.length + it.bengaliTranslation.length + it.englishTranslation.length).toLong() * 2L
                    }
                    dao.upsertSurahDownload(
                        SurahDownloadEntity(
                            surahNumber = surahNumber,
                            surahNameEn = meta.nameEn,
                            ayahCount = res.ayahs.size,
                            status = "DOWNLOADED",
                            progressPercent = 100,
                            bytesUsed = bytes
                        )
                    )
                }
                is SurahLoadResult.Error -> {
                    dao.upsertSurahDownload(
                        SurahDownloadEntity(
                            surahNumber = surahNumber,
                            surahNameEn = meta.nameEn,
                            ayahCount = meta.ayahCount,
                            status = "FAILED",
                            progressPercent = 0
                        )
                    )
                }
                else -> {}
            }
        }
    }

    suspend fun cancelOrDeleteOfflineSurah(surahNumber: Int) {
        activeDownloadJobs[surahNumber]?.cancel()
        activeDownloadJobs.remove(surahNumber)
        dao.deleteCachedSurah(surahNumber)
        dao.deleteSurahDownloadRecord(surahNumber)
        val audioDir = File(context.filesDir, "quran_audio/$surahNumber")
        if (audioDir.exists()) audioDir.deleteRecursively()
    }

    // --- Quran Audio Recitation, Repeat & Range Loop Controls ---
    fun setAudioRepeatAndRange(
        repeatCountPerAyah: Int = _audioState.value.repeatCountPerAyah,
        rangeStartAyah: Int = _audioState.value.rangeStartAyah,
        rangeEndAyah: Int = _audioState.value.rangeEndAyah,
        isRangeLoopEnabled: Boolean = _audioState.value.isRangeLoopEnabled
    ) {
        val safeStart = rangeStartAyah.coerceAtLeast(1)
        val safeEnd = rangeEndAyah.coerceAtLeast(safeStart)
        _audioState.value = _audioState.value.copy(
            repeatCountPerAyah = repeatCountPerAyah.coerceIn(0, 20),
            rangeStartAyah = safeStart,
            rangeEndAyah = safeEnd,
            isRangeLoopEnabled = isRangeLoopEnabled
        )
    }

    fun playAyahAudio(
        ayahsInSurah: List<CachedAyahEntity>,
        startIndex: Int,
        continuousSurah: Boolean = true,
        repeatIteration: Int = 1
    ) {
        if (ayahsInSurah.isEmpty() || startIndex !in ayahsInSurah.indices) return
        val targetAyah = ayahsInSurah[startIndex]
        val prev = _audioState.value
        val sameSurah = prev.currentSurahNumber == targetAyah.surahNumber

        releasePlayer()
        _audioState.value = prev.copy(
            isPlaying = false,
            isPreparing = true,
            currentSurahNumber = targetAyah.surahNumber,
            currentAyahNumber = targetAyah.ayahNumberInSurah,
            totalAyahsInSurah = ayahsInSurah.size,
            continuousSurahPlayback = continuousSurah,
            currentRepeatIteration = repeatIteration,
            rangeStartAyah = if (sameSurah) prev.rangeStartAyah.coerceIn(1, ayahsInSurah.size) else 1,
            rangeEndAyah = if (sameSurah) prev.rangeEndAyah.coerceIn(1, ayahsInSurah.size) else ayahsInSurah.size,
            errorMessage = null
        )

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                val source = targetAyah.localAudioPath?.takeIf { File(it).exists() } ?: targetAyah.audioUrl
                setDataSource(source)
                setOnPreparedListener { mp ->
                    mp.start()
                    _audioState.value = _audioState.value.copy(isPlaying = true, isPreparing = false)
                }
                setOnCompletionListener {
                    val stateNow = _audioState.value
                    val maxRepeats = stateNow.repeatCountPerAyah // 0 = Infinite
                    if (maxRepeats == 0 || stateNow.currentRepeatIteration < maxRepeats) {
                        playAyahAudio(
                            ayahsInSurah = ayahsInSurah,
                            startIndex = startIndex,
                            continuousSurah = stateNow.continuousSurahPlayback,
                            repeatIteration = stateNow.currentRepeatIteration + 1
                        )
                    } else if (stateNow.isRangeLoopEnabled) {
                        val nextIdx = startIndex + 1
                        val nextAyahNum = ayahsInSurah.getOrNull(nextIdx)?.ayahNumberInSurah
                        if (nextAyahNum != null && nextAyahNum <= stateNow.rangeEndAyah) {
                            playAyahAudio(
                                ayahsInSurah = ayahsInSurah,
                                startIndex = nextIdx,
                                continuousSurah = true,
                                repeatIteration = 1
                            )
                        } else {
                            val loopStartIdx = ayahsInSurah.indexOfFirst { it.ayahNumberInSurah == stateNow.rangeStartAyah }
                                .takeIf { it >= 0 } ?: 0
                            playAyahAudio(
                                ayahsInSurah = ayahsInSurah,
                                startIndex = loopStartIdx,
                                continuousSurah = true,
                                repeatIteration = 1
                            )
                        }
                    } else if (stateNow.continuousSurahPlayback && startIndex + 1 < ayahsInSurah.size) {
                        playAyahAudio(
                            ayahsInSurah = ayahsInSurah,
                            startIndex = startIndex + 1,
                            continuousSurah = true,
                            repeatIteration = 1
                        )
                    } else {
                        _audioState.value = _audioState.value.copy(isPlaying = false, isPreparing = false)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    _audioState.value = _audioState.value.copy(
                        isPlaying = false,
                        isPreparing = false,
                        errorMessage = "Audio stream unavailable offline. Connect to Wi-Fi to stream recitation."
                    )
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            _audioState.value = _audioState.value.copy(
                isPlaying = false,
                isPreparing = false,
                errorMessage = "Unable to play recitation: ${e.localizedMessage ?: "Network unavailable"}"
            )
        }
    }

    fun togglePauseResume() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                _audioState.value = _audioState.value.copy(isPlaying = false)
            } else {
                mp.start()
                _audioState.value = _audioState.value.copy(isPlaying = true)
            }
        } catch (_: Exception) { }
    }

    fun stopAudio() {
        releasePlayer()
        _audioState.value = _audioState.value.copy(isPlaying = false, isPreparing = false, errorMessage = null)
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) { }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null
    }
}
