package th.go.banlat.kiosk.ui.welcome

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import th.go.banlat.kiosk.ui.theme.NotoSansThai
import th.go.banlat.kiosk.ui.common.SoftBlur

/*
 * ตัวเครื่อง + ช่องสแกน QR + ช่องเสียบบัตร + บัตรประชาชนเคลื่อนไหว
 * เวกเตอร์ชุดเดียวกับ Figma node 26:50 / 26:58 / 26:59 และต้นแบบ HTML (ระบบพิกัด viewBox 800 x 612)
 * ตัวเลขทุกตัวในไฟล์นี้คือพิกัด SVG เดิม ห้ามปรับทีละจุด ให้ปรับที่ transform ของกลุ่ม
 */

private fun p(d: String): Path = PathParser().parsePathString(d).toPath()
private fun c(hex: Long, a: Float = 1f) = Color(hex).copy(alpha = a)

/** ความคืบหน้าการเสียบบัตรในรอบ 14 วิ (ไป 7 วิ กลับ 7 วิ เหมือน animation alternate) */
fun cardPhase(t14: Float): Float = if (t14 < 7000f) t14 / 7000f else (14000f - t14) / 7000f

private val InsertEase = CubicBezierEasing(.45f, .05f, .3f, 1f)
private val ScanEase = CubicBezierEasing(.42f, 0f, .58f, 1f)

private fun insertOffset(ph: Float): Offset {
    val k = when { ph <= .12f -> 0f; ph >= .72f -> 1f; else -> InsertEase.transform((ph - .12f) / .6f) }
    return Offset(-21.6f + (43.5f + 21.6f) * k, 47.3f + (-95.4f - 47.3f) * k)
}

private fun scanAlpha(ph: Float): Float = when {
    ph <= .56f -> 0f; ph >= .72f -> .85f; else -> ScanEase.transform((ph - .56f) / .16f) * .85f
}

private class CardText(val x: Float, val y: Float, val size: Float, val color: Long, val bold: Boolean, val text: String, val ls: Float = 0f)

private val CardTexts = listOf(
    CardText(89f, 30.6f, 18f, 0xFF1E2A5A, true, "บัตรประจำตัวประชาชน"),
    CardText(274f, 29.8f, 15f, 0xFF1F6FD8, true, "Thai National ID Card"),
    CardText(89f, 45.3f, 9f, 0xFF1E2A5A, false, "เลขประจำตัวประชาชน"),
    CardText(89f, 54.9f, 7.5f, 0xFF1F6FD8, false, "Identification Number"),
    CardText(209f, 53.7f, 16f, 0xFF1E2A5A, true, "0 0000 00000 00 0", 1.2f),
    CardText(75f, 77.3f, 9f, 0xFF1E2A5A, false, "ชื่อตัวและชื่อสกุล"),
    CardText(181f, 79.9f, 14f, 0xFF1E2A5A, true, "XXXXX XXXXXXXXX"),
    CardText(176f, 104.8f, 8.5f, 0xFF1F6FD8, false, "Name"),
    CardText(219f, 106f, 13f, 0xFF1E2A5A, true, "XXXXX"),
    CardText(176f, 125.8f, 8.5f, 0xFF1F6FD8, false, "Last name"),
    CardText(219f, 127f, 13f, 0xFF1E2A5A, true, "XXXXX"),
    CardText(195f, 152.7f, 10.5f, 0xFF1E2A5A, false, "เกิดวันที่ 28 มี.ค. 2537"),
    CardText(195f, 170.3f, 9f, 0xFF1F6FD8, false, "Date of Birth 28 Mar. 1994"),
    CardText(195f, 191.7f, 10.5f, 0xFF1E2A5A, false, "ศาสนา พุทธ"),
    CardText(75f, 211.7f, 10.5f, 0xFF1E2A5A, true, "ที่อยู่ xx/xx หมู่ที่ xx ถนน xxxxx"),
    CardText(75f, 228.7f, 10.5f, 0xFF1E2A5A, false, "แขวง xxxxx อ.xxxx จ.xxxxx"),
    CardText(75f, 248.8f, 8.5f, 0xFF1E2A5A, true, "28 มี.ค. 2567"),
    CardText(75f, 259.4f, 8f, 0xFF1E2A5A, false, "วันออกบัตร"),
    CardText(75f, 270.4f, 8f, 0xFF1F6FD8, false, "28 Mar. 2024"),
    CardText(75f, 281.4f, 8f, 0xFF1F6FD8, false, "Date of Issue"),
    CardText(165f, 285.9f, 7.5f, 0xFF1E2A5A, false, "เจ้าพนักงานออกบัตร"),
    CardText(287f, 248.8f, 8.5f, 0xFF1E2A5A, true, "28 มี.ค. 2576"),
    CardText(287f, 259.4f, 8f, 0xFF1E2A5A, false, "วันหมดอายุ"),
    CardText(287f, 270.4f, 8f, 0xFF1F6FD8, false, "28 Mar. 2033"),
    CardText(287f, 281.4f, 8f, 0xFF1F6FD8, false, "Date of Expiry"),
    CardText(358f, 292.9f, 7.5f, 0xFF1E2A5A, false, "0000-00-00000000"),
)

