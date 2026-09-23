package th.go.banlat.kiosk.ui.service

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibleForward
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ชั่วคราวใช้ Material icon ไปก่อนเพื่อให้ประกอบหน้าจอได้
 * ของจริงให้ดีไซเนอร์ส่ง SVG มา แล้วแปลงเป็น vector drawable
 * เปลี่ยนที่นี่ที่เดียว หน้าจอไม่ต้องแก้
 */
object ServiceIcons {
    fun of(key: String): ImageVector = when (key) {
        "herb" -> Icons.Outlined.LocalFlorist
        "physio" -> Icons.Outlined.AccessibleForward
        "wound" -> Icons.Outlined.Vaccines
        "stetho" -> Icons.Outlined.MonitorHeart
        "brain" -> Icons.Outlined.Psychology
        "tooth" -> Icons.Outlined.Healing
        "lab" -> Icons.Outlined.Biotech
        "xray" -> Icons.Outlined.MedicalServices
        else -> Icons.Outlined.MedicalServices
    }
}
