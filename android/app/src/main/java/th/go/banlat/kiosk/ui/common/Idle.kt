package th.go.banlat.kiosk.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.unitPx
import th.go.banlat.kiosk.ui.welcome.swallowTaps

/** นับรวมกี่วินาทีจากแตะครั้งล่าสุดจึงกลับหน้าแรก · กี่วินาทีสุดท้ายที่เด้งเตือนนับถอยหลัง (ตรงกับ preview/idle.js)
 *  TODO(integration): อ่านจากตั้งค่าตู้ "ระยะเวลาในการกลับไปหน้าแรก (วินาที)" */
const val IDLE_SECONDS = 60
const val IDLE_WARN_SECONDS = 20

/** เตือน "ยังใช้งานอยู่หรือไม่" + วงนับถอยหลังสีทอง → ครบเวลา = onHome */
@Composable
fun IdleWarning(onContinue: () -> Unit, onHome: () -> Unit) {
    var left by remember { mutableFloatStateOf(IDLE_WARN_SECONDS.toFloat()) }
    LaunchedEffect(Unit) {
        val t0 = withFrameMillis { it }
        while (left > 0f) withFrameMillis { left = (IDLE_WARN_SECONDS - (it - t0) / 1000f).coerceAtLeast(0f) }
        onHome()
    }
    val u = unitPx()
    val strong = SpanStyle(color = K.Ink, fontWeight = FontWeight.Bold)
    Box(Modifier.fillMaxSize().background(K.ScrimLight).swallowTaps().padding(horizontal = 100.s), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth()
            .softShadow(40f, Shade(Color(0x59061432), 40f, 100f))
            .clip(RoundedCornerShape(40.s))
            .background(Brush.linearGradient(0f to K.PearlTop, .55f to K.PearlMid, 1f to K.PearlBottom))
            .border(1.s, Color(0x52BF913A), RoundedCornerShape(40.s))
            .padding(start = 56.s, end = 56.s, top = 64.s, bottom = 48.s),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.padding(bottom = 32.s).size(160.s), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = 10f * u; val r = size.minDimension / 2 - w / 2
                    drawCircle(Color(0xFFF1E6CF), r, style = Stroke(w))
                    drawArc(Color(0xFFC9942F), -90f, 360f * left / IDLE_WARN_SECONDS, false,
                        topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2),
                        size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w), style = Stroke(w, cap = StrokeCap.Round))
                }
                KText("${kotlin.math.ceil(left).toInt()}", 64, weight = FontWeight.Bold, tabular = true)
            }
            KText("ยังใช้งานอยู่หรือไม่", 44, weight = FontWeight.Bold, lineHeight = 60f, align = TextAlign.Center)
            KText(buildAnnotatedString {
                append("ไม่มีการใช้งานสักพักแล้ว\nระบบจะ"); withStyle(strong) { append("กลับหน้าแรก") }; append("อัตโนมัติเมื่อครบเวลา")
            }, 38, Modifier.padding(top = 16.s), color = K.InkMuted, lineHeight = 62f, align = TextAlign.Center)
            Row(Modifier.fillMaxWidth().padding(top = 48.s), horizontalArrangement = Arrangement.spacedBy(24.s)) {
                Box(Modifier.widthIn(min = 280.s).height(96.s).clip(RoundedCornerShape(99.s)).background(Color.White)
                    .border(2.s, Color(0xFFDCE4EE), RoundedCornerShape(99.s)).press(onClick = onHome).padding(horizontal = 32.s),
                    contentAlignment = Alignment.Center) {
                    KText("กลับหน้าแรก", 30, weight = FontWeight.Bold, color = K.InkMuted, softWrap = false)
                }
                Box(Modifier.weight(1f).height(96.s).softShadow(99f, Shade(Color(0x4714265A), 10f, 24f)).clip(RoundedCornerShape(99.s))
                    .background(Brush.linearGradient(listOf(Color(0xFF223A7A), K.Ink))).press(onClick = onContinue),
                    contentAlignment = Alignment.Center) {
                    KText("ใช้งานต่อ", 30, weight = FontWeight.Bold, color = Color.White, softWrap = false)
                }
            }
        }
    }
}
