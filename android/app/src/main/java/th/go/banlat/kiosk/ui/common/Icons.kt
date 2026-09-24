package th.go.banlat.kiosk.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser

/**
 * ไอคอนเส้น 24x24 ชุดเดียวกับต้นแบบ HTML (path เดียวกันทุกตัว)
 * วาดเป็นเส้นปลายมน ไม่มีไฟล์รูป · เปลี่ยนสี/ขนาด/ความหนาได้จากโค้ด
 */
enum class KIcon(vararg val paths: String) {
    IdCard("M7 3h10a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3V6a3 3 0 0 1 3-3Z", "M8 8h8M8 12h8M8 16h4"),
    FaceScan(
        "M4 8V6a2 2 0 0 1 2-2h2M16 4h2a2 2 0 0 1 2 2v2M20 16v2a2 2 0 0 1-2 2h-2M8 20H6a2 2 0 0 1-2-2v-2",
        "M14.4 11a2.4 2.4 0 1 1-4.8 0 2.4 2.4 0 0 1 4.8 0Z", "M8.5 16.4a4.2 4.2 0 0 1 7 0",
    ),
    Gear(
        "M15.2 12a3.2 3.2 0 1 1-6.4 0 3.2 3.2 0 0 1 6.4 0Z",
        "M19.4 14.6a1.7 1.7 0 0 0 .34 1.87l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.7 1.7 0 0 0-1.87-.34 1.7 1.7 0 0 0-1.03 1.56V21a2 2 0 1 1-4 0v-.11a1.7 1.7 0 0 0-1.11-1.56 1.7 1.7 0 0 0-1.87.34l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.7 1.7 0 0 0 .34-1.87 1.7 1.7 0 0 0-1.56-1.03H3a2 2 0 1 1 0-4h.11a1.7 1.7 0 0 0 1.56-1.11 1.7 1.7 0 0 0-.34-1.87l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.7 1.7 0 0 0 1.87.34H9a1.7 1.7 0 0 0 1.03-1.56V3a2 2 0 1 1 4 0v.11a1.7 1.7 0 0 0 1.03 1.56 1.7 1.7 0 0 0 1.87-.34l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.7 1.7 0 0 0-.34 1.87V9a1.7 1.7 0 0 0 1.56 1.03H21a2 2 0 1 1 0 4h-.11a1.7 1.7 0 0 0-1.49 1.03Z",
    ),
    Doc("M7 3h7l4 4v14H7z", "M14 3v4h4", "M10 12h6M10 16h6"),
    Check("M4 12.5 9 17.5 20 6.5"),
    BigCheck("M4 12.5 9.5 18 20 6.5"),
    Cross("M6.5 6.5 17.5 17.5M17.5 6.5 6.5 17.5"),
    Info("M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z", "M12 11v5M12 7.6h.01"),
    Warn("M12 4 2.8 20h18.4L12 4Z", "M12 10v4.5M12 17.4h.01"),
    Down("M12 5v13M6 12.5l6 6 6-6"),
    Backspace("M9 5h11a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H9L2.5 12 9 5Z", "m12 9.5 5 5M17 9.5l-5 5"),
    User("M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z", "M4.5 20.5a7.5 7.5 0 0 1 15 0"),
    Shield("M12 3 5 6v5.5c0 4.3 2.9 8.2 7 9.5 4.1-1.3 7-5.2 7-9.5V6l-7-3Z", "m9 12 2.2 2.2L15.5 10"),
    Clock("M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z", "M12 7v5.2l3.2 2"),
    Phone("M9.5 2.5h5a3 3 0 0 1 3 3v13a3 3 0 0 1-3 3h-5a3 3 0 0 1-3-3v-13a3 3 0 0 1 3-3Z", "M10.5 18.5h3"),
}

@Composable
fun LineIcon(icon: KIcon, color: Color, modifier: Modifier, stroke: Float = 1.8f) {
    val paths: List<Path> = remember(icon) { icon.paths.map { PathParser().parsePathString(it).toPath() } }
    Canvas(modifier) {
        val k = size.minDimension / 24f
        scale(k, k, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            val st = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            paths.forEach { drawPath(it, color, style = st) }
        }
    }
}
