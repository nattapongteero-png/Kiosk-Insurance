package th.go.banlat.kiosk.ui.theme

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import th.go.banlat.kiosk.R

/** Noto Sans Thai (ไทย + ละติน) ฝังในแอป ไม่พึ่งฟอนต์ของเครื่อง */
val NotoSansThai = FontFamily(
    Font(R.font.noto_sans_thai_regular, FontWeight.Normal),
    Font(R.font.noto_sans_thai_medium, FontWeight.Medium),
    Font(R.font.noto_sans_thai_semibold, FontWeight.SemiBold),
    Font(R.font.noto_sans_thai_bold, FontWeight.Bold),
)

/** จัดข้อความกึ่งกลางบรรทัดเหมือน CSS line-height */
private val CssLine = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)

/**
 * ข้อความตามค่าในแบบ: size / lineHeight / letterSpacing เป็นหน่วยออกแบบ (px ของกรอบ 1080)
 * lineHeight = 0 → ใช้ 1.5 เท่าของขนาด (ใกล้เคียง line-height: normal ของ Noto Sans Thai)
 */
@Composable
fun KText(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    color: Color = K.Ink,
    lineHeight: Float = 0f,
    letterSpacing: Float = 0f,
    align: TextAlign = TextAlign.Start,
    tabular: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    minSize: Int = 0,
) = KText(AnnotatedString(text), size, modifier, weight, color, lineHeight, letterSpacing, align, tabular, maxLines, softWrap, minSize)

@Composable
fun KText(
    text: AnnotatedString,
    size: Int,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    color: Color = K.Ink,
    lineHeight: Float = 0f,
    letterSpacing: Float = 0f,
    align: TextAlign = TextAlign.Start,
    tabular: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    minSize: Int = 0,   // > 0 = ย่อตัวอักษรลงได้ถึงขนาดนี้เมื่อพื้นที่ไม่พอ (ฟอนต์บน Android กว้างกว่า Chrome เล็กน้อย)
    inline: Map<String, androidx.compose.foundation.text.InlineTextContent> = emptyMap(),   // รูปแทรกกลางข้อความ (appendInlineContent)
) {
    BasicText(
        text = text,
        inlineContent = inline,
        modifier = modifier,
        style = TextStyle(
            fontFamily = NotoSansThai,
            fontWeight = weight,
            fontSize = size.st,
            lineHeight = (if (lineHeight > 0f) lineHeight else size * 1.5f).st,
            letterSpacing = letterSpacing.st,
            color = color,
            textAlign = align,
            fontFeatureSettings = if (tabular) "tnum" else null,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = CssLine,
        ),
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = TextOverflow.Clip,
        autoSize = if (minSize > 0) TextAutoSize.StepBased(minSize.st, size.st, 1.st) else null,
    )
}
