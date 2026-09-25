package th.go.banlat.kiosk.ui.insurance

import th.go.banlat.kiosk.ui.common.imgPainter
import th.go.banlat.kiosk.ui.common.img
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.data.FitMode
import th.go.banlat.kiosk.data.Insurer
import th.go.banlat.kiosk.data.Plan
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

/** โลโก้บริษัทในวงกลมขาว + วงแหวนทอง · โลโก้พื้นเต็ม (cover) เต็มวง · โลโก้อื่นวางกลางเว้นขอบ */
@Composable
fun InsurerLogo(co: Insurer, size: Int, inset: Int) {
    Box(Modifier.size(size.s).creamDisc(halo = 7f, white = true), contentAlignment = Alignment.Center) {
        if (co.fit == FitMode.Cover) Image(imgPainter(co.logo), co.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Image(imgPainter(co.logo), co.name, Modifier.size((size - inset * 2).s), contentScale = ContentScale.Fit)
    }
}

/** การ์ดแบบประกัน (ผิวไข่มุก ขอบทองบาง) · แตะ = เลือกแบบนี้แล้วส่งข้อมูล */
@Composable
fun PlanCard(p: Plan, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.pearl(24f).press(onClick = onClick).padding(32.s), verticalArrangement = Arrangement.spacedBy(8.s)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.s), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            InsurerLogo(p.co, 64, 10)
            KText(p.tag, 20, Modifier.clip(RoundedCornerShape(99.s)).background(K.GoldTint).padding(horizontal = 16.s, vertical = 4.s),
                weight = FontWeight.SemiBold, color = K.GoldText, softWrap = false)
        }
        KText(p.name, 32, weight = FontWeight.Bold, lineHeight = 40f, softWrap = false)
        KText(p.cov, 22, color = K.InkMuted, lineHeight = 30.8f)
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.s)) {
            KText("เบี้ยเริ่มต้น", 20, color = K.InkSoft)
            KText(p.price, 30, weight = FontWeight.Bold, tabular = true, lineHeight = 36f)
            KText("บาท/ปี", 20, color = K.InkSoft)
        }
    }
}

enum class StatusKind { Busy, Done, Wait }

@Composable
fun StatusPill(kind: StatusKind, text: String) {
    val (bg, fg) = when (kind) {
        StatusKind.Done -> K.Green050 to K.GreenDeep
        StatusKind.Wait -> K.GoldTint to K.GoldText
        StatusKind.Busy -> Color(0x0D14265A) to K.InkMuted
    }
    Row(Modifier.clip(RoundedCornerShape(99.s)).background(bg).padding(horizontal = 18.s, vertical = 10.s),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.s)) {
        when (kind) {
            StatusKind.Done -> LineIcon(KIcon.Check, K.Green, Modifier.size(22.s), 3f)
            StatusKind.Wait -> LineIcon(KIcon.Clock, K.GoldIcon, Modifier.size(22.s), 2f)
            StatusKind.Busy -> GoldSpinner(Modifier.size(22.s), 4f)
        }
        KText(text, 22, weight = FontWeight.SemiBold, color = fg, softWrap = false)
    }
}

/** สรุปแบบประกันที่เลือก: โลโก้ 80 · ชื่อแบบ · บริษัท · สถานะ */
@Composable
fun PlanSummary(p: Plan, modifier: Modifier, status: @Composable () -> Unit) {
    Row(modifier.pearl(24f, gradientRing = false).padding(32.s), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.s)) {
        InsurerLogo(p.co, 80, 12)
        Column(Modifier.weight(1f)) {
            KText(p.name, 34, weight = FontWeight.Bold, lineHeight = 44f, softWrap = false)
            KText(p.co.name, 22, color = K.InkMuted, lineHeight = 32f, softWrap = false)
        }
        status()
    }
}

@Composable
fun AppRow(name: String, sub: String, done: Boolean) {
    Row(Modifier.fillMaxWidth().pearl(24f, gradientRing = false).padding(horizontal = 32.s, vertical = 24.s),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.s)) {
        // หมอพร้อม = ไอคอนจริงจาก Google Play · MyAtlas ยังไม่มีโลโก้ทางการ ใช้ไอคอนโทรศัพท์ไปก่อน
        // TODO: ขอไฟล์โลโก้ MyAtlas จากทีม BMS แล้วเพิ่มเงื่อนไขตรงนี้
        if (name.contains("หมอพร้อม")) {
            Image(imgPainter(R.drawable.app_mohprom), name, Modifier.size(64.s)
                .softShadow(16f, Shade(Color(0x1F14265A), 4f, 10f))
                .clip(RoundedCornerShape(16.s)).border(1.s, Color(0x1414265A), RoundedCornerShape(16.s)),
                contentScale = ContentScale.Crop)
        } else Box(Modifier.size(64.s).creamDisc(), contentAlignment = Alignment.Center) {
            LineIcon(KIcon.Phone, K.GoldIcon, Modifier.size(32.s))
        }
        Column(Modifier.weight(1f)) {
            KText(name, 30, weight = FontWeight.Bold, lineHeight = 40f)
            KText(sub, 22, color = K.InkMuted, lineHeight = 32f)
        }
        Row(Modifier.clip(RoundedCornerShape(99.s)).background(if (done) K.Green050 else K.GoldTint).padding(horizontal = 16.s, vertical = 8.s),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.s)) {
            if (done) LineIcon(KIcon.Check, K.Green, Modifier.size(20.s), 3f)
            KText(if (done) "ส่งแล้ว" else "รอแจ้งผล", 22, weight = FontWeight.SemiBold, color = if (done) K.GreenDeep else K.GoldText)
        }
    }
}

/** ปุ่มรอง (ขาว ขอบเส้นบาง) */
@Composable
fun SecondaryPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.height(88.s)
        .softShadow(44f, Shade(Color(0x1414265A), 8f, 20f))
        .clip(RoundedCornerShape(99.s)).background(Color.White).border(1.5.s, K.Line, RoundedCornerShape(99.s))
        .press(scaleTo = .97f, onClick = onClick).padding(horizontal = 56.s), contentAlignment = Alignment.Center) {
        KText(text, 28, weight = FontWeight.SemiBold, color = K.InkMuted, softWrap = false)
    }
}

/** ปุ่มหลัก (น้ำเงินกรมท่า) */
@Composable
fun PrimaryPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                colors: List<Color> = listOf(Color(0xFF223A7A), K.Ink), glow: Color = Color(0x3814265A)) {
    Box(modifier.height(88.s)
        .softShadow(44f, Shade(glow, 10f, 24f))
        .clip(RoundedCornerShape(99.s)).background(Brush.verticalGradient(colors))
        .press(scaleTo = .97f, onClick = onClick).padding(horizontal = 56.s), contentAlignment = Alignment.Center) {
        KText(text, 30, weight = FontWeight.Bold, color = Color.White, softWrap = false)
    }
}

