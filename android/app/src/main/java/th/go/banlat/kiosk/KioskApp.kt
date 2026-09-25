package th.go.banlat.kiosk

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import th.go.banlat.kiosk.ui.theme.s
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import th.go.banlat.kiosk.ui.common.IDLE_SECONDS
import th.go.banlat.kiosk.ui.common.IDLE_WARN_SECONDS
import th.go.banlat.kiosk.ui.common.IdleWarning
import th.go.banlat.kiosk.ui.insurance.InsuranceFlow
import th.go.banlat.kiosk.ui.services.ServicesScreen
import th.go.banlat.kiosk.ui.theme.KioskFrame
import th.go.banlat.kiosk.ui.welcome.WelcomeScreen

/**
 * เส้นทางหลักของตู้ (ต้นแบบ)
 * หน้าแรก → ยืนยันตัวตน → ยินยอม → เลือกแบบประกัน → ส่งข้อมูล → สรุปผล → ระบบลงทะเบียน (เลือกบริการ)
 * ไม่ยินยอมประกัน → ระบบลงทะเบียนได้เลย (ประกันไม่ใช่เงื่อนไขการรับบริการ)
 * ไม่แตะจอรวม 60 วิ → กลับหน้าแรก + ล้างข้อมูล (แตะ = นับใหม่) · 20 วิสุดท้ายเด้งเตือนนับถอยหลัง
 * หน้าแรกนับเฉพาะตอนมีชั้นซ้อนเปิด (ตรงกับ preview/idle.js)
 * ทีมพัฒนาเปลี่ยนจุดที่ใส่ความเห็น "TODO(integration)" ให้เรียกระบบจริงได้เลย
 */
private enum class Route { Welcome, Insurance, Services }

@Composable
fun KioskApp() {
    var route by remember { mutableStateOf(Route.Welcome) }
    var homeKey by remember { mutableIntStateOf(0) }          // เปลี่ยน = สร้างหน้าแรกใหม่ (ล้างชั้นซ้อน/ข้อมูลค้าง)
    var bye by remember { mutableStateOf<String?>(null) }     // "cancel" / "idle" = แจ้งผลบนหน้าแรก
    var homeBusy by remember { mutableStateOf(false) }        // หน้าแรกมีชั้นซ้อนเปิดอยู่
    var lastTouch by remember { mutableLongStateOf(SystemClock.uptimeMillis()) }
    var warn by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(SystemClock.uptimeMillis()) }   // เวลาปัจจุบัน (อัปเดตทุก 250 มิลลิวินาที)

    // TODO(integration): ล้างข้อมูลผู้ป่วยที่อ่านจากบัตร / session
    fun goHome(reason: String) { warn = false; route = Route.Welcome; homeKey++; bye = reason; lastTouch = SystemClock.uptimeMillis() }

    val active = route != Route.Welcome || homeBusy
    LaunchedEffect(active) {
        lastTouch = SystemClock.uptimeMillis()
        while (active) {
            delay(250)
            now = SystemClock.uptimeMillis()
            if (!warn && now - lastTouch >= (IDLE_SECONDS - IDLE_WARN_SECONDS) * 1000L) warn = true
        }
        warn = false
    }

    KioskFrame {
        // ทุกการแตะ (ดักก่อนถึงปุ่ม ไม่กินเหตุการณ์) = รีเซ็ตเวลา
        Box(Modifier.fillMaxSize().pointerInput(Unit) {
            awaitPointerEventScope { while (true) { awaitPointerEvent(PointerEventPass.Initial); if (!warn) lastTouch = SystemClock.uptimeMillis() } }
        }) {
            when (route) {
                Route.Welcome -> key(homeKey) {
                    WelcomeScreen(onConsentAccepted = { route = Route.Insurance }, onConsentDeclined = { route = Route.Services },
                        bye = bye, onByeShown = { bye = null }, onBusy = { homeBusy = it }, onCancel = { goHome("cancel") })
                }
                Route.Insurance -> InsuranceFlow(onExit = { route = Route.Services })
                // หลังเลือกลักษณะการมา หน้าเลือกบริการแสดงบัตรคิวเอง · TODO(integration): onQueued → ส่ง HIS แล้วสั่งพิมพ์บัตรคิว
                Route.Services -> ServicesScreen(onExit = { route = Route.Welcome })
            }
            if (warn) IdleWarning(onContinue = { warn = false; lastTouch = SystemClock.uptimeMillis() }, onHome = { goHome("idle") })
        }
    }
}
