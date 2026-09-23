package th.go.banlat.kiosk.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.ui.theme.Bright
import th.go.banlat.kiosk.ui.theme.IdCardColors
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.st

enum class KioskLanguage { Thai, English }

/**
 * หน้าต้อนรับ — หน้าแรกสุดที่คนไข้เห็นตอนเดินมาถึงตู้
 *
 * ทางเข้าระบบมี 3 ทาง เรียงตามลำดับความสำคัญชัดเจน
 *   1. เสียบบัตรประชาชน  — ทางหลัก ใช้อนิเมชัน 3D สอนวิธีใช้โดยไม่ต้องอ่าน
 *   2. กรอกเลข HN      — สำรอง สำหรับคนไม่ได้พกบัตร
 *   3. สแกนใบหน้า      — สำหรับคนที่ลงทะเบียนใบหน้าไว้แล้ว
 */
@Composable
fun WelcomeScreen(
    language: KioskLanguage,
    onLanguageChange: (KioskLanguage) -> Unit,
    onEnterHn: () -> Unit,
    onFaceScan: () -> Unit,
) {
    val str = WelcomeStrings.of(language)

    Box(Modifier.fillMaxSize()) {
        Backdrop()
        Column(Modifier.fillMaxSize()) {

            WelcomeHeader(str)

            Headline(str)

            // ---- ฉาก 3D: บัตร + ช่องอ่านบัตร ----
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                CardReaderScene(str)
            }

            Spacer(Modifier.weight(1f))
        }

        // ป้าย + ปุ่มภาพ 2 ปุ่ม
        AltLabel(str, Modifier.align(Alignment.TopCenter).offset(y = 1090.s))
        ImageButtons(str, onEnterHn = onEnterHn, onFaceScan = onFaceScan)

        // ปุ่มภาษา กึ่งกลางล่าง
        LanguageSwitch(str, onLanguageChange, Modifier.align(Alignment.BottomCenter).padding(bottom = 34.s))
    }
}

/* ============================ พื้นหลัง ============================ */

/**
 * ภาพพื้นหลัง bg_home.png (1080 x 1920) เป็นแค่ฉากหลัง ปุ่มวางทับด้วย ImageButtons แยกต่างหาก
 */
@Composable
private fun Backdrop() {
    Image(
        painter = painterResource(R.drawable.bg_home),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

/* ============================ Header ============================ */

@Composable
private fun WelcomeHeader(str: WelcomeStrings) {
    Row(
        Modifier.fillMaxWidth().padding(start = 60.s, top = 28.s, end = 60.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(26.s)
    ) {
        // ตราโรงพยาบาล — ใส่ไฟล์โลโก้จริงแทน Box นี้ได้เลย
        Box(
            Modifier
                .size(104.s)
                .clip(CircleShape)
                .background(Bright.Glass)
                .border(1.5.dp, Bright.GlassBorder, CircleShape)
        )
        Column {
            Text(
                str.hospital,
                fontSize = 46.st, fontWeight = FontWeight.Bold, color = Bright.Ink
            )
            Text(
                str.hospitalSub,
                fontSize = 24.st, color = Bright.Blue, fontWeight = FontWeight.SemiBold, letterSpacing = 5.st
            )
        }
    }
}

/**
 * ปุ่มเปลี่ยนภาษา = ภาพ 2 สถานะจากดีไซเนอร์ (lang_th.png / lang_en.png ขนาด 576 x 149)
 * ครึ่งซ้ายของภาพคือ EN ครึ่งขวาคือ TH เหมือนกันทั้งสองไฟล์ จึงวางพื้นที่กดทับได้เลย
 */
@Composable
private fun LanguageSwitch(str: WelcomeStrings, onChange: (KioskLanguage) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.width(360.s).height(93.s)) {
        Image(
            painter = painterResource(str.langRes),
            contentDescription = "Language",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Row(Modifier.fillMaxSize()) {
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onChange(KioskLanguage.English)
                    }
            )
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onChange(KioskLanguage.Thai)
                    }
            )
        }
    }
}

