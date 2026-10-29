"""Sổ đăng ký nguồn dữ liệu R2 — mọi biểu đồ, bảng, con số trong R2_Nganh_5luc trích về đây.

[Dx] = dữ liệu do fetch_data.py tải thẳng từ nguồn (tệp gốc trong data/raw, kê trong _MANIFEST.csv).
[Sx] = số liệu thứ cấp nhập tay (đã dùng trong 01_Nghien-cuu/R2_Nganh_5-luc), làm tham số mô hình.
[GT] = giả định của nhóm, không có nguồn.

Chạy: python sources.py → ghi data/raw/_MANIFEST.csv và SOURCES.md
"""
import csv
import hashlib
import re
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "data" / "raw"
ACCESSED = "26/10/2026"

SOURCES = {
    "D1": ("Báo cáo, thông cáo tình hình kinh tế – xã hội hằng tháng/quý/năm", "Cục Thống kê, Bộ Tài chính",
           "https://www.nso.gov.vn/",
           "https://www.nso.gov.vn/wp-json/wp/v2/posts?search=…  ·  /wp-json/wp/v2/posts/{id}",
           "Doanh thu du lịch lữ hành; doanh thu dịch vụ lưu trú, ăn uống (luỹ kế từ đầu năm); khách quốc tế — bóc từ văn bản báo cáo", "A"),
    "D2": ("Google Play — thông tin ứng dụng (cửa hàng Việt Nam)", "Google (truy cập qua google-play-scraper 1.2.7)",
           "https://play.google.com/store/apps?hl=vi&gl=VN",
           "google_play_scraper.search(term, lang='vi', country='vn', n_hits=30) · app(appId, lang='vi', country='vn')",
           "Lượt cài (ngưỡng và ước tính thực), điểm, số đánh giá, phân bố sao, ngày ra mắt, ngày cập nhật, mô tả, quảng cáo, mua trong app", "B"),
    "D3": ("Google Play — đánh giá người dùng (tiếng Việt, cửa hàng Việt Nam)", "Google (qua google-play-scraper 1.2.7)",
           "https://play.google.com/store/apps?hl=vi&gl=VN",
           "google_play_scraper.reviews(appId, lang='vi', country='vn', sort=NEWEST, count=500)",
           "Tối đa 500 đánh giá mới nhất mỗi app: số sao, nội dung, ngày, có/không phản hồi của nhà phát triển", "B"),
    "D4": ("Apple App Store — iTunes Search & Lookup API (cửa hàng Việt Nam)", "Apple",
           "https://performance-partners.apple.com/search-api",
           "https://itunes.apple.com/search?term=…&country=vn&entity=software&limit=200 · /lookup?id=…&country=vn",
           "Số đánh giá và điểm trung bình **của cửa hàng Việt Nam**, ngày ra mắt, ngày cập nhật, thể loại, mô tả", "B"),
    "D5": ("Apple App Store — Customer Reviews RSS (Việt Nam)", "Apple",
           "https://rss.applemarketingtools.com/",
           "https://itunes.apple.com/vn/rss/customerreviews/page={1..10}/id={trackId}/sortby=mostrecent/json",
           "Tối đa 500 đánh giá gần nhất mỗi app ở cửa hàng Việt Nam", "B"),
    "D6": ("Google Trends — Việt Nam", "Google (qua pytrends 4.9.2)", "https://trends.google.com/trends/explore?geo=VN",
           "pytrends interest_over_time, geo=VN, 2016-01-01 → 2026-09-30; nhóm từ khoá trong fetch_data.TREND_GROUPS",
           "Mức quan tâm tương đối theo tháng của thương hiệu OTA, sản phẩm thay thế, từ khoá săn giá, loại hình lưu trú", "B (chỉ số tương đối)"),
    "D7": ("OpenStreetMap qua Overpass API (© OpenStreetMap contributors, ODbL)", "OpenStreetMap",
           "https://www.openstreetmap.org/",
           "https://overpass-api.de/api/interpreter — ranh giới admin_level=4 ở Việt Nam; nwr(area)[tourism=hotel|guest_house|hostel|motel|apartment|chalet|camp_site|…]",
           "Cơ sở lưu trú đã được vẽ trên bản đồ theo tỉnh: loại hình, tên, thương hiệu (brand), đơn vị vận hành, liên hệ", "B (thiếu cơ sở chưa vẽ)"),
    "D8": ("Levart — data_inspect/R1_PESTLE (kết quả đã kiểm chứng nguồn)", "Nội bộ (nguồn gốc: World Bank, Wikipedia NQ 202/2025, Google Trends)",
           "", "R1_PESTLE/results/summary.json", "Dân số Hà Nội, số người 20–29 tuổi, thị phần sự chú ý OTA 2019–2025, độ co giãn chi trả", "A/B"),
    # ---- số liệu thứ cấp nhập tay ----
    "S1": ("Vietnam Short-Term Rental Market Review 2025", "Airbtics", "https://airbtics.com/short-term-rental-market-report-2025-vietnam",
           "", "Cung chỗ ở ngắn hạn +42,76%, doanh thu +14,52%, công suất 45,33%, giá/đêm ~974.000 đ (2025)", "C"),
    "S2": ("Chiến lược phân phối kênh đặt phòng: Mường Thanh và Vinpearl (12/05/2026)", "Đỗ Công Nguyên, Tạp chí Kinh tế Tài chính",
           "https://nghiencuu.tapchikinhtetaichinh.vn/chien-luoc-phan-phoi-kenh-dat-phong-cua-cac-chuoi-khach-san-viet-nam-nghien-cuu-truong-hop-muong-thanh-va-vinpearl-155966.html",
           "", "OTA 58,85% lượt đặt phòng 2025; hoa hồng 15–25%; chi phí kênh trực tiếp 4,25–4,5%", "C"),
    "S3": ("e-Conomy SEA 2025 — du lịch trực tuyến Việt Nam 4 tỷ USD (+16%), 8 tỷ USD năm 2030", "Google, Temasek, Bain (qua PhocusWire)",
           "https://www.phocuswire.com/vietnam-online-travel-market-double-2030", "", "Quy mô và tăng trưởng du lịch trực tuyến", "B"),
    "S4": ("Vietnam Online Travel Market 2031 (tóm tắt công khai)", "Mordor Intelligence (qua DayTrips Vietnam)",
           "https://daytripsvietnam.com/research/vietnam-online-travel-market-research/", "", "2,87 tỷ USD (2025) → 4,69 tỷ USD (2031); OTA ngoại ~80%", "C"),
    "S5": ("Khách du lịch nội địa và tổng thu 2019, 2022, 2023, 2025", "Cục Du lịch Quốc gia (qua Thông tin du lịch 12/2022; Thời báo Tài chính; Tuổi Trẻ)",
           "https://images.vietnamtourism.gov.vn/vn/dmdocuments/2023/ttdl2023.1.12.pdf", "", "85 / 101,3 / 108 / 135,5 triệu lượt; 495 / 672 / ~1.000 nghìn tỷ", "A"),
    "S6": ("Google Play — Service fees", "Google", "https://support.google.com/googleplay/android-developer/answer/112622", "",
           "15% cho 1 triệu USD đầu tiên mỗi năm, 30% phần trên", "A"),
    "S7": ("Google Maps Platform — Billing & pricing FAQ", "Google", "https://developers.google.com/maps/billing-and-pricing/faq", "",
           "Từ 01/03/2025: ngưỡng miễn phí theo SKU 10.000 / 5.000 / 1.000 lượt", "A"),
    "S8": ("Decision Lab Travel Rankings 2024 (YouGov BrandIndex)", "Decision Lab", "https://www.decisionlab.co/blog/vietnam-travel-landscape-2024", "",
           "Điểm cân nhắc: Traveloka 20,0; MoMo Travel 8,0; Shopee Travel 7,0", "B"),
    "S9": ("Klook: đặt chỗ qua creator +246% năm 2025", "Inc. Russia",
           "https://incrussia.ru/news/turisticheskaya-platforma-klook-privlekla-blogerov-k-prodvizheniyu-novyh-napravlenij-vo-vetname/", "",
           "Kênh creator — đối thủ mới", "C"),
}

