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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawWithContent
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

/**
 * เบลอบนเครื่องเก่า: BlurMaskFilter ทำงานบน hardware canvas ตั้งแต่ Android 9 (API 28) เท่านั้น
 * Android 7–8 (ตู้ rk3288) จะเรนเดอร์รูปทรงเบลอลงบิตแมป ALPHA_8 บน software canvas ครั้งเดียวแล้วแคช
 * วาดด้วยสีของชั้นนั้น → ภาพเหมือน Android 9+ · บิตแมปย่อครึ่งเมื่อเบลอมาก (เงานุ่มอยู่แล้ว ไม่เห็นความต่าง)
 */
object SoftBlur {
    val needed = android.os.Build.VERSION.SDK_INT < 28
    private val cache = object : android.util.LruCache<String, android.graphics.Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: android.graphics.Bitmap) = value.byteCount
    }

    /** bounds = กรอบรูปทรง (หน่วยของ canvas ปัจจุบัน) · draw วาดรูปทรงสีขาวทึบลงพิกัดเดียวกัน */
    fun draw(canvas: android.graphics.Canvas, key: String, bounds: RectF, radius: Float, color: Color,
             draw: (android.graphics.Canvas, android.graphics.Paint) -> Unit) {
        val pad = radius * 2f + 2f
        val sc = if (radius > 12f) .5f else if (radius < 4f) 2f else 1f
        val bmp = cache.get(key) ?: run {
            val w = ((bounds.width() + pad * 2) * sc).toInt().coerceAtLeast(1)
            val h = ((bounds.height() + pad * 2) * sc).toInt().coerceAtLeast(1)
            val b = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ALPHA_8)
            val c = android.graphics.Canvas(b)
            c.scale(sc, sc); c.translate(pad - bounds.left, pad - bounds.top)
            val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                this.color = android.graphics.Color.WHITE
                maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
            }
            draw(c, p); cache.put(key, b); b
        }
        val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG).apply { this.color = color.toArgb() }
        canvas.drawBitmap(bmp, null, RectF(bounds.left - pad, bounds.top - pad, bounds.right + pad, bounds.bottom + pad), paint)
    }
}

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
                if (SoftBlur.needed && l.blur > 0f) {
                    val br = l.blur * 0.87f * u
                    SoftBlur.draw(c.nativeCanvas, "rr:${r.width().toInt()}:${r.height().toInt()}:${rr.toInt()}:${br.toInt()}", r, br, l.color) { cv, bp ->
                        cv.drawRoundRect(r, rr, rr, bp)
                    }
                } else c.nativeCanvas.drawRoundRect(r, rr, rr, p)
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

/**
 * ผิวไข่มุก (ไล่ขาว → ครีม 176°) + ขอบไล่สีแบบการ์ดเสียบบัตร ใช้กับการ์ด ปุ่ม และแถวรายการ
 * ขอบ 2 หน่วย: เข้มสุดที่มุมซ้ายบน (จุด 14,14) แล้วจางลงเรื่อยๆ จนแทบหายที่มุมขวาล่าง (ไม่มีประกายวิ่ง)
 * ระยะไล่สีเป็นสัดส่วนกับขนาดการ์ด (รัศมี = ระยะถึงมุมไกลสุด) การ์ดเล็กใหญ่จึงไล่สีเหมือนกัน
 * ring = สีขอบ (ทองเป็นค่าเริ่มต้น · หน้ารายละเอียดประกันส่งสีโลโก้บริษัท) · gradientRing = false → ขอบทองเส้นเรียบแบบเดิม
 */
@Composable
fun Modifier.pearl(radius: Float, pressed: Boolean = false, ring: Color = K.GoldGlint, gradientRing: Boolean = true): Modifier {
    val u = unitPx()
    val shape = RoundedCornerShape((radius * u / androidx.compose.ui.platform.LocalDensity.current.density).dp)   // ต้องเป็น Dp — ถ้าส่ง Float จะถูกตีเป็น px ทำให้มุมเล็กลงตาม density
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
        .then(if (!gradientRing) Modifier.border(((if (pressed) 2f else 1f) * u / androidx.compose.ui.platform.LocalDensity.current.density).dp,
            if (pressed) Color(0x99BF913A) else K.GoldRing, shape) else Modifier)   // ขอบทองเส้นเรียบ (ปุ่มหน้าแรก)
        .drawWithContent {
            drawContent()
            if (!gradientRing) return@drawWithContent
            val w = (if (pressed) 2.5f else 2f) * u
            val c = Offset(14f * u, 14f * u)
            val far = kotlin.math.hypot(size.width - c.x, size.height - c.y)
            // สัดส่วนเดียวกับขอบการ์ดเสียบบัตร (หยุดที่ 0 · 70 · 220 · 500 · 800 · 1120 ของรัศมี 1120)
            val brush = Brush.radialGradient(
                0f to lerp(ring, Color.White, .12f), .0625f to ring, .196f to ring.copy(alpha = .55f),
                .446f to ring.copy(alpha = .2f), .714f to ring.copy(alpha = .08f), 1f to ring.copy(alpha = .02f),
                center = c, radius = far)
            // ขอบโลหะเงา: แสงขาวสะท้อน 2 แถบใกล้มุมซ้ายบน วาดทับสีขอบ
            val gloss = Brush.radialGradient(
                0f to Color.White.copy(alpha = .55f), .03f to Color.Transparent, .06f to Color.Transparent,
                .10f to Color(0xF2FFFAEB), .17f to Color.Transparent, .24f to Color.Transparent,
                .30f to Color(0x8CFFFAEB), .38f to Color.Transparent, 1f to Color.Transparent,
                center = c, radius = far)
            val i = w / 2f
            val tl = Offset(i, i); val sz = Size(size.width - w, size.height - w); val cr = CornerRadius(radius * u - i)
            drawRoundRect(brush, tl, sz, cr, style = Stroke(w + 3f * u), alpha = .12f)   // เรืองจางๆ รอบขอบ
            drawRoundRect(brush, tl, sz, cr, style = Stroke(w))
            drawRoundRect(gloss, tl, sz, cr, style = Stroke(w))
        }
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
