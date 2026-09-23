package th.go.banlat.kiosk.ui.welcome

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue

/** ความยาวขาไป 8 วิ; ขากลับเล่นย้อนทางเดิมอีก 8 วิ (RepeatMode.Reverse) รวมรอบละ 16 วิ */
const val INSERT_CYCLE_MS = 8000

/** เดินหน้าแล้วย้อนกลับทางเดิม = animation-direction: alternate ของ CSS */
private fun <T> pingPong(spec: androidx.compose.animation.core.DurationBasedAnimationSpec<T>) =
    infiniteRepeatable<T>(animation = spec, repeatMode = RepeatMode.Reverse)

private fun <T> loop(spec: androidx.compose.animation.core.DurationBasedAnimationSpec<T>) =
    infiniteRepeatable<T>(animation = spec, repeatMode = RepeatMode.Restart)

/**
 * ไทม์ไลน์ขาไป ตรงกับ @keyframes insert ใน preview/welcome.html
 *
 *  0.0–1.8 วิ  โชว์หน้าบัตรตั้งตรงเต็มใบ
 *  1.8–2.7 วิ  เอนบัตรลงนอนราบ (rotationX 0→88)
 *  2.7–3.4 วิ  ยกขึ้นวางบนขอบรับ (translateY 90→0)
 *  3.4–4.3 วิ  หมุนในระนาบให้ชิปหันเข้าช่อง (rotationZ 0→90)
 *  5.0–6.6 วิ  แทงเข้าครึ่งใบให้ชิปถึงหัวอ่าน (z 250→0)
 *  6.6–8.0 วิ  ค้างอ่าน
 *  จากนั้นเล่นย้อนกลับ: ดึงบัตรออก → หมุนกลับ → ยกขึ้นตั้ง แล้ววนใหม่ ไม่มีการจาง/วาร์ป
 */
@Composable
fun rememberInsertPose(): CardPose {
    val t = rememberInfiniteTransition(label = "insert")

    val ty by t.animateFloat(
        initialValue = 90f, targetValue = 0f, label = "ty",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            90f at 0; 90f at 2720; 0f at 3360; 0f at INSERT_CYCLE_MS
        })
    )
    val tz by t.animateFloat(
        initialValue = 320f, targetValue = 0f, label = "tz",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            320f at 0; 320f at 4640
            250f at 5040                         // ปลายชิปแตะปากช่อง
            0f at 6560; 0f at INSERT_CYCLE_MS    // เข้าครึ่งใบ แล้วค้างอ่าน
        })
    )
    val rx by t.animateFloat(
        initialValue = 0f, targetValue = 90f, label = "rx",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            0f at 0; 0f at 1760; 88f at 2720; 88f at 4640; 89f at 5040; 90f at 6560; 90f at INSERT_CYCLE_MS
        })
    )
    val rz by t.animateFloat(
        initialValue = 0f, targetValue = 90f, label = "rz",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            0f at 0; 0f at 3360; 90f at 4320; 90f at INSERT_CYCLE_MS
        })
    )
    return CardPose(ty, tz, rx, rz, alpha = 1f)
}

/** ความเข้มของเงาติดตัวบัตร: เต็มตอนตั้ง หายไปตอนนอน (ให้เงาบนพื้นรับช่วง) */
@Composable
fun rememberDropShadowAlpha(): Float {
    val t = rememberInfiniteTransition(label = "drop")
    val v by t.animateFloat(
        initialValue = 1f, targetValue = 0f, label = "dropA",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            1f at 0; 1f at 1760; 0f at 2720; 0f at INSERT_CYCLE_MS
        })
    )
    return v
}

/** ท่าของเงาบนพื้น + สัดส่วน (กว้าง, ลึก) — ตรงกับ @keyframes cardShadow ใน HTML */
@Immutable
data class ShadowPose(val pose: CardPose, val scaleX: Float, val scaleDepth: Float)

/**
 * เงาตกกระทบบนพื้น นอนราบเสมอ (rotationX 90) เดินตามบัตรตอนบัตรนอน
 *  ตอนบัตรตั้ง  → ซ่อน (ใช้เงาติดตัวบัตรแทน)
 *  ตอนบัตรนอน  → ใหญ่กว่ารอยเท้าบัตรนิดหน่อย (1.16 x 1.12) เยื้องลงล่าง 14 หน่วย
 */
@Composable
fun rememberShadowPose(): ShadowPose {
    val t = rememberInfiniteTransition(label = "shadow")
    val ty by t.animateFloat(
        initialValue = 104f, targetValue = 14f, label = "sty",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            104f at 0; 104f at 2720; 14f at 3360; 14f at INSERT_CYCLE_MS
        })
    )
    val tz by t.animateFloat(
        initialValue = 332f, targetValue = 12f, label = "stz",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            332f at 0; 332f at 4640; 262f at 5040; 12f at 6560; 12f at INSERT_CYCLE_MS
        })
    )
    val rz by t.animateFloat(
        initialValue = 0f, targetValue = 90f, label = "srz",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            0f at 0; 0f at 3360; 90f at 4320; 90f at INSERT_CYCLE_MS
        })
    )
    val alpha by t.animateFloat(
        initialValue = 0f, targetValue = .85f, label = "sal",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            0f at 0; 0f at 1760; .85f at 2720; .85f at INSERT_CYCLE_MS
        })
    )
    return ShadowPose(CardPose(ty, tz, 90f, rz, alpha), 1.16f, 1.12f)
}

/** แสงกวาดผิวบัตร วิ่งตอนบัตรยังยกอยู่ */
@Composable
fun rememberSheenProgress(): Float {
    val t = rememberInfiniteTransition(label = "sheen")
    val v by t.animateFloat(
        initialValue = 0f, targetValue = 1f, label = "sheenP",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            0f at 0; 0f at 480; 1f at 2080; 1f at INSERT_CYCLE_MS
        })
    )
    return v
}

/** ไฟทองในช่อง สว่างขึ้นตอนบัตรเข้า แล้วกะพริบช่วงอ่าน */
@Composable
fun rememberSlotGlow(): Float {
    val t = rememberInfiniteTransition(label = "slot")
    val v by t.animateFloat(
        initialValue = .25f, targetValue = .4f, label = "glow",
        animationSpec = pingPong(keyframes {
            durationMillis = INSERT_CYCLE_MS
            .25f at 0; .25f at 4480; 1f at 5120
            1f at 6560; .4f at 7040; 1f at 7520; .4f at INSERT_CYCLE_MS
        })
    )
    return v
}

/** ลูกศรบนพื้น ไล่กันเป็นชุดวิ่งเข้าหาช่อง */
@Composable
fun rememberArrowPulse(indexDelayMs: Int): Float {
    val t = rememberInfiniteTransition(label = "arrow$indexDelayMs")
    val v by t.animateFloat(
        initialValue = 0f, targetValue = 1f, label = "arrowP",
        animationSpec = loop(keyframes {
            durationMillis = 1900 + indexDelayMs
            0f at 0; 0f at indexDelayMs; 1f at 1900 + indexDelayMs
        })
    )
    return v
}