FIG_SOURCES = {
    "b1_nso_revenue.png": ["D1"],
    "b1_nso_growth.png": ["D1"],
    "b1_market_estimates.png": ["S3", "S4", "S5", "D1"],
    "b2_concentration.png": ["D2", "D4"],
    "b2_share_of_search.png": ["D6"],
    "b2_positioning_pca.png": ["D2", "D4", "GT"],
    "b2_feature_heatmap.png": ["D2", "D4"],
    "b3_entry_cohorts.png": ["D2", "D4"],
    "b3_survival.png": ["D2", "D4"],
    "b3_installs_power_law.png": ["D2"],
    "b3_exit_trends.png": ["D6"],
    "b4_review_topics.png": ["D3", "D5"],
    "b4_rating_drivers.png": ["D3", "D5"],
    "b4_reply_rate.png": ["D3"],
    "b4_price_seeking.png": ["D6"],
    "b5_lodging_structure.png": ["D7"],
    "b5_fragmentation.png": ["D7", "S1"],
    "b5_bargaining.png": ["S1", "S2", "GT"],
    "b6_substitutes_scale.png": ["D2", "D4"],
    "b6_substitutes_trends.png": ["D6"],
    "b7_force_radar.png": ["D1", "D2", "D3", "D4", "D5", "D6", "D7", "GT"],
}

