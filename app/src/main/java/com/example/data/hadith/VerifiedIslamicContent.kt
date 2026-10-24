package com.example.data.hadith

data class VerifiedHadithItem(
    val id: String,
    val collectionNameEn: String,
    val collectionNameBn: String,
    val bookNumber: Int,
    val bookNameEn: String,
    val hadithNumber: String,
    val arabicText: String,
    val bengaliTranslation: String,
    val englishTranslation: String,
    val narratorEn: String,
    val grading: String, // Only from verified primary sources
    val sourceReference: String
)

data class VerifiedDuaItem(
    val id: String,
    val categoryEn: String,
    val categoryBn: String,
    val titleEn: String,
    val titleBn: String,
    val arabicText: String,
    val transliteration: String,
    val bengaliMeaning: String,
    val englishMeaning: String,
    val sourceReference: String,
    val recommendedCount: Int = 1
)

data class AsmaUlHusnaItem(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val meaningEn: String,
    val meaningBn: String
)

data class KalimaItem(
    val number: Int,
    val titleEn: String,
    val titleBn: String,
    val arabic: String,
    val transliteration: String,
    val meaningEn: String,
    val meaningBn: String
)

data class LearningGuideStep(
    val stepNumber: Int,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val referenceNote: String
)

object VerifiedIslamicContent {

    val HADITH_COLLECTIONS = listOf(
        "All",
        "Sahih al-Bukhari",
        "Sahih Muslim",
        "Riyad as-Salihin",
        "40 Hadith Nawawi"
    )

