package com.example.data.quran

import com.example.core.database.CachedAyahEntity

data class SurahMeta(
    val number: Int,
    val nameArabic: String,
    val nameEn: String,
    val nameBn: String,
    val meaningEn: String,
    val meaningBn: String,
    val ayahCount: Int,
    val revelationType: String, // "Meccan" or "Medinan"
    val startJuz: Int
)

object SurahCatalog {
    // Complete 114 Surahs of the Holy Quran
    val ALL_SURAHS: List<SurahMeta> = listOf(
        SurahMeta(1, "الفاتحة", "Al-Fatihah", "আল-ফাতিহা", "The Opening", "সূচনা", 7, "Meccan", 1),
        SurahMeta(2, "البقرة", "Al-Baqarah", "আল-বাকারা", "The Cow", "বকনা-বাছুর", 286, "Medinan", 1),
        SurahMeta(3, "آل عمران", "Ali 'Imran", "আলে-ইমরান", "Family of Imran", "ইমরানের পরিবার", 200, "Medinan", 3),
        SurahMeta(4, "النساء", "An-Nisa", "আন-নিসা", "The Women", "নারী", 176, "Medinan", 4),
        SurahMeta(5, "المائدة", "Al-Ma'idah", "আল-মায়িদাহ", "The Table Spread", "খাদ্য পরিবেশিত টেবিল", 120, "Medinan", 6),
        SurahMeta(6, "الأنعام", "Al-An'am", "আল-আনআম", "The Cattle", "গৃহপালিত পশু", 165, "Meccan", 7),
        SurahMeta(7, "الأعراف", "Al-A'raf", "আল-আরাফ", "The Heights", "উঁচু স্থানসমূহ", 206, "Meccan", 8),
        SurahMeta(8, "الأنفال", "Al-Anfal", "আল-আনফাল", "The Spoils of War", "যুদ্ধলব্ধ সম্পদ", 75, "Medinan", 9),
        SurahMeta(9, "التوبة", "At-Tawbah", "আত-তাওবাহ", "The Repentance", "তওবা", 129, "Medinan", 10),
        SurahMeta(10, "يونس", "Yunus", "ইউনুস", "Jonah", "নবী ইউনুস", 109, "Meccan", 11),
        SurahMeta(11, "هود", "Hud", "হুদ", "Hud", "নবী হুদ", 123, "Meccan", 11),
        SurahMeta(12, "يوسف", "Yusuf", "ইউসুফ", "Joseph", "নবী ইউসুফ", 111, "Meccan", 12),
        SurahMeta(13, "الرعد", "Ar-Ra'd", "আর-রাদ", "The Thunder", "বজ্রনাদ", 43, "Medinan", 13),
        SurahMeta(14, "إبراهيم", "Ibrahim", "ইবরাহীম", "Abraham", "নবী ইবরাহীম", 52, "Meccan", 13),
        SurahMeta(15, "الحجر", "Al-Hijr", "আল-হিজর", "The Rocky Tract", "পাথুরে পাহাড়", 99, "Meccan", 14),
        SurahMeta(16, "النحل", "An-Nahl", "আন-নাহল", "The Bee", "মৌমাছি", 128, "Meccan", 14),
        SurahMeta(17, "الإسراء", "Al-Isra", "বনী-ইসরাঈল", "The Night Journey", "রাতের সফর", 111, "Meccan", 15),
        SurahMeta(18, "الكهف", "Al-Kahf", "আল-কাহফ", "The Cave", "গুহা", 110, "Meccan", 15),
        SurahMeta(19, "مريم", "Maryam", "মারইয়াম", "Mary", "মারইয়াম", 98, "Meccan", 16),
        SurahMeta(20, "طه", "Taha", "ত্বা-হা", "Ta-Ha", "ত্বা-হা", 135, "Meccan", 16),
        SurahMeta(21, "الأنبياء", "Al-Anbiya", "আল-আম্বিয়া", "The Prophets", "নবীগণ", 112, "Meccan", 17),
        SurahMeta(22, "الحج", "Al-Hajj", "আল-হাজ্জ", "The Pilgrimage", "হজ্জ", 78, "Medinan", 17),
        SurahMeta(23, "المؤمنون", "Al-Mu'minun", "আল-মুমিনূন", "The Believers", "মুমিনগণ", 118, "Meccan", 18),
        SurahMeta(24, "النور", "An-Nur", "আন-নূর", "The Light", "আলো", 64, "Medinan", 18),
        SurahMeta(25, "الفرقان", "Al-Furqan", "আল-ফুরকান", "The Criterion", "সত্য-মিথ্যার পার্থক্যকারী", 77, "Meccan", 18),
        SurahMeta(26, "الشعراء", "Ash-Shu'ara", "আশ-শুআরা", "The Poets", "কবিগণ", 227, "Meccan", 19),
        SurahMeta(27, "النمل", "An-Naml", "আন-নামল", "The Ant", "পিপীলিকা", 93, "Meccan", 19),
        SurahMeta(28, "القصص", "Al-Qasas", "আল-কাসাস", "The Stories", "কাহিনী", 88, "Meccan", 20),
        SurahMeta(29, "العنكبوت", "Al-'Ankabut", "আল-আনকাবূত", "The Spider", "মাকড়সা", 69, "Meccan", 20),
        SurahMeta(30, "الروم", "Ar-Rum", "আর-রূম", "The Romans", "রোমান জাতি", 60, "Meccan", 21),
        SurahMeta(31, "لقمان", "Luqman", "লুকমান", "Luqman", "জ্ঞানী লুকমান", 34, "Meccan", 21),
        SurahMeta(32, "السجدة", "As-Sajdah", "আস-সাজদাহ", "The Prostration", "সিজদা", 30, "Meccan", 21),
        SurahMeta(33, "الأحزاب", "Al-Ahzab", "আল-আহযাব", "The Combined Forces", "জোটসমূহ", 73, "Medinan", 21),
        SurahMeta(34, "سبأ", "Saba", "সাবা", "Sheba", "সাবা জাতি", 54, "Meccan", 22),
        SurahMeta(35, "فاطر", "Fatir", "ফাতির", "Originator", "আদি স্রষ্টা", 45, "Meccan", 22),
        SurahMeta(36, "يس", "Ya-Sin", "ইয়াসীন", "Ya Sin", "ইয়াসীন", 83, "Meccan", 22),
        SurahMeta(37, "الصافات", "As-Saffat", "আস-সাফফাত", "Those Ranged in Ranks", "সারিবদ্ধ ফেরেশতাগণ", 182, "Meccan", 23),
        SurahMeta(38, "ص", "Sad", "সোয়াদ", "The Letter Sad", "সোয়াদ", 88, "Meccan", 23),
        SurahMeta(39, "الزمر", "Az-Zumar", "আয-যুমার", "The Troops", "দলসমূহ", 75, "Meccan", 23),
        SurahMeta(40, "غافر", "Ghafir", "গাফির", "The Forgiver", "ক্ষমাশীল", 85, "Meccan", 24),
        SurahMeta(41, "فصلت", "Fussilat", "ফুসসিলাত", "Explained in Detail", "সুস্পষ্ট ব্যাখ্যা", 54, "Meccan", 24),
        SurahMeta(42, "الشورى", "Ash-Shuraa", "আশ-শূরা", "The Consultation", "পরামর্শ", 53, "Meccan", 25),
        SurahMeta(43, "الزخرف", "Az-Zukhruf", "আয-যুখরুফ", "The Ornaments of Gold", "স্বর্ণালঙ্কার", 89, "Meccan", 25),
        SurahMeta(44, "الدخان", "Ad-Dukhan", "আদ-দুখান", "The Smoke", "ধোঁয়া", 59, "Meccan", 25),
        SurahMeta(45, "الجاثية", "Al-Jathiyah", "আল-জাসিয়াহ", "The Crouching", "নতজানু", 37, "Meccan", 25),
        SurahMeta(46, "الأحقاف", "Al-Ahqaf", "আল-আহকাফ", "The Wind-Curved Sandhills", "বালুর পাহাড়", 35, "Meccan", 26),
        SurahMeta(47, "محمد", "Muhammad", "মুহাম্মাদ", "Muhammad", "নবী মুহাম্মাদ (ﷺ)", 38, "Medinan", 26),
        SurahMeta(48, "الفتح", "Al-Fath", "আল-ফাতহ", "The Victory", "বিজয়", 29, "Medinan", 26),
        SurahMeta(49, "الحجرات", "Al-Hujurat", "আল-হুজুরাত", "The Rooms", "বাসগৃহসমূহ", 18, "Medinan", 26),
        SurahMeta(50, "ق", "Qaf", "ক্বাফ", "The Letter Qaf", "ক্বাফ", 45, "Meccan", 26),
        SurahMeta(51, "الذاريات", "Adh-Dhariyat", "আয-যারিয়াত", "The Winnowing Winds", "বিক্ষেপকারী বাতাস", 60, "Meccan", 26),
        SurahMeta(52, "الطور", "At-Tur", "আত-তূর", "The Mount", "তূর পাহাড়", 49, "Meccan", 27),
        SurahMeta(53, "النجم", "An-Najm", "আন-নাজম", "The Star", "তারা", 62, "Meccan", 27),
        SurahMeta(54, "القمر", "Al-Qamar", "আল-কামার", "The Moon", "চন্দ্র", 55, "Meccan", 27),
        SurahMeta(55, "الرحمن", "Ar-Rahman", "আর-রাহমান", "The Beneficent", "পরম করুণাময়", 78, "Medinan", 27),
        SurahMeta(56, "الواقعة", "Al-Waqi'ah", "আল-ওয়াকিয়াহ", "The Inevitable", "নিশ্চিত ঘটনা", 96, "Meccan", 27),
        SurahMeta(57, "الحديد", "Al-Hadid", "আল-হাদীদ", "The Iron", "লোহা", 29, "Medinan", 27),
        SurahMeta(58, "المجادلة", "Al-Mujadila", "আল-মুজাদালাহ", "The Pleading Woman", "অনুযোগকারিণী", 22, "Medinan", 28),
        SurahMeta(59, "الحشر", "Al-Hashr", "আল-হাশর", "The Exile", "সমাবেশ", 24, "Medinan", 28),
        SurahMeta(60, "الممتحنة", "Al-Mumtahanah", "আল-মুমতাহিনাহ", "She that is Examined", "পরীক্ষাসাপেক্ষ নারী", 13, "Medinan", 28),
        SurahMeta(61, "الصف", "As-Saff", "আস-সাফ", "The Ranks", "সারিবদ্ধ সৈন্যদল", 14, "Medinan", 28),
        SurahMeta(62, "الجمعة", "Al-Jumu'ah", "আল-জুমুআহ", "The Congregation", "জুমার দিন", 11, "Medinan", 28),
        SurahMeta(63, "المنافقون", "Al-Munafiqun", "আল-মুনাফিকূন", "The Hypocrites", "মুনাফিকগণ", 11, "Medinan", 28),
        SurahMeta(64, "التغابن", "At-Taghabun", "আত-তাগাবুন", "The Mutual Disillusion", "মোহ অপসারণ", 18, "Medinan", 28),
        SurahMeta(65, "الطلاق", "At-Talaq", "আত-তালাক", "The Divorce", "তালাক", 12, "Medinan", 28),
        SurahMeta(66, "التحريم", "At-Tahrim", "আত-তাহরীম", "The Prohibition", "নিষিদ্ধকরণ", 12, "Medinan", 28),
        SurahMeta(67, "الملك", "Al-Mulk", "আল-মুলক", "The Sovereignty", "সার্বভৌম কর্তৃত্ব", 30, "Meccan", 29),
        SurahMeta(68, "القلم", "Al-Qalam", "আল-কলম", "The Pen", "কলম", 52, "Meccan", 29),
        SurahMeta(69, "الحاقة", "Al-Haqqah", "আল-হাক্কাহ", "The Reality", "নিশ্চিত সত্য", 52, "Meccan", 29),
        SurahMeta(70, "المعارج", "Al-Ma'arij", "আল-মাআরিজ", "The Ascending Stairways", "উন্নয়নের সোপান", 44, "Meccan", 29),
        SurahMeta(71, "نوح", "Nuh", "নূহ", "Noah", "নবী নূহ", 28, "Meccan", 29),
        SurahMeta(72, " الجن", "Al-Jinn", "আল-জিন", "The Jinn", "জিন জাতি", 28, "Meccan", 29),
        SurahMeta(73, "المزمل", "Al-Muzzammil", "আল-মুযযাম্মিল", "The Enshrouded One", "বস্ত্রাচ্ছাদিত", 20, "Meccan", 29),
        SurahMeta(74, "المدثر", "Al-Muddaththir", "আল-মুদ্দাসসির", "The Cloaked One", "চাদরাবৃত", 56, "Meccan", 29),
        SurahMeta(75, "القيامة", "Al-Qiyamah", "আল-কিয়ামাহ", "The Resurrection", "পুনরুত্থান", 40, "Meccan", 29),
        SurahMeta(76, "الإنسان", "Al-Insan", "আল-ইনসান", "The Man", "মানুষ", 31, "Medinan", 29),
        SurahMeta(77, "المرسلات", "Al-Mursalat", "আল-মুরসালাত", "The Emissaries", "প্রেরিত বাতাস", 50, "Meccan", 29),
        SurahMeta(78, "النبأ", "An-Naba", "আন-নাবা", "The Tidings", "মহাসংবাদ", 40, "Meccan", 30),
        SurahMeta(79, "النازعات", "An-Nazi'at", "আন-নাযিয়াত", "Those who drag forth", "প্রচেষ্টাকারী ফেরেশতা", 46, "Meccan", 30),
        SurahMeta(80, "عبس", "'Abasa", "আবাসা", "He Frowned", "ভ্রুকুটি করলেন", 42, "Meccan", 30),
        SurahMeta(81, "التكوير", "At-Takwir", "আত-তাকভীর", "The Overthrowing", "নিষ্প্রভকরণ", 29, "Meccan", 30),
        SurahMeta(82, "الانفطار", "Al-Infitar", "আল-ইনফিতার", "The Cleaving", "বিদীর্ণ হওয়া", 19, "Meccan", 30),
        SurahMeta(83, "المطففين", "Al-Mutaffifin", "আল-মুতাফফিফীন", "The Defrauding", "ওজনে কম দানকারী", 36, "Meccan", 30),
        SurahMeta(84, "الانشقاق", "Al-Inshiqaq", "আল-ইনশিকাক", "The Sundering", "খণ্ড-বিখণ্ড হওয়া", 25, "Meccan", 30),
        SurahMeta(85, "البروج", "Al-Buruj", "আল-বুরূজ", "The Mansions of the Stars", "নক্ষত্রপুঞ্জ", 22, "Meccan", 30),
        SurahMeta(86, "الطارق", "At-Tariq", "আত-তারিক", "The Morning Star", "রাতের আগন্তুক", 17, "Meccan", 30),
        SurahMeta(87, "الأعلى", "Al-A'la", "আল-আলা", "The Most High", "সর্বোন্নত", 19, "Meccan", 30),
        SurahMeta(88, "الغاشية", "Al-Ghashiyah", "আল-গাশিয়াহ", "The Overwhelming", "আচ্ছন্নকারী ঘটনা", 26, "Meccan", 30),
        SurahMeta(89, "الفجر", "Al-Fajr", "আল-ফজর", "The Dawn", "ভোরবেলা", 30, "Meccan", 30),
        SurahMeta(90, "البلد", "Al-Balad", "আল-বালাদ", "The City", "নগরী", 20, "Meccan", 30),
        SurahMeta(91, "الشمس", "Ash-Shams", "আশ-শামস", "The Sun", "সূর্য", 15, "Meccan", 30),
        SurahMeta(92, "الليل", "Al-Layl", "আল-লাইল", "The Night", "রাত্রি", 21, "Meccan", 30),
        SurahMeta(93, "الضحى", "Ad-Duhaa", "আদ-দুহা", "The Morning Hours", "পূর্বাহ্নের কিরণ", 11, "Meccan", 30),
        SurahMeta(94, "الشرح", "Ash-Sharh", "আল-ইনশিরাহ", "The Relief", "বক্ষ প্রশস্তকরণ", 8, "Meccan", 30),
        SurahMeta(95, "التين", "At-Tin", "আত-তীন", "The Fig", "ডুমুর", 8, "Meccan", 30),
        SurahMeta(96, "العلق", "Al-'Alaq", "আল-আলাক", "The Clot", "রক্তপিণ্ড", 19, "Meccan", 30),
        SurahMeta(97, "القدر", "Al-Qadr", "আল-কদর", "The Power", "মহিমান্বিত রাত", 5, "Meccan", 30),
        SurahMeta(98, "البينة", "Al-Bayyinah", "আল-বাইয়্যিনাহ", "The Clear Proof", "সুস্পষ্ট প্রমাণ", 8, "Medinan", 30),
        SurahMeta(99, "الزلزلة", "Az-Zalzalah", "আয-যিলযাল", "The Earthquake", "ভূমিকম্প", 8, "Medinan", 30),
        SurahMeta(100, "العاديات", "Al-'Adiyat", "আল-আদিয়াত", "The Courser", "অভিযানকারী অশ্ব", 11, "Meccan", 30),
        SurahMeta(101, "القارعة", "Al-Qari'ah", "আল-কারিআহ", "The Calamity", "মহাসংকট", 11, "Meccan", 30),
        SurahMeta(102, "التكاثر", "At-Takathur", "আত-তাকাসুর", "The Rivalry in Worldly Increase", "প্রাচুর্যের প্রতিযোগিতা", 8, "Meccan", 30),
        SurahMeta(103, "العصر", "Al-'Asr", "আল-আসর", "The Declining Day", "সময়", 3, "Meccan", 30),
        SurahMeta(104, "الهمزة", "Al-Humazah", "আল-হুমাযাহ", "The Traducer", "পরনিন্দাকারী", 9, "Meccan", 30),
        SurahMeta(105, "الفيل", "Al-Fil", "আল-ফীল", "The Elephant", "হাতি", 5, "Meccan", 30),
        SurahMeta(106, "قريش", "Quraysh", "কুরাইশ", "Quraysh", "কুরাইশ গোত্র", 4, "Meccan", 30),
        SurahMeta(107, "الماعون", "Al-Ma'un", "আল-মাউন", "The Small Kindnesses", "নিত্যপ্রয়োজনীয় বস্তু", 7, "Meccan", 30),
        SurahMeta(108, "الكوثر", "Al-Kawthar", "আল-কাওসার", "The Abundance", "প্রাচুর্যপূর্ণ ঝর্ণা", 3, "Meccan", 30),
        SurahMeta(109, "الكافرون", "Al-Kafirun", "আল-কাফিরূন", "The Disbelievers", "অবিশ্বাসীগণ", 6, "Meccan", 30),
        SurahMeta(110, "النصر", "An-Nasr", "আন-নাসর", "The Divine Support", "সাহায্য", 3, "Medinan", 30),
        SurahMeta(111, "المسد", "Al-Masad", "আল-মাসাদ", "The Palm Fiber", "খেজুরের রশি", 5, "Meccan", 30),
        SurahMeta(112, "الإخلاص", "Al-Ikhlas", "আল-ইখলাস", "The Sincerity", "একত্ববাদ", 4, "Meccan", 30),
        SurahMeta(113, "الفلق", "Al-Falaq", "আল-ফালাক", "The Daybreak", "ঊষাকাল", 5, "Meccan", 30),
        SurahMeta(114, "الناس", "An-Nas", "আন-নাস", "Mankind", "মানবজাতি", 6, "Meccan", 30)
    )

