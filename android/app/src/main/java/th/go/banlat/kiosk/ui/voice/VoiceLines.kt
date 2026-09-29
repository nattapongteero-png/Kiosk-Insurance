package th.go.banlat.kiosk.ui.voice

/* สร้างจาก voice_phrases.py — ประโยคชุดเดียวกับ preview/voice_lines.js */
internal val VoiceLines: Map<String, Pair<String, String>> = mapOf(
    "home" to ("กรุณาเสียบบัตรประชาชน แล้วกดปุ่ม อ่านบัตร" to "Please insert your ID card, then tap Read card."),
    "reading" to ("กำลังอ่านบัตร กรุณาอย่าดึงบัตรออก" to "Reading your card. Please don't remove it."),
    "nocard" to ("ไม่พบบัตรประชาชน กรุณาเสียบบัตรให้สุดช่อง แล้วกดอ่านบัตรอีกครั้ง" to "No ID card detected. Please push the card all the way in, then tap Read card again."),
    "readfail" to ("อ่านบัตรไม่สำเร็จ กรุณาดึงบัตรออก เช็ดชิปให้สะอาด แล้วเสียบใหม่อีกครั้ง" to "Couldn't read the card. Please remove it, wipe the chip, and insert it again."),
    "kpcid" to ("กรุณากรอกเลขบัตรประชาชน สิบสามหลัก แล้วกดยืนยัน" to "Please enter your 13-digit ID card number, then tap Confirm."),
    "kphn" to ("กรุณาสแกนบาร์โค้ดบนบัตรโรงพยาบาล หรือกรอกเลข เอช เอ็น" to "Please scan the barcode on your hospital card, or enter your HN."),
    "face" to ("กรุณามองตรงมาที่กล้องด้านบนของตู้" to "Please look at the camera at the top of the kiosk."),
    "checking" to ("กำลังตรวจสอบข้อมูล กรุณารอสักครู่" to "Checking your information. Please wait."),
    "consent" to ("กรุณาอ่านข้อความ เลือกข้อมูลที่ยินยอมให้เปิดเผย แล้วกดยินยอม หรือไม่ยินยอม" to "Please read, choose the data you agree to share, then tap Agree or Decline."),
    "rightsok" to ("ตรวจสอบสิทธิเรียบร้อย กรุณากดถัดไป เพื่อเลือกบริการ" to "Your coverage is verified. Tap Next to choose a service."),
    "rightsbad" to ("สิทธิประจำตัวไม่สามารถใช้ที่โรงพยาบาลนี้ได้ ท่านยังรับบริการได้โดยชำระเงินเอง" to "Your coverage cannot be used at this hospital. You can still be seen as self-pay."),
    "services" to ("กรุณาเลือกบริการที่ต้องการ" to "Please choose a service."),
    "arrive" to ("ท่านมาในลักษณะใด กรุณาเลือก" to "How did you arrive? Please choose."),
    "confirm" to ("กรุณาตรวจสอบข้อมูล แล้วกดยืนยันการรับบริการ" to "Please check the details, then tap Confirm visit."),
    "printing" to ("กำลังพิมพ์บัตรคิว กรุณารอสักครู่" to "Printing your queue ticket. Please wait."),
    "printed" to ("ลงทะเบียนสำเร็จ คิวของท่านคือ {q} กรุณารับบัตรคิว แล้วไปที่{room}" to "You're registered. Your queue number is {q}. Please take your ticket and go to {room}."),
    "askins" to ("ลงทะเบียนรับบริการแล้ว สนใจตรวจสอบแผนประกันที่เหมาะกับท่านไหม ถ้าไม่สนใจ กดพิมพ์บัตรคิวได้เลย" to "You're registered. Would you like to check insurance plans that suit you? If not, tap Print ticket."),
    "insgw" to ("กำลังส่งข้อมูลให้บริษัทประกัน กรุณารอสักครู่" to "Sending your data to insurers. Please wait."),
    "insdone" to ("บันทึกความสนใจแล้ว บริษัทประกันจะติดต่อกลับ กรุณากดพิมพ์บัตรคิว" to "Your interest is recorded. The insurer will contact you. Please tap Print ticket."),
    "insselect" to ("ได้รับข้อเสนอจากบริษัทประกันแล้ว กรุณาเลือกแผนที่สนใจ" to "Offers have arrived. Please choose a plan you like."),
    "inssent" to ("ส่งข้อมูลให้บริษัทประกันแล้ว ท่านจะได้รับผลผ่านแอป" to "Your data has been sent. You'll get the results in the app."),
    "insslow" to ("บริษัทยังพิจารณาอยู่ ท่านไม่ต้องรอที่ตู้ ระบบจะแจ้งผลผ่านแอป กรุณากดพิมพ์บัตรคิว" to "The insurer is still reviewing. No need to wait here; results will come to the app. Please tap Print ticket."),
    "idle" to ("ท่านยังใช้งานอยู่หรือไม่ กรุณาแตะ ใช้งานต่อ" to "Are you still there? Tap Continue."),
    "bye" to ("กรุณารับบัตรประชาชนคืน" to "Please take your ID card."),
)
