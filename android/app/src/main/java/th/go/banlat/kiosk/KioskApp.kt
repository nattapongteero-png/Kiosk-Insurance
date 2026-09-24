package th.go.banlat.kiosk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import th.go.banlat.kiosk.ui.insurance.InsuranceFlow
import th.go.banlat.kiosk.ui.theme.KioskFrame
import th.go.banlat.kiosk.ui.welcome.WelcomeScreen

/**
 * เส้นทางหลักของตู้ (ต้นแบบ)
 * หน้าแรก → ยืนยันตัวตน → ยินยอม → เลือกแบบประกัน → ส่งข้อมูล → สรุปผล → กลับหน้าแรก
 * ทีมพัฒนาเปลี่ยนจุดที่ใส่ความเห็น "TODO(integration)" ให้เรียกระบบจริงได้เลย
 */
private enum class Route { Welcome, Insurance }

@Composable
fun KioskApp() {
    var route by remember { mutableStateOf(Route.Welcome) }
    KioskFrame {
        when (route) {
            Route.Welcome -> WelcomeScreen(onConsentAccepted = { route = Route.Insurance })
            Route.Insurance -> InsuranceFlow(onExit = { route = Route.Welcome })
        }
    }
}

