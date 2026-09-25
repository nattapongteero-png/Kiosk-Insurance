package th.go.banlat.kiosk.ui.welcome

import th.go.banlat.kiosk.ui.common.creamDisc
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import th.go.banlat.kiosk.ui.theme.NotoSansThai
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import th.go.banlat.kiosk.ui.theme.st
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.Placeholder
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.style.TextAlign
import th.go.banlat.kiosk.ui.common.KIcon
import th.go.banlat.kiosk.ui.common.LineIcon
import th.go.banlat.kiosk.ui.common.Shade
import th.go.banlat.kiosk.ui.common.press
import th.go.banlat.kiosk.ui.common.rich
import th.go.banlat.kiosk.ui.common.softShadow
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.unitPx

// ================= ชั้นอ่านบัตร / ตรวจสอบ / สแกนหน้า =================
@Composable
fun ReadingOverlay(title: String, sub: String) {
    val rot = rememberInfiniteTransition(label = "ring").animateFloat(0f, 360f,
        infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart), label = "rot")
    val u = unitPx()
    Column(Modifier.fillMaxSize().background(Color(0xD1FFFFFF)).swallowTaps(),
        verticalArrangement = Arrangement.spacedBy(30.s, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(170.s)) {
            val w = 10f * u
            drawCircle(K.Blue050, radius = size.minDimension / 2 - w / 2, style = Stroke(w))
            drawArc(K.Blue, rot.value - 45f, 90f, false, topLeft = Offset(w / 2, w / 2),
                size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w), style = Stroke(w, cap = StrokeCap.Butt))
        }
        KText(title, 46, weight = FontWeight.Bold)
        KText(sub, 28, color = K.InkMuted)
    }
}

// ================= แจ้งเตือนอ่านบัตรไม่ได้ (ตรงกับ welcome_v2.html #rdErr) =================
// กล่องไข่มุกขอบทองแบบ modal ความยินยอม · ไอคอนเตือนในวงอำพัน · ปุ่มรอง + "อ่านบัตรอีกครั้ง"
@Composable
fun ReadErrorModal(fail: Boolean, onAlt: () -> Unit, onRetry: () -> Unit) {
    val strong = SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold)
    Box(Modifier.fillMaxSize().background(K.ScrimLight).swallowTaps().padding(horizontal = 100.s), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth()
            .softShadow(40f, Shade(Color(0x59061432), 40f, 100f))
            .clip(RoundedCornerShape(40.s))
            .background(Brush.linearGradient(0f to K.PearlTop, .55f to K.PearlMid, 1f to K.PearlBottom))
            .border(1.s, Color(0x52BF913A), RoundedCornerShape(40.s))
            .padding(start = 56.s, end = 56.s, top = 64.s, bottom = 48.s),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.padding(bottom = 32.s).size(136.s)
                .drawBehind { drawCircle(Color(0x0FB45309), radius = size.minDimension * .6f) }
                .clip(CircleShape).background(Brush.radialGradient(listOf(Color.White, Color(0xFFFFF8EE), Color(0xFFFCEBD3))))
                .border(2.s, Color(0x59B45309), CircleShape), contentAlignment = Alignment.Center) {
                LineIcon(KIcon.Warn, K.Amber, Modifier.size(68.s), 2f)
            }
            KText(if (fail) "อ่านบัตรไม่สำเร็จ" else "ไม่พบบัตรประชาชน", 44, weight = FontWeight.Bold, lineHeight = 60f, align = TextAlign.Center)
            KText(buildAnnotatedString {
                if (fail) {
                    append("กรุณาดึงบัตรออก เช็ดชิปให้สะอาด\nแล้วเสียบใหม่อีกครั้ง\nหากยังอ่านไม่ได้ ใช้"); withStyle(strong) { append("กรอกเลขบัตรแทน") }; append("\nหรือติดต่อเจ้าหน้าที่")
                } else {
                    // "ด้านที่มี" + รูปชิป (แบบเดียวกับการ์ดเสียบบัตรหน้าแรก)
                    append("กรุณาเสียบบัตรให้สุดช่อง\nโดยหันด้านที่มี "); appendInlineContent("chip", "ชิป"); append(" เข้าช่องอ่านบัตร\nแล้วกด "); withStyle(strong) { append("อ่านบัตรอีกครั้ง") }
                }
            }, 38, Modifier.padding(top = 16.s), color = K.InkMuted, lineHeight = 62f, align = TextAlign.Center,
                inline = mapOf("chip" to InlineTextContent(Placeholder(80.st, 55.st, PlaceholderVerticalAlign.TextCenter)) {
                    ChipIcon(Modifier.padding(horizontal = 2.s).fillMaxSize())
                }))
            Row(Modifier.fillMaxWidth().padding(top = 48.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                Box(Modifier.widthIn(min = 280.s).height(96.s).clip(RoundedCornerShape(99.s)).background(Color.White)
                    .border(2.s, Color(0xFFDCE4EE), RoundedCornerShape(99.s)).press(onClick = onAlt).padding(horizontal = 32.s),
                    contentAlignment = Alignment.Center) {
                    KText(if (fail) "กรอกเลขบัตรแทน" else "ปิด", 30, weight = FontWeight.Bold, color = K.InkMuted, softWrap = false)
                }
                Box(Modifier.weight(1f).height(96.s).softShadow(99f, Shade(Color(0x4714265A), 10f, 24f)).clip(RoundedCornerShape(99.s))
                    .background(Brush.linearGradient(listOf(Color(0xFF223A7A), K.Ink))).press(onClick = onRetry),
                    contentAlignment = Alignment.Center) {
                    KText("อ่านบัตรอีกครั้ง", 30, weight = FontWeight.Bold, color = Color.White, softWrap = false)
                }
            }
        }
    }
}

