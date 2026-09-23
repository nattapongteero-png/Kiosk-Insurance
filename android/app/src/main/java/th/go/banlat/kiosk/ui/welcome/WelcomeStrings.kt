package th.go.banlat.kiosk.ui.welcome

import androidx.annotation.DrawableRes
import th.go.banlat.kiosk.R

/**
 * ข้อความของหน้าต้อนรับแยกตามภาษา — ตรงกับ I18N ใน preview/welcome.html
 * ส่วนที่เป็นภาพ (ปุ่ม 2 ใบ) ใช้ไฟล์ *_en แยก; ตอนนี้ btn_hn_en / btn_scan_en เป็นสำเนาของภาษาไทยไว้ก่อน รอไฟล์จริง
 */
data class WelcomeStrings(
    val hospital: String,
    val hospitalSub: String,
    val titleLead: String,      // ส่วนหน้าของหัวข้อ (สีกรมท่า)
    val titleAccent: String,    // ส่วนเน้น (ไล่สีน้ำเงิน)
    val subtitleLead: String,
    val subtitleAccent: String, // คำที่เน้นหนา
    val subtitleTail: String,
    val altLabel: String,
    val btnHn: String,
    val btnScan: String,
    @DrawableRes val btnHnRes: Int,
    @DrawableRes val btnScanRes: Int,
    @DrawableRes val langRes: Int,
    /** ขนาดตัวอักษร (หน่วยดีไซน์) — อังกฤษเล็กลงเล็กน้อยกันขึ้น 2 บรรทัด */
    val titleSize: Int = 78,
    val subtitleSize: Int = 31,
) {
    companion object {
        val TH = WelcomeStrings(
            hospital = "โรงพยาบาลบ้านลาด", hospitalSub = "BANLAT HOSPITAL",
            titleLead = "กรุณาเสียบบัตร", titleAccent = "ประชาชน",
            subtitleLead = "หันด้านที่มี", subtitleAccent = "ชิปสีทอง", subtitleTail = "เข้าช่อง และระบบจะอ่านข้อมูลของท่านโดยอัตโนมัติ",
            altLabel = "หรือเลือกวิธีอื่น",
            btnHn = "HN หรือบัตรประชาชน", btnScan = "สแกนหน้า",
            btnHnRes = R.drawable.btn_hn, btnScanRes = R.drawable.btn_scan, langRes = R.drawable.lang_th,
        )
        val EN = WelcomeStrings(
            hospital = "Banlat Hospital", hospitalSub = "โรงพยาบาลบ้านลาด",
            titleLead = "Insert your ", titleAccent = "ID card",
            subtitleLead = "", subtitleAccent = "Chip end", subtitleTail = " first — your details are read automatically.",
            altLabel = "Or choose another option",
            btnHn = "HN or ID Card", btnScan = "Face scan",
            btnHnRes = R.drawable.btn_hn_en, btnScanRes = R.drawable.btn_scan_en, langRes = R.drawable.lang_en,
            titleSize = 70, subtitleSize = 29,
        )

        fun of(lang: KioskLanguage) = if (lang == KioskLanguage.Thai) TH else EN
    }
}
