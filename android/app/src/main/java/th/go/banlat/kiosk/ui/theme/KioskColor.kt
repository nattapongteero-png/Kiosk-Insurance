package th.go.banlat.kiosk.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design token ชุดเดียวกับ preview/index.html
 * ห้าม hard-code สีในหน้าจอ ให้เรียกผ่านที่นี่เท่านั้น
 */
object KioskColor {
    val Brand900 = Color(0xFF0A3D62)
    val Brand700 = Color(0xFF0B6FB4)
    val Brand500 = Color(0xFF1E88D6)
    val Brand050 = Color(0xFFEAF3FB)

    val Teal600 = Color(0xFF0E9384)
    val Teal050 = Color(0xFFE6F6F4)
    val Amber600 = Color(0xFFB54708)
    val Amber050 = Color(0xFFFEF3E2)
    val Violet600 = Color(0xFF6941C6)
    val Violet050 = Color(0xFFF1ECFB)

    val Danger600 = Color(0xFFD92D20)
    val Danger050 = Color(0xFFFEF3F2)
    val Danger300 = Color(0xFFFDA29B)

    val Ink900 = Color(0xFF101828)
    val Ink600 = Color(0xFF475467)
    val Ink400 = Color(0xFF98A2B3)
    val Line = Color(0xFFE4E7EC)
    val Surface = Color(0xFFFFFFFF)
    val Canvas = Color(0xFFEFF3F8)
    val Muted = Color(0xFFF2F4F7)
}

/** คู่สี accent/tint ของการ์ดบริการ — ฝั่ง API ส่งมาเป็น key เช่น "teal" */
enum class ServiceAccent(val accent: Color, val tint: Color) {
    Blue(KioskColor.Brand700, KioskColor.Brand050),
    Teal(KioskColor.Teal600, KioskColor.Teal050),
    Amber(KioskColor.Amber600, KioskColor.Amber050),
    Violet(KioskColor.Violet600, KioskColor.Violet050);

    companion object {
        fun from(key: String?): ServiceAccent =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: Blue
    }
}