RULES = [
    (r"^nso_search_(.+)\.json$", "D1", lambda m: f"https://www.nso.gov.vn/wp-json/wp/v2/posts?search=… (truy vấn '{m[1]}')&per_page=100"),
    (r"^nso_post_(\d+)\.json$", "D1", lambda m: f"https://www.nso.gov.vn/wp-json/wp/v2/posts/{m[1]}?_fields=id,date,title,content"),
    (r"^play_search_core_(.+)\.json$", "D2", lambda m: f"google_play_scraper.search('{m[1]}', lang='vi', country='vn', n_hits=5)"),
    (r"^play_search_(.+)\.json$", "D2", lambda m: f"google_play_scraper.search('{m[1]}', lang='vi', country='vn', n_hits=30)"),
    (r"^play_app_(.+)\.json$", "D2", lambda m: f"https://play.google.com/store/apps/details?id={m[1]}&hl=vi&gl=VN"),
    (r"^play_reviews_(.+)\.json$", "D3", lambda m: f"google_play_scraper.reviews('{m[1]}', lang='vi', country='vn', sort=NEWEST, count=500)"),
    (r"^itunes_search_(.+)\.json$", "D4", lambda m: f"https://itunes.apple.com/search?term=… ('{m[1]}')&country=vn&entity=software&limit=200"),
    (r"^itunes_lookup_core\.json$", "D4", lambda m: "https://itunes.apple.com/lookup?id=…(đối thủ cốt lõi)&country=vn"),
    (r"^itunes_rss_(\d+)_(\d+)\.json$", "D5", lambda m: f"https://itunes.apple.com/vn/rss/customerreviews/page={m[2]}/id={m[1]}/sortby=mostrecent/json"),
    (r"^trends_(.+)\.csv$", "D6", lambda m: f"Google Trends interest_over_time geo=VN 2016-01-01 2026-09-30, nhóm '{m[1]}'"),
    (r"^osm_provinces\.json$", "D7", lambda m: "Overpass: rel(area VN)[boundary=administrative][admin_level=4]; out tags"),
    (r"^osm_lodging_(\d+)\.json$", "D7", lambda m: f"Overpass: nwr(area:{3600000000 + int(m[1])})[tourism~lưu trú]; out tags"),
    (r"^osm_boundary_(\d+)\.json$", "D7", lambda m: f"Overpass: rel({m[1]}); out geom (ranh giới tỉnh, dùng khi area trả 0 phần tử)"),
    (r"^osm_lodging_bbox_(\d+)\.json$", "D7", lambda m: f"Overpass: nwr[tourism~lưu trú](khung bao rel {m[1]}); out center tags → lọc điểm trong đa giác"),
]


