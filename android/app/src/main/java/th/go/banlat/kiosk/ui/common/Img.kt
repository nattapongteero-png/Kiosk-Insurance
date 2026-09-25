package th.go.banlat.kiosk.ui.common

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext

/**
 * รูปใช้ร่วมทั้งแอป: ถอดรหัสครั้งเดียวแล้วเก็บไว้ (painterResource ถอดใหม่ทุกหน้า → ตู้ Android 7 หน่วยความจำเต็ม)
 * sample = ย่อตอนถอด (2 = ครึ่งหนึ่ง) ใช้กับรูปที่ไฟล์ใหญ่กว่าขนาดที่แสดงจริงเกิน 2 เท่า ภาพบนจอจึงไม่ต่างกัน
 */
object Img {
    private val cache = HashMap<Int, ImageBitmap>()
    fun get(context: android.content.Context, id: Int, sample: Int = 1): ImageBitmap = cache.getOrPut(id) {
        BitmapFactory.decodeResource(context.resources, id, BitmapFactory.Options().apply { inSampleSize = sample }).asImageBitmap()
    }
}

@Composable
fun imgPainter(id: Int, sample: Int = 1) = BitmapPainter(Img.get(LocalContext.current, id, sample))

@Composable
fun img(id: Int, sample: Int = 1): ImageBitmap = Img.get(LocalContext.current, id, sample)