/** ภาพบาร์โค้ดบนบัตรโรงพยาบาล + เส้นแสงสแกนสีแดงวิ่งขึ้นลง (ตรงกับ .kpScan .bc) */
@Composable
private fun BarcodeArt(modifier: Modifier) {
    val beam = rememberInfiniteTransition(label = "beam").animateFloat(.1f, .78f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "y")
    val tm = rememberTextMeasurer()
    val u = unitPx()
    Canvas(modifier) {
        val k = size.width / 120f
        drawRoundRect(Color.White, size = size, cornerRadius = CornerRadius(10f * k))
        drawRoundRect(Color(0xFFDCE7F4), size = size, cornerRadius = CornerRadius(10f * k), style = Stroke(2f * k))
        listOf(18, 24, 28, 35, 40, 44, 52, 56, 63, 67, 74, 80, 84, 91, 96, 102).forEach { x ->
            drawLine(K.Ink, Offset(x * k, 16f * k), Offset(x * k, 56f * k), 3f * k)
        }
        val t = tm.measure("HN 000890314", TextStyle(fontFamily = NotoSansThai, fontSize = (10f * k / density).sp, fontWeight = FontWeight.Bold, color = K.Ink))
        drawText(t, topLeft = Offset((size.width - t.size.width) / 2, 71f * k - t.size.height * .8f))
        val y = size.height * beam.value
        drawRect(Color(0x55E5484D), Offset(6f * k, y - 3f * u), Size(size.width - 12f * k, 9f * u))
        drawRect(Color(0xFFE5484D), Offset(6f * k, y), Size(size.width - 12f * k, 3f * u))
    }
}

/** กันการแตะทะลุลงไปชั้นล่าง */
@Composable
fun Modifier.swallowTaps(): Modifier = this.clickable(remember { MutableInteractionSource() }, indication = null) {}

// ================= ปุ่มแบบเดียวกันทั้งแอป =================
@Composable
fun RowScope.GhostButton(text: String, height: Int, size: Int = 26, onClick: () -> Unit) {
    Box(Modifier.weight(1f).height(height.s)
        .softShadow(99f, Shade(Color(0x1414265A), 6f, 16f))
        .clip(RoundedCornerShape(99.s))
        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF4F8FD))))
        .border(2.s, Color(0xFFDCE7F4), RoundedCornerShape(99.s))
        .press(onClick = onClick), contentAlignment = Alignment.Center) {
        KText(text, size, weight = FontWeight.Bold, color = K.InkMuted, softWrap = false)
    }
}

