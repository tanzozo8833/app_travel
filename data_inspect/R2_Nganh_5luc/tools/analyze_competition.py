"""B2 cạnh tranh hiện tại · B3 đối thủ mới gia nhập · B6 sản phẩm thay thế — từ dữ liệu cửa hàng ứng dụng và Google Trends.

Phương pháp:
- Độ tập trung: thị phần theo lượt cài (Google Play) và theo số đánh giá ở cửa hàng Việt Nam (App Store), HHI, CR3, CR5.
- Định vị: ma trận tính năng bóc từ mô tả app (từ điển song ngữ), chuẩn hoá, PCA, k-means (nhóm chiến lược),
  độ tương đồng cosine với vector mục tiêu của Levart, chỉ số "khoảng trống" (khoảng cách tới láng giềng gần nhất).
- Gia nhập: số app mới theo năm (cohort), tỉ lệ còn cập nhật, phân tích sống sót Kaplan–Meier (biến cố = ngừng cập nhật ≥ 2 năm),
  phân phối lượt cài (Lorenz, Gini, luật luỹ thừa rank–size), xác suất một app mới đạt ngưỡng lượt cài.
- Rút lui: đường quan tâm Google Trends của thương hiệu đã rút (Luxstay, Vntrip) so với đỉnh.
- Thay thế: quy mô (lượt cài, số đánh giá) của sản phẩm thay thế so với app du lịch; xu hướng tìm kiếm.
"""
import json
import re
from datetime import datetime

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from common import PROC, RES, SERIES, MUTED, INK2, style, savefig, update_summary
from fetch_data import CORE, core_ids

style()
TODAY = datetime(2026, 10, 26)

# Từ điển tính năng — mỗi trục một nhóm từ khoá (tiếng Việt + tiếng Anh). Đây là định nghĩa của nhóm (GT), kiểm lại được.
FEATURES = {
    "Đặt chỗ, giao dịch": r"đặt phòng|đặt vé|đặt chỗ|booking|book (?:hotel|flight)|khách sạn|vé máy bay|hotel|flight|thanh toán|payment",
    "Lên lịch trình": r"lịch trình|itinerar|trip plan|travel plan|lập kế hoạch|kế hoạch chuyến|plan your trip|planner",
    "Bản đồ, dẫn đường": r"bản đồ|\bmap|offline map|chỉ đường|dẫn đường|navigation|gps|định vị",
    "Cộng đồng, xã hội": r"cộng đồng|community|bạn bè|friends|follow|theo dõi|chia sẻ|share|social|mạng xã hội|nhóm",
    "Check-in, sưu tầm": r"check[- ]?in|đóng dấu|con dấu|stamp|huy hiệu|badge|sưu tầm|collect|passport|hộ chiếu|visited|đã đến|scratch|thành tựu|achievement",
    "Nhật ký, kỷ niệm": r"nhật ký|kỷ niệm|journal|diary|album|photo ?book|memories|lưu giữ|ký ức|travel log",
    "Trợ lý AI": r"\bai\b|trí tuệ nhân tạo|chatbot|gpt|artificial intelligence|trợ lý ảo|ai-powered",
    "Ưu đãi, khuyến mãi": r"ưu đãi|giảm giá|voucher|khuyến mãi|mã giảm|deal|coupon|discount|cashback|hoàn tiền|điểm thưởng|reward",
    "Tour, trải nghiệm": r"\btour|trải nghiệm|hoạt động|vé tham quan|activities|experience|attraction|vui chơi",
    "Đánh giá, review": r"đánh giá|review|nhận xét|rating|xếp hạng",
    "Di chuyển": r"xe khách|vé xe|thuê xe|taxi|đặt xe|\bbus\b|train|tàu hỏa|ride|car rental|transfer",
    "Nội dung địa phương": r"cẩm nang|guide|hướng dẫn|địa điểm|điểm đến|destination|khám phá|explore|ẩm thực|quán",
}
# Vector mục tiêu của Levart (GT — theo Business.md: hộ chiếu 34 tỉnh, cộng đồng, ưu đãi đối tác)
LEVART = {"Đặt chỗ, giao dịch": 0.2, "Lên lịch trình": 0.6, "Bản đồ, dẫn đường": 0.8, "Cộng đồng, xã hội": 0.8,
          "Check-in, sưu tầm": 1.0, "Nhật ký, kỷ niệm": 0.9, "Trợ lý AI": 0.5, "Ưu đãi, khuyến mãi": 0.6,
          "Tour, trải nghiệm": 0.4, "Đánh giá, review": 0.4, "Di chuyển": 0.3, "Nội dung địa phương": 0.8}

