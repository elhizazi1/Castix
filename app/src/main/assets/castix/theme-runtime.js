;(()=>{const d=window.CastixData;if(!d||!document.body)return;
const managed=document.querySelector('meta[name="castix-widgets"]')?.content!=="self";
let host=document.getElementById('__castix_widgets');
if(!managed){host?.remove();return;}
if(!host){host=document.createElement('div');host.id='__castix_widgets';document.body.appendChild(host);host.attachShadow({mode:'open'});}
for(const id of ['date','stats','app','session','note','battery','notifications']){const e=document.getElementById(id);if(e)e.style.setProperty('display','none','important');}
const clock=document.querySelector('[data-castix-clock]')||document.getElementById('clock')||document.querySelector('time');
if(clock){clock.style.setProperty('scale',String(Math.max(.75,Math.min(1.4,d.scale/100||1))));clock.style.setProperty('translate','0 '+(Number(d.verticalOffset)||0)+'px');}
Object.assign(host.style,{position:'fixed',left:'5%',right:'5%',bottom:'10%',zIndex:'2147483647',pointerEvents:'none'});
const sh=host.shadowRoot;sh.replaceChildren();const style=document.createElement('style');style.textContent=':host{color:#ddd;font:12px system-ui,sans-serif;text-align:center}section{display:flex;flex-direction:column;align-items:center;gap:'+Math.max(3,Math.min(9,5*(d.spacing||100)/100))+'px}div{max-width:100%;overflow-wrap:anywhere;line-height:1.25} .battery{color:#65dca4}.app{border:1px solid #426966;border-radius:20px;padding:2px 9px;color:#a0d8d2}.note{color:#999}';sh.append(style);const section=document.createElement('section');section.dir='auto';section.lang=d.language||'en';sh.append(section);
function add(v,cls){if(v==null||v==='')return;const e=document.createElement('div');e.className=cls||'';e.textContent=String(v);section.append(e);}
add(d.date);add(d.battery==null?null:d.battery+'%'+(d.charging?' ⚡':''),'battery');add(d.notificationCount>0?'◉ '+d.notificationCount:null);add(d.foregroundApp,'app');add(d.sessionMinutes==null?null:d.sessionLabel);add(d.inscription,'note');
})();