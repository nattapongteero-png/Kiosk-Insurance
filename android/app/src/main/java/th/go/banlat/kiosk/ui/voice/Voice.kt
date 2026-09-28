package th.go.banlat.kiosk.ui.voice

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import th.go.banlat.kiosk.ui.i18n.I18n
import th.go.banlat.kiosk.ui.i18n.Lang

/**
 * เสียงแนะนำตามขั้นตอน + อ่านเลขคิว/ห้องตรวจ — เล่นไฟล์เสียงอัดไว้ (res/raw/v_*.m4a) ตรงกับ preview/voice.js
 * ต้นแบบ: ไฟล์สร้างจากเสียง macOS (ไทย Kanya · อังกฤษ Samantha) ให้ฟังรู้เรื่องทุกเครื่อง ไม่ต้องมีเสียง TTS ในตู้
 * TODO(integration): เปลี่ยนเป็นไฟล์อัดเสียงจริงชื่อเดิม (ประโยคอยู่ใน VoiceLines.kt / voice_phrases.py)
 * ไฟล์: v_{th|en}_{คีย์} · เลขคิว = v_*_l{อักษร} + v_*_d{ตัวเลข} · ห้อง = v_*_room_{รหัสบริการ}
 * ประโยคใหม่ตัดประโยคเก่าทันที · เปิดเสมอเหมือนตู้เดิม (ตั้งค่าเดิมไม่มีตัวเลือกเสียง) · ระดับเสียงใช้ปุ่มเสียงของเครื่อง
 */
object Voice {
    private lateinit var app: Context
    private var player: MediaPlayer? = null
    private var queue = ArrayDeque<Int>()

    fun init(context: Context) { app = context.applicationContext }

    /** พูดประโยคตามคีย์ · printed ใช้ vars q = เลขคิว (เช่น A003) และ room = รหัสบริการ (เช่น opd) */
    fun say(key: String, vars: Map<String, String> = emptyMap()) {
        if (!::app.isInitialized) return
        val l = if (I18n.lang == Lang.EN) "en" else "th"
        val names = if (key == "printed") buildList {
            add("${l}_printed1")
            vars["q"].orEmpty().lowercase().forEach { c -> add(if (c.isDigit()) "${l}_d$c" else "${l}_l$c") }
            add("${l}_printed2")
            vars["room"]?.let { add("${l}_room_$it") }
        } else listOf("${l}_$key")
        val ids = names.mapNotNull { n ->
            app.resources.getIdentifier("v_$n", "raw", app.packageName).takeIf { it != 0 }
                ?: run { Log.w("KioskVoice", "missing clip v_$n"); null }
        }
        stop()
        queue = ArrayDeque(ids)
        playNext()
    }

    fun stop() {
        queue.clear()
        player?.run { runCatching { stop() }; release() }
        player = null
    }

    private fun playNext() {
        val id = queue.removeFirstOrNull() ?: return
        player = MediaPlayer.create(app, id)?.apply {
            setOnCompletionListener { mp -> mp.release(); if (player === mp) player = null; playNext() }
            start()
        }
    }
}

/** พูดเมื่อเข้าหน้า/สถานะนี้ (และเมื่อสลับภาษา) */
@Composable
fun Speak(key: String, vararg deps: Any?, vars: () -> Map<String, String> = { emptyMap() }) {
    LaunchedEffect(key, I18n.lang, *deps) { Voice.say(key, vars()) }
}
