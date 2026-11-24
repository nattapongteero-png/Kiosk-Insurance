/* =========================================================
   เสียงแนะนำตามขั้นตอน + อ่านเลขคิว/ห้องตรวจ (ตรงกับ ui/voice/Voice.kt)
   เล่นไฟล์เสียงใน voice/ ({th|en}_{คีย์}.m4a) · สร้างจาก VoxCPM TTS ของ BMS (voice "female_alma")
   เลขคิว = ไฟล์ตัวอักษร/ตัวเลขต่อกัน · ห้อง = {ภาษา}_room_{รหัสบริการ}
   ประโยคใหม่ตัดประโยคเก่าเมื่อผู้ใช้แตะจอระหว่างพูด · หน้าจอเปลี่ยนเอง = พูดประโยคเดิมให้จบก่อนแล้วต่อประโยคใหม่ (ตรงกับ Voice.kt)
   เบราว์เซอร์ให้เล่นเสียงได้หลังผู้ใช้แตะจอครั้งแรกเท่านั้น (ประโยคที่ค้างจะเล่นเมื่อแตะ) · ?mute=1 ปิดไว้ทดสอบ
   ========================================================= */
(function(){
  const mute = new URLSearchParams(location.search).get('mute') === '1';
  let last = null, lastKey = null, lastVars = {}, pending = null, unlocked = false, cur = null, seq = [];
  const lang = () => (window.I18N && I18N.lang) || 'th';
  let tok = 0, ctx = null, src = null, srcOn = false, lastTouch = 0, startedAt = 0, queued = null;
  const playing = () => !!(cur && !cur.paused && !cur.ended) || seq.length > 0 || srcOn;
  function finished(){ if (queued){ const [k, v] = queued; queued = null; VOICE.say(k, v, true); } }
  addEventListener('pointerdown', () => { lastTouch = performance.now(); }, true);
  function stop(){ tok++; seq = []; queued = null; srcOn = false; if (cur){ cur.onended = null; cur.pause(); cur = null; } if (src){ try { src.stop(); } catch (e) {} src = null; } }

  /* ---------- ประโยคเลขคิว (ตรงกับ Voice.kt) ----------
     ออนไลน์: ขอประโยคเต็มจาก VoxCPM (จังหวะพูดเลขต่อเนื่อง) รอไม่เกิน 3.5 วิ · ไม่ได้ = ต่อชิ้น WAV เป็นก้อนเดียวด้วย Web Audio เว้นช่วงคงที่ */
  const VOX = 'https://vox-cpm.bmscloud.in.th/v1/audio/speech', VOX_VOICE = 'female_alma';
  const thL = {a:'เอ',b:'บี',c:'ซี',d:'ดี',e:'อี',f:'เอฟ',g:'จี',l:'แอล',x:'เอ็กซ์'};
  const thD = ['ศูนย์','หนึ่ง','สอง','สาม','สี่','ห้า','หก','เจ็ด','แปด','เก้า'], enD = ['zero','one','two','three','four','five','six','seven','eight','nine'];
  const ROOM = {opd:['ห้องตรวจ','the exam room'], wound:['ห้องฉีดยาทำแผล','the injection and wound room'], med:['ห้องตรวจอายุรกรรม','the internal medicine room'],
    dent:['ทันตกรรม','the dental clinic'], physio:['กายภาพบำบัด','physical therapy'], thai:['แผนไทย ฝังเข็ม','Thai medicine and acupuncture'],
    psy:['จิตเวช','mental health'], lab:['ห้องปฏิบัติการ แล็บ','the laboratory'], xray:['ห้องเอกซเรย์','the X-ray room']};
  const LINE = {th:'ลงทะเบียนสำเร็จ คิวของท่านคือ {q} กรุณารับบัตรคิว แล้วไปที่{room}', en:"You're registered. Your queue number is {q}. Please take your ticket and go to {room}."};
  async function printed(l, q, room){
    if (mute) return; stop(); const my = tok, en = l === 'en';
    const spelled = [...q.toLowerCase()].map(c => /\d/.test(c) ? (en ? enD : thD)[+c] : (en ? c.toUpperCase() : (thL[c] || c))).join(en ? ', ' : ' ');
    const text = LINE[l].replace('{q}', spelled).replace('{room}', (ROOM[room] || ['', ''])[en ? 1 : 0]);
    ctx = ctx || new (window.AudioContext || window.webkitAudioContext)();
    try {
      /* VoxCPM จำกัดความยาวเสียงตามความยาวข้อความ → คำท้ายโดนตัด: ต่อคำท้ายให้ประโยคจริงพูดครบ แล้วตัดทิ้ง (dropTail) · ตัดไม่ได้ = ใช้ชิ้นในเครื่อง */
      const ac = new AbortController(); setTimeout(() => ac.abort(), 3500);
      const r = await fetch(VOX, {method:'POST', headers:{'Content-Type':'application/json'}, signal: ac.signal,
        body: JSON.stringify({model:'voxcpm-thai', input:text + (en ? ', hello.' : ', สวัสดี'), voice:VOX_VOICE, response_format:'wav'})});
      if (!r.ok) throw new Error(r.status);
      const buf = await ctx.decodeAudioData(await r.arrayBuffer()); if (my !== tok) return;
      const y = dropTail(buf.getChannelData(0), buf.sampleRate);
      if (!y) throw new Error('tail word not found');
      playBuf(y, buf.sampleRate); return;
    } catch (e) { console.info('[voice] online voice unavailable → joined clips', e.message || e); }
    if (my !== tok) return;
    const parts = [[l + '_printed1', 220]];
    [...q.toLowerCase()].forEach((c, i, a) => parts.push([l + (/\d/.test(c) ? '_d' : '_l') + c, i === a.length - 1 ? 260 : 90]));
    parts.push([l + '_printed2', 80]); if (room) parts.push([l + '_room_' + room, 0]);
    const bufs = await Promise.all(parts.map(([n]) => fetch('voice/' + n + '.wav').then(r => r.arrayBuffer()).then(b => ctx.decodeAudioData(b))));
    if (my !== tok) return;
    const rate = bufs[0].sampleRate, len = bufs.reduce((t, b, i) => t + b.length + Math.round(rate * parts[i][1] / 1000), 0);
    const d = new Float32Array(len); let at = 0;
    bufs.forEach((b, i) => { d.set(b.getChannelData(0), at); at += b.length + Math.round(rate * parts[i][1] / 1000); });
    playBuf(d, rate);
  }
  /* เล่นก้อนเดียว · เงียบนำ 150 ms (ลำโพงตื่นก่อนพยางค์แรก) ท้าย 250 ms */
  function playBuf(y, rate){
    const lead = Math.round(rate * .15), all = ctx.createBuffer(1, lead + y.length + Math.round(rate * .25), rate);
    all.getChannelData(0).set(y, lead);
    src = ctx.createBufferSource(); src.buffer = all; src.connect(ctx.destination); srcOn = true; src.onended = () => { srcOn = false; finished(); }; src.start();
  }
  /* ตัดคำต่อท้าย: ช่วงเงียบ ≥120 ms สุดท้าย ที่เสียงหลังช่วงนั้นยาว 0.25–1.2 วิ → ตัดหลังคำจริง 60 ms (ตรงกับ Voice.kt dropTailWord) */
  function dropTail(x, rate){
    const win = rate / 100 | 0, nw = x.length / win | 0, env = new Float32Array(nw);
    for (let w = 0; w < nw; w++){ let m = 0; for (let k = w * win; k < (w + 1) * win; k++) m = Math.max(m, Math.abs(x[k])); env[w] = m; }
    const pk = Math.max(...env), loud = [...env].map(v => v > pk * .04), end = loud.lastIndexOf(true), start = loud.indexOf(true);
    for (let w = end; w > start;){
      if (loud[w]){ w--; continue; }
      let j = w; while (j > start && !loud[j]) j--;
      if (w - j >= 12){
        const tail = (end - w) / 100; if (tail < .25 || tail > 1.2) return null;
        const cut = (j + 1 + 6) * win, y = x.slice(0, cut), f = rate / 50 | 0;
        for (let k = 0; k < f; k++) y[cut - 1 - k] *= k / f;
        return y;
      }
      w = j;
    }
    return null;
  }
  function next(){
    const n = seq.shift(); if (!n){ cur = null; finished(); return; }
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
      /* หน้าจอเปลี่ยนเอง (ไม่ได้แตะจอตั้งแต่ประโยคนี้เริ่ม) → รอให้จบก่อน ไม่ตัดกลางประโยค */
      if (unlocked && playing() && lastTouch < startedAt){ queued = [key, vars]; return; }
      if (unlocked) startedAt = performance.now();
      if (key === 'printed'){ if (!unlocked){ pending = () => printed(l, vars.q || '', vars.room); return; } printed(l, vars.q || '', vars.room); return; }
      const n = names(key, vars, l);
      if (!unlocked){ pending = n; return; }
      play(n);
    },
    stop(){ stop(); last = null; },
  };
  const unlock = () => { if (unlocked) return; unlocked = true; if (pending){ typeof pending === 'function' ? pending() : play(pending); pending = null; } };
  addEventListener('pointerdown', unlock, true);
  document.addEventListener('langchange', () => { if (lastKey) VOICE.say(lastKey, lastVars, true); });   /* สลับภาษา = พูดประโยคเดิมเป็นภาษาใหม่ */
  /* แต่ละหน้าบอกสถานะปัจจุบันผ่าน window.VOICE_STATE() → [คีย์, ค่าแทน] · เช็กทุก 300 ms พูดเมื่อสถานะเปลี่ยน · เตือนหมดเวลา (idle.js) มาก่อนเสมอ */
  setInterval(() => {
    const io = document.querySelector('.idleOv'); if (io && !io.hidden){ VOICE.say('idle'); return; }
    if (typeof window.VOICE_STATE !== 'function') return;
    const r = window.VOICE_STATE(); if (r) VOICE.say(r[0], r[1] || {});
  }, 300);
})();
