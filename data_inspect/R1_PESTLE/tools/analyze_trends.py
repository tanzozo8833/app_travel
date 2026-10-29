"""A4 xu hướng tìm kiếm, A5 mùa vụ & tăng trưởng điểm đến.

Phương pháp: phân rã STL (xu hướng + mùa vụ + nhiễu), sức mạnh mùa vụ/xu hướng (Wang–Smith–Hyndman),
Mann–Kendall + độ dốc Sen, phân loại xu hướng/mốt theo luật của PLAN_RESEARCH 3.1,
chuỗi thời gian gián đoạn (interrupted time series) quanh ngày ra mắt ChatGPT,
so sánh trước–sau theo tháng (event study) cho cú sốc nhiên liệu 2026, chỉ số HHI,
chỉ số mùa vụ, hệ số Gini mùa vụ, CAGR, ma trận danh mục (quy mô × tăng trưởng).
"""
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import statsmodels.api as sm
from scipy import stats
from statsmodels.tsa.seasonal import STL

from common import PROC, RES, SERIES, MUTED, INK2, style, savefig, update_summary
from analyze_macro import mann_kendall

style()

LABEL = {"du lịch": "du lịch", "du lịch tự túc": "du lịch tự túc", "homestay": "homestay", "phượt": "phượt",
         "review": "review", "chữa lành": "chữa lành", "cắm trại": "cắm trại", "trekking": "trekking",
         "du lịch một mình": "du lịch một mình", "lịch trình": "lịch trình", "chatgpt": "ChatGPT",
         "google maps": "Google Maps", "gemini": "Gemini", "vé máy bay": "vé máy bay", "xe khách": "xe khách",
         "vé tàu": "vé tàu", "thuê xe máy": "thuê xe máy", "agoda": "Agoda", "traveloka": "Traveloka",
         "booking": "Booking", "klook": "Klook"}


def load_trends():
    t = pd.read_csv(PROC / "trends_long.csv", parse_dates=["month"])
    # Quy về thang chung: chia cho trung bình của từ mốc trong nhóm ("du lịch" hoặc "Đà Lạt") rồi ×100
    t["scaled"] = t.raw / t.groupby("group").anchor_raw.transform("mean") * 100
    return t


def stl_stats(s):
    """s: chuỗi tháng. Trả STL + các thống kê."""
    y = np.log1p(s.astype(float))
    r = STL(y, period=12, robust=True).fit()
    var = lambda x: np.nanvar(x)
    fs = max(0.0, 1 - var(r.resid) / var(r.seasonal + r.resid))
    ft = max(0.0, 1 - var(r.resid) / var(r.trend + r.resid))
    return r, fs, ft


