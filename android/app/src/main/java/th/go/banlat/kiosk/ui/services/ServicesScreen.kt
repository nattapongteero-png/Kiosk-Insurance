package th.go.banlat.kiosk.ui.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import th.go.banlat.kiosk.ui.common.KIcon
import th.go.banlat.kiosk.ui.common.cssLinear
import th.go.banlat.kiosk.ui.common.LineIcon
import th.go.banlat.kiosk.ui.common.Shade
import th.go.banlat.kiosk.ui.common.creamDisc
import th.go.banlat.kiosk.ui.common.pearl
import th.go.banlat.kiosk.ui.common.press
import th.go.banlat.kiosk.ui.common.softShadow
import th.go.banlat.kiosk.ui.insurance.BottomBar
import th.go.banlat.kiosk.ui.insurance.Heading
import th.go.banlat.kiosk.ui.insurance.PageShell
import th.go.banlat.kiosk.ui.insurance.SecondaryPill
import th.go.banlat.kiosk.ui.insurance.topFade
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.unitPx
import th.go.banlat.kiosk.ui.welcome.swallowTaps
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.ui.welcome.SlipCycle
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.AnimatedContent
import th.go.banlat.kiosk.ui.welcome.PrinterIllustration
import th.go.banlat.kiosk.ui.welcome.SlipData
import th.go.banlat.kiosk.ui.welcome.Watermark
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import th.go.banlat.kiosk.ui.insurance.PrimaryPill
import th.go.banlat.kiosk.data.DemoPatient
import th.go.banlat.kiosk.data.DemoSession
import th.go.banlat.kiosk.data.ageYMD
import th.go.banlat.kiosk.data.thaiDate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import java.time.LocalDateTime

/*
 * หน้าเลือกบริการ (ระบบลงทะเบียน / ส่งตรวจ) — ตรงกับ preview/services.html
 * เข้าได้ 2 ทาง: หลังจบ flow ประกัน หรือ "ไม่ยินยอม" ที่ modal ความยินยอม (ประกันไม่ใช่เงื่อนไขการลงทะเบียน)
 * ลำดับ: เลือกบริการ → เลือกลักษณะการมา → บัตรคิว (ข้อมูลชุดเดียวกับสลิปใบนำทางผู้ป่วยนอก)
 * TODO(integration): รายการบริการ/คลินิกดึงจาก HIS · ส่งบริการ + ลักษณะการมาให้ HIS แล้วพิมพ์บัตรคิว
 */
/**
 * tag = ป้ายบนขอบการ์ด = สถานะบริการ "เลือกบริการ" — TODO(integration): บริการที่ปิด/งดรับ เปลี่ยนข้อความและทำการ์ดจาง
 * tint/deep = สีพื้นการ์ด/สีเข้มของบริการ · art = ภาพประกอบ (ยังมีภาพเดียว ที่เหลือใช้ไอคอนในวงขาวแทน)
 */
data class Service(val id: String, val icon: KIcon, val name: String, val sub: String,
                   val tag: String, val tint: Long, val deep: Long, val art: Int? = null,
                   val bullets: List<String> = emptyList(), val title: String = name,
                   val fit: ArtFit = ArtFit(-1.213f, -.28f, 1.34f, -9f))

/**
 * ตำแหน่งภาพประกอบ หน่วย P (ความสูงแผง) — ตรงกับ af ใน services.html
 * x = ขอบซ้ายภาพจากขอบขวาแผง · y = บนภาพจากบนแผง · h = สูงภาพ · cl = ขอบตัดซ้าย (จากขอบขวาแผง)
 * ภาพแต่ละบริการครอปตาม Figma แล้วเลื่อนลงให้ส่วนบนสุดห่างขอบการ์ด 24 เท่าขอบบนป้าย
 */
data class ArtFit(val x: Float, val y: Float, val h: Float, val cl: Float)   // title = ชื่อบนแผง (ขึ้นบรรทัดตาม \n)

