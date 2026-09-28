/* =========================================================
   เสียงแนะนำตามขั้นตอน + อ่านเลขคิว/ห้องตรวจ (ตรงกับ ui/voice/Voice.kt)
   เล่นไฟล์เสียงอัดไว้ใน voice/ ({th|en}_{คีย์}.m4a) · ต้นแบบสร้างจากเสียง macOS (ไทย Kanya · อังกฤษ Samantha)
   เลขคิว = ไฟล์ตัวอักษร/ตัวเลขต่อกัน · ห้อง = {ภาษา}_room_{รหัสบริการ} · ประโยคใหม่ตัดประโยคเก่า
   เบราว์เซอร์ให้เล่นเสียงได้หลังผู้ใช้แตะจอครั้งแรกเท่านั้น (ประโยคที่ค้างจะเล่นเมื่อแตะ) · ?mute=1 ปิดไว้ทดสอบ
   ========================================================= */
(function(){
  const mute = new URLSearchParams(location.search).get('mute') === '1';
  let last = null, lastKey = null, lastVars = {}, pending = null, unlocked = false, cur = null, seq = [];
  const lang = () => (window.I18N && I18N.lang) || 'th';
  function stop(){ seq = []; if (cur){ cur.onended = null; cur.pause(); cur = null; } }
  function next(){
    const n = seq.shift(); if (!n) return;
    cur = new Audio('voice/' + n + '.m4a');
    cur.onended = next; cur.onerror = next;
    cur.play().catch(() => {});
  }
  function play(names){ if (mute) return; stop(); seq = names.slice(); next(); }
  function names(key, vars, l){
    if (key !== 'printed') return [l + '_' + key];
    const out = [l + '_printed1'];
    for (const c of (vars.q || '').toLowerCase()) out.push(l + (/\d/.test(c) ? '_d' : '_l') + c);
    out.push(l + '_printed2'); if (vars.room) out.push(l + '_room_' + vars.room);
    return out;
  }
  window.VOICE = {
    /* พูดประโยคตามคีย์ · printed: vars.q = เลขคิว (A003) · vars.room = รหัสบริการ (opd) · ซ้ำสถานะเดิม = ไม่พูดซ้ำ */
    say(key, vars = {}, force = false){
      const l = lang(), id = key + '|' + l + '|' + JSON.stringify(vars);
      if (!force && id === last) return; last = id; lastKey = key; lastVars = vars;
      const n = names(key, vars, l);
      if (!unlocked){ pending = n; return; }
      play(n);
    },
    stop(){ stop(); last = null; },
  };
  const unlock = () => { if (unlocked) return; unlocked = true; if (pending){ play(pending); pending = null; } };
  addEventListener('pointerdown', unlock, true);
  document.addEventListener('langchange', () => { if (lastKey) VOICE.say(lastKey, lastVars, true); });   /* สลับภาษา = พูดประโยคเดิมเป็นภาษาใหม่ */
  /* แต่ละหน้าบอกสถานะปัจจุบันผ่าน window.VOICE_STATE() → [คีย์, ค่าแทน] · เช็กทุก 300 ms พูดเมื่อสถานะเปลี่ยน · เตือนหมดเวลา (idle.js) มาก่อนเสมอ */
  setInterval(() => {
    const io = document.querySelector('.idleOv'); if (io && !io.hidden){ VOICE.say('idle'); return; }
    if (typeof window.VOICE_STATE !== 'function') return;
    const r = window.VOICE_STATE(); if (r) VOICE.say(r[0], r[1] || {});
  }, 300);
})();