// ---------- เรขาคณิต (สร้างครั้งเดียว) ----------
private object Geo {
    val castShade = p("M458.9 393.0 L1025.9 651.5 L1013.5 678.8 L446.5 420.3 Z")
    val panelSide = p("M466.0 377.5 L1033.0 636.0 L1025.9 651.5 L458.9 393.0 Z")
    val panel = p("M466 377.5 V0 H1033 V636 Z")
    val bevel = p("M466 377.5 L1033 636")

    val qrShadow = p("M546 310 L628 347.4 V393.3 L546 355.9 Z")
    val qrHole = p("M542.5 306 L624.5 343.4 V389.3 L542.5 351.9 Z")
    val qrTop = p("M542.5 306 L624.5 343.4 L618 346.4 L548 314.5 Z")
    val qrLeft = p("M542.5 306 L548 314.5 L548 349.4 L542.5 351.9 Z")
    val qrBottom = p("M542.5 351.9 L624.5 389.3 L618 386.3 L548 354.4 Z")
    val qrLitEdge = p("M624.5 343.4 V389.3 L542.5 351.9")
    val qrDarkEdge = p("M542.5 306 L624.5 343.4")

    val slotShadow = p("M650 356 L873 457.7 V504.1 L650 402.4 Z")
    val slot = p("M644.5 350 L867.5 451.7 V498.1 L644.5 396.4 Z")
    val lipTop = p("M644.5 350 L867.5 451.7 V469.7 L644.5 368 Z")
    val lipTopEdge = p("M644.5 350 L867.5 451.7")
    val lipBot = p("M644.5 378 L867.5 479.7 V498.1 L644.5 396.4 Z")
    val lipBotEdge = p("M644.5 378 L867.5 479.7")
    val slotEnd = p("M645 384.5 V368 L661.5 375.5 Z")
    val scan = p("M652 376.4 L860 471.2")

    /** ส่วนของบัตรที่อยู่นอกเครื่อง = ใต้เส้นกึ่งกลางร่องช่องเสียบ */
    val outsideSlot = p("M-500 -148.8 L2000 991 L2000 2400 L-500 2400 Z")

    val chipLines = p("M84 111.6h64M103.2 90v60M128.8 90v60")
    val idLine = p("M75 58.5h395")
    val cardRect = Path().apply { addRoundRect(RoundRect(0f, 0f, 480f, 303f, CornerRadius(14f))) }
    val photoBox = Path().apply { addRoundRect(RoundRect(358f, 151f, 457f, 271f, CornerRadius(6f))) }

    /** matrix(-0.26841 0.58869 -0.58557 -0.26698 844.7 464.3) — ฉายบัตรลงระนาบเครื่อง (determinant บวก ตัวหนังสือไม่กลับด้าน) */
    val cardMatrix = Matrix().apply {
        values[Matrix.ScaleX] = -0.26841f; values[Matrix.SkewY] = 0.58869f
        values[Matrix.SkewX] = -0.58557f; values[Matrix.ScaleY] = -0.26698f
        values[Matrix.TranslateX] = 844.7f; values[Matrix.TranslateY] = 464.3f
    }
}

private fun DrawScope.blurFill(path: Path, color: Color, radius: Float, key: String? = null) = drawIntoCanvas { cv ->
    val ap = path.asAndroidPath()
    if (SoftBlur.needed) {   // Android < 9: เบลอจากบิตแมปที่แคชไว้ (พิกัด path คงที่ → แคชตามตัว path)
        val b = android.graphics.RectF().also { ap.computeBounds(it, true) }
        SoftBlur.draw(cv.nativeCanvas, key ?: "pf:${System.identityHashCode(path)}:$radius", b, radius, color) { c, p -> c.drawPath(ap, p) }
        return@drawIntoCanvas
    }
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true; this.color = color.toArgb(); maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
    }
    cv.nativeCanvas.drawPath(ap, paint)
}
private fun DrawScope.blurStroke(path: Path, color: Color, width: Float, radius: Float) = drawIntoCanvas { cv ->
    val ap = path.asAndroidPath()
    fun android.graphics.Paint.stroke() = apply {
        style = android.graphics.Paint.Style.STROKE; strokeWidth = width; strokeCap = android.graphics.Paint.Cap.ROUND
    }
    if (SoftBlur.needed) {
        val b = android.graphics.RectF().also { ap.computeBounds(it, true); it.inset(-width, -width) }
        SoftBlur.draw(cv.nativeCanvas, "ps:${System.identityHashCode(path)}:$width:$radius", b, radius, color) { c, p -> c.drawPath(ap, p.stroke()) }
        return@drawIntoCanvas
    }
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true; this.color = color.toArgb(); maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
    }.stroke()
    cv.nativeCanvas.drawPath(ap, paint)
}

