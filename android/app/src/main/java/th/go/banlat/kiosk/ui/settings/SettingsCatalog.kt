package th.go.banlat.kiosk.ui.settings

/* หน้าตั้งค่าตู้ V.24.09.69 — ทุกรายการจากหน้าตั้งค่าเดิม (ภาพ 9 ภาพ) จัดกลุ่มใหม่ 9 หมวด
 * ไฟล์นี้สร้างจากรายการเดียวกับ preview/settings.html (สคริปต์ gen_settings.py) · แก้รายการที่นี่ให้ตรงกับ HTML
 * รายการซ้ำในหน้าเดิม (Scan HN / Scan ใบหน้า / Mobile Application / ยืนยันครั้งที่ 2 / ชื่อเครื่องพิมพ์ใบนำทาง) รวมเหลืออันเดียว
 * TODO(integration): ผูกแต่ละรายการกับคีย์ในตาราง setting_kiosk */

sealed interface SettingItem { val label: String; val hint: String }
data class ToggleItem(override val label: String, val on: Boolean, override val hint: String = "") : SettingItem
data class ValueItem(override val label: String, val value: String, override val hint: String = "") : SettingItem
data class ActionItem(override val label: String, override val hint: String = "") : SettingItem
data class SettingSection(val title: String, val items: List<SettingItem>)
data class SettingGroup(val id: String, val name: String, val desc: String, val icon: List<String>, val sections: List<SettingSection>)