    val VERIFIED_HADITHS = listOf(
        VerifiedHadithItem(
            id = "hadith_bukhari_1",
            collectionNameEn = "Sahih al-Bukhari",
            collectionNameBn = "সহীহ আল-বুখারী",
            bookNumber = 1,
            bookNameEn = "Revelation (Kitab Bad' al-Wahy)",
            hadithNumber = "1",
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ",
            bengaliTranslation = "সকল কাজ নিয়তের ওপর নির্ভরশীল, এবং প্রত্যেক মানুষ তাই পাবে যা সে নিয়ত করবে। সুতরাং যার হিজরত দুনিয়া অর্জন অথবা কোনো নারীকে বিবাহ করার উদ্দেশ্যে হবে, তার হিজরত সে উদ্দেশ্যেই গণ্য হবে।",
            englishTranslation = "Actions are judged only by intentions, and every person shall have only that which they intended. Whoever emigrated for worldly gain or to marry a woman, his emigration is for that to which he emigrated.",
            narratorEn = "'Umar ibn al-Khattab (RA)",
            grading = "Sahih (Agreed Upon — Bukhari & Muslim)",
            sourceReference = "Sahih al-Bukhari, Book 1, Hadith 1; Sahih Muslim 1907"
        ),
        VerifiedHadithItem(
            id = "hadith_bukhari_8",
            collectionNameEn = "Sahih al-Bukhari",
            collectionNameBn = "সহীহ আল-বুখারী",
            bookNumber = 2,
            bookNameEn = "Belief (Kitab al-Iman)",
            hadithNumber = "8",
            arabicText = "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَإِقَامِ الصَّلاَةِ، وَإِيتَاءِ الزَّكَاةِ، وَالْحَجِّ، وَصَوْمِ رَمَضَانَ",
            bengaliTranslation = "ইসলাম পাঁচটি স্তম্ভের ওপর প্রতিষ্ঠিত: এই সাক্ষ্য দেওয়া যে আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই এবং মুহাম্মাদ (ﷺ) আল্লাহর রাসূল, সালাত কায়েম করা, যাকাত প্রদান করা, হজ্জ সম্পাদন করা এবং রমজানের সিয়াম পালন করা।",
            englishTranslation = "Islam is built upon five pillars: testifying that there is no deity worthy of worship except Allah and that Muhammad is the Messenger of Allah, establishing the prayer, paying the Zakat, making the pilgrimage to the House, and fasting in Ramadan.",
            narratorEn = "'Abdullah ibn 'Umar (RA)",
            grading = "Sahih (Bukhari & Muslim)",
            sourceReference = "Sahih al-Bukhari, Book 2, Hadith 8; Sahih Muslim 16"
        ),
        VerifiedHadithItem(
            id = "hadith_bukhari_5027",
            collectionNameEn = "Sahih al-Bukhari",
            collectionNameBn = "সহীহ আল-বুখারী",
            bookNumber = 66,
            bookNameEn = "Virtues of the Qur'an (Fada'il al-Qur'an)",
            hadithNumber = "5027",
            arabicText = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            bengaliTranslation = "তোমাদের মধ্যে সর্বোত্তম ঐ ব্যক্তি যে নিজে কুরআন শিক্ষা করে এবং অন্যকে তা শিক্ষা দেয়।",
            englishTranslation = "The best among you are those who learn the Qur'an and teach it to others.",
            narratorEn = "'Uthman ibn 'Affan (RA)",
            grading = "Sahih (Al-Bukhari)",
            sourceReference = "Sahih al-Bukhari, Book 66, Hadith 5027"
        ),
        VerifiedHadithItem(
            id = "hadith_muslim_223",
            collectionNameEn = "Sahih Muslim",
            collectionNameBn = "সহীহ মুসলিম",
            bookNumber = 2,
            bookNameEn = "Purification (Kitab at-Taharah)",
            hadithNumber = "223",
            arabicText = "الطُّهُورُ شَطْرُ الإِيمَانِ وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ وَسُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ تَمْلآَنِ أَوْ تَمْلأُ مَا بَيْنَ السَّمَاوَاتِ وَالأَرْضِ وَالصَّلاَةُ نُورٌ وَالصَّدَقَةُ بُرْهَانٌ وَالصَّبْرُ ضِيَاءٌ",
            bengaliTranslation = "পবিত্রতা ঈমানের অঙ্গ (অর্ধেক)। আলহামদুলিল্লাহ মীযানের পাল্লা পূর্ণ করে দেয় এবং সুবহানাল্লাহ ও আলহামদুলিল্লাহ আসমান ও জমিনের মধ্যবর্তী স্থান পূর্ণ করে দেয়। সালাত হচ্ছে নূর (আলো), সদকা হচ্ছে প্রমাণ এবং ধৈর্য হচ্ছে জ্যোতি।",
            englishTranslation = "Purification is half of faith. 'Alhamdulillah' fills the scale, and 'SubhanAllah' and 'Alhamdulillah' fill up what is between the heavens and the earth. Prayer is a light, charity is a proof, and patience is illumination.",
            narratorEn = "Abu Malik al-Ash'ari (RA)",
            grading = "Sahih (Muslim)",
            sourceReference = "Sahih Muslim, Book 2, Hadith 223"
        ),
        VerifiedHadithItem(
            id = "hadith_muslim_2699",
            collectionNameEn = "Sahih Muslim",
            collectionNameBn = "সহীহ মুসলিম",
            bookNumber = 48,
            bookNameEn = "Remembrance, Supplication, and Repentance",
            hadithNumber = "2699",
            arabicText = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            bengaliTranslation = "যে ব্যক্তি জ্ঞানার্জনের জন্য কোনো পথ অবলম্বন করে, আল্লাহ তার জন্য জান্নাতের পথ সহজ করে দেন।",
            englishTranslation = "Whoever treads a path in search of knowledge, Allah will make easy for him through it a path to Paradise.",
            narratorEn = "Abu Hurairah (RA)",
            grading = "Sahih (Muslim)",
            sourceReference = "Sahih Muslim, Hadith 2699"
        ),
        VerifiedHadithItem(
            id = "hadith_riyad_13",
            collectionNameEn = "Riyad as-Salihin",
            collectionNameBn = "রিয়াদুস সালেহীন",
            bookNumber = 1,
            bookNameEn = "Good Manners & Sincerity",
            hadithNumber = "183",
            arabicText = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            bengaliTranslation = "তোমাদের কেউ প্রকৃত মুমিন হতে পারবে না, যতক্ষণ না সে নিজের জন্য যা পছন্দ করে তার ভাইয়ের জন্যও তাই পছন্দ করে।",
            englishTranslation = "None of you truly believes until he loves for his brother what he loves for himself.",
            narratorEn = "Anas ibn Malik (RA)",
            grading = "Sahih (Bukhari 13, Muslim 45)",
            sourceReference = "Riyad as-Salihin 183; Sahih al-Bukhari 13"
        ),
        VerifiedHadithItem(
            id = "hadith_nawawi_12",
            collectionNameEn = "40 Hadith Nawawi",
            collectionNameBn = "ইমাম নববীর ৪০ হাদিস",
            bookNumber = 1,
            bookNameEn = "Forty Hadith of Imam an-Nawawi",
            hadithNumber = "12",
            arabicText = "مِنْ حُسْنِ إِسْلَامِ الْمَرْءِ تَرْكُهُ مَا لَا يَعْنِيهِ",
            bengaliTranslation = "একজন মানুষের ইসলামের সৌন্দর্য হলো অনর্থক (অপ্রয়োজনীয়) কথা ও কাজ বর্জন করা।",
            englishTranslation = "Part of the perfection of a person's Islam is his leaving that which does not concern him.",
            narratorEn = "Abu Hurairah (RA)",
            grading = "Hasan (Tirmidhi 2317, Ibn Majah 3976)",
            sourceReference = "Al-Arba'in an-Nawawiyyah, Hadith 12; Jami' at-Tirmidhi 2317"
        ),
        VerifiedHadithItem(
            id = "hadith_nawawi_18",
            collectionNameEn = "40 Hadith Nawawi",
            collectionNameBn = "ইমাম নববীর ৪০ হাদিস",
            bookNumber = 1,
            bookNameEn = "Forty Hadith of Imam an-Nawawi",
            hadithNumber = "18",
            arabicText = "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ",
            bengaliTranslation = "তুমি যেখানেই থাকো আল্লাহকে ভয় করো, কোনো মন্দ কাজ হয়ে গেলে সাথে সাথে ভালো কাজ করো যা তা মিটিয়ে দেবে, এবং মানুষের সাথে উত্তম আচরণ করো।",
            englishTranslation = "Have Taqwa (consciousness) of Allah wherever you are, follow up an evil deed with a good deed which will wipe it out, and behave well towards the people.",
            narratorEn = "Abu Dharr & Mu'adh ibn Jabal (RA)",
            grading = "Hasan Sahih (Jami' at-Tirmidhi 1987)",
            sourceReference = "Al-Arba'in an-Nawawiyyah, Hadith 18; Jami' at-Tirmidhi 1987"
        )
    )

