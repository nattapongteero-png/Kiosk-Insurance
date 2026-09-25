package th.go.banlat.kiosk.ui.insurance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import th.go.banlat.kiosk.data.CoverByPlan
import th.go.banlat.kiosk.data.DemoPatient
import th.go.banlat.kiosk.data.Plan
import th.go.banlat.kiosk.data.TermsByTag
import th.go.banlat.kiosk.data.ageYMD
import th.go.banlat.kiosk.ui.common.KIcon
import th.go.banlat.kiosk.ui.common.LineIcon
import th.go.banlat.kiosk.ui.common.pearl
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.unitPx

/**
 * รายละเอียดแบบประกัน — อ่านให้ครบก่อนตัดสินใจส่งข้อมูล (ตรงกับ insurance.html หน้า detail)
 * การ์ดสรุป + เบี้ย → ความคุ้มครอง → เงื่อนไขสำคัญ → ข้อยกเว้น → หมายเหตุว่าส่งให้บริษัทไหน
 * ระยะ: ส่วนห่าง 48 · หัวข้อ → การ์ด 24 · แถวในการ์ด 24 คั่นเส้นบาง · ล่างเผื่อแถบปุ่ม 248
 */
@Composable
internal fun DetailScreen(plan: Plan, onBack: () -> Unit, onSend: () -> Unit) {
    val u = unitPx()
    val scroll = rememberScrollState()
    val t = TermsByTag.getValue(plan.tag)
    val br = Color(plan.co.brand); val brd = Color(plan.co.brandDeep)
    PageShell {
        Column(Modifier.padding(top = 550.s).fillMaxSize()
            .topFade(16f * u, 96f * u) { scroll.value > 4 }
            .verticalScroll(scroll)
            .padding(start = 80.s, end = 80.s, top = 8.s, bottom = 336.s),
            verticalArrangement = Arrangement.spacedBy(48.s)) {
            Hero(plan, br, brd)
            Section("ความคุ้มครอง", br) {
                CoverByPlan[plan.name].orEmpty().forEachIndexed { i, c -> KvRow(c.label, c.sub, c.value, first = i == 0) }
            }
            Section("เงื่อนไขสำคัญ", br) {
                listOf("อายุที่รับประกัน" to t.age, "ระยะเวลาคุ้มครอง" to t.term, "ระยะเวลารอคอย" to t.wait, "การชำระเบี้ย" to t.pay)
                    .forEachIndexed { i, (k, v) -> KvRow(k, "", v, first = i == 0) }
            }
            Section("ข้อยกเว้นที่ควรทราบ", br, rows = false) {
                t.exclusions.forEach { x ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.s)) {
                        Box(Modifier.padding(top = 15.s).size(10.s).clip(CircleShape).background(br))
                        KText(x, 26, lineHeight = 40f)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.s)).background(br.copy(alpha = .06f))
                .padding(horizontal = 32.s, vertical = 24.s), horizontalArrangement = Arrangement.spacedBy(16.s)) {
                LineIcon(KIcon.Info, brd, Modifier.padding(top = 2.s).size(32.s), 2f)
                KText(buildAnnotatedString {
                    append("ข้อมูลนี้เป็น"); withStyle(SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold)) { append("สรุปเบื้องต้น") }
                    append(" เงื่อนไขจริงเป็นไปตามกรมธรรม์ เมื่อกดส่ง ระบบจะส่งข้อมูลสุขภาพตามที่ท่านยินยอมให้ ")
                    withStyle(SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold)) { append(plan.co.name) }
                    append(" เท่านั้น เพื่อพิจารณาเบี้ยและการรับประกัน")
                }, 24, color = K.InkMuted, lineHeight = 36f)
            }
        }
        BottomBar {
            Row(Modifier.fillMaxWidth().padding(horizontal = 80.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                SecondaryPill("ย้อนกลับ", onBack, Modifier.widthIn(min = 280.s))
                PrimaryPill("ส่งข้อมูลเพื่อขอพิจารณา", onSend, Modifier.weight(1f))   // ปุ่มใช้สีของแอป ไม่เปลี่ยนตามบริษัท
            }
        }
    }
}

@Composable
private fun Hero(plan: Plan, br: Color, brd: Color) {
    // โทนตามบริษัท: พื้นขาวไล่สีโลโก้อ่อนๆ จากบน
    Column(Modifier.fillMaxWidth().pearl(24f, ring = br)
        .background(Color.White)
        .background(Brush.verticalGradient(0f to br.copy(alpha = .10f), .45f to br.copy(alpha = .03f), 1f to Color.Transparent))
        .padding(40.s), verticalArrangement = Arrangement.spacedBy(32.s)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.s)) {
            Box(Modifier.border(1.5.s, br.copy(alpha = .45f), CircleShape).clip(CircleShape)) { InsurerLogo(plan.co, 112, 16) }
            Column {
                KText(plan.tag, 22, Modifier.clip(RoundedCornerShape(99.s)).background(br.copy(alpha = .12f)).padding(horizontal = 16.s, vertical = 4.s),
                    weight = FontWeight.SemiBold, color = brd, lineHeight = 32f, softWrap = false)
                KText(plan.name, 52, Modifier.padding(top = 8.s), weight = FontWeight.Bold, lineHeight = 68f, softWrap = false)
                KText(plan.co.name, 26, color = K.InkMuted, lineHeight = 36f, softWrap = false)
            }
        }
        KText(plan.cov, 30, weight = FontWeight.Medium, lineHeight = 44f)
        Box(Modifier.fillMaxWidth().height(1.s).background(br.copy(alpha = .22f)))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                KText("เบี้ยประกันเริ่มต้น", 26, color = K.InkMuted, lineHeight = 36f)
                KText("ประมาณการสำหรับอายุ ${ageYMD(DemoPatient.dob).substringBefore(' ')} ปี", 22, color = K.InkSoft, lineHeight = 32f)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.s)) {
                KText(plan.price, 64, weight = FontWeight.Bold, color = brd, lineHeight = 72f, tabular = true)
                KText("บาท/ปี", 26, Modifier.padding(bottom = 10.s), color = K.InkMuted)
            }
        }
    }
}

@Composable
private fun Section(title: String, br: Color, rows: Boolean = true, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.s)) {
        KText(title, 40, weight = FontWeight.Bold, lineHeight = 56f)
        Column(Modifier.fillMaxWidth().pearl(24f, gradientRing = false)   // การ์ดล่าง: ขอบทองบางเรียบ (สีบริษัทเฉพาะการ์ดบนสุด)
            .padding(horizontal = 40.s, vertical = if (rows) 8.s else 32.s),
            verticalArrangement = if (rows) Arrangement.Top else Arrangement.spacedBy(16.s)) { content() }
    }
}

@Composable
private fun KvRow(k: String, sub: String, v: String, first: Boolean) {
    if (!first) Box(Modifier.fillMaxWidth().height(1.s).background(Color(0x1414265A)))
    Row(Modifier.fillMaxWidth().padding(vertical = 24.s), horizontalArrangement = Arrangement.spacedBy(32.s)) {
        Column(Modifier.weight(1f)) {
            KText(k, 28, lineHeight = 40f)
            if (sub.isNotEmpty()) KText(sub, 22, color = K.InkSoft, lineHeight = 32f)
        }
        KText(v, 28, weight = FontWeight.Bold, lineHeight = 40f, align = TextAlign.End, tabular = true, softWrap = false)
    }
}