/* ============================ Headline ============================ */

@Composable
private fun Headline(str: WelcomeStrings) {
    Column(
        Modifier.fillMaxWidth().padding(start = 60.s, top = 28.s, end = 60.s),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            str.titleLead + str.titleAccent,
            color = Bright.Ink,
            fontSize = str.titleSize.st,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, maxLines = 1, softWrap = false
        )
        Spacer(Modifier.height(14.s))
        Text(
            buildAnnotatedString {
                append(str.subtitleLead)
                withStyle(SpanStyle(color = Bright.Gold, fontWeight = FontWeight.Bold)) { append(str.subtitleAccent) }
                append(str.subtitleTail)
            },
            fontSize = str.subtitleSize.st, color = Bright.InkMuted, textAlign = TextAlign.Center, maxLines = 1, softWrap = false
        )
    }
}

/* ============================ ฉาก 3D ============================ */

/**
 * พิกัดทั้งหมดเป็น "หน่วยดีไซน์" บน rig 760 x 700 ตรงกับ preview/welcome.html
 * แก้ที่ไหนต้องแก้ทั้งสองที่
 *
 * โครงเครื่องอ่านบัตร (มองจากด้านหน้าเฉียงบน):
 *   ฝาบน (lid)        ระนาบนอนราบ ถอยลึกเข้าไป 260
 *   หน้าตัด (face)     y 120..320  ปากช่องอยู่ที่ y 260 = ใต้ "ฝาครอบ" 140 หน่วย
 *   ขอบรับบัตร (lip)   y 260..320  บัตรวางบนนี้ก่อนไถลเข้าใต้ฝาครอบ
 *
 * ลำดับการซ้อน (zIndex) คงที่ตลอด เพราะบัตรไม่เคยอยู่เหนือกล่อง:
 *   1  ขอบรับบัตร + ลูกศรบนพื้น        (อยู่หลังบัตรเสมอ)
 *   2  บัตร
 *   3  ฝาบน + ฝาครอบ + ป้าย            (บังส่วนของบัตรที่เข้าไปแล้ว)
 */
private object Scene {
    const val FACE_X = 70f; const val FACE_Y = 120f
    const val FACE_W = 620f; const val FACE_H = 200f
    const val SLOT_Y = 260f                       // ระดับปากช่อง = ระนาบที่บัตรไถล
    const val SLOT_W = 344f                       // กว้างเท่าด้านสั้นของบัตร + เผื่อ
    const val LID_DEPTH = 260f
    const val CARD_W = 480f; const val CARD_H = 303f
    const val CARD_CX = Rig.WIDTH / 2f; const val CARD_CY = SLOT_Y
    val SPLIT = (SLOT_Y - FACE_Y) / FACE_H        // จุดผ่าหน้าตัดเป็นฝาครอบ/ขอบรับ
}

@Composable
private fun CardReaderScene(str: WelcomeStrings) {
    val pose = rememberInsertPose()
    val sheen = rememberSheenProgress()
    val slotGlow = rememberSlotGlow()
    val shadow = rememberShadowPose()
    val dropAlpha = rememberDropShadowAlpha()

    Box(
        Modifier
            .width(Rig.WIDTH.toInt().s)
            .height(Rig.HEIGHT.toInt().s)
            .clipToBounds()
            .bottomFade(.90f)   // กันขอบล่างตัดขาดถ้าบัตรยื่นเลยฉาก
    ) {

        // ---- ชั้นหลัง ----
        ReaderFace(glow = slotGlow, part = FacePart.Lip, modifier = Modifier.zIndex(1f))
        FloorArrows(x = 120f, modifier = Modifier.zIndex(1f))
        FloorArrows(x = Rig.WIDTH - 120f - 60f, modifier = Modifier.zIndex(1f))

        CardShadow(shadow, Modifier.zIndex(1f))

        // ---- บัตร ----
        IdCard(pose = pose, sheen = sheen, dropAlpha = dropAlpha, modifier = Modifier.zIndex(2f))

        // ---- ชั้นหน้า ----
        ReaderLid(Modifier.zIndex(3f))
        ReaderFace(glow = slotGlow, part = FacePart.Hood, modifier = Modifier.zIndex(3f))
    }
}