VI_CHARS = re.compile(r"[ăâđêôơưạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹ]", re.I)


def parse_vi_date(s):
    """'18 thg 8, 2014' → datetime."""
    if not isinstance(s, str):
        return pd.NaT
    m = re.match(r"(\d{1,2}) thg (\d{1,2}), (\d{4})", s)
    if m:
        return datetime(int(m[3]), int(m[2]), int(m[1]))
    try:
        return pd.to_datetime(s)
    except Exception:
        return pd.NaT


def load_play():
    p = pd.read_json(PROC / "play_apps.jsonl", lines=True)
    p["released_dt"] = p.released.map(parse_vi_date)
    p["updated_dt"] = pd.to_datetime(p.updated, unit="s", errors="coerce")
    p["released_dt"] = pd.to_datetime(p.released_dt, errors="coerce")
    p["updated_dt"] = p.updated_dt.fillna(p.released_dt)   # app mới chưa có bản cập nhật: lần cập nhật cuối = ngày ra mắt
    p["vi_listing"] = (p.description.fillna("") + " " + p.title.fillna("")).map(lambda t: len(VI_CHARS.findall(t)) >= 15)
    p["travel"] = p.genreId.isin(["TRAVEL_AND_LOCAL", "MAPS_AND_NAVIGATION"])
    p["real"] = p.realInstalls.fillna(p.minInstalls).astype(float)
    return p


def load_apple():
    a = pd.read_json(PROC / "appstore_apps.jsonl", lines=True)
    a = a.sort_values("userRatingCount", ascending=False).drop_duplicates("trackId")
    a["released_dt"] = pd.to_datetime(a.releaseDate, errors="coerce").dt.tz_localize(None)
    a["updated_dt"] = pd.to_datetime(a.currentVersionReleaseDate, errors="coerce").dt.tz_localize(None)
    a["travel"] = a.primaryGenreName.isin(["Travel", "Navigation", "Du lịch", "Điều hướng"])
    a["vi_listing"] = (a.description.fillna("") + " " + a.trackName.fillna("")).map(lambda t: len(VI_CHARS.findall(t)) >= 15)
    return a


def feature_matrix(texts):
    rows = []
    for t in texts:
        t = (t or "").lower()
        n = max(len(t.split()), 50)
        rows.append({k: len(re.findall(v, t)) / n * 100 for k, v in FEATURES.items()})
    X = pd.DataFrame(rows)
    # Chuẩn hoá về 0–1 theo phân vị 95 của mỗi trục (tránh một mô tả rất dài lấn át)
    cap = X.quantile(0.95).replace(0, 1)
    return (X / cap).clip(0, 1)


def hhi(shares):
    s = np.asarray(shares, float)
    s = s / s.sum() * 100
    return float((s ** 2).sum())


