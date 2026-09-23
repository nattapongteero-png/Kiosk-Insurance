package th.go.banlat.kiosk.ui.welcome

import android.graphics.Matrix
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import th.go.banlat.kiosk.ui.theme.LocalKioskScale
import kotlin.math.cos
import kotlin.math.sin

/**
 * กล้อง 3D ของฉากเสียบบัตร — ตัวเลขเดียวกับ .rig ใน preview/welcome.html
 *
 *   perspective: 3000px; perspective-origin: 50% -60%  (ของ rig 760 x 700)
 *   ตาอยู่สูงเหนือฉากมาก เพื่อให้เห็นหน้าบัตรที่นอนราบชัด
 *
 * ทำไมไม่ใช้ graphicsLayer.rotationX ตรงๆ:
 *   Compose ไม่มี translationZ และกล้องของแต่ละเลเยอร์อยู่กลางชิ้นงานตัวเอง
 *   บัตรที่นอนราบ (rotationX ≈ 90°) จึงมองเห็นเป็นเส้นบาง ไม่ใช่สี่เหลี่ยมคางหมู
 *   ที่นี่เลยคำนวณจุดฉายภาพเองด้วยกล้องร่วมจุดเดียว แล้วส่ง 4 มุมให้ Canvas วาดด้วย perspective matrix
 *
 * หน่วยทุกตัวเป็น "หน่วยดีไซน์" (design unit) ของ rig; แปลงเป็น px ตอนวาดด้วย k = density * kioskScale
 */
object Rig {
    const val WIDTH = 760f
    const val HEIGHT = 700f
    const val FOCAL = 3000f                     // ระยะตาถึงจอ
    val EYE = Offset(WIDTH / 2f, -HEIGHT * .60f) // จุดรวมสายตา (perspective-origin)

    /** ฉายจุด 3 มิติ (x, y ในหน่วย rig; z บวก = ใกล้ตา) ลงบนระนาบจอ */
    fun project(x: Float, y: Float, z: Float): Offset {
        val s = FOCAL / (FOCAL - z)
        return Offset(EYE.x + (x - EYE.x) * s, EYE.y + (y - EYE.y) * s)
    }
}

/** ท่าของบัตรในโลก 3D — ตรงกับค่าใน @keyframes insert ของ HTML */
data class CardPose(
    val translateY: Float,   // สูง(-)/ต่ำ(+) จากระดับปากช่อง
    val translateZ: Float,   // ใกล้ตา(+) / ลึกเข้าไปในเครื่อง(-)
    val rotationX: Float,    // องศาที่เอนบัตรลงนอนราบ (90 = นอนราบสนิท)
    val rotationZ: Float,    // หมุนในระนาบบัตร (90 = ริมซ้าย/ชิป ไปอยู่ขอบบน)
    val alpha: Float,
) {
}

/**
 * คำนวณ 4 มุมของบัตร (ขนาด w x h หน่วย rig, จุดหมุนกลางบัตรที่ (cx, cy) บน rig)
 * ลำดับ transform เหมือน CSS: rotateZ → rotateX → translate3d → project
 * คืนค่าเรียง: ซ้ายบน, ขวาบน, ขวาล่าง, ซ้ายล่าง ของ layout เดิม (ก่อนหมุน)
 */
fun CardPose.projectedCorners(cx: Float, cy: Float, w: Float, h: Float): List<Offset> {
    val rz = Math.toRadians(rotationZ.toDouble())
    val rx = Math.toRadians(rotationX.toDouble())
    val hw = w / 2f; val hh = h / 2f
    val local = listOf(Offset(-hw, -hh), Offset(hw, -hh), Offset(hw, hh), Offset(-hw, hh))
    return local.map { (u, v) ->
        // หมุนในระนาบ (rotateZ, ตามเข็ม)
        val x = (u * cos(rz) - v * sin(rz)).toFloat()
        val y = (u * sin(rz) + v * cos(rz)).toFloat()
        // เอนลง (rotateX): ขอบบนถอยลึกเข้าไป (z ลบ)
        val y2 = (y * cos(rx)).toFloat()
        val z2 = (y * sin(rx)).toFloat()
        Rig.project(cx + x, cy + translateY + y2, translateZ + z2)
    }
}

/**
 * มุมของระนาบนอนราบ (ฝาบนเครื่อง, ลูกศรบนพื้น) กว้าง w ที่ระดับ y
 * ขอบบนของ layout อยู่ที่ zTop, ขอบล่างอยู่ที่ zBottom (z บวก = ใกล้ตา)
 */
fun floorQuad(x: Float, y: Float, w: Float, zTop: Float, zBottom: Float): List<Offset> = listOf(
    Rig.project(x, y, zTop), Rig.project(x + w, y, zTop),
    Rig.project(x + w, y, zBottom), Rig.project(x, y, zBottom),
)

/**
 * วาดเนื้อหาของ composable นี้ผ่าน perspective matrix
 * @param originInRig ตำแหน่งซ้ายบนของ composable นี้บน rig (หน่วยดีไซน์)
 * @param corners     4 มุมปลายทางบน rig เรียง ซ้ายบน ขวาบน ขวาล่าง ซ้ายล่าง
 */
@Composable
fun Modifier.projectQuad(originInRig: Offset, corners: () -> List<Offset>): Modifier {
    val k = LocalDensity.current.density * LocalKioskScale.current
    return drawWithContent {
        val dst = corners()
        val src = floatArrayOf(0f, 0f, size.width, 0f, size.width, size.height, 0f, size.height)
        val dstPx = FloatArray(8)
        dst.forEachIndexed { i, p ->
            dstPx[i * 2] = (p.x - originInRig.x) * k
            dstPx[i * 2 + 1] = (p.y - originInRig.y) * k
        }
        val m = Matrix().apply { setPolyToPoly(src, 0, dstPx, 0, 4) }
        drawIntoCanvas { c ->
            c.nativeCanvas.save()
            c.nativeCanvas.concat(m)
            this@drawWithContent.drawContent()
            c.nativeCanvas.restore()
        }
    }
}

/** วาดเฉพาะส่วนบน (fraction จากขอบบน) หรือส่วนล่างของ composable */
fun Modifier.drawOnly(topFraction: Float, upperPart: Boolean): Modifier = drawWithContent {
    val cut = size.height * topFraction
    if (upperPart) clipRect(top = 0f, bottom = cut) { this@drawWithContent.drawContent() }
    else clipRect(top = cut, bottom = size.height) { this@drawWithContent.drawContent() }
}
