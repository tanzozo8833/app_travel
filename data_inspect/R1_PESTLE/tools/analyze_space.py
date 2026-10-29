"""A6 khí hậu & rủi ro, A7 mô hình trọng lực không gian, A8 cung–cầu, A12 chấm điểm chọn tỉnh (QĐ3).

Phương pháp: chỉ số tiện nghi khí hậu (kiểu Holiday Climate Index, rút gọn), tần suất mưa cực đoan và
xu hướng Mann–Kendall 2005–2025, độ lệch cung–cầu theo tháng (chênh lệch chuẩn hoá),
mô hình trọng lực ước lượng bằng Poisson pseudo-maximum likelihood (PPML) với hiệu ứng cố định,
mô hình lựa chọn không gian Huff, áp lực cung (cầu / cơ sở lưu trú), phân tích đa tiêu chí (MCDA)
với trọng số ngẫu nhiên Dirichlet để đo độ vững của thứ hạng.
"""
import json

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import statsmodels.api as sm

from common import PROC, RES, SERIES, MUTED, INK2, style, savefig, update_summary
from analyze_macro import mann_kendall

style()
MON = [f"T{i}" for i in range(1, 13)]


def comfort_score(tmax, rain_mm, heavy_days):
    """0–100. Nhiệt: tối ưu 22–30°C; mưa tháng: ≤60 mm tốt, ≥400 mm xấu; ngày mưa ≥50 mm: rủi ro."""
    thermal = np.clip(1 - np.maximum(0, np.maximum(22 - tmax, tmax - 30)) / 10, 0, 1)
    rain = np.clip(1 - (rain_mm - 60) / 340, 0, 1)
    risk = np.clip(1 - heavy_days / 2.0, 0, 1)
    return 100 * (0.45 * thermal + 0.35 * rain + 0.20 * risk)


def climate():
    c = pd.read_csv(PROC / "climate_daily.csv.gz", parse_dates=["time"])
    dest = pd.read_csv(PROC / "destinations.csv").set_index("key")
    c["m"] = c.time.dt.month
    c["y"] = c.time.dt.year
    c["heavy"] = (c.precipitation_sum >= 50).astype(int)
    monthly = c.groupby(["key", "y", "m"]).agg(tmax=("temperature_2m_max", "mean"), tmin=("temperature_2m_min", "mean"),
                                              rain=("precipitation_sum", "sum"), heavy=("heavy", "sum")).reset_index()
    clim = monthly.groupby(["key", "m"]).mean(numeric_only=True).reset_index()
    clim["comfort"] = comfort_score(clim.tmax, clim.rain, clim.heavy)
    clim["name"] = clim.key.map(dest["name"])
    clim.to_csv(RES / "a6_climate_monthly.csv", index=False)

    # Xu hướng mưa cực đoan: số ngày ≥50 mm mỗi năm, Mann–Kendall
    yearly = c.groupby(["key", "y"]).heavy.sum().reset_index()
    tr = []
    for k, d in yearly.groupby("key"):
        tau, p, slope = mann_kendall(d.heavy.values)
        tr.append(dict(key=k, name=dest.loc[k, "name"], heavy_days_per_year=round(float(d.heavy.mean()), 1),
                       sen_per_decade=round(slope * 10, 2), tau=round(tau, 2), p=round(p, 3),
                       max_1day_mm=round(float(c[c.key == k].precipitation_sum.max()), 0)))
    trend = pd.DataFrame(tr).sort_values("heavy_days_per_year", ascending=False)
    # Kiểm định gộp cho 12 điểm miền Bắc: tổng số ngày mưa ≥50 mm mỗi năm
    north = dest[dest.type.isin(["Núi phía Bắc", "Di sản ĐB Bắc Bộ", "Biển miền Bắc"])].index
    ny = yearly[yearly.key.isin(north)].groupby("y").heavy.sum()
    tau, p, slope = mann_kendall(ny.values)
    pooled = {"n_dest": int(len(north)), "mean_per_year": round(float(ny.mean()), 1), "tau": round(tau, 2), "p": round(p, 4),
              "sen_per_decade": round(slope * 10, 1), "first5_mean": round(float(ny.iloc[:5].mean()), 1),
              "last5_mean": round(float(ny.iloc[-5:].mean()), 1)}
    (RES / "a6_north_pooled.json").write_text(__import__("json").dumps(pooled), encoding="utf-8")
    trend.to_csv(RES / "a6_extreme_rain_trend.csv", index=False)

    C = clim.pivot(index="name", columns="m", values="comfort")
    H = clim.pivot(index="name", columns="m", values="heavy")
    order = dest.sort_values(["type", "drive_h"])["name"]
    order = [o for o in order if o in C.index]
    fig, axes = plt.subplots(1, 2, figsize=(13, 7), sharey=True)
    for ax, M, cmap, title, fmt in [(axes[0], C.loc[order], "Greens", "Chỉ số tiện nghi khí hậu (0–100)", "{:.0f}"),
                                    (axes[1], H.loc[order], "Reds", "Số ngày mưa ≥50 mm trung bình/tháng", "{:.1f}")]:
        im = ax.imshow(M.values, cmap=cmap, aspect="auto")
        ax.set_xticks(range(12), MON)
        ax.set_yticks(range(len(M)), M.index, fontsize=8.5)
        for i in range(M.shape[0]):
            for j in range(12):
                v = M.values[i, j]
                ax.text(j, i, fmt.format(v), ha="center", va="center", fontsize=6.8,
                        color="white" if v > np.nanpercentile(M.values, 75) else "#0b0b0b")
        ax.grid(False)
        ax.set_title(title)
    f1 = savefig(fig, "a6_climate_heatmaps.png",
                 "Open-Meteo ERA5, dữ liệu ngày 2005–2025 tại toạ độ điểm đến. Tiện nghi = 45% nhiệt độ (tối ưu 22–30°C) + 35% lượng mưa tháng + 20% rủi ro mưa cực đoan.")
    return clim, trend, f1


