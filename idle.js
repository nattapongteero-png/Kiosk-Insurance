/* =========================================================
   กลับหน้าแรกอัตโนมัติเมื่อไม่มีการแตะ (ใช้ร่วมทุกหน้า · ตรงกับ IdleGuard ใน Kotlin)
   นับรวม 1 นาทีนับจากแตะครั้งล่าสุด (แตะจอ = เริ่มนับใหม่) · ระหว่างนั้นไม่แสดงอะไร ไม่รบกวนหน้าจอ
   เหลือ 20 วิสุดท้าย → เด้งเตือน "ยังใช้งานอยู่หรือไม่" พร้อมวงนับถอยหลัง → 0 = กลับหน้าแรก + ล้างข้อมูลผู้ป่วย
   หน้าแรกนับเฉพาะตอนมีชั้นซ้อนเปิดอยู่ (window.IDLE_BUSY) · ต้นแบบ: ?idle=20 = ย่อเวลารวมไว้ทดสอบ
   TODO(integration): อ่านเวลาจากตั้งค่าตู้ "ระยะเวลาในการกลับไปหน้าแรก (วินาที)"
   ========================================================= */
(function(){
  const q = new URLSearchParams(location.search);
  const TOTAL = +(q.get('idle') || 60), WARN = 20;
  const css = `
.idleOv{position:absolute;left:0;top:0;width:1080px;height:var(--H,1920px);z-index:60;   /* ขนาดจอจริง: กรอบนอก (#stage) ถูกตั้งความสูงเป็นค่าที่ย่อแล้วตอนแสดงแบบกรอบตัวอย่าง จึงอ้าง inset:0 ไม่ได้ */
  display:flex;align-items:center;justify-content:center;padding:0 100px;
  background:rgba(10,25,60,.42);-webkit-backdrop-filter:blur(8px);backdrop-filter:blur(8px);font-family:"Noto Sans Thai",system-ui,sans-serif;color:#14265A}
.idleOv[hidden]{display:none!important}
body:not(.bleed) .idleOv{-webkit-backdrop-filter:none;backdrop-filter:none;background:rgba(10,25,60,.55)}   /* กรอบตัวอย่าง (ย่อจอ): เบราว์เซอร์เบลอเพี้ยนเป็นลายซ้ำ → ใช้ scrim ทึบแทน */
.idleBox{width:100%;padding:64px 56px 48px;border-radius:40px;display:flex;flex-direction:column;align-items:center;text-align:center;
  background:linear-gradient(176deg,#FFFFFF 0%,#FDFCFA 55%,#F8F5EF 100%);box-shadow:0 0 0 1px rgba(191,145,58,.32),0 40px 100px rgba(6,20,50,.35)}
.idleBox .idleRing{position:relative;width:160px;height:160px;margin-bottom:32px}
.idleBox .idleRing svg{position:absolute;inset:0;width:100%;height:100%;transform:rotate(-90deg)}
.idleBox .idleRing b{position:absolute;inset:0;display:grid;place-items:center;font-size:64px;font-weight:700;font-variant-numeric:tabular-nums}
.idleBox h2{font-size:44px;font-weight:700;line-height:60px}
.idleBox p{margin-top:16px;font-size:38px;line-height:62px;color:#5B6B8A}
.idleBox p b{color:#14265A}
.idleBox .aFoot{display:flex;gap:24px;width:100%;margin-top:48px}
.idleBox .aFoot button{flex:1;height:96px;border-radius:99px;font:inherit;font-size:30px;font-weight:700;cursor:pointer}
.idleBox .aFoot .ghost{flex:0 0 auto;min-width:280px;padding:0 32px;background:#fff;border:0;color:#5B6B8A;box-shadow:0 0 0 2px #DCE4EE}
.idleBox .aFoot .pri{border:0;color:#fff;background:linear-gradient(135deg,#223A7A 0%,#14265A 100%);box-shadow:0 10px 24px rgba(20,38,90,.28)}`;
  const st = document.createElement('style'); st.textContent = css; document.head.appendChild(st);

  const stage = document.getElementById('stage'); stage.style.position = 'relative';
  const ov = document.createElement('div');
  ov.className = 'idleOv'; ov.hidden = true;
  ov.setAttribute('role', 'alertdialog'); ov.setAttribute('aria-modal', 'true');
  const R = 70, C = 2 * Math.PI * R;
  ov.innerHTML = `<div class="idleBox">
      <div class="idleRing"><svg viewBox="0 0 160 160"><circle cx="80" cy="80" r="${R}" fill="none" stroke="#F1E6CF" stroke-width="10"/>
        <circle id="idleArc" cx="80" cy="80" r="${R}" fill="none" stroke="#C9942F" stroke-width="10" stroke-linecap="round" stroke-dasharray="${C}" stroke-dashoffset="0"/></svg><b id="idleN">${WARN}</b></div>
      <h2>ยังใช้งานอยู่หรือไม่</h2>
      <p>ไม่มีการใช้งานสักพักแล้ว<br>ระบบจะ<b>กลับหน้าแรก</b>อัตโนมัติเมื่อครบเวลา</p>
      <div class="aFoot"><button class="ghost" id="idleHome">กลับหน้าแรก</button><button class="pri" id="idleGo">ใช้งานต่อ</button></div>
    </div>`;
  stage.appendChild(ov);

  let last = Date.now(), warnAt = 0;
  const bump = () => { if (ov.hidden) last = Date.now(); };
  ['pointerdown', 'keydown', 'wheel', 'touchstart'].forEach(e => addEventListener(e, bump, true));
  addEventListener('scroll', bump, true);

  const busy = () => typeof window.IDLE_BUSY === 'function' ? window.IDLE_BUSY() : true;
  const home = () => {
    ov.hidden = true; last = Date.now();
    if (typeof window.IDLE_HOME === 'function') window.IDLE_HOME('idle');
    else location.href = 'welcome_v2.html?bye=idle';
  };
  document.getElementById('idleGo').onclick = () => { ov.hidden = true; last = Date.now(); };
  document.getElementById('idleHome').onclick = home;

  setInterval(() => {
    if (!busy()){ ov.hidden = true; last = Date.now(); return; }
    if (ov.hidden){
      if (TOTAL - (Date.now() - last) / 1000 > WARN) return;
      ov.hidden = false; warnAt = Date.now();
    }
    const left = Math.max(0, WARN - (Date.now() - warnAt) / 1000);
    document.getElementById('idleN').textContent = Math.ceil(left);
    document.getElementById('idleArc').setAttribute('stroke-dashoffset', C * (1 - left / WARN));
    if (left <= 0) home();
  }, 250);
})();