@Composable
fun MachineIllustration(clock: State<Float>, modifier: Modifier) {
    val tm = rememberTextMeasurer()
    val density = LocalDensity.current.density
    // ข้อความบนบัตรวัดขนาดครั้งเดียว (หน่วยพิกัดบัตร = px ก่อนถูก transform)
    val texts: List<Pair<CardText, TextLayoutResult>> = remember(tm, density) {
        CardTexts.map { t ->
            t to tm.measure(
                t.text,
                TextStyle(
                    fontFamily = NotoSansThai, fontSize = (t.size / density).sp, color = Color(t.color),
                    fontWeight = if (t.bold) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = (t.ls / density).sp,
                ),
                softWrap = false,
            )
        }
    }

    Canvas(modifier) {
        val ph = cardPhase(clock.value)
        drawMachine(c(0xFF5FB2FF), scanAlpha(ph)) {
            // บัตรประชาชน: เห็นเฉพาะส่วนนอกเครื่อง · เงาตกบนหน้าเครื่อง · ไถลตามแนวยาวของบัตร
            clipPath(Geo.outsideSlot) {
                val o = insertOffset(ph)
                translate(o.x, o.y) {
                    // feDropShadow dx 8 dy 16 std 12 #001334 .2 (เงาบาง/จางลงจากเดิม 11/24/15/.36)
                    translate(8f, 16f) {
                        withTransform({ transform(Geo.cardMatrix) }) { blurFill(Geo.cardRect, c(0xFF001334, .2f), 20f) }
                    }
                    withTransform({ transform(Geo.cardMatrix) }) { drawIdCard(texts) }
                }
            }
        }
    }
}

/**
 * ตัวเครื่อง (แผง + ช่อง QR + ช่องเสียบ) ใช้ร่วมกันระหว่างการ์ดเสียบบัตร (หน้าแรก) และการ์ดรับบัตรคิว (หน้าลงทะเบียนสำเร็จ)
 * มุม/องศา/ตำแหน่งเดียวกันทุกพิกัด · light = สีไฟที่ปากช่อง (ฟ้า = อ่านบัตร · เขียว = กำลังพิมพ์) · inside = วาดในพิกัดช่อง (บัตร/บัตรคิว)
 */
private fun DrawScope.drawMachine(light: Color, lightAlpha: Float, inside: DrawScope.() -> Unit) {
    // viewBox 800 x 612 ยืดเต็มการ์ด (preserveAspectRatio=none)
    scale(size.width / 800f, size.height / 612f, pivot = Offset.Zero) {
        translate(-48f, -100f) {
            // ---------- แผงหน้าเครื่อง ----------
            drawPath(Geo.castShade, Brush.linearGradient(
                listOf(c(0xFF14265A, .20f), c(0xFF14265A, 0f)), Offset(458.9f, 393f), Offset(446.5f, 420.3f)))
            drawPath(Geo.panelSide, Brush.horizontalGradient(
                0f to c(0xFF001E45), .5f to c(0xFF002A5E), 1f to c(0xFF001631), startX = 458.9f, endX = 1033f))
            drawPath(Geo.panel, Brush.linearGradient(
                0f to c(0xFF0B50B2), .52f to c(0xFF013F98), 1f to c(0xFF002C6B),
                start = Offset(534.04f, 0f), end = Offset(817.54f, 636f)))
            drawPath(Geo.panel, Brush.radialGradient(
                listOf(Color.White.copy(alpha = .11f), Color.White.copy(alpha = 0f)), Offset(613.4f, 63.6f), 510f))
            drawPath(Geo.bevel, Brush.horizontalGradient(
                0f to c(0xFFE7C47C, .12f), .45f to c(0xFFEBCB8B, .62f), 1f to c(0xFFE7C47C, .18f), startX = 466f, endX = 1033f),
                style = Stroke(2.4f, cap = StrokeCap.Round))

            // ---------- ช่อง + บัตร ----------
            withTransform({ translate(-12.8f, -61.9f); scale(.95f, .95f, pivot = Offset.Zero) }) {
                // ช่องสแกน QR — หลุมลึกเรียบๆ
                blurFill(Geo.qrShadow, c(0xFF000000, .26f), 3.5f)
                drawPath(Geo.qrHole, Brush.linearGradient(
                    0f to c(0xFF050B16), .55f to c(0xFF0B1A2E), 1f to c(0xFF132A45), start = Offset(542f, 306f), end = Offset(624f, 389f)))
                drawPath(Geo.qrTop, c(0xFF000000, .55f))
                drawPath(Geo.qrLeft, c(0xFF000000, .45f))
                drawPath(Geo.qrBottom, c(0xFF8FB8E8, .16f))
                drawPath(Geo.qrLitEdge, c(0xFF5C93D8, .5f), style = Stroke(2.2f))
                drawPath(Geo.qrDarkEdge, c(0xFF000000, .5f), style = Stroke(2.2f))
                drawPath(Geo.qrHole, Color.Black, style = Stroke(1f))

                // ช่องเสียบบัตร — ร่องลึกมีขอบเฉียงรับแสง
                blurFill(Geo.slotShadow, c(0xFF000000, .28f), 3.5f)
                drawPath(Geo.slot, Brush.verticalGradient(
                    0f to c(0xFF000000, .95f), .55f to c(0xFF0A0F18, .85f), 1f to c(0xFF16202E, .55f), startY = 352f, endY = 480f))
                drawPath(Geo.lipTop, Brush.verticalGradient(
                    0f to c(0xFF4A525E), .25f to c(0xFF2A2F38), 1f to c(0xFF15181E), startY = 349f, endY = 420f))
                drawPath(Geo.lipTopEdge, c(0xFF7B838F, .7f), style = Stroke(2.2f))
                drawPath(Geo.lipBot, Brush.verticalGradient(
                    0f to c(0xFF333A45), .4f to c(0xFF20242C), 1f to c(0xFF0E1116), startY = 428f, endY = 500f))
                drawPath(Geo.lipBotEdge, c(0xFF5E6774, .75f), style = Stroke(1.8f))
                drawPath(Geo.slotEnd, Color.Black)
                val sa = lightAlpha
                if (sa > 0f) {
                    blurStroke(Geo.scan, light.copy(alpha = sa), 3.5f, 3.5f)
                    drawPath(Geo.scan, light.copy(alpha = sa * .7f), style = Stroke(2f))
                }
                drawPath(Geo.slot, Color.Black, style = Stroke(1f))
                inside()
            }
        }
    }
}