private val Main = Service("opd", KIcon.Steth, "ตรวจโรคทั่วไป", "เจ็บป่วยทั่วไป ไม่มีนัดหมาย", "เลือกบริการ", 0xFFDCEBFB, 0xFF0D5BC6, R.drawable.art_opd, bullets = listOf("เจ็บป่วยทั่วไป", "ไม่มีนัดหมายล่วงหน้า"), title = "ตรวจ\nโรคทั่วไป", fit = ArtFit(-1.281f, -0.322f, 1.561f, -1.281f))
private val Clinics = listOf(
    Service("wound", KIcon.Bandage, "ฉีดยา / ทำแผล", "ฉีดยาตามนัด ล้างแผล ตัดไหม", "เลือกบริการ", 0xFFFBE6E1, 0xFFC2410C, art = R.drawable.art_wound, bullets = listOf("ฉีดยาตามนัด", "ล้างแผล / ทำแผล", "ตัดไหม"), title = "ฉีดยา /\nทำแผล", fit = ArtFit(-1.376f, -0.256f, 1.369f, -1.375f)),
    Service("med", KIcon.Heart, "อายุรกรรม", "โรคเรื้อรัง เบาหวาน ความดัน", "เลือกบริการ", 0xFFFBE3EC, 0xFFBE185D, art = R.drawable.art_med, bullets = listOf("โรคเรื้อรัง", "เบาหวาน", "ความดันโลหิตสูง"), fit = ArtFit(-1.431f, -0.304f, 1.895f, -1.381f)),
    Service("dent", KIcon.Tooth, "ทันตกรรม", "ตรวจฟัน อุดฟัน ถอนฟัน", "เลือกบริการ", 0xFFDDF1F8, 0xFF0E7490, art = R.drawable.art_dent, bullets = listOf("ตรวจฟัน", "อุดฟัน / ขูดหินปูน", "ถอนฟัน"), fit = ArtFit(-1.134f, -0.36f, 1.471f, -1.134f)),
    Service("physio", KIcon.Physio, "กายภาพบำบัด", "ฟื้นฟูร่างกาย ปวดกล้ามเนื้อ", "เลือกบริการ", 0xFFE1F3EA, 0xFF15803D, art = R.drawable.art_physio, bullets = listOf("ฟื้นฟูร่างกาย", "ปวดคอ บ่า หลัง", "ปวดกล้ามเนื้อ"), title = "กายภาพ\nบำบัด", fit = ArtFit(-1.807f, -0.102f, 1.557f, -1.375f)),
    Service("thai", KIcon.Leaf, "แพทย์แผนไทย", "นวด ประคบ สมุนไพร", "เลือกบริการ", 0xFFEDF3DC, 0xFF4D7C0F, art = R.drawable.art_thai, bullets = listOf("นวดรักษา", "ประคบสมุนไพร", "ยาสมุนไพร"), title = "แพทย์\nแผนไทย", fit = ArtFit(-2.364f, -0.309f, 1.726f, -1.375f)),
    Service("psy", KIcon.Mind, "จิตเวช", "ปรึกษาสุขภาพใจ", "เลือกบริการ", 0xFFEAE6FA, 0xFF6D28D9, art = R.drawable.art_psy, bullets = listOf("ปรึกษาสุขภาพใจ", "นอนไม่หลับ / เครียด"), fit = ArtFit(-2.82f, -0.38f, 1.875f, -1.169f)),
)
private val Appointments = listOf(
    Service("lab", KIcon.Flask, "LAB มาก่อนนัด", "เจาะเลือด / ตรวจแล็บ ก่อนพบแพทย์", "เลือกบริการ", 0xFFFDF0D9, 0xFFB45309, art = R.drawable.ins_couple, bullets = listOf("เจาะเลือด", "ตรวจแล็บก่อนพบแพทย์"), title = "LAB\nมาก่อนนัด"),
    Service("xray", KIcon.Xray, "X-RAY มาก่อนนัด", "เอกซเรย์ ก่อนพบแพทย์", "เลือกบริการ", 0xFFE3E8F6, 0xFF3730A3, art = R.drawable.ins_couple, bullets = listOf("เอกซเรย์", "ก่อนพบแพทย์ตามนัด"), title = "X-RAY\nมาก่อนนัด"),
)
private data class Arrive(val id: String, val icon: KIcon, val name: String)
private val Arrivals = listOf(
    Arrive("walk", KIcon.Walk, "เดินมาเอง"), Arrive("wheel", KIcon.Wheelchair, "รถเข็น / รถนั่ง"),
    Arrive("bed", KIcon.Stretcher, "เปลนอน"), Arrive("carry", KIcon.Carry, "อุ้มมา"),
)
private val Proxy = Arrive("proxy", KIcon.Family, "ญาติมาแทน")

@Composable
fun ServicesScreen(onExit: () -> Unit, onQueued: (service: String, arrive: String) -> Unit = { _, _ -> }) {
    var pick by remember { mutableStateOf<Service?>(null) }
    var ticket by remember { mutableStateOf<Pair<Service, String>?>(null) }
    var confirm by remember { mutableStateOf<Pair<Service, String>?>(null) }   // รอกดยืนยันการมารับบริการก่อนพิมพ์บัตรคิว (ตาม flow ตู้เดิม)
    var rightsDone by remember { mutableStateOf(false) }
    if (!rightsDone) { RightsScreen(onCancel = onExit, onNext = { if (!DemoSession.rightsOk) DemoSession.selfPay = true; rightsDone = true }); return }
    // หน้าเดียว 2 สถานะ: รอยืนยัน (เครื่องพิมพ์ว่าง) → กดยืนยัน → พิมพ์บัตรคิวบนการ์ดเดิม ไม่เปลี่ยนหน้า
    (ticket ?: confirm)?.let { (sv, ar) ->
        TicketScreen(sv, Arrivals.plus(Proxy).first { it.id == ar }.name, wait = ticket == null, onCancel = onExit,
            onConfirm = { ticket = sv to ar; onQueued(sv.id, ar) }, onDone = onExit); return
    }
    val u = unitPx()
    val scroll = rememberScrollState()
    Box(Modifier.fillMaxSize()) {
        PageShell {
            Column(Modifier.padding(top = 550.s).fillMaxSize()
                .topFade(16f * u, 96f * u) { scroll.value > 4 }
                .verticalScroll(scroll)
                .padding(start = 80.s, end = 80.s, top = 8.s, bottom = 336.s),
                verticalArrangement = Arrangement.spacedBy(48.s)) {
                Heading("เลือกบริการที่ต้องการ", "แตะบริการเพื่อรับบัตรคิว")
                // ตรวจโรคทั่วไป (ใช้บ่อยที่สุด) อยู่ใบแรกของรายการ ขนาดเท่าการ์ดอื่น
                listOf(null to listOf(Main) + Clinics, "มาตามนัด" to Appointments).forEach { (title, list) ->
                    Section(title) {   // กลุ่มแรกไม่มีหัวข้อ (ต่อจาก "เลือกบริการที่ต้องการ")
                        Column(verticalArrangement = Arrangement.spacedBy(32.s)) {
                            list.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(24.s)) {
                                    row.forEach { s -> ServiceCard(s, main = false, Modifier.weight(1f)) { pick = s } }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))   // ใบสุดท้ายเดี่ยว: กว้างเท่าครึ่งแถว
                                }
                            }
                        }
                    }
                }
            }
            BottomBar { SecondaryPill("ยกเลิก, กลับหน้าแรก", onExit) }
        }
        pick?.let { s -> ArriveModal(s, onCancel = { pick = null }, onPick = { a -> pick = null; confirm = s to a }) }
    }
}