def classify(s):
    """Luật phân loại theo PLAN_RESEARCH 3.1, đo trên dữ liệu.

    - Không đủ dữ liệu: >30% số tháng bằng 0 trong 36 tháng gần nhất.
    - Từ mới nổi (xuất hiện sau 2019): đo từ năm đầu tiên có tín hiệu.
    - Xu hướng tăng: Mann–Kendall trên trung bình năm, p<0,05, tau>0, ≥2 năm tăng liên tiếp.
    - Suy giảm: Mann–Kendall p<0,05, tau<0.
    - Mốt (đã qua đỉnh): 12 tháng gần nhất <70% đỉnh 12 tháng, đỉnh sau 2020, đi lên nhanh rồi xuống.
    - Đã qua đỉnh (lâu): như trên nhưng đỉnh trước 2020.
    """
    import itertools
    annual = s.groupby(s.index.year).mean()
    annual = annual[annual.index <= 2025]
    start = next((y for y, v in annual.items() if v > 0.5), 2016)
    base_year = max(2019, start)
    recent = annual.loc[base_year:]
    if len(recent) >= 4:
        tau, p, slope = mann_kendall(recent.values)
    else:  # từ quá mới: kiểm định trên chuỗi tháng 24 tháng gần nhất
        tau, p, slope = mann_kendall(s.iloc[-24:].values)
    last12 = s.iloc[-12:].mean()
    roll = s.rolling(12).mean()
    peak12, peak_month = roll.max(), roll.idxmax()
    zero_recent = (s.iloc[-36:] == 0).mean()
    ups = (recent.diff().dropna() > 0).astype(int)
    run = max((len(list(g)) for k, g in itertools.groupby(ups) if k == 1), default=0)
    growth = (annual.loc[2025] / annual.loc[base_year] - 1) * 100 if annual.loc[base_year] > 0 else np.nan
    if zero_recent > 0.3:
        cls = "Không đủ dữ liệu"
    elif p < 0.05 and tau > 0 and (run >= 2 or len(recent) < 4) and last12 >= 0.7 * peak12:
        cls = "Xu hướng tăng"
    elif last12 < 0.7 * peak12 and peak_month.year >= 2020 and annual.loc[base_year] < 0.6 * annual.max():
        cls = "Mốt (đã qua đỉnh)"
    elif p < 0.05 and tau < 0:
        cls = "Suy giảm"
    elif last12 < 0.7 * peak12:
        cls = f"Đã qua đỉnh ({peak_month.year})"
    else:
        cls = "Ổn định / chưa rõ"
    return dict(tau=round(float(tau), 2), p=round(float(p), 4), up_years_run=int(run), base_year=int(base_year),
                growth_since_base_pct=round(float(growth), 1), last12_vs_peak_pct=round(float(last12 / peak12 * 100), 0),
                peak_12m_ending=str(peak_month.date())[:7], zero_share_36m=round(float(zero_recent), 2), cls=cls)


def keyword_trends(t):
    kws = t[~t.group.str.startswith("dest")]
    rows, series = [], {}
    for (g, kw), d in kws.groupby(["group", "kw"]):
        if kw == "du lịch" and g != "behaviour":
            continue
        s = d.set_index("month")["scaled"].asfreq("MS")
        series[kw] = s
        c = classify(s)
        try:
            _, fs, ft = stl_stats(s)
        except Exception:
            fs = ft = np.nan
        rows.append(dict(group=g, kw=kw, mean_level=round(float(s.mean()), 1), seasonal_strength=round(fs, 2),
                         trend_strength=round(ft, 2), **c))
    tab = pd.DataFrame(rows).sort_values(["cls", "growth_since_base_pct"], ascending=[True, False])
    tab.to_csv(RES / "a4_keyword_classification.csv", index=False)

    order = [k for k in ["du lịch", "du lịch tự túc", "homestay", "phượt", "review", "chữa lành", "cắm trại",
                         "trekking", "du lịch một mình", "lịch trình", "chatgpt", "google maps", "gemini",
                         "vé máy bay", "xe khách", "vé tàu", "thuê xe máy", "agoda", "traveloka", "klook"] if k in series]
    n = len(order)
    cols = 4
    fig, axes = plt.subplots(int(np.ceil(n / cols)), cols, figsize=(12, 2.0 * np.ceil(n / cols)), sharex=True)
    clsmap = tab.set_index("kw").cls.to_dict()
    colr = {"Xu hướng tăng": SERIES[2], "Mốt (đã qua đỉnh)": SERIES[1], "Suy giảm": SERIES[7],
            "Ổn định / chưa rõ": SERIES[0], "Không đủ dữ liệu": MUTED}
    colr.update({c: SERIES[3] for c in tab.cls if c.startswith("Đã qua đỉnh")})
    for ax, kw in zip(axes.flat, order):
        s = series[kw]
        c = colr.get(clsmap.get(kw), SERIES[0])
        ax.plot(s.index, s, color=c, lw=0.9, alpha=0.55)
        ax.plot(s.index, s.rolling(12, center=True).mean(), color=c, lw=2)
        ax.set_title(f"{LABEL.get(kw, kw)} — {clsmap.get(kw, '')}", fontsize=9)
        ax.tick_params(labelsize=7)
        ax.grid(lw=0.4)
    for ax in list(axes.flat)[n:]:
        ax.axis("off")
    fig.suptitle("Google Trends Việt Nam 2016–2026: đường mảnh = tháng, đường đậm = trung bình 12 tháng; màu = kết luận",
                 x=0.01, ha="left", fontweight="bold", y=1.01)
    fig.tight_layout()
    f = savefig(fig, "a4_keywords_small_multiples.png",
                "Thang chung: chia cho trung bình từ khoá 'du lịch' cùng lượt tải (=100). Luật: tăng = Mann–Kendall p<0,05, ≥2 năm tăng liên tiếp; mốt = 12 tháng gần nhất <70% đỉnh (đỉnh sau 2020); vàng = đã qua đỉnh trước 2020.")
    return tab, series, f


