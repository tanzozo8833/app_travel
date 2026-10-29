"""B1 — Tổng quan ngành: quy mô, tăng trưởng, phục hồi, dự báo.

Phương pháp: bóc số liệu từ văn bản báo cáo Cục Thống kê bằng biểu thức chính quy (có kiểm tra chéo),
chuỗi luỹ kế từ đầu năm → chuỗi năm; tăng trưởng danh nghĩa và thực (khử lạm phát bằng CPI của R1),
CAGR, chỉ số phục hồi so với 2019, hồi quy log-tuyến tính để ngoại suy, so sánh các ước lượng thị trường trực tuyến.
"""
import json
import re
import unicodedata

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from common import PROC, RES, ROOT, SERIES, MUTED, INK2, style, savefig, update_summary

style()
NUM = r"(\d{1,3}(?:\.\d{3})*(?:,\d+)?)"


def vn(x):
    return float(x.replace(".", "").replace(",", "."))


def period(title):
    t = title.lower()
    y = re.findall(r"(20\d\d)", t)
    year = int(y[-1]) if y else None
    m = re.search(r"(\d{1,2})\s*tháng", t)
    if "quý iv" in t and "năm" in t or re.search(r"\bnăm 20\d\d$", t.strip()) and "tháng" not in t and "quý" not in t:
        months = 12
    elif m:
        months = int(m.group(1))
    elif "quý iii" in t:
        months = 9
    elif "quý ii" in t:
        months = 6
    elif "quý i" in t:
        months = 3
    else:
        mm = re.search(r"tháng\s*(\d{1,2}|một|hai|ba|tư|năm|sáu|bảy|tám|chín|mười một|mười hai|mười)\b", t)
        words = {"một": 1, "hai": 2, "ba": 3, "tư": 4, "năm": 5, "sáu": 6, "bảy": 7, "tám": 8, "chín": 9, "mười": 10,
                 "mười một": 11, "mười hai": 12}
        months = (int(mm.group(1)) if mm and mm.group(1).isdigit() else words.get(mm.group(1)) if mm else None)
    return year, months


def extract(text, label):
    """Trong một báo cáo, tìm mọi câu '{label} … đạt N nghìn tỷ đồng … tăng G%'.
    Giá trị luỹ kế từ đầu năm luôn lớn nhất trong cùng báo cáo → lấy mức lớn nhất (và tăng trưởng đi kèm).
    Bỏ các câu so với năm 2019. Nếu không có câu nào có mức, lấy tăng trưởng của câu đầu tiên."""
    pat_level = re.compile(label + r"[^;.]{0,90}?(?:ước|ước tính)?\s*(?:tính\s*)?đạt\s*" + NUM + r"\s*nghìn tỷ đồng([^;.]{0,70}?)tăng\s*" + NUM + r"\s*%")
    best = None
    for m in pat_level.finditer(text):
        ctx = text[max(0, m.start() - 160): m.start()]
        if "2019" in ctx and "So với năm 2019" in ctx:
            continue
        lv, g = vn(m.group(1)), vn(m.group(3))
        if best is None or lv > best[0]:
            best = (lv, g)
    if best:
        return best
    pat_g = re.compile(label + r"[^;.]{0,60}?tăng\s*" + NUM + r"\s*%")
    for m in pat_g.finditer(text):
        ctx = text[max(0, m.start() - 160): m.end()]
        if "2019" in ctx:
            continue
        return None, vn(m.group(1))
    return None, None


