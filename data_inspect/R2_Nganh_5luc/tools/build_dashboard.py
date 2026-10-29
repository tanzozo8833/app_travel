"""Dựng data_inspect/R2_Nganh_5luc/Levart_5Forces_Dashboard.html — chạy offline (plotly.js nhúng sẵn).

Mọi mô phỏng (bản đồ định vị chọn trục, HHI khi Levart vào, khuếch tán Bass, hồi quy đánh giá, thương lượng Nash,
chỉ số 5 lực với trọng số tự chỉnh) chạy lại bằng JavaScript khi kéo thanh trượt.
"""
import json

import numpy as np
import pandas as pd
from plotly.offline import get_plotlyjs

from common import RES, ROOT
from sources import ACCESSED, SOURCES


def build():
    S = json.loads((RES / "summary.json").read_text(encoding="utf-8"))
    X = pd.read_csv(RES / "b2_feature_matrix.csv", index_col=0)
    pos = pd.read_csv(RES / "b2_positioning.csv")
    core = pd.read_csv(RES / "b2_core_competitors.csv")
    r1 = ROOT.parent / "R1_PESTLE" / "results" / "summary.json"
    R1 = json.loads(r1.read_text(encoding="utf-8")) if r1.exists() else {}
    prov = pd.read_csv(RES / "b5_lodging_by_province.csv") if (RES / "b5_lodging_by_province.csv").exists() else None
    D = {
        "S": S, "features": list(X.columns), "X": {n: [round(float(v), 3) for v in X.loc[n].values] for n in X.index},
        "pos": pos.round(4).to_dict("records"),
        "core": core[["name", "type", "real", "vn_ratings_ios", "score", "ratings"]].replace({np.nan: None}).to_dict("records"),
        "hn_young": R1.get("a11_market", {}).get("people_20_29_p50_m", 1.34),
        "prov": prov.round(2).to_dict("records") if prov is not None else [],
        "src": {k: {"name": v[0], "org": v[1], "url": v[2], "api": v[3], "note": v[4], "lv": v[5]} for k, v in SOURCES.items()},
        "accessed": ACCESSED,
    }
    data = json.dumps(D, ensure_ascii=False, default=lambda o: None).replace("NaN", "null")
    html = TEMPLATE.replace("/*PLOTLY*/", get_plotlyjs()).replace("/*DATA*/", data)
    out = ROOT / "Levart_5Forces_Dashboard.html"
    out.write_text(html, encoding="utf-8")
    print("dashboard ->", out, f"{out.stat().st_size / 1e6:.1f} MB")