def its_chatgpt(series):
    """Chuỗi gián đoạn: tỉ lệ 'lịch trình' / 'du lịch' trước và sau 30/11/2022 (ChatGPT ra mắt)."""
    if "lịch trình" not in series:
        return None
    t = load_trends()
    pl = t[t.group == "planning"].pivot(index="month", columns="kw", values="raw")
    y = np.log((pl["lịch trình"] + 0.5) / (pl["du lịch"] + 0.5))
    df = pd.DataFrame({"y": y})
    df["t"] = np.arange(len(df))
    df["post"] = (df.index >= "2022-12-01").astype(int)
    df["t_post"] = df["post"] * (df["t"] - df.loc[df.post == 1, "t"].min())
    df["covid"] = ((df.index >= "2020-02-01") & (df.index <= "2021-12-01")).astype(int)
    X = pd.get_dummies(df.index.month, prefix="m", drop_first=True, dtype=float)
    X.index = df.index
    X = sm.add_constant(pd.concat([df[["t", "post", "t_post", "covid"]], X], axis=1))
    m = sm.OLS(df.y, X).fit(cov_type="HAC", cov_kwds={"maxlags": 6})
    df["fit"] = m.predict(X)
    Xcf = X.copy()
    Xcf["post"] = 0
    Xcf["t_post"] = 0
    df["cf"] = m.predict(Xcf)

    gpt = pl["chatgpt"]
    fig, ax = plt.subplots(figsize=(9.5, 4))
    ax.plot(df.index, np.exp(df.y), color="#9ec5f4", lw=1, label="Tỉ lệ 'lịch trình' / 'du lịch' (tháng)")
    ax.plot(df.index, np.exp(df.fit), color=SERIES[0], lw=2, label="Mô hình ITS")
    ax.plot(df.index[df.post == 1], np.exp(df.cf[df.post == 1]), color=SERIES[0], lw=1.6, ls="--",
            label="Nếu không có ChatGPT (phản thực)")
    ax.axvline(pd.Timestamp("2022-12-01"), color=MUTED, lw=0.8)
    ax2 = ax.twinx()
    ax2.fill_between(gpt.index, gpt, color=SERIES[1], alpha=0.12, lw=0)
    ax2.set_yticks([])
    ax2.grid(False)
    ax.text(pd.Timestamp("2023-01-15"), ax.get_ylim()[1] * 0.95, "ChatGPT ra mắt\n(vùng cam = mức tìm 'chatgpt')",
            fontsize=8, color=INK2, va="top")
    ax.set_title("Người Việt có bớt tự tìm 'lịch trình' trên Google sau khi có ChatGPT?")
    ax.legend(fontsize=8, loc="upper left")
    f = savefig(fig, "a4_its_chatgpt.png",
                "Hồi quy đoạn (ITS) với biến giả tháng, biến giả COVID 2/2020–12/2021, sai số HAC. Vùng cam chỉ để minh hoạ thời điểm, không cùng thang.")
    p = m.params
    pv = m.pvalues
    eff_2026 = float(np.exp(df.y.loc["2026"].mean() - df.cf.loc["2026"].mean()) - 1) * 100
    return {"level_change_log": round(float(p["post"]), 3), "level_p": round(float(pv["post"]), 4),
            "slope_change_per_month_log": round(float(p["t_post"]), 4), "slope_p": round(float(pv["t_post"]), 4),
            "gap_vs_counterfactual_2026_pct": round(eff_2026, 1), "r2": round(float(m.rsquared), 2), "fig": f}


