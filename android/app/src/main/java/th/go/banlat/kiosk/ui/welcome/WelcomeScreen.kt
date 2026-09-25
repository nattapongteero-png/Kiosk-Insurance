package th.go.banlat.kiosk.ui.welcome

import androidx.compose.foundation.layout.wrapContentHeight
import th.go.banlat.kiosk.ui.common.imgPainter
import th.go.banlat.kiosk.ui.common.img
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.runtime.derivedStateOf
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
import th.go.banlat.kiosk.data.DemoSession
import th.go.banlat.kiosk.ui.common.HospitalBanner
import th.go.banlat.kiosk.ui.common.KIcon
import th.go.banlat.kiosk.ui.common.LineIcon
import th.go.banlat.kiosk.ui.common.Shade
import th.go.banlat.kiosk.ui.common.creamDisc
import th.go.banlat.kiosk.ui.common.pearl
import th.go.banlat.kiosk.ui.common.press
import th.go.banlat.kiosk.ui.common.softShadow
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.LocalDesignHeight
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.unitPx

/** ชั้นที่ซ้อนบนหน้าแรก */
sealed interface WelcomeOverlay {
    data object None : WelcomeOverlay
    data class Reading(val title: String, val sub: String, val then: WelcomeOverlay = Consent) : WelcomeOverlay
    /** กดอ่านบัตรแล้วอ่านไม่ได้ · fail=false ไม่พบบัตร · fail=true อ่านบัตรไม่สำเร็จ */
    data class ReadError(val fail: Boolean) : WelcomeOverlay
    /** hn = false กรอกเลขบัตรประชาชน · true สแกนบาร์โค้ด / กรอก HN */
    data class Keypad(val hn: Boolean) : WelcomeOverlay
    data object Consent : WelcomeOverlay
}

/**
 * หน้าแรกของตู้ (Figma 26:38) — เสียบบัตร / กรอก HN หรือเลขบัตร / สแกนหน้า
 * ยืนยันตัวตนสำเร็จ → เด้ง modal ความยินยอม → ยินยอม → onConsentAccepted
 */
