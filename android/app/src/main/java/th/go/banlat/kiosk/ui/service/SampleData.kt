package th.go.banlat.kiosk.ui.service

import th.go.banlat.kiosk.model.PatientInfo
import th.go.banlat.kiosk.model.ServiceGroup
import th.go.banlat.kiosk.model.ServiceItem

/** ข้อมูลจำลองไว้ดู @Preview ก่อนต่อ API จริง */
object SampleData {
    val patient = PatientInfo(
        fullName = "ฐากูร ทดสอบ",
        maskedCitizenId = "14XXXXXXXX34",
        hn = "000890314",
        ageText = "26 ปี 7 เดือน",
        insuranceRight = "ชำระเงินเอง",
    )

    val services = listOf(
        ServiceItem("TTM", "แพทย์แผนไทย", icon = "herb", accent = "teal"),
        ServiceItem("PT", "กายภาพบำบัด", icon = "physio", accent = "teal"),
        ServiceItem("INJ", "ฉีดยา ทำแผล", icon = "wound", accent = "blue"),
        ServiceItem("OPD", "ตรวจโรคทั่วไป", icon = "stetho", accent = "blue"),
        ServiceItem("PSY", "จิตเวช", icon = "brain", accent = "violet"),
        ServiceItem("DENT", "ทันตกรรม", icon = "tooth", accent = "violet"),
        ServiceItem("MED", "อายุรกรรม", icon = "stetho", accent = "blue"),
        ServiceItem(
            "LAB", "เจาะเลือด (LAB)", note = "สำหรับผู้ที่มาก่อนเวลานัด",
            icon = "lab", accent = "amber", group = ServiceGroup.EarlyArrival
        ),
        ServiceItem(
            "XRAY", "เอกซเรย์ (X-RAY)", note = "สำหรับผู้ที่มาก่อนเวลานัด",
            icon = "xray", accent = "amber", group = ServiceGroup.EarlyArrival
        ),
    )
}
