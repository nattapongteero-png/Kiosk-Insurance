package th.go.banlat.kiosk.ui.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import th.go.banlat.kiosk.ui.i18n.I18n
import th.go.banlat.kiosk.ui.i18n.Lang

/**
 * เสียงแนะนำตามขั้นตอน + อ่านเลขคิว/ห้องตรวจ — เล่นไฟล์เสียงอัดไว้ (res/raw/v_*.m4a) ตรงกับ preview/voice.js
 * ไฟล์สร้างจาก VoxCPM TTS ของ BMS (vox-cpm.bmscloud.in.th · model voxcpm-thai · voice "female_alma") ไม่ต้องมีเสียง TTS ในตู้ ไม่ต้องใช้เน็ตตอนเล่น
 * TODO(integration): เปลี่ยนเป็นไฟล์อัดเสียงจริงชื่อเดิม (ประโยคอยู่ใน VoiceLines.kt / voice_phrases.py)
 * ไฟล์: v_{th|en}_{คีย์} · เลขคิว = v_*_l{อักษร} + v_*_d{ตัวเลข} · ห้อง = v_*_room_{รหัสบริการ}
 * ประโยคเลขคิว (printed): ออนไลน์ = ขอประโยคเต็มจาก VoxCPM (จังหวะพูดเลขต่อเนื่องเป็นธรรมชาติ · รอไม่เกิน 3.5 วิ)
 *   ออฟไลน์/ช้า = ต่อชิ้นเสียง WAV (ตัดเงียบ + ปรับความดังเท่ากันไว้แล้ว) เป็นก้อนเดียว เว้นช่วงคงที่ แล้วเล่นทีเดียว ไม่มีช่องว่างของตัวเล่น
 * ประโยคใหม่ตัดประโยคเก่าทันทีเมื่อผู้ใช้แตะจอระหว่างพูด · หน้าจอเปลี่ยนเอง (อ่านบัตร/สแกนหน้า/ส่งข้อมูล) = พูดประโยคเดิมให้จบก่อนแล้วต่อประโยคใหม่
 * เปิดเสมอเหมือนตู้เดิม (ตั้งค่าเดิมไม่มีตัวเลือกเสียง) · ระดับเสียงใช้ปุ่มเสียงของเครื่อง
 */
object Voice {
    private lateinit var app: Context
    private var player: MediaPlayer? = null
    private var queue = ArrayDeque<Int>()
    private var track: AudioTrack? = null
    private var token = 0          // ประโยคล่าสุด · งานที่ค้าง (เช่น ขอเสียงออนไลน์) ของประโยคเก่าจะถูกทิ้ง
    @Volatile private var lastTouch = 0L      // แตะจอล่าสุด (KioskApp แจ้ง)
    private var startedAt = 0L                // ประโยคปัจจุบันเริ่มพูด
    private var pending: Pair<String, Map<String, String>>? = null   // ประโยครอพูดต่อ (หน้าจอเปลี่ยนเองระหว่างพูด)
    private val main = android.os.Handler(android.os.Looper.getMainLooper())

    private const val VOX_URL = "https://vox-cpm.bmscloud.in.th/v1/audio/speech"   // TODO(integration): ตั้งค่าได้ในหน้าตั้งค่า
    private const val VOX_VOICE = "female_alma"
    private val ThL = mapOf('a' to "เอ", 'b' to "บี", 'c' to "ซี", 'd' to "ดี", 'e' to "อี", 'f' to "เอฟ", 'g' to "จี", 'l' to "แอล", 'x' to "เอ็กซ์")
    private val ThD = listOf("ศูนย์", "หนึ่ง", "สอง", "สาม", "สี่", "ห้า", "หก", "เจ็ด", "แปด", "เก้า")
    private val EnD = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
    private val RoomName = mapOf("opd" to ("ห้องตรวจ" to "the exam room"), "wound" to ("ห้องฉีดยาทำแผล" to "the injection and wound room"),
        "med" to ("ห้องตรวจอายุรกรรม" to "the internal medicine room"), "dent" to ("ทันตกรรม" to "the dental clinic"),
        "physio" to ("กายภาพบำบัด" to "physical therapy"), "thai" to ("แผนไทย ฝังเข็ม" to "Thai medicine and acupuncture"),
        "psy" to ("จิตเวช" to "mental health"), "lab" to ("ห้องปฏิบัติการ แล็บ" to "the laboratory"), "xray" to ("ห้องเอกซเรย์" to "the X-ray room"))

    fun init(context: Context) { app = context.applicationContext }

    /** เรียกทุกครั้งที่ผู้ใช้แตะจอ */
    fun touched() { lastTouch = android.os.SystemClock.uptimeMillis() }
    private fun playing() = player?.let { runCatching { it.isPlaying }.getOrDefault(false) } == true || queue.isNotEmpty()
    /** จบประโยค: มีประโยครอ → พูดต่อ */
    private fun finished() { pending?.let { (k, v) -> pending = null; main.post { say(k, v) } } }