# ------------------------------------------------------------------------------------------------
def rivalry(p, a):
    core = pd.DataFrame(CORE, columns=["name", "play", "apple", "type"])
    ids = core_ids()
    (RES / "core_ids.json").write_text(json.dumps(ids, ensure_ascii=False, indent=1), encoding="utf-8")
    core["play"] = core.apply(lambda r: r.play or ids.get(r["name"]), axis=1)
    pc = core.merge(p[["appId", "title", "real", "minInstalls", "score", "ratings", "description", "updated_dt", "released_dt"]],
                    left_on="play", right_on="appId", how="left")
    # App Store: khớp theo id cố định, hoặc theo tên
    def apple_row(r):
        m = a[a.trackId == r.apple] if r.apple else a.iloc[0:0]   # chỉ khớp theo ID đã kiểm tay — không đoán theo tên
        return m.sort_values("userRatingCount", ascending=False).iloc[0] if len(m) else None
    apple_cnt, apple_desc = [], []
    for _, r in pc.iterrows():
        m = apple_row(r)
        apple_cnt.append(m.userRatingCount if m is not None else np.nan)
        apple_desc.append(m.description if m is not None else "")
    pc["vn_ratings_ios"] = apple_cnt
    pc["desc_all"] = pc.description.fillna("") + " " + pd.Series(apple_desc).fillna("")
    pc.drop(columns=["description"]).to_csv(RES / "b2_core_competitors.csv", index=False)

    # Độ tập trung trong nhóm đặt chỗ (OTA + trải nghiệm + chỗ ở chia sẻ)
    book = pc[pc.type.isin(["OTA", "Sàn trải nghiệm", "Chỗ ở chia sẻ", "OTA Việt", "Vé xe"])].copy()
    conc = {}
    for col, lab in [("vn_ratings_ios", "Số đánh giá App Store Việt Nam"), ("real", "Lượt cài Google Play (toàn cầu)")]:
        d = book[[ "name", col]].dropna()
        d = d[d[col] > 0].sort_values(col, ascending=False)
        sh = d[col] / d[col].sum() * 100
        conc[col] = {"label": lab, "HHI": round(hhi(d[col])), "CR3": round(float(sh.iloc[:3].sum()), 1),
                     "shares": dict(zip(d.name, sh.round(1)))}

    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    for ax, col, c in [(axes[0], "vn_ratings_ios", SERIES[0]), (axes[1], "real", SERIES[1])]:
        sh = pd.Series(conc[col]["shares"]).sort_values()
        ax.barh(sh.index, sh.values, color=c)
        for i, v in enumerate(sh.values):
            ax.text(v + 0.5, i, f"{v:.1f}%", va="center", fontsize=8)
        ax.set_title(f"{conc[col]['label']}\nHHI = {conc[col]['HHI']:,} · CR3 = {conc[col]['CR3']}%".replace(",", "."))
        ax.set_xlim(0, sh.max() * 1.2)
    f1 = savefig(fig, "b2_concentration.png",
                 "Nhóm đặt chỗ: OTA, sàn trải nghiệm, chỗ ở chia sẻ, OTA Việt, vé xe. Số đánh giá App Store là của cửa hàng Việt Nam; lượt cài Google Play là toàn cầu (không tách được Việt Nam). HHI > 2.500 = rất tập trung.")

    # Ma trận tính năng + PCA + k-means
    X = feature_matrix(pc.desc_all.tolist())
    X.index = pc.name
    X.loc["Levart (mục tiêu)"] = pd.Series(LEVART)
    Xc = X - X.mean()
    U, Sv, Vt = np.linalg.svd(Xc.values, full_matrices=False)
    pcs = Xc.values @ Vt[:3].T
    expl = (Sv ** 2 / (Sv ** 2).sum())[:3]
    from sklearn.cluster import KMeans
    km = KMeans(n_clusters=5, n_init=20, random_state=1).fit(X.drop(index="Levart (mục tiêu)").values)
    groups = list(km.labels_) + [-1]
    cos = (X.values @ X.loc["Levart (mục tiêu)"].values) / (np.linalg.norm(X.values, axis=1) * np.linalg.norm(X.loc["Levart (mục tiêu)"].values) + 1e-9)
    pos = pd.DataFrame({"name": X.index, "pc1": pcs[:, 0], "pc2": pcs[:, 1], "pc3": pcs[:, 2], "group": groups, "cos_levart": cos})
    pos = pos.merge(core[["name", "type"]], on="name", how="left").fillna({"type": "Levart"})
    pos.to_csv(RES / "b2_positioning.csv", index=False)
    X.to_csv(RES / "b2_feature_matrix.csv")
    load = pd.DataFrame(Vt[:3].T, index=X.columns, columns=["pc1", "pc2", "pc3"])
    load.to_csv(RES / "b2_pca_loadings.csv")

    # Khoảng trống: khoảng cách từ Levart tới app gần nhất so với khoảng cách láng giềng gần nhất trung vị
    D = np.linalg.norm(X.values[:, None, :] - X.values[None, :, :], axis=2)
    np.fill_diagonal(D, np.inf)
    nn = D.min(1)
    gap_ratio = float(nn[-1] / np.median(nn[:-1]))
    nearest = X.index[np.argsort(D[-1])[:3]].tolist()

    fig, ax = plt.subplots(figsize=(10, 6.5))
    gname = {}
    for gid in sorted(set(groups)):
        if gid < 0:
            continue
        members = pos[pos.group == gid]
        top = X.loc[members.name].mean().sort_values(ascending=False).index[:2]
        gname[gid] = f"Nhóm {gid + 1}: " + " + ".join(top)
    for gid in sorted(set(groups)):
        d = pos[pos.group == gid]
        lev = gid < 0
        ax.scatter(d.pc1, d.pc2, s=200 if lev else 60, color=SERIES[1] if lev else [SERIES[0], SERIES[2], SERIES[3], SERIES[6], SERIES[7]][gid % 5],
                   edgecolor="white", lw=1.5, label="Levart (mục tiêu)" if lev else gname[gid], zorder=3, marker="*" if lev else "o")
        for _, r in d.iterrows():
            ax.annotate(r["name"], (r.pc1, r.pc2), xytext=(5, 3), textcoords="offset points", fontsize=8,
                        fontweight="bold" if lev else None)
    sc = 1.6
    for f, (x, y) in load[["pc1", "pc2"]].iterrows():
        ax.annotate("", (x * sc, y * sc), (0, 0), arrowprops=dict(arrowstyle="->", color=MUTED, lw=0.8))
        ax.text(x * sc * 1.08, y * sc * 1.08, f, fontsize=7, color=INK2)
    ax.axhline(0, color=MUTED, lw=0.5)
    ax.axvline(0, color=MUTED, lw=0.5)
    ax.set_xlabel(f"PC1 ({expl[0] * 100:.0f}% phương sai)")
    ax.set_ylabel(f"PC2 ({expl[1] * 100:.0f}% phương sai)")
    ax.set_title("Bản đồ định vị theo tính năng mô tả trên cửa hàng ứng dụng (PCA, 12 trục)")
    ax.legend(fontsize=7.5, loc="lower right", title="Nhóm chiến lược (k-means) — 2 trục nổi bật", title_fontsize=7.5)
    f2 = savefig(fig, "b2_positioning_pca.png",
                 "Mỗi trục = mật độ từ khoá trong mô tả Google Play + App Store (từ điển trong analyze_competition.FEATURES). Mũi tên = hướng của từng trục. Vector Levart là mục tiêu (GT).")

    fig, ax = plt.subplots(figsize=(11, 7))
    M = X.copy()
    im = ax.imshow(M.values, cmap="Blues", aspect="auto", vmin=0, vmax=1)
    ax.set_xticks(range(M.shape[1]), M.columns, rotation=35, ha="right", fontsize=8.5)
    ax.set_yticks(range(M.shape[0]), M.index, fontsize=8.5)
    for i in range(M.shape[0]):
        for j in range(M.shape[1]):
            ax.text(j, i, f"{M.values[i, j]:.1f}", ha="center", va="center", fontsize=6.5, color="white" if M.values[i, j] > 0.6 else "#0b0b0b")
    ax.grid(False)
    ax.set_title("Ma trận tính năng (0 = không nhắc tới, 1 = nhắc nhiều nhất) — dòng cuối là Levart mục tiêu")
    f3 = savefig(fig, "b2_feature_heatmap.png", "Bóc tự động từ mô tả; chỉ phản ánh điều app tự giới thiệu, không phải tính năng đã kiểm tra.")

    stamp = X["Check-in, sưu tầm"].drop("Levart (mục tiêu)")
    return {
        "concentration": conc, "pca_explained": [round(float(x), 3) for x in expl],
        "kmeans_groups": {gname[g]: pos[pos.group == g].name.tolist() for g in sorted(set(groups)) if g >= 0},
        "levart_nearest": nearest, "levart_cos_top": pos.sort_values("cos_levart", ascending=False).iloc[1:6][["name", "cos_levart"]].round(3).to_dict("records"),
        "gap_ratio": round(gap_ratio, 2),
        "stamp_feature_top": stamp.sort_values(ascending=False).head(5).round(2).to_dict(),
        "figs": [f1, f2, f3],
    }, pc, X


