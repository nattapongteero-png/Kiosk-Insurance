package th.go.banlat.kiosk.data

import th.go.banlat.kiosk.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/*
 * ข้อมูลจำลองสำหรับต้นแบบ — TODO(integration): แทนด้วยข้อมูลจากเครื่องอ่านบัตร / HIS / ระบบกลาง BMS
 */

data class Patient(val name: String, val cid: String, val dob: LocalDate, val hn: String, val right: String)

val DemoPatient = Patient("ฐากูร ทดสอบ", "1103700123123", LocalDate.of(2000, 6, 11), "000890314", "บัตรทอง (UC)")

/** เลขบัตร 13 หลักแสดงบนจอเฉพาะ 3 หลักท้าย เพราะจอตู้อยู่ในที่สาธารณะ: X-XXXX-XXXXX-12-3 */
fun maskCid(c: String) = "X-XXXX-XXXXX-${c.substring(10, 12)}-${c.substring(12)}"

/** อายุเป็น ปี เดือน วัน · วันเกิด 29–31 ในเดือนที่สั้นกว่า ถือวันสุดท้ายของเดือนเป็นวันครบเดือน */
fun ageYMD(dob: LocalDate, today: LocalDate = LocalDate.now()): String {
    var months = ChronoUnit.MONTHS.between(dob.withDayOfMonth(1), today.withDayOfMonth(1)).toInt()
    fun anchor(n: Int): LocalDate = dob.plusMonths(n.toLong())   // plusMonths ปัดวันเกินเป็นวันสุดท้ายของเดือนให้เอง
    if (anchor(months).isAfter(today)) months -= 1
    val days = ChronoUnit.DAYS.between(anchor(months), today)
    return "${months / 12} ปี ${months % 12} เดือน $days วัน"
}

enum class FitMode { Contain, Cover }
data class Insurer(val name: String, val short: String, val logo: Int, val fit: FitMode, val slow: Boolean = false)

/* โลโก้จาก Wikimedia Commons ใช้ในต้นแบบเท่านั้น (เมืองไทยประกันชีวิต: CC BY-SA 4.0 · ไทยประกันชีวิต / AIA: Public domain) */
val InsurerA = Insurer("บริษัท เมืองไทยประกันชีวิต จำกัด (มหาชน)", "MTL", R.drawable.ins_logo_a, FitMode.Contain)
val InsurerB = Insurer("บริษัท ไทยประกันชีวิต จำกัด (มหาชน)", "TL", R.drawable.ins_logo_b, FitMode.Cover)
val InsurerC = Insurer("บริษัท เอไอเอ จำกัด", "AIA", R.drawable.ins_logo_c, FitMode.Contain, slow = true) // slow = สาธิตกรณีส่งนาน

data class Plan(val co: Insurer, val tag: String, val name: String, val cov: String, val price: String)

val RecommendedPlans = listOf(
    Plan(InsurerA, "ประกันสุขภาพ", "Health Plus", "ค่ารักษาผู้ป่วยใน สูงสุด 500,000 บาท/ปี", "12,000"),
    Plan(InsurerB, "ประกันสุขภาพ", "Health Care Lite", "ค่ารักษาผู้ป่วยใน สูงสุด 200,000 บาท/ปี", "6,500"),
    Plan(InsurerC, "ประกันชีวิต", "Life Protect 20", "ทุนประกัน 1,000,000 บาท ระยะ 20 ปี", "18,000"),
)
val OtherPlans = listOf(
    Plan(InsurerB, "ประกันสุขภาพ", "Cancer Care", "คุ้มครองโรคมะเร็งทุกระยะ สูงสุด 1,000,000 บาท", "3,200"),
    Plan(InsurerC, "ประกันอุบัติเหตุ", "Accident Shield", "ค่ารักษาจากอุบัติเหตุ สูงสุด 100,000 บาท/ครั้ง", "1,800"),
    Plan(InsurerA, "ประกันโรคร้ายแรง", "Critical Illness 30", "จ่ายเงินก้อนเมื่อตรวจพบ 30 โรคร้ายแรง", "7,400"),
    Plan(InsurerC, "ประกันชีวิต", "Saving Life 15", "ออมทรัพย์พร้อมคุ้มครองชีวิต ระยะ 15 ปี", "24,000"),
    Plan(InsurerA, "ประกันสุขภาพ", "OPD Care", "ค่ารักษาผู้ป่วยนอก ครั้งละ 1,500 บาท 30 ครั้ง/ปี", "4,900"),
    Plan(InsurerB, "ประกันสุขภาพ", "Senior Health 60+", "ค่ารักษาผู้ป่วยใน สูงสุด 300,000 บาท/ปี", "21,500"),
    Plan(InsurerC, "ประกันชีวิต", "Life Care 99", "คุ้มครองชีวิตถึงอายุ 99 ปี ทุน 500,000 บาท", "15,800"),
    Plan(InsurerA, "ประกันสุขภาพ", "Family Health", "คุ้มครองทั้งครอบครัว สูงสุด 4 คน", "28,000"),
)

const val RequestId = "INS-2569-000123"

private val ThaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
    "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
fun thaiDate(d: LocalDate) = "${d.dayOfMonth} ${ThaiMonths[d.monthValue - 1]} ${d.year + 543}"
