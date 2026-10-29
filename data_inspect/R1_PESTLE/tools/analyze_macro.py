"""A1–A3, A9–A10: nhân khẩu học, sức mua, hội tụ thu nhập, hệ điều hành di động, cú sốc nhiên liệu.

Phương pháp: phân rã quy mô–thu nhập (shift-share), dự báo bằng nước tương tự (analog),
kiểm định Mann–Kendall + độ dốc Sen, hồi quy logistic theo thời gian, điểm z của cú sốc, hệ số truyền dẫn giá.
"""
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from scipy import stats

from common import PROC, RES, SERIES, INK2, MUTED, style, savefig, update_summary

style()
NAMES = {"VNM": "Việt Nam", "THA": "Thái Lan", "IDN": "Indonesia", "PHL": "Philippines", "MYS": "Malaysia"}


def mann_kendall(y):
    """Kiểm định xu hướng Mann–Kendall (không tham số) + độ dốc Sen. Trả (tau, p, slope)."""
    y = np.asarray(y, float)
    y = y[~np.isnan(y)]
    tau, p = stats.kendalltau(np.arange(len(y)), y)
    slope = stats.theilslopes(y)[0]
    return float(tau), float(p), float(slope)


# ------------------------------------------------------------------------------------------------
def demography():
    pop = pd.read_csv(PROC / "wb_pop_age.csv", dtype={"age": str})
    tot = pd.read_csv(PROC / "wb_pop_total_proj.csv")
    g = pop.groupby(["iso3", "year", "age"])["pop"].sum().unstack()
    g["y2029"] = g["2024"] + g["2529"]
    g["y1834"] = g["1519"] * 2 / 5 + g["2024"] + g["2529"] + g["3034"]
    vn = g.loc["VNM"]

    fig, ax = plt.subplots(figsize=(9, 4.2))
    for a, lab, c in [("2024", "20–24 tuổi", SERIES[0]), ("2529", "25–29 tuổi", SERIES[1]), ("3034", "30–34 tuổi", SERIES[2])]:
        ax.plot(vn.index, vn[a] / 1e6, color=c, lw=2, label=lab)
    ax.axvspan(2025.5, 2050, color="#f3f2ee", zorder=0)
    ax.axvline(2025, color=MUTED, lw=0.8, ls="--")
    ax.text(2026, ax.get_ylim()[1] * 0.97, "dự báo LHQ/WB", color=MUTED, fontsize=8, va="top")
    ax.set_title("Việt Nam: số người theo nhóm tuổi trẻ, 1990–2050 (triệu người)")
    ax.legend(loc="lower right")
    f1 = savefig(fig, "a1_cohorts.png", "Tổng nam + nữ; sau 2025 là dự báo.")

    # So sánh 20–29 giữa các nước, chỉ số 2025 = 100
    fig, ax = plt.subplots(figsize=(9, 4))
    for i, iso in enumerate(NAMES):
        s = g.loc[iso]["y2029"]
        ax.plot(s.index, s / s.loc[2025] * 100, lw=2.4 if iso == "VNM" else 1.4, color=SERIES[i], label=NAMES[iso])
    ax.axhline(100, color=MUTED, lw=0.8)
    ax.set_xlim(2000, 2050)
    ax.set_title("Nhóm 20–29 tuổi, chỉ số 2025 = 100 — Việt Nam đang ở đáy, các nước khác đang đi xuống")
    ax.legend(ncol=5, loc="upper center", fontsize=8)
    f2 = savefig(fig, "a1_cohort_peers.png", "Nhóm 20–29 = 20–24 + 25–29; chỉ số 2025 = 100.")

    y = vn["y2029"] / 1e6
    trough_year = int(y.loc[2015:2035].idxmin())
    out = {
        "pop_20_29": {int(k): round(float(v), 2) for k, v in y.loc[[2015, 2020, 2025, 2030, 2035, 2040, 2050]].items()},
        "trough_year": trough_year, "trough_value": round(float(y.loc[trough_year]), 2),
        "change_2015_2025_pct": round(float((y.loc[2025] / y.loc[2015] - 1) * 100), 1),
        "change_2025_2035_pct": round(float((y.loc[2035] / y.loc[2025] - 1) * 100), 1),
        "peers_2025_2035_pct": {NAMES[i]: round(float((g.loc[i]["y2029"].loc[2035] / g.loc[i]["y2029"].loc[2025] - 1) * 100), 1)
                                for i in NAMES},
        "share_20_29_2025_pct": round(float(vn["y2029"].loc[2025] / tot[(tot.iso3 == "VNM") & (tot.year == 2025)].pop_total.iloc[0] * 100), 1),
        "pop_18_34_2025": round(float(vn["y1834"].loc[2025] / 1e6), 2),
        "figs": [f1, f2],
    }
    return out, y


