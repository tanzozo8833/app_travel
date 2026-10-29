"""B5 — Quyền lực nhà cung cấp dịch vụ địa phương (chỗ ở) + mô hình thương lượng phí.

Phương pháp:
- Cơ cấu cung theo tỉnh từ OpenStreetMap: loại hình, tỉ lệ thuộc chuỗi/thương hiệu, HHI theo đơn vị vận hành
  (mỗi cơ sở độc lập là một "hãng"), chỉ số phân mảnh = 1 − HHI/10.000, tỉ lệ có hiện diện số (điện thoại, web, Facebook).
- Thương lượng Nash (Nash bargaining) giữa Levart và một chủ homestay về mức phí trên mỗi đêm phòng:
  bên homestay có phương án ngoài (khách có thể vẫn đến qua OTA với hoa hồng c), Levart có chi phí kéo khách.
  Dải phí khả thi [f_min, f_max] và nghiệm Nash f* theo sức thương lượng β.
"""
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from common import PROC, RES, SERIES, MUTED, INK2, style, savefig, update_summary

style()

CHAINS = r"mường thanh|vinpearl|melia|sheraton|novotel|ibis|accor|marriott|hilton|intercontinental|holiday inn|crowne|pullman|mercure|" \
         r"wyndham|radisson|fusion|flc|a25|sen việt|la vela|oyo|reddoorz|airbnb|best western|movenpick|mövenpick|sofitel|hyatt|" \
         r"four points|sun group|sun world|vinholidays|vin hotel|mgallery|amanoi|six senses|anantara|avani|alma|hoang yen|hoàng yến"

ASSUME = {   # tham số mô hình thương lượng (nguồn ghi bên cạnh)
    "adr_vnd": 974_000,          # giá/đêm trung bình chỗ ở ngắn hạn 2025 [S1]
    "occupancy": 0.4533,         # công suất 2025 [S1]
    "ota_commission": 0.18,      # giữa dải 15–25% [S2]
    "ota_share": 0.5885,         # tỉ lệ lượt đặt qua OTA [S2]
    "marginal_cost_share": 0.25, # chi phí biến đổi mỗi đêm phòng (dọn dẹp, điện nước) = 25% giá (GT)
    "levart_cost_vnd": 40_000,   # chi phí Levart kéo một đêm phòng (marketing, vận hành) (GT)
    "beta_levart": 0.3,          # sức thương lượng của Levart (0 = homestay quyết hết, 1 = Levart quyết hết) (GT)
}


