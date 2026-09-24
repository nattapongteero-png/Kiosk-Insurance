package th.go.banlat.kiosk.ui.welcome

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.ui.common.BrandBlock
import th.go.banlat.kiosk.ui.common.ClockBlock
import th.go.banlat.kiosk.ui.common.KIcon
import th.go.banlat.kiosk.ui.common.LineIcon
import th.go.banlat.kiosk.ui.common.Shade
import th.go.banlat.kiosk.ui.common.creamDisc
import th.go.banlat.kiosk.ui.common.pearl
import th.go.banlat.kiosk.ui.common.press
import th.go.banlat.kiosk.ui.common.softShadow
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.unitPx

/** ชั้นที่ซ้อนบนหน้าแรก */
sealed interface WelcomeOverlay {
    data object None : WelcomeOverlay
    data class Reading(val title: String, val sub: String) : WelcomeOverlay
    data object Keypad : WelcomeOverlay
    data object Consent : WelcomeOverlay
}

/**
 * หน้าแรกของตู้ (Figma 26:38) — เสียบบัตร / กรอก HN หรือเลขบัตร / สแกนหน้า
 * ยืนยันตัวตนสำเร็จ → เด้ง modal ความยินยอม → ยินยอม → onConsentAccepted
 */
@Composable
fun WelcomeScreen(onConsentAccepted: () -> Unit) {
    var overlay by remember { mutableStateOf<WelcomeOverlay>(WelcomeOverlay.None) }
    var next by remember { mutableStateOf(0L) }   // ใช้ key ให้ LaunchedEffect ของชั้นอ่านบัตรเริ่มใหม่ทุกครั้ง

    // TODO(integration): เปลี่ยนเป็น callback จากเครื่องอ่านบัตร / ระบบค้นหา HN / กล้องสแกนหน้า
    fun verify(title: String, sub: String, ms: Long) { overlay = WelcomeOverlay.Reading(title, sub); next = ms }
    LaunchedEffect(overlay, next) {
        if (overlay is WelcomeOverlay.Reading) { delay(next); overlay = WelcomeOverlay.Consent }
    }

    // นาฬิกาแอนิเมชันรอบละ 14 วิ (บัตรไป 7 วิ กลับ 7 วิ · ประกายทองผูกกับจังหวะเดียวกัน)
    val clock = rememberInfiniteTransition(label = "welcome").animateFloat(
        0f, 14000f, infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart), label = "t14")

    val dim = overlay != WelcomeOverlay.None
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().then(if (dim && Build.VERSION.SDK_INT >= 31) Modifier.blur(6.s) else Modifier)) {
            Image(painterResource(R.drawable.bg_home), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            BrandBlock(Modifier.offset(80.s, 80.s))
            ClockBlock(Modifier.align(Alignment.TopEnd).padding(top = 80.s, end = 80.s))

            // ---------- การ์ดเสียบบัตร 920 x 704 ที่ (80,416) ----------
            InsertCard(clock, Modifier.offset(80.s, 416.s).size(920.s, 704.s)) {
                verify("กำลังอ่านบัตรประชาชน", "กรุณาอย่าดึงบัตรออกจนกว่าจะอ่านเสร็จ", 1800)
            }
            EdgeGlint(clock, Modifier.offset(80.s, 416.s).size(920.s, 704.s))

            // ---------- "หรือ" ----------
            Row(Modifier.offset(y = 1216.s).fillMaxWidth().height(40.s),
                horizontalArrangement = Arrangement.spacedBy(24.s, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(170.s, 1.s).background(Color(0x4D14265A)))
                KText("หรือ", 24, weight = FontWeight.Medium, color = K.InkMuted, letterSpacing = .6f)
                Box(Modifier.size(170.s, 1.s).background(Color(0x4D14265A)))
            }

            // ---------- ปุ่มทางเลือก ----------
            Row(Modifier.offset(80.s, 1352.s).size(920.s, 152.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                OptionButton(KIcon.IdCard, "HN หรือบัตรประชาชน", "ลงทะเบียนผู้ป่วยใหม่") { overlay = WelcomeOverlay.Keypad }
                OptionButton(KIcon.FaceScan, "สแกนหน้า", "ลงทะเบียนผู้ป่วยใหม่") {
                    verify("กำลังสแกนใบหน้า", "กรุณามองตรงมาที่กล้องด้านบนของตู้", 2000)
                }
            }

            LangToggle(Modifier.offset(80.s, 1755.s))

            // ปุ่มตั้งค่าระบบ (ทำไว้แค่ปุ่ม) — TODO(integration): ใส่ PIN / กดค้างก่อนเข้าหน้าตั้งค่า
            Box(Modifier.offset(912.s, 1752.s).size(88.s)
                .softShadow(44f, Shade(Color(0x2414265A), 10f, 30f))
                .clip(CircleShape).background(Color(0xB8FFFFFF)).border(1.5.s, Color(0xF2FFFFFF), CircleShape)
                .press(scaleTo = .95f) {}, contentAlignment = Alignment.Center) {
                LineIcon(KIcon.Gear, K.InkMuted, Modifier.size(42.s))
            }
        }

        when (val o = overlay) {
            is WelcomeOverlay.Reading -> ReadingOverlay(o.title, o.sub)
            WelcomeOverlay.Keypad -> KeypadModal(
                onCancel = { overlay = WelcomeOverlay.None },
                onConfirm = { verify("กำลังตรวจสอบข้อมูล", "ระบบกำลังค้นหาข้อมูลผู้ป่วยจากเลขที่ท่านกรอก", 1600) },
            )
            WelcomeOverlay.Consent -> ConsentModal(
                onDecline = { overlay = WelcomeOverlay.None },
                onAccept = { overlay = WelcomeOverlay.None; onConsentAccepted() },
            )
            WelcomeOverlay.None -> Unit
        }
    }
}

