package com.mohamedabdelazeim.salah.data

import com.mohamedabdelazeim.salah.R

data class ZekrItem(val id: Int, val name: String, val audioRes: Int?, val text: String = "")

object ZekrData {
    val zekrList = listOf(
        ZekrItem(1, "الصلاة على النبي ﷺ", R.raw.nozaker_salt_ala_habib, "اللهم صلِّ وسلِّم على نبينا محمد"),
        ZekrItem(2, "آية الأحزاب", R.raw.ayah_elahzab, "إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ"),
        ZekrItem(3, "الحمد لله", R.raw.alhamdo_lelah, "الحمد لله"),
        ZekrItem(4, "اللهم لك الحمد", R.raw.allahom_lk_alhamd, "اللهم لك الحمد"),
        ZekrItem(5, "لا حول ولا قوة إلا بالله", R.raw.lahawla_wlaqowat, "لا حول ولا قوة إلا بالله"),
        ZekrItem(6, "سبحان الله وبحمده", R.raw.sobhanallah_wabehamdeh, "سبحان الله وبحمده"),
        ZekrItem(7, "ربي اغفر لي ولوالدي", R.raw.rbna_ighfer_li, "ربي اغفر لي ولوالديَّ")
    )
}