def fuel_substitution():
    """Tỉ lệ 'xe khách' / 'vé máy bay': hồi quy log-tỉ lệ ~ hiệu ứng tháng + xu hướng tuyến tính + biến giả sốc
    (T3–T9/2026), mẫu 2017–2026 bỏ COVID (2/2020–12/2021), sai số HAC. So sánh thêm: chênh lệch so với 2025."""
    t = load_trends()
    tr = t[t.group == "transport"].pivot(index="month", columns="kw", values="raw")
    out = {}
    for lab, num in [("bus", "xe khách"), ("train", "vé tàu")]:
        r = np.log((tr[num] + 0.5) / (tr["vé máy bay"] + 0.5))
        df = pd.DataFrame({"y": r}).loc["2017-01-01":]
        df = df[~((df.index >= "2020-02-01") & (df.index <= "2021-12-01"))]
        df["t"] = (df.index.year - 2017) * 12 + df.index.month
        df["shock"] = ((df.index >= "2026-03-01") & (df.index <= "2026-09-01")).astype(float)
        X = pd.get_dummies(df.index.month, prefix="m", drop_first=True, dtype=float)
        X.index = df.index
        X = sm.add_constant(pd.concat([df[["t", "shock"]], X], axis=1))
        m = sm.OLS(df.y, X).fit(cov_type="HAC", cov_kwds={"maxlags": 6})
        months = range(3, 10)
        a26 = r[(r.index.year == 2026) & r.index.month.isin(months)].mean()
        a25 = r[(r.index.year == 2025) & r.index.month.isin(months)].mean()
        pre26 = r[(r.index.year == 2026) & r.index.month.isin([1, 2])].mean()
        pre25 = r[(r.index.year == 2025) & r.index.month.isin([1, 2])].mean()
        out[lab] = {"shock_effect_pct": round(float(np.exp(m.params["shock"]) - 1) * 100, 1),
                    "ci95_pct": [round(float(np.exp(x) - 1) * 100, 1) for x in m.conf_int().loc["shock"]],
                    "p": round(float(m.pvalues["shock"]), 4),
                    "trend_pct_per_year": round(float(np.exp(m.params["t"] * 12) - 1) * 100, 1),
                    "vs2025_same_months_pct": round(float(np.exp(a26 - a25) - 1) * 100, 1),
                    "did_vs2025_pct": round(float(np.exp((a26 - a25) - (pre26 - pre25)) - 1) * 100, 1)}

    r = np.log((tr["xe khách"] + 0.5) / (tr["vé máy bay"] + 0.5))
    fig, ax = plt.subplots(figsize=(9.5, 3.8))
    for yr, c, lw in [(2023, "#b7d3f6", 1.2), (2024, "#86b6ef", 1.2), (2025, "#3987e5", 1.4), (2026, SERIES[1], 2.6)]:
        s = r[r.index.year == yr]
        ax.plot(s.index.month, np.exp(s.values), color=c, lw=lw, marker="o" if yr == 2026 else None, ms=3, label=str(yr))
    ax.axvspan(2.5, 9.5, color="#f3f2ee", zorder=0)
    ax.set_xticks(range(1, 13), [f"T{i}" for i in range(1, 13)])
    ax.set_title("Tìm 'xe khách' so với 'vé máy bay' theo tháng — tỉ lệ đã tăng đều trước cả 2026")
    ax.set_ylabel("tỉ lệ xe khách / vé máy bay")
    ax.legend(ncol=4, fontsize=8)
    b = out["bus"]
    f = savefig(fig, "a4_fuel_substitution.png",
                f"Hồi quy: hiệu ứng T3–T9/2026 sau khi trừ xu hướng ({b['trend_pct_per_year']:+.1f}%/năm) và mùa vụ = {b['shock_effect_pct']:+.1f}% "
                f"(KTC95 {b['ci95_pct'][0]}…{b['ci95_pct'][1]}%, p={b['p']}). Vùng xám = giá nhiên liệu bay tăng mạnh.")
    out["fig"] = f
    return out