TEMPLATE = r"""<!doctype html><html lang="vi"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Levart – 5 lực cạnh tranh</title>
<style>
:root{--bg:#f9f9f7;--card:#fcfcfb;--ink:#0b0b0b;--ink2:#52514e;--mut:#7a7872;--grid:#e1e0d9;--ring:rgba(11,11,11,.10);--acc:#256abf}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:14.5px/1.55 system-ui,-apple-system,"Segoe UI",sans-serif}
.wrap{max-width:1200px;margin:0 auto;padding:22px 16px 60px}
h1{font-size:28px;margin:4px 0}h2{font-size:20px;margin:36px 0 4px;padding-top:12px;border-top:1px solid var(--grid)}
.lede{color:var(--ink2);max-width:880px}.meta{color:var(--mut);font-size:12.5px}
.card{background:var(--card);border-radius:12px;box-shadow:0 0 0 1px var(--ring);padding:14px 16px;margin:12px 0}
.kpis{display:grid;grid-template-columns:repeat(auto-fill,minmax(min(210px,100%),1fr));gap:10px;margin:14px 0}
.kpi .v{font-size:25px;font-weight:650;line-height:1.15}.kpi .l{color:var(--ink2);font-size:13px;margin-top:3px}
.grid2{display:grid;grid-template-columns:minmax(0,320px) minmax(0,1fr);gap:16px;align-items:start}
@media(max-width:820px){.grid2{grid-template-columns:1fr}}
.ctl label{display:block;font-size:12.5px;color:var(--ink2);margin:9px 0 2px}.ctl label b{color:var(--ink);float:right;font-variant-numeric:tabular-nums}
.ctl input[type=range]{width:100%}.ctl select{width:100%;padding:5px;font:inherit;border-radius:6px;border:1px solid var(--grid);background:#fff}
.insight{border-left:3px solid var(--acc);padding:8px 12px;background:#fff;border-radius:0 8px 8px 0;margin:10px 0;font-size:13.5px;box-shadow:0 0 0 1px var(--ring)}
.plot{width:100%;min-height:380px}table{border-collapse:collapse;width:100%;font-size:12.5px}th,td{padding:5px 7px;border-bottom:1px solid var(--grid);text-align:left;vertical-align:top}
th{color:var(--ink2);font-weight:650;background:#f3f2ee}td.n{text-align:right;font-variant-numeric:tabular-nums}
nav.toc{display:flex;flex-wrap:wrap;gap:6px;margin:8px 0}nav.toc a{font-size:12.5px;text-decoration:none;color:var(--ink2);padding:3px 10px;border-radius:999px;box-shadow:inset 0 0 0 1px var(--ring)}
.src{font-size:11.5px;color:var(--mut);margin:8px 0 0}.src a{color:var(--acc)}
.btn{font:inherit;font-size:12.5px;padding:5px 10px;border-radius:6px;border:1px solid var(--grid);background:#fff;cursor:pointer;margin-top:10px}
.chk{font-size:12.5px;display:flex;gap:6px;align-items:flex-start;margin:4px 0}
</style><script>/*PLOTLY*/</script></head><body><div class="wrap">
<div class="meta">LEVART · data_inspect/R2_Nganh_5luc · dữ liệu tải 26/10/2026 · mọi con số tính từ dữ liệu gốc bằng tools/*.py</div>
<h1>Ngành và 5 lực cạnh tranh — mô phỏng bằng dữ liệu</h1>
<p class="lede">Đi kèm <code>report.md</code>. Mỗi phần là một mô hình chạy trong trình duyệt: đổi giả định để xem kết luận đứng vững tới đâu.</p>
<nav class="toc"><a href="#s1">① Tổng quan ngành</a><a href="#s2">② Bản đồ định vị</a><a href="#s3">③ Tập trung & Levart vào thị trường</a><a href="#s4">④ Gia nhập & sống sót</a><a href="#s5">⑤ Người dùng</a><a href="#s6">⑥ Nhà cung cấp</a><a href="#s7">⑦ Thay thế</a><a href="#s8">⑧ Tổng hợp 5 lực</a><a href="#src">Nguồn</a></nav>
<div class="kpis" id="kpis"></div>

<h2 id="s1">① Tổng quan ngành — quy mô và tốc độ tăng</h2>
<div class="card"><div id="pGva" class="plot"></div><div class="insight" id="iGva"></div><p class="src" data-ids="D1"></p></div>
<div class="card"><div id="pEst" class="plot" style="min-height:330px"></div><p class="src" data-ids="S3,S4,S5"></p></div>

<h2 id="s2">② Bản đồ định vị — chọn trục bất kỳ, chỉnh vector Levart</h2>
<div class="grid2"><div class="card ctl">
<label>Trục X</label><select id="ax"></select><label>Trục Y</label><select id="ay"></select><label>Trục Z (3D, tuỳ chọn)</label><select id="az"></select>
<div id="levCtl"></div><button class="btn" onclick="resetLev()">Về vector Levart gốc</button>
<p class="meta">12 trục = mật độ từ khoá trong mô tả Google Play + App Store của 23 đối thủ. Vector Levart là mục tiêu (GT) — kéo để thử định vị khác.</p></div>
<div class="card"><div id="pPos" class="plot" style="min-height:560px"></div><div class="insight" id="iPos"></div><p class="src" data-ids="D2,D4,GT"></p></div></div>

<h2 id="s3">③ Độ tập trung và mô phỏng Levart vào thị trường</h2>
<div class="grid2"><div class="card ctl">
<label>Thị phần Levart giành được trong nhóm đặt chỗ (%) <b id="vSh"></b></label><input id="sh" type="range" min="0" max="20" step="0.5" value="2">
<hr><b style="font-size:13px">Khuếch tán Bass — người dùng Levart ở Hà Nội 20–29 tuổi</b>
<label>Tiềm năng tối đa m (% của nhóm 20–29 Hà Nội) <b id="vM"></b></label><input id="m" type="range" min="1" max="30" step="1" value="10">
<label>Hệ số đổi mới p (tự tìm đến) <b id="vP"></b></label><input id="p" type="range" min="0.001" max="0.05" step="0.001" value="0.01">
<label>Hệ số bắt chước q (bạn rủ bạn) <b id="vQ"></b></label><input id="q" type="range" min="0.05" max="0.8" step="0.01" value="0.35">
<label>Rời bỏ mỗi quý (%) — chi phí chuyển đổi thấp thì cao <b id="vC"></b></label><input id="churn" type="range" min="0" max="50" step="1" value="20">
<p class="meta">Bass (1969): người dùng mới = (p + q·N/m)·(m − N). Thêm rời bỏ để thể hiện quyền lực người dùng (đổi app không tốn gì).</p></div>
<div class="card"><div id="pHhi" class="plot" style="min-height:300px"></div><div id="pBass" class="plot" style="min-height:330px"></div><div class="insight" id="iBass"></div><p class="src" data-ids="D4,D8,GT"></p></div></div>

<h2 id="s4">④ Đối thủ mới — gia nhập, sống sót, phân phối lượt cài</h2>
<div class="card"><div id="pCoh" class="plot" style="min-height:340px"></div><div class="insight" id="iCoh"></div><p class="src" data-ids="D2,D4"></p></div>

<h2 id="s5">⑤ Người dùng — điều gì làm họ “trừng phạt” một app</h2>
<div class="grid2"><div class="card ctl"><b style="font-size:13px">Mô phỏng một đánh giá: chọn các chủ đề được nhắc</b><div id="topicCtl"></div>
<label>Cửa hàng</label><select id="store"><option value="0">Google Play</option><option value="1">App Store</option></select>
<p class="meta">Xác suất đánh giá 1–2 sao từ hồi quy logistic trên 7.931 đánh giá của 10 app đặt chỗ (hiệu ứng cố định app lấy trung bình).</p></div>
<div class="card"><div id="pTop" class="plot" style="min-height:360px"></div><div class="insight" id="iTop"></div><p class="src" data-ids="D3,D5"></p></div></div>
<div class="card"><div id="pReply" class="plot" style="min-height:380px"></div><p class="src" data-ids="D3"></p></div>

<h2 id="s6">⑥ Nhà cung cấp địa phương — cơ cấu cung và thương lượng phí</h2>
<div class="card" id="provCard"><div id="pProv" class="plot" style="min-height:420px"></div><div class="insight" id="iProv"></div><p class="src" data-ids="D7"></p></div>
<div class="grid2"><div class="card ctl"><b style="font-size:13px">Thương lượng Nash: Levart ↔ chủ homestay</b>
<label>Giá phòng/đêm (nghìn đ) <b id="vAdr"></b></label><input id="adr" type="range" min="300" max="2500" step="25">
<label>Công suất phòng (%) <b id="vOcc"></b></label><input id="occ" type="range" min="10" max="95" step="1">
<label>Hoa hồng OTA (%) <b id="vOta"></b></label><input id="ota" type="range" min="10" max="30" step="1">
<label>Tỉ lệ lượt đặt qua OTA (%) <b id="vOs"></b></label><input id="os" type="range" min="10" max="90" step="1">
<label>Chi phí biến đổi/đêm (% giá) <b id="vMc"></b></label><input id="mc" type="range" min="5" max="60" step="1">
<label>Chi phí Levart kéo một đêm phòng (nghìn đ) <b id="vCl"></b></label><input id="cl" type="range" min="0" max="200" step="5">
<label>Sức thương lượng của Levart β <b id="vB"></b></label><input id="beta" type="range" min="0" max="1" step="0.05">
<button class="btn" onclick="resetNash()">Về giả định gốc</button></div>
<div class="card"><div id="pNash" class="plot" style="min-height:380px"></div><div class="insight" id="iNash"></div><p class="src" data-ids="S1,S2,GT"></p></div></div>

<h2 id="s7">⑦ Sản phẩm thay thế — quy mô và xu hướng</h2>
<div class="card"><div id="pSub" class="plot" style="min-height:420px"></div><div class="insight" id="iSub"></div><p class="src" data-ids="D2,D4,D6"></p></div>

<h2 id="s8">⑧ Tổng hợp 5 lực — tự chọn chỉ báo và trọng số</h2>
<div class="grid2"><div class="card ctl" id="indCtl"><b style="font-size:13px">Bật/tắt chỉ báo và đặt trọng số</b></div>
<div class="card"><div id="pRadar" class="plot" style="min-height:460px"></div><div class="insight" id="iRadar"></div><p class="src" data-ids="D1,D2,D3,D4,D5,D6,D7,D8,GT"></p></div></div>

<h2 id="src">Nguồn dữ liệu</h2>
<p class="meta">Tệp gốc, đường dẫn yêu cầu, thời điểm tải, mã băm: <code>data/raw/_MANIFEST.csv</code> · bảng đầy đủ: <code>SOURCES.md</code>.</p>
<div class="card" id="srcTable"></div>
</div>
<script>
const D=/*DATA*/;const S=D.S;
const C=['#2a78d6','#eb6834','#1baf7a','#eda100','#e87ba4','#008300','#6250d6','#e34948'],G='#e1e0d9';
const L=(t,x)=>Object.assign({title:{text:t,x:0,font:{size:15}},margin:{l:60,r:20,t:46,b:46},paper_bgcolor:'rgba(0,0,0,0)',plot_bgcolor:'rgba(0,0,0,0)',font:{family:'system-ui,Segoe UI,sans-serif',size:12},xaxis:{gridcolor:G,zeroline:false},yaxis:{gridcolor:G,zeroline:false},legend:{orientation:'h',y:-0.2}},x||{});
const CFG={displaylogo:false,responsive:true},$=id=>document.getElementById(id);
const fmt=(x,d=1)=>x==null?'–':Number(x).toLocaleString('vi-VN',{maximumFractionDigits:d,minimumFractionDigits:d});
function cite(ids){return 'Nguồn: '+ids.split(',').map(i=>i==='GT'?'[GT] giả định của nhóm':(D.src[i]?`<a href="${D.src[i].url||'#src'}" target="_blank" rel="noopener">[${i}] ${D.src[i].org} — ${D.src[i].name}</a>`:i)).join('; ')+' · tải '+D.accessed+'.'}
document.querySelectorAll('p.src').forEach(p=>p.innerHTML=cite(p.dataset.ids));
$('srcTable').innerHTML='<table><tr><th>Mã</th><th>Nguồn</th><th>Tổ chức</th><th>Nội dung</th><th>Mức</th></tr>'+Object.entries(D.src).map(([k,v])=>`<tr><td><b>${k}</b></td><td>${v.url?`<a href="${v.url}" target="_blank" rel="noopener">${v.name}</a>`:v.name}${v.api?'<br><code style="font-size:10.5px">'+v.api.replace(/</g,'&lt;')+'</code>':''}</td><td>${v.org}</td><td>${v.note}</td><td>${v.lv}</td></tr>`).join('')+'</table>';

// KPI
(function(){const b1=S.b1_overview||{},b2=S.b2_rivalry||{},b3=S.b3_entrants||{},b4=S.b4_buyers||{},f=(S.f_synthesis||{}).force_index||{};
 const c=b2.concentration?.vn_ratings_ios||{};
 const k=[[fmt(b1.gva_recovery_last_vs_2019,1),'chỉ số GTTT thực ngành lưu trú–ăn uống 2025 (2019 = 100)','D1'],
 [fmt(b1.gva_trend_2023_2025_pct,1)+'%/năm','nhịp tăng thực 2023–2025','D1'],
 ['HHI '+fmt(c.HHI,0),'nhóm đặt chỗ trên App Store VN; 3 hãng đầu '+fmt(c.CR3,1)+'%','D4'],
 [fmt(b3.p_new_reach_100k,1)+'%','app du lịch ra mắt từ 2022 đạt ≥ 100.000 lượt cài','D2'],
 ['×'+fmt(b4.logit_or?.['Hoàn tiền, huỷ']?.OR,1),'khả năng bị chấm 1–2 sao khi nhắc “hoàn tiền, huỷ”','D3, D5'],
 [fmt(f['Sản phẩm thay thế']?.score,2)+'/4','chỉ số sức ép của sản phẩm thay thế — mạnh nhất trong 5 lực','tổng hợp']];
 $('kpis').innerHTML=k.map(x=>`<div class="card kpi"><div class="v">${x[0]}</div><div class="l">${x[1]}</div><div class="src">Nguồn: ${x[2]}</div></div>`).join('')})();

// ① GVA
(function(){const b=S.b1_overview||{};const idx=b.gva_index_2019_100||{},g=b.gva_accom_growth||{};const ys=Object.keys(idx).map(Number).sort((a,c)=>a-c);
 Plotly.newPlot('pGva',[{x:Object.keys(g),y:Object.values(g),type:'bar',name:'Tăng trưởng thực (%)',marker:{color:Object.values(g).map(v=>v<0?C[7]:C[0])},xaxis:'x',yaxis:'y',hovertemplate:'%{x}: %{y:+.1f}%<extra></extra>'},
  {x:ys,y:ys.map(y=>idx[y]),mode:'lines+markers',name:'Chỉ số 2019 = 100',line:{color:C[1],width:2.5},xaxis:'x2',yaxis:'y2',hovertemplate:'%{x}: %{y:.1f}<extra></extra>'}],
  L('Ngành dịch vụ lưu trú và ăn uống — giá trị tăng thêm thực',{grid:{rows:1,columns:2,pattern:'independent'},xaxis:{gridcolor:G,title:'tăng trưởng (%)'},xaxis2:{gridcolor:G,title:'chỉ số'},yaxis2:{gridcolor:G}}),CFG);
 $('iGva').innerHTML=`Năm 2021 ngành chỉ còn <b>${fmt(idx[2021],1)}%</b> mức 2019; năm 2025 đạt <b>${fmt(idx[2025],1)}%</b>, nhịp tăng 2023–2025 <b>${fmt(b.gva_trend_2023_2025_pct,1)}%/năm</b>; 9 tháng 2026 tăng ${fmt(b.gva_accom_growth_9m_2026,2)}%. Ngành đang tăng nhanh hơn GDP — điều kiện làm dịu cạnh tranh trực diện.`;
 const e=b.online_estimates||[];Plotly.newPlot('pEst',e.map((r,i)=>({x:[r.y0,r.y1],y:[r.v0,r.v1],mode:'lines+markers',name:`${r.src}: ${fmt(r.cagr,1)}%/năm`,line:{color:C[i],width:2.5}})),L('Du lịch trực tuyến Việt Nam — 3 ước lượng (tỷ USD)'),CFG)})();

// ② positioning
const F=D.features,LEV0=D.X['Levart (mục tiêu)'].slice();let LEV=LEV0.slice();
const axOpts=[...F.map((f,i)=>({v:'f'+i,t:f})),{v:'pc1',t:'PC1'},{v:'pc2',t:'PC2'},{v:'pc3',t:'PC3'}];
['ax','ay','az'].forEach((id,j)=>{if(id==='az')$(id).insertAdjacentHTML('beforeend','<option value="">— 2D —</option>');axOpts.forEach(o=>$(id).insertAdjacentHTML('beforeend',`<option value="${o.v}">${o.t}</option>`))});
$('ax').value='pc1';$('ay').value='pc2';$('az').value='';
F.forEach((f,i)=>{$('levCtl').insertAdjacentHTML('beforeend',`<label>${f} <b id="lv${i}"></b></label><input id="l${i}" type="range" min="0" max="1" step="0.05" value="${LEV0[i]}">`);$('l'+i).addEventListener('input',posPlot)});
function resetLev(){LEV0.forEach((v,i)=>$('l'+i).value=v);posPlot()}
function val(n,a){if(a.startsWith('f'))return(n==='Levart (mục tiêu)'?LEV:D.X[n])[+a.slice(1)];const p=D.pos.find(r=>r.name===n);return p?p[a]:0}
function posPlot(){LEV=F.map((f,i)=>+$('l'+i).value);F.forEach((f,i)=>$('lv'+i).textContent=fmt(LEV[i],2));
 const ax=$('ax').value,ay=$('ay').value,az=$('az').value,names=Object.keys(D.X),types=[...new Set(D.pos.map(r=>r.type))];
 const tr=types.map((t,k)=>{const ns=names.filter(n=>(D.pos.find(r=>r.name===n)||{}).type===t);const lev=t==='Levart';
  const o={x:ns.map(n=>val(n,ax)),y:ns.map(n=>val(n,ay)),text:ns,name:t,mode:'markers+text',textposition:'top center',textfont:{size:10},marker:{size:lev?16:10,symbol:lev?'star':'circle',color:lev?C[1]:C[(k+2)%8],line:{color:'#fff',width:1}},hovertemplate:'%{text}<extra>'+t+'</extra>'};
  if(az){o.type='scatter3d';o.z=ns.map(n=>val(n,az))}return o});
 const t=n=>axOpts.find(o=>o.v===n)?.t||n;
 Plotly.react('pPos',tr,az?L('Không gian 3D',{scene:{xaxis:{title:t(ax)},yaxis:{title:t(ay)},zaxis:{title:t(az)}},margin:{l:0,r:0,t:40,b:0}}):L(`${t(ax)} × ${t(ay)}`,{xaxis:{title:t(ax),gridcolor:G},yaxis:{title:t(ay),gridcolor:G}}),CFG);
 const nrm=v=>Math.sqrt(v.reduce((a,b)=>a+b*b,0))||1,cos=(a,b)=>a.reduce((s,x,i)=>s+x*b[i],0)/(nrm(a)*nrm(b));
 const sims=names.filter(n=>n!=='Levart (mục tiêu)').map(n=>[n,cos(LEV,D.X[n]),Math.sqrt(D.X[n].reduce((s,x,i)=>s+(x-LEV[i])**2,0))]).sort((a,b)=>b[1]-a[1]);
 $('iPos').innerHTML=`Gần Levart nhất (cosine): ${sims.slice(0,3).map(s=>`<b>${s[0]}</b> ${fmt(s[1],2)}`).join(' · ')}. Khoảng cách tới app gần nhất: <b>${fmt(Math.min(...sims.map(s=>s[2])),2)}</b>. Cả ba app gần nhất đều là app nước ngoài, ít người dùng ở Việt Nam (xem ③) — ô “hộ chiếu, check-in, cộng đồng” chưa có người Việt chiếm.`}
['ax','ay','az'].forEach(i=>$(i).addEventListener('input',posPlot));posPlot();

// ③ HHI + Bass
const SH=(S.b2_rivalry?.concentration?.vn_ratings_ios?.shares)||{};
function hhiPlot(){const s=+$('sh').value;$('vSh').textContent=s+'%';const names=Object.keys(SH),base=names.map(n=>SH[n]);
 const after=base.map(v=>v*(1-s/100));const h0=base.reduce((a,v)=>a+v*v,0),h1=after.reduce((a,v)=>a+v*v,0)+s*s;
 Plotly.react('pHhi',[{y:names.concat(['Levart']),x:after.concat([s]),type:'bar',orientation:'h',marker:{color:names.map(()=>C[0]).concat([C[1]])},hovertemplate:'%{y}: %{x:.1f}%<extra></extra>'}],
  L(`Thị phần sau khi Levart giành ${s}% — HHI ${fmt(h0,0)} → ${fmt(h1,0)}`,{yaxis:{autorange:'reversed',automargin:true},margin:{l:10,r:20,t:46,b:30}}),CFG)}
function bass(){const m=+$('m').value/100*D.hn_young*1e6,p=+$('p').value,q=+$('q').value,ch=+$('churn').value/100;
 $('vM').textContent=$('m').value+'%';$('vP').textContent=p.toFixed(3);$('vQ').textContent=q.toFixed(2);$('vC').textContent=$('churn').value+'%';
 let N=0,cum=0,xs=[],act=[],cumu=[];for(let t=1;t<=12;t++){const nw=(p+q*N/m)*(m-N);cum+=nw;N=N+nw-ch*N;xs.push('Q'+t);act.push(N);cumu.push(cum)}
 Plotly.react('pBass',[{x:xs,y:act,name:'Người dùng hoạt động',line:{color:C[0],width:2.5}},{x:xs,y:cumu,name:'Tổng đã từng cài',line:{color:C[1],dash:'dash'}}],L('Khuếch tán Bass có rời bỏ — 12 quý (3 năm)'),CFG);
 $('iBass').innerHTML=`Sau 3 năm: <b>${fmt(act[11],0)}</b> người hoạt động trên <b>${fmt(cumu[11],0)}</b> người từng cài (tiềm năng m = ${fmt(m,0)}). Kéo “rời bỏ” xuống — tương đương tạo chi phí chuyển đổi bằng bộ sưu tập con dấu — để thấy nó quan trọng hơn cả hệ số lan truyền q.`}
['sh'].forEach(i=>$(i).addEventListener('input',hhiPlot));['m','p','q','churn'].forEach(i=>$(i).addEventListener('input',bass));hhiPlot();bass();

// ④ cohorts
(function(){const b=S.b3_entrants||{},c=b.cohorts||{},ys=Object.keys(c).sort();
 Plotly.newPlot('pCoh',[{x:ys,y:ys.map(y=>c[y].play_new),type:'bar',name:'Google Play',marker:{color:C[0]}},{x:ys,y:ys.map(y=>c[y].ios_new),type:'bar',name:'App Store VN',marker:{color:C[1]}}],L('App du lịch mới theo năm ra mắt (còn trên cửa hàng)',{barmode:'group'}),CFG);
 $('iCoh').innerHTML=`Từ 2022 có <b>${b.n_new_since_2022}</b> app du lịch mới trên Google Play trong mẫu; chỉ <b>${fmt(b.p_new_reach_100k,1)}%</b> đạt 100.000 lượt cài và <b>${fmt(b.p_new_reach_1m,1)}%</b> đạt 1 triệu. Lượt cài cực kỳ dồn: Gini <b>${fmt(b.gini_installs,3)}</b>, 1% app lớn nhất giữ <b>${fmt(b.top1pct_share,1)}%</b> tổng lượt cài. Vào thì dễ, lớn lên thì rất khó.`})();

// ⑤ topics + logit simulator
const TP=Object.keys((S.b4_buyers||{}).prevalence?.['1–2 sao']||{}),LP=(S.b4_buyers||{}).logit_params||{},FE=Object.values((S.b4_buyers||{}).logit_app_fe||{});
const feMean=FE.length?FE.reduce((a,b)=>a+b,0)/(FE.length+1):0;
TP.forEach((t,i)=>$('topicCtl').insertAdjacentHTML('beforeend',`<label class="chk"><input type="checkbox" id="t${i}"> ${t}</label>`));
function topics(){const pr=S.b4_buyers.prevalence;Plotly.react('pTop',[{y:TP,x:TP.map(t=>pr['1–2 sao'][t]),type:'bar',orientation:'h',name:'1–2 sao',marker:{color:C[7]}},{y:TP,x:TP.map(t=>pr['4–5 sao'][t]),type:'bar',orientation:'h',name:'4–5 sao',marker:{color:C[0]}}],
  L('% đánh giá nhắc tới chủ đề',{barmode:'group',yaxis:{autorange:'reversed',automargin:true},margin:{l:10,r:20,t:46,b:40}}),CFG);
 let z=(LP.const||0)+feMean+(+$('store').value?(LP['App Store']||0):0);TP.forEach((t,i)=>{if($('t'+i).checked)z+=LP[t]||0});const pn=1/(1+Math.exp(-z));
 $('iTop').innerHTML=`Xác suất một đánh giá như vậy là <b>1–2 sao: ${fmt(pn*100,0)}%</b>. Thử bật “Hoàn tiền, huỷ” hoặc “Đặt chỗ thất bại”: xác suất nhảy vọt; bật “Giá, phí” gần như không đổi — người dùng phạt khi dịch vụ hỏng, không phạt vì giá.`}
TP.forEach((t,i)=>$('t'+i).addEventListener('change',topics));$('store').addEventListener('input',topics);topics();
(function(){const per=S.b4_buyers.per_app_play,ns=Object.keys(per).sort((a,b)=>per[a].reply-per[b].reply);
 Plotly.newPlot('pReply',[{y:ns,x:ns.map(n=>per[n].reply*100),type:'bar',orientation:'h',name:'% được trả lời',marker:{color:C[0]}},{y:ns,x:ns.map(n=>per[n].neg*100),type:'bar',orientation:'h',name:'% 1–2 sao',marker:{color:C[7]}}],L('Google Play VN — 500 đánh giá mới nhất mỗi app',{barmode:'group',yaxis:{automargin:true},margin:{l:10,r:20,t:46,b:40}}),CFG)})();

// ⑥ provinces + Nash
(function(){if(!D.prov.length){$('provCard').innerHTML='<p class="meta">Dữ liệu OpenStreetMap theo tỉnh chưa tải xong khi dựng bảng điều khiển.</p>';return}
 const p=D.prov.slice().sort((a,b)=>b.n-a.n);Plotly.newPlot('pProv',[{x:p.map(r=>r.province),y:p.map(r=>r.n),type:'bar',name:'Số cơ sở lưu trú (OSM)',marker:{color:C[0]},yaxis:'y'},
  {x:p.map(r=>r.province),y:p.map(r=>r.chain_share),type:'bar',name:'% thuộc chuỗi',marker:{color:C[1]},xaxis:'x2',yaxis:'y2'}],
  L('Cơ sở lưu trú theo tỉnh — số lượng và % thuộc chuỗi',{grid:{rows:2,columns:1,pattern:'independent'},yaxis:{gridcolor:G,title:'số cơ sở'},yaxis2:{gridcolor:G,title:'% chuỗi'},xaxis:{showticklabels:false},margin:{l:60,r:20,t:46,b:110}}),CFG);
 const b5=S.b5_suppliers||{};$('iProv').innerHTML=`${fmt(b5.n_lodging,0)} cơ sở lưu trú ở ${b5.n_provinces} tỉnh; chỉ <b>${fmt(b5.chain_share_pct,2)}%</b> thuộc chuỗi/thương hiệu; chỉ số phân mảnh toàn quốc <b>${fmt(b5.fragmentation_national,4)}</b> (1 = phân mảnh hoàn toàn). Bên cung rời rạc → từng cơ sở có rất ít quyền lực với một nền tảng.`})();
const NA=(S.b5_suppliers||{}).bargaining?.assume||{adr_vnd:974000,occupancy:.4533,ota_commission:.18,ota_share:.5885,marginal_cost_share:.25,levart_cost_vnd:40000,beta_levart:.3};
function resetNash(){$('adr').value=NA.adr_vnd/1000;$('occ').value=Math.round(NA.occupancy*100);$('ota').value=Math.round(NA.ota_commission*100);$('os').value=Math.round(NA.ota_share*100);$('mc').value=Math.round(NA.marginal_cost_share*100);$('cl').value=NA.levart_cost_vnd/1000;$('beta').value=NA.beta_levart;nash()}
function nashF(adr,mc,c,k,cl,b){const d=k*(adr*(1-c)-mc*adr),fmin=cl/adr,fmax=(adr-mc*adr-d)/adr,sur=adr-mc*adr-d-cl;return[fmin,fmax,sur>0?(cl+b*sur)/adr:NaN]}
function nash(){const adr=+$('adr').value*1000,occ=+$('occ').value/100,c=+$('ota').value/100,os=+$('os').value/100,mc=+$('mc').value/100,cl=+$('cl').value*1000,b=+$('beta').value;
 $('vAdr').textContent=$('adr').value;$('vOcc').textContent=$('occ').value+'%';$('vOta').textContent=$('ota').value+'%';$('vOs').textContent=$('os').value+'%';$('vMc').textContent=$('mc').value+'%';$('vCl').textContent=$('cl').value;$('vB').textContent=b.toFixed(2);
 const ks=[...Array(101).keys()].map(i=>i/100),r=ks.map(k=>nashF(adr,mc,c,k,cl,b)),k0=occ*os,f0=nashF(adr,mc,c,k0,cl,b);
 Plotly.react('pNash',[{x:ks.map(k=>k*100),y:r.map(v=>v[1]*100),name:'Trần phí (homestay hoà)',line:{color:C[2]}},{x:ks.map(k=>k*100),y:r.map(v=>v[0]*100),name:'Sàn phí (Levart hoà vốn)',line:{color:C[7]},fill:'tonexty',fillcolor:'rgba(205,226,251,.5)'},
  {x:ks.map(k=>k*100),y:r.map(v=>v[2]*100),name:'Nghiệm Nash',line:{color:C[0],width:3}},{x:[k0*100],y:[f0[2]*100],mode:'markers',marker:{size:12,color:C[1]},name:'Hiện tại'}],
  L('Phí Levart khả thi theo k = % đêm phòng vẫn đến qua OTA nếu không có Levart',{xaxis:{title:'k (%)',gridcolor:G},yaxis:{title:'% giá phòng',range:[0,100],gridcolor:G},shapes:[{type:'rect',xref:'paper',x0:0,x1:1,y0:15,y1:25,fillcolor:'rgba(235,104,52,.10)',line:{width:0}}]}),CFG);
 $('iNash').innerHTML=`Ở công suất ${fmt(occ*100,0)}% và ${fmt(os*100,0)}% lượt đặt qua OTA, k ≈ <b>${fmt(k0*100,0)}%</b>: phí cả hai cùng có lợi nằm trong <b>${fmt(f0[0]*100,1)}%–${fmt(f0[1]*100,1)}%</b>, nghiệm Nash <b>${fmt(f0[2]*100,1)}%</b> (dải cam = hoa hồng OTA 15–25%). Kéo công suất lên cao: trần phí sụt — homestay đông khách sẽ không cần Levart.`}
['adr','occ','ota','os','mc','cl','beta'].forEach(i=>$(i).addEventListener('input',nash));resetNash();

// ⑦ substitutes
(function(){const cs=D.core.filter(r=>r.real).sort((a,b)=>a.real-b.real);const grp=t=>['Bản đồ','Trợ lý AI','Mạng xã hội','Siêu ứng dụng'].includes(t)?0:(['Lập lịch trình','Sưu tầm, hành trình'].includes(t)?2:1);
 Plotly.newPlot('pSub',[{y:cs.map(r=>r.name),x:cs.map(r=>r.real/1e6),type:'bar',orientation:'h',marker:{color:cs.map(r=>[C[7],C[0],C[2]][grp(r.type)])},hovertemplate:'%{y}: %{x:,.1f} triệu<extra></extra>'}],
  L('Lượt cài Google Play (triệu, thang log) — đỏ: sản phẩm thay thế · xanh lá: lên kế hoạch, sưu tầm · xanh dương: đặt chỗ',{xaxis:{type:'log',gridcolor:G},yaxis:{automargin:true},margin:{l:10,r:20,t:46,b:40}}),CFG);
 const b=S.b6_substitutes||{};$('iSub').innerHTML=`Sản phẩm thay thế có lượt cài gấp <b>${fmt(b.substitute_to_planning_installs_ratio,0)} lần</b> các app lên kế hoạch, sưu tầm; năm 2025 người Việt tìm “chatgpt” nhiều gấp <b>${fmt(b.chatgpt_vs_lichtrinh_2025,1)} lần</b> “lịch trình”; tìm “tour du lịch” giảm ${fmt(b.tour_search_change_2019_2025_pct,1)}% so với 2019.`})();

// ⑧ synthesis
const IND=(S.f_synthesis||{}).indicators||[],ORD=['Cạnh tranh hiện tại','Đối thủ mới gia nhập','Quyền lực người dùng','Nhà cung cấp — địa phương','Nhà cung cấp — công nghệ','Sản phẩm thay thế'],DESK=(S.f_synthesis||{}).desk||{};
IND.forEach((r,i)=>$('indCtl').insertAdjacentHTML('beforeend',`<label class="chk"><input type="checkbox" id="c${i}" checked> <span><b>${r.force}</b>: ${r.indicator} = ${r.value} → ${fmt(r.score,2)}</span></label><input id="w${i}" type="range" min="0" max="3" step="0.5" value="1">`));
function radar(){const sc=ORD.map(f=>{let s=0,w=0;IND.forEach((r,i)=>{if(r.force===f&&$('c'+i).checked){const ww=+$('w'+i).value;s+=r.score*ww;w+=ww}});return w?s/w:0});
 Plotly.react('pRadar',[{type:'scatterpolar',r:ORD.map(f=>DESK[f]).concat([DESK[ORD[0]]]),theta:ORD.concat([ORD[0]]),name:'Tại bàn',line:{color:C[0]},fill:'toself',fillcolor:'rgba(42,120,214,.08)'},
  {type:'scatterpolar',r:sc.concat([sc[0]]),theta:ORD.concat([ORD[0]]),name:'Đo từ dữ liệu',line:{color:C[1],dash:'dash'},fill:'toself',fillcolor:'rgba(235,104,52,.08)'}],
  L('Hồ sơ 5 lực (0–4)',{polar:{radialaxis:{range:[0,4]}},legend:{orientation:'h',y:-0.1}}),CFG);
 const top=ORD.map((f,i)=>[f,sc[i]]).sort((a,b)=>b[1]-a[1]);$('iRadar').innerHTML='Mạnh nhất: '+top.slice(0,2).map(t=>`<b>${t[0]}</b> ${fmt(t[1],2)}`).join(' · ')+'. Yếu nhất: '+top.slice(-1).map(t=>`<b>${t[0]}</b> ${fmt(t[1],2)}`).join('')+'.'}
IND.forEach((r,i)=>{$('c'+i).addEventListener('change',radar);$('w'+i).addEventListener('input',radar)});radar();
</script></body></html>"""

if __name__ == "__main__":
    build()
