"""A3 khả năng chi trả & đường cầu, A11 quy mô thị trường (Monte Carlo) — nhóm 20–29 tuổi ở Hà Nội.

Phương pháp: ước lượng phân phối log-chuẩn từ dữ liệu nhóm (grouped data), chi phí tổng quát
(giá vé + giá trị thời gian), đường cầu tham gia theo giá, độ co giãn cung (arc elasticity),
mô phỏng Monte Carlo TAM/SAM/SOM, độ nhạy kiểu tornado (tương quan hạng Spearman).
Mọi giả định nằm trong ASSUME — sửa ở đây rồi chạy lại.
"""
import json

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from scipy import optimize, stats

from common import PROC, RES, SERIES, MUTED, INK2, style, savefig, update_summary

style()
RNG = np.random.default_rng(42)

ASSUME = {
    # Người đi làm trẻ ở Hà Nội: thu nhập tháng log-chuẩn (GT — hiệu chỉnh từ TN bình quân 8,4tr cả nước, 10,1tr thành thị 2025)
    "worker_income_median_m": 9.0, "worker_income_sigma": 0.55,
    "worker_budget_share": 0.45,     # ngân sách 1 chuyến ≈ 45% thu nhập tháng (3–5tr / 8,4tr)
    "student_budget_months": 1.5,    # sinh viên dành dụm ≈ 1,5 tháng chi tiêu cho 1 chuyến (GT)
    # Chi phí chuyến 3 ngày 2 đêm / người, giá 2026
    "bus_vnd_per_km": 1000,          # xe giường nằm HN–Sa Pa 270–370k / 314 km
    "lodging_per_person_night": 175_000,   # phòng 350k chia 2 (homestay bình dân, GT)
    "food_activity_per_day": 300_000,
    "nights": 2,
    "value_of_time_vnd_h": 25_000,   # ≈ thu nhập 9tr / 176 giờ làm × 0,5 (thời gian nghỉ, GT)
    "pass_through": 0.123,
    "max_bus_hours_one_way": 12,           # từ A10: giá vé bay +7,34% khi nhiên liệu +59,8%
}

# Vé máy bay khứ hồi thực tế hè 2026 (The Leader 25/05/2026) — dùng để ước lượng giá theo cự ly
FARES = pd.DataFrame([
    ("Đà Nẵng", 767, 3.2, 4.0), ("Phú Quốc", 1606, 4.3, 5.0), ("Nha Trang", 1272, 4.7, 5.4), ("TP.HCM", 1497, 4.5, 7.0),
], columns=["dest", "road_km", "lo", "hi"])


def fit_student_spending():
    """Ước lượng log-chuẩn từ 4 nhóm chi tiêu của Cốc Cốc 2025: <1, 1–3, 3–5, >5 triệu."""
    edges = np.array([1, 3, 5])
    obs_cdf = np.array([0.20, 0.55, 0.79])

    def loss(p):
        mu, s = p
        return ((stats.norm.cdf((np.log(edges) - mu) / s) - obs_cdf) ** 2).sum()
    r = optimize.minimize(loss, [np.log(2.5), 0.8], bounds=[(-1, 3), (0.1, 3)])
    mu, s = r.x
    return float(np.exp(mu)), float(s), float(r.fun)


def flight_fare_model():
    y = (FARES.lo + FARES.hi) / 2 * 1e6
    b, a = np.polyfit(FARES.road_km, y, 1)
    return a, b


def trip_costs(dest, fuel_shock=0.0, bus_shock=0.0, a=None, b=None, vot=None):
    A = ASSUME
    vot = A["value_of_time_vnd_h"] if vot is None else vot
    stay = A["nights"] * A["lodging_per_person_night"] + (A["nights"] + 1) * A["food_activity_per_day"]
    bus = 2 * dest.road_km * A["bus_vnd_per_km"] * (1 + bus_shock)
    bus_time = 2 * dest.drive_h * 1.25            # xe khách chậm hơn ô tô con ~25% (GT)
    fly = (a + b * dest.road_km) * (1 + A["pass_through"] * fuel_shock)
    fly_time = 2 * 3.5                            # 1h30 bay + ra sân bay, chờ
    fly = np.where(dest.road_km < 450, np.inf, fly)   # dưới ~450 km không có đường bay thực tế
    # Ràng buộc thời gian: chuyến 3N2Đ không thể ngồi xe quá ~12 giờ mỗi chiều
    bus = np.where(dest.drive_h * 1.25 > A["max_bus_hours_one_way"], np.inf, bus)
    g_bus = bus + vot * bus_time
    g_fly = fly + vot * fly_time
    mode = np.where(g_fly < g_bus, "máy bay", "xe khách")
    cash = np.where(g_fly < g_bus, fly, bus) + stay
    return pd.DataFrame({"name": dest["name"], "road_km": dest.road_km, "bus_cost": bus + stay,
                         "fly_cost": fly + stay, "mode": mode, "cost": cash})


