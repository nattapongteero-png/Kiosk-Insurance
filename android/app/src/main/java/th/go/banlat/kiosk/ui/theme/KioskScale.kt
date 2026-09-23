package th.go.banlat.kiosk.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * หัวใจของการรองรับหลายขนาดจอ
 *
 * ดีไซน์ทุกตัวเลขอ้างอิงกรอบ 1080 x 1920 (เท่า preview/index.html)
 * รันจริงบนจอไหนก็ตาม เราคูณด้วยอัตราส่วนความกว้างจริง / 1080
 * ทำให้จอ 21" 27" 32" ใช้โค้ดชุดเดียวกัน ไม่ต้องแยก layout
 */
const val DESIGN_WIDTH = 1080f

val LocalKioskScale = CompositionLocalOf { 1f }

/** ระยะ/ขนาด: เขียน 32.s แทน 32.dp */
val Int.s: Dp @Composable get() = (this * LocalKioskScale.current).dp

/** ขนาดตัวอักษร: เขียน 44.st แทน 44.sp */
val Int.st: TextUnit @Composable get() = (this * LocalKioskScale.current).sp

/** เวอร์ชันทศนิยม สำหรับค่าอย่าง 1.5.st (letterSpacing) หรือ 0.5.s */
val Double.s: Dp @Composable get() = (this * LocalKioskScale.current).dp
val Double.st: TextUnit @Composable get() = (this * LocalKioskScale.current).sp

/**
 * ครอบหน้าจอทั้งหมดด้วยตัวนี้หนึ่งครั้ง
 * ต้องตั้ง fontScale = 1 ด้วย เพราะ kiosk ไม่ควรให้ระบบขยายตัวอักษรจนล้นกรอบ
 */
@Composable
fun KioskScaleProvider(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val scale = constraints.maxWidth / DESIGN_WIDTH
        CompositionLocalProvider(LocalKioskScale provides scale) { content() }
    }
}