def offseason_gap(clim):
    """Cửa sổ lệch mùa: tháng khí hậu tốt (z cao) nhưng nhu cầu thấp (z thấp)."""
    S = pd.read_csv(RES / "a5_seasonal_index_trends.csv", index_col=0)
    S.columns = S.columns.astype(int)
    C = clim.pivot(index="name", columns="m", values="comfort")
    names = [n for n in S.index if n in C.index]
    zc = C.loc[names].sub(C.loc[names].mean(1), axis=0).div(C.loc[names].std(1), axis=0)
    zd = S.loc[names].sub(S.loc[names].mean(1), axis=0).div(S.loc[names].std(1), axis=0)
    gap = zc - zd
    gap.to_csv(RES / "a6_offseason_gap.csv")
    best = []
    for n in names:
        g = gap.loc[n]
        m = int(g.idxmax())
        best.append(dict(name=n, best_month=m, gap=round(float(g.max()), 2), comfort=round(float(C.loc[n, m]), 0),
                         demand_index=round(float(S.loc[n, m]), 2), peak_month=int(S.loc[n].idxmax())))
    best = pd.DataFrame(best).sort_values("gap", ascending=False)
    fig, ax = plt.subplots(figsize=(10, 7))
    im = ax.imshow(gap.values, cmap="RdBu", vmin=-3, vmax=3, aspect="auto")
    ax.set_xticks(range(12), MON)
    ax.set_yticks(range(len(gap)), gap.index, fontsize=8.5)
    for i, n in enumerate(gap.index):
        j = int(gap.loc[n].idxmax()) - 1
        ax.add_patch(plt.Rectangle((j - 0.5, i - 0.5), 1, 1, fill=False, ec="#0b0b0b", lw=1.6))
    ax.grid(False)
    ax.set_title("Cửa sổ lệch mùa: xanh = thời tiết đẹp hơn mức thường mà ít người tìm; đỏ = đông mà thời tiết xấu")
    fig.colorbar(im, ax=ax, fraction=0.025, label="z(tiện nghi) − z(nhu cầu)")
    f = savefig(fig, "a6_offseason_gap.png", "Ô viền đen = tháng có chênh lệch lớn nhất của từng điểm đến — ứng viên cho ưu đãi 'đi lệch mùa'.")
    return best, f


# ------------------------------------------------------------------------------------------------
def haversine(lat1, lon1, lat2, lon2):
    R = 6371
    p1, p2 = np.radians(lat1), np.radians(lat2)
    dphi, dl = p2 - p1, np.radians(lon2 - lon1)
    a = np.sin(dphi / 2) ** 2 + np.cos(p1) * np.cos(p2) * np.sin(dl / 2) ** 2
    return 2 * R * np.arcsin(np.sqrt(a))


