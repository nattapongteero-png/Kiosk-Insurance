package th.go.banlat.kiosk.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser

/**
 * ไอคอนเส้น 24x24 ชุดเดียวกับต้นแบบ HTML (path เดียวกันทุกตัว)
 * วาดเป็นเส้นปลายมน ไม่มีไฟล์รูป · เปลี่ยนสี/ขนาด/ความหนาได้จากโค้ด
 */
enum class KIcon(vararg val paths: String) {
    ReadCard("M5 5h14a2.5 2.5 0 0 1 2.5 2.5v9A2.5 2.5 0 0 1 19 19H5a2.5 2.5 0 0 1-2.5-2.5v-9A2.5 2.5 0 0 1 5 5Z",
        "M10.5 11a2 2 0 1 1-4 0 2 2 0 0 1 4 0Z", "M5.5 16c.6-1.4 1.7-2 3-2s2.4.6 3 2M14 10h4.5M14 13.5h3"),
    Barcode("M3 7V5.5A1.5 1.5 0 0 1 4.5 4H6M18 4h1.5A1.5 1.5 0 0 1 21 5.5V7M21 17v1.5a1.5 1.5 0 0 1-1.5 1.5H18M6 20H4.5A1.5 1.5 0 0 1 3 18.5V17",
        "M7 8v8M10 8v8M12.5 8v8M15.5 8v8M17.5 8v8"),
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
    Close("M18 6 6 18M6 6l12 12"),
    Info("M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z", "M12 11v5M12 7.6h.01"),
    Warn("M12 4 2.8 20h18.4L12 4Z", "M12 10v4.5M12 17.4h.01"),
    Down("M12 5v13M6 12.5l6 6 6-6"),
    Backspace("M9 5h11a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H9L2.5 12 9 5Z", "m12 9.5 5 5M17 9.5l-5 5"),
    User("M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z", "M4.5 20.5a7.5 7.5 0 0 1 15 0"),
    Shield("M12 3 5 6v5.5c0 4.3 2.9 8.2 7 9.5 4.1-1.3 7-5.2 7-9.5V6l-7-3Z", "m9 12 2.2 2.2L15.5 10"),
    Clock("M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z", "M12 7v5.2l3.2 2"),
    Phone("M9.5 2.5h5a3 3 0 0 1 3 3v13a3 3 0 0 1-3 3h-5a3 3 0 0 1-3-3v-13a3 3 0 0 1 3-3Z", "M10.5 18.5h3"),
    // หน้าเลือกบริการ (ระบบลงทะเบียน)
    Steth("M6 3v6a5 5 0 0 0 10 0V3", "M11 14v2.5a4.5 4.5 0 0 0 9 0V14", "M18 12a2 2 0 1 0 4 0a2 2 0 1 0 -4 0Z"),
    Bandage("M8.5 3.5 20.5 15.5a3.5 3.5 0 0 1-5 5L3.5 8.5a3.5 3.5 0 0 1 5-5Z", "M10 10.5h.01M12 12h.01M13.5 13.5h.01M10.5 13.5h.01M13.5 10.5h.01"),
    Physio("M10 4.5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0Z", "M12 7.5v6l-3 7M12 13.5l3 7M7 10.5l5-2 5 2"),
    Leaf("M5 19c0-8 5-14 15-14 0 10-6 15-14 15", "M5 19 14 10"),
    Mind("M9.5 20v-3H7a2 2 0 0 1-2-2v-2.5L3.5 11 5 9a7 7 0 0 1 14 1c0 2.4-1 4.2-3 5.4V20", "M12 7.5v4M10 9.5h4"),
    Tooth("M12 5.5c-1.6-1.4-4.8-1.9-6.3 0C4.1 7.6 5 11 6 13c.8 1.7 1 7 2.8 7 1.6 0 1.4-4.5 3.2-4.5s1.6 4.5 3.2 4.5c1.8 0 2-5.3 2.8-7 1-2 1.9-5.4.3-7.5-1.5-1.9-4.7-1.4-6.3 0Z"),
    Heart("M12 20s-7.5-4.6-7.5-10A4.3 4.3 0 0 1 12 7.3 4.3 4.3 0 0 1 19.5 10C19.5 15.4 12 20 12 20Z", "M4.5 12.5h4l1.5-2.5 2.5 5 1.5-2.5h5.5"),
    Flask("M9 3h6M10 3v6.5L4.8 18.2A1.8 1.8 0 0 0 6.3 21h11.4a1.8 1.8 0 0 0 1.5-2.8L14 9.5V3", "M7.5 15h9"),
    Xray("M6.5 3.5h11a3 3 0 0 1 3 3v11a3 3 0 0 1-3 3h-11a3 3 0 0 1-3-3v-11a3 3 0 0 1 3-3Z", "M12 6.5v11M9 8.5h6M8.5 11h7M9 13.5h6"),
    Walk("M11 4.5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0Z", "m9.5 21 2-6 2.5 2v4M8 12l2-4.5 3.5.5 2 3.5 2.5 1", "m11.5 15 1.5-6.5"),
    Wheelchair("M8 4.5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0Z", "M10 7.5v5h5l2.5 5.5H19", "M7.5 11.5a5 5 0 1 0 7.5 5"),
    Stretcher("M3 13h18M5 13v4M19 13v4", "M3.5 19a1.5 1.5 0 1 0 3.0 0a1.5 1.5 0 1 0 -3.0 0Z", "M17.5 19a1.5 1.5 0 1 0 3.0 0a1.5 1.5 0 1 0 -3.0 0Z", "M5 9.5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0Z", "M10 11h8.5"),
    Carry("M7 4.5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0Z", "M9 7.5v6l-2 7.5M9 13.5l2.5 7.5M6 10.5l3-1 5 2.5", "M14.9 9a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0Z", "M13.5 12.5l5-1"),
    Family("M5.5 7a2.5 2.5 0 1 0 5.0 0a2.5 2.5 0 1 0 -5.0 0Z", "M14.5 8a2 2 0 1 0 4 0a2 2 0 1 0 -4 0Z", "M3.5 20a4.5 4.5 0 0 1 9 0M13 20a3.5 3.5 0 0 1 7 0"),
}

@Composable
fun LineIcon(icon: KIcon, color: Color, modifier: Modifier, stroke: Float = 1.8f) {
    val paths: List<Path> = remember(icon) { icon.paths.map { PathParser().parsePathString(it).toPath() } }
    // ขยาย "ตัว path" เป็นขนาดพิกเซลจริงก่อนวาด (ไม่ scale canvas)
    // Android 7–8 วาด path ใต้ canvas ที่ถูก scale เป็นภาพเล็กแล้วขยาย → เส้นไอคอนเบลอ · แบบนี้คมทุกเวอร์ชัน
    var cache by remember(icon) { mutableStateOf<Pair<Float, List<Path>>?>(null) }
    Canvas(modifier) {
        val k = size.minDimension / 24f
        val scaled = cache?.takeIf { it.first == k }?.second ?: paths.map { src ->
            Path().apply {
                addPath(src)
                asAndroidPath().transform(android.graphics.Matrix().apply { setScale(k, k) })
            }
        }.also { cache = k to it }
        val st = Stroke(width = stroke * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        scaled.forEach { drawPath(it, color, style = st) }
    }
}
