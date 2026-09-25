package th.go.banlat.kiosk.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.ui.common.KIcon
import th.go.banlat.kiosk.ui.common.LineIcon
import th.go.banlat.kiosk.ui.common.PathIcon
import th.go.banlat.kiosk.ui.common.Shade
import th.go.banlat.kiosk.ui.common.creamDisc
import th.go.banlat.kiosk.ui.common.imgPainter
import th.go.banlat.kiosk.ui.common.pearl
import th.go.banlat.kiosk.ui.common.press
import th.go.banlat.kiosk.ui.common.softShadow
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.NotoSansThai
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.st
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

/**
 * หน้าตั้งค่าตู้ (สำหรับเจ้าหน้าที่) — ตรงกับ preview/settings.html
 * Pain point เดิม: รายการกว่า 140 ข้อเรียงยาวหน้าเดียว หาไม่เจอ · มีรายการซ้ำ · สลับคำ "เปิด/ปิด"
 * ใหม่: ค้นหาได้ทันที → 9 หมวด (การ์ดบอกจำนวนรายการ/ที่เปิดอยู่) → ในหมวดแยกหัวข้อย่อย
 * TODO(integration): อ่าน/เขียนตาราง setting_kiosk · เข้าหน้านี้ต้องผ่านรหัสผ่านตั้งค่าก่อน
 */