def gravity():
    tr = pd.read_csv(PROC / "trends_region.csv")
    rc = pd.read_csv(PROC / "trends_regions_coords.csv")
    dest = pd.read_csv(PROC / "destinations.csv").set_index("key")
    df = tr.merge(rc[["region", "lat", "lon"]], on="region").dropna(subset=["lat"])
    df["dlat"] = df.dest.map(dest.lat)
    df["dlon"] = df.dest.map(dest.lon)
    df["dist"] = np.maximum(haversine(df.lat, df.lon, df.dlat, df.dlon), 15)
    df = df[df.value.notna()]
    X = pd.concat([np.log(df["dist"]).rename("log_dist"),
                   pd.get_dummies(df.dest, prefix="d", dtype=float),
                   pd.get_dummies(df.region, prefix="o", drop_first=True, dtype=float)], axis=1)
    model = sm.GLM(df.value.astype(float), X, family=sm.families.Poisson()).fit(cov_type="HC1")
    beta = float(-model.params["log_dist"])
    ci = [float(-x) for x in model.conf_int().loc["log_dist"][::-1]]
    # Trends theo tỉnh chuẩn hoá riêng từng từ khoá → hiệu ứng cố định điểm đến KHÔNG so được giữa các điểm.
    # Sức hút nội tại = mức quan tâm toàn quốc (cùng thang nhờ từ mốc) ÷ khả năng tiếp cận trung bình từ 63 tỉnh.
    d5 = pd.read_csv(RES / "a5_destinations.csv").set_index("key")
    regs = rc.dropna(subset=["lat"])
    access = {k: float(np.mean(np.maximum(haversine(regs.lat.values, regs.lon.values, dest.loc[k, "lat"], dest.loc[k, "lon"]), 15) ** (-beta)))
              for k in dest.index}
    access = pd.Series(access)
    attract = (d5.trends_level_2025 / access).dropna()
    attract = attract / attract.max() * 100

    # Huff từ Hà Nội: P_j ∝ A_j · d_j^-β
    d_hn = haversine(21.0285, 105.8542, dest.lat, dest.lon).clip(lower=15)
    util = attract.reindex(dest.index) * d_hn ** (-beta)
    share = util / util.sum() * 100
    # Kiểm định ngoài mẫu trên Hà Nội: mức "vượt trung bình" quan sát (giá trị HN ÷ TB các tỉnh, cùng từ khoá)
    # so với dự đoán (d_HN^-β ÷ TB_r d_r^-β)
    hn = df[df.region.str.contains("Hà Nội|Hanoi", regex=True)].set_index("dest").value
    mean_r = df.groupby("dest").value.mean()
    obs_idx = (hn / mean_r).reindex(dest.index)
    pred_idx = (d_hn ** (-beta)) / access.reindex(dest.index)
    ok = obs_idx.notna() & (obs_idx > 0)
    r = float(np.corrcoef(np.log(obs_idx[ok]), np.log(pred_idx[ok]))[0, 1])
    out = pd.DataFrame({"name": dest["name"], "attractiveness": attract.reindex(dest.index).round(1),
                        "dist_from_hanoi_km": d_hn.round(0), "huff_share_hanoi_pct": share.round(1),
                        "hanoi_index_observed": obs_idx.round(2), "hanoi_index_predicted": pred_idx.round(2)})         .sort_values("huff_share_hanoi_pct", ascending=False)
    out.to_csv(RES / "a7_gravity_huff.csv")

    # Đường suy giảm theo khoảng cách
    fig, axes = plt.subplots(1, 2, figsize=(11, 4.2))
    ax = axes[0]
    resid = np.log(df.value + 1) - np.log(df.value + 1).groupby(df.dest).transform("mean")
    ax.scatter(df["dist"], resid, s=6, alpha=0.25, color=SERIES[0])
    xs = np.linspace(15, 1800, 100)
    ax.plot(xs, -beta * (np.log(xs) - np.log(df["dist"]).mean()), color=SERIES[1], lw=2,
            label=f"β = {beta:.2f} (KTC 95%: {ci[0]:.2f}–{ci[1]:.2f})")
    ax.set_xscale("log")
    ax.set_xlabel("Khoảng cách tỉnh tìm kiếm → điểm đến (km, log)")
    ax.set_ylabel("mức quan tâm (lệch so TB điểm đến, log)")
    ax.set_title("Quan tâm giảm theo khoảng cách")
    ax.legend(fontsize=8)
    ax = axes[1]
    o = out.dropna()
    ax.scatter(o.hanoi_index_predicted, o.hanoi_index_observed, color=SERIES[2], s=40)
    for _, rr in o.iterrows():
        ax.annotate(rr["name"], (rr.hanoi_index_predicted, rr.hanoi_index_observed), fontsize=7.5, xytext=(3, 2),
                    textcoords="offset points")
    ax.set_xscale("log")
    ax.set_yscale("log")
    lo_, hi_ = min(o.hanoi_index_predicted.min(), o.hanoi_index_observed.min()), max(o.hanoi_index_predicted.max(), o.hanoi_index_observed.max())
    ax.plot([lo_, hi_], [lo_, hi_], color=MUTED, lw=0.8, ls="--")
    ax.set_xlabel("Dự đoán: người Hà Nội quan tâm gấp mấy lần TB cả nước")
    ax.set_ylabel("Quan sát (Google Trends)")
    ax.set_title(f"Kiểm định trên Hà Nội (tương quan log r = {r:.2f})")
    f = savefig(fig, "a7_gravity.png",
                "PPML với hiệu ứng cố định nơi tìm và điểm đến; dữ liệu Google Trends theo tỉnh (63 tỉnh cũ) 2023–9/2026 cho 22 điểm đến.")
    return {"beta": round(beta, 3), "beta_ci95": [round(ci[0], 3), round(ci[1], 3)], "n_pairs": int(len(df)),
            "r_log_hanoi_index": round(r, 2), "pseudo_r2": round(float(1 - model.deviance / model.null_deviance), 3),
            "attractiveness": out.attractiveness.to_dict(), "table": out.reset_index().to_dict("records"), "fig": f}, out


