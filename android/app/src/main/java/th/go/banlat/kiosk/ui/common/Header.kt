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
 * แบนเนอร์โรงพยาบาล (เฉพาะหน้าแรก) เต็มกว้าง 1080 x 262 — สูตรเดียวกับ welcome_v2.html
 * ไฟล์ภาพต่อท้องฟ้าแทนเส้นเทา/หญ้าท้ายภาพ · ส่วนท้ายจาง y196→262 แบบ 1−t^2.2 (ข้อความยังชัด แล้วจางเร็วช่วงท้าย) ลงบนหมอกขาว
 * ใต้แบนเนอร์ต่อด้วยฟ้าไล่เฉดจากสีท้ายแบนเนอร์ → ฟ้าอ่อน → โทนพื้นหลัง แล้วจางหาย (→y780) · วางก่อนการ์ด จึงอยู่ใต้การ์ด
 */
@Composable
fun HospitalBanner(modifier: Modifier = Modifier, fade: Boolean = true) {   // fade = ฟ้าไล่เฉดใต้แบนเนอร์ (หน้าแรก) · หน้าในมีพื้นขาวรับต่อแล้ว
    fun sm(t: Float) = t * t * (3 - 2 * t)
    Box(modifier.fillMaxWidth().height(600.s)) {
        // ชั้นหลัง (y150→600, ฟ้าเป็นโทนพื้นหลังภายใน y360): เฉพาะสีท้องฟ้าของแบนเนอร์ เบลอแนวนอนจนเป็นฟ้าเนื้อเดียว → โทนพื้นหลัง → จางหาย (banner_fade)
        // ชั้นไล่สีหลังแบนเนอร์ (container แยก): สีฟ้าท้ายแบนเนอร์ ทึบถึง y200 → จางนุ่มพร้อมอ่อนลงเข้าโทนพื้นหลัง จน 0 ที่ y600
        if (fade) Canvas(Modifier.fillMaxWidth().height(600.s)) {
            fun ss(t: Float) = t * t * t * (t * (t * 6 - 15) + 10)
            val c0 = Color(176, 236, 244); val c1 = Color(226, 240, 247)
            val stops = (0..40).map { i ->
                val y = i * 600f / 40; val t = ss(((y - 200f) / 400f).coerceIn(0f, 1f))
                (i / 40f) to androidx.compose.ui.graphics.lerp(c0, c1, t).copy(alpha = 1f - t)
            }
            drawRect(Brush.verticalGradient(*stops.toTypedArray()))
        }
        // แบนเนอร์: ทึบถึง y222 (ตัวหนังสือ/โลโก้ชัดเต็ม) → 0 ที่ขอบล่าง (อบในไฟล์) ละลายลงบนชั้นไล่สีด้านหลัง
        // แบนเนอร์สีเต็ม · ส่วนล่างโปร่งนุ่ม y206→262 อบไว้ในไฟล์ (ตัวการ์ตูน/ตึกละลายเข้าท้องฟ้า)
        Image(imgPainter(R.drawable.hdr_banlat), "โรงพยาบาลบ้านลาด · ระบบลงทะเบียนอัตโนมัติ",
            Modifier.fillMaxWidth().height(262.s), contentScale = ContentScale.FillBounds)
    }
}