def affordability():
    dest = pd.read_csv(PROC / "destinations.csv")
    A = ASSUME
    med_s, sig_s, err = fit_student_spending()
    a, b = flight_fare_model()
    n = 200_000
    workers = np.exp(np.log(A["worker_income_median_m"] * 1e6) + A["worker_income_sigma"] * RNG.standard_normal(n))
    students = np.exp(np.log(med_s * 1e6) + sig_s * RNG.standard_normal(n))
    w_budget = workers * A["worker_budget_share"]
    s_budget = students * A["student_budget_months"]

    base = trip_costs(dest, 0, 0, a, b)
    # Kịch bản sốc 2026 so với 2025: nhiên liệu bay +59,8%, xe khách +8,06% → giá 2025 = giá 2026 / (1+sốc)
    pre = trip_costs(dest, -0.598 / 1.598, -0.0806 / 1.0806, a, b)
    base["afford_worker"] = [(w_budget >= c).mean() * 100 for c in base.cost]
    base["afford_student"] = [(s_budget >= c).mean() * 100 for c in base.cost]
    base["afford_worker_2025"] = [(w_budget >= c).mean() * 100 for c in pre.cost]
    base["afford_student_2025"] = [(s_budget >= c).mean() * 100 for c in pre.cost]
    base["cost_2025"] = pre.cost
    base = base.sort_values("road_km")
    base.to_csv(RES / "a3_affordability.csv", index=False)

    # Đường cầu: tỉ lệ đủ khả năng theo chi phí chuyến; độ co giãn tại các mức giá
    grid = np.linspace(0.8e6, 9e6, 120)
    dw = np.array([(w_budget >= c).mean() for c in grid]) * 100
    ds = np.array([(s_budget >= c).mean() for c in grid]) * 100

    def elast(budget, c):
        h = 0.05
        q1, q0 = (budget >= c * (1 + h)).mean(), (budget >= c * (1 - h)).mean()
        return (np.log(q1) - np.log(q0)) / (np.log(1 + h) - np.log(1 - h))

    el = {f"{c / 1e6:.0f}tr": {"worker": round(float(elast(w_budget, c)), 2), "student": round(float(elast(s_budget, c)), 2)}
          for c in [2e6, 3e6, 4e6, 5e6, 6e6]}

    fig, axes = plt.subplots(1, 2, figsize=(11, 4.3), gridspec_kw={"width_ratios": [1, 1.25]})
    ax = axes[0]
    ax.plot(grid / 1e6, dw, lw=2.2, label="Người đi làm 22–29")
    ax.plot(grid / 1e6, ds, lw=2.2, label="Sinh viên")
    for c in [3, 5]:
        ax.axvline(c, color=MUTED, lw=0.7, ls=":")
    ax.set_xlabel("Chi phí một chuyến / người (triệu đồng)")
    ax.set_ylabel("% đủ khả năng chi trả")
    ax.set_title("Đường cầu tham gia theo giá chuyến đi")
    ax.legend(fontsize=8, loc="lower left")
    ax = axes[1]
    d = base.sort_values("cost")
    y = np.arange(len(d))
    ax.barh(y, d.afford_worker, color=SERIES[0], height=0.42, label="Đi làm — 2026")
    ax.barh(y + 0.42, d.afford_student, color=SERIES[1], height=0.42, label="Sinh viên — 2026")
    ax.scatter(d.afford_worker_2025, y, marker="|", color="#0b0b0b", s=60, zorder=3, label="mức 2025 (trước sốc nhiên liệu)")
    ax.scatter(d.afford_student_2025, y + 0.42, marker="|", color="#0b0b0b", s=60, zorder=3)
    ax.set_yticks(y + 0.21, [f"{n} ({m}, {c / 1e6:.1f}tr)" for n, m, c in zip(d.name, d["mode"], d.cost)], fontsize=8)
    ax.invert_yaxis()
    ax.set_xlim(0, 100)
    ax.set_title("% người trẻ Hà Nội đủ tiền đi (3N2Đ)")
    ax.legend(fontsize=7.5, loc="lower right")
    f = savefig(fig, "a3_affordability.png",
                "Mô phỏng 200.000 người: thu nhập log-chuẩn (GT), chi tiêu sinh viên ước lượng từ Cốc Cốc 2025; giá vé bay hồi quy theo giá thực tế hè 2026; xe khách 1.000đ/km.")

    near = base[base.road_km < 450]
    far = base[base.road_km >= 450]
    return {
        "assumptions": A, "student_spending_fit": {"median_m": round(med_s, 2), "sigma": round(sig_s, 2), "sse": err},
        "flight_fare_model": {"intercept_vnd": round(a), "vnd_per_km": round(b)},
        "elasticity": el,
        "avg_afford_worker_near": round(float(near.afford_worker.mean()), 1),
        "avg_afford_worker_far": round(float(far.afford_worker.mean()), 1),
        "avg_afford_student_near": round(float(near.afford_student.mean()), 1),
        "avg_afford_student_far": round(float(far.afford_student.mean()), 1),
        "far_drop_worker_pp": round(float((far.afford_worker_2025 - far.afford_worker).mean()), 1),
        "far_drop_student_pp": round(float((far.afford_student_2025 - far.afford_student).mean()), 1),
        "table": base[["name", "mode", "cost", "afford_worker", "afford_student"]].round(1).to_dict("records"),
        "figs": [f],
    }