    val DUA_CATEGORIES = listOf(
        "All",
        "Morning & Evening",
        "Sleep & Waking",
        "Prayer & Worship",
        "Forgiveness & Repentance",
        "Travel",
        "Eating & Drinking",
        "Protection"
    )

    val VERIFIED_DUAS = listOf(
        VerifiedDuaItem(
            id = "dua_morning_1",
            categoryEn = "Morning & Evening",
            categoryBn = "সকাল ও সন্ধ্যার যিকির",
            titleEn = "Morning Remembrance of Sovereignty",
            titleBn = "সকালবেলার তাসবীহ ও দোয়া",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Asbahna wa asbahal-mulku lillah, walhamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadir.",
            bengaliMeaning = "আমরা এবং সমগ্র বিশ্বজাহান আল্লাহর অনুগ্রহে সকালে উপনীত হয়েছি। সকল প্রশংসা আল্লাহরই। আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই, তিনি একক, তাঁর কোনো শরীক নেই। সার্বভৌমত্ব তাঁরই এবং সকল প্রশংসা তাঁরই; তিনি সর্ববিষয়ে সর্বশক্তিমান।",
            englishMeaning = "We have reached the morning and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah alone, without partner; to Him belongs all sovereignty and praise, and He is over all things Omnipotent.",
            sourceReference = "Sahih Muslim 2723 (Hisnul Muslim #75)",
            recommendedCount = 1
        ),
        VerifiedDuaItem(
            id = "dua_forgive_sayyidul",
            categoryEn = "Forgiveness & Repentance",
            categoryBn = "ক্ষমা ও তওবা",
            titleEn = "Sayyidul Istighfar (Chief Supplication for Forgiveness)",
            titleBn = "সাইয়্যিদুল ইস্তিগফার (ক্ষমা প্রার্থনার শ্রেষ্ঠ দোয়া)",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u لaka bidhanbi faghfir li, fa innahu la yaghfirudh-dhunuba illa Anta.",
            bengaliMeaning = "হে আল্লাহ! আপনি আমার রব, আপনি ছাড়া কোনো সত্য উপাস্য নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা। আমি আমার সাধ্যমতো আপনার অঙ্গীকার ও প্রতিশ্রুতির ওপর অটল রয়েছি। আমি আমার কৃতকর্মের অনিষ্ট থেকে আপনার কাছে আশ্রয় চাই। আমার ওপর আপনার অনুগ্রহ আমি স্বীকার করছি এবং আমার অপরাধও স্বীকার করছি; অতএব আমাকে ক্ষমা করুন, কেননা আপনি ছাড়া গুনাহ ক্ষমাকারী আর কেউ নেই।",
            englishMeaning = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide to Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for verily none can forgive sins except You.",
            sourceReference = "Sahih al-Bukhari 6306",
            recommendedCount = 1
        ),
        VerifiedDuaItem(
            id = "dua_sleep_1",
            categoryEn = "Sleep & Waking",
            categoryBn = "ঘুমানো ও জাগ্রত হওয়া",
            titleEn = "Before Going to Sleep",
            titleBn = "ঘুমানোর পূর্বের দোয়া",
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya.",
            bengaliMeaning = "হে আল্লাহ! আপনারই নামে আমি মৃত্যুবরণ করি (নিদ্রা যাই) এবং আপনারই নামে জীবিত (জাগ্রত) হই।",
            englishMeaning = "In Your name, O Allah, I die (sleep) and I live (wake).",
            sourceReference = "Sahih al-Bukhari 6324",
            recommendedCount = 1
        ),
        VerifiedDuaItem(
            id = "dua_waking_1",
            categoryEn = "Sleep & Waking",
            categoryBn = "ঘুমানো ও জাগ্রত হওয়া",
            titleEn = "Upon Waking Up",
            titleBn = "ঘুম থেকে জেগে ওঠার দোয়া",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur.",
            bengaliMeaning = "সকল প্রশংসা আল্লাহর জন্য যিনি আমাদেরকে নিদ্রারূপ মৃত্যুর পর পুনরায় জীবন দান করেছেন, এবং তাঁরই কাছে আমাদের পুনরুত্থান।",
            englishMeaning = "All praise is for Allah who gave us life after having taken it from us and unto Him is the resurrection.",
            sourceReference = "Sahih al-Bukhari 6312",
            recommendedCount = 1
        ),
        VerifiedDuaItem(
            id = "dua_prayer_after_salam",
            categoryEn = "Prayer & Worship",
            categoryBn = "নামাজ ও ইবাদত",
            titleEn = "After Completing Obligatory Prayer",
            titleBn = "ফরজ সালাতের সালাম ফেরানোর পরের দোয়া",
            arabicText = "أَسْتَغْفِرُ اللَّهَ (ثَلاَثًا) اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ",
            transliteration = "Astaghfirullah (3x). Allahumma Antas-Salamu wa minkas-salamu, tabarakta ya Dhal-Jalali wal-Ikram.",
            bengaliMeaning = "আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি (৩ বার)। হে আল্লাহ! আপনিই শান্তিদাতা এবং আপনার কাছ থেকেই শান্তি আসে। হে মহিমাময় ও মহানুভব, আপনি বরকতময়।",
            englishMeaning = "I seek the forgiveness of Allah (three times). O Allah, You are Peace and from You comes peace. Blessed are You, O Possessor of Majesty and Honor.",
            sourceReference = "Sahih Muslim 591",
            recommendedCount = 1
        ),
        VerifiedDuaItem(
            id = "dua_travel_1",
            categoryEn = "Travel",
            categoryBn = "সফর ও ভ্রমণ",
            titleEn = "Supplication for Mounting a Vehicle / Travel",
            titleBn = "যানবাহনে আরোহণ ও সফরের দোয়া",
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhanal-ladhi sakhkhara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqalibun.",
            bengaliMeaning = "মহাপবিত্র তিনি, যিনি আমাদের জন্য এই বাহনকে বশীভূত করে দিয়েছেন, অথচ আমরা একে বশীভূত করতে সক্ষম ছিলাম না; আর আমরা অবশ্যই আমাদের রবের কাছে ফিরে যাব।",
            englishMeaning = "Glory is to Him Who has subjected this to us, and we could never have it by our efforts. And verily, to Our Lord we indeed are to return.",
            sourceReference = "Surah Az-Zukhruf 43:13-14; Sahih Muslim 1342",
            recommendedCount = 1
        ),
        VerifiedDuaItem(
            id = "dua_eating_1",
            categoryEn = "Eating & Drinking",
            categoryBn = "পানাহার",
            titleEn = "After Finishing a Meal",
            titleBn = "খাবার শেষ করার পরের দোয়া",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdu lillahil-ladhi at'amani hadha wa razaqanihi min ghayri hawlin minni wa la quwwatin.",
            bengaliMeaning = "সকল প্রশংসা আল্লাহর জন্য যিনি আমার কোনো কৌশল ও শক্তি ছাড়াই আমাকে এ আহার করিয়েছেন এবং রিযিক দান করেছেন।",
            englishMeaning = "Praise be to Allah Who has fed me this food and provided it for me, without any strength or power on my part.",
            sourceReference = "Sunan Abi Dawud 4023, Jami' at-Tirmidhi 3458 (Hasan)",
            recommendedCount = 1
        ),
        VerifiedDuaItem(
            id = "dua_protection_1",
            categoryEn = "Protection",
            categoryBn = "নিরাপত্তা ও হেফাজত",
            titleEn = "Protection from Harm (Morning & Evening 3x)",
            titleBn = "সকল অনিষ্ট থেকে হেফাজতের দোয়া (সকাল-সন্ধ্যা ৩ বার)",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            bengaliMeaning = "আল্লাহর নামে, যাঁর নামের বরকতে আসমান ও জমিনের কোনো কিছুই কোনো ক্ষতি করতে পারে না; আর তিনি সর্বশ্রোতা, সর্বজ্ঞ।",
            englishMeaning = "In the Name of Allah, with Whose Name nothing on earth or in the heavens can cause harm, and He is the All-Hearing, the All-Knowing.",
            sourceReference = "Sunan Abi Dawud 5088, Jami' at-Tirmidhi 3388 (Sahih)",
            recommendedCount = 3
        )
    )