def purchasing_power(y2029):
    """Phân rã giá trị thị trường người trẻ = quy mô nhóm tuổi × chi tiêu/người (thu nhập)."""
    macro = pd.read_csv(PROC / "wb_macro.csv")
    imf = pd.read_csv(PROC / "imf_weo_vnm.csv").set_index("year")
    vn = macro[macro.iso3 == "VNM"].set_index("year")

    # Chi tiêu hộ/người thực: WB tới 2025, nối dài bằng tăng trưởng GDP/người thực của IMF (giả định cùng tốc độ)
    cons = vn["hh_cons_pc"].dropna()
    popg = pd.read_csv(PROC / "wb_pop_total_proj.csv").query("iso3=='VNM'").set_index("year").pop_total
    gdp_real_growth = imf["NGDP_RPCH"] / 100
    for yr in range(2026, 2032):
        pc_growth = (1 + gdp_real_growth.loc[yr]) / (popg.loc[yr] / popg.loc[yr - 1]) - 1
        cons.loc[yr] = cons.loc[yr - 1] * (1 + pc_growth)
    # Sau 2031: giả định tăng 4%/năm (thấp hơn 2026–2031) — (GT)
    for yr in range(2032, 2036):
        cons.loc[yr] = cons.loc[yr - 1] * 1.04
    cons = cons.sort_index()

    years = list(range(2015, 2036))
    mkt = pd.DataFrame({"pop": y2029.reindex(years), "cons": cons.reindex(years)})
    mkt["value"] = mkt["pop"] * mkt["cons"]          # chỉ số giá trị (triệu người × USD/người)
    base, end = 2025, 2035
    dV = np.log(mkt.value[end] / mkt.value[base])
    d_size = np.log(mkt["pop"][end] / mkt["pop"][base])
    d_inc = np.log(mkt.cons[end] / mkt.cons[base])
    back_size = np.log(mkt["pop"][2025] / mkt["pop"][2015])
    back_inc = np.log(mkt.cons[2025] / mkt.cons[2015])

    fig, axes = plt.subplots(1, 2, figsize=(10, 4), gridspec_kw={"width_ratios": [1.35, 1]})
    ax = axes[0]
    idx = mkt / mkt.loc[2025] * 100
    ax.plot(idx.index, idx["pop"], lw=2, label="Số người 20–29")
    ax.plot(idx.index, idx["cons"], lw=2, label="Chi tiêu/người (thực)")
    ax.plot(idx.index, idx["value"], lw=2.6, color=SERIES[2], label="Giá trị thị trường = tích hai đường")
    ax.axvline(2025, color=MUTED, lw=0.8, ls="--")
    ax.axhline(100, color=MUTED, lw=0.6)
    ax.set_title("Chỉ số, 2025 = 100")
    ax.legend(fontsize=8)
    ax = axes[1]
    labs = ["2015→2025", "2025→2035"]
    sz = [back_size * 100, d_size * 100]
    inc = [back_inc * 100, d_inc * 100]
    x = np.arange(2)
    ax.bar(x - 0.18, sz, 0.34, label="do quy mô nhóm tuổi", color=SERIES[0])
    ax.bar(x + 0.18, inc, 0.34, label="do chi tiêu/người", color=SERIES[1])
    for i in range(2):
        ax.text(x[i] - 0.18, sz[i] + (2 if sz[i] >= 0 else -7), f"{sz[i]:+.0f}", ha="center", fontsize=9)
        ax.text(x[i] + 0.18, inc[i] + 2, f"{inc[i]:+.0f}", ha="center", fontsize=9)
    ax.set_xticks(x, labs)
    ax.axhline(0, color=INK2, lw=0.8)
    ax.set_title("Phân rã tăng trưởng (điểm log ×100)")
    ax.set_ylim(-35, 62)
    ax.legend(fontsize=8, loc="lower right")
    axes[0].xaxis.set_major_locator(plt.MaxNLocator(integer=True))
    fig.suptitle("Thị trường chi tiêu của người 20–29 tuổi: quy mô × thu nhập", x=0.01, y=1.05, ha="left", fontweight="bold")
    f = savefig(fig, "a2_market_decomposition.png",
                "Chi tiêu/người: WB NE.CON.PRVT.PC.KD tới 2025, nối dài theo tăng trưởng GDP thực/người của IMF WEO 2026–2031, 4%/năm sau 2031 (GT).")

    # Dự báo bằng nước tương tự: Việt Nam năm X giàu ngang Thái Lan năm nào?
    tha = macro[macro.iso3 == "THA"].set_index("year")["gdp_pc_ppp"].dropna()
    # GDP/người PPP giá cố định nối dài theo tăng trưởng GDP thực của IMF trừ tăng dân số
    proj = pd.Series({2025: vn["gdp_pc_ppp"].dropna().iloc[-1]})
    for yr in range(2026, 2032):
        proj.loc[yr] = proj.loc[yr - 1] * (1 + gdp_real_growth.loc[yr]) / (popg.loc[yr] / popg.loc[yr - 1])
    analog = {}
    for yr, val in [(2025, proj.loc[2025]), (2030, proj.loc[2030])]:
        near = (tha - val).abs().idxmin()
        analog[yr] = {"vn_gdp_pc_ppp": round(float(val)), "thai_year": int(near) if abs(tha.loc[near] - val) < 1500 else None,
                      "thai_value": round(float(tha.loc[near]))}
    fig, ax = plt.subplots(figsize=(9, 4))
    for i, iso in enumerate(NAMES):
        s = macro[macro.iso3 == iso].set_index("year")["gdp_pc_ppp"].dropna()
        ax.plot(s.index, s / 1000, lw=2.4 if iso == "VNM" else 1.4, color=SERIES[i], label=NAMES[iso])
    ax.plot(proj.index, proj / 1000, ls="--", color=SERIES[0], lw=2, label="Việt Nam — theo tăng trưởng IMF")
    ax.set_title("GDP bình quân đầu người theo sức mua (nghìn USD PPP 2021)")
    ax.legend(ncol=3, fontsize=8)
    f2 = savefig(fig, "a2_income_convergence.png", "NY.GDP.PCAP.PP.KD [D1]; nối dài 2026–2031 theo tăng trưởng GDP thực của IMF WEO trừ tăng dân số.")

    return {
        "cons_pc_usd_2015": round(float(cons.loc[2015])), "cons_pc_usd_2025": round(float(cons.loc[2025])),
        "cons_pc_usd_2035": round(float(cons.loc[2035])),
        "value_change_2015_2025_pct": round(float((mkt.value[2025] / mkt.value[2015] - 1) * 100), 1),
        "value_change_2025_2035_pct": round(float((mkt.value[2035] / mkt.value[2025] - 1) * 100), 1),
        "decomp_2025_2035_logpts": {"size": round(float(d_size * 100), 1), "income": round(float(d_inc * 100), 1)},
        "decomp_2015_2025_logpts": {"size": round(float(back_size * 100), 1), "income": round(float(back_inc * 100), 1)},
        "imf_real_gdp_growth": {int(k): v for k, v in imf.NGDP_RPCH.loc[2025:2031].items()},
        "imf_inflation": {int(k): v for k, v in imf.PCPIPCH.loc[2025:2031].items()},
        "thai_analog": analog, "figs": [f, f2],
    }