    fun getSurahMeta(number: Int): SurahMeta =
        ALL_SURAHS.find { it.number == number } ?: ALL_SURAHS.first()

    /**
     * Verified pre-bundled Uthmani Arabic, Muhiuddin Khan Bengali translation, and
     * Saheeh International English translation for essential daily Surahs so cold-start
     * offline users always have immediate Quran access even before connecting to Wi-Fi.
     */
    val BUNDLED_OFFLINE_AYAHS: Map<Int, List<CachedAyahEntity>> = mapOf(
        1 to listOf(
            CachedAyahEntity(1, 1, 1, 1, 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "শুরু করছি আল্লাহর নামে যিনি পরম করুণাময়, অতি দয়ালু।", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3"),
            CachedAyahEntity(1, 2, 2, 1, 1, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", "যাবতীয় প্রশংসা আল্লাহ তা'আলার জন্য, যিনি সকল সৃষ্টি জগতের পালনকর্তা।", "[All] praise is [due] to Allah, Lord of the worlds -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2.mp3"),
            CachedAyahEntity(1, 3, 3, 1, 1, "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "যিনি নিতান্ত মেহেরবান ও পরম দয়ালু।", "The Entirely Merciful, the Especially Merciful,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/3.mp3"),
            CachedAyahEntity(1, 4, 4, 1, 1, "مَٰلِكِ يَوْمِ ٱلدِّينِ", "যিনি বিচার দিনের মালিক।", "Sovereign of the Day of Recompense.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/4.mp3"),
            CachedAyahEntity(1, 5, 5, 1, 1, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "আমরা কেবল তোমারই ইবাদত করি এবং কেবল তোমারই সাহায্য প্রার্থনা করি।", "It is You we worship and You we ask for help.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5.mp3"),
            CachedAyahEntity(1, 6, 6, 1, 1, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "আমাদেরকে সরল পথ দেখাও,", "Guide us to the straight path -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6.mp3"),
            CachedAyahEntity(1, 7, 7, 1, 1, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "সে সমস্ত লোকের পথ, যাদেরকে তুমি নেয়ামত দান করেছ। তাদের পথ নয়, যাদের প্রতি তোমার গজব নাযিল হয়েছে এবং যারা পথভ্রষ্ট হয়েছে।", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/7.mp3")
        ),
        103 to listOf(
            CachedAyahEntity(103, 1, 6177, 30, 601, "وَٱلْعَصْرِ", "সময়ের শপথ,", "By time,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6177.mp3"),
            CachedAyahEntity(103, 2, 6178, 30, 601, "إِنَّ ٱلْإِنسَٰنَ لَفِى خُسْرٍ", "নিশ্চয় মানুষ ক্ষতিগ্রস্ততায় নিপতিত;", "Indeed, mankind is in loss,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6178.mp3"),
            CachedAyahEntity(103, 3, 6179, 30, 601, "إِلَّا ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّٰلِحَٰتِ وَتَوَاصَوْا۟ بِٱلْحَقِّ وَتَوَاصَوْا۟ بِٱلصَّبْرِ", "কিন্তু তারা নয়, যারা বিশ্বাস স্থাপন করে ও সৎকর্ম করে এবং পরস্পরকে সত্যের উপদেশ দেয় এবং পরস্পরকে ধৈর্যের উপদেশ দেয়।", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6179.mp3")
        ),
        108 to listOf(
            CachedAyahEntity(108, 1, 6205, 30, 602, "إِنَّآ أَعْطَيْنَٰكَ ٱلْكَوْثَرَ", "নিশ্চয় আমি আপনাকে কাওসার দান করেছি।", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6205.mp3"),
            CachedAyahEntity(108, 2, 6206, 30, 602, "فَصَلِّ لِرَبِّكَ وَٱنْحَرْ", "অতএব আপনার পালনকর্তার উদ্দেশ্যে নামায পড়ুন এবং কুরবানী করুন।", "So pray to your Lord and sacrifice [to Him alone].", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6206.mp3"),
            CachedAyahEntity(108, 3, 6207, 30, 602, "إِنَّ شَانِئَكَ هُوَ ٱلْأَبْتَرُ", "যারা আপনার শত্রু, তারাই তো নির্বংশ।", "Indeed, your enemy is the one cut off.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6207.mp3")
        ),
        112 to listOf(
            CachedAyahEntity(112, 1, 6222, 30, 604, "قُلْ هُوَ ٱللَّهُ أَحَدٌ", "বলুন, তিনি আল্লাহ, এক,", "Say, \"He is Allah, [who is] One,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6222.mp3"),
            CachedAyahEntity(112, 2, 6223, 30, 604, "ٱللَّهُ ٱلصَّمَدُ", "আল্লাহ অমুখাপেক্ষী,", "Allah, the Eternal Refuge.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6223.mp3"),
            CachedAyahEntity(112, 3, 6224, 30, 604, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "তিনি কাউকে জন্ম দেননি এবং কেউ তাঁকে জন্ম দেয়নি", "He neither begets nor is born,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6224.mp3"),
            CachedAyahEntity(112, 4, 6225, 30, 604, "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ", "এবং তাঁর সমতুল্য কেউ নেই।", "Nor is there to Him any equivalent.\"", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6225.mp3")
        ),
        113 to listOf(
            CachedAyahEntity(113, 1, 6226, 30, 604, "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ", "বলুন, আমি আশ্রয় গ্রহণ করছি প্রভাতের পালনকর্তার,", "Say, \"I seek refuge in the Lord of daybreak", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6226.mp3"),
            CachedAyahEntity(113, 2, 6227, 30, 604, "مِن شَرِّ مَا خَلَقَ", "তিনি যা সৃষ্টি করেছেন, তার অনিষ্ট থেকে,", "From the evil of that which He created", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6227.mp3"),
            CachedAyahEntity(113, 3, 6228, 30, 604, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "অন্ধকার রাত্রির অনিষ্ট থেকে, যখন তা সমাগত হয়,", "And from the evil of darkness when it settles", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6228.mp3"),
            CachedAyahEntity(113, 4, 6229, 30, 604, "وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِى ٱلْعُقَدِ", "গ্রন্থিতে ফুঁৎকার দিয়ে জাদুকারিণীদের অনিষ্ট থেকে", "And from the evil of the blowers in knots", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6229.mp3"),
            CachedAyahEntity(113, 5, 6230, 30, 604, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "এবং হিংসুকের অনিষ্ট থেকে যখন সে হিংসা করে।", "And from the evil of an envier when he envies.\"", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6230.mp3")
        ),
        114 to listOf(
            CachedAyahEntity(114, 1, 6231, 30, 604, "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ", "বলুন, আমি আশ্রয় গ্রহণ করিতেছি মানুষের পালনকর্তার,", "Say, \"I seek refuge in the Lord of mankind,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6231.mp3"),
            CachedAyahEntity(114, 2, 6232, 30, 604, "مَلِكِ ٱلنَّاسِ", "মানুষের অধিপতির,", "The Sovereign of mankind.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6232.mp3"),
            CachedAyahEntity(114, 3, 6233, 30, 604, "إِلَٰهِ ٱلنَّاسِ", "মানুষের মা'বুদের,", "The God of mankind,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6233.mp3"),
            CachedAyahEntity(114, 4, 6234, 30, 604, "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ", "তার অনিষ্ট থেকে, যে কুমন্ত্রণা দেয় ও আত্মগোপন করে,", "From the evil of the retreating whisperer -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6234.mp3"),
            CachedAyahEntity(114, 5, 6235, 30, 604, "ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ", "যে কুমন্ত্রণা দেয় মানুষের অন্তরে", "Who whispers [evil] into the breasts of mankind -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6235.mp3"),
            CachedAyahEntity(114, 6, 6236, 30, 604, "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ", "জ্বিনের মধ্য থেকে অথবা মানুষের মধ্য থেকে।", "From among the jinn and mankind.\"", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6236.mp3")
        )
    )
}
