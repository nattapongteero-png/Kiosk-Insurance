package th.go.banlat.kiosk.ui.insurance

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
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.data.DemoPatient
import th.go.banlat.kiosk.data.OtherPlans
import th.go.banlat.kiosk.data.Plan
import th.go.banlat.kiosk.data.RecommendedPlans
import th.go.banlat.kiosk.data.RequestId
import th.go.banlat.kiosk.data.ageYMD
import th.go.banlat.kiosk.data.maskCid
import th.go.banlat.kiosk.ui.common.BrandBlock
import th.go.banlat.kiosk.ui.common.ClockBlock
import th.go.banlat.kiosk.ui.common.KIcon
import th.go.banlat.kiosk.ui.common.LineIcon
import th.go.banlat.kiosk.ui.common.Shade
import th.go.banlat.kiosk.ui.common.creamDisc
import th.go.banlat.kiosk.ui.common.softShadow
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.unitPx
import th.go.banlat.kiosk.ui.welcome.swallowTaps

private sealed interface Step {
    data object Select : Step
    data class Sending(val plan: Plan) : Step
    data class Sent(val plan: Plan) : Step
    data class Slow(val plan: Plan) : Step
}

/**
 * flow ประกัน: เลือกแบบประกัน → modal กำลังส่งข้อมูล → สรุป (ส่งสำเร็จ / ส่งนาน รับผลทางแอป)
 * ต้นแบบ: บริษัทที่ slow = true (AIA) จำลองกรณีส่งนาน · TODO(integration): ใช้ผลตอบกลับจริงจากระบบกลาง BMS
 */
@Composable
fun InsuranceFlow(onExit: () -> Unit) {
    var step by remember { mutableStateOf<Step>(Step.Select) }
    LaunchedEffect(step) {
        val s = step
        if (s is Step.Sending) {
            delay(if (s.plan.co.slow) 5200 else 2800)
            step = if (s.plan.co.slow) Step.Slow(s.plan) else Step.Sent(s.plan)
        }
    }
    Box(Modifier.fillMaxSize()) {
        val sending = step is Step.Sending
        Box(Modifier.fillMaxSize().then(if (sending && Build.VERSION.SDK_INT >= 31) Modifier.blur(8.s) else Modifier)) {
            when (val s = step) {
                Step.Select, is Step.Sending -> SelectScreen(onPick = { step = Step.Sending(it) }, onCancel = onExit)
                is Step.Sent -> SummaryScreen(s.plan, slow = false, onDone = onExit)
                is Step.Slow -> SummaryScreen(s.plan, slow = true, onDone = onExit)
            }
        }
        (step as? Step.Sending)?.let { SendModal(it.plan) }
    }
}

// ---------------- ส่วนที่ใช้ร่วมทุกหน้า ----------------

/** พื้นหลัง + หัวจอ + ผู้ยืนยันตัวตน (คงที่ ไม่เลื่อน) + พื้นขาวไล่สีผืนเดียวหลังเนื้อหา */
@Composable
private fun PageShell(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_home), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Veil(Modifier.offset(y = 208.s).fillMaxSize())
        // หัวจอตำแหน่งเดียวกับหน้าแรก
        BrandBlock(Modifier.offset(80.s, 80.s))
        ClockBlock(Modifier.align(Alignment.TopEnd).padding(top = 80.s, end = 80.s))
        content()
        PatientBar(Modifier.offset(y = 208.s).fillMaxWidth())
    }
}

/** พื้นขาว: ทึบที่มุมโค้งบน → ใสที่ +816 → ขาวสนิทที่ +1101 แล้วขาวจนสุดจอ (smoothstep เหมือนต้นแบบ) */
@Composable
private fun Veil(modifier: Modifier) {
    val u = unitPx()
    Canvas(modifier.clip(RoundedCornerShape(topStart = 40.s, topEnd = 40.s))) {
        fun sm(t: Float) = t * t * (3 - 2 * t)
        val stops = buildList {
            for (i in 0..12) add(816f * i / 12 to Color.White.copy(alpha = 1f - sm(i / 12f)))
            for (i in 1..8) add(816f + 285f * i / 8 to Color.White.copy(alpha = sm(i / 8f)))
        }.map { (y, c) -> (y * u / size.height) to c }.toTypedArray()
        drawRect(Brush.verticalGradient(*stops, 1f to Color.White))
    }
}

/** ผู้ยืนยันตัวตน (Figma 67:947) — เลขบัตรปิดเหลือ 3 หลักท้าย */
@Composable
private fun PatientBar(modifier: Modifier) {
    val p = DemoPatient
    Column(modifier.padding(horizontal = 80.s, vertical = 48.s), verticalArrangement = Arrangement.spacedBy(24.s)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.s)) {
            Box(Modifier.size(88.s).creamDisc(halo = 8f), contentAlignment = Alignment.Center) {
                LineIcon(KIcon.User, K.GoldIcon, Modifier.size(44.s))
            }
            Column {
                KText(p.name, 40, weight = FontWeight.Bold, lineHeight = 44f)
                KText("HN ${p.hn}", 28, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 44f)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.s)) {
            listOf("เลขบัตรประชาชน" to maskCid(p.cid), "อายุ" to ageYMD(p.dob), "สิทธิการรักษา" to p.right).forEach { (k, v) ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.s)) {
                    KText(k, 24, color = K.InkSoft, lineHeight = 40f, softWrap = false)
                    KText(v, 28, weight = FontWeight.Bold, lineHeight = 40f, tabular = true, softWrap = false)
                }
            }
        }
    }
}