@Composable
fun RowScope.PrimaryButton(text: String, height: Int, enabled: Boolean, size: Int = 32, onClick: () -> Unit) {
    Box(Modifier.weight(1f).height(height.s)
        .then(if (enabled) Modifier.softShadow(99f, Shade(Color(0x6B1E7BE0), 14f, 32f)) else Modifier)
        .clip(RoundedCornerShape(99.s))
        .background(if (enabled) Brush.linearGradient(0f to K.BlueLight, .45f to K.Blue, 1f to K.BlueDeep)
                    else Brush.verticalGradient(listOf(Color(0xFFDCE4EE), Color(0xFFCBD6E4))))
        .press(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        KText(text, size, weight = FontWeight.Bold, color = if (enabled) Color.White else Color(0xFFF2F6FA), softWrap = false)
    }
}

/** ม่านหลัง modal + กล่องกลางจอ */
@Composable
private fun ModalScrim(padV: Int, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(K.Scrim).swallowTaps().padding(vertical = padV.s, horizontal = 60.s),
        contentAlignment = Alignment.Center, content = content)
}

@Composable
private fun SheetHeader(title: String, hint: String, icon: Int = 64) {
    Row(Modifier.fillMaxWidth().drawBehind {
        drawLine(Color(0x1214265A), Offset(0f, size.height), Offset(size.width, size.height), 1f)
    }.padding(start = 44.s, end = 44.s, top = 40.s, bottom = 22.s), horizontalArrangement = Arrangement.spacedBy(20.s)) {
        Box(Modifier.size(icon.s).softShadow(20f, Shade(Color(0x261E7BE0), 8f, 18f)).clip(RoundedCornerShape(20.s))
            .background(Brush.radialGradient(0f to Color.White, .6f to Color(0xFFE9F3FD), 1f to Color(0xFFDCEBFA)))
            .border(1.s, Color(0xF2FFFFFF), RoundedCornerShape(20.s)), contentAlignment = Alignment.Center) {
            LineIcon(KIcon.Doc, K.BlueDeep, Modifier.size((icon * .53f).s), 1.9f)
        }
        Column {
            KText(title, 38, weight = FontWeight.Bold, lineHeight = 46f)
            Spacer(Modifier.height(6.s))
            KText(hint, 23, color = K.InkMuted)
        }
    }
}

private val SheetBg = Brush.linearGradient(0f to Color.White, .42f to Color(0xFFFAFCFF), 1f to Color(0xFFF3F8FE))