def platforms():
    t = load_trends()
    b = t[t.group == "booking"].pivot(index="month", columns="kw", values="raw")[["agoda", "traveloka", "klook"]]
    sh = b.div(b.sum(axis=1), axis=0) * 100
    hhi = (sh ** 2).sum(axis=1)
    ann = sh.groupby(sh.index.year).mean()
    fig, ax = plt.subplots(figsize=(9, 3.6))
    ax.stackplot(sh.index, sh.T.values, labels=["Agoda", "Traveloka", "Klook"], colors=SERIES[:3], alpha=0.85)
    ax.set_ylim(0, 100)
    ax.set_title("Thị phần sự chú ý trên Google giữa các nền tảng đặt chỗ (%)")
    ax.legend(loc="upper left", fontsize=8, ncol=3)
    f = savefig(fig, "a4_platform_attention.png", "Bỏ 'booking' vì là từ tiếng Anh thông dụng, không chỉ tên Booking.com. HHI tính trên 3 nền tảng.")
    return {"share_2019": {k: round(float(v), 1) for k, v in ann.loc[2019].items()},
            "share_2025": {k: round(float(v), 1) for k, v in ann.loc[2025].items()},
            "hhi_2019": round(float(hhi.loc["2019"].mean())), "hhi_2025": round(float(hhi.loc["2025"].mean())), "fig": f}


# ------------------------------------------------------------------------------------------------
def seasonal_index(s, years=("2022", "2025")):
    """Chỉ số mùa vụ: giá trị / trung bình trượt 12 tháng (tâm), trung bình theo tháng, chuẩn hoá trung bình = 1."""
    s = s.astype(float)
    ma = s.rolling(12, center=True).mean()
    ratio = (s / ma).loc[years[0]:years[1]]
    si = ratio.groupby(ratio.index.month).mean()
    return si / si.mean()


def gini(x):
    x = np.sort(np.asarray(x, float))
    n = len(x)
    return float((2 * np.arange(1, n + 1) - n - 1).dot(x) / (n * x.sum()))