private fun DrawScope.drawIdCard(texts: List<Pair<CardText, TextLayoutResult>>) {
    val ph = c(0xFFC9DCF0); val phLine = c(0xFF8FB4DC)
    drawPath(Geo.cardRect, Brush.verticalGradient(0f to c(0xFFD3E5F5), .45f to c(0xFFDFECF8), 1f to c(0xFFE8F2FB), startY = 0f, endY = 303f))
    drawPath(Geo.cardRect, c(0xFFB9D2EC), style = Stroke(2f))
    drawRoundRect(Color.White.copy(alpha = .7f), Offset(1.5f, 1.5f), Size(477f, 300f), CornerRadius(12.5f), style = Stroke(1.5f))

    fun phCircle(cx: Float, cy: Float, r: Float) { drawCircle(ph, r, Offset(cx, cy)); drawCircle(phLine, r, Offset(cx, cy), style = Stroke(1.5f)) }
    fun phRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
        drawRoundRect(ph, Offset(x, y), Size(w, h), CornerRadius(r)); drawRoundRect(phLine, Offset(x, y), Size(w, h), CornerRadius(r), style = Stroke(1.5f))
    }
    phCircle(47f, 34f, 27.25f)
    phRect(21.75f, 88.75f, 18.5f, 174.5f, 8f)
    phCircle(195f, 253f, 16.25f)
    phRect(216.75f, 250.75f, 38.5f, 6.5f, 3.25f)

    // กรอบรูป + ภาพคนแบบ wireframe (ตัดส่วนเกินในกรอบ)
    drawRoundRect(Color.White, Offset(358f, 151f), Size(99f, 120f), CornerRadius(6f))
    clipPath(Geo.photoBox) {
        drawOval(ph, Offset(407.5f - 16.8f, 187f - 16.8f), Size(33.6f, 33.6f))
        drawOval(phLine, Offset(407.5f - 16.8f, 187f - 16.8f), Size(33.6f, 33.6f), style = Stroke(1.5f))
        drawOval(ph, Offset(407.5f - 37.6f, 255.4f - 42f), Size(75.2f, 84f))
        drawOval(phLine, Offset(407.5f - 37.6f, 255.4f - 42f), Size(75.2f, 84f), style = Stroke(1.5f))
    }
    drawRoundRect(phLine, Offset(358f, 151f), Size(99f, 120f), CornerRadius(6f), style = Stroke(1.5f))

    // ชิปทอง (ด้านที่เข้าช่องก่อน)
    drawRoundRect(Brush.linearGradient(0f to c(0xFFF8D67A), .6f to c(0xFFF2C14E), 1f to c(0xFFD9A63A),
        start = Offset(84.6f, 90.6f), end = Offset(147.4f, 149.4f)), Offset(84.6f, 90.6f), Size(62.8f, 58.8f), CornerRadius(9f))
    drawRoundRect(c(0xFF8A6A20), Offset(84.6f, 90.6f), Size(62.8f, 58.8f), CornerRadius(9f), style = Stroke(1.2f))
    drawPath(Geo.chipLines, c(0xFF8A6A20), style = Stroke(1.2f))
    drawPath(Geo.idLine, c(0xFF7FB2E5), style = Stroke(1.5f))

    texts.forEach { (t, layout) -> drawText(layout, topLeft = Offset(t.x, t.y - layout.firstBaseline)) }
}


/* ================= บัตรคิวพิมพ์ออกจากช่องเดิม (หน้าลงทะเบียนสำเร็จ) — ตรงกับ services.html slipSvg ================= */

