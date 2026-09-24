package th.go.banlat.kiosk.ui.common

import android.graphics.BlurMaskFilter
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.unitPx
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** เงาแบบ CSS box-shadow: offsetY / blur / spread เป็นหน่วยออกแบบ */
data class Shade(val color: Color, val y: Float, val blur: Float, val spread: Float = 0f, val x: Float = 0f)

/**
 * เงานุ่มหลายชั้น (เท่า box-shadow หลายค่าในต้นแบบ) · วาดด้วย BlurMaskFilter บน hardware canvas (API 28+)
 * radius = มุมโค้งของกล่อง (หน่วยออกแบบ)
 */
@Composable
fun Modifier.softShadow(radius: Float, vararg layers: Shade): Modifier {
    val u = unitPx()
    return this.drawBehind {
        drawIntoCanvas { c ->
            layers.forEach { l ->
                val p = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = l.color.toArgb()
                    if (l.blur > 0f) maskFilter = BlurMaskFilter(l.blur * 0.87f * u, BlurMaskFilter.Blur.NORMAL)
                }
                val sp = l.spread * u
                val r = RectF(-sp + l.x * u, -sp + l.y * u, size.width + sp + l.x * u, size.height + sp + l.y * u)
                val rr = radius * u + sp
                c.nativeCanvas.drawRoundRect(r, rr, rr, p)
            }
        }
    }
}

/** เส้นทแยงของ linear-gradient(angle) แบบ CSS บนกล่องขนาด w x h (px) */
fun cssLinear(angleDeg: Float, w: Float, h: Float, vararg stops: Pair<Float, Color>): Brush {
    val a = angleDeg * PI.toFloat() / 180f
    val dx = sin(a); val dy = -cos(a)
    val half = (kotlin.math.abs(w * dx) + kotlin.math.abs(h * dy)) / 2f
    val cx = w / 2f; val cy = h / 2f
    return Brush.linearGradient(
        colorStops = stops,
        start = androidx.compose.ui.geometry.Offset(cx - dx * half, cy - dy * half),
        end = androidx.compose.ui.geometry.Offset(cx + dx * half, cy + dy * half),
    )
}

/** ผิวไข่มุก (ไล่ขาว → ครีม 176°) + ขอบทองบาง + ขอบขาว ใช้กับการ์ด ปุ่ม และแถวรายการ */
@Composable
fun Modifier.pearl(radius: Float, pressed: Boolean = false): Modifier {
    val u = unitPx()
    val shape = RoundedCornerShape(radius * u / androidx.compose.ui.platform.LocalDensity.current.density)
    return this
        .softShadow(
            radius,
            *(if (pressed) arrayOf(Shade(Color(0x1A14265A), 4f, 10f))
            else arrayOf(Shade(Color(0x0A14265A), 1f, 2f), Shade(Color(0x0E14265A), 7f, 16f), Shade(Color(0x1414265A), 20f, 38f))),
        )
        .clip(shape)
        .drawBehind {
            drawRect(cssLinear(176f, size.width, size.height, 0f to K.PearlTop, .52f to K.PearlMid, 1f to K.PearlBottom))
        }
        .border(((if (pressed) 2f else 1f) * u / androidx.compose.ui.platform.LocalDensity.current.density).dp,
            if (pressed) Color(0x99BF913A) else K.GoldRing, shape)
}

/** พื้นวงกลมครีม + วงแหวนทอง ของไอคอน/โลโก้ */
@Composable
fun Modifier.creamDisc(ring: Float = 1.5f, halo: Float = 0f, white: Boolean = false): Modifier {
    val u = unitPx()
    return this
        .drawBehind {
            if (halo > 0f) drawCircle(Color(0x0FBF913A), radius = size.minDimension / 2f + halo * u)
        }
        .clip(androidx.compose.foundation.shape.CircleShape)
        .background(
            if (white) Brush.linearGradient(listOf(Color.White, Color.White))
            else Brush.radialGradient(
                0f to K.PearlTop, .55f to K.CreamMid, 1f to K.CreamEdge,
                center = androidx.compose.ui.geometry.Offset.Unspecified,
            )
        )
        .border((ring * u / androidx.compose.ui.platform.LocalDensity.current.density).dp, K.GoldRingStrong, androidx.compose.foundation.shape.CircleShape)
}