@Composable
fun WelcomeScreen(
    onConsentAccepted: () -> Unit, onConsentDeclined: () -> Unit = {},
    bye: String? = null, onByeShown: () -> Unit = {},      // แจ้งผลตอนกลับหน้าแรก: "cancel" ยกเลิก / "idle" หมดเวลา
    onBusy: (Boolean) -> Unit = {}, onCancel: () -> Unit = {}, onSettings: () -> Unit = {},
) {
    var overlay by remember { mutableStateOf<WelcomeOverlay>(WelcomeOverlay.None) }
    LaunchedEffect(overlay != WelcomeOverlay.None) { onBusy(overlay != WelcomeOverlay.None) }   // ตัวจับเวลาไม่มีการแตะนับเฉพาะตอนมีชั้นซ้อน
    var byeText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {   // หน้าแรกถูกสร้างใหม่ทุกครั้งที่กลับมา (key) → แสดงครั้งเดียวต่อรอบ
        byeText = when (bye) { "cancel" -> "ยกเลิกรายการแล้ว · กรุณารับบัตรประชาชนคืน"; "idle" -> "หมดเวลาทำรายการ · กรุณารับบัตรประชาชนคืน"; else -> null }
        if (byeText != null) { delay(4000); byeText = null; onByeShown() }
    }
    var next by remember { mutableStateOf(0L) }   // ใช้ key ให้ LaunchedEffect ของชั้นอ่านบัตรเริ่มใหม่ทุกครั้ง

    // TODO(integration): เปลี่ยนเป็น callback จากเครื่องอ่านบัตร / ระบบค้นหา HN / กล้องสแกนหน้า
    fun verify(title: String, sub: String, ms: Long) { overlay = WelcomeOverlay.Reading(title, sub); next = ms }
    LaunchedEffect(overlay, next) {
        (overlay as? WelcomeOverlay.Reading)?.let { delay(next); overlay = it.then }
    }

    // ปุ่มอ่านบัตร — ต้นแบบวนผล: ครั้งที่ 1 ไม่พบบัตร · 2 อ่านไม่สำเร็จ · 3 สำเร็จ → consent (ตรงกับ welcome_v2.html)
    // TODO(integration): ผลจาก NHSO Secure Smartcard Agent (ไม่มีบัตรในช่อง / อ่านชิปไม่ได้ / สำเร็จ)
    var readN by remember { mutableStateOf(0) }
    fun readCard() {
        val r = readN % 3; readN++
        DemoSession.rightsOk = true; DemoSession.selfPay = false
        if (r == 2) verify("กำลังอ่านบัตรประชาชน", "กรุณาอย่าดึงบัตรออกจนกว่าจะอ่านเสร็จ", 1800)
        else { overlay = WelcomeOverlay.Reading("กำลังอ่านบัตรประชาชน", "กรุณาอย่าดึงบัตรออกจนกว่าจะอ่านเสร็จ", WelcomeOverlay.ReadError(fail = r == 1)); next = 1200 }
    }

    // นาฬิกาแอนิเมชันรอบละ 14 วิ (บัตรไป 7 วิ กลับ 7 วิ · ประกายทองผูกกับจังหวะเดียวกัน)
    val clock = rememberInfiniteTransition(label = "welcome").animateFloat(
        0f, 14000f, infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart), label = "t14")

    val dim = overlay != WelcomeOverlay.None
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().then(if (dim && Build.VERSION.SDK_INT >= 31) Modifier.blur(6.s) else Modifier)) {
            Image(imgPainter(R.drawable.bg_home), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            HospitalBanner()

            // ---------- การ์ดเสียบบัตร 920 x 704 ที่ (80,416) ----------
            // ส่วนกลาง (การ์ด → ปุ่มเลือก สูง 1088) ลอยระหว่างหัวจอ (ล่างสุด y208) กับแถวล่าง (H-168)
            // แบ่งที่ว่างบน:ล่าง = 208:248 เท่ากรอบ 1920 → บนจอ 1920 การ์ดอยู่ y416 ตรงแบบเดิม
            val h = LocalDesignHeight.current
            val mid = 262f + maxOf(0f, h - 1518f) * 208f / 456f   // ใต้แบนเนอร์ 262
            InsertCard(clock, Modifier.offset(80.s, mid.s).size(920.s, 704.s), onRead = { readCard() }) {
                DemoSession.rightsOk = true; DemoSession.selfPay = false
                verify("กำลังอ่านบัตรประชาชน", "กรุณาอย่าดึงบัตรออกจนกว่าจะอ่านเสร็จ", 1800)
            }
            EdgeGlint(clock, Modifier.offset(80.s, mid.s).size(920.s, 704.s))

            // ---------- "หรือ" ----------
            Row(Modifier.offset(y = (mid + 788f).s).fillMaxWidth().height(40.s),
                horizontalArrangement = Arrangement.spacedBy(24.s, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(170.s, 2.s).clip(RoundedCornerShape(1.s)).background(Color(0x4714265A)))   // 2 หน่วย: จอเล็กไม่หาย
                KText("หรือ", 24, weight = FontWeight.Medium, color = K.InkMuted, letterSpacing = .6f)
                Box(Modifier.size(170.s, 2.s).clip(RoundedCornerShape(1.s)).background(Color(0x4714265A)))   // 2 หน่วย: จอเล็กไม่หาย
            }

            // ---------- ปุ่มทางเลือก ----------
            // 3 ทางเลือก (ตาม flow ตู้เดิม + ตั้งค่า "Scan ใบหน้า") — ตรงกับ welcome_v2.html .optRow.three
            // TODO(integration): ซ่อนแต่ละปุ่มตามตั้งค่าตู้ (ปิดหน้าจอใส่เลขบัตรประชาชน / ปิด Scan HN / ปิด Scan ใบหน้า)
            Row(Modifier.offset(80.s, (mid + 912f).s).size(920.s, 204.s)   // เว้นบน-ล่างในปุ่ม ~22 · จอเตี้ยสุด 1600 ยังห่างปุ่มภาษา 20
           , horizontalArrangement = Arrangement.spacedBy(24.s)) {
                OptionButton(KIcon.ReadCard, "เลขบัตรประชาชน", "กรอกเลข 13 หลัก") { overlay = WelcomeOverlay.Keypad(hn = false) }
                OptionButton(KIcon.Barcode, "สแกน / กรอก HN", "บาร์โค้ดบัตรโรงพยาบาล") { overlay = WelcomeOverlay.Keypad(hn = true) }
                OptionButton(KIcon.FaceScan, "สแกนใบหน้า", "ยืนยันตัวตนด้วยใบหน้า") {
                    DemoSession.rightsOk = true; DemoSession.selfPay = false
                    verify("กำลังสแกนใบหน้า", "กรุณามองตรงมาที่กล้องด้านบนของตู้", 2000)
                }
            }

            LangToggle(Modifier.offset(80.s, (h - 165f).s))

            // ปุ่มตั้งค่าระบบ → หน้าตั้งค่าตู้ — TODO(integration): ใส่รหัสผ่านตั้งค่า / กดค้างก่อนเข้า
            Box(Modifier.offset(912.s, (h - 168f).s).size(88.s)
                .softShadow(44f, Shade(Color(0x2414265A), 10f, 30f))
                .clip(CircleShape).background(Color(0xB8FFFFFF)).border(1.5.s, Color(0xF2FFFFFF), CircleShape)
                .press(scaleTo = .95f, onClick = onSettings), contentAlignment = Alignment.Center) {
                LineIcon(KIcon.Gear, K.InkMuted, Modifier.size(42.s))
            }
        }

        when (val o = overlay) {
            is WelcomeOverlay.Reading -> ReadingOverlay(o.title, o.sub)
            is WelcomeOverlay.ReadError -> ReadErrorModal(o.fail,
                onAlt = { overlay = if (o.fail) WelcomeOverlay.Keypad(hn = false) else WelcomeOverlay.None },
                onRetry = { readCard() })
            is WelcomeOverlay.Keypad -> KeypadModal(o.hn,
                onCancel = { overlay = WelcomeOverlay.None },
                onConfirm = { DemoSession.rightsOk = false; DemoSession.selfPay = false; verify("กำลังตรวจสอบข้อมูล", "ระบบกำลังค้นหาข้อมูลผู้ป่วยจากเลขที่ท่านกรอก", 1600) },
            )
            WelcomeOverlay.Consent -> ConsentModal(
                onDecline = { overlay = WelcomeOverlay.None; onConsentDeclined() },   // ไม่ยินยอม → ระบบลงทะเบียน
                onAccept = { overlay = WelcomeOverlay.None; onConsentAccepted() },
                onClose = onCancel,   // ยกเลิกทั้งรายการ → กลับหน้าแรก ล้างข้อมูล
            )
            WelcomeOverlay.None -> Unit
        }
        // ป้ายแจ้งผลใต้แบนเนอร์ (ตรงกับ welcome_v2.html .bye) · หายเองใน 4 วิ
        byeText?.let { t ->
            Row(Modifier.align(Alignment.TopCenter).offset(y = 296.s)
                .softShadow(99f, Shade(Color(0x4D14265A), 16f, 40f))
                .clip(RoundedCornerShape(99.s))
                .background(Brush.linearGradient(listOf(Color(0xFF223A7A), K.Ink)))
                .padding(start = 32.s, end = 40.s, top = 22.s, bottom = 22.s),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.s)) {
                LineIcon(KIcon.Info, Color.White, Modifier.size(36.s), 2f)
                KText(t, 30, weight = FontWeight.SemiBold, color = Color.White, softWrap = false)
            }
        }
    }
}