    // --- Islamic Learning: Asma ul Husna (Curated Verified Selection) ---
    val ASMA_UL_HUSNA = listOf(
        AsmaUlHusnaItem(1, "ٱللَّٰه", "Allah", "The Greatest Name (God)", "আল্লাহ (সকল গুণের আধার)"),
        AsmaUlHusnaItem(2, "ٱلرَّحْمَٰن", "Ar-Rahman", "The Entirely Merciful", "পরম করুণাময়"),
        AsmaUlHusnaItem(3, "ٱلرَّحِيم", "Ar-Rahim", "The Especially Merciful", "অতি দয়ালু"),
        AsmaUlHusnaItem(4, "ٱلْمَلِك", "Al-Malik", "The Eternal Sovereign", "সার্বভৌম অধিপতি"),
        AsmaUlHusnaItem(5, "ٱلْقُدُّوس", "Al-Quddus", "The Pure and Holy", "মহাপবিত্র"),
        AsmaUlHusnaItem(6, "ٱلسَّلَام", "As-Salam", "The Source of Peace", "শান্তিদাতা"),
        AsmaUlHusnaItem(7, "ٱلْمُؤْمِن", "Al-Mu'min", "The Granter of Security", "নিরাপত্তা ও ঈমান দানকারী"),
        AsmaUlHusnaItem(8, "ٱلْمُهَيْمِن", "Al-Muhaymin", "The Guardian and Overseer", "রক্ষণাবেক্ষণকারী"),
        AsmaUlHusnaItem(9, "ٱلْعَزِيز", "Al-'Aziz", "The Almighty", "মহাপরাক্রমশালী"),
        AsmaUlHusnaItem(10, "ٱلْجَبَّار", "Al-Jabbar", "The Restorer and Compeller", "প্রবল ও সংশোধনকারী"),
        AsmaUlHusnaItem(11, "ٱلْمُتَكَبِّر", "Al-Mutakabbir", "The Supreme in Greatness", "শ্রেষ্ঠত্বের অধিকারী"),
        AsmaUlHusnaItem(12, "ٱلْخَالِق", "Al-Khaliq", "The Creator", "সৃষ্টিকর্তা"),
        AsmaUlHusnaItem(13, "ٱلْبَارِئ", "Al-Bari'", "The Evolver and Maker", "সঠিকভাবে উদ্ভাবনকারী"),
        AsmaUlHusnaItem(14, "ٱلْمُصَوِّر", "Al-Musawwir", "The Fashioner of Forms", "আকৃতি দানকারী"),
        AsmaUlHusnaItem(15, "ٱلْغَفَّار", "Al-Ghaffar", "The Repeatedly Forgiving", "বারবার ক্ষমাকারী"),
        AsmaUlHusnaItem(16, "ٱلْقَهَّار", "Al-Qahhar", "The Subduer", "সর্বজয়ী"),
        AsmaUlHusnaItem(17, "ٱلْوَهَّاب", "Al-Wahhab", "The Supreme Bestower", "মহাদানশীল"),
        AsmaUlHusnaItem(18, "ٱلرَّزَّاق", "Ar-Razzaq", "The All-Provider", "রিযিকদাতা"),
        AsmaUlHusnaItem(19, "ٱلْفَتَّاح", "Al-Fattah", "The Opener and Judge", "বিজয় ও রহমত উন্মোচনকারী"),
        AsmaUlHusnaItem(20, "ٱلْعَلِيم", "Al-'Alim", "The All-Knowing", "সর্বজ্ঞ")
    )