@Composable
fun SettingsScreen(onExit: () -> Unit) {
    var open by remember { mutableStateOf<SettingGroup?>(null) }
    var q by remember { mutableStateOf("") }
    val toggles = remember { mutableStateMapOf<SettingItem, Boolean>() }
    val values = remember { mutableStateMapOf<SettingItem, String>() }
    var toast by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toast) { if (toast != null) { delay(1400); toast = null } }
    fun isOn(i: ToggleItem) = toggles[i] ?: i.on
    // การเชื่อมต่อเซิร์ฟเวอร์ (เดิมเป็นหน้าแยก "เชื่อมต่อ" IP/Port) · TODO(integration): ค่าจากตู้ · ทดสอบ = เชื่อมต่อจริง
    //   เปิดแอปแล้วเชื่อมต่อไม่ได้ → เปิดหน้าตั้งค่าที่การ์ดนี้ให้เอง
    var srvIp by remember { mutableStateOf("10.91.118.56") }
    var srvPort by remember { mutableStateOf("23456") }
    var srvSt by remember { mutableStateOf("ok") }
    LaunchedEffect(srvSt) {
        if (srvSt == "busy") {
            delay(1200)
            srvSt = if (Regex("""^\d{1,3}(\.\d{1,3}){3}$""").matches(srvIp.trim()) && Regex("""^\d{2,5}$""").matches(srvPort.trim())) "ok" else "bad"
            toast = if (srvSt == "ok") "เชื่อมต่อเซิร์ฟเวอร์แล้ว" else "เชื่อมต่อไม่ได้ ตรวจสอบ IP / Port"
        }
    }
    val server: @Composable () -> Unit = { ServerCard(srvIp, { srvIp = it }, srvPort, { srvPort = it }, srvSt) { srvSt = "busy" } }

    Box(Modifier.fillMaxSize()) {
        Image(imgPainter(R.drawable.bg_home), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Color(0xE6FFFFFF)))

        // เนื้อหา (เลื่อนได้) เริ่มใต้หัว y340
        val scroll = rememberScrollState()
        LaunchedEffect(open, q.isNotEmpty()) { scroll.scrollTo(0) }
        Column(Modifier.padding(top = 340.s).fillMaxSize().verticalScroll(scroll).padding(start = 80.s, end = 80.s, top = 24.s, bottom = 120.s)) {
            val query = q.trim()
            val g = open
            when {
                query.isNotEmpty() -> {
                    val hits = SettingGroups.flatMap { gr -> gr.sections.flatMap { sc -> sc.items.map { Triple(gr, sc, it) } } }
                        .filter { (gr, sc, i) -> "${i.label} ${i.hint} ${sc.title} ${gr.name}".contains(query, ignoreCase = true) }
                    if (Regex("เซิร์ฟ|server|ip|port|เชื่อม|ฐานข้อมูล", RegexOption.IGNORE_CASE).containsMatchIn(query)) server()
                    KText("ผลการค้นหา ${hits.size} รายการ", 26, Modifier.padding(top = 8.s, bottom = 20.s), weight = FontWeight.SemiBold, color = K.InkMuted)
                    if (hits.isEmpty()) KText("ไม่พบการตั้งค่าที่ค้นหา", 28, Modifier.fillMaxWidth().padding(vertical = 80.s), color = K.InkMuted, align = androidx.compose.ui.text.style.TextAlign.Center)
                    else ListCard { hits.forEachIndexed { n, (gr, sc, i) ->
                        SettingRow(i, n > 0, "${gr.name} › ${sc.title}", query, toggles, values, ::isOn) { toast = it } } }
                }
                g != null -> {
                    Row(Modifier.height(64.s).clip(RoundedCornerShape(99.s)).background(Color.White).border(1.5.s, K.Line, RoundedCornerShape(99.s))
                        .press { open = null }.padding(start = 20.s, end = 28.s), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.s)) {
                        LineIcon(KIcon.Back, K.InkMuted, Modifier.size(28.s), 2.2f)
                        KText("หมวดทั้งหมด", 24, weight = FontWeight.SemiBold, color = K.InkMuted)
                    }
                    Row(Modifier.padding(top = 28.s, bottom = 8.s), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.s)) {
                        Box(Modifier.size(88.s).creamDisc(), contentAlignment = Alignment.Center) { PathIcon(g.icon, K.GoldIcon, Modifier.size(44.s), 1.9f) }
                        Column {
                            KText(g.name, 44, weight = FontWeight.Bold, lineHeight = 56f)
                            KText(g.desc, 24, color = K.InkMuted, lineHeight = 34f)
                        }
                    }
                    g.sections.forEach { sc ->
                        KText(sc.title, 24, Modifier.padding(start = 8.s, top = 32.s, bottom = 12.s), weight = FontWeight.Bold, color = K.GoldIcon, letterSpacing = .3f)
                        ListCard { sc.items.forEachIndexed { n, i -> SettingRow(i, n > 0, null, "", toggles, values, ::isOn) { toast = it } } }
                    }
                }
                else -> {
                    server()
                    KText("หมวดการตั้งค่า", 26, Modifier.padding(top = 8.s, bottom = 20.s), weight = FontWeight.SemiBold, color = K.InkMuted)
                    SettingGroups.chunked(2).forEach { pair ->
                        Row(Modifier.padding(bottom = 24.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                            pair.forEach { gr ->
                                val items = gr.sections.flatMap { it.items }
                                val on = items.count { it is ToggleItem && isOn(it) }
                                Column(Modifier.weight(1f).pearl(32f, gradientRing = false).press(scaleTo = .98f) { open = gr }.padding(32.s),
                                    verticalArrangement = Arrangement.spacedBy(12.s)) {
                                    Box(Modifier.padding(bottom = 4.s).size(72.s).creamDisc(halo = 8f), contentAlignment = Alignment.Center) {
                                        PathIcon(gr.icon, K.GoldIcon, Modifier.size(36.s), 1.9f)
                                    }
                                    KText(gr.name, 30, weight = FontWeight.Bold, lineHeight = 40f)
                                    KText(gr.desc, 22, Modifier.heightIn(min = 64.s), color = K.InkMuted, lineHeight = 32f)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.s)) {
                                        Chip("${items.size} รายการ", false)
                                        if (on > 0) Chip("เปิดอยู่ $on", true)
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // หัว: ชื่อหน้า · ปุ่มออก · ช่องค้นหา
        Column(Modifier.fillMaxWidth().height(340.s)
            .background(Brush.verticalGradient(0f to Color(0xF2FFFFFF), .88f to Color(0xF2FFFFFF), 1f to Color(0x00FFFFFF)))
            .padding(start = 80.s, end = 80.s, top = 64.s)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.s)) {
                Box(Modifier.size(88.s).creamDisc(halo = 9f), contentAlignment = Alignment.Center) { LineIcon(KIcon.Gear, K.GoldIcon, Modifier.size(44.s)) }
                Column(Modifier.weight(1f)) {
                    KText("ตั้งค่าตู้", 48, weight = FontWeight.Bold, lineHeight = 60f)
                    KText("V.24.09.69 · แก้ไขแล้วบันทึกทันที", 24, color = K.InkMuted, lineHeight = 34f)
                }
                Box(Modifier.size(88.s).clip(CircleShape).background(Color(0x0F14265A)).press(scaleTo = .94f, onClick = onExit), contentAlignment = Alignment.Center) {
                    LineIcon(KIcon.Close, K.InkMuted, Modifier.size(40.s), 2.2f)
                }
            }
            Row(Modifier.padding(top = 40.s).fillMaxWidth().height(96.s)
                .softShadow(99f, Shade(Color(0x1414265A), 10f, 24f))
                .clip(RoundedCornerShape(99.s)).background(Color.White).border(1.5.s, Color(0x66BF913A), RoundedCornerShape(99.s))
                .padding(start = 32.s, end = 20.s), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.s)) {
                LineIcon(KIcon.Search, K.InkSoft, Modifier.size(36.s), 2f)
                Box(Modifier.weight(1f)) {
                    if (q.isEmpty()) KText("ค้นหา เช่น Authen, Scan HN, เครื่องพิมพ์", 30, color = K.InkSoft, softWrap = false)
                    BasicTextField(q, { q = it }, Modifier.fillMaxWidth(), singleLine = true,
                        textStyle = TextStyle(fontFamily = NotoSansThai, fontSize = 30.st, color = K.Ink), cursorBrush = SolidColor(K.Blue))
                }
                if (q.isNotEmpty()) Box(Modifier.size(56.s).clip(CircleShape).background(Color(0x0F14265A)).press { q = "" }, contentAlignment = Alignment.Center) {
                    LineIcon(KIcon.Close, K.InkMuted, Modifier.size(28.s), 2.4f)
                }
            }
        }

        // แจ้งบันทึกแล้ว · อยู่บนจุดเดียวกันทุกหน้า (y296 เหมือนป้ายแจ้งผลบนหน้าแรก) ไม่อยู่ล่างเพราะแป้นพิมพ์/มือบัง
        toast?.let { t ->
            Row(Modifier.align(Alignment.TopCenter).offset(y = 296.s)
                .softShadow(99f, Shade(Color(0x4D14265A), 16f, 40f)).clip(RoundedCornerShape(99.s))
                .background(Brush.linearGradient(listOf(Color(0xFF223A7A), K.Ink))).padding(horizontal = 32.s, vertical = 18.s),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.s)) {
                LineIcon(KIcon.Check, Color.White, Modifier.size(30.s), 2.6f)
                KText(t, 26, weight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun Chip(text: String, on: Boolean) {
    Box(Modifier.height(36.s).clip(RoundedCornerShape(99.s)).background(if (on) K.Green050 else Color(0x0F14265A)).padding(horizontal = 14.s),
        contentAlignment = Alignment.Center) {
        KText(text, 20, weight = FontWeight.SemiBold, color = if (on) K.GreenDeep else K.InkMuted, softWrap = false)
    }
}

@Composable
private fun ListCard(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().pearl(24f, gradientRing = false).padding(horizontal = 32.s)) { content() }
}

/** แถวตั้งค่า: สวิตช์ / ช่องกรอกค่า / ปุ่มสั่งงาน · crumb = หมวด › หัวข้อ (ผลค้นหา) · คำค้นไฮไลต์สีเหลือง */
@Composable
private fun SettingRow(i: SettingItem, divider: Boolean, crumb: String?, query: String,
                       toggles: MutableMap<SettingItem, Boolean>, values: MutableMap<SettingItem, String>,
                       isOn: (ToggleItem) -> Boolean, onSaved: (String) -> Unit) {
    if (divider) Box(Modifier.fillMaxWidth().height(1.s).background(Color(0x1414265A)))
    Row(Modifier.fillMaxWidth().heightIn(min = 104.s).padding(vertical = 20.s), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.s)) {
        Column(Modifier.weight(1f)) {
            crumb?.let { KText(it, 20, weight = FontWeight.SemiBold, color = K.GoldIcon, lineHeight = 28f) }
            KText(buildAnnotatedString {
                val at = if (query.isEmpty()) -1 else i.label.indexOf(query, ignoreCase = true)
                if (at < 0) append(i.label) else {
                    append(i.label.substring(0, at))
                    withStyle(SpanStyle(background = Color(0xFFFFE9A8))) { append(i.label.substring(at, at + query.length)) }
                    append(i.label.substring(at + query.length))
                }
            }, 28, weight = FontWeight.Medium, lineHeight = 40f)
            if (i.hint.isNotEmpty()) KText(i.hint, 22, color = K.InkSoft, lineHeight = 32f)
        }
        when (i) {
            is ToggleItem -> Switch(isOn(i)) { toggles[i] = !isOn(i); onSaved("บันทึกแล้ว") }
            is ValueItem -> {
                val v = values[i] ?: i.value
                Box(Modifier.width(380.s).height(64.s).clip(RoundedCornerShape(16.s)).background(Color.White)
                    .border(1.5.s, K.Line, RoundedCornerShape(16.s)).padding(horizontal = 20.s), contentAlignment = Alignment.CenterStart) {
                    if (v.isEmpty()) KText(i.hint.ifEmpty { "ยังไม่ได้ตั้งค่า" }, 24, color = K.InkSoft, softWrap = false, maxLines = 1)
                    BasicTextField(v, { values[i] = it; onSaved("บันทึกแล้ว") }, Modifier.fillMaxWidth(), singleLine = true,
                        textStyle = TextStyle(fontFamily = NotoSansThai, fontSize = 24.st, color = K.Ink), cursorBrush = SolidColor(K.Blue))
                }
            }
            is ActionItem -> Box(Modifier.height(64.s).clip(RoundedCornerShape(99.s)).background(Color(0xFFFBF3E2))
                .border(1.5.s, Color(0x59BF913A), RoundedCornerShape(99.s)).press { onSaved("ส่งคำสั่งแล้ว") }.padding(horizontal = 28.s),
                contentAlignment = Alignment.Center) {
                KText("ดำเนินการ", 24, weight = FontWeight.SemiBold, color = Color(0xFF8A6420), softWrap = false)
            }
        }
    }
}

/** สวิตช์ใหญ่ 96×56 แตะง่ายบนจอสัมผัส · เปิด = กรมท่า */
@Composable
private fun Switch(on: Boolean, onToggle: () -> Unit) {
    val x by animateFloatAsState(if (on) 40f else 0f, label = "sw")
    Box(Modifier.size(96.s, 56.s).clip(RoundedCornerShape(99.s))
        .background(if (on) Brush.linearGradient(listOf(Color(0xFF223A7A), K.Ink)) else SolidColor(Color(0xFFD5DCE6)))
        .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onToggle)) {
        Box(Modifier.offset((6f + x).s, 6.s).size(44.s).softShadow(22f, Shade(Color(0x4014265A), 2f, 6f)).clip(CircleShape).background(Color.White))
    }
}

/** การ์ดการเชื่อมต่อเซิร์ฟเวอร์ (ตรงกับ .srv ใน settings.html) · สถานะ ok / bad / busy */
@Composable
private fun ServerCard(ip: String, onIp: (String) -> Unit, port: String, onPort: (String) -> Unit, st: String, onTest: () -> Unit) {
    val cloud = listOf("M7 18a4.5 4.5 0 0 1-.6-8.96A6 6 0 0 1 18 8.5a4 4 0 0 1-.5 9.5", "M9 13v6c0 .8 1.3 1.5 3 1.5s3-.7 3-1.5v-6",
        "M9 13c0 .8 1.3 1.5 3 1.5s3-.7 3-1.5-1.3-1.5-3-1.5-3 .7-3 1.5ZM9 16c0 .8 1.3 1.5 3 1.5s3-.7 3-1.5")
    Column(Modifier.padding(bottom = 40.s).fillMaxWidth().pearl(32f, gradientRing = false).padding(32.s)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.s)) {
            Box(Modifier.size(72.s).creamDisc(halo = 8f), contentAlignment = Alignment.Center) { PathIcon(cloud, K.GoldIcon, Modifier.size(36.s), 1.9f) }
            Column(Modifier.weight(1f)) {
                KText("การเชื่อมต่อเซิร์ฟเวอร์", 30, weight = FontWeight.Bold, lineHeight = 40f)
                KText("เครื่องแม่ข่ายที่ตู้ใช้ส่งข้อมูลกับระบบโรงพยาบาล", 22, color = K.InkMuted, lineHeight = 32f)
            }
            val (bg, fg, t) = when (st) {
                "ok" -> Triple(K.Green050, K.GreenDeep, "เชื่อมต่อแล้ว")
                "bad" -> Triple(Color(0xFFFEF3F2), Color(0xFFB42318), "เชื่อมต่อไม่ได้")
                else -> Triple(K.Blue050, K.BlueDeep, "กำลังทดสอบ…")
            }
            Row(Modifier.height(48.s).clip(RoundedCornerShape(99.s)).background(bg).padding(horizontal = 20.s),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.s)) {
                Box(Modifier.size(12.s).clip(CircleShape).background(fg))
                KText(t, 22, weight = FontWeight.SemiBold, color = fg, softWrap = false)
            }
        }
        Row(Modifier.padding(top = 24.s), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.s)) {
            @Composable fun Field(label: String, v: String, on: (String) -> Unit, m: Modifier) = Column(m, verticalArrangement = Arrangement.spacedBy(8.s)) {
                KText(label, 22, color = K.InkSoft)
                Box(Modifier.fillMaxWidth().height(72.s).clip(RoundedCornerShape(16.s)).background(Color.White)
                    .border(1.5.s, K.Line, RoundedCornerShape(16.s)).padding(horizontal = 20.s), contentAlignment = Alignment.CenterStart) {
                    BasicTextField(v, on, Modifier.fillMaxWidth(), singleLine = true,
                        textStyle = TextStyle(fontFamily = NotoSansThai, fontSize = 28.st, color = K.Ink), cursorBrush = SolidColor(K.Blue))
                }
            }
            Field("IP Address", ip, onIp, Modifier.weight(1f))
            Field("Port", port, onPort, Modifier.width(200.s))
            Box(Modifier.height(72.s).softShadow(99f, Shade(Color(0x3814265A), 10f, 24f)).clip(RoundedCornerShape(99.s))
                .background(Brush.linearGradient(listOf(Color(0xFF223A7A), K.Ink))).press(enabled = st != "busy", onClick = onTest)
                .padding(horizontal = 32.s), contentAlignment = Alignment.Center) {
                KText("ทดสอบและเชื่อมต่อ", 26, weight = FontWeight.Bold, color = Color.White, softWrap = false)
            }
        }
    }
}
