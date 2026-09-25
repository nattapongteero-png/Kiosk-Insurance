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
/** brand = สีหลักจากโลโก้ · brandDeep = สีเข้มสำหรับตัวอักษรบนพื้นอ่อน — ใช้เปลี่ยนโทนหน้ารายละเอียดแบบประกันตามบริษัท */
data class Insurer(val name: String, val short: String, val logo: Int, val fit: FitMode, val brand: Long, val brandDeep: Long, val slow: Boolean = false)

/* โลโก้จาก Wikimedia Commons ใช้ในต้นแบบเท่านั้น (เมืองไทยประกันชีวิต: CC BY-SA 4.0 · ไทยประกันชีวิต / AIA: Public domain) */
val InsurerA = Insurer("บริษัท เมืองไทยประกันชีวิต จำกัด (มหาชน)", "MTL", R.drawable.ins_logo_a, FitMode.Contain, 0xFFE2007A, 0xFFB0005F)
val InsurerB = Insurer("บริษัท ไทยประกันชีวิต จำกัด (มหาชน)", "TL", R.drawable.ins_logo_b, FitMode.Cover, 0xFF0080C3, 0xFF005E94)
val InsurerC = Insurer("บริษัท เอไอเอ จำกัด", "AIA", R.drawable.ins_logo_c, FitMode.Contain, 0xFFD31145, 0xFFA00D34, slow = true) // slow = สาธิตกรณีส่งนาน

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

/* ---------- รายละเอียดแบบประกัน (ข้อมูลสมมติ) — TODO(integration): รับจาก API บริษัทประกันผ่านระบบกลาง BMS ---------- */
data class Terms(val age: String, val term: String, val wait: String, val pay: String, val exclusions: List<String>)
data class Cover(val label: String, val sub: String, val value: String)

val TermsByTag = mapOf(
    "ประกันสุขภาพ" to Terms("แรกเกิด 15 วัน – 70 ปี", "1 ปี ต่ออายุได้ถึง 99 ปี", "30 วัน (โรคทั่วไป) · 120 วัน (โรคเฉพาะ)", "รายปี / รายเดือน",
        listOf("โรคหรืออาการที่เป็นมาก่อนทำประกัน (ต้องแถลงสุขภาพ)", "การรักษาเพื่อความงามหรือศัลยกรรมตกแต่ง", "การตั้งครรภ์ การคลอดบุตร และภาวะแทรกซ้อน", "การบาดเจ็บจากการใช้สารเสพติดหรือแอลกอฮอล์")),
    "ประกันชีวิต" to Terms("1 เดือน – 65 ปี", "ตามระยะของแบบประกัน", "ไม่มี (คุ้มครองทันทีเมื่ออนุมัติ)", "รายปี / ราย 6 เดือน",
        listOf("การฆ่าตัวตายภายใน 1 ปีแรกนับจากวันเริ่มสัญญา", "การถูกฆาตกรรมโดยผู้รับประโยชน์", "การปกปิดข้อมูลสุขภาพที่ควรแถลง")),
    "ประกันอุบัติเหตุ" to Terms("6 – 70 ปี", "1 ปี ต่ออายุได้", "ไม่มี (คุ้มครองทันที)", "รายปี",
        listOf("การบาดเจ็บขณะเล่นกีฬาอันตราย เช่น ดำน้ำลึก กระโดดร่ม", "การบาดเจ็บขณะเมาสุราหรือใช้สารเสพติด", "สงคราม การจลาจล การก่อการร้าย")),
    "ประกันโรคร้ายแรง" to Terms("15 วัน – 65 ปี", "1 ปี ต่ออายุได้ถึง 85 ปี", "90 วัน นับจากวันเริ่มสัญญา", "รายปี",
        listOf("โรคร้ายแรงที่ตรวจพบภายในระยะเวลารอคอย", "โรคที่เป็นมาก่อนทำประกัน", "การติดเชื้อ HIV หรือโรคเอดส์")),
)