def market_size():
    prov = pd.read_csv(PROC / "provinces34.csv")
    hn_pop = float(prov.loc[prov.province == "Hà Nội", "pop"].iloc[0])
    summ = json.loads((RES / "summary.json").read_text(encoding="utf-8"))
    nat_share = summ["a1_demography"]["share_20_29_2025_pct"] / 100
    n = 100_000
    tri = lambda lo, mode, hi: RNG.triangular(lo, mode, hi, n)
    P = {
        "Tỉ lệ 20–29 tuổi trong dân Hà Nội": tri(nat_share, 0.15, 0.18),   # Hà Nội đông sinh viên, lao động nhập cư
        "Tỉ lệ đi du lịch nội địa/năm": tri(0.65, 0.78, 0.88),             # Q&Me: 80% đi trong 12 tháng (20–60 tuổi)
        "Số chuyến/năm (người có đi)": tri(1.5, 2.5, 4.0),                # Agoda 2026: "tới 6 chuyến"
        "Chi tiêu/chuyến (triệu đồng)": tri(2.5, 3.5, 5.5),              # Vietnam Report 3–5tr
        "Tỉ trọng chi tại đối tác địa phương": tri(0.35, 0.45, 0.60),      # ở + ăn + hoạt động, không gồm vé
        "Tỉ lệ người dùng Levart sau 3 năm": tri(0.005, 0.02, 0.05),
        "Hoa hồng đối tác (take rate)": tri(0.05, 0.08, 0.12),
    }
    people = hn_pop * P["Tỉ lệ 20–29 tuổi trong dân Hà Nội"]
    tam = people * P["Tỉ lệ đi du lịch nội địa/năm"] * P["Số chuyến/năm (người có đi)"] * P["Chi tiêu/chuyến (triệu đồng)"] * 1e6
    sam = tam * P["Tỉ trọng chi tại đối tác địa phương"]
    som_gmv = sam * P["Tỉ lệ người dùng Levart sau 3 năm"]
    rev = som_gmv * P["Hoa hồng đối tác (take rate)"]
    q = lambda x: [round(float(np.percentile(x, k)) / 1e9, 1) for k in (10, 50, 90)]

    # Tornado: tương quan hạng Spearman giữa từng giả định và doanh thu
    sens = {k: float(stats.spearmanr(v, rev)[0]) for k, v in P.items()}
    sens = dict(sorted(sens.items(), key=lambda kv: abs(kv[1])))

    fig, axes = plt.subplots(1, 2, figsize=(11, 4), gridspec_kw={"width_ratios": [1.1, 1]})
    ax = axes[0]
    ax.hist(rev / 1e9, bins=80, color="#9ec5f4")
    for k, ls in zip((10, 50, 90), (":", "-", ":")):
        v = np.percentile(rev, k) / 1e9
        ax.axvline(v, color=SERIES[0], ls=ls, lw=1.5)
        ax.text(v, ax.get_ylim()[1] * (0.92 if k == 50 else 0.8), f" P{k}: {v:.1f} tỷ", fontsize=8, color=INK2)
    ax.set_xlabel("Doanh thu hoa hồng/năm sau 3 năm (tỷ đồng)")
    ax.set_title("Phân phối doanh thu khả dĩ — chỉ khách Hà Nội 20–29")
    ax.set_xlim(0, np.percentile(rev, 99.5) / 1e9)
    ax = axes[1]
    ax.barh(list(sens.keys()), list(sens.values()), color=[SERIES[0] if v > 0 else SERIES[7] for v in sens.values()])
    ax.set_title("Giả định nào quyết định kết quả (Spearman ρ)")
    ax.set_xlim(0, 1)
    f = savefig(fig, "a11_market_montecarlo.png", "Monte Carlo 100.000 lượt; phân phối tam giác cho mọi giả định (xem tools/analyze_market.py).")

    return {
        "hanoi_pop": hn_pop, "people_20_29_p50_m": round(float(np.median(people)) / 1e6, 2),
        "TAM_bn_vnd_p10_p50_p90": q(tam), "SAM_bn_vnd": q(sam), "SOM_gmv_bn_vnd": q(som_gmv), "revenue_bn_vnd": q(rev),
        "sensitivity_spearman": {k: round(v, 2) for k, v in sens.items()},
        "figs": [f],
    }


if __name__ == "__main__":
    update_summary("a3_affordability", affordability())
    update_summary("a11_market", market_size())
    print("market ok")
