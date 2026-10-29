"""B7 — Tổng hợp: chỉ số sức ép của từng lực (0–4) tính từ chỉ báo đo được.

Mỗi chỉ báo được quy về thang 0–4 bằng một luật ghi rõ (cột "luật"); điểm lực = trung bình các chỉ báo.
Độ vững: 20.000 bộ trọng số Dirichlet cho các chỉ báo trong mỗi lực → khoảng P10–P90 của điểm lực.
Kết quả lưu ở results/b7_force_indicators.csv và summary.json › f_synthesis (tệp 07 của R2 đọc lại từ đây).
"""
import json

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from common import RES, ROOT, SERIES, MUTED, INK2, style, savefig, update_summary

style()
clip = lambda x: float(np.clip(x, 0, 4))
NAME = lambda s: "Rất cao" if s >= 3.25 else "Cao" if s >= 2.5 else "Trung bình" if s >= 1.75 else "Thấp–trung bình" if s >= 1 else "Thấp"


def synth():
    S = json.loads((RES / "summary.json").read_text(encoding="utf-8"))
    r1p = ROOT.parent / "R1_PESTLE" / "results" / "summary.json"
    R1 = json.loads(r1p.read_text(encoding="utf-8")) if r1p.exists() else {}
    b1, b2, b3, b4, b5, b6 = (S.get(k, {}) for k in ["b1_overview", "b2_rivalry", "b3_entrants", "b4_buyers", "b5_suppliers", "b6_substitutes"])
    conc = b2["concentration"]["vn_ratings_ios"]
    cos_max = max(r["cos_levart"] for r in b2["levart_cos_top"])
    rank = np.mean(list(b2["share_of_search"]["rank_corr"].values()))
    growth = b1.get("gva_trend_2023_2025_pct", 8)
    coh = pd.DataFrame(b3["cohorts"]).T
    ios_recent = coh.loc[[str(y) for y in range(2022, 2026) if str(y) in coh.index], "ios_new"].sum() / coh.ios_new.sum() * 100 if "ios_new" in coh else 30
    booking_apps = ["Traveloka", "Agoda", "Booking.com", "Trip.com", "Klook", "Vexere", "Mytour", "iVIVU"]
    reply = np.mean([b4["per_app_play"][a]["reply"] for a in booking_apps if a in b4["per_app_play"]])
    el = R1.get("a3_affordability", {}).get("elasticity", {})
    e45 = np.mean([abs(el.get("4tr", {}).get("worker", -1.4)), abs(el.get("5tr", {}).get("worker", -1.9))])
    ratio = b6.get("substitute_to_planning_installs_ratio") or 100
    gpt = b6.get("chatgpt_vs_lichtrinh_2025") or 5
    tour = b6.get("tour_search_change_2019_2025_pct") or 0
    fbar = b5.get("bargaining", {})
    occ = fbar.get("assume", {}).get("occupancy", 0.45)
    chain = b5.get("chain_share_pct")

    rows = [
        # lực, chỉ báo, giá trị, luật, điểm, nguồn
        ("Cạnh tranh hiện tại", "Độ trùng tính năng lớn nhất với Levart (cosine)", round(cos_max, 2), "4 × cosine", clip(4 * cos_max), "D2, D4"),
        ("Cạnh tranh hiện tại", "HHI nhóm đặt chỗ (App Store VN)", conc["HHI"], "HHI ÷ 1.000", clip(conc["HHI"] / 1000), "D4"),
        ("Cạnh tranh hiện tại", "Độ ổn định thứ hạng (Spearman giữa các năm)", round(rank, 2), "4 × ρ (thứ hạng cứng = khó chen)", clip(4 * rank), "D6"),
        ("Cạnh tranh hiện tại", "Tăng trưởng thực của ngành 2023–2025 (%/năm)", growth, "4 − tăng trưởng ÷ 3 (ngành tăng nhanh làm dịu cạnh tranh)", clip(4 - growth / 3), "D1"),
        ("Đối thủ mới gia nhập", "% app du lịch iOS ra mắt từ 2022", round(ios_recent, 1), "% ÷ 10", clip(ios_recent / 10), "D4"),
        ("Đối thủ mới gia nhập", "Chi phí đăng ký phát hành (USD)", 25, "phí ≤ 100 USD → 4", 4.0, "S6"),
        ("Đối thủ mới gia nhập", "% app mới (từ 2022) đạt ≥ 100.000 lượt cài", b3["p_new_reach_100k"], "4 × % ÷ 25 (ít người mới thành công = mối đe doạ thấp)", clip(4 * b3["p_new_reach_100k"] / 25), "D2"),
        ("Đối thủ mới gia nhập", "Gini lượt cài (thắng thua dồn về số ít)", b3["gini_installs"], "4 × (1 − Gini) × 4, tối đa 4", clip(16 * (1 - b3["gini_installs"])), "D2"),
        ("Quyền lực người dùng", "% đánh giá app đặt chỗ nhắc tên đối thủ (so sánh, dùng nhiều app)", b4["cross_mention_share_pct"], "% ÷ 5 × 4 (nhắc tên đối thủ hiếm trong đánh giá; 5% coi là rất nhiều)", clip(b4["cross_mention_share_pct"] / 5 * 4), "D3, D5"),
        ("Quyền lực người dùng", "Tỉ lệ OTA trả lời đánh giá (phải chiều người dùng)", round(reply, 2), "4 × tỉ lệ", clip(4 * reply), "D3"),
        ("Quyền lực người dùng", "|Độ co giãn| tham gia ở chuyến 4–5 triệu (R1)", round(e45, 2), "4 × |e| ÷ 2", clip(4 * e45 / 2), "D8"),
        ("Quyền lực người dùng", "Có sản phẩm thay thế miễn phí", 1, "có → 4", 4.0, "D2"),
        ("Nhà cung cấp — địa phương", "Công suất phòng (thừa cung → yếu)", occ, "4 × công suất ÷ 0,9", clip(4 * occ / 0.9), "S1"),
        ("Nhà cung cấp — địa phương", "Mức phí tối đa homestay vẫn có lợi (f_max)", fbar.get("f_max_pct", 60), "4 × (1 − f_max)", clip(4 * (1 - fbar.get("f_max_pct", 60) / 100)), "S1, S2, GT"),
    ]
    if chain is not None:
        rows.append(("Nhà cung cấp — địa phương", "% cơ sở lưu trú thuộc chuỗi (OSM)", chain, "4 × % ÷ 50", clip(4 * chain / 50), "D7"))
    rows += [
        ("Nhà cung cấp — công nghệ", "Số nhà cung cấp then chốt không thay ngay được (Play, Maps, Firebase, Gemini)", 4, "số ÷ 4 × 3 (GT)", 3.0, "S6, S7, GT"),
        ("Nhà cung cấp — công nghệ", "Phí kho ứng dụng (%)", 15, "% ÷ 5", clip(15 / 5), "S6"),
        ("Sản phẩm thay thế", "Lượt cài thay thế ÷ app lên kế hoạch, sưu tầm (lần)", ratio, "4 × log10(tỉ lệ) ÷ 3", clip(4 * np.log10(max(ratio, 1)) / 3), "D2"),
        ("Sản phẩm thay thế", "Tìm 'chatgpt' ÷ tìm 'lịch trình' (2025)", gpt, "4 × log10(tỉ lệ) ÷ 1", clip(4 * np.log10(max(gpt, 1))), "D6"),
        ("Sản phẩm thay thế", "Thay đổi tìm 'tour du lịch' 2019→2025 (%)", tour, "4 × (1 + % ÷ 100) (tour truyền thống giảm = bớt một thay thế)", clip(4 * (1 + tour / 100)), "D6"),
    ]
    ind = pd.DataFrame(rows, columns=["force", "indicator", "value", "rule", "score", "source"])
    ind.to_csv(RES / "b7_force_indicators.csv", index=False)

    rng = np.random.default_rng(3)
    fi = {}
    for f, g in ind.groupby("force", sort=False):
        sc = g.score.values
        W = rng.dirichlet(np.ones(len(sc)) * 2, 20000)
        sims = W @ sc
        fi[f] = {"score": round(float(sc.mean()), 2), "p10": round(float(np.percentile(sims, 10)), 2), "p90": round(float(np.percentile(sims, 90)), 2),
                 "level": NAME(sc.mean()), "drivers": "; ".join(f"{r.indicator} = {r.value}" for _, r in g.iterrows())}

    order = ["Cạnh tranh hiện tại", "Đối thủ mới gia nhập", "Quyền lực người dùng", "Nhà cung cấp — địa phương", "Nhà cung cấp — công nghệ", "Sản phẩm thay thế"]
    desk = {"Cạnh tranh hiện tại": 2.5, "Đối thủ mới gia nhập": 3, "Quyền lực người dùng": 4, "Nhà cung cấp — địa phương": 1.5,
            "Nhà cung cấp — công nghệ": 3, "Sản phẩm thay thế": 4}
    ang = np.linspace(0, 2 * np.pi, len(order), endpoint=False).tolist()
    fig = plt.figure(figsize=(12.5, 5.2))
    gs = fig.add_gridspec(1, 2, width_ratios=[1, 1.15], wspace=0.75)
    ax = fig.add_subplot(gs[0], polar=True)
    for vals, lab, c, ls in [([desk[o] for o in order], "Nghiên cứu tại bàn (R2 tệp 02–06)", SERIES[0], "-"),
                             ([fi[o]["score"] for o in order], "Chỉ số đo từ dữ liệu", SERIES[1], "--")]:
        ax.plot(ang + ang[:1], vals + vals[:1], color=c, lw=2, ls=ls, label=lab)
        ax.fill(ang + ang[:1], vals + vals[:1], color=c, alpha=0.1)
    ax.set_xticks(ang, [o.replace(" — ", "\n") for o in order], fontsize=8)
    ax.set_ylim(0, 4)
    ax.set_yticks([1, 2, 3, 4], ["1", "2", "3", "4"], fontsize=7, color=MUTED)
    ax.set_rlabel_position(200)
    ax.tick_params(axis="x", pad=10)
    ax.legend(fontsize=7.5, loc="upper center", bbox_to_anchor=(0.5, -0.1), ncol=2)
    ax2 = fig.add_subplot(gs[1])
    y = np.arange(len(order))
    sc = [fi[o]["score"] for o in order]
    ax2.barh(y, sc, color=SERIES[1], height=0.5)
    ax2.errorbar(sc, y, xerr=[[s - fi[o]["p10"] for s, o in zip(sc, order)], [fi[o]["p90"] - s for s, o in zip(sc, order)]],
                 fmt="none", ecolor=INK2, capsize=3)
    ax2.scatter([desk[o] for o in order], y, marker="D", color=SERIES[0], zorder=3, label="tại bàn")
    ax2.set_yticks(y, order, fontsize=8.5)
    ax2.invert_yaxis()
    ax2.set_xlim(0, 4.2)
    ax2.set_title("Chỉ số đo (thanh, P10–P90 khi đổi trọng số)\nvà mức tại bàn (◆)", fontsize=11)
    ax2.legend(fontsize=8, loc="upper center", bbox_to_anchor=(0.5, -0.1))
    f = savefig(fig, "b7_force_radar.png", "Bảng chỉ báo và luật quy đổi: results/b7_force_indicators.csv. Luật quy đổi là lựa chọn của nhóm (GT) — đổi luật thì điểm đổi.")
    return {"force_index": fi, "indicators": ind.to_dict("records"), "desk": desk, "figs": [f]}


if __name__ == "__main__":
    update_summary("f_synthesis", synth())
    print("synthesis ok")