/** ทำให้ส่วนล่างของ composable ค่อยๆ โปร่งใส (เทียบเท่า mask-image ใน CSS) */
private fun Modifier.bottomFade(from: Float): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(from to Color.Black, 1f to Color.Transparent),
            blendMode = BlendMode.DstIn
        )
    }

private enum class FacePart { Hood, Lip }

/** หน้าตัดของเครื่อง วาดสองครั้ง (ฝาครอบ/ขอบรับ) เพื่อเสียบบัตรไว้ตรงกลาง */
@Composable
private fun ReaderFace(glow: Float, part: FacePart, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(bottomStart = 26.s, bottomEnd = 26.s)
    Box(
        modifier
            .offset(x = Scene.FACE_X.toInt().s, y = Scene.FACE_Y.toInt().s)
            .width(Scene.FACE_W.toInt().s)
            .height(Scene.FACE_H.toInt().s)
            .drawOnly(Scene.SPLIT, upperPart = part == FacePart.Hood)
            .clip(shape)
            .background(Bright.ReaderFace)
            .border(1.dp, Bright.Line, shape)
            .drawBehind {
                // ปากช่อง คร่อมเส้นผ่าพอดี ครึ่งบนอยู่บนฝาครอบ ครึ่งล่างอยู่บนขอบรับ
                val cy = size.height * Scene.SPLIT
                val w = size.width * (Scene.SLOT_W / Scene.FACE_W)
                val h = size.height * (16f / Scene.FACE_H)
                drawRoundRect(
                    brush = Bright.Slot,
                    topLeft = Offset((size.width - w) / 2f, cy - h / 2f),
                    size = androidx.compose.ui.geometry.Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2f)
                )
                // ไฟทองในช่อง
                val gw = w * .84f * (.5f + .5f * glow)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Bright.BlueLight, Color.Transparent)
                    ),
                    topLeft = Offset((size.width - gw) / 2f, cy - 1f),
                    size = androidx.compose.ui.geometry.Size(gw, 2f * density),
                    alpha = glow
                )
            }
    )
}

/** ฝาบนของเครื่อง: ระนาบนอนราบถอยลึกจากขอบบนของหน้าตัด */
@Composable
private fun ReaderLid(modifier: Modifier = Modifier) {
    val origin = Offset(Scene.FACE_X, Scene.FACE_Y - Scene.LID_DEPTH)
    Box(
        modifier
            .offset(x = origin.x.toInt().s, y = origin.y.toInt().s)
            .width(Scene.FACE_W.toInt().s)
            .height(Scene.LID_DEPTH.toInt().s)
            .projectQuad(origin) {
                floorQuad(Scene.FACE_X, Scene.FACE_Y, Scene.FACE_W, zTop = -Scene.LID_DEPTH, zBottom = 0f)
            }
            .clip(RoundedCornerShape(topStart = 22.s, topEnd = 22.s))
            .background(Bright.ReaderLid)
            .border(1.dp, Bright.Line, RoundedCornerShape(topStart = 22.s, topEnd = 22.s))
    )
}