@Composable
private fun Disc(icon: KIcon, size: Int) {
    Box(Modifier.size(size.s).creamDisc(halo = 8f), contentAlignment = Alignment.Center) {
        LineIcon(icon, K.GoldIcon, Modifier.size((size / 2).s), 1.7f)
    }
}

@Composable
private fun Section(title: String?, gap: Int = 24, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(gap.s)) {
        if (title != null) KText(title, 32, Modifier.padding(bottom = (24 - gap).s), weight = FontWeight.Bold, color = K.InkMuted, lineHeight = 44f, letterSpacing = .5f)
        content()
    }
}

/** ท่านมาในลักษณะใด (เดิม "ประเภทสภาพผู้ป่วย") — การ์ดใหญ่มีไอคอน 2 คอลัมน์ + ญาติมาแทนเต็มแถว */
@Composable
private fun ArriveModal(s: Service, onCancel: () -> Unit, onPick: (String) -> Unit) {
    Box(Modifier.fillMaxSize().background(K.ScrimLight).swallowTaps().padding(horizontal = 80.s), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth()
            .softShadow(40f, Shade(Color(0x59061432), 40f, 100f))
            .clip(RoundedCornerShape(40.s))
            .background(Brush.linearGradient(0f to K.PearlTop, .55f to K.PearlMid, 1f to K.PearlBottom))
            .padding(56.s), verticalArrangement = Arrangement.spacedBy(40.s)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                KText(s.name, 26, color = K.InkMuted, lineHeight = 36f)
                KText("ท่านมาในลักษณะใด", 44, Modifier.padding(top = 4.s), weight = FontWeight.Bold, lineHeight = 60f)
                KText("เพื่อให้เจ้าหน้าที่เตรียมรับได้ถูกต้อง", 26, Modifier.padding(top = 8.s), color = K.InkMuted, lineHeight = 40f, align = TextAlign.Center)
            }
            Column(verticalArrangement = Arrangement.spacedBy(24.s)) {
                Arrivals.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.s)) {
                        row.forEach { a ->
                            Column(Modifier.weight(1f).heightIn(min = 128.s).pearl(24f, gradientRing = false).press { onPick(a.id) }.padding(24.s),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                KText(a.name, 32, weight = FontWeight.Bold, lineHeight = 44f)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().heightIn(min = 128.s).pearl(24f, gradientRing = false).press { onPick(Proxy.id) }.padding(horizontal = 32.s, vertical = 24.s),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    KText(Proxy.name, 32, weight = FontWeight.Bold, lineHeight = 44f)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { SecondaryPill("ยกเลิก", onCancel) }
        }
    }
}


/* ---------- บัตรคิว ----------
 * TODO(integration): QN / คิวเรียกคนไข้ / ห้อง / สถานะสิทธิ / แพ้ยา / QR รับจาก HIS
 * ต้นแบบ: ตัวอักษรนำหน้าคิวตามบริการ · QR เป็นลายจำลอง (ไม่ใช่ QR จริง)
 */
private val Room = mapOf("opd" to "ห้องตรวจ", "wound" to "ห้องฉีดยาทำแผล", "med" to "ห้องตรวจอายุรกรรม", "dent" to "ทันตกรรม",
    "physio" to "กายภาพบำบัด", "thai" to "แผนไทย / ฝังเข็ม", "psy" to "จิตเวช", "lab" to "ห้องปฏิบัติการ (LAB)", "xray" to "ห้องเอกซเรย์")
/** ห้องที่ติ๊กบนสลิป (ชุดเดียวกับใบจริง) · LAB/X-RAY ติ๊ก "Lab/X-Ray วันนี้" แทน */
private val SlipRoomOf = mapOf("opd" to "ห้องตรวจ", "med" to "ห้องตรวจ", "wound" to "ห้องฉีดยาทำแผล", "dent" to "ทันตกรรม",
    "physio" to "กายภาพบำบัด", "thai" to "แผนไทย/ฝังเข็ม", "psy" to "จิตเวช")
private val Prefix = mapOf("opd" to "A", "wound" to "B", "med" to "C", "dent" to "D", "physio" to "E", "thai" to "F", "psy" to "G", "lab" to "L", "xray" to "X")