# ------------------------------------------------------------------------------------------------
def mobile_os():
    df = pd.read_csv(PROC / "statcounter_os_mobile.csv")
    df["date"] = pd.to_datetime(df["Date"])
    d = df[df.date >= "2016-01-01"].set_index("date")
    ios = d["iOS"]
    yearly = ios.groupby(ios.index.year).mean()
    tau, p, slope = mann_kendall(ios.loc["2022-01-01":].values)
    # Hồi quy logistic theo thời gian trên 2023–2026 để ngoại suy (có khoảng tin cậy bootstrap)
    seg = ios.loc["2023-01-01":]
    t = (seg.index - seg.index[0]).days.values / 365.25
    z = np.log(seg.values / (100 - seg.values))
    b, a = np.polyfit(t, z, 1)
    resid = z - (a + b * t)
    rng = np.random.default_rng(1)
    fut_t = np.arange(t[-1], t[-1] + 3.01, 1 / 12)
    sims = []
    for _ in range(2000):
        zz = a + b * t + rng.choice(resid, len(resid))
        bb, aa = np.polyfit(t, zz, 1)
        sims.append(100 / (1 + np.exp(-(aa + bb * fut_t))))
    sims = np.array(sims)
    fut_idx = pd.date_range(seg.index[-1], periods=len(fut_t), freq="MS")
    p50, p10, p90 = (np.percentile(sims, q, axis=0) for q in (50, 10, 90))

    fig, ax = plt.subplots(figsize=(9, 4))
    ax.plot(d.index, d["Android"], color=SERIES[0], lw=1.6, label="Android")
    ax.plot(d.index, ios, color=SERIES[1], lw=1.6, label="iOS")
    ax.fill_between(fut_idx, p10, p90, color=SERIES[1], alpha=0.18, lw=0)
    ax.plot(fut_idx, p50, color=SERIES[1], ls="--", lw=1.6, label="iOS — ngoại suy logistic (P10–P90)")
    ax.axhline(50, color=MUTED, lw=0.7, ls=":")
    ax.set_ylim(0, 100)
    ax.set_title("Thị phần lượt truy cập web từ điện thoại, Việt Nam (%)")
    ax.legend(fontsize=8, loc="upper left")
    f = savefig(fig, "a9_mobile_os.png", "Theo tháng, 2016-01 → 2026-09. Ngoại suy logistic trên 2023–2026, bootstrap 2.000 lượt; chỉ minh hoạ hướng đi, không phải dự báo chắc chắn.")

    cross = fut_idx[np.argmax(p50 >= 50)] if (p50 >= 50).any() else None
    vend = pd.read_csv(PROC / "statcounter_vendor_mobile.csv")
    last = vend.iloc[-1].drop("Date").astype(float).sort_values(ascending=False)
    hhi = float(((last / 100) ** 2).sum() * 10000)
    soc = pd.read_csv(PROC / "statcounter_social_all.csv")
    return {
        "ios_latest": float(ios.iloc[-1]), "ios_latest_month": str(ios.index[-1].date())[:7],
        "ios_yearly": {int(k): round(float(v), 1) for k, v in yearly.items()},
        "ios_low_2023": round(float(ios.loc["2023"].min()), 1),
        "mk_2022_2026": {"tau": round(tau, 2), "p": p, "sen_pp_per_month": round(slope, 3)},
        "ios_2027_09_p50": round(float(p50[12]), 1), "ios_2029_09_p50": round(float(p50[-1]), 1),
        "ios_2029_09_p10_p90": [round(float(p10[-1]), 1), round(float(p90[-1]), 1)],
        "ios_cross_50_month": str(cross.date())[:7] if cross is not None else None,
        "vendor_latest": {k: round(float(v), 1) for k, v in last.head(6).items()}, "vendor_hhi": round(hhi),
        "social_latest": {k: round(float(v), 1) for k, v in soc.iloc[-1].drop("Date").astype(float).sort_values(ascending=False).head(5).items()},
        "figs": [f],
    }