    val KALIMAS = listOf(
        KalimaItem(
            1,
            "First Kalima — Tayyibah (Purity)",
            "প্রথম কালিমা — তাইয়্যিবা (পবিত্রতা)",
            "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ مُحَمَّدٌ رَسُولُ ٱللَّٰهِ",
            "La ilaha illallahu Muhammadur Rasulullah.",
            "There is no deity worthy of worship except Allah, and Muhammad (ﷺ) is the Messenger of Allah.",
            "আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই, মুহাম্মাদ (ﷺ) আল্লাহর রাসূল।"
        ),
        KalimaItem(
            2,
            "Second Kalima — Shahadah (Testimony)",
            "দ্বিতীয় কালিমা — শাহাদাত (সাক্ষ্য)",
            "أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا ٱللَّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            "Ash-hadu an la ilaha illallahu wahdahu la sharika lahu, wa ash-hadu anna Muhammadan 'abduhu wa rasuluh.",
            "I bear witness that there is no deity worthy of worship except Allah alone, without partner, and I bear witness that Muhammad is His servant and Messenger.",
            "আমি সাক্ষ্য দিচ্ছি যে আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই, তিনি একক, তাঁর কোনো শরীক নেই; এবং আমি আরও সাক্ষ্য দিচ্ছি যে মুহাম্মাদ (ﷺ) তাঁর বান্দা ও রাসূল।"
        ),
        KalimaItem(
            3,
            "Third Kalima — Tamjeed (Glorification)",
            "তৃতীয় কালিমা — তামজীদ (মহিমা)",
            "سُبْحَانَ ٱللَّٰهِ وَٱلْحَمْدُ لِلَّٰهِ وَلَا إِلَٰهَ إِلَّا ٱللَّٰهُ وَٱللَّٰهُ أَكْبَرُ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِٱللَّٰهِ ٱلْعَلِيِّ ٱلْعَظِيمِ",
            "Subhanallahi walhamdu lillahi wa la ilaha illallahu wallahu akbar, wa la hawla wa la quwwata illa billahil-'Aliyyil-'Azim.",
            "Glory be to Allah, all praise be to Allah, there is no deity except Allah, and Allah is the Greatest. There is no might nor power except with Allah, the Most High, the Supreme.",
            "আল্লাহ পবিত্র, সকল প্রশংসা আল্লাহর, আল্লাহ ছাড়া কোনো উপাস্য নেই, আল্লাহ মহান। সুমহান ও সর্বোচ্চ আল্লাহর সাহায্য ছাড়া কোনো শক্তি ও সামর্থ্য নেই।"
        ),
        KalimaItem(
            4,
            "Fourth Kalima — Tawheed (Oneness)",
            "চতুর্থ কালিমা — তাওহীদ (একত্ববাদ)",
            "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ ٱلْمُلْكُ وَلَهُ ٱلْحَمْدُ يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ أَبَدًا أَبَدًا، ذُو ٱلْجَلَالِ وَٱلْإِكْرَامِ، بِيَدِهِ ٱلْخَيْرُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "La ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu yuhyi wa yumitu wa Huwa Hayyun la yamutu abadan abada, Dhul-Jalali wal-Ikram, biyadihil-khayru wa Huwa 'ala kulli shay'in Qadir.",
            "There is no deity except Allah alone, without partner. His is the sovereignty and His is the praise; He gives life and causes death, and He is Ever-Living and never dies. Possessor of Majesty and Honor; in His hand is all good and He is over all things Omnipotent.",
            "আল্লাহ ছাড়া কোনো উপাস্য নেই, তিনি একক, তাঁর কোনো শরীক নেই। রাজত্ব তাঁরই এবং প্রশংসা তাঁরই; তিনি জীবন ও মৃত্যু দান করেন, তিনি চিরঞ্জীব। সকল কল্যাণ তাঁর হাতে এবং তিনি সর্ববিষয়ে সর্বশক্তিমান।"
        )
    )

