/* =========================================================
   สลับภาษา ไทย / อังกฤษ (ใช้ร่วมทุกหน้าที่ผู้ป่วยเห็น · ตรงกับ ui/i18n ใน Kotlin)
   แปลที่ชั้นแสดงผล: เดินทุก text node ในหน้า แทนด้วยคำแปลจากพจนานุกรม (i18n_dict.js) · สลับกลับไทยได้ทันที
   ข้อความในโค้ดยังเป็นไทยตามเดิม · ไม่อยู่ในพจนานุกรม = แสดงไทย (เก็บไว้ใน window.I18N_MISSING เพื่อเติมคำแปล)
   ไม่แปล: ข้อความใน <svg> (บัตรคิวที่พิมพ์ · ภาพบัตรประชาชน) · องค์ประกอบที่มี data-noi18n
   ภาษาเก็บใน sessionStorage 'kioskLang' ข้ามหน้าในรอบเดียว · หน้าแรกโหลดใหม่/กลับหน้าแรก = ไทย
   ========================================================= */
(function(){
  const KEY = 'kioskLang';
  const hasThai = s => /[฀-๿]/.test(s);
  const missing = window.I18N_MISSING = new Set();
  let lang = 'th';
  try { lang = sessionStorage.getItem(KEY) || 'th'; } catch (e) {}
  { const q = new URLSearchParams(location.search).get('lang'); if (q === 'en' || q === 'th'){ lang = q; try { sessionStorage.setItem(KEY, q); } catch (e) {} } }   /* ?lang=en ไว้เปิดดู/ทดสอบ */

  function tr(s){
    if (lang === 'th' || !hasThai(s)) return s;
    const lead = s.match(/^\s*/)[0], trail = s.match(/\s*$/)[0], key = s.trim().replace(/\s+/g, ' ');
    const D = window.I18N_EN || {};
    if (D[key] != null) return lead + D[key] + trail;
    for (const [re, f] of (window.I18N_RULES || [])){ const m = key.match(re); if (m) return lead + f(m) + trail; }
    missing.add(key);
    return s;
  }
  const skip = n => { const el = n.parentElement; return !el || el.closest('svg,script,style,[data-noi18n],[data-en]:not([data-en=""])'); };

  /* ตัวอักษรสุดท้ายของข้อความก่อนหน้าในบรรทัดเดียวกัน (ภายในกล่องเดียวกัน · <br> = ขึ้นบรรทัดใหม่) */
  function prevChar(n){
    const block = n.parentElement.closest('p,li,h1,h2,h3,div,label,button,td,span.tx,small') || document.body;
    for (let x = n; x && x !== block;){
      let p = x.previousSibling;
      while (!p){ x = x.parentNode; if (!x || x === block) return ''; p = x.previousSibling; }
      if (p.nodeName === 'BR') return '';
      const t = p.textContent; if (t && t.length) return t.slice(-1);
      x = p;
    }
    return '';
  }
  let busy = false;
  function apply(root){
    busy = true;
    const w = document.createTreeWalker(root || document.body, NodeFilter.SHOW_TEXT);
    for (let n; (n = w.nextNode());){
      if (skip(n)) continue;
      // ต้นฉบับไทย: ถ้าแอปเปลี่ยนข้อความเอง (ไม่ใช่ค่าที่เราแปลใส่) ถือเป็นต้นฉบับใหม่
      if (n.__th == null || n.nodeValue !== n.__out) n.__th = n.nodeValue;
      let out = tr(n.__th);
      // ภาษาไทยไม่เว้นวรรคระหว่างคำ: ช่วงที่ต่อกัน (เช่น ข้อความ + ตัวหนา) ต้องเติมช่องว่างเองในภาษาอังกฤษ
      if (lang === 'en' && /^[A-Za-z0-9(“"]/.test(out)){ const pv = prevChar(n); if (pv && /[A-Za-z0-9),.:”"]/.test(pv)) out = ' ' + out; }
      if (n.nodeValue !== out) n.nodeValue = out;
      n.__out = out;
    }
    /* องค์ประกอบที่มีคำแปลจัดบรรทัดเอง (data-en = HTML ภาษาอังกฤษ) */
    document.querySelectorAll('[data-en]').forEach(el => {
      if (!el.dataset.en) return;
      if (el.__thHtml == null) el.__thHtml = el.innerHTML;
      const h = lang === 'en' ? el.dataset.en : el.__thHtml;
      if (el.innerHTML !== h) el.innerHTML = h;
    });
    document.querySelectorAll('input[placeholder]').forEach(el => {
      if (el.closest('[data-noi18n]')) return;
      if (el.__th == null || el.placeholder !== el.__out) el.__th = el.placeholder;
      el.placeholder = el.__out = tr(el.__th);
    });
    document.documentElement.lang = lang;
    busy = false;
  }
  new MutationObserver(() => { if (!busy) apply(); })
    .observe(document.documentElement, {subtree:true, childList:true, characterData:true});

  window.I18N = {
    get lang(){ return lang; },
    set(l){ lang = l; try { sessionStorage.setItem(KEY, l); } catch (e) {} apply(); document.dispatchEvent(new CustomEvent('langchange')); },
    tr, apply,
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', () => apply());
  else apply();
})();
