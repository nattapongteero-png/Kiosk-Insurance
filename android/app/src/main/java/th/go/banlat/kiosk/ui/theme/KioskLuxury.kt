package th.go.banlat.kiosk.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * โทน "คลินิกสว่าง" — ดึงสีจากภาพพื้นหลัง res/drawable-nodpi/bg_home.png
 * (ล็อบบี้ขาว-ฟ้า, ปุ่มน้ำเงิน/เขียว, ตัวอักษรกรมท่า)
 * ตัวเลขเดียวกับ :root ใน preview/welcome.html
 */
object Bright {
    val Ink = Color(0xFF14265A)        // ตัวอักษรหลัก
    val InkMuted = Color(0xFF5B6B8A)   // ตัวอักษรรอง
    val Blue = Color(0xFF1E7BE0)       // น้ำเงินหลัก (ปุ่มซ้ายในภาพ)
    val BlueDeep = Color(0xFF0D5BC6)
    val BlueLight = Color(0xFF5FB2FF)  // แสง/ไฮไลต์
    val Green = Color(0xFF1E9C78)      // เขียว (ปุ่มขวาในภาพ)
    val Line = Color(0xFFB9CCE6)
    val Gold = Color(0xFFB8860B)   // คำว่า "ชิปสีทอง" ในบรรทัดรอง
    val Online = Color(0xFF22B573)

    /** ผิวกระจกขาวโปร่ง ใช้กับตรา/ปุ่มภาษา */
    val Glass = Color.White.copy(alpha = .72f)
    val GlassBorder = Color.White.copy(alpha = .95f)

    val BlueText = Brush.linearGradient(listOf(BlueDeep, Blue, BlueLight))
    val BlueButton = Brush.linearGradient(listOf(BlueLight, Blue, BlueDeep))

    /** ตัวเครื่องอ่านบัตร สีขาว-ฟ้าเข้ากับล็อบบี้ */
    val ReaderFace = Brush.verticalGradient(
        0f to Color(0xFFFFFFFF), .45f to Color(0xFFE6EFFA), 1f to Color(0xFFCFDDF0)
    )
    val ReaderLid = Brush.verticalGradient(listOf(Color(0xFFF6FAFF), Color(0xFFE4EDF9)))
    val Slot = Brush.verticalGradient(listOf(Color(0xFF0F1B33), Color(0xFF1B2A4A)))
}

/** สีของบัตรประชาชนในอนิเมชัน — ตามแม่แบบบัตรประชาชนไทย */
object IdCardColors {
    val Face = Brush.verticalGradient(0f to Color(0xFFD3E5F5), .45f to Color(0xFFDFECF8), 1f to Color(0xFFE8F2FB))
    val Ink = Color(0xFF1E2A5A)       // ตัวอักษรไทย/ตัวเลข
    val Blue = Color(0xFF1F6FD8)      // ตัวอักษรอังกฤษ
    val Red = Color(0xFFD8202B)       // วงแหวนตราครุฑ, ตราประทับ, เส้นโค้ง
    val Line = Color(0xFF7FB2E5)      // เส้นใต้แถวเลขบัตร
    val Sky = Color(0xFFC9DDF1)       // เส้นขอบฟ้าวัด/เมือง
    val Barcode = Color(0xFF151515)
    val Chip = Brush.linearGradient(listOf(Color(0xFFF8D67A), Color(0xFFF2C14E), Color(0xFFD9A63A)))
    val ChipLine = Color(0xFF8A6A20)
    val PhotoBorder = Color(0xFFB8CCE2)
    val Silhouette = Color(0xFF2B2F3A)
    val Ruler = Color(0xFF8A99AD)
    val RulerText = Color(0xFF6B7A90)
    val PhFill = Color(0xFFC9DCF0)     // placeholder wireframe
    val PhBorder = Color(0xFF8FB4DC)
}