val CoverByPlan = mapOf(
    "Health Plus" to listOf(Cover("ค่ารักษาผู้ป่วยใน (IPD)", "สูงสุดต่อปี", "500,000 บาท"), Cover("ค่าห้องและค่าอาหาร", "ต่อวัน สูงสุด 365 วัน", "5,000 บาท"), Cover("ค่าผ่าตัดและหัตถการ", "", "ตามจ่ายจริง"), Cover("ค่ารักษาผู้ป่วยนอกหลังออกจากโรงพยาบาล", "ภายใน 30 วัน", "จ่ายจริง")),
    "Health Care Lite" to listOf(Cover("ค่ารักษาผู้ป่วยใน (IPD)", "สูงสุดต่อปี", "200,000 บาท"), Cover("ค่าห้องและค่าอาหาร", "ต่อวัน สูงสุด 180 วัน", "2,500 บาท"), Cover("ค่าผ่าตัดและหัตถการ", "ต่อครั้ง", "50,000 บาท")),
    "Life Protect 20" to listOf(Cover("ทุนประกันชีวิต", "กรณีเสียชีวิตทุกกรณี", "1,000,000 บาท"), Cover("ทุพพลภาพถาวรสิ้นเชิง", "", "1,000,000 บาท"), Cover("ระยะเวลาคุ้มครอง", "", "20 ปี"), Cover("ระยะเวลาชำระเบี้ย", "", "10 ปี")),
    "Cancer Care" to listOf(Cover("ตรวจพบมะเร็งระยะเริ่มต้น", "จ่ายครั้งเดียว", "250,000 บาท"), Cover("ตรวจพบมะเร็งระยะลุกลาม", "จ่ายครั้งเดียว", "1,000,000 บาท"), Cover("ค่ายาเคมีบำบัด / รังสีรักษา", "", "ตามจ่ายจริง")),
    "Accident Shield" to listOf(Cover("ค่ารักษาจากอุบัติเหตุ", "ต่อครั้ง", "100,000 บาท"), Cover("เสียชีวิต / ทุพพลภาพจากอุบัติเหตุ", "", "500,000 บาท"), Cover("ชดเชยรายได้ระหว่างนอนโรงพยาบาล", "ต่อวัน", "1,000 บาท")),
    "Critical Illness 30" to listOf(Cover("ตรวจพบโรคร้ายแรง 30 โรค", "จ่ายเงินก้อนครั้งเดียว", "1,000,000 บาท"), Cover("ตรวจพบโรคร้ายแรงระยะเริ่มต้น", "", "250,000 บาท"), Cover("ยกเว้นเบี้ยหลังตรวจพบ", "", "มี")),
    "Saving Life 15" to listOf(Cover("ทุนประกันชีวิต", "", "300,000 บาท"), Cover("เงินคืนระหว่างสัญญา", "ทุก 3 ปี", "2% ของทุน"), Cover("เงินครบกำหนดสัญญา", "ปีที่ 15", "120% ของทุน"), Cover("ลดหย่อนภาษี", "ตามที่กฎหมายกำหนด", "สูงสุด 100,000 บาท")),
    "OPD Care" to listOf(Cover("ค่ารักษาผู้ป่วยนอก (OPD)", "ต่อครั้ง", "1,500 บาท"), Cover("จำนวนครั้งที่คุ้มครอง", "ต่อปี", "30 ครั้ง"), Cover("ค่ายาและเวชภัณฑ์", "", "รวมในวงเงินต่อครั้ง")),
    "Senior Health 60+" to listOf(Cover("ค่ารักษาผู้ป่วยใน (IPD)", "สูงสุดต่อปี", "300,000 บาท"), Cover("ค่าห้องและค่าอาหาร", "ต่อวัน", "3,000 บาท"), Cover("ตรวจสุขภาพประจำปี", "", "1 ครั้ง/ปี")),
    "Life Care 99" to listOf(Cover("ทุนประกันชีวิต", "คุ้มครองถึงอายุ 99 ปี", "500,000 บาท"), Cover("เงินครบกำหนดสัญญา", "เมื่ออายุ 99 ปี", "500,000 บาท"), Cover("ระยะเวลาชำระเบี้ย", "", "20 ปี")),
    "Family Health" to listOf(Cover("ค่ารักษาผู้ป่วยใน (IPD)", "ต่อคน ต่อปี", "300,000 บาท"), Cover("จำนวนสมาชิกที่คุ้มครอง", "", "สูงสุด 4 คน"), Cover("ค่าห้องและค่าอาหาร", "ต่อวัน", "3,000 บาท"), Cover("ค่ารักษาผู้ป่วยนอก (OPD)", "ต่อครั้ง", "1,000 บาท")),
)

/* ---------- ผลตรวจสอบสิทธิ (ต้นแบบ) — TODO(integration): NHSO Smartcard Agent / Web Service สปสช. · Authen Code จาก nhsoendpoint ---------- */
data class Rights(val main: String, val sub: String, val hosp: String, val hosp2: String, val authen: String?)
val RightsOk = Rights("สิทธิหลักประกันสุขภาพแห่งชาติ (UC)", "—", "โรงพยาบาลบ้านลาด", "—", "PP1234567890")
val RightsFail = Rights("สิทธิประกันสังคม", "เบิกกองทุนประกันสังคม (ผู้ประกันตน)", "โรงพยาบาลขอนแก่น", "—", null)

/** สถานะของผู้ใช้คนปัจจุบัน · ต้นแบบ: เสียบบัตร = สิทธิใช้ได้ · กรอก HN/เลขบัตร = ใช้ที่นี่ไม่ได้ (สาธิตทั้ง 2 กรณี) */
object DemoSession {
    var rightsOk = true
    var selfPay = false
    val rights get() = if (rightsOk) RightsOk else RightsFail
    /** สิทธิที่แสดงในแถบผู้ป่วย/บัตรคิว */
    val rightLabel get() = when { selfPay -> "ชำระเงินเอง"; rightsOk -> DemoPatient.right; else -> "ประกันสังคม" }
    /** ชื่อสิทธิเต็มในแถบผู้ป่วย (แถวเต็มความกว้าง) */
    val rightFull get() = if (selfPay) "ชำระเงินเอง" else rights.main
}
