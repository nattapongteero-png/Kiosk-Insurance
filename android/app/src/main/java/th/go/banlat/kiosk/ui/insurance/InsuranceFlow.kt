package th.go.banlat.kiosk.ui.insurance

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
import th.go.banlat.kiosk.ui.common.HospitalBanner
import th.go.banlat.kiosk.ui.common.drawBannerFade
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
    data class Detail(val plan: Plan) : Step
    data class Sending(val plan: Plan) : Step
    data class Sent(val plan: Plan) : Step
    data class Slow(val plan: Plan) : Step
}

/**
 * flow ประกัน: เลือกแบบประกัน → อ่านรายละเอียดเต็ม → modal กำลังส่งข้อมูล → สรุป (ส่งสำเร็จ / ส่งนาน รับผลทางแอป)
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
                Step.Select -> SelectScreen(onPick = { step = Step.Detail(it) }, onCancel = onExit)
                is Step.Detail -> DetailScreen(s.plan, onBack = { step = Step.Select }, onSend = { step = Step.Sending(s.plan) })
                is Step.Sending -> DetailScreen(s.plan, onBack = {}, onSend = {})
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
internal fun PageShell(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(imgPainter(R.drawable.bg_home), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        // ชั้นฟ้าไล่เฉดใต้แบนเนอร์ (ตรงกับ .bannerFade ใน insurance.html / services.html): ฟ้าท้ายแบนเนอร์ → อ่อนลง → จางหาย y196→1000
        // อยู่ใต้พื้นขาว (veil เริ่ม y262) จึงเห็นเฉพาะช่วงท้ายแบนเนอร์และขอบข้างมุมโค้ง
        Canvas(Modifier.offset(y = 196.s).fillMaxWidth().height(804.s)) {
            fun sm(t: Float) = t * t * (3 - 2 * t)
            fun ss(t: Float) = t.coerceIn(0f, 1f).let { it * it * it * (it * (it * 6 - 15) + 10) }
            fun c(r: Int, g: Int, b: Int, a: Float) = Color(r, g, b, (255 * a).toInt())
            val keys = listOf(196f to c(178, 238, 244, 1f), 262f to c(190, 240, 247, .96f), 360f to c(214, 241, 249, .9f),
                480f to c(228, 242, 249, .74f), 640f to c(234, 243, 248, .46f), 820f to c(237, 244, 248, .18f), 1000f to c(238, 244, 248, 0f))
            fun at(y: Float): Color {
                for (n in 0 until keys.size - 1) {
                    val (y0, c0) = keys[n]; val (y1, c1) = keys[n + 1]
                    if (y in y0..y1) return androidx.compose.ui.graphics.lerp(c0, c1, sm((y - y0) / (y1 - y0)))
                }
                return keys.last().second
            }
            // สีท้องฟ้าตามแนวนอนของแบนเนอร์ค่อยๆ หมดน้ำหนักใน y230→480 (ตรงกับ .bannerFade)
            drawBannerFade(196f, 804f, size.height / 804f, color = { at(it).copy(alpha = 1f) }, alpha = { at(it).alpha }, tint = { ss((it - 230f) / 250f) })
        }
        // ทุกหน้าข้างใน: พื้นขาวไล่จางผืนเดียว ต่ำสุด 65% ช่วงกลาง ให้เห็นลายพื้นหลังรางๆ (ตรงกับ .veil)
        Veil(Modifier.offset(y = 262.s).fillMaxSize(), floor = .65f)
        // หัวจอตำแหน่งเดียวกับหน้าแรก
        HospitalBanner(fade = false)   // หัวจอ = แบนเนอร์โรงพยาบาล ชุดเดียวกับหน้าแรก (สูง 262)
        content()
        PatientBar(Modifier.offset(y = 262.s).fillMaxWidth())
    }
}

/** พื้นขาว: ทึบที่มุมโค้งบน → ใสที่ +640 → ขาวสนิทที่ +920 แล้วขาวจนสุดจอ (smoothstep เหมือนต้นแบบ) */
@Composable
private fun Veil(modifier: Modifier, floor: Float = 0f) {
    val u = unitPx()
    Canvas(modifier.clip(RoundedCornerShape(topStart = 40.s, topEnd = 40.s))) {
        fun sm(t: Float) = t * t * (3 - 2 * t)
        val stops = buildList {
            for (i in 0..12) add(640f * i / 12 to Color.White.copy(alpha = 1f - (1f - floor) * sm(i / 12f)))
            for (i in 1..8) add(640f + 280f * i / 8 to Color.White.copy(alpha = floor + (1f - floor) * sm(i / 8f)))
        }.map { (y, c) -> (y * u / size.height) to c }.toTypedArray()
        drawRect(Brush.verticalGradient(*stops, 1f to Color.White))
    }
}

