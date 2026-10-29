"""B4 — Quyền lực người dùng: đánh giá trên cửa hàng ứng dụng + hành vi săn giá trên Google.

Phương pháp: phân loại chủ đề bằng từ điển (song ngữ), tỉ lệ chủ đề theo nhóm sao, hệ số nâng (lift),
hồi quy logistic P(đánh giá 1–2 sao) theo chủ đề + hiệu ứng cố định app (tỉ số chênh, KTC 95%),
tỉ lệ nhà phát triển phản hồi, mạng nhắc tên đối thủ chéo (dấu hiệu so sánh, dùng nhiều app),
xu hướng tìm kiếm từ khoá săn giá (Mann–Kendall).
"""
import re

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import statsmodels.api as sm
from scipy import stats

from common import PROC, RES, SERIES, MUTED, INK2, style, savefig, update_summary

style()

TOPICS = {   # từ điển chủ đề (GT) — kiểm lại được trong results/b4_review_examples.csv
    "Giá, phí": r"giá|đắt|rẻ|phí|chênh|tăng giá|price|expensive|cheap|fee|overcharg",
    "Hoàn tiền, huỷ": r"hoàn tiền|hoàn lại|refund|huỷ|hủy|cancel|không hoàn",
    "Chăm sóc khách hàng": r"cskh|chăm sóc khách|tổng đài|hotline|hỗ trợ|support|liên hệ|customer service|phản hồi chậm|không ai trả lời",
    "Lỗi ứng dụng": r"lỗi|đơ|crash|không vào được|không mở|chậm|lag|bug|văng|treo|update|cập nhật|đăng nhập|login",
    "Khuyến mãi, điểm thưởng": r"mã giảm|voucher|khuyến mãi|ưu đãi|giảm giá|coupon|điểm thưởng|xu |point|cashback|deal",
    "Thanh toán": r"thanh toán|trừ tiền|thẻ|payment|charged|ví |chuyển khoản",
    "Đặt chỗ thất bại": r"không nhận phòng|không có phòng|overbook|hết phòng|không xác nhận|sai thông tin|không có vé|bị huỷ phòng",
    "Lừa đảo, tin cậy": r"lừa|scam|uy tín|fraud|không tin|gian lận",
    "Tiện, dễ dùng": r"tiện|dễ dùng|nhanh|tốt|tuyệt|ok|hài lòng|great|good|easy|love|excellent",
}
BRANDS = {"Agoda": r"agoda", "Traveloka": r"traveloka", "Booking.com": r"booking.com|booking com", "Trip.com": r"trip\.com|ctrip", "Klook": r"klook",
          "Airbnb": r"airbnb", "Vexere": r"vexere", "Google Maps": r"google map|gg map", "ChatGPT": r"chatgpt|chat gpt|gpt"}


def load():
    p = pd.read_csv(PROC / "play_reviews.csv.gz")
    p["store"] = "Google Play"
    a = pd.read_csv(PROC / "appstore_reviews.csv.gz")
    a["store"] = "App Store"
    a["replied"] = np.nan
    a["content"] = a.title.fillna("") + ". " + a.content.fillna("")
    df = pd.concat([p[["app", "store", "score", "at", "content", "replied", "repliedAt"]],
                    a[["app", "store", "score", "at", "content", "replied"]]], ignore_index=True)
    df["content"] = df.content.fillna("").astype(str)
    df["text"] = df.content.str.lower()
    df["neg"] = (df.score <= 2).astype(int)
    for k, v in TOPICS.items():
        df[k] = df.text.str.contains(v, regex=True).astype(int)
    df["at"] = pd.to_datetime(df["at"], errors="coerce", utc=True).dt.tz_localize(None)
    return df