// ================= แป้นตัวเลข: เลขบัตรประชาชน 13 หลัก / HN 9 หลัก =================
@Composable
fun KeypadModal(hn: Boolean, onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    var digits by remember { mutableStateOf("") }
    val len = if (hn) 9 else 13
    val groups = if (hn) listOf(3, 3, 3) else listOf(1, 4, 5, 2, 1)
    var scanned by remember { mutableStateOf(false) }
    LaunchedEffect(scanned) { if (scanned) { delay(500); onConfirm(digits) } }

    ModalScrim(padV = 130) {
        Column(Modifier.width(880.s)
            .softShadow(40f, Shade(Color(0x80061432), 60f, 130f))
            .clip(RoundedCornerShape(40.s)).background(SheetBg).border(1.s, Color(0x0F14265A), RoundedCornerShape(40.s))) {
            SheetHeader(if (hn) "สแกน หรือกรอกเลข HN" else "กรอกเลขบัตรประชาชน",
                if (hn) "เลขประจำตัวผู้ป่วย 9 หลักจากบัตรโรงพยาบาล" else "เลข 13 หลักหน้าบัตรประชาชน", icon = 70)
            Column(Modifier.padding(start = 46.s, end = 46.s, top = 26.s, bottom = 32.s), verticalArrangement = Arrangement.spacedBy(22.s)) {
                // โหมด HN: สแกนบาร์โค้ดบนบัตรโรงพยาบาล หรือกรอกเลขเอง (ต้นแบบ: แตะช่องสแกน = จำลองสแกนสำเร็จ)
                // TODO(integration): รับค่าจากเครื่องสแกนบาร์โค้ด (แปลงสัญลักษณ์ตามตั้งค่า "การแปลงสัญลักษณ์จากการ Scan HN")
                if (hn) {
                    Row(Modifier.fillMaxWidth().softShadow(26f, Shade(Color(0x1414265A), 6f, 16f)).clip(RoundedCornerShape(26.s))
                        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF4F8FD)))).border(1.s, Color(0xFFDCE7F4), RoundedCornerShape(26.s))
                        .press { if (!scanned) { digits = "000890314"; scanned = true } }.padding(horizontal = 30.s, vertical = 24.s),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.s)) {
                        BarcodeArt(Modifier.size(150.s, 100.s))
                        Column {
                            KText("สแกนบาร์โค้ด HN", 32, weight = FontWeight.Bold, lineHeight = 44f)
                            KText("ยื่นบาร์โค้ดบนบัตรโรงพยาบาลที่ช่องสแกนใต้จอ", 24, Modifier.padding(top = 4.s), color = K.InkMuted, lineHeight = 36f, softWrap = false, maxLines = 1, minSize = 20)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.s)) {
                        Box(Modifier.weight(1f).height(2.s).background(Color(0x1F14265A)))
                        KText("หรือกรอกเลข HN", 24, weight = FontWeight.Medium, color = K.InkMuted)
                        Box(Modifier.weight(1f).height(2.s).background(Color(0x1F14265A)))
                    }
                }
                // ช่องแสดงตัวเลข
                Row(Modifier.fillMaxWidth().heightIn(min = 130.s).clip(RoundedCornerShape(26.s))
                    .background(Brush.verticalGradient(listOf(Color(0xFFEDF3FB), Color(0xFFFAFCFF))))
                    .border(1.s, Color(0xFFDCE7F4), RoundedCornerShape(26.s)).padding(horizontal = 30.s, vertical = 26.s),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.s)) {
                    KText(masked(digits, groups), 58, Modifier.weight(1f), weight = FontWeight.Bold, lineHeight = 64f,
                        letterSpacing = 5f, tabular = true, softWrap = false)
                    KText("${digits.length} / $len", 24, weight = FontWeight.SemiBold, color = K.InkSoft, tabular = true)
                }
                // แป้น
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "ล้าง", "0", "⌫")
                keys.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.s)) {
                        row.forEach { k -> PadKey(k) {
                            digits = when (k) { "ล้าง" -> ""; "⌫" -> digits.dropLast(1); else -> if (digits.length < len) digits + k else digits }
                        } }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 4.s), horizontalArrangement = Arrangement.spacedBy(16.s)) {
                    GhostButton("ยกเลิก", 96, 28, onCancel)
                    PrimaryButton("ยืนยัน", 96, digits.length == len) { onConfirm(digits) }
                }
            }
        }
    }
}

private fun masked(d: String, groups: List<Int>): AnnotatedString = buildAnnotatedString {
    var i = 0
    groups.forEachIndexed { gi, g ->
        repeat(g) {
            if (i < d.length) append(d[i]) else withStyle(SpanStyle(color = Color(0xFFBCCBDE), fontWeight = FontWeight.SemiBold)) { append("–") }
            i++
        }
        if (gi < groups.lastIndex) append(" ")
    }
}

@Composable
private fun RowScope.PadKey(k: String, onTap: () -> Unit) {
    val act = k == "ล้าง" || k == "⌫"
    Box(Modifier.weight(1f).height(114.s)
        .softShadow(22f, Shade(Color(0x1A14265A), 8f, 18f))
        .clip(RoundedCornerShape(22.s))
        .background(if (act) Brush.verticalGradient(listOf(Color(0xFFF5F8FC), Color(0xFFE9EFF7)))
                    else Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F6FD))))
        .border(1.s, Color(0xE6FFFFFF), RoundedCornerShape(22.s))
        .press(scaleTo = .97f, onClick = onTap), contentAlignment = Alignment.Center) {
        when {
            k == "⌫" -> LineIcon(KIcon.Backspace, K.InkMuted, Modifier.size(34.s), 1.9f)
            act -> KText(k, 27, weight = FontWeight.SemiBold, color = K.InkMuted)
            else -> KText(k, 48, weight = FontWeight.Bold)
        }
    }
}

// ================= ความยินยอมให้ส่งข้อมูลสุขภาพ (เด้งหลังยืนยันตัวตนสำเร็จ) =================
private data class ScopeItem(val t: String, val s: String, val sent: Boolean)