    /** พูดประโยคตามคีย์ · printed ใช้ vars q = เลขคิว (เช่น A003) และ room = รหัสบริการ (เช่น opd) */
    fun say(key: String, vars: Map<String, String> = emptyMap()) {
        if (!::app.isInitialized) return
        val l = if (I18n.lang == Lang.EN) "en" else "th"
        // หน้าจอเปลี่ยนเอง (ไม่ได้แตะจอตั้งแต่ประโยคนี้เริ่ม) → รอให้จบก่อน ไม่ตัดกลางประโยค
        if (playing() && lastTouch < startedAt) { pending = key to vars; Log.d("KioskVoice", "wait $key"); return }
        Log.d("KioskVoice", "say $key")
        startedAt = android.os.SystemClock.uptimeMillis()
        if (key == "printed") { sayPrinted(l, vars["q"].orEmpty(), vars["room"]); return }
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
        token++; pending = null
        track?.run { runCatching { stop() }; release() }; track = null
        queue.clear()
        player?.run { runCatching { stop() }; release() }
        player = null
    }

    /** ประโยคเลขคิว: ออนไลน์ก่อน ไม่ได้ภายใน 3.5 วิ ค่อยต่อชิ้นเสียงในเครื่อง */
    private fun sayPrinted(l: String, q: String, room: String?) {
        stop(); val my = token
        val en = l == "en"
        val spelled = q.lowercase().map { c -> if (c.isDigit()) (if (en) EnD else ThD)[c - '0'] else if (en) c.uppercase() else ThL[c] ?: c.toString() }
            .joinToString(if (en) ", " else " ")
        val (th, enT) = VoiceLines["printed"] ?: return
        val roomName = room?.let { RoomName[it] }?.let { if (en) it.second else it.first }.orEmpty()
        val text = (if (en) enT else th).replace("{q}", spelled).replace("{room}", roomName)
        thread(isDaemon = true) {
            val wav = runCatching { fetch(text) }.onFailure { Log.i("KioskVoice", "online voice unavailable: ${it.message}") }.getOrNull()
            if (my != token) return@thread
            if (wav != null) {
                val f = File(app.cacheDir, "voice_printed.wav").apply { writeBytes(wav) }
                val mp = MediaPlayer().apply { setDataSource(f.path); prepare() }
                if (my != token) { mp.release(); return@thread }
                player = mp.apply { setOnCompletionListener { it.release(); if (player === it) player = null; finished() }; start() }
            } else playJoined(l, q, room, my)
        }
    }

    private fun fetch(text: String): ByteArray {
        val c = URL(VOX_URL).openConnection() as HttpURLConnection
        c.connectTimeout = 2000; c.readTimeout = 3500; c.requestMethod = "POST"; c.doOutput = true
        c.setRequestProperty("Content-Type", "application/json")
        // เติม " ..." ท้ายข้อความ: VoxCPM มักตัดพยางค์สุดท้ายของประโยคที่ไม่มีจุดจบ
        val body = org.json.JSONObject().put("model", "voxcpm-thai").put("input", "$text ...").put("voice", VOX_VOICE).put("response_format", "wav").toString()
        c.outputStream.use { it.write(body.toByteArray()) }
        check(c.responseCode == 200) { "HTTP ${c.responseCode}" }
        return c.inputStream.use { it.readBytes() }.also { check(it.size > 2000) }
    }

    /** ต่อชิ้น WAV (24 kHz 16-bit mono) เป็นก้อนเดียว เว้นช่วงคงที่ → เล่นด้วย AudioTrack ครั้งเดียว */
    private fun playJoined(l: String, q: String, room: String?, my: Int) {
        val rate = 24000
        fun pcm(n: String): ByteArray? {
            val id = app.resources.getIdentifier("v_$n", "raw", app.packageName).takeIf { it != 0 } ?: return null
            return app.resources.openRawResource(id).use { it.readBytes() }.let { it.copyOfRange(44, it.size) }
        }
        fun gap(ms: Int) = ByteArray(rate * ms / 1000 * 2)
        val out = java.io.ByteArrayOutputStream()
        out.write(gap(150))   // เงียบนำ 150 ms ให้ลำโพงตื่นก่อน ไม่กินพยางค์แรก
        pcm("${l}_printed1")?.let { out.write(it) }; out.write(gap(220))
        q.lowercase().forEachIndexed { i, c -> if (i > 0) out.write(gap(90)); pcm(if (c.isDigit()) "${l}_d$c" else "${l}_l$c")?.let { out.write(it) } }
        out.write(gap(260)); pcm("${l}_printed2")?.let { out.write(it) }
        room?.let { out.write(gap(80)); pcm("${l}_room_$it")?.let { b -> out.write(b) } }
        val data = out.toByteArray()
        if (my != token || data.isEmpty()) return
        @Suppress("DEPRECATION")
        val t = AudioTrack(AudioManager.STREAM_MUSIC, rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, data.size, AudioTrack.MODE_STATIC)
        t.write(data, 0, data.size); t.play(); track = t
    }

    private fun playNext() {
        val id = queue.removeFirstOrNull() ?: return
        player = MediaPlayer.create(app, id)?.apply {
            setOnCompletionListener { mp -> mp.release(); if (player === mp) player = null; if (queue.isEmpty()) finished() else playNext() }
            start()
        }
    }
}

/** พูดเมื่อเข้าหน้า/สถานะนี้ (และเมื่อสลับภาษา) */
@Composable
fun Speak(key: String, vararg deps: Any?, vars: () -> Map<String, String> = { emptyMap() }) {
    LaunchedEffect(key, I18n.lang, *deps) { Voice.say(key, vars()) }
}