def parse_nso():
    rows = []
    for line in open(PROC / "nso_reports.jsonl", encoding="utf-8"):
        r = json.loads(line)
        t = unicodedata.normalize("NFC", r["text"])
        title = unicodedata.normalize("NFC", r["title"])
        year, months = period(title)
        if not year or not months:
            continue
        lv_t, g_t = extract(t, r"doanh thu (?:du lịch )?lữ hành")
        lv_a, g_a = extract(t, r"(?:doanh thu )?dịch vụ lưu trú,? ăn uống")
        lv_r, g_r = extract(t, r"[Tt]ổng mức (?:hàng hóa )?bán lẻ hàng hóa và doanh thu dịch vụ tiêu dùng")
        # Giá trị tăng thêm (thực) của ngành lưu trú và ăn uống trong phần GDP: "dịch vụ lưu trú và ăn uống tăng X%, đóng góp Y"
        gva = re.search(r"(?:dịch vụ |ngành )?lưu trú và ăn uống([^;%]{0,90}?)" + NUM + r"\s*%([^;.]{0,40}?(?:đóng góp|làm giảm)\s*(?:\w+\s*)?" + NUM + ")?", t)
        if gva:
            sign = -1 if "giảm" in gva.group(1) else 1
            gva_g = vn(gva.group(2)) * sign
            gva_c = vn(gva.group(4)) * sign if gva.group(4) else None
        else:
            gva_g = gva_c = None
        if any(v is not None for v in (lv_t, g_t, lv_a, g_a, gva_g)):
            rows.append(dict(post=r["id"], date=r["date"], title=title, year=year, months=months, press="Thông cáo" in title,
                             travel_level=lv_t, travel_g=g_t, accom_level=lv_a, accom_g=g_a, retail_level=lv_r, retail_g=g_r,
                             gva_accom_g=gva_g, gva_accom_contrib=gva_c))
    df = pd.DataFrame(rows)
    # Ưu tiên bản có mức (đạt N nghìn tỷ) và bản công bố muộn nhất cho cùng kỳ
    # Mỗi kỳ (năm, số tháng luỹ kế): với từng chỉ tiêu, lấy giá trị ở báo cáo công bố muộn nhất có chỉ tiêu đó
    df = df.sort_values("date")
    cols = ["travel_level", "travel_g", "accom_level", "accom_g", "retail_level", "retail_g", "gva_accom_g", "gva_accom_contrib"]
    how = {c: ("max" if c.endswith("_level") else "last") for c in cols}   # mức luỹ kế: lớn nhất trong kỳ; tăng trưởng: bản muộn nhất
    agg = df.groupby(["year", "months"]).agg({**how, "date": "last", "post": lambda s: ",".join(map(str, s))}).reset_index()
    df = agg
    df.to_csv(RES / "b1_nso_extracted.csv", index=False)
    return df


def build_series(df):
    """Chuỗi năm (12 tháng) và luỹ kế 9 tháng; bổ khuyết mức thiếu bằng mức năm trước × (1 + tăng trưởng)."""
    ann = df[df.months == 12].set_index("year").sort_index()
    m9 = df[df.months == 9].set_index("year").sort_index()
    out = {}
    for key in ["travel", "accom"]:
        for tab, name in [(ann, "annual"), (m9, "ytd9")]:
            s = tab[f"{key}_level"].copy()
            g = tab[f"{key}_g"]
            for y in sorted(tab.index):
                if pd.isna(s.get(y)) and (y - 1) in s.index and not pd.isna(s.get(y - 1)) and not pd.isna(g.get(y)):
                    s.loc[y] = s.loc[y - 1] * (1 + g.loc[y] / 100)
            out[f"{key}_{name}"] = s
            out[f"{key}_{name}_g"] = g
    return out