@Composable
private fun TicketScreen(sv: Service, arrive: String, wait: Boolean, onCancel: () -> Unit, onConfirm: () -> Unit, onDone: () -> Unit) {
    val now = remember { LocalDateTime.now() }
    PageShell(fullVeil = true) {
        // การ์ดรับบัตรคิว + สรุปข้อมูลที่ใช้รับบริการ · จัดกลางระหว่างข้อมูลผู้ป่วยกับแถบปุ่ม
        Column(Modifier.fillMaxSize().padding(start = 80.s, end = 80.s, top = 558.s, bottom = 328.s),
            verticalArrangement = Arrangement.spacedBy(32.s, Alignment.CenterVertically)) {
            PrintCard(sv, arrive, now, wait)
            val right = if (DemoSession.rightsOk) DemoSession.rights.main else "ชำระเงินเอง"
            Column(Modifier.fillMaxWidth().pearl(24f, gradientRing = false).padding(horizontal = 40.s, vertical = 8.s)) {
                listOf("บริการ" to sv.name, "ลักษณะการมา" to arrive, "สิทธิการรักษาที่จะใช้" to right).forEachIndexed { i, (k, v) ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.s).background(Color(0x1414265A)))
                    Row(Modifier.fillMaxWidth().padding(vertical = 20.s), horizontalArrangement = Arrangement.spacedBy(32.s),
                        verticalAlignment = Alignment.CenterVertically) {
                        KText(k, 26, color = K.InkSoft, lineHeight = 36f, softWrap = false)
                        Spacer(Modifier.weight(1f))
                        val use = i == 2
                        Column(horizontalAlignment = Alignment.End) {
                            KText(v, if (use) 32 else 30, weight = FontWeight.Bold, color = if (use) K.BlueDeep else K.Ink,
                                lineHeight = 40f, align = TextAlign.End)
                            if (use && !DemoSession.rightsOk)
                                KText("ชำระค่าบริการที่ห้องการเงินหลังพบแพทย์", 22, weight = FontWeight.Medium, color = Color(0xFF7A4A0A), lineHeight = 32f, align = TextAlign.End)
                        }
                    }
                }
            }
        }
        BottomBar {
            if (wait) Row(Modifier.fillMaxWidth().padding(horizontal = 80.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                SecondaryPill("ยกเลิกการทำรายการ", onCancel, Modifier.widthIn(min = 280.s))
                PrimaryPill("ยืนยันการรับบริการ", onConfirm, Modifier.weight(1f))
            } else PrimaryPill("เสร็จสิ้น, กลับหน้าแรก", onDone, Modifier.padding(horizontal = 80.s).fillMaxWidth())   // ปุ่มหลักเดี่ยว = เต็มความกว้าง
        }
    }
}

/** รอยปรุ: เส้นประ + รอยเว้าครึ่งวงกลมสองข้าง */
@Composable
private fun Perforation() {
    val u = unitPx()
    Box(Modifier.fillMaxWidth().height(40.s)) {
        Canvas(Modifier.fillMaxSize().padding(horizontal = 40.s)) {
            drawLine(Color(0x2414265A), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2f * u,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * u, 6f * u)))
        }
        Box(Modifier.offset((-20).s, 0.s).size(40.s).clip(CircleShape).background(Color.White).border(1.s, Color(0x52BF913A), CircleShape))
        Box(Modifier.align(Alignment.TopEnd).offset(20.s, 0.s).size(40.s).clip(CircleShape).background(Color.White).border(1.s, Color(0x52BF913A), CircleShape))
    }
}

/** ลาย QR จำลอง 25x25 + จุดมุม 3 จุด (สูตรสุ่มเดียวกับ HTML) — TODO(integration): ใช้ QR จริงจาก HIS */
@Composable
private fun FakeQr(modifier: Modifier) {
    val cells = remember {
        var r = 14L * 9301 + 49297   // Long กันล้นตอนคูณ
        fun rnd(): Double { r = (r * 9301 + 49297) % 233280; return r / 233280.0 }
        fun finder(x: Int, y: Int) = (x < 7 && y < 7) || (x > 17 && y < 7) || (x < 7 && y > 17)
        buildList { for (y in 0 until 25) for (x in 0 until 25) if (!finder(x, y) && rnd() > .52) add(x to y) }
    }
    Canvas(modifier) {
        val c = size.width / 25f; val ink = K.Ink
        cells.forEach { (x, y) -> drawRect(ink, Offset(x * c, y * c), Size(c, c)) }
        listOf(0 to 0, 18 to 0, 0 to 18).forEach { (fx, fy) ->
            drawRect(ink, Offset(fx * c, fy * c), Size(7 * c, 7 * c))
            drawRect(Color.White, Offset((fx + 1) * c, (fy + 1) * c), Size(5 * c, 5 * c))
            drawRect(ink, Offset((fx + 2) * c, (fy + 2) * c), Size(3 * c, 3 * c))
        }
    }
}

/* ---------- การ์ดรับบัตรคิว — ตัวเครื่องเดียวกับการ์ดเสียบบัตรหน้าแรก (ตรงกับ services.html .pcard) ----------
 * 920 x 704 มุม 48 · พื้น ลาย ตัวเครื่อง มุม/องศาเหมือนหน้าแรกทุกพิกัด → เล่าเรื่องต่อกัน: เสียบบัตร → บัตรคิวพิมพ์ออกมา
 * ซ้าย: สถานะ → "กรุณารับ / บัตรคิว" → เส้นทอง → เลขคิว + QN ตัวใหญ่ (อ่านง่ายกว่าบนบัตรเอียง) → ไปห้องไหน
 * แอนิเมชันวนรอบละ 7.5 วิ: พิมพ์ออกจนสุดใบ (กระดาษโค้งตามน้ำหนัก) → ค้างให้อ่าน → หลุดเลื่อนออกนอกการ์ด → พิมพ์ใหม่
 */