    val AYATUL_KURSI_ARABIC =
        "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَىْءٍ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ ۚ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ ۖ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا ۚ وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ"

    val AYATUL_KURSI_TRANSLITERATION =
        "Allahu la ilaha illa Huwal-Hayyul-Qayyum. La ta'khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa'u 'indahu illa bi-idhnih. Ya'lamu ma bayna aydihim wa ma khalfahum, wa la yuhituna bishay'in min 'ilmihi illa bima sha'a. Wasi'a kursiyyuhus-samawati wal-ard, wa la ya'uduhu hifzuhuma, wa Huwal-'Aliyyul-'Azim."

    val AYATUL_KURSI_MEANING_BN =
        "আল্লাহ—তিনি ছাড়া কোনো সত্য উপাস্য নেই; তিনি চিরঞ্জীব, সর্বধারক। তাঁকে তন্দ্রা বা নিদ্রা স্পর্শ করে না। আসমান ও জমিনে যা কিছু আছে সবই তাঁর। কে আছে এমন যে তাঁর অনুমতি ছাড়া তাঁর কাছে সুপারিশ করবে? তাদের সামনে ও পেছনে যা কিছু আছে তা তিনি জানেন। আর তিনি যা ইচ্ছা করেন তা ছাড়া তাঁর জ্ঞানের কোনো কিছুই তারা আয়ত্ত করতে পারে না। তাঁর কুরসি আসমান ও জমিন পরিব্যাপ্ত করে আছে এবং এ দুইয়ের সংরক্ষণ তাঁকে ক্লান্ত করে না; তিনি সুউচ্চ, সুমহান। (সূরা আল-বাকারা ২:২৫৫)"

