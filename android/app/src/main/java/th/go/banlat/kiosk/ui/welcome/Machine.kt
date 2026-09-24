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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
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

private fun DrawScope.blurFill(path: Path, color: Color, radius: Float) = drawIntoCanvas { cv ->
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true; this.color = color.toArgb(); maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
    }
    cv.nativeCanvas.drawPath(path.asAndroidPath(), paint)
}

private fun DrawScope.blurStroke(path: Path, color: Color, width: Float, radius: Float) = drawIntoCanvas { cv ->
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true; this.color = color.toArgb(); style = android.graphics.Paint.Style.STROKE
        strokeWidth = width; strokeCap = android.graphics.Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
    }
    cv.nativeCanvas.drawPath(path.asAndroidPath(), paint)
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
                    val sa = scanAlpha(ph)
                    if (sa > 0f) {
                        blurStroke(Geo.scan, c(0xFF5FB2FF, sa), 3.5f, 3.5f)
                        drawPath(Geo.scan, c(0xFF5FB2FF, sa * .7f), style = Stroke(2f))
                    }
                    drawPath(Geo.slot, Color.Black, style = Stroke(1f))

                    // บัตรประชาชน: เห็นเฉพาะส่วนนอกเครื่อง · เงาตกบนหน้าเครื่อง · ไถลตามแนวยาวของบัตร
                    clipPath(Geo.outsideSlot) {
                        val o = insertOffset(ph)
                        translate(o.x, o.y) {
                            // feDropShadow dx 11 dy 24 std 15 #001334 .36
                            translate(11f, 24f) {
                                withTransform({ transform(Geo.cardMatrix) }) { blurFill(Geo.cardRect, c(0xFF001334, .36f), 25f) }
                            }
                            withTransform({ transform(Geo.cardMatrix) }) { drawIdCard(texts) }
                        }
                    }
                }
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