def supply():
    osm = pd.read_csv(PROC / "osm_supply.csv")
    d5 = pd.read_csv(RES / "a5_destinations.csv")
    m = d5.merge(osm, on="key", how="left")
    m["pressure"] = m.pageviews_2025 / m.lodging.clip(lower=1)
    m.to_csv(RES / "a8_supply_demand.csv", index=False)
    fig, ax = plt.subplots(figsize=(9.5, 5.5))
    ax.scatter(m.lodging, m.pageviews_2025, s=60, color=SERIES[0], alpha=0.8)
    for _, r in m.iterrows():
        ax.annotate(r["name"], (r.lodging, r.pageviews_2025), fontsize=8, xytext=(4, 2), textcoords="offset points")
    ax.set_xscale("log")
    ax.set_yscale("log")
    b, a = np.polyfit(np.log(m.lodging.clip(lower=1)), np.log(m.pageviews_2025), 1)
    xs = np.linspace(np.log(m.lodging.clip(lower=1).min()), np.log(m.lodging.max()), 50)
    ax.plot(np.exp(xs), np.exp(a + b * xs), color=MUTED, ls="--", lw=1, label=f"đường hồi quy log–log, hệ số {b:.2f}")
    ax.set_xlabel("Số cơ sở lưu trú trong bán kính 15 km (OpenStreetMap, log)")
    ax.set_ylabel("Lượt xem Wikipedia 2025 (log)")
    ax.set_title("Cầu (sự chú ý) so với cung (lưu trú): trên đường = cầu vượt cung")
    ax.legend(fontsize=8)
    f = savefig(fig, "a8_supply_demand.png", "OSM chỉ đếm cơ sở đã được vẽ lên bản đồ — thiếu nhiều homestay nhỏ; dùng để so tương đối.")
    m["resid"] = np.log(m.pageviews_2025) - (a + b * np.log(m.lodging.clip(lower=1)))
    return {"elasticity_attention_lodging": round(float(b), 2),
            "table": m[["name", "lodging", "attractions", "food", "pageviews_2025", "resid"]].round(2).to_dict("records"),
            "fig": f}, m