/** แถบล่างคงที่: พื้นขาวไล่ขึ้น 40 + ปุ่มกลางจอ ขอบบนปุ่มที่ y1752 */
@Composable
private fun BoxScope.BottomBar(button: @Composable () -> Unit) {
    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(248.s)
        .background(Brush.verticalGradient(0f to Color.Transparent, 40f / 248f to Color.White))
        .padding(bottom = 80.s), contentAlignment = Alignment.BottomCenter) { button() }
}

@Composable
private fun Heading(title: String, sub: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        KText(title, 48, weight = FontWeight.Bold, lineHeight = 68f, softWrap = false)
        KText(sub, 32, weight = FontWeight.Medium, color = K.InkSub, lineHeight = 48f, softWrap = false)
    }
}

/** สีขอบบนของพื้นที่เลื่อน: ตอนยังไม่เลื่อนจาง 16 · เลื่อนแล้วจางยาว 96 ให้เนื้อหาค่อยๆ หายใต้ข้อมูลผู้ป่วย */
private fun Modifier.topFade(px16: Float, px96: Float, scrolled: () -> Boolean) = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val h = size.height
        val brush = if (scrolled()) Brush.verticalGradient(0f to Color.Transparent, (40f / 96f) * px96 / h to Color.Black.copy(alpha = .35f), px96 / h to Color.Black)
                    else Brush.verticalGradient(0f to Color.Transparent, px16 / h to Color.Black)
        drawRect(brush, blendMode = BlendMode.DstIn)
    }

// ---------------- หน้าเลือกแบบประกัน (Figma 50:101) ----------------
@Composable
private fun SelectScreen(onPick: (Plan) -> Unit, onCancel: () -> Unit) {
    val u = unitPx()
    val scroll = rememberScrollState()
    PageShell {
        Column(Modifier.offset(y = 496.s).fillMaxWidth().height((1920 - 496).s)
            .topFade(16f * u, 96f * u) { scroll.value > 4 }
            .verticalScroll(scroll)) {
            // แบบประกันที่แนะนำ (สูง 517: การ์ดจบที่ 469 + ห่างส่วนถัดไป 48)
            Box(Modifier.fillMaxWidth().height(517.s)) {
                Heading("แบบประกันที่แนะนำ", "เลือกประกันที่ต้องการ", Modifier.offset(80.s, 8.s))
                ArtCouple(Modifier.offset(0.s, 154.s).size(615.s, 410.s))
                // แถบการ์ดปัดซ้าย-ขวา: เผื่อพื้นที่ให้เงา บน 40 ซ้าย 40 ล่าง 72 · ปัดแล้วการ์ดหายเข้าหลังภาพที่ x475
                val rail = rememberLazyListState()
                LazyRow(Modifier.offset(475.s, 34.s).width((1080 - 475).s).height(507.s), state = rail,
                    contentPadding = PaddingValues(start = 40.s, end = 80.s, top = 40.s, bottom = 72.s),
                    horizontalArrangement = Arrangement.spacedBy(40.s),
                    flingBehavior = rememberSnapFlingBehavior(rail)) {
                    items(RecommendedPlans) { PlanCard(it, Modifier.size(348.s, 395.s)) { onPick(it) } }
                }
            }
            // แบบประกันอื่น
            Column(Modifier.padding(start = 80.s, end = 80.s, top = 48.s, bottom = 248.s)) {
                Heading("แบบประกันอื่น", "เลือกประกันที่ต้องการ")
                Spacer(Modifier.height(32.s))
                OtherPlans.chunked(2).forEachIndexed { i, row ->
                    if (i > 0) Spacer(Modifier.height(48.s))
                    Row(horizontalArrangement = Arrangement.spacedBy(40.s)) {
                        row.forEach { PlanCard(it, Modifier.weight(1f).height(318.s)) { onPick(it) } }
                    }
                }
            }
        }
        BottomBar { SecondaryPill("ยังไม่สนใจ, ไปที่ระบบลงทะเบียน", onCancel) }
    }
}

/** ภาพประกอบคู่สูงอายุ จางลงด้านล่าง (45% → 88%) */
@Composable
private fun ArtCouple(modifier: Modifier) {
    Image(painterResource(R.drawable.ins_couple), null, modifier
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            fun sm(t: Float) = t * t * (3 - 2 * t)
            val stops = (0..8).map { i -> (.45f + .43f * i / 8f) to Color.Black.copy(alpha = 1f - sm(i / 8f)) }.toTypedArray()
            drawRect(Brush.verticalGradient(0f to Color.Black, *stops), blendMode = BlendMode.DstIn)
        }, contentScale = ContentScale.Crop)
}