/** ข้อมูลบนบัตรคิว · TODO(integration): รับจาก HIS */
data class SlipData(
    val queue: String, val qn: String, val printed: String,
    val rows: List<Pair<String, String>>,          // ป้าย/ค่า ข้อมูลผู้ป่วย (แถว "แพ้ยา" เป็นสีส้ม)
    val room: String?,                              // จุดบริการที่ติ๊กให้ (null = ไม่ติ๊ก)
    val labToday: Boolean,                          // ติ๊ก "Lab/X-Ray วันนี้"
    val status: String,
    val authen: String? = null,                    // Authen Code สปสช. (null = ไม่มี)
)

/** จุดบริการบนสลิป (ชุดเดียวกับใบจริง) */
val SlipRooms = listOf(listOf("ห้องตรวจ", "ห้องฉีดยาทำแผล", "ห้องฉุกเฉิน", "ห้องยา", "ห้องการเงิน"),
    listOf("ทันตกรรม", "แผนไทย/ฝังเข็ม", "กายภาพบำบัด", "จิตเวช", "ศัลยกรรม"))
private const val SlipL = 940f

/** ช่องพิมพ์บัตรคิว — แยกจากช่องเสียบบัตร อยู่ใต้ช่องเสียบบนแผงเดียวกัน (ขนานกัน) · พิกัดเดียวกับ services.html */
private object PrinterGeo {
    fun p(d: String): Path = PathParser().parsePathString(d).toPath()
    val shadow = p("M654 442 L874 542.3 V562 L654 461.7 Z")
    val body = p("M650 436 L870 536.3 V556 L650 455.7 Z")
    val lipTop = p("M650 436 L870 536.3 V543 L650 442.7 Z")
    val lipTopEdge = p("M650 436 L870 536.3")
    val lipBot = p("M650 449.5 L870 549.8 V556 L650 455.7 Z")
    val lipBotEdge = p("M650 449.5 L870 549.8")
    val light = p("M656 448.7 L864 543.5")
    /** ส่วนที่อยู่นอกเครื่อง = ใต้เส้นปากช่องพิมพ์ */
    val outside = p("M-500 -78.4 L2000 1061.6 L2000 2400 L-500 2400 Z")
    /** แกนบัตรคิว: ทิศเดียวกับบัตรประชาชน แต่ต้นแกนอยู่ที่ปากช่องพิมพ์ */
    val slipMatrix = Matrix().apply {
        values[Matrix.ScaleX] = -0.26841f; values[Matrix.SkewY] = 0.58869f
        values[Matrix.SkewX] = -0.58557f; values[Matrix.ScaleY] = -0.26698f
        // ต้นแกนบนเส้นกลางปากช่องพิมพ์ → ใบกว้าง 300 อยู่กึ่งกลางช่องพอดี
        values[Matrix.TranslateX] = 847.9f; values[Matrix.TranslateY] = 536.3f
    }
}

/** ลาย QR จำลอง 25x25 (สูตรสุ่มเดียวกับ HTML) — จุดมุมวาดแยก */
fun fakeQrCells(seed: Int = 14): List<Pair<Int, Int>> {
    var r = seed.toLong() * 9301 + 49297
    fun rnd(): Double { r = (r * 9301 + 49297) % 233280; return r / 233280.0 }
    fun finder(x: Int, y: Int) = (x < 7 && y < 7) || (x > 17 && y < 7) || (x < 7 && y > 17)
    return buildList { for (y in 0 until 25) for (x in 0 until 25) if (!finder(x, y) && rnd() > .52) add(x to y) }
}

private val PrintEase = CubicBezierEasing(.3f, .1f, .3f, 1f)

/**
 * ตัวเครื่องเดียวกับหน้าแรก + บัตรคิวพิมพ์ออกจากช่องพิมพ์แยก (ใต้ช่องเสียบบัตร) ในแนวเดียวกับบัตรประชาชน (สูตรเดียวกับ services.html driveSlip)
 * sec = เวลาในรอบ 0–7.5 วิ · 0.3–3.9 พิมพ์ออกจนสุดใบ (ไฟเขียว) · 3.9–5.6 ค้าง · 5.6–6.7 หลุดเลื่อนออกนอกการ์ด · แล้วพิมพ์ใหม่
 * กระดาษนิ่ม: ส่วนที่ออกมาแล้วโค้งตามน้ำหนัก (ความโค้ง κ คงที่ตามความยาว φ = κd) · แบ่ง 24 แถบ วางแบบแข็งตามมุมต้นแถบ ซ้อนขอบ 4 หน่วย
 * เนื้อหาวาดตั้งตรง (u ขวาง, v = ระยะจากปลายในสุด) → กรอบ F (u', v' = ระยะออกจากปากช่อง) → แกนบัตร x = v', y = 300 − u'
 */