/** การ์ดขาวไล่ฟ้า + ลายหลังบัตร + ตัวเครื่อง + ข้อความ · แตะที่การ์ด = จำลองการเสียบบัตร */
@Composable
private fun InsertCard(clock: androidx.compose.runtime.State<Float>, modifier: Modifier, onRead: () -> Unit, onInsert: () -> Unit) {
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
            Spacer(Modifier.height(24.s))
            Box(Modifier.size(88.s, 6.s)
                .softShadow(3f, Shade(Color(0x4DCC9017), 2f, 8f))
                .clip(RoundedCornerShape(3.s))
                .background(Brush.horizontalGradient(0f to Color(0xFFB4780C), .55f to Color(0xFFE0A93A), 1f to Color(0xFFCC9017))))
            Spacer(Modifier.height(24.s))
            KText("เสียบด้านที่มี", 46, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 64f, softWrap = false)
            ChipIcon(Modifier.padding(vertical = 8.s).size(144.s, 104.s))
            // ปุ่มอ่านบัตร: เสียบบัตรแล้วกดเพื่อสั่งอ่าน (ตามตู้เดิม) · กรมท่า ชิดใต้คำอธิบาย ใกล้ช่องเสียบบัตร
            Box(Modifier.padding(top = 24.s).height(112.s).widthIn(min = 340.s)
                .softShadow(99f, Shade(Color(0x4714265A), 10f, 24f))
                .clip(RoundedCornerShape(99.s))
                .background(Brush.linearGradient(listOf(Color(0xFF223A7A), K.Ink)))
                .press(scaleTo = .97f, onClick = onRead)
                .padding(horizontal = 56.s),
                contentAlignment = Alignment.Center) {
                KText("กดเพื่ออ่านบัตร", 40, weight = FontWeight.Bold, color = Color.White, softWrap = false)
            }
        }
    }
}

