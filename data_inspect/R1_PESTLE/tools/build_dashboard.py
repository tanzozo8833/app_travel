"""Dựng data_inspect/Levart_Market_Dashboard.html — chạy offline (plotly.js nhúng sẵn).

Đọc kết quả trong results/ và data/processed/, nhúng thành JSON; mọi mô phỏng tính lại bằng JavaScript
ngay trong trình duyệt khi kéo thanh trượt.
"""
import json

import numpy as np
import pandas as pd
from plotly.offline import get_plotlyjs

from common import PROC, RES, ROOT
from analyze_market import ASSUME, fit_student_spending, flight_fare_model
from sources import ACCESSED, SOURCES


def build():
    S = json.loads((RES / "summary.json").read_text(encoding="utf-8"))
    dest = pd.read_csv(PROC / "destinations.csv")
    a5 = pd.read_csv(RES / "a5_destinations.csv")
    si = pd.read_csv(RES / "a5_seasonal_index_trends.csv", index_col=0)
    clim = pd.read_csv(RES / "a6_climate_monthly.csv")
    gap = pd.read_csv(RES / "a6_offseason_gap.csv", index_col=0)
    mc = pd.read_csv(RES / "a12_mcda.csv")
    pop = pd.read_csv(PROC / "wb_pop_age.csv", dtype={"age": str})
    t = pd.read_csv(PROC / "trends_long.csv", parse_dates=["month"])
    t["scaled"] = t.raw / t.groupby("group").anchor_raw.transform("mean") * 100
    os_ = pd.read_csv(PROC / "statcounter_os_mobile.csv")
    jf = pd.read_csv(PROC / "fred_DJFUELUSGULF.csv")

    med_s, sig_s, _ = fit_student_spending()
    a, b = flight_fare_model()
    g = S.get("a7_gravity", {})
    attract = g.get("attractiveness", {})

    D = {
        "summary": S,
        "dest": [dict(key=r.key, name=r["name"], type=r.type, lat=r.lat, lon=r.lon, km=r.road_km, h=r.drive_h,
                      A=attract.get(r["name"], attract.get(r.key, 10)),
                      si=[round(float(x), 3) for x in si.loc[r["name"]].values] if r["name"] in si.index else [1] * 12)
                 for _, r in dest.iterrows()],
        "assume": ASSUME, "student": {"median": med_s, "sigma": sig_s}, "fare": {"a": a, "b": b},
        "beta": g.get("beta", 1.0),
        "pop": {age: pop[(pop.iso3 == "VNM") & (pop.age == age)].groupby("year")["pop"].sum().round(-3).to_dict()
                for age in ["1519", "2024", "2529", "3034"]},
        "kw": {kw: [d.month.dt.strftime("%Y-%m").tolist(), d.scaled.round(2).tolist()]
               for kw, d in t[~t.group.str.startswith("dest")].groupby("kw")},
        "kwcls": {r["kw"]: r["cls"] for r in S.get("a4_trends", {}).get("classification", [])},
        "clim_names": sorted(clim.name.unique().tolist()),
        "comfort": {n: d.sort_values("m").comfort.round(1).tolist() for n, d in clim.groupby("name")},
        "heavy": {n: d.sort_values("m").heavy.round(2).tolist() for n, d in clim.groupby("name")},
        "gap": {n: [round(float(x), 2) for x in gap.loc[n].values] for n in gap.index},
        "mcda_crit": [c for c in mc.columns if c not in ("name", "score", "p_top3", "median_rank")],
        "mcda": mc.to_dict("records"),
        "mcda_w": list(S.get("a12_mcda", {}).get("weights", {}).values()),
        "os": [os_.Date.tolist(), os_.Android.tolist(), os_.iOS.tolist()],
        "fuel": [jf.date.str[:7].tolist(), jf.DJFUELUSGULF.round(3).tolist()],
        "a5": a5.to_dict("records"),
        "src": {k: {"name": v[0], "org": v[1], "url": v[2], "api": v[3], "note": v[4], "lv": v[5]} for k, v in SOURCES.items()},
        "accessed": ACCESSED,
    }
    data = json.dumps(D, ensure_ascii=False, default=lambda o: None if (isinstance(o, float) and np.isnan(o)) else float(o))
    data = data.replace("NaN", "null")
    html = TEMPLATE.replace("/*PLOTLY*/", get_plotlyjs()).replace("/*DATA*/", data)
    out = ROOT / "Levart_Market_Dashboard.html"
    out.write_text(html, encoding="utf-8")
    print("dashboard ->", out, f"{out.stat().st_size / 1e6:.1f} MB")