// ---------------- modal กำลังส่งข้อมูล ----------------
@Composable
private fun SendModal(plan: Plan) {
    Box(Modifier.fillMaxSize().background(K.ScrimLight).swallowTaps().padding(horizontal = 80.s), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth()
            .softShadow(40f, Shade(Color(0x59061432), 40f, 100f))
            .clip(RoundedCornerShape(40.s))
            .background(Brush.linearGradient(0f to K.PearlTop, .55f to K.PearlMid, 1f to K.PearlBottom))
            .padding(start = 56.s, end = 56.s, top = 64.s, bottom = 56.s),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.s)) {
            GoldSpinner(Modifier.padding(bottom = 8.s).size(160.s), thickness = 13f)
            KText("กำลังส่งข้อมูลให้บริษัทประกัน", 44, weight = FontWeight.Bold, lineHeight = 60f, align = TextAlign.Center)
            KText("ระบบกำลังส่งข้อมูลสุขภาพตามที่ท่านยินยอม\nผ่านระบบกลางของ BMS แบบเข้ารหัส กรุณารอสักครู่",
                26, color = K.InkMuted, lineHeight = 40f, align = TextAlign.Center)
            PlanSummary(plan, Modifier.padding(top = 8.s).fillMaxWidth()) { StatusPill(StatusKind.Busy, "กำลังส่ง") }
            RefId()
        }
    }
}

@Composable
internal fun GoldSpinner(modifier: Modifier, thickness: Float) {
    val rot = rememberInfiniteTransition(label = "spin").animateFloat(0f, 360f,
        infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart), label = "r")
    val u = unitPx()
    Canvas(modifier) {
        val w = thickness * u
        val sweep = Brush.sweepGradient(0f to Color(0x00D6A850), 200f / 360f to Color(0x00D6A850),
            330f / 360f to Color(0xFFD9A546), 1f to Color(0xFFF2CC78))
        rotate(rot.value) {
            drawCircle(sweep, radius = size.minDimension / 2 - w / 2, style = androidx.compose.ui.graphics.drawscope.Stroke(w))
        }
    }
}

// ---------------- หน้าสรุป (ส่งสำเร็จ / ส่งนาน) ----------------
@Composable
private fun SummaryScreen(plan: Plan, slow: Boolean, onDone: () -> Unit) {
    PageShell {
        Column(Modifier.offset(y = 504.s).fillMaxWidth().padding(horizontal = 80.s), verticalArrangement = Arrangement.spacedBy(32.s)) {
            if (slow) Heading("การส่งข้อมูลใช้เวลานานกว่าปกติ", "ท่านไม่ต้องรอที่ตู้ ระบบจะแจ้งผลผ่านแอป")
            else Heading("ส่งข้อมูลให้บริษัทประกันแล้ว", "ท่านจะได้รับผลผ่านแอปด้านล่าง")
            Column(verticalArrangement = Arrangement.spacedBy(24.s)) {
                PlanSummary(plan, Modifier.fillMaxWidth()) {
                    if (slow) StatusPill(StatusKind.Wait, "กำลังดำเนินการ") else StatusPill(StatusKind.Done, "บริษัทได้รับแล้ว")
                }
                Column(verticalArrangement = Arrangement.spacedBy(16.s)) {
                    if (slow) {
                        AppRow("แอปหมอพร้อม", "แจ้งเตือนเมื่อส่งข้อมูลเสร็จ", done = false)
                        AppRow("แอป MyAtlas", "แจ้งเตือนและดูรายละเอียดที่ “ประกันของฉัน”", done = false)
                    } else {
                        AppRow("แอป MyAtlas", "ดูรายละเอียดได้ที่เมนู “ประกันของฉัน”", done = true)
                        AppRow("แอปหมอพร้อม", "ดูรายละเอียดได้ที่การแจ้งเตือน", done = true)
                    }
                }
                if (slow) Row(horizontalArrangement = Arrangement.spacedBy(16.s)) {
                    LineIcon(KIcon.Shield, K.InkSoft, Modifier.size(32.s))
                    KText("การแจ้งเตือนจะไม่แสดงข้อมูลสุขภาพ ท่านต้องยืนยันตัวตนในแอปก่อนจึงจะเห็นรายละเอียด",
                        22, color = K.InkMuted, lineHeight = 32f)
                }
                RefId()
            }
        }
        BottomBar { PrimaryPill("เข้าใจแล้ว, ไปที่ระบบลงทะเบียน", onDone) }
    }
}

@Composable
private fun RefId() {
    KText(androidx.compose.ui.text.buildAnnotatedString {
        append("รหัสรายการ ")
        pushStyle(androidx.compose.ui.text.SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold, letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified))
        append(RequestId); pop()
    }, 24, color = K.InkSoft, tabular = true)
}

