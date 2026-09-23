package th.go.banlat.kiosk.model

/** บริการหนึ่งปุ่ม — โครงสร้างตรงกับที่ API ส่งมา */
data class ServiceItem(
    val code: String,
    val label: String,
    /** คำอธิบายใต้ชื่อ เช่น "สำหรับผู้ที่มาก่อนเวลานัด" */
    val note: String? = null,
    /** key ของไอคอน map กับ ServiceIcons */
    val icon: String,
    /** blue | teal | amber | violet */
    val accent: String = "blue",
    val group: ServiceGroup = ServiceGroup.General,
)

enum class ServiceGroup(val title: String) {
    General("บริการทั่วไป"),
    EarlyArrival("มาก่อนเวลานัด"),
}

/** ข้อมูลผู้ป่วยที่อ่านจากบัตรประชาชน */
data class PatientInfo(
    val fullName: String,
    val maskedCitizenId: String,
    val hn: String,
    val ageText: String,
    val insuranceRight: String,
)
