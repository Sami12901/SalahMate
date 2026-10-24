package com.example.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

// --- AlAdhan Prayer Times API DTOs ---
data class AlAdhanResponse(
    @Json(name = "code") val code: Int = 200,
    @Json(name = "status") val status: String = "",
    @Json(name = "data") val data: AlAdhanData? = null
)

data class AlAdhanData(
    @Json(name = "timings") val timings: AlAdhanTimings? = null,
    @Json(name = "date") val date: AlAdhanDateInfo? = null,
    @Json(name = "meta") val meta: AlAdhanMeta? = null
)

data class AlAdhanTimings(
    @Json(name = "Fajr") val fajr: String = "",
    @Json(name = "Sunrise") val sunrise: String = "",
    @Json(name = "Dhuhr") val dhuhr: String = "",
    @Json(name = "Asr") val asr: String = "",
    @Json(name = "Sunset") val sunset: String = "",
    @Json(name = "Maghrib") val maghrib: String = "",
    @Json(name = "Isha") val isha: String = "",
    @Json(name = "Imsak") val imsak: String = "",
    @Json(name = "Midnight") val midnight: String = ""
)

data class AlAdhanDateInfo(
    @Json(name = "readable") val readable: String = "",
    @Json(name = "hijri") val hijri: AlAdhanHijriDate? = null
)

data class AlAdhanHijriDate(
    @Json(name = "date") val date: String = "",
    @Json(name = "day") val day: String = "",
    @Json(name = "year") val year: String = "",
    @Json(name = "month") val month: AlAdhanHijriMonth? = null
)

data class AlAdhanHijriMonth(
    @Json(name = "number") val number: Int = 1,
    @Json(name = "en") val en: String = "",
    @Json(name = "ar") val ar: String = ""
)

data class AlAdhanMeta(
    @Json(name = "timezone") val timezone: String = ""
)

interface AlAdhanApiService {
    @GET("v1/timingsByCity/{date}")
    suspend fun getTimingsByCity(
        @Path("date") dateDdMmYyyy: String,
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int,
        @Query("school") school: Int
    ): AlAdhanResponse

    @GET("v1/timings/{date}")
    suspend fun getTimingsByCoordinates(
        @Path("date") dateDdMmYyyy: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int
    ): AlAdhanResponse
}

// --- AlQuran.cloud API DTOs ---
data class AlQuranMultiEditionResponse(
    @Json(name = "code") val code: Int = 200,
    @Json(name = "status") val status: String = "",
    @Json(name = "data") val data: List<AlQuranSurahPayload>? = null
)

data class AlQuranSurahPayload(
    @Json(name = "number") val number: Int = 1,
    @Json(name = "name") val name: String = "",
    @Json(name = "englishName") val englishName: String = "",
    @Json(name = "englishNameTranslation") val englishNameTranslation: String = "",
    @Json(name = "revelationType") val revelationType: String = "",
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int = 0,
    @Json(name = "ayahs") val ayahs: List<AlQuranAyahPayload> = emptyList(),
    @Json(name = "edition") val edition: AlQuranEditionInfo? = null
)

data class AlQuranAyahPayload(
    @Json(name = "number") val number: Int = 1,
    @Json(name = "text") val text: String = "",
    @Json(name = "numberInSurah") val numberInSurah: Int = 1,
    @Json(name = "juz") val juz: Int = 1,
    @Json(name = "page") val page: Int = 1,
    @Json(name = "audio") val audio: String? = null
)

data class AlQuranEditionInfo(
    @Json(name = "identifier") val identifier: String = "",
    @Json(name = "language") val language: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "englishName") val englishName: String = "",
    @Json(name = "type") val type: String = ""
)

data class AlQuranEditionsListResponse(
    @Json(name = "code") val code: Int = 200,
    @Json(name = "data") val data: List<AlQuranEditionInfo>? = null
)

interface AlQuranApiService {
    @GET("v1/surah/{surahNumber}/editions/{editionsCsv}")
    suspend fun getSurahMultiEditions(
        @Path("surahNumber") surahNumber: Int,
        @Path("editionsCsv") editionsCsv: String = "quran-uthmani,bn.bengali,en.sahih,ar.alafasy"
    ): AlQuranMultiEditionResponse

    @GET("v1/edition")
    suspend fun getAvailableEditions(
        @Query("language") language: String? = null
    ): AlQuranEditionsListResponse
}

// --- Sunnah.com Optional Live Hadith API ---
data class SunnahApiResponse(
    @Json(name = "data") val data: List<SunnahRemoteHadith>? = null
)

data class SunnahRemoteHadith(
    @Json(name = "collection") val collection: String = "",
    @Json(name = "hadithNumber") val hadithNumber: String = "",
    @Json(name = "hadith") val hadith: List<SunnahLangBody> = emptyList()
)

data class SunnahLangBody(
    @Json(name = "lang") val lang: String = "",
    @Json(name = "body") val body: String = ""
)

interface SunnahApiService {
    @GET("v1/collections/{collection}/hadiths")
    suspend fun getCollectionHadiths(
        @Header("X-API-Key") apiKey: String,
        @Path("collection") collection: String,
        @Query("limit") limit: Int = 15,
        @Query("page") page: Int = 1
    ): SunnahApiResponse
}

object NetworkModule {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val alAdhanApi: AlAdhanApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AlAdhanApiService::class.java)
    }

    val alQuranApi: AlQuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.alquran.cloud/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AlQuranApiService::class.java)
    }

    val sunnahApi: SunnahApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.sunnah.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SunnahApiService::class.java)
    }

    /**
     * Executes a network block with exponential backoff retry on transient IO/network failures.
     */
    suspend fun <T> withRetry(
        maxAttempts: Int = 2,
        initialDelayMs: Long = 500L,
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelayMs
        for (attempt in 1..maxAttempts) {
            try {
                return Result.success(block())
            } catch (e: IOException) {
                if (attempt == maxAttempts) return Result.failure(e)
                delay(currentDelay)
                currentDelay *= 2
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        return Result.failure(IOException("Max retry attempts reached"))
    }
}