TEMPLATE = r"""<!doctype html><html lang="vi"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Levart – Mô phỏng thị trường</title>
<style>
:root{--bg:#f9f9f7;--card:#fcfcfb;--ink:#0b0b0b;--ink2:#52514e;--mut:#7a7872;--grid:#e1e0d9;--ring:rgba(11,11,11,.10);
--s1:#2a78d6;--s2:#eb6834;--s3:#1baf7a;--s4:#eda100;--acc:#256abf}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:14.5px/1.55 system-ui,-apple-system,"Segoe UI",sans-serif}
.wrap{max-width:1200px;margin:0 auto;padding:22px 16px 60px}
h1{font-size:28px;margin:4px 0 4px;letter-spacing:-.01em}h2{font-size:20px;margin:36px 0 4px;padding-top:12px;border-top:1px solid var(--grid)}
.lede{color:var(--ink2);max-width:860px;margin:4px 0 14px}.meta{color:var(--mut);font-size:12.5px}
.card{background:var(--card);border-radius:12px;box-shadow:0 0 0 1px var(--ring);padding:14px 16px;margin:12px 0}
.kpis{display:grid;grid-template-columns:repeat(auto-fill,minmax(min(210px,100%),1fr));gap:10px;margin:14px 0}
.kpi .v{font-size:26px;font-weight:650;line-height:1.15}.kpi .l{color:var(--ink2);font-size:13px;margin-top:3px}
.grid2{display:grid;grid-template-columns:minmax(0,320px) minmax(0,1fr);gap:16px;align-items:start}
@media(max-width:820px){.grid2{grid-template-columns:1fr}}
.ctl label{display:block;font-size:12.5px;color:var(--ink2);margin:10px 0 2px}.ctl label b{color:var(--ink);float:right;font-variant-numeric:tabular-nums}
.ctl input[type=range]{width:100%}.ctl select{width:100%;padding:5px;font:inherit;border-radius:6px;border:1px solid var(--grid);background:#fff}
.insight{border-left:3px solid var(--acc);padding:8px 12px;background:#fff;border-radius:0 8px 8px 0;margin:10px 0;font-size:13.5px;box-shadow:0 0 0 1px var(--ring)}
.plot{width:100%;min-height:380px}table{border-collapse:collapse;width:100%;font-size:12.5px}th,td{padding:5px 7px;border-bottom:1px solid var(--grid);text-align:left}
th{color:var(--ink2);font-weight:650;background:#f3f2ee}td.n{text-align:right;font-variant-numeric:tabular-nums}
.tag{font-size:11px;padding:1px 7px;border-radius:999px;box-shadow:inset 0 0 0 1px var(--grid);white-space:nowrap}
nav.toc{display:flex;flex-wrap:wrap;gap:6px;margin:8px 0 6px}nav.toc a{font-size:12.5px;text-decoration:none;color:var(--ink2);padding:3px 10px;border-radius:999px;box-shadow:inset 0 0 0 1px var(--ring)}
.src{font-size:11.5px;color:var(--mut);margin:8px 0 0}.src a{color:var(--acc)}
.btn{font:inherit;font-size:12.5px;padding:5px 10px;border-radius:6px;border:1px solid var(--grid);background:#fff;cursor:pointer;margin-top:10px}
</style><script>/*PLOTLY*/</script></head><body><div class="wrap">
<div class="meta">LEVART · data_inspect · dữ liệu tải 25/10/2026 · mọi con số tính lại từ dữ liệu gốc bằng tools/*.py</div>
<h1>Mô phỏng thị trường du lịch người trẻ — Levart</h1>
<p class="lede">Bảng điều khiển này đi kèm <code>report.md</code>. Mỗi phần là một mô hình kinh tế chạy thật trong trình duyệt: kéo thanh trượt để đổi giả định và xem kết luận có còn đứng vững không.</p>
<nav class="toc"><a href="#src">Nguồn dữ liệu</a><a href="#s1">① Nhân khẩu × thu nhập</a><a href="#s2">② Khả năng chi trả</a><a href="#s3">③ Bản đồ thị trường (Huff)</a><a href="#s4">④ Mùa vụ & lệch mùa</a><a href="#s5">⑤ Xu hướng hay mốt</a><a href="#s6">⑥ Chọn điểm thí điểm</a><a href="#s7">⑦ Quy mô thị trường</a><a href="#s8">⑧ Nền tảng & nhiên liệu</a></nav>
<div class="kpis" id="kpis"></div>

<h2 id="s1">① Nhân khẩu × thu nhập: thị trường người trẻ đang ở đáy về quy mô</h2>
<div class="card"><div id="pPop" class="plot"></div><div class="insight" id="iPop"></div><p class="src" data-ids="D2,D1,D3"></p></div>

<h2 id="s2">② Mô phỏng khả năng chi trả — ai đủ tiền đi đâu (chuyến 3 ngày 2 đêm từ Hà Nội)</h2>
<div class="grid2"><div class="card ctl">
<label>Thu nhập trung vị người đi làm 22–29 (triệu/tháng) <b id="vInc"></b></label><input id="inc" type="range" min="5" max="16" step="0.5">
<label>Ngân sách 1 chuyến (% thu nhập tháng) <b id="vShare"></b></label><input id="share" type="range" min="20" max="80" step="5">
<label>Cú sốc nhiên liệu bay so với 2026 (%) <b id="vFuel"></b></label><input id="fuel" type="range" min="-60" max="100" step="5">
<label>Truyền dẫn sang giá vé (hệ số) <b id="vPt"></b></label><input id="pt" type="range" min="0" max="0.6" step="0.01">
<label>Giá phòng/người/đêm (nghìn đ) <b id="vLodge"></b></label><input id="lodge" type="range" min="100" max="600" step="25">
<label>Giờ xe tối đa mỗi chiều chấp nhận <b id="vBusH"></b></label><input id="busH" type="range" min="6" max="20" step="1">
<button class="btn" onclick="resetAff()">Về giả định gốc</button>
<p class="meta">Thu nhập: log-chuẩn σ=0,55 (GT). Sinh viên: log-chuẩn ước lượng từ 4 nhóm chi tiêu Cốc Cốc 2025, ngân sách = 1,5 tháng chi tiêu. Giá vé bay: hồi quy giá khứ hồi thực tế hè 2026 theo cự ly.</p>
</div><div class="card"><div id="pAff" class="plot" style="min-height:560px"></div><div class="insight" id="iAff"></div><p class="src" data-ids="S2,S3,S4,S5,S1,D8,GT"></p></div></div>

<h2 id="s3">③ Bản đồ thị trường — mô hình lựa chọn không gian Huff</h2>
<div class="grid2"><div class="card ctl">
<label>Nơi xuất phát</label><select id="org"><option value="21.03,105.85">Hà Nội</option><option value="10.78,106.70">TP. Hồ Chí Minh</option><option value="16.05,108.20">Đà Nẵng</option><option value="20.86,106.68">Hải Phòng</option><option value="18.67,105.68">Vinh (Nghệ An)</option></select>
<label>Tháng đi <b id="vMon"></b></label><input id="mon" type="range" min="0" max="12" step="1">
<label>Độ nhạy khoảng cách β <b id="vBeta"></b></label><input id="beta" type="range" min="0.2" max="2.5" step="0.05">
<label>Cú sốc chi phí đi xa (>450 km), % <b id="vFar"></b></label><input id="far" type="range" min="0" max="60" step="5">
<label>Levart đẩy sức hút của</label><select id="boostDest"></select>
<label>Mức tăng sức hút (%) <b id="vBoost"></b></label><input id="boost" type="range" min="0" max="100" step="5">
<p class="meta">P(j) ∝ Sức hút<sub>j</sub> × Mùa<sub>j</sub>(tháng) × khoảng cách<sup>−β</sup>. β ước lượng bằng PPML trên dữ liệu Google Trends theo 63 tỉnh; sức hút = mức quan tâm toàn quốc ÷ khả năng tiếp cận trung bình (đã loại ảnh hưởng khoảng cách). Tháng 0 = cả năm.</p>
</div><div class="card"><div id="pMap" class="plot" style="min-height:600px"></div><div class="insight" id="iMap"></div><p class="src" data-ids="D9,D6,D7"></p></div></div>

<h2 id="s4">④ Mùa vụ, khí hậu và cửa sổ lệch mùa</h2>
<div class="card ctl" style="max-width:420px"><label>Lớp dữ liệu</label><select id="heatSel">
<option value="si">Chỉ số mùa vụ tìm kiếm (Google Trends, 1 = TB năm)</option><option value="comfort">Tiện nghi khí hậu (0–100, ERA5 2005–2025)</option>
<option value="heavy">Số ngày mưa ≥50 mm / tháng</option><option value="gap">Cửa sổ lệch mùa = z(khí hậu) − z(nhu cầu)</option></select></div>
<div class="card"><div id="pHeat" class="plot" style="min-height:620px"></div><div class="insight" id="iHeat"></div><p class="src" data-ids="D9,D11"></p></div>

<h2 id="s5">⑤ Xu hướng hay mốt — 20 từ khoá Google Trends 2016–2026</h2>
<div class="grid2"><div class="card ctl"><label>Từ khoá</label><select id="kwSel" multiple size="14"></select>
<p class="meta">Giữ Ctrl để chọn nhiều. Thang chung: “du lịch” = 100 (trung bình). Kết luận theo luật PLAN_RESEARCH 3.1, đo bằng Mann–Kendall.</p></div>
<div class="card"><div id="pKw" class="plot"></div><div id="tKw"></div><p class="src" data-ids="D9"></p></div></div>

<h2 id="s6">⑥ Chọn điểm đến thí điểm (QĐ3) — phân tích đa tiêu chí, tự chỉnh trọng số</h2>
<div class="grid2"><div class="card ctl" id="mcdaCtl"><button class="btn" onclick="resetW()">Về trọng số gốc</button></div>
<div class="card"><div id="pMcda" class="plot" style="min-height:520px"></div><div class="insight" id="iMcda"></div><p class="src" data-ids="D8,D9,D11,D12,D10,S3,GT"></p></div></div>

<h2 id="s7">⑦ Quy mô thị trường — Monte Carlo (khách Hà Nội 20–29 tuổi)</h2>
<div class="grid2"><div class="card ctl" id="mcCtl"><button class="btn" onclick="runMC()">Chạy lại 20.000 lượt</button></div>
<div class="card"><div id="pMc" class="plot"></div><div class="insight" id="iMc"></div><p class="src" data-ids="D6,D2,S6,S7,S8,GT"></p></div></div>

<h2 id="s8">⑧ Nền tảng di động và cú sốc nhiên liệu</h2>
<div class="card"><div id="pOs" class="plot" style="min-height:340px"></div><p class="src" data-ids="D4"></p></div>
<div class="card"><div id="pFuel" class="plot" style="min-height:300px"></div><p class="src" data-ids="D5,S1"></p></div>
<h2 id="src">Nguồn dữ liệu</h2>
<p class="meta">Mỗi tệp gốc có đường dẫn yêu cầu, thời điểm tải và mã băm trong <code>data/raw/_MANIFEST.csv</code>; bảng đầy đủ trong <code>SOURCES.md</code>. [GT] = giả định của nhóm, không có nguồn.</p>
<div class="card" id="srcTable"></div>
</div>
<script>
const D=/*DATA*/;
const C={s1:'#2a78d6',s2:'#eb6834',s3:'#1baf7a',s4:'#eda100',s5:'#e87ba4',s6:'#008300',s7:'#6250d6',s8:'#e34948',grid:'#e1e0d9',ink2:'#52514e'};
const L=(t,extra)=>Object.assign({title:{text:t,x:0,font:{size:15}},margin:{l:60,r:20,t:46,b:46},paper_bgcolor:'rgba(0,0,0,0)',plot_bgcolor:'rgba(0,0,0,0)',
 font:{family:'system-ui,Segoe UI,sans-serif',size:12,color:'#0b0b0b'},xaxis:{gridcolor:C.grid,zeroline:false},yaxis:{gridcolor:C.grid,zeroline:false},
 legend:{orientation:'h',y:-0.18},hoverlabel:{bgcolor:'#0b0b0b'}},extra||{});
const CFG={displaylogo:false,responsive:true};
const $=id=>document.getElementById(id);
const fmt=(x,d=1)=>Number(x).toLocaleString('vi-VN',{maximumFractionDigits:d,minimumFractionDigits:d});
// ---- phân phối chuẩn
function Phi(x){const t=1/(1+0.2316419*Math.abs(x));const d=0.3989423*Math.exp(-x*x/2);let p=d*t*(0.3193815+t*(-0.3565638+t*(1.781478+t*(-1.821256+t*1.330274))));return x>0?1-p:p}
function randn(){let u=0,v=0;while(!u)u=Math.random();while(!v)v=Math.random();return Math.sqrt(-2*Math.log(u))*Math.cos(2*Math.PI*v)}
function tri(a,m,b){const u=Math.random(),f=(m-a)/(b-a);return u<f?a+Math.sqrt(u*(b-a)*(m-a)):b-Math.sqrt((1-u)*(b-a)*(b-m))}
function hav(a,b,c,d){const R=6371,r=Math.PI/180,p1=a*r,p2=c*r,dp=(c-a)*r,dl=(d-b)*r;const x=Math.sin(dp/2)**2+Math.cos(p1)*Math.cos(p2)*Math.sin(dl/2)**2;return 2*R*Math.asin(Math.sqrt(x))}

// ---- nguồn
function cite(ids){return 'Nguồn: '+ids.split(',').map(i=>i==='GT'?'[GT] giả định của nhóm':(D.src[i]?`<a href="${D.src[i].url}" target="_blank" rel="noopener">[${i}] ${D.src[i].org} — ${D.src[i].name}</a>`:i)).join('; ')+' · tải '+D.accessed+'.'}
document.querySelectorAll('p.src').forEach(p=>p.innerHTML=cite(p.dataset.ids));
$('srcTable').innerHTML='<table><tr><th>Mã</th><th>Nguồn</th><th>Tổ chức</th><th>Nội dung / con số dùng</th><th>Mức</th></tr>'+Object.entries(D.src).map(([k,v])=>`<tr><td><b>${k}</b></td><td><a href="${v.url}" target="_blank" rel="noopener">${v.name}</a>${v.api?'<br><code style="font-size:10.5px">'+v.api.replace(/</g,'&lt;')+'</code>':''}</td><td>${v.org}</td><td>${v.note}</td><td>${v.lv}</td></tr>`).join('')+'</table>';

// ---- KPI
(function(){const S=D.summary,a1=S.a1_demography||{},a2=S.a2_purchasing_power||{},a4=S.a4_trends||{},a9=S.a9_mobile_os||{},a11=S.a11_market||{},a7=S.a7_gravity||{};
 const k=[[fmt(a1.pop_20_29?.['2025']??0,1)+' tr','người 20–29 tuổi năm 2025 — đáy của chu kỳ 2015–2035; '+fmt(a1.change_2015_2025_pct??0,1)+'% so với 2015'],
 ['+'+fmt(a2.value_change_2025_2035_pct??0,0)+'%','giá trị chi tiêu của nhóm 20–29 giai đoạn 2025→2035 (quy mô × thu nhập)'],
 ['+'+fmt(a4.fuel_substitution?.bus?.trend_pct_per_year??0,1)+'%/năm','tìm “xe khách” so với “vé máy bay” tăng đều từ 2017 (đã trừ mùa vụ); riêng cú sốc 2026: +'+fmt(a4.fuel_substitution?.bus?.shock_effect_pct??0,0)+'%, p='+(a4.fuel_substitution?.bus?.p??'')],
 [fmt(a9.ios_latest??0,1)+'%','lượt truy cập web điện thoại từ iOS ('+(a9.ios_latest_month||'')+'), tăng từ '+(a9.ios_low_2023??'')+'% năm 2023'],
 ['β = '+fmt(a7.beta??0,2),'độ suy giảm quan tâm theo khoảng cách (mô hình trọng lực)'],
 [(a11.revenue_bn_vnd?.[1]??'–')+' tỷ','doanh thu hoa hồng/năm (P50) nếu chỉ phục vụ khách Hà Nội 20–29 sau 3 năm']];
 const ks=['D2','D1, D2, D3','D9','D4','D9, D6','D6, D2, S6–S8, GT'];
 $('kpis').innerHTML=k.map((x,i)=>`<div class="card kpi"><div class="v">${x[0]}</div><div class="l">${x[1]}</div><div class="src">Nguồn: ${ks[i]} (<a href="#src">xem bảng</a>)</div></div>`).join('')})();

// ---- ① dân số
(function(){const ages={'1519':'15–19','2024':'20–24','2529':'25–29','3034':'30–34'},cols=[C.s4,C.s1,C.s2,C.s3];
 const tr=Object.keys(ages).map((a,i)=>{const o=D.pop[a],ys=Object.keys(o).map(Number).sort((x,y)=>x-y);return{x:ys,y:ys.map(y=>o[y]/1e6),name:ages[a]+' tuổi',line:{color:cols[i],width:a==='2024'||a==='2529'?3:1.6},hovertemplate:'%{x}: %{y:.2f} triệu<extra>'+ages[a]+'</extra>'}});
 Plotly.newPlot('pPop',tr,L('Số người theo nhóm tuổi (triệu) — 1990–2050, sau 2025 là dự báo',{shapes:[{type:'rect',x0:2025,x1:2050,y0:0,y1:1,yref:'paper',fillcolor:'#f3f2ee',line:{width:0},layer:'below'}]}),CFG);
 const a1=D.summary.a1_demography||{},a2=D.summary.a2_purchasing_power||{};
 $('iPop').innerHTML=`Nhóm 20–29 tuổi giảm <b>${a1.change_2015_2025_pct}%</b> từ 2015 tới 2025 và chạm đáy năm <b>${a1.trough_year}</b> (${a1.trough_value} triệu), rồi tăng lại <b>+${a1.change_2025_2035_pct}%</b> tới 2035 — ngược hẳn Thái Lan (${a1.peers_2025_2035_pct?.['Thái Lan']}%). Kết hợp với chi tiêu/người thực tăng, giá trị thị trường người trẻ <b>tăng ~${fmt(a2.value_change_2025_2035_pct,0)}%</b> trong 10 năm tới (đóng góp: quy mô ${a2.decomp_2025_2035_logpts?.size}, thu nhập ${a2.decomp_2025_2035_logpts?.income} điểm log).`})();

// ---- ② khả năng chi trả
const A0={inc:D.assume.worker_income_median_m,share:D.assume.worker_budget_share*100,fuel:0,pt:D.assume.pass_through,lodge:D.assume.lodging_per_person_night/1000,busH:D.assume.max_bus_hours_one_way};
function resetAff(){for(const k in A0){$(k).value=A0[k]}aff()}
function aff(){const inc=+$('inc').value,sh=+$('share').value/100,fu=+$('fuel').value/100,pt=+$('pt').value,lo=+$('lodge').value*1000,bh=+$('busH').value;
 $('vInc').textContent=fmt(inc,1);$('vShare').textContent=sh*100+'%';$('vFuel').textContent=(fu>0?'+':'')+Math.round(fu*100)+'%';$('vPt').textContent=fmt(pt,2);$('vLodge').textContent=lo/1000;$('vBusH').textContent=bh+' giờ';
 const A=D.assume,n=A.nights,stay=n*lo+(n+1)*A.food_activity_per_day,vot=A.value_of_time_vnd_h;
 const rows=D.dest.map(d=>{let bus=2*d.km*A.bus_vnd_per_km;if(d.h*1.25>bh)bus=Infinity;let fly=d.km<450?Infinity:(D.fare.a+D.fare.b*d.km)*(1+pt*fu);
  const gb=bus+vot*2*d.h*1.25,gf=fly+vot*7,useF=gf<gb,cost=(useF?fly:bus)+stay;
  const w=100*(1-Phi((Math.log(cost/sh)-Math.log(inc*1e6))/A.worker_income_sigma));
  const s=100*(1-Phi((Math.log(cost/A.student_budget_months)-Math.log(D.student.median*1e6))/D.student.sigma));
  return{name:d.name,cost,mode:useF?'máy bay':'xe khách',w,s}}).sort((a,b)=>a.cost-b.cost);
 const y=rows.map(r=>`${r.name} · ${r.mode} · ${fmt(r.cost/1e6,1)}tr`);
 Plotly.react('pAff',[{type:'bar',orientation:'h',y,x:rows.map(r=>r.w),name:'Người đi làm 22–29',marker:{color:C.s1},hovertemplate:'%{x:.0f}%<extra>đi làm</extra>'},
  {type:'bar',orientation:'h',y,x:rows.map(r=>r.s),name:'Sinh viên',marker:{color:C.s2},hovertemplate:'%{x:.0f}%<extra>sinh viên</extra>'}],
  L('% người trẻ Hà Nội đủ tiền cho chuyến 3N2Đ',{barmode:'group',bargap:0.25,yaxis:{autorange:'reversed',automargin:true},xaxis:{range:[0,100],ticksuffix:'%',gridcolor:C.grid},margin:{l:10,r:20,t:46,b:40}}),CFG);
 const near=rows.filter(r=>r.mode==='xe khách'),far=rows.filter(r=>r.mode==='máy bay'),avg=(a,k)=>a.length?a.reduce((s,r)=>s+r[k],0)/a.length:0;
 $('iAff').innerHTML=`Đi bằng xe khách: trung bình <b>${fmt(avg(near,'w'),0)}%</b> người đi làm và <b>${fmt(avg(near,'s'),0)}%</b> sinh viên đủ tiền. Phải bay: chỉ còn <b>${fmt(avg(far,'w'),0)}%</b> và <b>${fmt(avg(far,'s'),0)}%</b>. Ranh giới không nằm ở khoảng cách mà ở <b>chỗ phải chuyển từ xe sang máy bay</b> — chi phí nhảy bậc ~2–3 triệu.`}
['inc','share','fuel','pt','lodge','busH'].forEach(k=>$(k).addEventListener('input',aff));resetAff();

// ---- ③ Huff
(function(){D.dest.forEach((d,i)=>{const o=document.createElement('option');o.value=i;o.textContent=d.name;$('boostDest').appendChild(o)});
 $('beta').value=D.beta;$('mon').value=0;$('boost').value=0;$('far').value=0;$('boostDest').value=D.dest.findIndex(d=>d.key==='mocchau');})();
function huff(boostOn){const [ola,olo]=$('org').value.split(',').map(Number),m=+$('mon').value,b=+$('beta').value,far=+$('far').value/100,bi=+$('boostDest').value,bo=+$('boost').value/100;
 const u=D.dest.map((d,i)=>{const dist=Math.max(15,hav(ola,olo,d.lat,d.lon));let A=(d.A||1)*(m?d.si[m-1]:1);if(boostOn&&i===bi)A*=1+bo;const cost=dist*(dist>450?1+far:1);return A*Math.pow(cost,-b)});
 const s=u.reduce((a,b)=>a+b,0);return u.map(x=>100*x/s)}
function map(){const m=+$('mon').value;$('vMon').textContent=m?('T'+m):'cả năm';$('vBeta').textContent=fmt(+$('beta').value,2);$('vFar').textContent='+'+$('far').value+'%';$('vBoost').textContent='+'+$('boost').value+'%';
 const p=huff(true),p0=huff(false),bi=+$('boostDest').value,[ola,olo]=$('org').value.split(',').map(Number);
 const types=[...new Set(D.dest.map(d=>d.type))],cols=[C.s1,C.s2,C.s3,C.s4,C.s5,C.s6,C.s7,C.s8];
 const tr=types.map((t,k)=>{const idx=D.dest.map((d,i)=>i).filter(i=>D.dest[i].type===t);return{type:'scattergeo',lat:idx.map(i=>D.dest[i].lat),lon:idx.map(i=>D.dest[i].lon),
  text:idx.map(i=>`${D.dest[i].name}: ${fmt(p[i],1)}%`),mode:'markers+text',textposition:'middle right',textfont:{size:10},name:t,
  marker:{size:idx.map(i=>6+Math.sqrt(p[i])*7),color:cols[k%8],line:{color:'#fff',width:1}},hovertemplate:'%{text}<extra></extra>'}});
 tr.push({type:'scattergeo',lat:[ola],lon:[olo],mode:'markers',marker:{symbol:'star',size:16,color:'#0b0b0b'},name:'Nơi xuất phát',hoverinfo:'skip'});
 Plotly.react('pMap',tr,L('Thị phần lựa chọn dự đoán (%)',{geo:{scope:'asia',lonaxis:{range:[101.5,110.5]},lataxis:{range:[8,23.6]},showland:true,landcolor:'#f3f2ee',countrycolor:'#c3c2b7',showocean:true,oceancolor:'#eef4fb',showcountries:true,resolution:50},margin:{l:0,r:0,t:40,b:0},legend:{orientation:'v',x:0.68,y:0.98,bgcolor:'rgba(255,255,255,.8)'}}),CFG);
 const top=p.map((x,i)=>[x,i]).sort((a,b)=>b[0]-a[0]).slice(0,5).map(([x,i])=>`${D.dest[i].name} ${fmt(x,1)}%`).join(' · ');
 const gain=p[bi]-p0[bi],per=gain/(+$('boost').value||1)*10;
 $('iMap').innerHTML=`Top 5: ${top}.<br>Đẩy sức hút <b>${D.dest[bi].name}</b> thêm ${$('boost').value}% → thị phần ${fmt(p0[bi],1)}% → <b>${fmt(p[bi],1)}%</b> (+${fmt(gain,2)} điểm). Điểm đến nhỏ và gần cho “lãi” thị phần lớn hơn trên cùng nỗ lực tiếp thị khi β cao.`}
['org','mon','beta','far','boostDest','boost'].forEach(k=>$(k).addEventListener('input',map));map();

// ---- ④ heatmap
function heat(){const v=$('heatSel').value,M=['T1','T2','T3','T4','T5','T6','T7','T8','T9','T10','T11','T12'];let names,z,cs,zmid=null,title;
 const seq=c=>[[0,'#f6f5f1'],[1,c]];
 if(v==='si'){names=D.dest.map(d=>d.name);z=D.dest.map(d=>d.si);cs=seq('#184f95');title='Chỉ số mùa vụ tìm kiếm (1,00 = trung bình năm)'}
 else if(v==='comfort'){names=D.clim_names;z=names.map(n=>D.comfort[n]);cs=seq('#0f6b3f');title='Tiện nghi khí hậu (0–100)'}
 else if(v==='heavy'){names=D.clim_names;z=names.map(n=>D.heavy[n]);cs=seq('#b42d2c');title='Số ngày mưa ≥50 mm trung bình mỗi tháng'}
 else{names=Object.keys(D.gap);z=names.map(n=>D.gap[n]);cs=[[0,'#b42d2c'],[0.5,'#f0efec'],[1,'#1c5cab']];zmid=0;title='Cửa sổ lệch mùa: xanh = thời tiết tốt mà ít người tìm'}
 Plotly.react('pHeat',[{type:'heatmap',z,x:M,y:names,colorscale:cs,zmid,reversescale:v==='gap'?false:false,texttemplate:'%{z:.2f}',textfont:{size:9},zmin:v==='gap'?-3:undefined,zmax:v==='gap'?3:undefined,hovertemplate:'%{y} · %{x}: %{z:.2f}<extra></extra>'}],
  L(title,{yaxis:{automargin:true,autorange:'reversed'},margin:{l:10,r:10,t:46,b:30}}),CFG);
 const off=(D.summary.a6_climate?.offseason||[]).slice(0,5).map(r=>`${r.name} — T${r.best_month} (đỉnh tìm kiếm T${r.peak_month})`).join('; ');
 $('iHeat').innerHTML='Tháng “lệch mùa” tốt nhất (khí hậu đẹp, ít người tìm): '+off+'.'}
$('heatSel').addEventListener('input',heat);heat();

// ---- ⑤ từ khoá
(function(){const ks=Object.keys(D.kw);ks.forEach(k=>{const o=document.createElement('option');o.value=k;o.textContent=k+(D.kwcls[k]?' — '+D.kwcls[k]:'');$('kwSel').appendChild(o)});
 ['chữa lành','phượt','lịch trình','chatgpt'].forEach(k=>{const o=[...$('kwSel').options].find(x=>x.value===k);if(o)o.selected=true});
 function draw(){const sel=[...$('kwSel').selectedOptions].map(o=>o.value),cols=[C.s1,C.s2,C.s3,C.s4,C.s5,C.s6,C.s7,C.s8];
  const tr=sel.map((k,i)=>({x:D.kw[k][0],y:D.kw[k][1],name:k+' ('+(D.kwcls[k]||'')+')',line:{color:cols[i%8],width:2},hovertemplate:'%{x}: %{y:.1f}<extra>'+k+'</extra>'}));
  Plotly.react('pKw',tr,L('Mức tìm kiếm theo tháng (thang chung, “du lịch” = 100)',{yaxis:{gridcolor:C.grid,rangemode:'tozero'}}),CFG);
  const rows=(D.summary.a4_trends?.classification||[]).map(r=>`<tr><td>${r.kw}</td><td><span class="tag">${r.cls}</span></td><td class="n">${r.growth_since_base_pct??''}%</td><td class="n">${r.base_year}</td><td class="n">${r.p??''}</td><td class="n">${r.last12_vs_peak_pct??''}%</td><td>${r.peak_12m_ending}</td></tr>`).join('');
  $('tKw').innerHTML='<table><tr><th>Từ khoá</th><th>Kết luận</th><th>Tăng từ năm gốc</th><th>Năm gốc</th><th>p (MK)</th><th>12T gần nhất / đỉnh</th><th>Đỉnh 12T kết thúc</th></tr>'+rows+'</table>'}
 $('kwSel').addEventListener('change',draw);draw()})();

// ---- ⑥ MCDA
const W0=D.mcda_w.slice();
(function(){D.mcda_crit.forEach((c,i)=>{$('mcdaCtl').insertAdjacentHTML('beforeend',`<label>${c} <b id="wv${i}"></b></label><input id="w${i}" type="range" min="0" max="40" step="1" value="${Math.round(W0[i]*100)}">`);
 $('w'+i).addEventListener('input',mcda)})})();
function resetW(){W0.forEach((w,i)=>$('w'+i).value=Math.round(w*100));mcda()}
function mcda(){const w=D.mcda_crit.map((c,i)=>+$('w'+i).value),s=w.reduce((a,b)=>a+b,0)||1;w.forEach((x,i)=>$('wv'+i).textContent=Math.round(100*x/s)+'%');
 const r=D.mcda.map(d=>({name:d.name,parts:D.mcda_crit.map((c,i)=>d[c]*w[i]/s)})).map(d=>({...d,score:d.parts.reduce((a,b)=>a+b,0)})).sort((a,b)=>b.score-a.score);
 const cols=[C.s1,C.s2,C.s3,C.s4,C.s5,C.s6,C.s7,C.s8];
 Plotly.react('pMcda',D.mcda_crit.map((c,i)=>({type:'bar',orientation:'h',y:r.map(d=>d.name),x:r.map(d=>d.parts[i]),name:c,marker:{color:cols[i%8],line:{color:'#fff',width:1}},hovertemplate:'%{y}: %{x:.3f}<extra>'+c+'</extra>'})),
  L('Điểm tổng hợp (0–1) — xếp hạng thay đổi ngay khi kéo trọng số',{barmode:'stack',yaxis:{autorange:'reversed',automargin:true},margin:{l:10,r:10,t:46,b:80}}),CFG);
 const rob=D.mcda.slice().sort((a,b)=>b.p_top3-a.p_top3).slice(0,4).map(d=>`${d.name} (${fmt(d.p_top3*100,0)}%)`).join(', ');
 $('iMcda').innerHTML=`Top 3 hiện tại: <b>${r.slice(0,3).map(d=>d.name).join(', ')}</b>. Vững nhất khi xáo trộn 20.000 bộ trọng số (xác suất lọt top 3): ${rob}.`}
mcda();

// ---- ⑦ Monte Carlo
const MCP=[['Tỉ lệ 20–29 trong dân Hà Nội (%)',12.8,15,18],['Tỉ lệ đi du lịch/năm (%)',65,78,88],['Số chuyến/năm',1.5,2.5,4],['Chi tiêu/chuyến (triệu)',2.5,3.5,5.5],['Tỉ trọng chi tại đối tác (%)',35,45,60],['Tỉ lệ dùng Levart sau 3 năm (%)',0.5,2,5],['Hoa hồng (%)',5,8,12]];
(function(){MCP.forEach((p,i)=>{$('mcCtl').insertAdjacentHTML('beforeend',`<label>${p[0]} — khả dĩ nhất <b id="mv${i}">${p[2]}</b></label><input id="m${i}" type="range" min="${p[1]}" max="${p[3]}" step="${(p[3]-p[1])/40}" value="${p[2]}">`);$('m'+i).addEventListener('change',runMC);$('m'+i).addEventListener('input',()=>$('mv'+i).textContent=fmt(+$('m'+i).value,2))})})();
function runMC(){const N=20000,hn=(D.summary.a11_market?.hanoi_pop)||8.8e6,rev=new Float64Array(N),som=new Float64Array(N);
 const md=MCP.map((p,i)=>+$('m'+i).value);
 for(let k=0;k<N;k++){const v=MCP.map((p,i)=>tri(p[1],Math.min(Math.max(md[i],p[1]+1e-9),p[3]-1e-9),p[3]));
  const g=hn*v[0]/100*v[1]/100*v[2]*v[3]*1e6*v[4]/100*v[5]/100;som[k]=g;rev[k]=g*v[6]/100}
 const q=(a,p)=>{const s=Array.from(a).sort((x,y)=>x-y);return s[Math.floor(p*(s.length-1))]};
 const r=Array.from(rev).map(x=>x/1e9);
 Plotly.react('pMc',[{type:'histogram',x:r,nbinsx:70,marker:{color:'#9ec5f4'},hovertemplate:'%{x:.1f} tỷ: %{y} lượt<extra></extra>'}],
  L('Doanh thu hoa hồng/năm sau 3 năm (tỷ đồng)',{shapes:[0.1,0.5,0.9].map(p=>({type:'line',x0:q(rev,p)/1e9,x1:q(rev,p)/1e9,y0:0,y1:1,yref:'paper',line:{color:C.s1,dash:p===0.5?'solid':'dot'}})),bargap:0.02}),CFG);
 $('iMc').innerHTML=`GMV qua Levart: P10 <b>${fmt(q(som,.1)/1e9,0)}</b> · P50 <b>${fmt(q(som,.5)/1e9,0)}</b> · P90 <b>${fmt(q(som,.9)/1e9,0)}</b> tỷ đồng/năm → doanh thu hoa hồng P50 <b>${fmt(q(rev,.5)/1e9,1)} tỷ</b>. Kéo “tỉ lệ dùng Levart” để thấy đây là giả định quyết định gần như toàn bộ kết quả.`}
runMC();

// ---- ⑧
Plotly.newPlot('pOs',[{x:D.os[0],y:D.os[1],name:'Android',line:{color:C.s1}},{x:D.os[0],y:D.os[2],name:'iOS',line:{color:C.s2,width:2.5}}],L('Thị phần lượt truy cập web từ điện thoại theo hệ điều hành, Việt Nam (%) — StatCounter',{xaxis:{range:['2016-01','2026-09'],gridcolor:C.grid}}),CFG);
Plotly.newPlot('pFuel',[{x:D.fuel[0],y:D.fuel[1],name:'Nhiên liệu bay',line:{color:C.s8},fill:'tozeroy',fillcolor:'rgba(227,73,72,.08)'}],L('Giá nhiên liệu bay, USD/gallon (FRED, trung bình tháng)',{xaxis:{range:['2015-01','2026-10'],gridcolor:C.grid},showlegend:false}),CFG);
</script></body></html>"""

if __name__ == "__main__":
    build()