    val AYATUL_KURSI_MEANING_EN =
        "Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is [presently] before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great. (Surah Al-Baqarah 2:255)"

    val WUDU_STEPS = listOf(
        LearningGuideStep(1, "Intention (Niyyah) & Bismillah", "নিয়ত ও বিসমিল্লাহ বলা", "Make the intention in your heart to purify yourself for prayer and say 'Bismillah'.", "পবিত্রতা অর্জনের জন্য অন্তরে নিয়ত করুন এবং মুখে 'বিসমিল্লাহ' বলুন।", "Sunan Abi Dawud 101"),
        LearningGuideStep(2, "Washing Both Hands (3x)", "উভয় হাত কব্জি পর্যন্ত ধোয়া (৩ বার)", "Wash both hands up to the wrists three times, making sure water reaches between the fingers.", "উভয় হাতের কব্জি পর্যন্ত তিনবার ধৌত করুন এবং আঙুলের ফাঁকে পানি পৌঁছান।", "Sahih al-Bukhari 159"),
        LearningGuideStep(3, "Rinsing Mouth & Nose (3x)", "কুলি করা ও নাকে পানি দেওয়া (৩ বার)", "Rinse the mouth thoroughly and sniff water gently into the nostrils and blow it out three times.", "তিনবার ভালোভাবে কুলি করুন এবং নাকে পানি দিয়ে বাম হাতে পরিষ্কার করুন।", "Sahih al-Bukhari 159"),
        LearningGuideStep(4, "Washing the Face (3x)", "সমস্ত মুখমণ্ডল ধোয়া (৩ বার)", "Wash the entire face from hairline to chin and from ear to ear three times (Fard/Obligatory).", "কপালের চুলের গোড়া থেকে থুতনির নিচ পর্যন্ত এবং এক কানের লতি থেকে অন্য কান পর্যন্ত তিনবার ধৌত করুন (ফরজ)।", "Surah Al-Ma'idah 5:6"),
        LearningGuideStep(5, "Washing Forearms Including Elbows (3x)", "কনুইসহ উভয় হাত ধোয়া (৩ বার)", "Wash the right arm then the left arm from fingertips up to and including the elbows three times (Fard).", "প্রথমে ডান হাত ও পরে বাম হাত আঙুলের অগ্রভাগ থেকে কনুইসহ তিনবার ধৌত করুন (ফরজ)।", "Surah Al-Ma'idah 5:6"),
        LearningGuideStep(6, "Wiping the Head (Masah - 1x)", "মাথা মাসেহ করা (১ বার)", "Wipe wet hands over the head from front to back and back to front once, including the ears (Fard).", "ভেজা হাতে মাথার সামনের অংশ থেকে পেছনে নিয়ে পুনরায় সামনে এনে একবার মাসেহ করুন এবং কান মাসেহ করুন (ফরজ)।", "Surah Al-Ma'idah 5:6; Sahih Muslim 235"),
        LearningGuideStep(7, "Washing Both Feet Including Ankles (3x)", "টাখনুসহ উভয় পা ধোয়া (৩ বার)", "Wash the right foot then the left foot up to and including the ankles three times, cleaning between the toes (Fard).", "প্রথমে ডান পা এবং পরে বাম পা টাখনুসহ তিনবার ভালোভাবে ধৌত করুন এবং আঙুলের ফাঁক খিলাল করুন (ফরজ)।", "Surah Al-Ma'idah 5:6; Sahih al-Bukhari 159"),
        LearningGuideStep(8, "Supplication After Wudu", "ওযু শেষের দোয়া", "Recite the Shahadah and: 'Allahummaj-'alni minat-tawwabina waj-'alni minal-mutatahhirin.'", "শাহাদাত পাঠ করুন এবং বলুন: 'আল্লাহুম্মাজ-আলনী মিনাত-তাওয়্যাবীন ওয়াজ-আলনী মিনাল-মুতাতাহহিরীন।'", "Sahih Muslim 234, Jami' at-Tirmidhi 55")
    )
}