val SettingGroups = listOf(
    SettingGroup("home", "หน้าแรกและการยืนยันตัวตน", "ปุ่มบนหน้าแรก กลับหน้าแรกอัตโนมัติ ข้อความประกาศ Banner",
        listOf("M4 11 12 4l8 7v8a1 1 0 0 1-1 1h-4v-6H9v6H5a1 1 0 0 1-1-1z"), listOf(
        SettingSection("ช่องทางยืนยันตัวตนบนหน้าแรก", listOf(
            ToggleItem("แสดงปุ่ม \"เลขบัตรประชาชน\"", true, "เดิม: ปิดการใช้งานไปยังหน้าจอใส่เลขบัตรประชาชน"),
            ToggleItem("แสดงปุ่ม \"สแกน / กรอก HN\"", true, "เดิม: ปิด / เปิดการใช้งาน Scan HN (ซ้ำ 2 จุด รวมเป็นอันเดียว)"),
            ToggleItem("แสดงปุ่ม \"สแกนใบหน้า\"", true, "เดิม: ปิด / เปิดการใช้งาน Scan ใบหน้า (ซ้ำ 2 จุด รวมเป็นอันเดียว)"),
            ToggleItem("เมื่อกลับหน้าแรกให้แสดงหน้าจอ Scan HN เสมอ", false, ""),
            ToggleItem("ลงทะเบียนจาก Mobile Application", false, "เดิมมี 2 จุด รวมเป็นอันเดียว"),
        )),
        SettingSection("กลับหน้าแรกอัตโนมัติ", listOf(
            ToggleItem("กลับหน้าแรกอัตโนมัติขณะเลือกเมนู", false, ""),
            ValueItem("ระยะเวลาในการกลับไปหน้าแรก", "60", "วินาที"),
            ValueItem("ระยะเวลากลับหน้าแรกหลังส่งตรวจ", "", "วินาที"),
        )),
        SettingSection("ข้อความและรูปบนหน้าจอ", listOf(
            ValueItem("ข้อความประกาศ", "", ""),
            ValueItem("รูปภาพ Banner", "hdr_banlat.png", "แตะเพื่อเลือกรูปใหม่"),
        )),
        SettingSection("ข้อมูลจากบัตรประชาชน", listOf(
            ToggleItem("ดึงรูปภาพจากบัตรประชาชน", false, ""),
            ToggleItem("อัพเดทรูปภาพจากการอ่านบัตรประชาชน", false, ""),
            ToggleItem("ปิดการตรวจสอบความถูกต้องรหัส ปชช.", false, ""),
            ToggleItem("เปลี่ยนคำนำหน้า กรณีเด็กอายุตั้งแต่ 15 ปี", false, ""),
        )),
    )),
    SettingGroup("queue", "การส่งตรวจและออกคิว", "ออกคิว visit ใบนำทาง เบอร์โทร จำกัดจำนวน",
        listOf("M4 7h16v3a2 2 0 0 0 0 4v3H4v-3a2 2 0 0 0 0-4z", "M14.5 7v10"), listOf(
        SettingSection("การออกคิว", listOf(
            ToggleItem("เลือก Queue Slot จากการนัดหมาย", false, ""),
            ToggleItem("ออกคิวคัดกรอง (qs-slot)", false, ""),
            ToggleItem("บันทึกออกคิวการส่งตรวจ visit เดิมที่ส่งตรวจแล้ว", false, ""),
            ToggleItem("บันทึกออกคิวการส่งตรวจหลาย visit ที่ส่งตรวจแล้ว", false, ""),
            ToggleItem("ปิดปุ่ม \"ออกคิวรายการที่เลือก\"", false, ""),
            ToggleItem("ปิดการยืนยันครั้งที่ 2 เมื่อเลือกคิว", false, "เดิมมี 2 จุด รวมเป็นอันเดียว"),
        )),
        SettingSection("ขั้นตอนส่งตรวจ", listOf(
            ToggleItem("ให้เลือกลักษณะการมาของผู้ป่วยหลังเลือกเมนู", true, "เดิม: เลือกประเภทสภาพผู้ป่วยหลังเลือกเมนู"),
            ToggleItem("ลงข้อมูลเบอร์โทรศัพท์ทุกครั้งที่ส่งตรวจ", false, ""),
            ToggleItem("ใช้ Template แสดงเมนูส่งตรวจตามวัน", false, ""),
            ToggleItem("จำกัดจำนวนการส่งตรวจในวัน", false, ""),
            ToggleItem("เช็ควันหยุด เพื่อตรวจสอบเวลาทำการ", false, ""),
            ToggleItem("ออกใบนำทางใหม่ตาม visit ที่เลือก", false, ""),
            ToggleItem("เปิด visit แล้วเช็คประวัติยาเก่า", false, ""),
            ValueItem("ชื่อเครื่องที่ใช้เรียกเมนูห้องส่งตรวจ", "ab", ""),
        )),
        SettingSection("การจัดส่งยา", listOf(
            ToggleItem("ยืนยันจัดส่งยาหลังส่งตรวจ", false, ""),
        )),
    )),
    SettingGroup("rights", "ตรวจสอบสิทธิการรักษา", "สปสช. · Smartcard Agent · เงื่อนไขสิทธิ · ชำระเงินเอง",
        listOf("M12 3 5 6v5.5c0 4.3 2.9 8.2 7 9.5 4.1-1.3 7-5.2 7-9.5V6l-7-3Z", "m9 12 2.2 2.2L15.5 10"), listOf(
        SettingSection("การใช้งาน NHSO Secure Smartcard Agent", listOf(
            ToggleItem("ใช้งาน NHSO Secure Smartcard Agent", true, ""),
            ToggleItem("เช็คสิทธิผ่าน Smartcard Agent", true, ""),
            ValueItem("IP Service Smartcard", "10.91.118.43", ""),
        )),
        SettingSection("การเช็คสิทธิ สปสช.", listOf(
            ToggleItem("ปิดการตรวจสอบสิทธิรักษาผ่าน สปสช.", false, ""),
            ToggleItem("เช็ค Web Service จาก สปสช. โดยตรง", false, ""),
            ToggleItem("ปิดตรวจสอบผ่าน สปสช. และใช้สิทธิติดตัวของผู้รับบริการ", false, ""),
            ToggleItem("เช็คสิทธิ สปสช. ผ่าน api srm", false, ""),
            ToggleItem("เช็คสิทธิ สปสช. RealPerson", false, ""),
            ToggleItem("ปิดการตรวจสอบการเสียชีวิตผ่าน api สปสช.", true, ""),
            ToggleItem("ตรวจสอบ Moph Claim-NHSO", false, ""),
            ToggleItem("เช็คสิทธิที่ประสงค์ขอเบิกชดเชยจากกองทุน สปสช.", false, "กรณีไม่เลือกประสงค์เบิก"),
            ToggleItem("อัพเดทสิทธิการรักษาคนไข้", false, ""),
        )),
        SettingSection("การใช้งานเช็คสิทธิตามเงื่อนไข ตามตั้งค่า", listOf(
            ToggleItem("เช็คสิทธิตามเงื่อนไข", true, ""),
            ToggleItem("เช็ค HMain กับ csv", false, ""),
            ValueItem("รหัสสถานพยาบาล", "99999", ""),
            ToggleItem("ตรวจสอบสถานพยาบาลรอง", false, ""),
            ValueItem("รหัสสถานพยาบาลรอง", "XXXXX", ""),
            ToggleItem("ไม่เช็ค ThaiRefer", true, ""),
            ToggleItem("แสดงปุ่มเช็คผล", false, "ไม่ใช้งานให้ปิดเสมอ"),
        )),
        SettingSection("การตรวจสอบสิทธิจากการส่งตรวจล่าสุดในวัน", listOf(
            ToggleItem("ตรวจสอบสิทธิจากการส่งตรวจล่าสุดในวัน", false, ""),
            ValueItem("รหัสสิทธิที่ใช้ตรวจสอบ", "XXXXXXX", ""),
        )),
        SettingSection("Refer / ส่งต่อ", listOf(
            ToggleItem("ตรวจสอบสิทธิ UC นอกเขต ผ่านทะเบียน Refer IN", false, ""),
            ToggleItem("ไม่ใช้สิทธิการรักษาจากทะเบียน Refer In", false, ""),
            ToggleItem("ตรวจสอบ MOPH Refer", false, ""),
        )),
        SettingSection("การตรวจสอบสิทธิ พ.ร.บ.", listOf(
            ToggleItem("ตรวจสอบด้วยสิทธิ พ.ร.บ. ที่กำหนด", false, ""),
            ValueItem("รหัสสิทธิ พ.ร.บ. ที่ใช้ตรวจสอบ", "XX", ""),
            ToggleItem("เช็คสิทธิประกันอุบัติเหตุ", false, ""),
        )),
        SettingSection("การตรวจสอบสิทธิบุคคลต่างด้าว", listOf(
            ToggleItem("กำหนดสิทธิให้บุคคลต่างด้าว", false, ""),
            ValueItem("รหัสสิทธิที่ออกให้บุคคลต่างด้าว", "07", ""),
            ToggleItem("ส่งตรวจคนไข้กลุ่มต่างชาติโดยให้สิทธิชำระเงิน", false, ""),
        )),
        SettingSection("ชำระเงินเอง", listOf(
            ToggleItem("เมื่อได้สิทธิชำระเงินเอง ไม่ให้ส่งตรวจ", false, ""),
            ToggleItem("เมื่อสิทธิชำระเงินเอง เปลี่ยนสถานะเป็น \"ตรวจสอบสิทธิ\"", false, ""),
            ToggleItem("ส่งตรวจแล้วกำหนดสถานะเป็น \"รอตรวจสอบสิทธิ\" ทุกกรณี", false, ""),
        )),
    )),
    SettingGroup("authen", "Authen Code สปสช.", "ออก Authen Code และการพิมพ์ใบนำทาง",
        listOf("M14.5 3.5a6 6 0 1 1-5.2 9L4 17.8V20.5h2.7v-2h2v-2h2l1.2-1.2A6 6 0 0 1 14.5 3.5Z", "M16.5 7.5h.01"), listOf(
        SettingSection("การออก Authen Code สปสช.", listOf(
            ToggleItem("ออก Authen Code สปสช.", true, ""),
            ToggleItem("ออกผ่าน pucws", false, ""),
            ToggleItem("ออกผ่าน nhsoendpoint", true, ""),
            ToggleItem("ใช้ API Test Zone", false, ""),
            ToggleItem("ออก Authen Code ใหม่ กรณี visit ในวันยังไม่เคยออก", false, ""),
        )),
        SettingSection("ใบนำทาง", listOf(
            ToggleItem("ปิดการพิมพ์ใบนำทาง กรณีออก Authen Code ใหม่", false, ""),
            ValueItem("ชื่อเครื่องเมนูพิมพ์ใบนำทาง กรณีออก Authen Code ใหม่", "XXXXXXXXXXXX", "เดิมมี 2 จุด รวมเป็นอันเดียว"),
        )),
    )),
    SettingGroup("appt", "นัดหมาย", "ส่งตรวจตามนัด ช่วงเวลานัด MOPH Appointment",
        listOf("M4 6.5A1.5 1.5 0 0 1 5.5 5h13A1.5 1.5 0 0 1 20 6.5v12a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5z", "M4 10h16M8 3v4M16 3v4"), listOf(
        SettingSection("การส่งตรวจนัดหมาย", listOf(
            ToggleItem("ใช้ข้อมูลนัดหมายให้เลือกเปิด visit", true, ""),
            ToggleItem("ยืนยันส่งตรวจนัดหมายอัตโนมัติ", false, ""),
            ToggleItem("ส่งตรวจนัดใช้สิทธิบังคับจากเมนู Kiosk", false, ""),
            ToggleItem("ออกคิวซักประวัติตามคลินิกที่นัดหมาย", false, ""),
            ToggleItem("ออกคิวซักประวัติตามห้องตรวจที่นัด", false, ""),
            ToggleItem("ส่งตรวจพร้อมกันได้หลายคลินิกตามนัดในวัน", false, ""),
            ValueItem("ระยะหน่วงเวลา กรณีส่งตรวจนัดมากกว่า 1 นัด", "", "วินาที"),
        )),
        SettingSection("การส่งตรวจนัดหมายตามช่วงเวลานัดหมาย", listOf(
            ToggleItem("ส่งตรวจนัดหมายเช็คตามช่วงเวลานัด", false, ""),
            ToggleItem("ส่งตรวจนัดหมายเช็คหลังช่วงเวลานัด", false, ""),
            ToggleItem("ตรวจสอบเวลานัดจากการจอง slot นัด", false, ""),
            ToggleItem("ส่งตรวจนัดกรณีมี Lab หรือ X-ray ไม่สนช่วงเวลา", false, ""),
            ValueItem("ระยะเวลาก่อนมารับบริการ", "30", "นาที"),
            ValueItem("ระยะเวลาหลังเวลานัดหมาย", "90", "นาที"),
        )),
        SettingSection("การตรวจสอบประเภทการมารับบริการ กรณีมาก่อนเวลาทำการ", listOf(
            ToggleItem("ตรวจสอบประเภทการมา กรณีมาก่อนเวลาทำการ (นัดหมาย)", false, ""),
            ToggleItem("ตรวจสอบประเภทการมา กรณีมาก่อนเวลาทำการ (Walk-in)", false, ""),
        )),
        SettingSection("การตรวจสอบนัดหมาย MOPH API Specification", listOf(
            ToggleItem("ตรวจสอบนัดหมาย", false, ""),
            ValueItem("เมนูของห้องในการส่งตรวจนัดหมาย (ID)", "XXXX", ""),
        )),
        SettingSection("MOPH Appointment", listOf(
            ToggleItem("นัดหมายออนไลน์ MOPH Appointment", false, ""),
            ValueItem("ชื่อเครื่องเมนูส่งตรวจนัดหมายออนไลน์", "ab", ""),
        )),
        SettingSection("การส่งตรวจจากการนัดส่งต่อจาก รพช.", listOf(
            ToggleItem("ส่งตรวจจากการนัดส่งต่อจาก รพช.", false, ""),
            ValueItem("ชื่อเครื่องที่เรียกเมนูส่งตรวจนัดส่งต่อ", "", "ชื่อเครื่องของ Menu Kiosk"),
            ToggleItem("ไม่เช็ค ThaiRefer (นัดส่งต่อ)", false, ""),
        )),
        SettingSection("การส่งตรวจกลุ่มเป้าหมายวัคซีนโควิด-19", listOf(
            ToggleItem("แสดงกลุ่มเป้าหมายวัคซีนโควิด-19", false, ""),
            ValueItem("ผู้ใช้และรหัสผ่านยืนยันเช็คกลุ่มเป้าหมาย", "", "ตั้งค่าผู้ใช้งาน"),
            ValueItem("ตรงตามนัด ส่งตรวจไปยังห้อง (depcode)", "", ""),
            ValueItem("ไม่ตรงตามนัด ส่งตรวจไปยังห้อง (depcode)", "", ""),
        )),
    )),
    SettingGroup("lab", "Lab / X-ray และค่าใช้จ่าย", "การสั่ง Lab X-ray ค่าใช้จ่ายเพิ่ม ค่าธรรมเนียมนอกเวลา",
        listOf("M9 3h6M10 3v6.5L4.8 18.2A1.8 1.8 0 0 0 6.3 21h11.4a1.8 1.8 0 0 0 1.5-2.8L14 9.5V3", "M7.5 15h9"), listOf(
        SettingSection("การสั่ง Lab และ X-ray", listOf(
            ToggleItem("ปิดการสั่ง Lab กรณีมีการสั่งจากนัดส่งตรวจ", false, ""),
            ToggleItem("ปิดการสั่ง X-ray กรณีมีการสั่งจากนัดส่งตรวจ", false, ""),
            ToggleItem("ปิดการคิดค่าใช้จ่ายการสั่ง Lab จากนัดส่งตรวจ", false, ""),
            ToggleItem("ปิดการคิดค่าใช้จ่ายการสั่ง X-ray จากนัดส่งตรวจ", false, ""),
            ToggleItem("ตรวจสอบเมนูสั่ง Lab ล่วงหน้าไม่ตรงวันนัด", false, ""),
            ToggleItem("ตรวจสอบเมนูสั่ง X-ray ล่วงหน้าไม่ตรงวันนัด", false, ""),
            ToggleItem("ยืนยันการสั่ง Lab ล่วงหน้าทั้งหมดภายในวัน", false, ""),
            ToggleItem("ออก xn ในรูปแบบ HOSxP V.3", false, ""),
            ToggleItem("บันทึกข้อมูล PACS รูปแบบ HOSxP V.3", false, ""),
        )),
        SettingSection("ค่าใช้จ่ายเพิ่มเติมตามเมนูส่งตรวจ", listOf(
            ToggleItem("คิดค่าใช้จ่ายเพิ่มเติมตามเมนูส่งตรวจ", false, ""),
            ValueItem("รหัสการชำระเงิน", "", "ถ้าไม่ใส่จะใช้ 03"),
            ToggleItem("ใช้รหัสการชำระเงินตามสิทธิส่งตรวจ", false, ""),
            ToggleItem("คิดค่าธรรมเนียมจากสิทธิการรักษา", false, ""),
        )),
        SettingSection("ค่าธรรมเนียมตรวจนอกเวลาตามห้องตรวจ", listOf(
            ToggleItem("คิดค่าธรรมเนียมตรวจนอกเวลาตามห้องตรวจ", false, ""),
            ValueItem("รหัสการชำระเงิน (นอกเวลา)", "", "ถ้าไม่ใส่จะใช้ 03"),
            ToggleItem("ใช้รหัสการชำระเงินตามสิทธิส่งตรวจ (นอกเวลา)", false, ""),
            ToggleItem("คิดค่าธรรมเนียมจากสิทธิการรักษา (นอกเวลา)", false, ""),
        )),
    )),
    SettingGroup("ident", "eKYC · PHR · QR Code", "ยืนยันตัวตน eKYC ยินยอม PHR สแกน QR และบาร์โค้ด HN",
        listOf("M4 8V6a2 2 0 0 1 2-2h2M16 4h2a2 2 0 0 1 2 2v2M20 16v2a2 2 0 0 1-2 2h-2M8 20H6a2 2 0 0 1-2-2v-2", "M14.4 11a2.4 2.4 0 1 1-4.8 0 2.4 2.4 0 0 1 4.8 0Z", "M8.5 16.4a4.2 4.2 0 0 1 7 0"), listOf(
        SettingSection("การเช็ค eKYC", listOf(
            ToggleItem("เช็ค eKYC", false, ""),
            ToggleItem("สมัคร MOPH หลังยืนยันสำเร็จ", false, ""),
            ValueItem("รหัสประชาชนของเจ้าหน้าที่", "13XXXXXXXXX02", ""),
            ValueItem("pincode ของเจ้าหน้าที่", "", ""),
        )),
        SettingSection("การยินยอมการเข้าถึงข้อมูล PHR", listOf(
            ToggleItem("ยืนยันการยินยอมข้อมูล PHR", false, ""),
        )),
        SettingSection("การสแกน QR Code เพื่อใช้ Service API", listOf(
            ToggleItem("สแกน QR Code", false, ""),
            ToggleItem("ลงทะเบียนใหม่ด้วย QR Code", false, ""),
            ValueItem("รหัสประชาชนผู้บันทึก", "XXXXXXXXXXXXX", ""),
            ValueItem("รหัสสถานพยาบาล (QR Code)", "99999", ""),
        )),
        SettingSection("การแปลงสัญลักษณ์จากการ Scan HN", listOf(
            ValueItem("สัญลักษณ์ที่ต้องการแปลง", "XXXX", ""),
            ValueItem("ต้องการแปลงสัญลักษณ์เป็น", "XXXX", ""),
        )),
    )),
    SettingGroup("device", "อุปกรณ์", "เครื่องพิมพ์ กล้อง เครื่องชั่ง Gateway LIS",
        listOf("M7 9V4h10v5", "M6 18H4.5A1.5 1.5 0 0 1 3 16.5v-6A1.5 1.5 0 0 1 4.5 9h15a1.5 1.5 0 0 1 1.5 1.5v6a1.5 1.5 0 0 1-1.5 1.5H18", "M7 14h10v6H7z"), listOf(
        SettingSection("Printer", listOf(
            ActionItem("ทดสอบพิมพ์", "เฉพาะต่อกับเครื่องโดยตรง"),
            ToggleItem("พิมพ์ผ่าน Gateway API", false, ""),
            ValueItem("IP Printer Gateway API", "", ""),
            ValueItem("Port Printer Gateway API", "", ""),
        )),
        SettingSection("ตั้งค่ากล้อง", listOf(
            ValueItem("หมุนมุมกล้อง", "", "ใส่ได้ 90, 180, 270"),
            ValueItem("หมุนมุมกล้องแคปใบหน้า", "", "ใส่ได้ 90, 180, 270"),
        )),
        SettingSection("เครื่องชั่งน้ำหนัก วัดส่วนสูง", listOf(
            ToggleItem("วัดน้ำหนักส่วนสูงกับเครื่อง", false, ""),
            ToggleItem("ใช้กับ Kiosk All in one", false, ""),
        )),
        SettingSection("ตั้งค่า Gateway สำหรับออกเลข LIS", listOf(
            ToggleItem("ออกเลข LIS", false, ""),
            ValueItem("IP Gateway", "", ""),
            ValueItem("Port Gateway", "", ""),
        )),
    )),
    SettingGroup("system", "ระบบและความปลอดภัย", "รหัสผ่าน License อัพเดท ข้อมูลเครื่อง",
        listOf("M15.2 12a3.2 3.2 0 1 1-6.4 0 3.2 3.2 0 0 1 6.4 0Z", "M19.4 14.6a1.7 1.7 0 0 0 .34 1.87l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.7 1.7 0 0 0-1.87-.34 1.7 1.7 0 0 0-1.03 1.56V21a2 2 0 1 1-4 0v-.11a1.7 1.7 0 0 0-1.11-1.56 1.7 1.7 0 0 0-1.87.34l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.7 1.7 0 0 0 .34-1.87 1.7 1.7 0 0 0-1.56-1.03H3a2 2 0 1 1 0-4h.11a1.7 1.7 0 0 0 1.56-1.11 1.7 1.7 0 0 0-.34-1.87l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.7 1.7 0 0 0 1.87.34H9a1.7 1.7 0 0 0 1.03-1.56V3a2 2 0 1 1 4 0v.11a1.7 1.7 0 0 0 1.03 1.56 1.7 1.7 0 0 0 1.87-.34l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.7 1.7 0 0 0-.34 1.87V9a1.7 1.7 0 0 0 1.56 1.03H21a2 2 0 1 1 0 4h-.11a1.7 1.7 0 0 0-1.49 1.03Z"), listOf(
        SettingSection("รหัสผ่านตั้งค่า", listOf(
            ActionItem("เปลี่ยนรหัสผ่านตั้งค่า", ""),
            ValueItem("Device Access Token", "{3DD9574C-F5AB-46E4-8D44-4E11A5FB80B4}", ""),
        )),
        SettingSection("ข้อมูลโรงพยาบาล", listOf(
            ValueItem("สาขาที่ใช้งาน", "", ""),
            ValueItem("รหัสสถานพยาบาล", "11446", ""),
            ValueItem("รหัสประชาชนผู้บันทึก", "1559900318327", ""),
        )),
        SettingSection("License", listOf(
            ValueItem("Mac Address", "2C:D2:6B:74:68:C2", ""),
            ValueItem("License Key", "42E287A0B125C14DA65A8B53D4935224", ""),
        )),
        SettingSection("Update Version Kiosk", listOf(
            ToggleItem("ตรวจสอบเวอร์ชันใหม่ตอนเปิดแอปทุกครั้ง", false, ""),
            ActionItem("ตรวจสอบและอัพเดทเวอร์ชัน", "ปัจจุบัน V.24.09.69"),
        )),
        SettingSection("สำหรับผู้ดูแลระบบ", listOf(
            ToggleItem("เก็บ LogCat Debug", false, ""),
            ActionItem("ตรวจสอบและสร้างตาราง setting_kiosk", "ตรวจสอบตารางข้อมูล"),
        )),
    )),
)
