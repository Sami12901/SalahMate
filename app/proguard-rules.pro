# Add project specific ProGuard / R8 rules here.
# Preserve source file and line numbers for readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# --- Retrofit2 & OkHttp3 ---
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Moshi JSON DTOs ---
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.example.core.network.** { *; }

# --- Room Database Entities & DAOs ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.example.core.database.** { *; }

# --- Batoulapps Adhan Astronomical Calculation Library ---
-keep class com.batoulapps.adhan.** { *; }