/** ลูกศรบนพื้น ชี้เข้าหาช่อง วางบนระนาบเดียวกับบัตร */
@Composable
private fun FloorArrows(x: Float, modifier: Modifier = Modifier) {
    val origin = Offset(x, Scene.SLOT_Y)
    Column(
        modifier
            .offset(x = x.toInt().s, y = Scene.SLOT_Y.toInt().s)
            .width(60.s).height(230.s)
            .projectQuad(origin) { floorQuad(x, Scene.SLOT_Y, 60f, zTop = 0f, zBottom = 230f) }
            .padding(bottom = 20.s),
        verticalArrangement = Arrangement.spacedBy(14.s, Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ตัวที่อยู่บนสุด (ใกล้ช่องที่สุด) วิ่งก่อน
        listOf(440, 220, 0).forEach { delay ->
            val p = rememberArrowPulse(delay)
            Box(
                Modifier
                    .size(30.s)
                    .graphicsLayer {
                        alpha = if (p < .45f) p / .45f * .95f else (1f - p) / .55f * .95f
                        translationY = (7f - p * 14f) * density
                    }
                    .drawBehind {
                        val w = 4.dp.toPx()
                        // หัวลูกศรชี้ขึ้น = ชี้เข้าหาช่อง
                        drawLine(Bright.Blue, Offset(0f, size.height / 2), Offset(size.width / 2, 0f), w)
                        drawLine(Bright.Blue, Offset(size.width / 2, 0f), Offset(size.width, size.height / 2), w)
                    }
            )
        }
    }
}

/**
 * เงาตกกระทบบนพื้น: วงรีไล่สีเบลอ ฉายลงระนาบเดียวกับที่บัตรวาง
 * (blur ต้องการ API 31+; ต่ำกว่านั้นจะเป็นวงรีคมๆ ซึ่งยังพอดูได้)
 */
@Composable
private fun CardShadow(shadow: ShadowPose, modifier: Modifier = Modifier) {
    val origin = Offset(Scene.CARD_CX - Scene.CARD_W / 2f, Scene.CARD_CY - Scene.CARD_H / 2f)
    Box(
        modifier
            .offset(x = origin.x.toInt().s, y = origin.y.toInt().s)
            .width(Scene.CARD_W.toInt().s)
            .height(Scene.CARD_H.toInt().s)
            .graphicsLayer { alpha = shadow.pose.alpha }
            .projectQuad(origin) {
                shadow.pose.projectedCorners(
                    Scene.CARD_CX, Scene.CARD_CY,
                    Scene.CARD_W * shadow.scaleX, Scene.CARD_H * shadow.scaleDepth
                )
            }
            .blur(18.dp)
            .background(
                Brush.radialGradient(
                    0f to Bright.Ink.copy(alpha = .40f), .42f to Bright.Ink.copy(alpha = .20f), .72f to Color.Transparent
                ),
                CircleShape
            )
    )
}

@Composable
private fun IdCard(pose: CardPose, sheen: Float, dropAlpha: Float, modifier: Modifier = Modifier) {
    val origin = Offset(Scene.CARD_CX - Scene.CARD_W / 2f, Scene.CARD_CY - Scene.CARD_H / 2f)
    Box(
        modifier
            .offset(x = origin.x.toInt().s, y = origin.y.toInt().s)
            .width(Scene.CARD_W.toInt().s)
            .height(Scene.CARD_H.toInt().s)
            .graphicsLayer { alpha = pose.alpha }
            .projectQuad(origin) {
                pose.projectedCorners(Scene.CARD_CX, Scene.CARD_CY, Scene.CARD_W, Scene.CARD_H)
            }
    ) {
        // เงาติดตัวบัตร: นุ่มใหญ่ชั้นเดียว (ทิศทางถูกเฉพาะตอนบัตรตั้ง จึงหายไปเมื่อบัตรนอนราบ)
        Box(
            Modifier
                .fillMaxSize()
                .offset(y = 34.s)
                .padding(horizontal = 12.s)
                .graphicsLayer { alpha = dropAlpha }
                .blur(35.dp)
                .background(Bright.Ink.copy(alpha = .34f), RoundedCornerShape(24.s))
        )
        // ---- หน้าบัตร: จัดวางตามแม่แบบบัตรประชาชนไทย (ภาพอ้างอิง 340 x 220 → บัตร 480 x 303) ----
        // พิกัดเป็นหน่วยบนบัตร 480 x 303 ตัวเลขเดียวกับ preview/welcome.html
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.s))
                .background(IdCardColors.Face)
        ) {
            Placeholder(Modifier.offset(x = 19.s, y = 6.s).size(56.s), CircleShape)          // ตราครุฑ

            CardText("บัตรประจำตัวประชาชน", 89, 14, 18, IdCardColors.Ink, FontWeight.Bold)
            CardText("Thai National ID Card", 274, 16, 15, IdCardColors.Blue, FontWeight.Bold)
            CardText("เลขประจำตัวประชาชน", 89, 37, 9, IdCardColors.Ink, FontWeight.Medium)
            CardText("Identification Number", 89, 48, 8, IdCardColors.Blue, FontWeight.Medium)
            CardText("0 0000 00000 00 0", 209, 39, 16, IdCardColors.Ink, FontWeight.Bold, letterSpacing = 1.2f)
            Box(Modifier.offset(x = 75.s, y = 58.s).size(395.s, 1.s).background(IdCardColors.Line))

            CardText("ชื่อตัวและชื่อสกุล", 75, 69, 9, IdCardColors.Ink, FontWeight.Medium)
            CardText("XXXXX XXXXXXXXX", 181, 67, 14, IdCardColors.Ink, FontWeight.Bold)

            Placeholder(Modifier.offset(x = 21.s, y = 88.s).size(20.s, 176.s), RoundedCornerShape(8.s))   // บาร์โค้ด
            GoldChip(Modifier.offset(x = 84.s, y = 90.s))

            CardText("Name", 176, 97, 9, IdCardColors.Blue, FontWeight.Medium)
            CardText("XXXXX", 219, 94, 13, IdCardColors.Ink, FontWeight.Bold)
            CardText("Last name", 176, 118, 9, IdCardColors.Blue, FontWeight.Medium)
            CardText("XXXXX", 219, 115, 13, IdCardColors.Ink, FontWeight.Bold)
            CardText("เกิดวันที่ 28 มี.ค. 2537", 195, 143, 11, IdCardColors.Ink, FontWeight.Medium)
            CardText("Date of Birth 28 Mar. 1994", 195, 162, 9, IdCardColors.Blue, FontWeight.Medium)
            CardText("ศาสนา พุทธ", 195, 182, 11, IdCardColors.Ink, FontWeight.Medium)
            CardText("ที่อยู่ xx/xx หมู่ที่ xx ถนน xxxxx", 75, 202, 11, IdCardColors.Ink, FontWeight.Bold)
            CardText("แขวง xxxxx อ.xxxx จ.xxxxx", 75, 219, 11, IdCardColors.Ink, FontWeight.Medium)

            // วันออกบัตร / วันหมดอายุ
            listOf(75, 287).forEachIndexed { i, x ->
                val (th, en) = if (i == 0) "28 มี.ค. 2567" to "28 Mar. 2024" else "28 มี.ค. 2576" to "28 Mar. 2033"
                CardText(th, x, 241, 9, IdCardColors.Ink, FontWeight.Bold)
                CardText(if (i == 0) "วันออกบัตร" else "วันหมดอายุ", x, 252, 8, IdCardColors.Ink, FontWeight.Medium)
                CardText(en, x, 263, 8, IdCardColors.Blue, FontWeight.Medium)
                CardText(if (i == 0) "Date of Issue" else "Date of Expiry", x, 274, 8, IdCardColors.Blue, FontWeight.Medium)
            }
            Placeholder(Modifier.offset(x = 178.s, y = 236.s).size(34.s), CircleShape)        // ตราประทับ
            Placeholder(Modifier.offset(x = 216.s, y = 250.s).size(40.s, 8.s), CircleShape)   // ลายเซ็น
            CardText("เจ้าพนักงานออกบัตร", 165, 279, 8, IdCardColors.Ink, FontWeight.Medium)

            // รูปถ่าย → กล่อง + วงกลม(หัว) + วงรี(ไหล่)
            Box(
                Modifier.offset(x = 358.s, y = 151.s).size(99.s, 120.s)
                    .clip(RoundedCornerShape(6.s)).background(Color.White)
                    .border(1.5.dp, IdCardColors.PhBorder, RoundedCornerShape(6.s))
            ) {
                Placeholder(Modifier.align(Alignment.TopCenter).offset(y = 19.s).size(34.s), CircleShape)
                Placeholder(Modifier.align(Alignment.TopCenter).offset(y = 62.s).size(75.s, 84.s), CircleShape)
            }
            CardText("0000-00-00000000", 358, 286, 8, IdCardColors.Ink, FontWeight.Medium)

            // แสงกวาดผิวบัตร
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = (sheen * 2f - 1f) * size.width * .7f }
                    .background(
                        Brush.linearGradient(
                            0f to Color.Transparent, .48f to Color.White.copy(alpha = .8f), 1f to Color.Transparent
                        )
                    )
            )
        }
    }
}