def destinations():
    dest = pd.read_csv(PROC / "destinations.csv")
    pv = pd.read_csv(PROC / "pageviews.csv", parse_dates=["month"])
    t = load_trends()
    td = t[t.group.str.startswith("dest")]
    rows, si_pv, si_tr = [], {}, {}
    for _, d in dest.iterrows():
        p = pv[pv.article == d.wiki].set_index("month")["views"].asfreq("MS")
        tr = td[td.kw == d["name"]].groupby("month").scaled.mean().asfreq("MS")
        if len(p) < 36 or len(tr) < 60:
            print("  thiếu dữ liệu:", d["name"], len(p), len(tr))
            continue
        si_pv[d["name"]] = seasonal_index(p)
        si_tr[d["name"]] = seasonal_index(tr)
        ap = p.groupby(p.index.year).sum()
        at = tr.groupby(tr.index.year).mean()
        rows.append(dict(
            key=d.key, name=d["name"], type=d.type, province=d.province, road_km=d.road_km, drive_h=d.drive_h,
            trends_level_2025=round(float(at.loc[2025]), 2), pageviews_2025=int(ap.get(2025, 0)),
            cagr_trends_19_25=round(float((at.loc[2025] / at.loc[2019]) ** (1 / 6) - 1) * 100, 1),
            cagr_pv_19_25=round(float((ap.get(2025, np.nan) / ap.get(2019, np.nan)) ** (1 / 6) - 1) * 100, 1),
            momentum_trends_25_23=round(float(at.loc[2025] / at.loc[2023] - 1) * 100, 1),
            gini_season=round(gini(si_tr[d["name"]].values), 3), gini_season_pv=round(gini(si_pv[d["name"]].values), 3),
            peak_month=int(si_tr[d["name"]].idxmax()), low_month=int(si_tr[d["name"]].idxmin()),
            r_trends_pv=round(float(np.corrcoef(si_tr[d["name"]], si_pv[d["name"]])[0, 1]), 2),
        ))
    tab = pd.DataFrame(rows)
    tab.to_csv(RES / "a5_destinations.csv", index=False)
    S = pd.DataFrame(si_tr).T
    S.to_csv(RES / "a5_seasonal_index_trends.csv")
    pd.DataFrame(si_pv).T.to_csv(RES / "a5_seasonal_index_pageviews.csv")

    # Bản đồ nhiệt mùa vụ
    order = tab.sort_values(["type", "drive_h"]).name
    fig, ax = plt.subplots(figsize=(10, 7))
    M = S.loc[order]
    im = ax.imshow(M.values, cmap="Blues", aspect="auto", vmin=0.6, vmax=1.6)
    ax.set_xticks(range(12), [f"T{i}" for i in range(1, 13)])
    ax.set_yticks(range(len(M)), [f"{n}  ·  {tab.set_index('name').loc[n, 'type']}" for n in M.index], fontsize=8.5)
    for i in range(M.shape[0]):
        for j in range(12):
            v = M.values[i, j]
            ax.text(j, i, f"{v:.2f}", ha="center", va="center", fontsize=7, color="white" if v > 1.25 else "#0b0b0b")
    ax.grid(False)
    ax.set_title("Chỉ số mùa vụ tìm kiếm theo tháng (1,00 = trung bình năm), 2022–2025")
    fig.colorbar(im, ax=ax, fraction=0.025)
    f1 = savefig(fig, "a5_seasonality_heatmap.png", "Google Trends từng điểm đến ÷ trung bình trượt 12 tháng, lấy trung bình theo tháng 2022–2025.")

    # Ma trận danh mục
    fig, ax = plt.subplots(figsize=(9.5, 6))
    types = list(dict.fromkeys(tab.type))
    for i, ty in enumerate(types):
        d = tab[tab.type == ty]
        ax.scatter(d.trends_level_2025, d.momentum_trends_25_23, s=(d.gini_season * 900) ** 1.3 + 20,
                   color=SERIES[i % 8], alpha=0.75, label=ty, edgecolor="white", lw=1.5)
        for _, r in d.iterrows():
            ax.annotate(r["name"], (r.trends_level_2025, r.momentum_trends_25_23), xytext=(5, 3),
                        textcoords="offset points", fontsize=8)
    ax.set_xscale("log")
    ax.axhline(tab.momentum_trends_25_23.median(), color=MUTED, lw=0.8, ls="--")
    ax.axvline(tab.trends_level_2025.median(), color=MUTED, lw=0.8, ls="--")
    ax.set_xlabel("Quy mô quan tâm 2025 (Google Trends, thang chung; log)")
    ax.set_ylabel("Đà tăng 2025 so với 2023 (%)")
    ax.set_title("Danh mục điểm đến: quy mô × đà tăng (kích thước = độ dồn mùa, Gini)")
    ax.legend(fontsize=7.5, loc="lower right")
    f2 = savefig(fig, "a5_portfolio.png", "Đường đứt = trung vị. Góc trên-trái: nhỏ nhưng đang lên.")
    return tab, S, [f1, f2]


if __name__ == "__main__":
    t = load_trends()
    tab, series, f = keyword_trends(t)
    out = {"classification": tab[["kw", "cls", "base_year", "growth_since_base_pct", "tau", "p", "seasonal_strength",
                                   "last12_vs_peak_pct", "peak_12m_ending", "zero_share_36m"]].to_dict("records"), "figs": [f]}
    its = its_chatgpt(series)
    if its:
        out["its_chatgpt"] = its
        out["figs"].append(its["fig"])
    fs = fuel_substitution()
    out["fuel_substitution"] = fs
    out["figs"].append(fs["fig"])
    pl = platforms()
    out["platforms"] = pl
    out["figs"].append(pl["fig"])
    update_summary("a4_trends", out)
    dtab, S, figs = destinations()
    update_summary("a5_destinations", {"table": dtab.to_dict("records"), "figs": figs})
    print("trends ok")