@Composable
private fun PrintCard(sv: Service, arrive: String, now: LocalDateTime, wait: Boolean) {
    val u = unitPx()
    // วนรอบละ 7.5 วิ: พิมพ์ออกจนสุดใบ → ค้าง → หลุดเลื่อนออกนอกการ์ด → พิมพ์ใหม่ · รอยืนยัน = เวลา 0 (ยังไม่มีกระดาษ ไฟดับ)
    val idle = remember { mutableFloatStateOf(0f) }
    val sec = if (wait) idle else rememberInfiniteTransition(label = "print").animateFloat(0f, 7.5f,
        infiniteRepeatable(tween(7500, easing = LinearEasing), RepeatMode.Restart), label = "sec")
    val tm = listOf("ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
    val slip = remember(sv, arrive) {
        val p = DemoPatient; val dob = p.dob
        SlipData("${Prefix.getValue(sv.id)}003", "14",
            printed = "${now.dayOfMonth} ${tm[now.monthValue - 1]} ${(now.year + 543) % 100} %02d:%02d น.".format(now.hour, now.minute),
            rows = listOf("HN" to p.hn, "ชื่อ-สกุล" to p.name,
                "อายุ" to "${ageYMD(dob)} (${dob.dayOfMonth} ${tm[dob.monthValue - 1]} ${dob.year + 543})",
                "วันที่รับบริการ" to "${now.dayOfMonth} ${tm[now.monthValue - 1]} ${now.year + 543} %02d:%02d น.".format(now.hour, now.minute),
                "ประเภทการมา" to "$arrive (KIOSK)", "แพ้ยา" to "ไม่มีประวัติแพ้ยาในโรงพยาบาล", "สิทธิการรักษา" to DemoSession.rightLabel),
            room = SlipRoomOf[sv.id], labToday = sv.id == "lab" || sv.id == "xray", status = if (DemoSession.rightsOk) "ผ่านการตรวจสอบสิทธิแล้ว" else "ชำระเงินเอง", authen = DemoSession.rights.authen)
    }
    val shape = RoundedCornerShape(48.s)
    Box(Modifier.fillMaxWidth().height(704.s)
        .softShadow(48f, Shade(Color(0x0A14265A), 2f, 4f), Shade(Color(0x1214265A), 14f, 30f), Shade(Color(0x2114265A), 44f, 84f))
        .clip(shape)
        .background(Brush.verticalGradient(0f to Color.White, .16f to Color.White, .48f to Color(0xFFF7FAFD), .76f to Color(0xFFF2F6FB), 1f to Color(0xFFEEF3F9)))
        .border(1.s, Color(0x0F14265A), shape)
        .drawWithContent {
            drawContent()
            // ขอบทอง + แสงสะท้อน (ชุดเดียวกับการ์ดเสียบบัตร · ไม่มีประกายวิ่ง)
            val c = Offset(14f * u, 14f * u); val w = 2f * u; val i = w / 2
            val far = kotlin.math.hypot(size.width - c.x, size.height - c.y)
            val tl = Offset(i, i); val sz = Size(size.width - w, size.height - w); val cr = CornerRadius(48f * u - i)
            drawRoundRect(Brush.radialGradient(0f to Color(0xFFE2AE4C), 70f / 1120f to Color(0xFFD9A546), 220f / 1120f to Color(0x73D6A850),
                500f / 1120f to Color(0x29D6A850), 800f / 1120f to Color(0x0FD6A850), 1f to Color(0x00D6A850), center = c, radius = 1120f * u),
                tl, sz, cr, style = Stroke(w))
            drawRoundRect(Brush.radialGradient(0f to Color.White.copy(alpha = .55f), .03f to Color.Transparent, .06f to Color.Transparent,
                .10f to Color(0xF2FFFAEB), .17f to Color.Transparent, .24f to Color.Transparent, .30f to Color(0x8CFFFAEB), .38f to Color.Transparent,
                1f to Color.Transparent, center = c, radius = far), tl, sz, cr, style = Stroke(w))
        }) {
        Watermark(Modifier.fillMaxSize())
        PrinterIllustration(sec, slip, Modifier.fillMaxSize())
        if (wait) Column(Modifier.offset(64.s, 64.s).width(420.s)) {
            // รอยืนยัน: tag ทอง → หัวข้อ → เส้นทอง → คำแนะนำ (ตรงกับ services.html .ok.wait)
            Row(Modifier.clip(RoundedCornerShape(99.s)).background(Color(0xFFFBF3E2)).padding(start = 12.s, end = 18.s, top = 6.s, bottom = 6.s),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.s)) {
                LineIcon(KIcon.Doc, Color(0xFFA87C2A), Modifier.size(24.s), 2.2f)
                KText("รอยืนยัน", 24, weight = FontWeight.SemiBold, color = Color(0xFF8A6420), lineHeight = 32f)
            }
            KText("กรุณายืนยัน\nการรับบริการ", 72, Modifier.padding(top = 24.s), weight = FontWeight.Bold, lineHeight = 88f, letterSpacing = -1f, softWrap = false)
            Box(Modifier.padding(vertical = 24.s).size(88.s, 6.s).clip(RoundedCornerShape(3.s))
                .background(Brush.horizontalGradient(0f to Color(0xFFB4780C), .55f to Color(0xFFE0A93A), 1f to Color(0xFFCC9017))))
            KText(buildAnnotatedString {
                append("ตรวจสอบข้อมูลด้านล่าง\nแล้วกด "); withStyle(SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold)) { append("ยืนยันการรับบริการ") }
            }, 28, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 44f)
        } else Column(Modifier.offset(64.s, 64.s).width(420.s)) {
            // สถานะตามจังหวะพิมพ์ (สูตรเดียวกับ services.html) · TODO(integration): เปลี่ยนตามสัญญาณเครื่องพิมพ์จริง (กำลังพิมพ์ / เสร็จ / ขัดข้อง)
            val printing by remember { derivedStateOf { (sec.value % SlipCycle) - .3f < 3.6f } }
            val shape = RoundedCornerShape(99.s)
            Row(Modifier.clip(shape).background(if (printing) K.Blue050 else K.Green050).padding(start = 12.s, end = 18.s, top = 6.s, bottom = 6.s),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.s)) {
                if (printing) Canvas(Modifier.size(24.s).padding(2.s)) {   // วงหมุนเล็ก (หมุนตามนาฬิกาแอนิเมชัน อ่านค่าในขั้นวาด ไม่ recompose ทุกเฟรม)
                    rotate(sec.value * 360f) {
                        drawCircle(Brush.sweepGradient(0f to Color(0x001E7BE0), .42f to Color(0x001E7BE0), 1f to K.Blue),
                            radius = size.minDimension / 2 - 1.5f * u, style = Stroke(3f * u))
                    }
                } else LineIcon(KIcon.Check, K.Green, Modifier.size(24.s), 3f)
                KText(if (printing) "กำลังพิมพ์บัตรคิว" else "ลงทะเบียนสำเร็จ", 24, weight = FontWeight.SemiBold,
                    color = if (printing) K.BlueDeep else K.GreenDeep, lineHeight = 32f)
            }
            // หัวข้อ: รอ → รับ (เด้งขึ้นเบาๆ ตอนพิมพ์เสร็จ)
            AnimatedContent(printing, Modifier.padding(top = 24.s), label = "ttl",
                transitionSpec = {
                    if (targetState) EnterTransition.None togetherWith ExitTransition.None
                    else (fadeIn(tween(450)) + slideInVertically(tween(450, easing = CubicBezierEasing(.2f, .8f, .2f, 1f))) { it / 12 }) togetherWith ExitTransition.None
                }) { p ->
                KText(if (p) "กรุณารอ\nสักครู่" else "กรุณารับ\nบัตรคิว", 72, weight = FontWeight.Bold, lineHeight = 88f, letterSpacing = -1f, softWrap = false)
            }
            Box(Modifier.padding(vertical = 24.s).size(88.s, 6.s).clip(RoundedCornerShape(3.s))
                .background(Brush.horizontalGradient(0f to Color(0xFFB4780C), .55f to Color(0xFFE0A93A), 1f to Color(0xFFCC9017))))
            Row(horizontalArrangement = Arrangement.spacedBy(32.s)) {
                Column { KText("คิวเรียกคนไข้", 24, color = K.InkMuted, lineHeight = 36f); QNum(slip.queue, K.BlueDeep, printing, sec) }
                Column { KText("QN", 24, color = K.InkMuted, lineHeight = 36f); QNum(slip.qn, K.Ink, printing, sec) }
            }
            // ห้องที่ต้องไป: skeleton เหมือนเลขคิวระหว่างพิมพ์
            Reveal(printing, sec, Modifier.padding(top = 24.s), vInset = 6f, radius = 10f) {
                KText(buildAnnotatedString {
                    append("แล้วไปที่ "); withStyle(SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold)) { append(Room.getValue(sv.id)) }
                }, 32, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 48f, softWrap = false)
            }
        }
    }
}