/** ผู้ยืนยันตัวตน (Figma 67:947) — เลขบัตรปิดเหลือ 3 หลักท้าย */
@Composable
private fun PatientBar(modifier: Modifier) {
    val p = DemoPatient
    // 3 แถว: ชื่อ · เลขบัตร/อายุ · สิทธิการรักษาเต็มความกว้าง (ชื่อสิทธิบางรายการยาวมาก) · ป้ายกำกับอยู่บนค่าทุกช่อง · จบที่ y550
    @Composable
    fun Kv(k: String, v: String, modifier: Modifier) = Column(modifier, verticalArrangement = Arrangement.spacedBy(4.s)) {
        KText(k, 24, color = K.InkSoft, lineHeight = 32f, softWrap = false)
        KText(v, 28, weight = FontWeight.Bold, lineHeight = 40f, tabular = true, softWrap = false, maxLines = 1, minSize = 22)
    }
    Column(modifier.padding(start = 80.s, end = 80.s, top = 24.s, bottom = 24.s), verticalArrangement = Arrangement.spacedBy(12.s)) {
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
            Kv("เลขบัตรประชาชน", maskCid(p.cid), Modifier.weight(1f))
            Kv("อายุ", ageYMD(p.dob), Modifier.weight(1f))
        }
        Kv("สิทธิการรักษา", th.go.banlat.kiosk.data.DemoSession.rightFull, Modifier.fillMaxWidth())
    }
}

/** แถบล่างคงที่ + ปุ่มกลางจอ ขอบบนปุ่มที่ y1752
 *  พื้นขาวโปร่งไล่เฉด (0 → 90% ใน 80 แรก → 95%) ให้เนื้อหาที่เลื่อนผ่านยังเห็นรางๆ ไม่โดนตัดขาด แต่ปุ่มยังเด่น
 *  HTML เบลอด้วย backdrop-filter ได้ · Android 7 (ตู้จริง) เบลอสิ่งที่อยู่ข้างหลังไม่ได้ จึงใช้แค่ขาวโปร่ง */
@Composable
internal fun BoxScope.BottomBar(button: @Composable () -> Unit) {
    // สูง 288: ไล่จางนุ่ม 96 แรก (เนื้อหาค่อยๆ จางหายเข้าหาปุ่ม) → ทึบ 24 เหนือปุ่ม → ปุ่ม 88 → ห่างขอบจอ 80
    val h = 288f
    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(h.s)
        .background(Brush.verticalGradient(0f to Color.Transparent, 32f / h to Color.White.copy(alpha = .3f), 60f / h to Color.White.copy(alpha = .66f),
            84f / h to Color.White.copy(alpha = .88f), 96f / h to Color.White.copy(alpha = .93f), 1f to Color.White.copy(alpha = .95f)))
        .padding(bottom = 80.s), contentAlignment = Alignment.BottomCenter) { button() }
}

@Composable
internal fun Heading(title: String, sub: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        KText(title, 48, weight = FontWeight.Bold, lineHeight = 68f, softWrap = false)
        KText(sub, 32, weight = FontWeight.Medium, color = K.InkSub, lineHeight = 48f, softWrap = false)
    }
}

/** สีขอบบนของพื้นที่เลื่อน: ตอนยังไม่เลื่อนจาง 16 · เลื่อนแล้วจางยาว 96 ให้เนื้อหาค่อยๆ หายใต้ข้อมูลผู้ป่วย */
internal fun Modifier.topFade(px16: Float, px96: Float, scrolled: () -> Boolean) = this
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
        Column(Modifier.padding(top = 586.s).fillMaxSize()
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
            Column(Modifier.padding(start = 80.s, end = 80.s, top = 48.s, bottom = 336.s)) {
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
    Image(imgPainter(R.drawable.ins_couple, 2), null, modifier
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
internal fun SummaryScreen(plan: Plan, slow: Boolean, onDone: () -> Unit) {
    PageShell {
        // จัดกึ่งกลาง ไม่กระจุกบนซ้าย: ผู้ใช้ยืนมองระดับสายตา (กลาง–ล่างจอ) ได้โดยไม่ต้องเงยหน้า
        // ไอคอนสถานะ 160 → หัวข้อกลาง → การ์ด → รหัสรายการ · ทั้งก้อนอยู่กลางระหว่างข้อมูลผู้ป่วย (y504) กับแถบปุ่ม (ล่าง 248)
        Column(Modifier.fillMaxSize().padding(start = 80.s, end = 80.s, top = 594.s, bottom = 328.s),
            verticalArrangement = Arrangement.spacedBy(48.s, Alignment.CenterVertically)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.padding(bottom = 32.s).size(160.s).creamDisc(ring = 2f, halo = 14f), contentAlignment = Alignment.Center) {
                    if (slow) LineIcon(KIcon.Clock, K.GoldIcon, Modifier.size(80.s), 1.8f)
                    else LineIcon(KIcon.BigCheck, K.Green, Modifier.size(80.s), 2.4f)
                }
                KText(if (slow) "การส่งข้อมูลใช้เวลานานกว่าปกติ" else "ส่งข้อมูลให้บริษัทประกันแล้ว",
                    48, weight = FontWeight.Bold, lineHeight = 68f, align = TextAlign.Center)
                KText(if (slow) "ท่านไม่ต้องรอที่ตู้ ระบบจะแจ้งผลผ่านแอป" else "ท่านจะได้รับผลผ่านแอปด้านล่าง",
                    32, weight = FontWeight.Medium, color = K.InkSub, lineHeight = 48f, align = TextAlign.Center)
            }
            Column(verticalArrangement = Arrangement.spacedBy(24.s), horizontalAlignment = Alignment.CenterHorizontally) {
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
                RefId()
            }
        }
        BottomBar { PrimaryPill("เข้าใจแล้ว, ไปที่ระบบลงทะเบียน", onDone, Modifier.padding(horizontal = 80.s).fillMaxWidth()) }   // ปุ่มหลักเดี่ยว = เต็มความกว้าง
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