private val Scope = listOf(
    ScopeItem("ข้อมูลพื้นฐาน", "เพศ อายุ รหัสอ้างอิงผู้ป่วย", true),
    ScopeItem("ประวัติการรับบริการ", "วันที่ แผนก ย้อนหลัง 3 ปี", true),
    ScopeItem("การวินิจฉัยโรค", "รหัส ICD ตามขอบเขต", true),
    ScopeItem("ประวัติการใช้ยา", "รายการยาที่เกี่ยวข้อง", true),
    ScopeItem("ผลตรวจห้องปฏิบัติการ", "เฉพาะชุดที่บริษัทร้องขอ", true),
    ScopeItem("สัญญาณชีพ", "ความดันโลหิต ดัชนีมวลกาย", true),
    ScopeItem("รายงานแพทย์ (FMR)", "สรุปโดยแพทย์ผู้รักษา", true),
    ScopeItem("เลขบัตรประชาชน", "เลข 13 หลักไม่ถูกส่งออก", false),
    ScopeItem("ที่อยู่", "ตามทะเบียนบ้านและที่ติดต่อ", false),
    ScopeItem("เบอร์โทรศัพท์", "ช่องทางติดต่อส่วนตัว", false),
    ScopeItem("เวชระเบียนส่วนที่เหลือ", "ส่วนที่ไม่เกี่ยวกับการพิจารณา", false),
)