/** เลขคิว 88 ในกล่องสูง 100 (ฟอนต์ไทยตัวใหญ่เผื่อระยะสระมาก จึงล็อกความสูงเท่า HTML) · skeleton ระหว่างพิมพ์ */
@Composable
private fun QNum(text: String, color: Color, loading: Boolean, sec: androidx.compose.runtime.State<Float>) {
    Reveal(loading, sec, Modifier.height(100.s), vInset = 20f, radius = 14f) {
        KText(text, 88, Modifier.wrapContentHeight(unbounded = true),
            weight = FontWeight.Bold, color = color, lineHeight = 100f, letterSpacing = 1f, tabular = true, softWrap = false)
    }
}

/**
 * ระหว่างพิมพ์: แทนเนื้อหาด้วย skeleton เทาอมฟ้า (กว้างเท่าเนื้อหาจริง เว้นบน/ล่าง vInset) + แสงวิ่งผ่าน 1.4 วิ/รอบ
 * พิมพ์เสร็จ: เนื้อหาจริงเด้งขึ้นเบาๆ (เท่า .pop ใน services.html)
 */
@Composable
private fun Reveal(loading: Boolean, sec: androidx.compose.runtime.State<Float>, modifier: Modifier, vInset: Float, radius: Float,
                   content: @Composable () -> Unit) {
    val u = unitPx()
    val show = remember { Animatable(if (loading) 0f else 1f) }
    LaunchedEffect(loading) {
        if (loading) show.snapTo(0f) else show.animateTo(1f, tween(450, easing = CubicBezierEasing(.2f, .8f, .2f, 1f)))
    }
    Box(modifier.drawWithContent {
        if (loading) {
            val ph = (sec.value % 1.4f) / 1.4f
            val w = size.width; val x = -w + ph * w * 3f
            drawRoundRect(Brush.linearGradient(0f to Color(0xFFD6E0EC), .36f to Color(0xFFD6E0EC), .5f to Color(0xFFEEF3F9),
                .64f to Color(0xFFD6E0EC), 1f to Color(0xFFD6E0EC), start = Offset(x, 0f), end = Offset(x + w, 0f)),
                Offset(0f, vInset * u), Size(w, size.height - 2 * vInset * u), CornerRadius(radius * u))
        } else drawContent()
    }.graphicsLayer { alpha = show.value; translationY = (1f - show.value) * 12f * u }, contentAlignment = Alignment.CenterStart) {
        content()
    }
}