def mcda(gtab, clim, trend, sup):
    d5 = pd.read_csv(RES / "a5_destinations.csv")
    aff = pd.read_csv(RES / "a3_affordability.csv")[["name", "afford_student", "afford_worker"]]
    C = clim.pivot(index="name", columns="m", values="comfort")
    shoulder = C[[3, 4, 5, 9, 10, 11]].mean(1).rename("shoulder_comfort")
    risk = trend.set_index("name").heavy_days_per_year.rename("heavy_days")
    m = d5.merge(aff, on="name").merge(shoulder, left_on="name", right_index=True) \
        .merge(risk, left_on="name", right_index=True).merge(sup[["name", "resid"]], on="name", how="left")
    crit = {  # tiêu chí: (cột, hướng: +1 càng cao càng tốt)
        "Gần Hà Nội (giờ xe)": ("drive_h", -1), "Sinh viên đủ tiền": ("afford_student", +1),
        "Đà tăng quan tâm": ("momentum_trends_25_23", +1), "Quy mô quan tâm": ("trends_level_2025", +1),
        "Ít dồn mùa (Gini thấp)": ("gini_season", -1), "Khí hậu mùa giao": ("shoulder_comfort", +1),
        "Ít mưa cực đoan": ("heavy_days", -1), "Cung còn dư (cầu/cung thấp)": ("resid", -1),
    }
    w0 = np.array([0.20, 0.15, 0.15, 0.10, 0.10, 0.10, 0.10, 0.10])
    Z = pd.DataFrame(index=m.name)
    for k, (col, sgn) in crit.items():
        x = m[col].values.astype(float)
        if col == "trends_level_2025":
            x = np.log(x)
        x = np.nan_to_num(x, nan=np.nanmedian(x))
        z = (x - x.min()) / (x.max() - x.min() + 1e-9)
        Z[k] = z if sgn > 0 else 1 - z
    base = Z.values @ w0
    rng = np.random.default_rng(7)
    W = rng.dirichlet(w0 * 40, 20000)
    scores = Z.values @ W.T
    ranks = (-scores).argsort(0).argsort(0) + 1
    res = pd.DataFrame({"name": Z.index, "score": base.round(3), "p_top3": (ranks <= 3).mean(1).round(3),
                        "median_rank": np.median(ranks, 1)}).sort_values("score", ascending=False)
    res = res.merge(Z.round(2), left_on="name", right_index=True)
    res.to_csv(RES / "a12_mcda.csv", index=False)

    top = res.head(10)
    fig, axes = plt.subplots(1, 2, figsize=(12, 5), gridspec_kw={"width_ratios": [1.4, 1]})
    ax = axes[0]
    left = np.zeros(len(top))
    for i, k in enumerate(crit):
        v = top[k].values * w0[i]
        ax.barh(top.name, v, left=left, color=SERIES[i % 8], label=k, height=0.65, edgecolor="white", lw=1)
        left += v
    ax.invert_yaxis()
    ax.set_title("Điểm tổng hợp chọn điểm đến thí điểm (trọng số gốc)")
    ax.legend(fontsize=7.5, loc="upper center", bbox_to_anchor=(0.5, -0.08), ncol=4)
    ax = axes[1]
    ax.barh(top.name, top.p_top3 * 100, color=SERIES[0])
    ax.invert_yaxis()
    ax.set_xlim(0, 100)
    ax.set_title("Xác suất lọt top 3 khi trọng số thay đổi (%)")
    for i, v in enumerate(top.p_top3 * 100):
        ax.text(v + 1, i, f"{v:.0f}%", va="center", fontsize=8)
    f = savefig(fig, "a12_mcda.png", "20.000 bộ trọng số Dirichlet quanh trọng số gốc; tiêu chí chuẩn hoá min–max.")
    return {"weights": dict(zip(crit, w0.tolist())), "ranking": res.head(12).to_dict("records"), "fig": f}, res, Z, w0


if __name__ == "__main__":
    clim, trend, f1 = climate()
    best, f2 = offseason_gap(clim)
    update_summary("a6_climate", {"extreme_rain": trend.to_dict("records"), "offseason": best.to_dict("records"),
                                  "north_pooled": json.loads((RES / "a6_north_pooled.json").read_text(encoding="utf-8")),
                                  "figs": [f1, f2]})
    g, gtab = gravity()
    update_summary("a7_gravity", {k: v for k, v in g.items() if k != "fig"} | {"figs": [g["fig"]]})
    s, sup = supply()
    update_summary("a8_supply", {k: v for k, v in s.items() if k != "fig"} | {"figs": [s["fig"]]})
    mc, res, Z, w0 = mcda(gtab, clim, trend, sup)
    update_summary("a12_mcda", {k: v for k, v in mc.items() if k != "fig"} | {"figs": [mc["fig"]]})
    print("space ok")