def cite(*ids, note=""):
    parts = []
    for i in ids:
        if i == "GT":
            parts.append("[GT] giả định của nhóm")
            continue
        name, org, url, *_ = SOURCES[i]
        host = re.sub(r"^https?://(www\.)?", "", url).split("/")[0] if url else "nội bộ"
        parts.append(f"[{i}] {org} — {name} ({host})")
    return "Nguồn: " + "; ".join(parts) + f" · tải {ACCESSED}." + (" " + note if note else "")


def write_manifest():
    rows = []
    for fp in sorted(RAW.iterdir()):
        if fp.name.startswith("_") or not fp.is_file():
            continue
        sid, url = "?", ""
        for pat, s, f in RULES:
            m = re.match(pat, fp.name)
            if m:
                sid, url = s, f(m)
                break
        rows.append(dict(file=f"data/raw/{fp.name}", source=sid, request=url, bytes=fp.stat().st_size,
                         downloaded=datetime.fromtimestamp(fp.stat().st_mtime).strftime("%Y-%m-%d %H:%M"),
                         sha256_16=hashlib.sha256(fp.read_bytes()).hexdigest()[:16]))
    with open(RAW / "_MANIFEST.csv", "w", encoding="utf-8-sig", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0]))
        w.writeheader()
        w.writerows(rows)
    return rows


def write_sources_md(rows):
    from collections import Counter
    cnt = Counter(r["source"] for r in rows)
    L = ["# Nguồn dữ liệu — data_inspect/R2_Nganh_5luc", "",
         f"Tải ngày {ACCESSED}. Mỗi tệp gốc trong `data/raw/` có dòng trong `data/raw/_MANIFEST.csv` (yêu cầu chính xác, "
         "thời điểm tải, dung lượng, SHA-256 16 ký tự đầu). Lần tải mới ghi thêm vào `data/raw/_requests_log.csv`.", "",
         "## Dữ liệu tải trực tiếp [D]", "",
         "| Mã | Nguồn | Tổ chức | Trang dữ liệu | Điểm truy cập | Nội dung | Mức | Số tệp gốc |", "|---|---|---|---|---|---|---|---|"]
    for k, (n, o, u, api, note, lv) in SOURCES.items():
        if k.startswith("D"):
            L.append(f"| **{k}** | {n} | {o} | {('<' + u + '>') if u else '—'} | `{api}` | {note} | {lv} | {cnt.get(k, 0)} |")
    L += ["", "## Số liệu thứ cấp nhập tay [S]", "", "| Mã | Tài liệu | Đơn vị | Link | Con số dùng | Mức |", "|---|---|---|---|---|---|"]
    for k, (n, o, u, api, note, lv) in SOURCES.items():
        if k.startswith("S"):
            L.append(f"| **{k}** | {n} | {o} | <{u}> | {note} | {lv} |")
    L += ["", "## [GT] Giả định của nhóm", "", "Không có nguồn. Liệt kê trong `tools/analyze_*.py` (biến `ASSUME`, từ điển tính năng, "
          "từ điển chủ đề đánh giá, ngưỡng chấm điểm lực). Phải thay bằng số đo từ phỏng vấn, khảo sát.", "",
          "## Điều khoản", "", "- Google Play, App Store: dữ liệu công khai trên trang cửa hàng; dùng cho nghiên cứu, không phân phối lại nội dung đánh giá nguyên văn quy mô lớn.",
          "- Google Trends: dữ liệu công khai, ghi nguồn. · OpenStreetMap: ODbL 1.0. · Cục Thống kê: số liệu công bố chính thức.", ""]
    (ROOT / "SOURCES.md").write_text("\n".join(L), encoding="utf-8")


if __name__ == "__main__":
    rows = write_manifest()
    write_sources_md(rows)
    print("manifest:", len(rows), "tệp; không rõ nguồn:", [r["file"] for r in rows if r["source"] == "?"])
