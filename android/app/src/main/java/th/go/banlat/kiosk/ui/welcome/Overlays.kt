package th.go.banlat.kiosk.ui.welcome

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
fun KeypadModal(onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    var cid by remember { mutableStateOf(true) }
    var digits by remember { mutableStateOf("") }
    val len = if (cid) 13 else 9
    val groups = if (cid) listOf(1, 4, 5, 2, 1) else listOf(3, 3, 3)

    ModalScrim(padV = 130) {
        Column(Modifier.width(880.s)
            .softShadow(40f, Shade(Color(0x80061432), 60f, 130f))
            .clip(RoundedCornerShape(40.s)).background(SheetBg).border(1.s, Color(0x0F14265A), RoundedCornerShape(40.s))) {
            SheetHeader(if (cid) "กรอกเลขบัตรประชาชน" else "กรอกเลข HN",
                if (cid) "เลข 13 หลักหน้าบัตรประชาชน" else "เลขประจำตัวผู้ป่วย 9 หลักจากบัตรโรงพยาบาล", icon = 70)
            Column(Modifier.padding(start = 46.s, end = 46.s, top = 26.s, bottom = 32.s), verticalArrangement = Arrangement.spacedBy(22.s)) {
                // สลับประเภทเลข
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(99.s))
                    .background(Brush.verticalGradient(listOf(Color(0xFFE7EFF9), Color(0xFFF2F7FC)))).padding(9.s),
                    horizontalArrangement = Arrangement.spacedBy(10.s)) {
                    listOf(true to "เลขบัตรประชาชน", false to "เลข HN").forEach { (isCid, label) ->
                        val on = cid == isCid
                        Box(Modifier.weight(1f).height(76.s)
                            .then(if (on) Modifier.softShadow(99f, Shade(Color(0x571E7BE0), 8f, 20f)) else Modifier)
                            .clip(RoundedCornerShape(99.s))
                            .then(if (on) Modifier.background(Brush.linearGradient(0f to K.BlueLight, .48f to K.Blue, 1f to K.BlueDeep)) else Modifier)
                            .press { cid = isCid; digits = "" }, contentAlignment = Alignment.Center) {
                            KText(label, 27, weight = FontWeight.SemiBold, color = if (on) Color.White else K.InkMuted)
                        }
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
fun ConsentModal(onDecline: () -> Unit, onAccept: () -> Unit) {
    val scroll = rememberScrollState()
    val u = unitPx()
    // ต้องเลื่อนอ่านจนสุดก่อนจึงติ๊กยินยอมได้
    val readAll by remember { derivedStateOf { scroll.maxValue > 0 && scroll.value >= scroll.maxValue - 24 * u } }
    var agreed by remember { mutableStateOf(false) }

    ModalScrim(padV = 130) {
        Box(Modifier.width(952.s)
            .softShadow(40f, Shade(Color(0x80061432), 60f, 130f))
            .clip(RoundedCornerShape(40.s)).background(SheetBg).border(1.s, Color(0x0F14265A), RoundedCornerShape(40.s))) {
            Column {
                SheetHeader("ความยินยอมให้ส่งข้อมูลสุขภาพ", "กรุณาเลื่อนอ่านให้ครบก่อนตัดสินใจ · ฉบับที่ 1.0 (1 ก.ย. 2569)")
                Column(Modifier.weight(1f, fill = false).verticalScroll(scroll).padding(horizontal = 44.s, vertical = 26.s),
                    verticalArrangement = Arrangement.spacedBy(18.s)) {
                    CsCard(1, "วัตถุประสงค์") {
                        CsText("เพื่อให้บริษัทประกันใช้**รายงานแพทย์ (FMR)** และ**ประวัติสุขภาพส่วนบุคคล (PHR)** ของท่าน ประกอบการแนะนำแบบประกันชีวิต/สุขภาพที่เหมาะกับสุขภาพของท่าน และจัดทำข้อเสนอเบื้องต้น **เฉพาะการตรวจสอบครั้งนี้เท่านั้น**")
                    }
                    CsCard(2, "ผู้รับข้อมูล") {
                        CsText("**บริษัทประกันที่ร่วมโครงการ** ส่งผ่านระบบกลางของ BMS แบบเข้ารหัส ระบบจะแสดงรายชื่อบริษัทที่ได้รับข้อมูลให้ท่านทราบก่อนส่งจริง ท่านจะเป็นผู้เลือก**แบบประกัน**ที่สนใจเองหลังทราบผล")
                    }
                    CsCard(3, "ข้อมูลที่จะถูกส่ง (FMR / PHR)") {
                        Scope.chunked(2).forEach { pair ->
                            Row(Modifier.fillMaxWidth().padding(bottom = 12.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
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
                    CsNote(KIcon.Info, Color(0xFFEAF3FD), Color(0xFFC6DFF8), Color(0xFFD5E8FB), K.BlueDeep,
                        "การยินยอมนี้ **ไม่ใช่** การยินยอมให้ตัวแทนติดต่อขายประกัน หากท่านสนใจข้อเสนอ ระบบจะถามยืนยันแยกอีกครั้งหลังทราบผล")
                    CsNote(KIcon.Warn, Color(0xFFFEF5E7), Color(0xFFF6DCB4), Color(0xFFFBE3BE), Color(0xFF7C3B06),
                        "ผลการตรวจสอบเป็น**ข้อเสนอเบื้องต้น ไม่ใช่การอนุมัติกรมธรรม์** การรับประกันขึ้นอยู่กับการพิจารณาของบริษัทประกัน", K.Amber)
                }
                // ท้าย: ติ๊กยินยอม + ปุ่ม
                Column(Modifier.fillMaxWidth().drawBehind {
                    drawLine(Color(0x1214265A), Offset.Zero, Offset(size.width, 0f), 1f)
                }.padding(start = 44.s, end = 44.s, top = 24.s, bottom = 28.s), verticalArrangement = Arrangement.spacedBy(18.s)) {
                    AgreeBox(agreed, enabled = readAll) { agreed = !agreed }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.s)) {
                        GhostButton("ไม่ยินยอม ออกจากบริการ", 92, 26, onDecline)
                        PrimaryButton("ยินยอม", 92, agreed, onClick = onAccept)
                    }
                }
            }
            if (!readAll) ScrollCue(Modifier.align(Alignment.BottomCenter).padding(bottom = 272.s))
        }
    }
}

@Composable
private fun CsCard(n: Int, title: String, body: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.s)).background(Color(0xFFF8FBFF))
        .border(1.s, K.Line, RoundedCornerShape(24.s)).padding(horizontal = 28.s, vertical = 24.s)) {
        Row(Modifier.padding(bottom = 12.s), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.s)) {
            Box(Modifier.size(34.s).clip(RoundedCornerShape(9.s)).background(K.Blue050), contentAlignment = Alignment.Center) {
                KText("$n", 19, weight = FontWeight.Bold, color = K.BlueDeep)
            }
            KText(title, 26, weight = FontWeight.Bold, color = K.BlueDeep)
        }
        body()
    }
}

@Composable
private fun CsText(s: String) = KText(rich(s), 25, lineHeight = 37.5f)

@Composable
private fun RowScope.ScopeRow(it: ScopeItem) {
    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.s)) {
        Box(Modifier.padding(top = 3.s).size(25.s).clip(CircleShape).background(if (it.sent) K.Green050 else Color(0xFFFBDEDB)),
            contentAlignment = Alignment.Center) {
            LineIcon(if (it.sent) KIcon.Check else KIcon.Cross, if (it.sent) K.Green else K.Red, Modifier.size(14.s), 3f)
        }
        Column {
            KText(it.t, 23, color = if (it.sent) K.Ink else K.RedText, lineHeight = 32f)
            KText(it.s, 19, color = if (it.sent) K.InkSoft else Color(0xFFB07871), lineHeight = 26f)
        }
    }
}

