package com.example.core.utils

/**
 * Bilingual UI string provider supporting English ("en", default) and Bengali ("bn")
 * with instant runtime switching.
 */
data class BilingualStrings(
    val isBn: Boolean
) {
    fun tr(en: String, bn: String): String = if (isBn) bn else en

    val appTitle get() = tr("SalahMate", "সালাহমেট")
    val navHome get() = tr("Home", "হোম")
    val navPrayer get() = tr("Prayer", "নামাজ")
    val navQuran get() = tr("Quran", "কুরআন")
    val navWorship get() = tr("Worship", "ইবাদত")
    val navMore get() = tr("Tools", "টুলস")

    val currentPrayer get() = tr("Current Prayer", "বর্তমান নামাজ")
    val nextPrayer get() = tr("Next Prayer", "পরবর্তী নামাজ")
    val countdown get() = tr("Time Remaining", "বাকি সময়")
    val todaysSchedule get() = tr("Today's Five Prayers", "আজকের পাঁচ ওয়াক্ত নামাজ")
    val sunrise get() = tr("Sunrise", "সূর্যোদয়")
    val sunset get() = tr("Sunset", "সূর্যাস্ত")
    val imsak get() = tr("Imsak (Precaution)", "ইমসাক (সতর্কতা)")
    val midnight get() = tr("Islamic Midnight", "ইসলামিক মধ্যরাত")
    val lastThird get() = tr("Last Third (Tahajjud)", "শেষ তৃতীয়াংশ (তাহাজ্জুদ)")

    val fajr get() = tr("Fajr", "ফজর")
    val dhuhr get() = tr("Dhuhr", "যোহর")
    val asr get() = tr("Asr", "আসর")
    val maghrib get() = tr("Maghrib", "মাগরিব")
    val isha get() = tr("Isha", "এশা")

    val quickAccess get() = tr("Islamic Modules & Utilities", "ইসলামিক মডিউল ও সুবিধাসমূহ")
    val qibla get() = tr("Qibla Compass", "কিবলা কম্পাস")
    val tasbih get() = tr("Digital Tasbih", "ডিজিটাল তাসবীহ")
    val dua get() = tr("Dua & Adhkar", "দোয়া ও যিকির")
    val hadith get() = tr("Hadith Library", "হাদিস গ্রন্থাগার")
    val hifz get() = tr("Hifz Tracker", "হিফয ট্র্যাকার")
    val ramadan get() = tr("Ramadan & Iftar", "রমজান ও ইফতার")
    val calendar get() = tr("Hijri Calendar", "হিজরি ক্যালেন্ডার")
    val zakat get() = tr("Zakat Calculator", "যাকাত ক্যালকুলেটর")
    val tracker get() = tr("Prayer Tracker", "নামাজ ট্র্যাকার")
    val learning get() = tr("Islamic Learning", "ইসলামিক শিক্ষা")
    val mosque get() = tr("Mosque Finder", "মসজিদ খুঁজুন")
    val settings get() = tr("Settings & Backup", "সেটিংস ও ব্যাকআপ")

    val timetableDisclaimer get() = tr(
        "Astronomical calculations can differ by 1–3 minutes from local mosque or Islamic Foundation timetables. Use minute adjustments below to match your local congregation.",
        "জ্যোতির্বিজ্ঞানভিত্তিক হিসাব স্থানীয় মসজিদ বা ইসলামিক ফাউন্ডেশনের সময়সূচী থেকে ১–৩ মিনিট ভিন্ন হতে পারে। স্থানীয় জামাতের সাথে মিলাতে মিনিট সমন্বয় ব্যবহার করুন।"
    )
}