def buyers():
    df = load()
    core_booking = ["Traveloka", "Agoda", "Booking.com", "Trip.com", "Klook", "Airbnb", "Vexere", "Mytour", "iVIVU", "Vietravel"]
    d = df[df.app.isin(core_booking)].copy()
    # mẫu ví dụ để kiểm từ điển
    ex = []
    for k in TOPICS:
        s = d[d[k] == 1].sample(min(5, int(d[k].sum())), random_state=1)[["app", "store", "score", "content"]]
        s["topic"] = k
        ex.append(s)
    pd.concat(ex).to_csv(RES / "b4_review_examples.csv", index=False)

    # Tỉ lệ chủ đề theo nhóm sao + lift
    grp = d.assign(nhom=np.where(d.score <= 2, "1–2 sao", np.where(d.score >= 4, "4–5 sao", "3 sao")))
    prev = grp.groupby("nhom")[list(TOPICS)].mean() * 100
    lift = (prev.loc["1–2 sao"] / prev.loc["4–5 sao"].replace(0, np.nan)).round(2)
    fig, ax = plt.subplots(figsize=(10, 4.6))
    ks = list(TOPICS)
    y = np.arange(len(ks))
    ax.barh(y + 0.2, prev.loc["1–2 sao", ks], 0.4, color=SERIES[7], label="Đánh giá 1–2 sao")
    ax.barh(y - 0.2, prev.loc["4–5 sao", ks], 0.4, color=SERIES[0], label="Đánh giá 4–5 sao")
    ax.set_yticks(y, ks)
    ax.invert_yaxis()
    ax.set_xlabel("% đánh giá có nhắc tới chủ đề")
    ax.set_title(f"Người dùng app đặt chỗ than phiền điều gì — {len(d):,} đánh giá gần nhất".replace(",", "."))
    ax.legend(fontsize=8, loc="lower right")
    f1 = savefig(fig, "b4_review_topics.png", "10 app đặt chỗ; tối đa 500 đánh giá mới nhất/app/cửa hàng. Từ điển chủ đề trong analyze_buyers.TOPICS; ví dụ kiểm tay: results/b4_review_examples.csv.")

    # Hồi quy logistic: P(1–2 sao) ~ chủ đề + hiệu ứng cố định app + cửa hàng
    X = pd.concat([d[ks], pd.get_dummies(d.app, prefix="app", drop_first=True, dtype=float),
                   (d.store == "App Store").astype(float).rename("App Store")], axis=1)
    X = sm.add_constant(X.astype(float))
    m = sm.Logit(d.neg, X).fit(disp=0, cov_type="HC1")
    orr = pd.DataFrame({"OR": np.exp(m.params[ks]), "lo": np.exp(m.conf_int().loc[ks, 0]), "hi": np.exp(m.conf_int().loc[ks, 1]),
                        "p": m.pvalues[ks]}).sort_values("OR")
    fig, ax = plt.subplots(figsize=(9, 4.4))
    ax.errorbar(orr.OR, range(len(orr)), xerr=[orr.OR - orr.lo, orr.hi - orr.OR], fmt="o", color=SERIES[0], ecolor=MUTED, capsize=3)
    ax.axvline(1, color=INK2, lw=0.8)
    ax.set_xscale("log")
    ax.set_yticks(range(len(orr)), orr.index)
    for i, (k, r) in enumerate(orr.iterrows()):
        ax.text(r.hi * 1.08, i, f"×{r.OR:.1f}", va="center", fontsize=8)
    ax.set_xlabel("Tỉ số chênh bị chấm 1–2 sao khi nhắc tới chủ đề (log)")
    ax.set_title("Điều gì kéo điểm xuống — hồi quy logistic có hiệu ứng cố định từng app")
    f2 = savefig(fig, "b4_rating_drivers.png", f"Logit, sai số vững HC1; n = {len(d):,}; pseudo-R² = {m.prsquared:.2f}.".replace(",", "."))

    # Phản hồi của nhà phát triển (chỉ Google Play có dữ liệu) + điểm
    pl = df[df.store == "Google Play"].copy()
    per = pl.groupby("app").agg(n=("score", "size"), mean=("score", "mean"), neg=("neg", "mean"), reply=("replied", "mean"))
    per = per[per.n >= 50].sort_values("reply")
    negreply = pl[pl.neg == 1].groupby("app").replied.mean()
    fig, axes = plt.subplots(1, 2, figsize=(11, 4.6), sharey=True)
    axes[0].barh(per.index, per.reply * 100, color=SERIES[0])
    axes[0].set_title("% đánh giá được nhà phát triển trả lời")
    axes[1].barh(per.index, per.neg * 100, color=SERIES[7])
    axes[1].set_title("% đánh giá 1–2 sao (500 đánh giá mới nhất)")
    f3 = savefig(fig, "b4_reply_rate.png", "Google Play, 500 đánh giá mới nhất mỗi app, tiếng Việt, cửa hàng Việt Nam.")

    # Nhắc tên đối thủ chéo
    cm = pd.DataFrame(0, index=core_booking, columns=list(BRANDS))
    for app in core_booking:
        t = d[d.app == app].text
        for b, pat in BRANDS.items():
            if b == app:
                continue
            cm.loc[app, b] = t.str.contains(pat, regex=True).sum()
    # chỉ đếm tên của app KHÁC (không tính tự nhắc tên mình)
    cross_share = float(d.apply(lambda r: any(re.search(p, r.text) for b, p in BRANDS.items() if b != r.app), axis=1).mean() * 100)
    switch = d.text.str.contains(r"chuyển sang|qua bên|app khác|bên khác|ứng dụng khác|gỡ app|xoá app|xóa app|không dùng nữa|bỏ app|switch to|uninstall", regex=True)
    switch_share = float(switch.mean() * 100)
    switch_neg = float(switch[d.neg == 1].mean() * 100)
    cm.to_csv(RES / "b4_cross_mentions.csv")

    # Săn giá trên Google
    t = pd.read_csv(PROC / "trends_long.csv", parse_dates=["month"])
    pr = t[t.group == "price_seeking"].pivot(index="month", columns="kw", values="raw")
    ratio = pr.drop(columns=["du lịch"]).sum(1) / pr["du lịch"].replace(0, np.nan)
    ann = ratio.groupby(ratio.index.year).mean()
    tau, pval = stats.kendalltau(ann.loc[2016:2025].index, ann.loc[2016:2025].values)
    fig, axes = plt.subplots(1, 2, figsize=(11, 3.8))
    ax = axes[0]
    for i, k in enumerate([c for c in pr.columns if c != "du lịch"]):
        ax.plot(pr.index, pr[k].rolling(6, min_periods=1).mean(), lw=1.8, color=SERIES[i], label=k)
    ax.set_title("Từ khoá săn giá (trung bình trượt 6 tháng)")
    ax.legend(fontsize=7.5)
    ax = axes[1]
    ax.plot(ann.index, ann.values, marker="o", color=SERIES[0])
    ax.set_title(f"Tổng từ khoá săn giá ÷ “du lịch” (Mann–Kendall τ = {tau:.2f}, p = {pval:.3f})")
    f4 = savefig(fig, "b4_price_seeking.png", "Google Trends nhóm 'price_seeking' — cùng một lượt tải nên so trực tiếp được.")

    return {
        "n_reviews_booking": int(len(d)), "n_reviews_all": int(len(df)),
        "prevalence": prev.round(1).to_dict("index"), "lift_neg_vs_pos": lift.to_dict(),
        "logit_or": orr.round(3).to_dict("index"), "logit_pseudo_r2": round(float(m.prsquared), 3),
        "logit_params": {k: round(float(v), 4) for k, v in m.params.items() if not k.startswith("app_")},
        "logit_app_fe": {k[4:]: round(float(v), 4) for k, v in m.params.items() if k.startswith("app_")},
        "per_app_play": per.round(3).to_dict("index"), "reply_rate_on_negative": negreply.round(3).to_dict(),
        "cross_mention_share_pct": round(cross_share, 1), "switch_mention_pct": round(switch_share, 1),
        "switch_mention_neg_pct": round(switch_neg, 1), "cross_mentions": cm.to_dict("index"),
        "price_ratio_annual": ann.round(3).to_dict(), "price_trend_tau": round(float(tau), 2), "price_trend_p": round(float(pval), 4),
        "neg_share_all": round(float(d.neg.mean() * 100), 1),
        "figs": [f1, f2, f3, f4],
    }


if __name__ == "__main__":
    update_summary("b4_buyers", buyers())
    print("buyers ok")
