package th.go.banlat.kiosk.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color

/** กดแล้วยุบเล็กน้อย (scale .982 แบบ cubic-bezier(.2,.8,.2,1)) ไม่มี ripple — ให้ความรู้สึกปุ่มจริงบนจอสัมผัส */
fun Modifier.press(enabled: Boolean = true, scaleTo: Float = .982f, onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed && enabled) scaleTo else 1f, tween(200), label = "press")
    this.graphicsLayer { scaleX = sc; scaleY = sc }
        .clickable(src, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
}

/** สถานะกดอยู่ ไว้เปลี่ยนเงา/ขอบ */
@Composable
fun rememberPressSource(): MutableInteractionSource = remember { MutableInteractionSource() }

/** ข้อความที่มีส่วนตัวหนา เขียน **แบบนี้** */
fun rich(s: String, boldColor: Color? = null): AnnotatedString = buildAnnotatedString {
    val parts = s.split("**")
    parts.forEachIndexed { i, part ->
        if (i % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor ?: Color.Unspecified)) { append(part) }
        else append(part)
    }
}