@Composable
fun ConsentModal(onDecline: () -> Unit, onAccept: () -> Unit, onClose: () -> Unit) {
    val scroll = rememberScrollState()
    val u = unitPx()
    // ต้องเลื่อนอ่านจนสุดก่อนจึงติ๊กยินยอมได้
    val readAll by remember { derivedStateOf { scroll.maxValue > 0 && scroll.value >= scroll.maxValue - 24 * u } }
    var agreed by remember { mutableStateOf(false) }
    val goldLine = Color(0x33BF913A)

    // ปรับให้เข้ากับ UI ปัจจุบัน: ผิวไข่มุก ขอบทองบาง หัวข้อสีหมึก ปุ่มกรมท่า (ตรงกับ welcome_v2.html #consentOv)
    Box(Modifier.fillMaxSize().background(K.ScrimLight).swallowTaps().padding(vertical = 120.s, horizontal = 64.s),
        contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth()
            .softShadow(40f, Shade(Color(0x59061432), 40f, 100f))
            .clip(RoundedCornerShape(40.s))
            .background(Brush.linearGradient(0f to K.PearlTop, .55f to K.PearlMid, 1f to K.PearlBottom))
            .border(1.s, Color(0x52BF913A), RoundedCornerShape(40.s))) {
            Column {
                // หัว: วงครีมขอบทอง + ไอคอนเอกสารสีทอง
                Row(Modifier.fillMaxWidth().drawBehind { drawLine(goldLine, Offset(0f, size.height), Offset(size.width, size.height), 1f) }
                    .padding(start = 48.s, end = 48.s, top = 48.s, bottom = 28.s),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.s)) {
                    Box(Modifier.size(88.s).creamDisc(halo = 8f), contentAlignment = Alignment.Center) {
                        LineIcon(KIcon.Doc, K.GoldIcon, Modifier.size(44.s), 1.9f)
                    }
                    Column(Modifier.weight(1f)) {
                        KText("ความยินยอมให้ส่งข้อมูลสุขภาพ", 40, weight = FontWeight.Bold, lineHeight = 52f, softWrap = false)
                        KText("กรุณาเลื่อนอ่านให้ครบก่อนตัดสินใจ · ฉบับที่ 1.0 (1 ก.ย. 2569)", 24, color = K.InkMuted, lineHeight = 36f)
                    }
                    // ปิด = ยกเลิกทั้งรายการ กลับหน้าแรก (ต่างจาก "ไม่ยินยอม" ที่ยังลงทะเบียนต่อ)
                    Box(Modifier.size(88.s).clip(CircleShape).background(Color(0x0F14265A)).press(scaleTo = .94f, onClick = onClose),
                        contentAlignment = Alignment.Center) {
                        LineIcon(KIcon.Close, K.InkMuted, Modifier.size(40.s), 2.2f)
                    }
                }
                Column(Modifier.weight(1f, fill = false).verticalScroll(scroll).padding(horizontal = 48.s, vertical = 32.s),
                    verticalArrangement = Arrangement.spacedBy(24.s)) {
                    CsCard(1, "วัตถุประสงค์") {
                        CsText("เพื่อให้บริษัทประกันใช้**รายงานแพทย์ (FMR)** และ**ประวัติสุขภาพส่วนบุคคล (PHR)** ของท่าน ประกอบการแนะนำแบบประกันชีวิต/สุขภาพที่เหมาะกับสุขภาพของท่าน และจัดทำข้อเสนอเบื้องต้น **เฉพาะการตรวจสอบครั้งนี้เท่านั้น**")
                    }
                    CsCard(2, "ผู้รับข้อมูล") {
                        CsText("**บริษัทประกันที่ร่วมโครงการ** ส่งผ่านระบบกลางของ BMS แบบเข้ารหัส ระบบจะแสดงรายชื่อบริษัทที่ได้รับข้อมูลให้ท่านทราบก่อนส่งจริง ท่านจะเป็นผู้เลือก**แบบประกัน**ที่สนใจเองหลังทราบผล")
                    }
                    // ข้อ 3 คงแนวเดิม: ✓ ส่ง / ✗ ไม่ส่ง 2 คอลัมน์
                    CsCard(3, "ข้อมูลที่จะถูกส่ง (FMR / PHR)") {
                        Scope.chunked(2).forEach { pair ->
                            Row(Modifier.fillMaxWidth().padding(bottom = 16.s), horizontalArrangement = Arrangement.spacedBy(32.s)) {
                                pair.forEach { ScopeRow(it) }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    CsCard(4, "การรับผล") {
                        CsText("· ผลอาจกลับมา**ทันทีที่ตู้** หรือบริษัทใช้เวลาพิจารณาแล้วแจ้งภายหลัง\n· กรณีใช้เวลานาน ท่าน**ไม่ต้องรอที่ตู้** ระบบจะแจ้งผลผ่านแอป **MyAtlas** หรือ **หมอพร้อม**\n· สิ่งที่ได้คือ**คำแนะนำแบบประกันที่เหมาะกับข้อมูลสุขภาพของท่าน** พร้อมข้อเสนอเบื้องต้น")
                    }
                    CsCard(5, "สิทธิของท่าน") {
                        CsText("· ท่าน**ถอนความยินยอม**ได้ทุกเมื่อ ผ่านแอป MyAtlas หรือที่เคาน์เตอร์เวชระเบียน\n· ตู้บริการ**ไม่เก็บ**ข้อมูลสุขภาพของท่านไว้หลังจบรายการ\n· ทุกครั้งที่มีการส่งหรือเปิดดูข้อมูล ระบบบันทึกหลักฐานไว้ให้ตรวจสอบย้อนหลังได้")
                    }
                    CsNote(KIcon.Info, Color(0x0F1E7BE0), K.InkMuted,
                        "การยินยอมนี้ **ไม่ใช่** การยินยอมให้ตัวแทนติดต่อขายประกัน หากท่านสนใจข้อเสนอ ระบบจะถามยืนยันแยกอีกครั้งหลังทราบผล", K.Blue)
                    CsNote(KIcon.Warn, Color(0xFFFEF5E7), Color(0xFF7A4A0A),
                        "ผลการตรวจสอบเป็น**ข้อเสนอเบื้องต้น ไม่ใช่การอนุมัติกรมธรรม์** การรับประกันขึ้นอยู่กับการพิจารณาของบริษัทประกัน", K.Amber)
                }
                // ท้าย: ติ๊กยินยอม + ปุ่ม (ปุ่มรองขาวขอบบาง · ปุ่มหลักกรมท่า สูง 88 เหมือนทั้งแอป)
                Column(Modifier.fillMaxWidth().drawBehind { drawLine(goldLine, Offset.Zero, Offset(size.width, 0f), 1f) }
                    .padding(start = 48.s, end = 48.s, top = 28.s, bottom = 40.s), verticalArrangement = Arrangement.spacedBy(24.s)) {
                    AgreeBox(agreed, enabled = readAll) { agreed = !agreed }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                        th.go.banlat.kiosk.ui.insurance.SecondaryPill("ไม่ยินยอม, ไปที่ระบบลงทะเบียน", onDecline, Modifier.widthIn(min = 360.s))
                        Box(Modifier.weight(1f).height(88.s).then(if (agreed) Modifier.softShadow(44f, Shade(Color(0x3814265A), 10f, 24f)) else Modifier)
                            .clip(RoundedCornerShape(99.s))
                            .background(if (agreed) Brush.verticalGradient(listOf(Color(0xFF223A7A), K.Ink)) else Brush.verticalGradient(listOf(Color(0xFFD5DCE6), Color(0xFFD5DCE6))))
                            .press(enabled = agreed, scaleTo = .97f, onClick = onAccept), contentAlignment = Alignment.Center) {
                            KText("ยินยอม", 30, weight = FontWeight.Bold, color = Color.White, softWrap = false)
                        }
                    }
                }
            }
            if (!readAll) ScrollCue(Modifier.align(Alignment.BottomCenter).padding(bottom = 300.s))
        }
    }
}