def share_of_search():
    t = pd.read_csv(PROC / "trends_long.csv", parse_dates=["month"])
    t["scaled"] = t.raw / t.groupby("group").anchor_raw.transform("mean") * 100
    br = t[t.group.isin(["ota_global", "ota_local", "other_brands"])]
    br = br[~((br.kw == "agoda") & (br.group != "ota_global"))]
    piv = br.pivot_table(index="month", columns="kw", values="scaled")
    otas = [c for c in ["agoda", "traveloka", "booking.com", "trip.com", "klook", "mytour", "ivivu", "vntrip", "vexere", "airbnb"] if c in piv]
    sh = piv[otas].div(piv[otas].sum(1), axis=0) * 100
    ann = sh.groupby(sh.index.year).mean()
    hh = (sh ** 2).sum(1).groupby(sh.index.year).mean()
    # độ ổn định thứ hạng: tương quan hạng Spearman giữa các năm liên tiếp
    from scipy.stats import spearmanr
    rank_corr = {int(y): round(float(spearmanr(ann.loc[y - 1], ann.loc[y])[0]), 2) for y in ann.index[1:]}

    fig, axes = plt.subplots(1, 2, figsize=(11.5, 4.2), gridspec_kw={"width_ratios": [1.6, 1]})
    ax = axes[0]
    ax.stackplot(ann.index, ann.T.values, labels=otas, colors=(SERIES * 2)[:len(otas)], alpha=0.9)
    ax.set_ylim(0, 100)
    ax.set_xlim(ann.index.min(), ann.index.max())
    ax.set_title("Thị phần tìm kiếm giữa các thương hiệu đặt chỗ (%)")
    ax.legend(fontsize=7, ncol=5, loc="upper center", bbox_to_anchor=(0.5, -0.08))
    ax = axes[1]
    ax.plot(hh.index, hh.values, marker="o", color=SERIES[0])
    ax.axhline(2500, color=SERIES[7], ls="--", lw=1)
    ax.text(hh.index.min(), 2550, "ngưỡng rất tập trung (2.500)", fontsize=8, color=INK2)
    ax.set_title("HHI theo năm")
    f = savefig(fig, "b2_share_of_search.png", "Quy về thang chung qua từ mốc 'agoda' có mặt trong mọi nhóm tải. Thị phần = mức quan tâm của từng thương hiệu ÷ tổng.")
    return {"share_2019": ann.loc[2019].round(1).to_dict(), "share_2025": ann.loc[2025].round(1).to_dict(),
            "hhi_by_year": {int(k): round(float(v)) for k, v in hh.items()}, "rank_corr": rank_corr, "fig": f}, piv