/** เลขคิวตัวใหญ่ 112 ในกล่องสูง 120 เท่า HTML — ฟอนต์ไทยขนาดใหญ่มีระยะบน/ล่างเผื่อสระเยอะ จึงล็อกความสูงแล้ววางกึ่งกลาง */
@Composable
private fun BigNum(text: String, color: Color) {
    Box(Modifier.height(120.s), contentAlignment = Alignment.Center) {
        KText(text, 112, Modifier.wrapContentHeight(unbounded = true), weight = FontWeight.Bold, color = color,
            lineHeight = 120f, letterSpacing = 2f, tabular = true, softWrap = false)
    }
}

/**
 * การ์ดบริการแบบการ์ดคอร์ส (ภาพอ้างอิงจากผู้ใช้ · ตรงกับ services.html .sc)
 * การ์ดขาว → แผงสี ขอบหมึก 2.5 (ป้ายหมึกเข้มคร่อมขอบบนซ้าย · ชื่อตัวใหญ่ · ภาพประกอบล้นเหนือแผง ตัดที่ขอบล่างแผง) → รายการใต้แผง
 * ภาพประกอบ: ใช้ภาพคุณลุงคุณป้าทุกการ์ดไปก่อน — รอภาพแยกแต่ละบริการ
 */
@Composable
private fun ServiceCard(s: Service, main: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val u = unitPx()
    val tint = Color(s.tint); val deep = Color(s.deep)
    val panelH = if (main) 236 else 196
    Box(modifier.pearl(32f, gradientRing = false).press(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(start = 24.s, end = 24.s, top = 44.s, bottom = 24.s)) {   // ขอบการ์ด → ของชิ้นแรก 24 ทุกด้าน (บนวัดถึงขอบบนป้าย → แผงเริ่ม 44)
            val pShape = RoundedCornerShape(28.s)
            Box(Modifier.fillMaxWidth().height(panelH.s).clip(pShape)
                .drawBehind { drawRect(cssLinear(160f, size.width, size.height, 0f to Color.White, .45f to tint, 1f to tint)) }
                .border(2.5.s, K.Ink, pShape))
            val bullet = @Composable { t: String ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.s)) {
                    Box(Modifier.padding(top = 13.s).size(8.s).clip(CircleShape).background(deep))
                    KText(t, 24, color = K.InkMuted, lineHeight = 34f)
                }
            }
            if (main) Row(Modifier.padding(start = 8.s, top = 20.s), horizontalArrangement = Arrangement.spacedBy(40.s)) { s.bullets.forEach { bullet(it) } }
            else Column(Modifier.padding(start = 8.s, top = 20.s).heightIn(min = 110.s), verticalArrangement = Arrangement.spacedBy(4.s)) { s.bullets.forEach { bullet(it) } }
        }
        // ภาพประกอบตามภาพอ้างอิง (สูตรเดียวกับ services.html): ขนาดอิงความสูงแผง P — ภาพสูง 1.517P
        // ขอบซ้ายภาพห่างขอบขวาแผง 1.373P · บนภาพเหนือแผง 0.397P · ตัดที่ขอบในขวา/ล่างของแผง · ด้านบนไม่ตัด (หัวทับขอบบนแผง)
        s.art?.let { art ->
            val img = th.go.banlat.kiosk.ui.common.img(art, 2)
            Canvas(Modifier.padding(start = 26.5.s, end = 26.5.s).fillMaxWidth().height((44 + panelH - 2.5f).s)
                .clip(RoundedCornerShape(bottomStart = 25.5.s, bottomEnd = 25.5.s))) {
                val p = panelH * u
                val f = s.fit
                val ih = p * f.h; val iw = ih * img.width / img.height
                val right = size.width + 2.5f * u   // ขอบนอกขวาของแผง
                clipRect(left = (right + p * f.cl).coerceAtLeast(0f)) {
                    drawImage(img, dstOffset = androidx.compose.ui.unit.IntOffset((right + p * f.x).toInt(), (44f * u + p * f.y).toInt()),
                        dstSize = androidx.compose.ui.unit.IntSize(iw.toInt(), ih.toInt()))
                }
            }
        }
        // ชื่อบริการ (วางทับภาพได้ ตำแหน่งเดียวกับในแผง)
        KText(s.title, if (main) 52 else 32, Modifier.offset(48.s, (if (main) 108 else 84).s),
            weight = FontWeight.Bold, lineHeight = if (main) 64f else 42f, softWrap = false)
        // ป้ายหมึกเข้ม คร่อมขอบบนแผง
        Box(Modifier.offset(48.s, 24.s).height(40.s)
            .softShadow(22f, Shade(Color(0x2E14265A), 4f, 10f))
            .clip(RoundedCornerShape(99.s)).background(K.Ink).padding(horizontal = 20.s), contentAlignment = Alignment.Center) {
            KText(s.tag, 20, weight = FontWeight.SemiBold, color = Color.White, softWrap = false)
        }
    }
}