@Composable
private fun CsCard(n: Int, title: String, body: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()
        .softShadow(24f, Shade(Color(0x0D14265A), 6f, 14f))
        .clip(RoundedCornerShape(24.s)).background(Color.White)
        .border(1.s, Color(0x47BF913A), RoundedCornerShape(24.s)).padding(horizontal = 32.s, vertical = 28.s)) {
        Row(Modifier.padding(bottom = 16.s), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.s)) {
            Box(Modifier.size(40.s).clip(CircleShape).background(K.GoldTint), contentAlignment = Alignment.Center) {
                KText("$n", 22, weight = FontWeight.Bold, color = K.GoldText)
            }
            KText(title, 30, weight = FontWeight.Bold, lineHeight = 40f)
        }
        body()
    }
}

@Composable
private fun CsText(s: String) = KText(rich(s), 26, lineHeight = 42f)

@Composable
private fun RowScope.ScopeRow(it: ScopeItem) {
    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.s)) {
        Box(Modifier.padding(top = 2.s).size(32.s).clip(CircleShape).background(if (it.sent) K.Green050 else Color(0xFFFBDEDB)),
            contentAlignment = Alignment.Center) {
            LineIcon(if (it.sent) KIcon.Check else KIcon.Cross, if (it.sent) K.Green else K.Red, Modifier.size(18.s), 3f)
        }
        Column {
            KText(it.t, 25, weight = FontWeight.SemiBold, color = if (it.sent) K.Ink else K.RedText, lineHeight = 36f)
            KText(it.s, 21, color = if (it.sent) K.InkSoft else Color(0xFFB07871), lineHeight = 30f)
        }
    }
}

@Composable
private fun CsNote(icon: KIcon, bg: Color, fg: Color, text: String, iconColor: Color) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.s)).background(bg)
        .padding(horizontal = 32.s, vertical = 24.s), horizontalArrangement = Arrangement.spacedBy(16.s)) {
        LineIcon(icon, iconColor, Modifier.padding(top = 3.s).size(32.s), 2f)
        KText(rich(text, K.Ink), 25, color = fg, lineHeight = 38f)
    }
}

@Composable
private fun AgreeBox(checked: Boolean, enabled: Boolean, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(24.s)
    Row(Modifier.fillMaxWidth().alpha(if (enabled) 1f else .45f).clip(shape)
        .background(if (checked) K.Green050 else Color.White).border(if (checked) 2.s else 1.5.s, if (checked) K.Green else K.Line, shape)
        .press(enabled = enabled, scaleTo = .99f, onClick = onToggle).padding(horizontal = 28.s, vertical = 24.s),
        horizontalArrangement = Arrangement.spacedBy(20.s)) {
        Box(Modifier.size(44.s).clip(RoundedCornerShape(12.s)).background(if (checked) K.Green else Color.White)
            .border(2.5.s, if (checked) K.Green else K.Line, RoundedCornerShape(12.s)), contentAlignment = Alignment.Center) {
            if (checked) LineIcon(KIcon.Check, Color.White, Modifier.size(25.s), 3.2f)
        }
        KText(rich("ข้าพเจ้าอ่านและเข้าใจข้อความข้างต้น และ**ยินยอม**ให้ส่งข้อมูลสุขภาพ ตามรายการที่ระบุไปยังบริษัทประกันที่ร่วมโครงการ"),
            26, lineHeight = 40f)
    }
}

@Composable
private fun ScrollCue(modifier: Modifier) {
    Row(modifier.softShadow(99f, Shade(Color(0x5514265A), 10f, 26f)).clip(RoundedCornerShape(99.s)).background(K.Ink)
        .padding(horizontal = 24.s, vertical = 12.s), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.s)) {
        LineIcon(KIcon.Down, Color.White, Modifier.size(19.s), 2.6f)
        KText("เลื่อนลงเพื่ออ่านให้ครบ", 21, weight = FontWeight.SemiBold, color = Color.White)
    }
}