/**
 * ลายหลังบัตรประชาชน (Figma 71:8) — วางให้เจดีย์องค์ใหญ่อยู่เกือบกลางการ์ด ต่อขวาด้วยภาพกลับด้าน
 * ทึบ 32% ลดความอิ่มสีเหลือ 30% · ขอบบนจาง (y206→358) ให้หัวข้ออยู่บนพื้นขาว
 */
@Composable
internal fun Watermark(modifier: Modifier) {
    val img: ImageBitmap = img(R.drawable.bg_backcard, 2)
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
internal fun ChipIcon(modifier: Modifier) {
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

/** ปุ่มทางเลือก 3 ปุ่มเรียงแถว: ไอคอนบน · ชื่อ · คำอธิบาย จัดกลาง */
@Composable
private fun RowScope.OptionButton(icon: KIcon, title: String, sub: String, onClick: () -> Unit) {
    Column(Modifier.weight(1f).fillMaxHeight().pearl(32f, gradientRing = false).press(onClick = onClick).padding(horizontal = 12.s),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(76.s).creamDisc(halo = 9f), contentAlignment = Alignment.Center) {
            LineIcon(icon, K.GoldIcon, Modifier.size(38.s))
        }
        KText(title, 28, Modifier.padding(top = 14.s), weight = FontWeight.Bold, lineHeight = 36f, softWrap = false, maxLines = 1, minSize = 24)
        KText(sub, 21, Modifier.padding(top = 4.s), color = K.InkMuted, softWrap = false, maxLines = 1, minSize = 18)
    }
}

/** สลับภาษา EN / ไทย — ใช้รูปปุ่มตามแบบ (lang_th / lang_en) · แตะครึ่งซ้าย = EN ครึ่งขวา = ไทย
 *  TODO(integration): ผูกกับไฟล์คำแปล */
@Composable
private fun LangToggle(modifier: Modifier) {
    var th by remember { mutableStateOf(true) }
    Box(modifier.size(310.s, 81.s)) {
        Image(imgPainter(if (th) R.drawable.lang_th else R.drawable.lang_en),
            if (th) "ภาษา: ไทย" else "Language: English", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        Row(Modifier.fillMaxSize()) {
            listOf(false, true).forEach { isTh ->
                Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(99.s)).press(scaleTo = .97f) { th = isTh })
            }
        }
    }
}