/** การ์ดขาวไล่ฟ้า + ลายหลังบัตร + ตัวเครื่อง + ข้อความ · แตะที่การ์ด = จำลองการเสียบบัตร */
@Composable
private fun InsertCard(clock: androidx.compose.runtime.State<Float>, modifier: Modifier, onInsert: () -> Unit) {
    val shape = RoundedCornerShape(48.s)
    Box(modifier
        .softShadow(48f, Shade(Color(0x0A14265A), 2f, 4f), Shade(Color(0x1214265A), 14f, 30f), Shade(Color(0x2114265A), 44f, 84f))
        .clip(shape)
        .background(Brush.verticalGradient(0f to Color.White, .16f to Color.White, .48f to Color(0xFFF7FAFD),
            .76f to Color(0xFFF2F6FB), 1f to Color(0xFFEEF3F9)))
        .border(1.s, Color(0x0F14265A), shape)
        .press(scaleTo = 1f, onClick = onInsert)) {
        Watermark(Modifier.fillMaxSize())
        MachineIllustration(clock, Modifier.fillMaxSize())
        Column(Modifier.offset(64.s, 64.s).width(460.s)) {
            KText("กรุณาเสียบ\nบัตรประชาชน", 72, weight = FontWeight.Bold, lineHeight = 88f, letterSpacing = -1f, softWrap = false)
            Spacer(Modifier.height(32.s))
            Box(Modifier.size(88.s, 6.s)
                .softShadow(3f, Shade(Color(0x4DCC9017), 2f, 8f))
                .clip(RoundedCornerShape(3.s))
                .background(Brush.horizontalGradient(0f to Color(0xFFB4780C), .55f to Color(0xFFE0A93A), 1f to Color(0xFFCC9017))))
            Spacer(Modifier.height(32.s))
            KText("เสียบด้านที่มี", 46, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 64f, softWrap = false)
            ChipIcon(Modifier.padding(vertical = 16.s).size(144.s, 104.s))
            KText("เข้าช่องอ่านบัตร", 46, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 64f, softWrap = false)
        }
    }
}

/**
 * ลายหลังบัตรประชาชน (Figma 71:8) — วางให้เจดีย์องค์ใหญ่อยู่เกือบกลางการ์ด ต่อขวาด้วยภาพกลับด้าน
 * ทึบ 32% ลดความอิ่มสีเหลือ 30% · ขอบบนจาง (y206→358) ให้หัวข้ออยู่บนพื้นขาว
 */