@Composable
private fun GoldChip(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(64.s, 60.s)
            .clip(RoundedCornerShape(9.s))
            .background(IdCardColors.Chip)
            .border(1.2.dp, IdCardColors.ChipLine, RoundedCornerShape(9.s))
            .drawBehind {
                val c = IdCardColors.ChipLine; val w = 1.2.dp.toPx()
                drawLine(c, Offset(0f, size.height * .34f), Offset(size.width, size.height * .34f), w)
                drawLine(c, Offset(size.width * .30f, 0f), Offset(size.width * .30f, size.height), w)
                drawLine(c, Offset(size.width * .70f, 0f), Offset(size.width * .70f, size.height), w)
            }
    )
}

@Composable
private fun CardText(
    text: String, x: Int, y: Int, size: Int, color: Color, weight: FontWeight, letterSpacing: Float = 0f,
) {
    Text(
        text, fontSize = size.st, color = color, fontWeight = weight, maxLines = 1, softWrap = false,
        lineHeight = size.st, letterSpacing = letterSpacing.toDouble().st,
        modifier = Modifier.offset(x = x.s, y = y.s)
    )
}

/** placeholder แบบ wireframe: รูปทรงพื้นฐานฟ้าอ่อนขอบฟ้าเข้ม แทนภาพวาดทุกชิ้นบนบัตร */
@Composable
private fun Placeholder(modifier: Modifier, shape: androidx.compose.ui.graphics.Shape) {
    Box(modifier.clip(shape).background(IdCardColors.PhFill).border(1.5.dp, IdCardColors.PhBorder, shape))
}

