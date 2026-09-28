package th.go.banlat.kiosk.ui.i18n

import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

/**
 * สลับภาษา ไทย / อังกฤษ ทั้งแอป — แปลที่ชั้นแสดงผล (KText) ด้วยพจนานุกรมไทย→อังกฤษชุดเดียวกับ preview/i18n.js
 * ข้อความในโค้ดยังเขียนเป็นไทยตามเดิม · ข้อความที่ไม่อยู่ในพจนานุกรมแสดงเป็นไทย (และ log "KioskI18n" ไว้เติมคำแปล)
 * ไม่แปล: ข้อมูลจากระบบโรงพยาบาล (ชื่อ สิทธิ ชื่อ รพ.) · บัตรคิวที่พิมพ์ · ภาพบัตรประชาชน (วาดบน Canvas) · หน้าตั้งค่า (เจ้าหน้าที่)
 * กลับหน้าแรก = กลับเป็นไทยเสมอ (คนถัดไปไม่เจอภาษาค้าง)
 */
enum class Lang { TH, EN }

object I18n {
    var lang by mutableStateOf(Lang.TH)
    private val logged = mutableSetOf<String>()
    internal fun miss(s: String) { if (logged.add(s)) Log.d("KioskI18n", "MISSING «$s»") }
}

/** ปิดการแปลในบางหน้า (หน้าตั้งค่าของเจ้าหน้าที่) */
val LocalNoTranslate = compositionLocalOf { false }

private fun hasThai(s: String) = s.any { it in '฀'..'๿' }

/** แปลข้อความเดียว · อ่าน I18n.lang ใน composition → สลับภาษาแล้วหน้าจอเปลี่ยนทันที */
fun tr(s: String): String {
    if (I18n.lang == Lang.TH || !hasThai(s)) return s
    // ทั้งก้อน (มีคำแปลแบบหลายบรรทัดไว้) ก่อน · ไม่มีแล้วแปลทีละบรรทัด (คีย์ชุดเดียวกับ HTML ที่แยกบรรทัดด้วย <br>)
    s.trim().let { k -> EnDict[k]?.let { return s.substring(0, s.indexOf(k)) + it + s.substring(s.indexOf(k) + k.length) } }
    return s.split('\n').joinToString("\n") { trLine(it) }
}

private fun trLine(s: String): String {
    if (!hasThai(s)) return s
    val lead = s.takeWhile { it.isWhitespace() }; val trail = s.takeLastWhile { it.isWhitespace() }
    val key = s.trim().replace(Regex("\\s+"), " ")
    EnDict[key]?.let { return lead + it + trail }
    for ((re, f) in EnRules) re.matchEntire(key)?.let { return lead + f(it) + trail }
    I18n.miss(key)
    return s
}

/** แปลทีละช่วงของ AnnotatedString (ตัวหนา/สี/รูปแทรก คงไว้ตามช่วงเดิม) */
fun tr(a: AnnotatedString): AnnotatedString {
    if (I18n.lang == Lang.TH || !hasThai(a.text)) return a
    val cuts = sortedSetOf(0, a.length)
    a.spanStyles.forEach { cuts += it.start; cuts += it.end }
    val anns = a.getStringAnnotations(0, a.length)
    anns.forEach { cuts += it.start; cuts += it.end }
    val pts = cuts.filter { it in 0..a.length }
    return buildAnnotatedString {
        for (i in 0 until pts.size - 1) {
            val s = pts[i]; val e = pts[i + 1]; if (s >= e) continue
            val piece = a.text.substring(s, e)
            val inAnn = anns.filter { it.start <= s && it.end >= e }
            var out = if (inAnn.isNotEmpty()) piece else tr(piece)
            // ภาษาไทยไม่เว้นวรรคระหว่างคำ: ช่วงที่ต่อกัน (ข้อความ + ตัวหนา) เติมช่องว่างเองในภาษาอังกฤษ
            val pv = if (length > 0) this.toAnnotatedString().text.last() else ' '
            if (inAnn.isEmpty() && out.isNotEmpty() && (out[0].isLetterOrDigit() || out[0] == '(') && (pv.isLetterOrDigit() || pv in "),.:")) out = " $out"
            if (inAnn.isNotEmpty() && pv.isLetterOrDigit()) append(' ')
            val st = length
            append(out)
            a.spanStyles.filter { it.start <= s && it.end >= e }.forEach { addStyle(it.item, st, length) }
            inAnn.forEach { addStringAnnotation(it.tag, it.item, st, length) }
        }
    }
}