@Composable
fun PrinterIllustration(sec: State<Float>, slip: SlipData, modifier: Modifier) {
    val tm = rememberTextMeasurer()
    val density = LocalDensity.current.density
    fun m(t: String, size: Float, color: Long, bold: Boolean, ls: Float = 0f) = tm.measure(t,
        TextStyle(fontFamily = NotoSansThai, fontSize = (size / density).sp, color = Color(color),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, letterSpacing = (ls / density).sp), softWrap = false)
    // ข้อความทั้งใบวัดครั้งเดียว: (layout, x, baseline, จัดกลาง/ชิดขวา)
    class Txt(val l: TextLayoutResult, val x: Float, val y: Float, val align: Int)   // 0 ซ้าย · 1 กลาง · 2 ขวา
    val ink = 0xFF14265A; val muted = 0xFF5B6B8A; val soft = 0xFF8494AE; val amber = 0xFFB45309
    val texts = remember(tm, density, slip) {
        val out = mutableListOf<Txt>()
        fun t(x: Float, y: Float, size: Float, str: String, color: Long = ink, bold: Boolean = false, align: Int = 0, ls: Float = 0f) =
            out.add(Txt(m(str, size, color, bold, ls), x, y, align))
        t(18f, 32f, 14.5f, "ใบนำทางผู้ป่วยนอก", bold = true); t(18f, 50f, 11.5f, "โรงพยาบาล BMS", muted)
        t(282f, 32f, 9.5f, "วันที่พิมพ์", soft, align = 2); t(282f, 49f, 10.5f, slip.printed, bold = true, align = 2)
        t(83f, 88f, 11.5f, "คิวเรียกคนไข้", muted, align = 1); t(83f, 148f, 50f, slip.queue, 0xFF0D5BC6, true, 1, 1f)
        t(217f, 88f, 11.5f, "QN", muted, align = 1); t(217f, 148f, 50f, slip.qn, bold = true, align = 1)
        slip.rows.forEachIndexed { i, (k, v) -> t(18f, 200f + i * 23, 10.5f, k, soft); t(98f, 200f + i * 23, 11.5f, v, if (k == "แพ้ยา") amber else ink, true) }
        t(18f, 378f, 10.5f, "สพ.หลัก", soft); t(18f, 395f, 11.5f, "—", bold = true)
        t(18f, 420f, 10.5f, "Authen Code", soft); t(18f, 437f, 11.5f, slip.authen ?: "—", bold = true)
        t(18f, 490f, 10.5f, "จุดบริการ", soft, true, ls = .5f)
        SlipRooms.forEachIndexed { c, col -> col.forEachIndexed { i, r -> t(35f + c * 142, 512f + i * 23, 11.5f, r, if (r == slip.room) ink else muted, r == slip.room) } }
        t(18f, 652f, 10.5f, "สำหรับเจ้าหน้าที่", soft, true, ls = .5f)
        t(18f, 676f, 10.5f, "CC", muted); t(18f, 700f, 10.5f, "HPI", muted)
        t(35f, 752f, 11f, "มียา", muted); t(117f, 752f, 11f, "ไม่มียา", muted); t(207f, 752f, 11f, "ชำระเงิน", muted)
        t(35f, 776f, 11f, "Admit", muted); t(35f, 800f, 11f, "Lab/X-Ray วันนี้", if (slip.labToday) ink else muted, slip.labToday)
        t(35f, 838f, 11f, "นัด", muted); t(156f, 838f, 11f, "วัน/วันที่", muted)
        t(35f, 862f, 11f, "นัด Lab", muted); t(177f, 862f, 11f, "นัด X-ray", muted)
        t(150f, 900f, 11.5f, slip.status, if (slip.authen != null) 0xFF0F6B51 else amber, true, 1)
        out
    }
    // ช่องติ๊ก (x, baseline, ติ๊กไหม)
    val boxes = remember(slip) {
        buildList {
            SlipRooms.forEachIndexed { c, col -> col.forEachIndexed { i, r -> add(Triple(18f + c * 142, 512f + i * 23, r == slip.room)) } }
            add(Triple(18f, 752f, false)); add(Triple(100f, 752f, false)); add(Triple(190f, 752f, false))
            add(Triple(18f, 776f, false)); add(Triple(18f, 800f, slip.labToday))
            add(Triple(18f, 838f, false)); add(Triple(18f, 862f, false)); add(Triple(160f, 862f, false))
        }
    }
    // QR เป็น path เดียว (วาดเร็ว ต้องวาดซ้ำทุกแถบทุกเฟรม)
    val qrPath = remember {
        Path().apply {
            val k = 3.4f
            fakeQrCells().forEach { (x, y) -> addRect(androidx.compose.ui.geometry.Rect(198f + x * k, 366f + y * k, 198f + (x + 1) * k, 366f + (y + 1) * k)) }
        }
    }
    val slipPath = remember {
        Path().apply { moveTo(0f, 0f); for (i in 0..20) lineTo(i * 15f, if (i % 2 == 1) SlipL - 10f else SlipL); lineTo(300f, 0f); close() }
    }
    val rot = remember { Matrix().apply {
        values[Matrix.ScaleX] = 0f; values[Matrix.SkewY] = -1f; values[Matrix.SkewX] = 1f; values[Matrix.ScaleY] = 0f
        values[Matrix.TranslateY] = 300f } }
    val stripM = remember { Matrix() }

    fun DrawScope.content() {
        val inkC = c(ink); val dotC = c(0xFFAEB9CC)
        val dotFx = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(1.5f, 3f))
        val dashFx = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
        fun dots(x1: Float, x2: Float, y: Float) = drawLine(dotC, Offset(x1, y), Offset(x2, y), 1f, pathEffect = dotFx)
        fun dash(y: Float) = drawLine(c(0xFFC9D2E0), Offset(16f, y), Offset(284f, y), 1.5f, pathEffect = dashFx)
        drawPath(slipPath, Color.White)
        drawRect(Brush.verticalGradient(0f to c(0xFF14265A, .10f), 1f to c(0xFF14265A, 0f), startY = 0f, endY = 60f), Offset.Zero, Size(300f, 60f))
        drawPath(slipPath, c(0xFFD5DCE8), style = Stroke(1.5f))
        // กล่องคิวใหญ่เต็มกว้าง
        drawRoundRect(inkC, Offset(16f, 64f), Size(268f, 104f), CornerRadius(8f), style = Stroke(2.5f))
        drawLine(c(ink, .25f), Offset(150f, 72f), Offset(150f, 160f), 1.2f)
        dash(354f); dash(468f); dash(630f)
        dots(42f, 282f, 677f); dots(42f, 282f, 701f); dots(18f, 282f, 724f)
        dots(76f, 282f, 777f); dots(128f, 282f, 801f)
        drawLine(c(ink, .2f), Offset(16f, 814f), Offset(284f, 814f), 1f)
        dots(58f, 150f, 839f); dots(208f, 282f, 839f)
        drawRoundRect(if (slip.authen != null) c(0xFFE8F7F2) else c(0xFFFEF5E7), Offset(16f, 880f), Size(268f, 30f), CornerRadius(6f))
        // QR
        drawPath(qrPath, inkC)
        val k = 3.4f
        listOf(0 to 0, 18 to 0, 0 to 18).forEach { (fx, fy) ->
            drawRect(inkC, Offset(198f + fx * k, 366f + fy * k), Size(7 * k, 7 * k))
            drawRect(Color.White, Offset(198f + (fx + 1) * k, 366f + (fy + 1) * k), Size(5 * k, 5 * k))
            drawRect(inkC, Offset(198f + (fx + 2) * k, 366f + (fy + 2) * k), Size(3 * k, 3 * k))
        }
        // ช่องติ๊ก
        boxes.forEach { (x, y, on) ->
            if (on) {
                drawRoundRect(inkC, Offset(x, y - 10f), Size(11f, 11f), CornerRadius(2f))
                drawPath(Path().apply { moveTo(x + 2.5f, y - 4.5f); relativeLineTo(2.6f, 2.6f); relativeLineTo(4.2f, -5f) }, Color.White,
                    style = Stroke(1.6f, cap = StrokeCap.Round))
            } else drawRoundRect(inkC, Offset(x, y - 10f), Size(11f, 11f), CornerRadius(2f), style = Stroke(1.3f))
        }
        texts.forEach { t ->
            val x = when (t.align) { 1 -> t.x - t.l.size.width / 2f; 2 -> t.x - t.l.size.width; else -> t.x }
            drawText(t.l, topLeft = Offset(x, t.y - t.l.firstBaseline))
        }
    }

    // เนื้อหาบัตรคิวเรนเดอร์ลงภาพครั้งเดียว (ละเอียด 2 เท่า) แล้ววาดภาพนั้นทุกแถบทุกเฟรม — เบาพอสำหรับเครื่องตู้สเปกต่ำ
    val slipImg = remember(slip) { arrayOfNulls<androidx.compose.ui.graphics.ImageBitmap>(1) }
    Canvas(modifier) {
        val img = slipImg[0] ?: androidx.compose.ui.graphics.ImageBitmap(600, (SlipL * 2).toInt()).also { bmp ->
            androidx.compose.ui.graphics.drawscope.CanvasDrawScope().draw(this, layoutDirection,
                androidx.compose.ui.graphics.Canvas(bmp), Size(600f, SlipL * 2)) { scale(2f, 2f, pivot = Offset.Zero) { content() } }
            slipImg[0] = bmp
        }
        val pose = slipPose(sec.value % SlipCycle)
        drawMachine(Color.Transparent, 0f) {   // ช่องเสียบบัตรไม่มีไฟ — บัตรคิวออกจากช่องพิมพ์แยก
            // ---------- ช่องพิมพ์ ----------
            blurFill(PrinterGeo.shadow, c(0xFF000000, .26f), 3.5f)
            drawPath(PrinterGeo.body, c(0xFF0A0F18))
            drawPath(PrinterGeo.lipTop, Brush.verticalGradient(0f to c(0xFF4A525E), .25f to c(0xFF2A2F38), 1f to c(0xFF15181E), startY = 436f, endY = 500f))
            drawPath(PrinterGeo.lipTopEdge, c(0xFF7B838F, .7f), style = Stroke(2f))
            drawPath(PrinterGeo.lipBot, Brush.verticalGradient(0f to c(0xFF333A45), .4f to c(0xFF20242C), 1f to c(0xFF0E1116), startY = 449f, endY = 556f))
            drawPath(PrinterGeo.lipBotEdge, c(0xFF5E6774, .75f), style = Stroke(1.6f))
            if (pose.light > 0f) {
                blurStroke(PrinterGeo.light, c(0xFF2BD98F, pose.light), 3.5f, 3.5f)
                drawPath(PrinterGeo.light, c(0xFF2BD98F, pose.light * .7f), style = Stroke(2f))
            }
            drawPath(PrinterGeo.body, Color.Black, style = Stroke(1f))
            if (pose.s <= .5f) return@drawMachine
            val n = 24; val L = SlipL
            val va = L - pose.s; val h = pose.s / n
            fun X(d: Float) = if (kotlin.math.abs(pose.k) < 1e-7f) 0f else (1f - kotlin.math.cos(pose.k * d)) / pose.k
            fun Y(d: Float) = if (kotlin.math.abs(pose.k) < 1e-7f) d else kotlin.math.sin(pose.k * d) / pose.k
            fun setStrip(j: Int): Float {
                val a = va + j * h; val dA = j * h; val phi = pose.k * dA
                val cs = kotlin.math.cos(phi); val sn = kotlin.math.sin(phi)
                stripM.reset()
                stripM.values[Matrix.ScaleX] = cs; stripM.values[Matrix.SkewY] = -sn
                stripM.values[Matrix.SkewX] = sn; stripM.values[Matrix.ScaleY] = cs
                stripM.values[Matrix.TranslateX] = 150f + X(dA) - (cs * 150f + sn * a)
                stripM.values[Matrix.TranslateY] = Y(dA) - (-sn * 150f + cs * a)
                return phi
            }
            val endPhi = pose.k * pose.s
            val ex = pose.exit * kotlin.math.cos(endPhi); val ey = -pose.exit * kotlin.math.sin(endPhi) * .6f
            clipPath(PrinterGeo.outside) {
                // เงาตกบนหน้าเครื่อง (dx 8 dy 16) — วาดเป็นก้อนละ 4 แถบ ลดงานเบลอ
                translate(8f, 16f) {
                    for (j in 0 until n step 4) {
                        setStrip(j)
                        withTransform({ transform(PrinterGeo.slipMatrix); translate(ex, ey); transform(rot); transform(stripM) }) {
                            val a = va + j * h
                            blurStripShadow(a, h * 4)
                        }
                    }
                }
                for (j in 0 until n) {
                    val phi = setStrip(j)
                    val a = va + j * h
                    withTransform({ transform(PrinterGeo.slipMatrix); translate(ex, ey); transform(rot); transform(stripM) }) {
                        clipRect(-4f, a - 2f, 304f, a + h + 2f) {
                            drawImage(img, dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                                dstSize = androidx.compose.ui.unit.IntSize(300, SlipL.toInt()))
                            drawRect(c(0xFF14265A, kotlin.math.min(.07f, kotlin.math.abs(phi) * .22f)), Offset(0f, a - 2f), Size(300f, h + 4f))
                        }
                    }
                }
            }
        }
    }
}