/**
 * ผลตรวจสอบสิทธิ (ก่อนเลือกบริการ) — เลย์เอาต์เดียวกับหน้าสรุปผลประกัน · ตรงกับ services.html rightsScreen
 * มีสิทธิ: ✓ เขียว + Authen Code → "ถัดไป, เลือกบริการ" · ไม่มีสิทธิ: ⚠ ส้ม → "ยืนยันใช้สิทธิชำระเงินเอง"
 */
@Composable
private fun RightsScreen(onCancel: () -> Unit, onNext: () -> Unit) {
    val ok = DemoSession.rightsOk; val r = DemoSession.rights
    PageShell(fullVeil = true) {
        Column(Modifier.fillMaxSize().padding(start = 80.s, end = 80.s, top = 558.s, bottom = 328.s),
            verticalArrangement = Arrangement.spacedBy(48.s, Alignment.CenterVertically)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.padding(bottom = 32.s).size(160.s)
                    .then(if (ok) Modifier.creamDisc(ring = 2f, halo = 14f)
                          else Modifier.drawBehind { drawCircle(Color(0x0FB45309), radius = size.minDimension * .59f) }
                            .clip(CircleShape).background(Brush.radialGradient(listOf(Color.White, Color(0xFFFFF8EE), Color(0xFFFCEBD3))))
                            .border(2.s, Color(0x59B45309), CircleShape)),
                    contentAlignment = Alignment.Center) {
                    if (ok) LineIcon(KIcon.BigCheck, K.Green, Modifier.size(80.s), 2.4f)
                    else LineIcon(KIcon.Warn, K.Amber, Modifier.size(80.s), 2f)
                }
                KText(if (ok) "ตรวจสอบสิทธิเรียบร้อย" else "สิทธิประจำตัวไม่สามารถใช้ได้", 48, weight = FontWeight.Bold, lineHeight = 68f, align = TextAlign.Center)
                KText(buildAnnotatedString {
                    if (ok) append("สิทธิของท่านใช้รับบริการที่โรงพยาบาลนี้ได้")
                    else { append("ไม่สามารถใช้ได้ที่สถานพยาบาลนี้ · ท่านยังรับบริการได้โดย"); withStyle(SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold)) { append("ชำระเงินเอง") } }
                }, 32, weight = FontWeight.Medium, color = K.InkSub, lineHeight = 48f, align = TextAlign.Center)
            }
            Column(verticalArrangement = Arrangement.spacedBy(24.s)) {
                Column(Modifier.fillMaxWidth().pearl(24f, gradientRing = false).padding(horizontal = 40.s, vertical = 8.s)) {
                    val rows = listOf("สิทธิหลัก" to r.main, "สิทธิรอง" to r.sub, "สถานพยาบาลหลัก" to r.hosp, "สถานพยาบาลรอง" to r.hosp2) +
                        (r.authen?.let { listOf("Authen Code" to it) } ?: emptyList())
                    rows.forEachIndexed { i, (k, v) ->
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.s).background(Color(0x1414265A)))
                        Row(Modifier.fillMaxWidth().padding(vertical = 24.s), horizontalArrangement = Arrangement.spacedBy(32.s),
                            verticalAlignment = Alignment.CenterVertically) {
                            KText(k, 26, color = K.InkSoft, lineHeight = 36f, softWrap = false)
                            Spacer(Modifier.weight(1f))
                            val auth = k == "Authen Code"
                            KText(v, if (auth) 34 else 30, weight = if (v == "—") FontWeight.Medium else FontWeight.Bold,
                                color = when { auth -> K.BlueDeep; v == "—" -> K.InkSoft; else -> K.Ink },
                                lineHeight = 40f, letterSpacing = if (auth) 1f else 0f, tabular = auth, align = TextAlign.End)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.s)).background(if (ok) Color(0x0F1E7BE0) else Color(0xFFFEF5E7))
                    .padding(horizontal = 32.s, vertical = 24.s), horizontalArrangement = Arrangement.spacedBy(16.s)) {
                    LineIcon(KIcon.Info, if (ok) K.Blue else K.Amber, Modifier.padding(top = 2.s).size(32.s), 2f)
                    KText(buildAnnotatedString {
                        if (ok) append("Authen Code จะพิมพ์บนบัตรคิวให้อัตโนมัติ")
                        else { append("หากคิดว่าสิทธิไม่ถูกต้อง กรุณา"); withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("ติดต่อเจ้าหน้าที่ห้องบัตร") }; append("ก่อนยืนยัน") }
                    }, 24, color = if (ok) K.InkMuted else Color(0xFF7A4A0A), lineHeight = 36f)
                }
            }
        }
        BottomBar {
            Row(Modifier.fillMaxWidth().padding(horizontal = 80.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                SecondaryPill(if (ok) "ยกเลิก" else "ยกเลิกการทำรายการ", onCancel, Modifier.widthIn(min = 280.s))
                PrimaryPill(if (ok) "ถัดไป, เลือกบริการ" else "ยืนยันใช้สิทธิชำระเงินเอง", onNext, Modifier.weight(1f))
            }
        }
    }
}