/* ============================ ทางเลือกอื่น ============================ */

@Composable
private fun AltLabel(str: WelcomeStrings, modifier: Modifier = Modifier) {
    Row(
        modifier.padding(horizontal = 60.s).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(26.s)
    ) {
        val rule = Brush.horizontalGradient(
            listOf(Color.Transparent, Bright.Ink.copy(alpha = .25f), Color.Transparent)
        )
        Box(Modifier.weight(1f).height(2.dp).background(rule))
        Text(str.altLabel, fontSize = 30.st, fontWeight = FontWeight.SemiBold, color = Bright.Ink, letterSpacing = 1.st)
        Box(Modifier.weight(1f).height(2.dp).background(rule))
    }
}

/**
 * ปุ่มภาพ 2 ปุ่มจากดีไซเนอร์ (btn_hn.png / btn_scan.png ขนาด 466 x 668 พื้นโปร่ง มีเงาในไฟล์แล้ว)
 * ต้นฉบับ 466 x 668 แสดงที่ 85% = 396 x 568 วางที่ y 1150 ซ้าย x 124 / ขวา x 560 (หน่วยดีไซน์ 1080 x 1920)
 */
@Composable
private fun ImageButtons(str: WelcomeStrings, onEnterHn: () -> Unit, onFaceScan: () -> Unit) {
    ImageButton(str.btnHnRes, str.btnHn, x = 124, onClick = onEnterHn)
    ImageButton(str.btnScanRes, str.btnScan, x = 560, onClick = onFaceScan)
}

@Composable
private fun ImageButton(resId: Int, label: String, x: Int, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Image(
        painter = painterResource(resId),
        contentDescription = label,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .offset(x = x.s, y = 1150.s)
            .size(396.s, 568.s)
            .graphicsLayer { val k = if (pressed) .97f else 1f; scaleX = k; scaleY = k }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    )
}
