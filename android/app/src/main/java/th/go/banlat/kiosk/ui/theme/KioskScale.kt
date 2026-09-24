package th.go.banlat.kiosk.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ทุกตัวเลขในโค้ดอ้างอิงกรอบออกแบบ 1080 x 1920 (เท่า Figma และต้นแบบ HTML)
 * บนจอจริงกรอบนี้ถูกย่อ/ขยายให้พอดีจอแบบรักษาสัดส่วน · จอ 32" แนวตั้ง 1080x1920 = 1:1 พอดี
 *
 * เขียน 80.s แทนระยะ 80 px ในแบบ · 48.st แทนขนาดตัวอักษร 48 px ในแบบ
 */
const val DESIGN_W = 1080f
const val DESIGN_H = 1920f

val LocalKioskScale = staticCompositionLocalOf { 1f }

val Int.s: Dp @Composable get() = (this * LocalKioskScale.current).dp
val Float.s: Dp @Composable get() = (this * LocalKioskScale.current).dp
val Double.s: Dp @Composable get() = (this * LocalKioskScale.current).dp
val Int.st: TextUnit @Composable get() = (this * LocalKioskScale.current).sp
val Float.st: TextUnit @Composable get() = (this * LocalKioskScale.current).sp
val Double.st: TextUnit @Composable get() = (this * LocalKioskScale.current).sp

/** ครอบทั้งแอปครั้งเดียว: จัดกรอบ 1080x1920 กลางจอ และล็อก fontScale = 1 ไม่ให้ตัวอักษรล้นกรอบ */
@Composable
fun KioskFrame(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF0E1420)), contentAlignment = Alignment.Center) {
        val d = LocalDensity.current
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()
        val pxPerUnit = minOf(wPx / DESIGN_W, hPx / DESIGN_H)
        val dpPerUnit = pxPerUnit / d.density
        CompositionLocalProvider(
            LocalDensity provides Density(d.density, 1f),
            LocalKioskScale provides dpPerUnit,
        ) {
            Box(Modifier.requiredSize((DESIGN_W * dpPerUnit).dp, (DESIGN_H * dpPerUnit).dp).clipToBounds()) {
                content()
            }
        }
    }
}

/** จำนวน px จริงต่อ 1 หน่วยออกแบบ ใช้ใน Canvas */
@Composable
fun unitPx(): Float = LocalKioskScale.current * LocalDensity.current.density
