package th.go.banlat.kiosk

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import th.go.banlat.kiosk.data.RecommendedPlans
import th.go.banlat.kiosk.ui.insurance.InsuranceFlow
import th.go.banlat.kiosk.ui.insurance.SummaryScreen
import th.go.banlat.kiosk.ui.theme.KioskFrame
import th.go.banlat.kiosk.ui.welcome.WelcomeScreen

/*
 * Preview ในขนาดจอตู้ 32 นิ้ว แนวตั้ง 1080 x 1920 (density 160 แบบจอ signage ทั่วไป)
 * เปิดไฟล์นี้ใน Android Studio แล้วกด Split / Design เพื่อดูทุกหน้าพร้อมกัน
 */
private const val KIOSK_32 = "spec:width=1080px,height=1920px,dpi=160,orientation=portrait"

@Preview(name = "1 หน้าแรก", device = KIOSK_32, group = "ตู้ 32 นิ้ว")
@Composable private fun PreviewWelcome() = KioskFrame { WelcomeScreen(onConsentAccepted = {}) }

@Preview(name = "2 เลือกแบบประกัน", device = KIOSK_32, group = "ตู้ 32 นิ้ว")
@Composable private fun PreviewSelect() = KioskFrame { InsuranceFlow(onExit = {}) }

@Preview(name = "3 ส่งสำเร็จ", device = KIOSK_32, group = "ตู้ 32 นิ้ว")
@Composable private fun PreviewSent() = KioskFrame { SummaryScreen(RecommendedPlans[0], slow = false, onDone = {}) }

@Preview(name = "4 ส่งนาน รับผลทางแอป", device = KIOSK_32, group = "ตู้ 32 นิ้ว")
@Composable private fun PreviewSlow() = KioskFrame { SummaryScreen(RecommendedPlans[2], slow = true, onDone = {}) }

@Preview(name = "2.1 รายละเอียดแบบประกัน", device = KIOSK_32, group = "ตู้ 32 นิ้ว")
@Composable private fun PreviewDetail() = KioskFrame { th.go.banlat.kiosk.ui.insurance.DetailScreen(RecommendedPlans[0], onBack = {}, onSend = {}) }

/* ---------- ตรวจ responsive: จอสัดส่วนอื่น ---------- */
@Preview(name = "มือถือ 9:20 · หน้าแรก", device = "spec:width=1080px,height=2400px,dpi=420", group = "Responsive")
@Composable private fun PreviewTallWelcome() = KioskFrame { WelcomeScreen(onConsentAccepted = {}) }

@Preview(name = "แท็บเล็ต 10:16 · เลือกแบบประกัน", device = "spec:width=1200px,height=1920px,dpi=240", group = "Responsive")
@Composable private fun PreviewTabletSelect() = KioskFrame { InsuranceFlow(onExit = {}) }

@Preview(name = "แท็บเล็ต 10:16 · หน้าแรก", device = "spec:width=1200px,height=1920px,dpi=240", group = "Responsive")
@Composable private fun PreviewTabletWelcome() = KioskFrame { WelcomeScreen(onConsentAccepted = {}) }

@Preview(name = "5 เลือกบริการ (ระบบลงทะเบียน)", device = KIOSK_32, group = "ตู้ 32 นิ้ว")
@Composable private fun PreviewServices() = KioskFrame { th.go.banlat.kiosk.ui.services.ServicesScreen(onExit = {}) }
