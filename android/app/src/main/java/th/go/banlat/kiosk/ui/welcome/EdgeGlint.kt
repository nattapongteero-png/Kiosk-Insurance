package th.go.banlat.kiosk.ui.welcome

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import th.go.banlat.kiosk.ui.theme.unitPx
import kotlin.math.sqrt

/*
 * แสงแดดสะท้อนขอบทองของการ์ด "กรุณาเสียบบัตร"
 *  - ขอบทอง: เข้มสุดที่มุมซ้ายบน ไล่อ่อนลงจนแทบหายที่มุมขวาล่าง
 *  - ประกาย: วิ่งจากมุมซ้ายบนออกไปตามขอบบนและขอบซ้าย + ประกายสี่แฉกวาบที่มุม
 *  จังหวะผูกกับการเสียบบัตร (รอบ 14 วิ · บัตรเข้าสุดที่วินาที 5.04) ประกายจึงเล่นหลังบัตรเสียบเข้าช่องแล้ว
 */
private val GlintEase = CubicBezierEasing(.35f, .1f, .25f, 1f)

private fun glintPos(t14: Float): Float {
    val q = t14 / 14000f
    return when { q <= .36f -> -160f; q >= .56f -> 560f; else -> -160f + 720f * GlintEase.transform((q - .36f) / .20f) }
}

private data class Flare(val alpha: Float, val scale: Float, val rot: Float)

private fun flare(t14: Float): Flare {
    val q = t14 / 14000f
    fun seg(a: Float, b: Float) = FastOutSlowInEasing.transform(((q - a) / (b - a)).coerceIn(0f, 1f))
    return when {
        q <= .36f || q >= .50f -> Flare(0f, .3f, 0f)
        q < .395f -> seg(.36f, .395f).let { Flare(it, .3f + .7f * it, 30f * it) }
        q < .43f -> seg(.395f, .43f).let { Flare(1f - .15f * it, 1f - .1f * it, 30f + 12f * it) }
        else -> seg(.43f, .50f).let { Flare(.85f * (1f - it), .9f - .3f * it, 42f + 13f * it) }
    }
}

/** วาดทับการ์ด (ขนาดเท่าการ์ด 920 x 704 · มุมโค้ง 48) ไม่รับการแตะ */
@Composable
fun EdgeGlint(clock: State<Float>, modifier: Modifier) {
    val u = unitPx()
    Box(modifier) {
        // ขอบทองนิ่ง (radial จากจุด 14,14 รัศมี 1120)
        Canvas(Modifier.fillMaxSize()) {
            ring(2f * u, 48f * u, Brush.radialGradient(
                0f to Color(0xFFE2AE4C), 70f / 1120f to Color(0xFFD9A546), 220f / 1120f to Color(0x73D6A850),
                500f / 1120f to Color(0x29D6A850), 800f / 1120f to Color(0x0FD6A850), 1f to Color(0x00D6A850),
                center = Offset(14f * u, 14f * u), radius = 1120f * u))
        }
        // ประกายวิ่ง — linear-gradient(135deg) ตำแหน่ง g ตามแนวทแยง แล้วจำกัดอยู่รอบมุมด้วย radial mask (ชั้นแยก)
        Canvas(Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
            val g = glintPos(clock.value)
            if (g > -150f && g < 555f) {
                val d = 1f / sqrt(2f)
                val a = (g - 140f) * u; val b = (g + 140f) * u
                val sweep = Brush.linearGradient(
                    0f to Color(0x00FFECBE), 100f / 280f to Color(0xFFFFE9B0), .5f to Color.White,
                    180f / 280f to Color(0xFFFFE9B0), 1f to Color(0x00FFECBE),
                    start = Offset(a * d, a * d), end = Offset(b * d, b * d))
                val r = 48f * u
                ring(24f * u, r, sweep, alpha = .18f)   // เรืองกว้าง
                ring(12f * u, r, sweep, alpha = .45f)   // เรืองกลาง
                ring(5f * u, r, sweep)                  // เส้นแสง
                drawRect(Brush.radialGradient(0f to Color.Black, 260f / 560f to Color.Black, 1f to Color.Transparent,
                    center = Offset.Zero, radius = 560f * u), blendMode = BlendMode.DstIn)
            }
        }
        // ประกายสี่แฉกที่มุม
        Canvas(Modifier.fillMaxSize()) {
            val f = flare(clock.value)
            if (f.alpha > 0f) {
                val c = Offset(14f * u, 14f * u)
                val half = 64f * u * f.scale
                rotate(f.rot, c) {
                    drawCircle(Brush.radialGradient(0f to Color.White, 4f / 26f to Color.White, 8f / 26f to Color(0xD9FFEEBE),
                        16f / 26f to Color(0x59FFE2A0), 1f to Color(0x00FFE2A0), center = c, radius = 26f * u * f.scale),
                        radius = 26f * u * f.scale, center = c, alpha = f.alpha)
                    val beam = 3f * u * f.scale
                    drawRect(Brush.horizontalGradient(listOf(Color(0x00ECBE5A), Color(0xFFFFF6DC), Color(0x00ECBE5A)), c.x - half, c.x + half),
                        Offset(c.x - half, c.y - beam / 2), Size(half * 2, beam), alpha = f.alpha)
                    drawRect(Brush.verticalGradient(listOf(Color(0x00ECBE5A), Color(0xFFFFF6DC), Color(0x00ECBE5A)), c.y - half, c.y + half),
                        Offset(c.x - beam / 2, c.y - half), Size(beam, half * 2), alpha = f.alpha)
                }
            }
        }
    }
}

private fun DrawScope.ring(width: Float, radius: Float, brush: Brush, alpha: Float = 1f) {
    val i = width / 2f
    drawRoundRect(brush, Offset(i, i), Size(size.width - width, size.height - width),
        CornerRadius(radius - i), style = Stroke(width), alpha = alpha)
}