def overview():
    df = parse_nso()
    df.to_csv(RES / "b1_nso_extracted.csv", index=False)
    ann = df[df.months == 12].set_index("year")
    m9 = df[df.months == 9].set_index("year")
    # 1) Chuỗi tăng trưởng thực của ngành lưu trú và ăn uống (GVA, giá so sánh) → chỉ số 2019 = 100
    g = ann.gva_accom_g.dropna().loc[2017:]
    idx = pd.Series({2019: 100.0})
    for y in range(2020, int(g.index.max()) + 1):
        idx.loc[y] = idx.loc[y - 1] * (1 + g.loc[y] / 100)
    for y in range(2018, int(g.index.min()) - 1, -1):
        idx.loc[y] = idx.loc[y + 1] / (1 + g.loc[y + 1] / 100)
    idx = idx.sort_index()
    g9_2026 = float(m9.gva_accom_g.get(2026)) if 2026 in m9.index else None
    # ngoại suy log-tuyến tính 2023–2025 tới 2030
    s = idx.loc[2023:]
    b, a0 = np.polyfit(s.index, np.log(s.values), 1)
    proj = pd.Series({y: float(np.exp(a0 + b * y)) for y in range(int(s.index.max()), 2031)})

    fig, axes = plt.subplots(1, 2, figsize=(11.5, 4))
    ax = axes[0]
    cols = [SERIES[7] if v < 0 else SERIES[0] for v in g.values]
    ax.bar(g.index, g.values, color=cols)
    for y, v in g.items():
        ax.text(y, v + (1.2 if v >= 0 else -3.5), f"{v:+.1f}", ha="center", fontsize=8)
    if g9_2026 is not None:
        ax.bar([2026], [g9_2026], color=SERIES[0], alpha=0.45)
        ax.text(2026, g9_2026 + 1.2, f"{g9_2026:+.1f} (9T)", ha="center", fontsize=8)
    ax.axhline(0, color=INK2, lw=0.8)
    ax.set_title("Tăng trưởng thực giá trị tăng thêm — ngành lưu trú và ăn uống (%)")
    ax.set_xticks(list(g.index) + [2026])
    ax = axes[1]
    ax.plot(idx.index, idx.values, marker="o", color=SERIES[0], lw=2, label="Thực tế")
    ax.plot(proj.index, proj.values, ls="--", color=SERIES[0], lw=1.6, label=f"Ngoại suy theo nhịp 2023–2025 ({(np.exp(b) - 1) * 100:.1f}%/năm)")
    ax.axhline(100, color=MUTED, lw=0.7)
    ax.axvspan(2019.6, 2021.9, color="#f3f2ee", zorder=0)
    ax.text(2020, idx.min() + 2, "COVID", fontsize=8, color=INK2)
    for y in [2021, 2025]:
        ax.text(y, idx.loc[y] + 3, f"{idx.loc[y]:.0f}", ha="center", fontsize=8)
    ax.set_title("Chỉ số giá trị tăng thêm thực (2019 = 100)")
    ax.legend(fontsize=8)
    f2 = savefig(fig, "b1_nso_growth.png", "Tốc độ tăng bóc từ thông cáo quý IV hằng năm (và quý III năm 2026); chỉ số = tích luỹ các tốc độ tăng. Bảng gốc: results/b1_nso_extracted.csv.")

    # 2) Doanh thu danh nghĩa (những kỳ công bố có số): 9 tháng đầu năm và cả năm
    # chỉ dùng từ 2017: báo cáo cũ hơn dùng định dạng khác, dễ bóc nhầm số của riêng một tháng
    tr9, ac9 = m9.travel_level.dropna().loc[2017:], m9.accom_level.dropna().loc[2017:]
    tra, aca = ann.travel_level.dropna().loc[2017:], ann.accom_level.dropna().loc[2017:]
    fig, axes = plt.subplots(1, 2, figsize=(11, 3.9))
    for ax, s9, sa, title, col in [(axes[0], tr9, tra, "Doanh thu du lịch lữ hành", SERIES[0]),
                                   (axes[1], ac9, aca, "Doanh thu dịch vụ lưu trú, ăn uống", SERIES[1])]:
        ax.bar([y - 0.2 for y in sa.index], sa.values, 0.38, color=col, label="cả năm")
        ax.bar([y + 0.2 for y in s9.index], s9.values, 0.38, color=col, alpha=0.5, label="9 tháng đầu năm")
        for y, v in list(sa.items()) + list(s9.items()):
            off = -0.2 if y in sa.index and v == sa.get(y) else 0.2
            ax.text(y + off, v, f"{v:,.1f}".replace(",", "#").replace(".", ",").replace("#", "."), ha="center", va="bottom", fontsize=7.5)
        ax.set_title(title + " (nghìn tỷ đồng)\nchỉ các kỳ có công bố mức, giá hiện hành")
        ax.legend(fontsize=8)
    f1 = savefig(fig, "b1_nso_revenue.png", "Các năm 2020–2021 và nhiều kỳ gần đây trên web chỉ đăng tốc độ tăng, không đăng mức doanh thu — để trống, không nội suy.")

    # 3) Ước lượng thị trường trực tuyến (thứ cấp) + khách nội địa
    est = pd.DataFrame([("e-Conomy SEA (GMV)", 2025, 4.0, 2030, 8.0), ("Mordor (doanh thu)", 2025, 2.87, 2031, 4.69),
                        ("IMARC (doanh thu)", 2025, 3.0, 2034, 6.4)], columns=["src", "y0", "v0", "y1", "v1"])
    est["cagr"] = ((est.v1 / est.v0) ** (1 / (est.y1 - est.y0)) - 1) * 100
    dom = pd.Series({2019: 85, 2022: 101.3, 2023: 108, 2025: 135.5})
    fig, axes = plt.subplots(1, 2, figsize=(11, 3.8), gridspec_kw={"width_ratios": [1.2, 1]})
    ax = axes[0]
    for i, r in est.iterrows():
        ax.plot([r.y0, r.y1], [r.v0, r.v1], marker="o", lw=2, color=SERIES[i], label=f"{r.src}: {r.cagr:.1f}%/năm")
    ax.set_title("Du lịch trực tuyến Việt Nam — 3 ước lượng (tỷ USD)")
    ax.legend(fontsize=8)
    ax = axes[1]
    ax.plot(dom.index, dom.values, marker="o", color=SERIES[0], lw=2)
    for y, v in dom.items():
        ax.text(y, v + 2.5, f"{v:g}".replace(".", ","), ha="center", fontsize=8)
    ax.axhline(160, color=SERIES[1], ls="--", lw=1)
    ax.text(2019.1, 162, "Mục tiêu 2030 (NQ 26): 160 triệu", fontsize=8, color=INK2)
    ax.set_ylim(70, 175)
    ax.set_title("Khách du lịch nội địa (triệu lượt)")
    f3 = savefig(fig, "b1_market_estimates.png", "Ước lượng thứ cấp: e-Conomy SEA 2025, Mordor, IMARC; khách nội địa: Cục Du lịch Quốc gia.")

    rec = float(idx.loc[int(g.index.max())])
    return {
        "n_reports": int(len(df)), "gva_accom_growth": {int(k): float(v) for k, v in g.items()}, "gva_accom_growth_9m_2026": g9_2026,
        "gva_index_2019_100": {int(k): round(float(v), 1) for k, v in idx.items()},
        "gva_recovery_last_vs_2019": round(rec, 1), "gva_cagr_2019_last": round(float((rec / 100) ** (1 / (int(g.index.max()) - 2019)) - 1) * 100, 1),
        "gva_trend_2023_2025_pct": round(float(np.exp(b) - 1) * 100, 1), "gva_index_2030_proj": round(float(proj.loc[2030]), 1),
        "travel_rev_9m": {int(k): float(v) for k, v in tr9.items()}, "accom_rev_9m": {int(k): float(v) for k, v in ac9.items()},
        "travel_rev_annual": {int(k): float(v) for k, v in tra.items()}, "accom_rev_annual": {int(k): float(v) for k, v in aca.items()},
        "travel_9m_growth_2019_2024_pct": round(float(tr9.loc[2024] / tr9.loc[2019] * 100 - 100), 1) if {2019, 2024} <= set(tr9.index) else None,
        "accom_9m_growth_2019_2024_pct": round(float(ac9.loc[2024] / ac9.loc[2019] * 100 - 100), 1) if {2019, 2024} <= set(ac9.index) else None,
        "online_estimates": est.round(2).to_dict("records"),
        "domestic_visitors": {int(k): v for k, v in dom.items()}, "domestic_cagr_2019_2025": round(float((135.5 / 85) ** (1 / 6) - 1) * 100, 1),
        "figs": [f2, f1, f3],
    }


if __name__ == "__main__":
    update_summary("b1_overview", overview())
    print("overview ok")
