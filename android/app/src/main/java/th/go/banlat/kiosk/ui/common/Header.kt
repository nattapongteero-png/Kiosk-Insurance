package th.go.banlat.kiosk.ui.common

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import th.go.banlat.kiosk.ui.common.imgPainter
import th.go.banlat.kiosk.ui.common.img
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.data.thaiDate
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import java.time.LocalDateTime

/** ตราโรงพยาบาลบ้านลาด (วงขาว 88) + "โรงพยาบาลบ้านลาด" / "ระบบลงทะเบียนอัตโนมัติ" (หน้าแรกและหน้าประกันใช้ชุดเดียวกัน) */
@Composable
fun BrandBlock(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.s)) {
        Box(
            Modifier.size(88.s)
                .softShadow(44f, Shade(Color(0x1F14265A), 10f, 30f))
                .clip(CircleShape).background(Color.White).border(1.5.s, Color(0xF2FFFFFF), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(imgPainter(R.drawable.logo_banlat), "โรงพยาบาลบ้านลาด", Modifier.size(62.s), contentScale = ContentScale.Fit)
        }
        Column {
            KText("โรงพยาบาลบ้านลาด", 40, weight = FontWeight.Bold, lineHeight = 44f, softWrap = false)
            KText("ระบบลงทะเบียนอัตโนมัติ", 28, weight = FontWeight.SemiBold, color = K.Blue, lineHeight = 44f, letterSpacing = 1f, softWrap = false)
        }
    }
}

/** วันที่ไทย (พ.ศ.) + เวลา อัปเดตทุกวินาที */
@Composable
fun ClockBlock(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(1000L - now.nano / 1_000_000) } }
    Column(modifier, horizontalAlignment = Alignment.End) {
        KText(thaiDate(now.toLocalDate()), 28, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 44f, align = TextAlign.End, softWrap = false)
        KText("%02d:%02d:%02d".format(now.hour, now.minute, now.second), 46, weight = FontWeight.Bold,
            lineHeight = 44f, letterSpacing = 1f, tabular = true, align = TextAlign.End, softWrap = false)
    }
}


/**
 * แบนเนอร์โรงพยาบาล เต็มกว้าง 1080 x 262 ทุกหน้า — ตรงกับ .banner ใน welcome_v2.html / insurance.html / services.html
 * ไฟล์ภาพ = ต้นฉบับตัดเส้นเขียวท้ายภาพออก (ยืดแนวตั้ง 4%) · ทึบถึง y240 (ใต้ "ระบบลงทะเบียนอัตโนมัติ") แล้วจางจนโปร่ง 0% ที่ขอบล่าง (อบในไฟล์)
 * หน้าแรก: ใต้แบนเนอร์มีชั้นฟ้าไล่เฉดฟุ้งลงไปกลืนพื้นหลัง (fade) · หน้าใน: PageShell วาดชั้นฟุ้งเองใต้พื้นขาว
 */
@Composable
fun HospitalBanner(modifier: Modifier = Modifier, fade: Boolean = true) {   // fade = ฟ้าไล่เฉดใต้แบนเนอร์ (หน้าแรก) · หน้าในมีพื้นขาวรับต่อแล้ว
    fun sm(t: Float) = t * t * (3 - 2 * t)
    Box(modifier.fillMaxWidth().height(600.s)) {
        // ชั้นหลัง (y150→600, ฟ้าเป็นโทนพื้นหลังภายใน y360): เฉพาะสีท้องฟ้าของแบนเนอร์ เบลอแนวนอนจนเป็นฟ้าเนื้อเดียว → โทนพื้นหลัง → จางหาย (banner_fade)
        // ชั้นไล่สีหลังแบนเนอร์ (container แยก): สีฟ้าท้ายแบนเนอร์ ทึบถึง y200 → จางนุ่มพร้อมอ่อนลงเข้าโทนพื้นหลัง จน 0 ที่ y600
        if (fade) Canvas(Modifier.fillMaxWidth().height(600.s)) {
            fun ss(t: Float) = t.coerceIn(0f, 1f).let { it * it * it * (it * (it * 6 - 15) + 10) }
            val c1 = Color(226, 240, 247)
            drawBannerFade(0f, 600f, size.height / 600f, color = { c1 }, alpha = { 1f - ss((it - 200f) / 400f) }, tint = { ss((it - 200f) / 400f) })
        }
        // แบนเนอร์สีเต็ม · ใต้ตัวหนังสือจางลงจนโปร่งที่ขอบล่าง (อบในไฟล์) ละลายลงบนชั้นไล่สีด้านหลัง
        Image(imgPainter(R.drawable.hdr_banlat), "โรงพยาบาลบ้านลาด · ระบบลงทะเบียนอัตโนมัติ",
            Modifier.fillMaxWidth().height(262.s), contentScale = ContentScale.FillBounds)
    }
}

/** สีท้องฟ้าท้ายแบนเนอร์ตามแนวนอน (วัดจากไฟล์ hdr_banlat · ซ้ายฟ้าอ่อน → ขวาฟ้าเข้ม) — ชั้นฟุ้งใต้แบนเนอร์ใช้สีนี้ รอยต่อจึงกลืนเป็นแผ่นเดียว */
internal val BannerSky = listOf(0f to Color(186, 246, 247), 180f to Color(185, 245, 246), 270f to Color(186, 244, 247), 360f to Color(182, 240, 247), 450f to Color(177, 237, 247), 540f to Color(170, 234, 247), 630f to Color(157, 229, 248), 720f to Color(145, 224, 247), 810f to Color(141, 221, 245), 900f to Color(143, 221, 247), 990f to Color(141, 219, 248), 1080f to Color(158, 227, 249))

/** วาดชั้นฟุ้งใต้แบนเนอร์แบบไล่ต่อเนื่อง (ไม่เป็นแถบ):
 *  ① สีท้องฟ้าตามแนวนอนเต็มพื้นที่ ② ทับด้วยสีตามแนวตั้ง color(y) ความทึบ tint(y) → สีค่อยๆ เข้าโทนพื้นหลัง
 *  ③ ตัดความทึบรวมตาม alpha(y) ด้วย DstIn ในเลเยอร์แยก (ใช้ได้ถึง Android 7) */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBannerFade(
    top: Float, height: Float, u: Float, color: (Float) -> Color, alpha: (Float) -> Float, tint: (Float) -> Float) {
    val n = 60
    val ys = (0..n).map { top + height * it / n }
    val rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
    drawIntoCanvas { cv ->
        cv.saveLayer(rect, androidx.compose.ui.graphics.Paint())
        drawRect(Brush.horizontalGradient(*BannerSky.map { (x, c) -> (x / 1080f) to c }.toTypedArray(), startX = 0f, endX = size.width))
        drawRect(Brush.verticalGradient(*ys.mapIndexed { i, y -> (i.toFloat() / n) to color(y).copy(alpha = tint(y)) }.toTypedArray(), startY = 0f, endY = size.height))
        drawRect(Brush.verticalGradient(*ys.mapIndexed { i, y -> (i.toFloat() / n) to Color.Black.copy(alpha = alpha(y)) }.toTypedArray(), startY = 0f, endY = size.height),
            blendMode = androidx.compose.ui.graphics.BlendMode.DstIn)
        cv.restore()
    }
}