def lodging():
    fp = PROC / "osm_lodging.csv.gz"
    if not fp.exists():
        return None
    d = pd.read_csv(fp)
    d["name_l"] = d.name.fillna("").str.lower()
    d["chain"] = d.brand.notna() | d.brand_wikidata.notna() | d.name_l.str.contains(CHAINS, regex=True)
    d["firm"] = np.where(d.chain, d.brand.fillna(d.operator).fillna(d.name_l.str.extract("(" + CHAINS + ")", expand=False)), "id_" + d.osm_id.astype(str))
    d["small"] = d.tourism.isin(["guest_house", "hostel", "motel", "camp_site", "chalet", "alpine_hut"])
    d["digital"] = d.phone | d.website | d.facebook
    rows = []
    for p, g in d.groupby("province"):
        sh = g.firm.value_counts(normalize=True) * 100
        rows.append(dict(province=p.replace("Tỉnh ", "").replace("Thành phố ", "TP. "), n=len(g), chain_share=g.chain.mean() * 100,
                         small_share=g.small.mean() * 100, digital_share=g.digital.mean() * 100,
                         hhi=float((sh ** 2).sum()), hotel=(g.tourism == "hotel").mean() * 100,
                         guest_house=(g.tourism == "guest_house").mean() * 100, hostel=(g.tourism == "hostel").mean() * 100,
                         apartment=(g.tourism == "apartment").mean() * 100, other=(~g.tourism.isin(["hotel", "guest_house", "hostel", "apartment"])).mean() * 100))
    t = pd.DataFrame(rows).sort_values("n", ascending=False)
    t["fragmentation"] = 1 - t.hhi / 10000
    t.to_csv(RES / "b5_lodging_by_province.csv", index=False)
    top_chains = d[d.chain].firm.str.lower().value_counts().head(12)

    fig, ax = plt.subplots(figsize=(11, max(4, len(t) * 0.26)))
    cols = ["hotel", "guest_house", "hostel", "apartment", "other"]
    labs = ["Khách sạn", "Nhà nghỉ, homestay (guest_house)", "Hostel", "Căn hộ", "Khác (motel, cắm trại…)"]
    left = np.zeros(len(t))
    for c, l, colr in zip(cols, labs, [SERIES[0], SERIES[1], SERIES[2], SERIES[3], SERIES[6]]):
        ax.barh(t.province, t[c], left=left, color=colr, label=l, height=0.7, edgecolor="white", lw=0.8)
        left += t[c].values
    for i, (_, r) in enumerate(t.iterrows()):
        ax.text(101, i, f"n={r.n:,} · chuỗi {r.chain_share:.1f}%".replace(",", "."), va="center", fontsize=7.5, color=INK2)
    ax.invert_yaxis()
    ax.set_xlim(0, 130)
    ax.set_xlabel("% cơ sở lưu trú")
    ax.set_title("Cơ cấu cơ sở lưu trú theo tỉnh (OpenStreetMap) — tỉ lệ thuộc chuỗi ghi bên phải")
    ax.legend(fontsize=7.5, loc="upper center", bbox_to_anchor=(0.45, -0.06), ncol=5)
    f1 = savefig(fig, "b5_lodging_structure.png", "Chuỗi = có thẻ brand/brand:wikidata hoặc tên khớp danh sách chuỗi (analyze_suppliers.CHAINS). OSM thiếu nhiều cơ sở nhỏ chưa được vẽ.")

    fig, axes = plt.subplots(1, 2, figsize=(12, 4.4), gridspec_kw={"width_ratios": [1.5, 1]})
    fig.subplots_adjust(wspace=0.35)
    ax = axes[0]
    ax.scatter(t.n, t.chain_share, s=40, color=SERIES[0])
    # chỉ ghi tên tỉnh lớn (≥ 300 cơ sở) và tỉnh ở hai đầu (chuỗi ≥ 4% hoặc = 0%) để nhãn không chồng nhau
    for _, r in t[(t.n >= 300) | (t.chain_share >= 4) | (t.chain_share == 0)].iterrows():
        ax.annotate(r.province, (r.n, r.chain_share), fontsize=7, xytext=(3, 2), textcoords="offset points")
    ax.set_xscale("log")
    ax.set_xlabel("Số cơ sở lưu trú trên OSM (log)")
    ax.set_ylabel("% thuộc chuỗi, thương hiệu")
    ax.set_title("Càng nhiều cơ sở, chuỗi càng không chiếm ưu thế?")
    ax = axes[1]
    ax.barh(top_chains.index[::-1], top_chains.values[::-1], color=SERIES[1])
    ax.tick_params(axis="y", labelsize=8)
    ax.set_title("Chuỗi, thương hiệu nhiều cơ sở nhất")
    f2 = savefig(fig, "b5_fragmentation.png", "Mỗi cơ sở độc lập được tính là một đơn vị vận hành riêng khi tính HHI.")
    nat_sh = d.firm.value_counts(normalize=True) * 100
    return {
        "n_lodging": int(len(d)), "n_provinces": int(t.shape[0]), "chain_share_pct": round(float(d.chain.mean() * 100), 2),
        "small_share_pct": round(float(d.small.mean() * 100), 1), "digital_share_pct": round(float(d.digital.mean() * 100), 1),
        "hhi_national": round(float((nat_sh ** 2).sum()), 2), "fragmentation_national": round(float(1 - (nat_sh ** 2).sum() / 10000), 4),
        "max_chain_share_province": t.sort_values("chain_share", ascending=False).head(3)[["province", "chain_share", "n"]].round(1).to_dict("records"),
        "top_chains": top_chains.to_dict(), "by_province": t.round(2).to_dict("records"), "figs": [f1, f2],
    }