const val SlipCycle = 7.5f
private class SlipPose(val s: Float, val k: Float, val exit: Float, val light: Float)

/** ท่าของบัตรคิว ณ เวลา t ในรอบ — สูตรเดียวกับ slipPose ใน services.html */
private fun slipPose(t: Float): SlipPose {
    fun ez(x: Float) = x.coerceIn(0f, 1f).let { it * it * (3 - 2 * it) }
    val pr = ((t - .3f) / 3.6f).coerceIn(0f, 1f)
    val s = SlipL * (pr * .85f + ez(pr) * .15f)
    val sway = if (t < 4.1f) kotlin.math.sin(t * 7f) * .00009f * kotlin.math.min(1f, s / 120f) else 0f
    val k = .00056f * ez(s / SlipL) + sway   // กระดาษยาว 940 → ความโค้งรวมราว 30°
    val exN = kotlin.math.max(0f, (t - 5.6f) / 1.1f)
    val light = when { t < .3f -> 0f; t < .5f -> (t - .3f) / .2f * .9f; t < 3.8f -> .9f; t < 4.2f -> .9f * (1f - (t - 3.8f) / .4f); else -> 0f }
    return SlipPose(s, k, 1500f * exN * exN, light)
}

/** เงาแถบบัตรคิว (ทุกเฟรม) — Android 9+: BlurMaskFilter ตรงๆ · Android < 9: บิตแมปเบลอสำเร็จรูปยืดตามความสูงแถบ */
private val stripRect = Path().apply { addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 300f, 160f)) }
private fun DrawScope.blurStripShadow(top: Float, height: Float) {
    if (height <= 0f) return
    if (SoftBlur.needed) scale(1f, height / 160f, pivot = Offset(0f, 0f)) {
        translate(0f, top * 160f / height) { blurFill(stripRect, c(0xFF001334, .2f), 20f, "strip") }
    } else blurFill(Path().apply { addRect(androidx.compose.ui.geometry.Rect(0f, top, 300f, top + height)) }, c(0xFF001334, .2f), 20f)
}