@Composable
private fun Watermark(modifier: Modifier) {
    val img: ImageBitmap = ImageBitmap.imageResource(R.drawable.bg_backcard)
    val u = unitPx()
    val desat = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(.3f) }) }
    Canvas(modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val src = IntSize(img.width, img.height)
        drawImage(img, IntOffset.Zero, src, IntOffset((-77 * u).toInt(), (100 * u).toInt()),
            IntSize((760 * u).toInt(), (760 * u).toInt()), alpha = .32f, colorFilter = desat)
        scale(-1f, 1f, pivot = Offset((683 + 380) * u, 0f)) {
            drawImage(img, IntOffset.Zero, src, IntOffset((683 * u).toInt(), (100 * u).toInt()),
                IntSize((760 * u).toInt(), (760 * u).toInt()), alpha = .32f, colorFilter = desat)
        }
        drawRect(Brush.verticalGradient(0f to Color.Transparent, 206f * u / size.height to Color.Transparent,
            358f * u / size.height to Color.Black, 1f to Color.Black), blendMode = BlendMode.DstIn)
    }
}

/** ภาพชิปทอง (สี่เหลี่ยมผืนผ้า 1.4:1) แทนคำว่า "ชิปสีทอง" */
@Composable
private fun ChipIcon(modifier: Modifier) {
    val lines = remember { PathParser().parsePathString("M38 1v76M70 1v76M1 27h37M1 51h37M70 27h37M70 51h37M38 39h32").toPath() }
    Canvas(modifier.softShadow(17f, Shade(Color(0x478A6A20), 4f, 8f))) {
        scale(size.width / 108f, size.height / 78f, pivot = Offset.Zero) {
            val r = CornerRadius(13f)
            drawRoundRect(Brush.linearGradient(0f to Color(0xFFF8D67A), .6f to Color(0xFFF2C14E), 1f to Color(0xFFD9A63A),
                start = Offset(1f, 1f), end = Offset(107f, 77f)), Offset(1f, 1f), Size(106f, 76f), r)
            drawRoundRect(Color(0xFF8A6A20), Offset(1f, 1f), Size(106f, 76f), r, style = Stroke(1.6f))
            drawPath(lines, Color(0xFF8A6A20), style = Stroke(1.6f))
        }
    }
}

@Composable
private fun RowScope.OptionButton(icon: KIcon, title: String, sub: String, onClick: () -> Unit) {
    Row(Modifier.weight(1f).fillMaxHeight().pearl(32f).press(onClick = onClick).padding(horizontal = 32.s),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.s)) {
        Box(Modifier.size(88.s).creamDisc(halo = 9f), contentAlignment = Alignment.Center) {
            LineIcon(icon, K.GoldIcon, Modifier.size(42.s))
        }
        Column(Modifier.weight(1f)) {
            KText(title, 30, weight = FontWeight.Bold, lineHeight = 36f, softWrap = false, maxLines = 1, minSize = 26)
            Spacer(Modifier.height(4.s))
            KText(sub, 22, color = K.InkMuted, softWrap = false)
        }
    }
}

/** สลับภาษา EN / ไทย — เขียนเป็นโค้ด (เดิมเป็นรูป) · TODO(integration): ผูกกับไฟล์คำแปล */
@Composable
private fun LangToggle(modifier: Modifier) {
    var th by remember { mutableStateOf(true) }
    Row(modifier.size(310.s, 81.s)
        .softShadow(40f, Shade(Color(0x2914265A), 10f, 24f))
        .clip(RoundedCornerShape(99.s)).background(Color(0xB8FFFFFF)).border(1.5.s, Color(0xF2FFFFFF), RoundedCornerShape(99.s))
        .padding(8.s)) {
        listOf(false to "EN", true to "ไทย").forEach { (isTh, label) ->
            val on = th == isTh
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(99.s))
                .then(if (on) Modifier.background(Brush.linearGradient(listOf(K.BlueLight, K.Blue, K.BlueDeep))) else Modifier)
                .press(scaleTo = .97f) { th = isTh }, contentAlignment = Alignment.Center) {
                KText(label, 28, weight = FontWeight.Bold, color = if (on) Color.White else K.InkMuted)
            }
        }
    }
}