@Composable
private fun CsNote(icon: KIcon, bg: Color, border: Color, iconBg: Color, fg: Color, text: String, iconColor: Color = fg) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.s)).background(bg).border(1.s, border, RoundedCornerShape(20.s))
        .padding(horizontal = 24.s, vertical = 20.s), horizontalArrangement = Arrangement.spacedBy(16.s)) {
        Box(Modifier.size(36.s).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            LineIcon(icon, iconColor, Modifier.size(20.s), 1.9f)
        }
        KText(rich(text), 24, color = fg, lineHeight = 36f)
    }
}

@Composable
private fun AgreeBox(checked: Boolean, enabled: Boolean, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(20.s)
    Row(Modifier.fillMaxWidth().alpha(if (enabled) 1f else .45f).clip(shape)
        .background(if (checked) K.Green050 else Color.White).border(2.s, if (checked) K.Green else K.Line, shape)
        .press(enabled = enabled, scaleTo = .99f, onClick = onToggle).padding(horizontal = 24.s, vertical = 20.s),
        horizontalArrangement = Arrangement.spacedBy(18.s)) {
        Box(Modifier.size(40.s).clip(RoundedCornerShape(11.s)).background(if (checked) K.Green else Color.White)
            .border(2.5.s, if (checked) K.Green else K.Line, RoundedCornerShape(11.s)), contentAlignment = Alignment.Center) {
            if (checked) LineIcon(KIcon.Check, Color.White, Modifier.size(23.s), 3.2f)
        }
        KText(rich("ข้าพเจ้าอ่านและเข้าใจข้อความข้างต้น และ**ยินยอม**ให้ส่งข้อมูลสุขภาพ ตามรายการที่ระบุไปยังบริษัทประกันที่ร่วมโครงการ"),
            25, lineHeight = 36f)
    }
}

@Composable
private fun ScrollCue(modifier: Modifier) {
    Row(modifier.softShadow(99f, Shade(Color(0x660D5BC6), 10f, 26f)).clip(RoundedCornerShape(99.s)).background(K.BlueDeep)
        .padding(horizontal = 24.s, vertical = 12.s), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.s)) {
        LineIcon(KIcon.Down, Color.White, Modifier.size(19.s), 2.6f)
        KText("เลื่อนลงเพื่ออ่านให้ครบ", 21, weight = FontWeight.SemiBold, color = Color.White)
    }
}