def fuel_shock():
    jf = pd.read_csv(PROC / "fred_DJFUELUSGULF.csv", parse_dates=["date"]).set_index("date")["DJFUELUSGULF"]
    br = pd.read_csv(PROC / "fred_DCOILBRENTEU.csv", parse_dates=["date"]).set_index("date")["DCOILBRENTEU"]
    yoy = jf.pct_change(12) * 100
    z = (yoy - yoy.loc[:"2025-12"].mean()) / yoy.loc[:"2025-12"].std()
    jan_sep_26 = jf.loc["2026-01":"2026-09"].mean()
    jan_sep_25 = jf.loc["2025-01":"2025-09"].mean()
    fuel_9m = (jan_sep_26 / jan_sep_25 - 1) * 100
    # Truyền dẫn: giá dịch vụ hàng không VN 9T/2026 +7,34% (Cục Thống kê) — chia cho % tăng nhiên liệu bay
    airfare_9m = 7.34
    road_9m = 8.06
    pass_through = airfare_9m / fuel_9m

    fig, axes = plt.subplots(1, 2, figsize=(10, 3.8), gridspec_kw={"width_ratios": [1.5, 1]})
    ax = axes[0]
    s = jf.loc["2015":]
    ax.plot(s.index, s, color=SERIES[0], lw=1.6)
    ax.set_title("Giá nhiên liệu bay (USD/gallon, Gulf Coast)")
    ax.axvspan(pd.Timestamp("2026-03-01"), s.index[-1], color=SERIES[7], alpha=0.12, lw=0)
    ax.annotate("Trung Đông 2026", (pd.Timestamp("2026-04-01"), s.loc["2026-04"].iloc[0]), xytext=(-110, 10),
                textcoords="offset points", fontsize=8, color=INK2, arrowprops=dict(arrowstyle="-", color=MUTED))
    ax = axes[1]
    pct = (yoy.dropna() <= yoy.iloc[-1]).mean() * 100
    ax.hist(yoy.loc[:"2025-12"].dropna(), bins=40, color="#b7d3f6")
    ax.axvline(yoy.iloc[-1], color=SERIES[7], lw=2)
    ax.text(yoy.iloc[-1], ax.get_ylim()[1] * 0.9, f" {yoy.index[-1]:%m/%Y}: {yoy.iloc[-1]:+.0f}%", color=SERIES[7], fontsize=9)
    ax.set_title("Phân phối mức tăng cùng kỳ 1991–2025 (%)")
    f = savefig(fig, "a10_fuel_shock.png", "Giá ngày → trung bình tháng. Truyền dẫn = % tăng giá dịch vụ hàng không VN 9T/2026 [S1] ÷ % tăng nhiên liệu bay cùng kỳ.")
    return {
        "jet_latest_usd_gal": round(float(jf.iloc[-1]), 2), "jet_latest_month": str(jf.index[-1].date())[:7],
        "jet_yoy_latest_pct": round(float(yoy.iloc[-1]), 1), "jet_yoy_z": round(float(z.iloc[-1]), 2),
        "jet_yoy_percentile": round(float(pct), 1), "jet_9m_2026_vs_2025_pct": round(float(fuel_9m), 1),
        "airfare_ppi_9m_pct": airfare_9m, "road_ppi_9m_pct": road_9m,
        "pass_through_airfare": round(float(pass_through), 3),
        "brent_latest": round(float(br.dropna().iloc[-1]), 1),
        "figs": [f],
    }


if __name__ == "__main__":
    demo, y = demography()
    pp = purchasing_power(y)
    update_summary("a1_demography", demo)
    update_summary("a2_purchasing_power", pp)
    update_summary("a9_mobile_os", mobile_os())
    update_summary("a10_fuel", fuel_shock())
    print("macro ok")