def kaplan_meier(durations, events):
    d = pd.DataFrame({"t": durations, "e": events}).sort_values("t")
    times = np.sort(d.t[d.e == 1].unique())
    S, out = 1.0, [(0, 1.0)]
    n = len(d)
    for t in times:
        at_risk = (d.t >= t).sum()
        died = ((d.t == t) & (d.e == 1)).sum()
        S *= 1 - died / at_risk
        out.append((t, S))
    return pd.DataFrame(out, columns=["t", "S"])


def entrants(p, a, piv):
    pt = p[p.travel & p.released_dt.notna()].copy()
    pt["year"] = pt.released_dt.dt.year
    pt["age_y"] = (TODAY - pt.released_dt).dt.days / 365.25
    pt["since_update_y"] = (TODAY - pt.updated_dt).dt.days / 365.25
    pt["active"] = pt.since_update_y <= 1
    pt["dead"] = pt.since_update_y > 2
    at = a[a.travel & a.released_dt.notna()].copy()
    at["year"] = at.released_dt.dt.year
    at["since_update_y"] = (TODAY - at.updated_dt).dt.days / 365.25
    at["dead"] = at.since_update_y > 2

    coh = pd.DataFrame({"play_new": pt.groupby("year").size(), "play_new_vi": pt[pt.vi_listing].groupby("year").size(),
                        "ios_new": at.groupby("year").size()}).fillna(0).astype(int)
    coh = coh.loc[2010:2026]
    act = pt.groupby("year").active.mean() * 100

    fig, axes = plt.subplots(1, 2, figsize=(11.5, 4), gridspec_kw={"width_ratios": [1.4, 1]})
    ax = axes[0]
    w = 0.38
    ax.bar(coh.index - w / 2, coh.play_new, w, label="Google Play (hiển thị ở VN)", color=SERIES[0])
    ax.bar(coh.index + w / 2, coh.ios_new, w, label="App Store Việt Nam", color=SERIES[1])
    ax.set_title("Số app du lịch mới theo năm ra mắt (còn trên cửa hàng)")
    ax.legend(fontsize=8)
    ax.xaxis.set_major_locator(plt.MaxNLocator(integer=True))
    ax = axes[1]
    act_ios = (1 - at.groupby("year").apply(lambda d: (d.since_update_y > 1).mean())) * 100
    ax.plot(act.loc[2012:2025].index, act.loc[2012:2025].values, marker="o", ms=3, color=SERIES[0], label="Google Play")
    ax.plot(act_ios.loc[2012:2025].index, act_ios.loc[2012:2025].values, marker="o", ms=3, color=SERIES[1], label="App Store VN")
    ax.set_ylim(0, 105)
    ax.set_title("% còn cập nhật trong 12 tháng qua, theo năm ra mắt")
    ax.legend(fontsize=8)
    ax.xaxis.set_major_locator(plt.MaxNLocator(integer=True))
    f1 = savefig(fig, "b3_entry_cohorts.png", "Chỉ đếm app còn hiển thị khi tìm 44 từ khoá — app đã bị gỡ không còn trong dữ liệu (thiên lệch người sống sót), nên tỉ lệ thật còn thấp hơn.")

    # thời gian "sống" = từ ra mắt tới lần cập nhật cuối (nếu đã chết), hoặc tới hôm nay (nếu còn sống — kiểm duyệt)
    pt["life"] = np.where(pt.dead, (pt.updated_dt - pt.released_dt).dt.days / 365.25, pt.age_y).clip(min=0.01)
    km_all = kaplan_meier(pt.life, pt.dead.astype(int))
    km_vi = kaplan_meier(pt[pt.vi_listing].life, pt[pt.vi_listing].dead.astype(int))
    km_ot = kaplan_meier(pt[~pt.vi_listing].life, pt[~pt.vi_listing].dead.astype(int))
    at["age_y"] = (TODAY - at.released_dt).dt.days / 365.25
    at["life"] = np.where(at.dead, (at.updated_dt - at.released_dt).dt.days / 365.25, at.age_y).clip(min=0.01)
    km_ios = kaplan_meier(at.life, at.dead.astype(int))
    km_ios_vi = kaplan_meier(at[at.vi_listing].life, at[at.vi_listing].dead.astype(int))

    def med(km):
        below = km[km.S <= 0.5]
        return round(float(below.t.iloc[0]), 1) if len(below) else None

    def s_at(km, t):
        k = km[km.t <= t]
        return round(float(k.S.iloc[-1] * 100), 1) if len(k) else 100.0
    fig, ax = plt.subplots(figsize=(9, 4))
    for km, lab, c, ls in [(km_all, "Google Play — tất cả", SERIES[0], "-"), (km_vi, "Google Play — có tiếng Việt", SERIES[1], "-"),
                           (km_ios, "App Store VN — tất cả", SERIES[0], "--"), (km_ios_vi, "App Store VN — có tiếng Việt", SERIES[1], "--")]:
        ax.step(km.t, km.S * 100, where="post", label=f"{lab}: còn {s_at(km, 5)}% sau 5 năm", color=c, lw=2, ls=ls)
    ax.set_xlabel("Số năm kể từ khi ra mắt")
    ax.set_ylabel("% app còn được cập nhật")
    ax.set_xlim(0, 12)
    ax.set_ylim(0, 102)
    ax.set_title("Đường sống sót Kaplan–Meier của app du lịch (chết = ngừng cập nhật trên 2 năm)")
    ax.legend(fontsize=8)
    f2 = savefig(fig, "b3_survival.png", "Kaplan–Meier tự cài đặt; app còn cập nhật được coi là bị kiểm duyệt phải (chưa chết).")

    # Phân phối lượt cài: Lorenz/Gini + rank–size
    inst = np.sort(pt.real.clip(lower=1).values)[::-1]
    lor = np.cumsum(np.sort(inst)) / inst.sum()
    gini = 1 - 2 * np.trapz(lor, dx=1 / len(lor))
    rank = np.arange(1, len(inst) + 1)
    top = inst[: max(10, len(inst) // 4)]
    zipf = -np.polyfit(np.log(rank[: len(top)]), np.log(top), 1)[0]
    top1 = inst[: max(1, len(inst) // 100)].sum() / inst.sum() * 100
    top10 = inst[: max(1, len(inst) // 10)].sum() / inst.sum() * 100
    recent = pt[pt.year >= 2022]
    p100k = float((recent.real >= 1e5).mean() * 100)
    p1m = float((recent.real >= 1e6).mean() * 100)
    p100k_vi = float((recent[recent.vi_listing].real >= 1e5).mean() * 100) if recent.vi_listing.any() else None
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    ax = axes[0]
    ax.loglog(rank, inst, ".", color=SERIES[0], ms=4)
    ax.set_xlabel("Thứ hạng theo lượt cài (log)")
    ax.set_ylabel("Lượt cài (log)")
    ax.set_title(f"Quy luật thứ hạng – quy mô: số mũ Zipf ≈ {zipf:.2f}")
    ax = axes[1]
    ax.plot(np.linspace(0, 100, len(lor)), lor * 100, color=SERIES[1], lw=2)
    ax.plot([0, 100], [0, 100], color=MUTED, ls="--", lw=0.8)
    ax.set_title(f"Đường Lorenz lượt cài — Gini = {gini:.2f}")
    ax.set_xlabel("% app (xếp từ nhỏ tới lớn)")
    ax.set_ylabel("% tổng lượt cài")
    f3 = savefig(fig, "b3_installs_power_law.png", f"{len(inst)} app du lịch/bản đồ trên Google Play hiển thị ở Việt Nam; lượt cài là ước tính toàn cầu của Google Play.")

    # Rút lui: Luxstay, Vntrip, Mytour, iVIVU so với đỉnh
    ex = {}
    fig, ax = plt.subplots(figsize=(9.5, 3.8))
    for i, k in enumerate([c for c in ["luxstay", "vntrip", "mytour", "ivivu", "vexere"] if c in piv]):
        s = piv[k].rolling(6, min_periods=1).mean()
        ax.plot(s.index, s, lw=2, color=SERIES[i], label=k)
        pk = s.idxmax()
        ex[k] = {"peak": str(pk.date())[:7], "last_vs_peak_pct": round(float(s.iloc[-1] / s.max() * 100), 1)}
    ax.set_title("Mức quan tâm (trung bình trượt 6 tháng) — thương hiệu du lịch Việt")
    ax.legend(fontsize=8)
    f4 = savefig(fig, "b3_exit_trends.png", "Cùng thang với 'agoda' (= 100 trung bình nhóm OTA toàn cầu).")

    return {
        "n_play_travel": int(len(pt)), "n_ios_travel": int(len(at)),
        "cohorts": coh.to_dict("index"), "active_share_by_year": act.round(1).to_dict(),
        "active_share_by_year_ios": act_ios.loc[2012:2025].round(1).to_dict(),
        "share_dead_play": round(float(pt.dead.mean() * 100), 1), "share_dead_ios": round(float(at.dead.mean() * 100), 1),
        "km_median_years": {"play_all": med(km_all), "play_vi": med(km_vi), "ios_all": med(km_ios), "ios_vi": med(km_ios_vi)},
        "km_survival_5y": {"play_all": s_at(km_all, 5), "play_vi": s_at(km_vi, 5), "ios_all": s_at(km_ios, 5), "ios_vi": s_at(km_ios_vi, 5)},
        "km_survival_10y": {"play_all": s_at(km_all, 10), "ios_all": s_at(km_ios, 10), "ios_vi": s_at(km_ios_vi, 10)},
        "ios_cohorts_dead_share": (at.groupby("year").dead.mean() * 100).loc[2012:2024].round(1).to_dict(),
        "gini_installs": round(float(gini), 3), "zipf_exponent": round(float(zipf), 2),
        "top1pct_share": round(float(top1), 1), "top10pct_share": round(float(top10), 1),
        "p_new_reach_100k": round(p100k, 1), "p_new_reach_1m": round(p1m, 1), "p_new_vi_reach_100k": round(p100k_vi, 1) if p100k_vi is not None else None,
        "n_new_since_2022": int(len(recent)), "exits": ex, "figs": [f1, f2, f3, f4],
    }, pt


def substitutes(pc, piv):
    t = pd.read_csv(PROC / "trends_long.csv", parse_dates=["month"])
    s = t[t.group == "substitutes"].pivot(index="month", columns="kw", values="raw")
    ann = s.groupby(s.index.year).mean()
    sc = pc[["name", "type", "real", "vn_ratings_ios"]].copy()
    sc["nhóm"] = sc.type.map(lambda x: "Sản phẩm thay thế" if x in ["Bản đồ", "Trợ lý AI", "Mạng xã hội", "Siêu ứng dụng"] else
                             ("App lên kế hoạch, sưu tầm" if x in ["Lập lịch trình", "Sưu tầm, hành trình"] else "Đặt chỗ"))
    grp = sc.groupby("nhóm")[["real", "vn_ratings_ios"]].sum()
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    ax = axes[0]
    d = sc.dropna(subset=["real"]).sort_values("real")
    cols = {"Sản phẩm thay thế": SERIES[7], "App lên kế hoạch, sưu tầm": SERIES[2], "Đặt chỗ": SERIES[0]}
    ax.barh(d.name, d.real / 1e6, color=[cols[x] for x in d["nhóm"]])
    ax.set_xscale("log")
    ax.set_title("Lượt cài Google Play (triệu, log)")
    from matplotlib.patches import Patch
    ax.legend(handles=[Patch(color=v, label=k) for k, v in cols.items()], fontsize=7.5, loc="lower right")
    ax = axes[1]
    for i, k in enumerate([c for c in ["chatgpt", "google maps", "tour du lịch", "lịch trình", "du lịch"] if c in ann]):
        ax.plot(ann.index, ann[k], marker="o", ms=3, lw=2, color=SERIES[i], label=k)
    ax.set_title("Mức tìm kiếm trung bình năm (cùng một lượt tải)")
    ax.legend(fontsize=8)
    f = savefig(fig, "b6_substitutes_scale.png", "Trái: lượt cài là ước tính toàn cầu. Phải: Google Trends nhóm 'substitutes'.")
    ratio = float(grp.loc["Sản phẩm thay thế", "real"] / grp.loc["App lên kế hoạch, sưu tầm", "real"]) if "App lên kế hoạch, sưu tầm" in grp.index and grp.loc["App lên kế hoạch, sưu tầm", "real"] > 0 else None
    tour_change = float(ann.loc[2025, "tour du lịch"] / ann.loc[2019, "tour du lịch"] * 100 - 100) if "tour du lịch" in ann else None
    gpt_vs_plan = float(ann.loc[2025, "chatgpt"] / ann.loc[2025, "lịch trình"]) if "chatgpt" in ann and ann.loc[2025, "lịch trình"] > 0 else None
    return {"installs_by_group_m": (grp.real / 1e6).round(1).to_dict(), "ios_vn_ratings_by_group": grp.vn_ratings_ios.round(0).to_dict(),
            "substitute_to_planning_installs_ratio": round(ratio) if ratio else None,
            "tour_search_change_2019_2025_pct": round(tour_change, 1) if tour_change is not None else None,
            "chatgpt_vs_lichtrinh_2025": round(gpt_vs_plan, 1) if gpt_vs_plan else None,
            "annual": ann.round(1).to_dict(), "fig": f}


if __name__ == "__main__":
    p, a = load_play(), load_apple()
    r2, pc, X = rivalry(p, a)
    sos, piv = share_of_search()
    r2["share_of_search"] = {k: v for k, v in sos.items() if k != "fig"}
    r2["figs"].append(sos["fig"])
    update_summary("b2_rivalry", r2)
    r3, pt = entrants(p, a, piv)
    update_summary("b3_entrants", r3)
    r6 = substitutes(pc, piv)
    update_summary("b6_substitutes", {k: v for k, v in r6.items() if k != "fig"} | {"figs": [r6["fig"]]})
    print("competition ok")