def nash(adr, mc_share, ota_c, k, cost_l, beta):
    """Trả (f_min, f_max, f*) theo % giá phòng. k = xác suất đêm phòng đó vẫn đến qua OTA nếu không có Levart."""
    mc = mc_share * adr
    d_h = k * (adr * (1 - ota_c) - mc)           # phương án ngoài của homestay (kỳ vọng)
    f_min = cost_l / adr                          # Levart hoà vốn
    f_max = (adr - mc - d_h) / adr                # homestay không thiệt so với phương án ngoài
    surplus = adr - mc - d_h - cost_l
    f_star = (cost_l + beta * surplus) / adr if surplus > 0 else np.nan
    return f_min, f_max, f_star


def bargaining():
    A = ASSUME
    ks = np.linspace(0, 1, 101)
    res = np.array([nash(A["adr_vnd"], A["marginal_cost_share"], A["ota_commission"], k, A["levart_cost_vnd"], A["beta_levart"]) for k in ks])
    # k gắn với công suất: phòng trống càng nhiều, đêm đó càng ít khả năng đến qua OTA. Ước k ≈ công suất × tỉ lệ đặt qua OTA
    k0 = A["occupancy"] * A["ota_share"]
    f0 = nash(A["adr_vnd"], A["marginal_cost_share"], A["ota_commission"], k0, A["levart_cost_vnd"], A["beta_levart"])
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    ax = axes[0]
    ax.fill_between(ks * 100, res[:, 0] * 100, res[:, 1] * 100, color="#cde2fb", label="Dải phí cả hai cùng có lợi")
    ax.plot(ks * 100, res[:, 2] * 100, color=SERIES[0], lw=2, label=f"Nghiệm Nash (β Levart = {A['beta_levart']})")
    ax.axhspan(15, 25, color=SERIES[1], alpha=0.12, label="Hoa hồng OTA 15–25%")
    ax.axvline(k0 * 100, color=INK2, ls="--", lw=1)
    ax.text(k0 * 100 + 1, 85, f"k hiện tại ≈ {k0 * 100:.0f}%\n(công suất × tỉ lệ qua OTA)", fontsize=8, color=INK2)
    ax.set_xlabel("k = % đêm phòng vẫn sẽ đến qua OTA nếu không có Levart")
    ax.set_ylabel("Phí Levart (% giá phòng)")
    ax.set_ylim(0, 100)
    ax.set_title("Levart được thu bao nhiêu? (mô hình thương lượng Nash)")
    ax.legend(fontsize=7.5, loc="upper right")
    ax = axes[1]
    betas = np.linspace(0, 1, 11)
    fs = [nash(A["adr_vnd"], A["marginal_cost_share"], A["ota_commission"], k0, A["levart_cost_vnd"], b)[2] * 100 for b in betas]
    ax.plot(betas, fs, marker="o", color=SERIES[0])
    ax.axhspan(15, 25, color=SERIES[1], alpha=0.12)
    ax.set_xlabel("β — sức thương lượng của Levart")
    ax.set_ylabel("Phí Nash (% giá phòng)")
    ax.set_title(f"Ở k hiện tại: phí khả thi {f0[0] * 100:.0f}%–{f0[1] * 100:.0f}%")
    f = savefig(fig, "b5_bargaining.png", f"Giá phòng {A['adr_vnd']:,} đ và công suất {A['occupancy'] * 100:.0f}% [S1]; hoa hồng OTA và tỉ lệ đặt qua OTA [S2]; chi phí biên, chi phí Levart, β là giả định (GT).".replace(",", "."))
    return {"assume": A, "k_current": round(float(k0), 3), "f_min_pct": round(float(f0[0] * 100), 1), "f_max_pct": round(float(f0[1] * 100), 1),
            "f_nash_pct": round(float(f0[2] * 100), 1),
            "f_nash_by_beta": {round(float(b), 1): round(float(x), 1) for b, x in zip(betas, fs)}, "fig": f}


if __name__ == "__main__":
    out = {}
    lg = lodging()
    if lg:
        out.update(lg)
    bg = bargaining()
    out["bargaining"] = {k: v for k, v in bg.items() if k != "fig"}
    out["figs"] = (lg["figs"] if lg else []) + [bg["fig"]]
    update_summary("b5_suppliers", out)
    print("suppliers ok", "(có OSM)" if lg else "(chưa có OSM)")
