"""Sổ đăng ký nguồn dữ liệu — mọi biểu đồ, bảng, con số trong data_inspect trích về đây.

[Dx] = dữ liệu do fetch_data.py tải thẳng từ nguồn (có tệp gốc trong data/raw, kê trong _MANIFEST.csv).
[Sx] = số liệu thứ cấp nhập tay từ báo cáo/báo chí (đã dùng trong 7 tệp R1), dùng làm tham số mô hình.
[GT] = giả định của nhóm, không có nguồn — xem ASSUME trong analyze_market.py.

Chạy: python sources.py  → ghi data/raw/_MANIFEST.csv và SOURCES.md
"""
import csv
import hashlib
import re
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "data" / "raw"
ACCESSED = "25/10/2026"

# id: (tên ngắn, tổ chức, trang dữ liệu cho người đọc, điểm truy cập máy, ghi chú, mức bằng chứng)
SOURCES = {
    "D1": ("World Development Indicators", "World Bank",
           "https://databank.worldbank.org/source/world-development-indicators",
           "https://api.worldbank.org/v2/country/VNM;THA;IDN;PHL;MYS/indicator/{mã}?format=json&date=1990:2025",
           "Mã chỉ tiêu: NY.GDP.PCAP.PP.KD (GDP/người PPP, USD 2021 cố định), NY.GDP.PCAP.KD, NE.CON.PRVT.PC.KD "
           "(tiêu dùng hộ/người, USD 2015 cố định), FP.CPI.TOTL.ZG, IT.NET.USER.ZS, SP.URB.TOTL.IN.ZS, SP.POP.TOTL, "
           "ST.INT.ARVL, ST.INT.DPRT, ST.INT.RCPT.CD", "A"),
    "D2": ("Population Estimates and Projections", "World Bank (nguồn 40)",
           "https://databank.worldbank.org/source/population-estimates-and-projections",
           "https://api.worldbank.org/v2/country/VNM;THA;IDN;PHL;MYS/indicator/SP.POP.{tuổi}.{MA|FE}?source=40&date=1990:2050",
           "Dân số theo nhóm tuổi 5 năm (15–19 … 35–39), nam và nữ; ước lượng tới 2025, dự báo 2026–2050", "A"),
    "D3": ("World Economic Outlook (DataMapper)", "IMF",
           "https://www.imf.org/external/datamapper/",
           "https://www.imf.org/external/datamapper/api/v1/{mã}/VNM",
           "Mã: NGDP_RPCH (tăng trưởng GDP thực), PCPIPCH (lạm phát), NGDPDPC, PPPPC; bản WEO mới nhất tại ngày tải", "A"),
    "D4": ("StatCounter Global Stats — Viet Nam", "StatCounter",
           "https://gs.statcounter.com/os-market-share/mobile/viet-nam",
           "https://gs.statcounter.com/{os-market-share|vendor-market-share|social-media-stats}/chart.php?region_hidden=VN&granularity=monthly&csv=1",
           "Thị phần theo lượt truy cập web (không phải số máy): hệ điều hành di động, hãng, mạng xã hội; tháng 1/2012–9/2026", "B"),
    "D5": ("FRED — Jet Fuel Gulf Coast (DJFUELUSGULF), Brent (DCOILBRENTEU)", "Federal Reserve Bank of St. Louis / U.S. EIA",
           "https://fred.stlouisfed.org/series/DJFUELUSGULF",
           "https://fred.stlouisfed.org/graph/fredgraph.csv?id={DJFUELUSGULF|DCOILBRENTEU}",
           "Giá ngày, tính trung bình tháng; USD/gallon (nhiên liệu bay), USD/thùng (Brent)", "A"),
    "D6": ("Đơn vị hành chính cấp tỉnh (Việt Nam)", "Wikipedia tiếng Việt (bảng dẫn NQ 202/2025/QH15)",
           "https://vi.wikipedia.org/wiki/%C4%90%C6%A1n_v%E1%BB%8B_h%C3%A0nh_ch%C3%ADnh_c%E1%BA%A5p_t%E1%BB%89nh_(Vi%E1%BB%87t_Nam)",
           "https://vi.wikipedia.org/w/api.php?action=parse&prop=wikitext · action=query&prop=coordinates",
           "34 tỉnh thành: diện tích, dân số, vùng; toạ độ lấy từ thẻ toạ độ của bài Wikipedia từng tỉnh, điểm đến, tỉnh cũ", "A (dân số theo NQ 202) / B (toạ độ)"),
    "D7": ("Open-Meteo Geocoding API (dữ liệu GeoNames)", "Open-Meteo",
           "https://open-meteo.com/en/docs/geocoding-api",
           "https://geocoding-api.open-meteo.com/v1/search?name={tên}&countryCode=VN",
           "Toạ độ cho 8 điểm đến không có thẻ toạ độ trên Wikipedia; Mai Châu, Ninh Bình (Tràng An), Pù Luông và 5 vùng Google Trends nhập tay sau khi đối chiếu bản đồ", "B"),
    "D8": ("OSRM Table service (dữ liệu đường OpenStreetMap)", "Project OSRM",
           "https://project-osrm.org/",
           "https://router.project-osrm.org/table/v1/driving/{toạ độ}?sources=0&annotations=duration,distance",
           "Km đường bộ và giờ lái ô tô từ Hà Nội (21,0285; 105,8542) tới 22 điểm đến và 34 tỉnh", "B"),
    "D9": ("Google Trends — Việt Nam", "Google (truy cập qua thư viện pytrends 4.9.2)",
           "https://trends.google.com/trends/explore?geo=VN",
           "pytrends: interest_over_time (2016-01-01 → 2026-09-30), interest_by_region (2023-01-01 → 2026-09-30, 63 tỉnh cũ)",
           "Chỉ số tương đối 0–100 theo tháng; từ khoá điểm đến gộp có dấu + không dấu + viết liền (vd. 'Sa Pa + SaPa'); "
           "danh sách từ khoá trong fetch_data.TREND_GROUPS và DESTS", "B (chỉ số tương đối)"),
    "D10": ("Wikimedia Pageviews — vi.wikipedia", "Wikimedia Foundation",
            "https://pageviews.wmcloud.org/?project=vi.wikipedia.org",
            "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/vi.wikipedia/all-access/user/{bài}/monthly/2016010100/2026093000",
            "Lượt xem theo tháng (chỉ người dùng, không tính bot) của bài về 22 điểm đến và 34 tỉnh", "B"),
    "D11": ("Open-Meteo Historical Weather API (tái phân tích ERA5 / ERA5-Land, ECMWF Copernicus)", "Open-Meteo",
            "https://open-meteo.com/en/docs/historical-weather-api",
            "https://archive-api.open-meteo.com/v1/archive?latitude=…&longitude=…&start_date=2005-01-01&end_date=2025-12-31"
            "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum",
            "Dữ liệu ngày 2005–2025 tại toạ độ 22 điểm đến (~168.000 ngày-điểm)", "A"),
    "D12": ("OpenStreetMap qua Overpass API (© OpenStreetMap contributors, ODbL)", "OpenStreetMap",
            "https://www.openstreetmap.org/",
            "https://overpass-api.de/api/interpreter — đếm tourism=hotel|guest_house|hostel|motel|apartment|chalet|camp_site, "
            "tourism=attraction|viewpoint|museum, amenity=restaurant|cafe|fast_food trong bán kính 15 km",
            "Số cơ sở đã được vẽ lên bản đồ tính tới 10/2026 — thiếu nhiều homestay nhỏ", "B"),
    # ---- số liệu thứ cấp nhập tay ----
    "S1": ("Thông cáo báo chí tình hình giá tháng 9, quý III và 9 tháng 2026 (03/10/2026)", "Cục Thống kê, Bộ Tài chính",
           "https://www.nso.gov.vn/wp-content/uploads/2026/10/2.-Thong-cao-bao-chi-ve-tinh-hinh-gia-thang-9-quy-III-va-9-thang-nam-2026.pdf",
           "", "Giá dịch vụ vận tải hàng không 9T/2026 +7,34%; vận tải đường bộ ngoài xe buýt +8,06%", "A"),
    "S2": ("Thu nhập bình quân lao động 2025 (8,4 triệu; thành thị 10,1 triệu)", "Cục Thống kê, qua báo Nhân Dân",
           "https://nhandan.vn/dan-so-va-lao-dong-viec-lam-tiep-tuc-on-dinh-nam-2025-tao-da-cho-2026-post946357.html",
           "", "Dùng hiệu chỉnh trung vị thu nhập người đi làm trẻ ở Hà Nội (9 triệu — GT)", "A"),
    "S3": ("Students Behavior Report 2025 — phân nhóm chi tiêu tháng của sinh viên", "Cốc Cốc Research, qua SmartAds",
           "https://smartads.tech/blog/detail/students-behavior-report-2025-an-analysis-of-vietnamese-students-consumer-behavior-based-on-coc-coc-data-Fi4tFVdbdFNcWQ==.html",
           "", "<1 triệu 20% · 1–3 triệu 35% · 3–5 triệu 24% · >5 triệu 21% (phần còn lại)", "C"),
    "S4": ("Giá vé máy bay tiếp tục tăng mạnh trước cao điểm hè (25/05/2026)", "The Leader",
           "https://theleader.vn/gia-ve-may-bay-tiep-tuc-tang-manh-truoc-cao-diem-he-d45968.html",
           "", "Vé khứ hồi hè 2026: HN–Đà Nẵng 3,2–4 tr; HN–Phú Quốc 4,3–5 tr; HN–Nha Trang 4,7–5,4 tr; HN–TP.HCM 4,5–7 tr", "C"),
    "S5": ("Hà Nội đi Sa Pa năm 2026: so sánh tàu hoả, xe khách và chi phí", "vietnamtourism.com",
           "https://www.vietnamtourism.com/vi/ha-noi-i-sapa-nam-2026-so-sanh-tau-hoa-xe-khach-va-chi-phi",
           "", "Xe giường nằm HN–Sa Pa 270.000–370.000 đ/chiều → ~1.000 đ/km", "C"),
    "S6": ("Gen Z chi 3–5 triệu đồng/chuyến (Vietnam Report)", "Báo Văn hoá",
           "https://baovanhoa.vn/du-lich/the-he-gen-z-dang-lam-thay-doi-thi-truong-du-lich-toan-cau-186000.html",
           "", "Dùng cho phân phối chi tiêu/chuyến trong Monte Carlo", "C"),
    "S7": ("Phong cách du lịch nội địa của người Việt (n=603, 20–60 tuổi)", "Q&Me",
           "https://qandme.net/vi/baibaocao/Phong-cach-du-lich-noi-dia-cua-nguoi-Viet.html",
           "", "80% đi du lịch trong 12 tháng qua — dùng cho tỉ lệ tham gia", "B (cũ)"),
    "S8": ("Agoda Travel Outlook 2026 — Gen Z Việt Nam (22/04/2026)", "Agoda, qua Matterhorn Communications",
           "https://matterhorncommunications.com/agoda-vietnamese-gen-z-embrace-bite-sized-frequent-and-experience-first-travel-across-the-region/",
           "", "81% định đi tới 6 chuyến/năm; 49% cân nhắc điểm ít tiếng — dùng cho số chuyến/năm và để đối chiếu", "C"),
    "S9": ("Agoda Travel Outlook 2026 — 81% định dùng AI, 28% tin AI (10/02/2026)", "Agoda, qua Matterhorn Communications",
           "https://matterhorncommunications.com/81-of-vietnamese-travelers-want-to-use-ai-to-plan-their-next-trip-agoda-report-finds/",
           "", "Chỉ dùng để đối chiếu với kết quả ITS", "C"),
    "S10": ("Thị phần máy bán ra quý IV/2025 (Omdia): Apple 20%", "VnEconomy",
            "https://vneconomy.vn/cac-hang-trung-quoc-chiem-40-thi-phan-dien-thoai-tai-viet-nam.htm",
            "", "Chỉ dùng để đối chiếu với StatCounter", "B"),
}

# Biểu đồ → nguồn dữ liệu thật sự dùng để vẽ (common.savefig tự chèn dòng nguồn)
FIG_SOURCES = {
    "a1_cohorts.png": ["D2"],
    "a1_cohort_peers.png": ["D2"],
    "a2_market_decomposition.png": ["D1", "D2", "D3", "GT"],
    "a2_income_convergence.png": ["D1", "D2", "D3"],
    "a3_affordability.png": ["S2", "S3", "S4", "S5", "S1", "D8", "GT"],
    "a4_keywords_small_multiples.png": ["D9"],
    "a4_its_chatgpt.png": ["D9"],
    "a4_fuel_substitution.png": ["D9"],
    "a4_platform_attention.png": ["D9"],
    "a5_seasonality_heatmap.png": ["D9"],
    "a5_portfolio.png": ["D9"],
    "a6_climate_heatmaps.png": ["D11"],
    "a6_offseason_gap.png": ["D11", "D9"],
    "a7_gravity.png": ["D9", "D6", "D7"],
    "a8_supply_demand.png": ["D12", "D10"],
    "a9_mobile_os.png": ["D4"],
    "a10_fuel_shock.png": ["D5", "S1"],
    "a11_market_montecarlo.png": ["D6", "D2", "S6", "S7", "S8", "GT"],
    "a12_mcda.png": ["D8", "D9", "D11", "D12", "D10", "S3", "GT"],
}

# Tệp gốc → mã nguồn + cách dựng lại URL chính xác
RULES = [
    (r"^wb_(.+)\.json$", "D1", lambda m: f"https://api.worldbank.org/v2/country/VNM;THA;IDN;PHL;MYS/indicator/{m[1]}?format=json&per_page=2000&date=1990:2025"),
    (r"^wbpop_total40\.json$", "D2", lambda m: "https://api.worldbank.org/v2/country/VNM;THA;IDN;PHL;MYS/indicator/SP.POP.TOTL?format=json&per_page=2000&date=1990:2050&source=40"),
    (r"^wbpop_(.+)\.json$", "D2", lambda m: f"https://api.worldbank.org/v2/country/VNM;THA;IDN;PHL;MYS/indicator/{m[1]}?format=json&per_page=2000&date=1990:2050&source=40"),
    (r"^imf_(.+)\.json$", "D3", lambda m: f"https://www.imf.org/external/datamapper/api/v1/{m[1]}/VNM"),
    (r"^statcounter_os_mobile\.csv$", "D4", lambda m: "https://gs.statcounter.com/os-market-share/mobile/viet-nam (xuất CSV, 2012-01 → 2026-09)"),
    (r"^statcounter_vendor_mobile\.csv$", "D4", lambda m: "https://gs.statcounter.com/vendor-market-share/mobile/viet-nam (xuất CSV, 2012-01 → 2026-09)"),
    (r"^statcounter_social_all\.csv$", "D4", lambda m: "https://gs.statcounter.com/social-media-stats/all/viet-nam (xuất CSV, 2012-01 → 2026-09)"),
    (r"^fred_(.+)\.csv$", "D5", lambda m: f"https://fred.stlouisfed.org/graph/fredgraph.csv?id={m[1]}"),
    (r"^wiki_.+\.json$", "D6", lambda m: "https://vi.wikipedia.org/w/api.php?action=parse&page=Đơn vị hành chính cấp tỉnh (Việt Nam)&prop=wikitext"),
    (r"^wikicoords_.+\.json$", "D6", lambda m: "https://vi.wikipedia.org/w/api.php?action=query&prop=coordinates (lô ≤40 bài)"),
    (r"^geocode_(.+)\.json$", "D7", lambda m: f"https://geocoding-api.open-meteo.com/v1/search?countryCode=VN&name=… (điểm '{m[1]}')"),
    (r"^osrm_(.+)\.json$", "D8", lambda m: f"https://router.project-osrm.org/table/v1/driving/… (Hà Nội → {m[1]})"),
    (r"^trends_(.+)\.csv$", "D9", lambda m: f"Google Trends interest_over_time, geo=VN, 2016-01-01 2026-09-30, nhóm '{m[1]}'"),
    (r"^trendsreg_(.+)\.csv$", "D9", lambda m: f"Google Trends interest_by_region, geo=VN, 2023-01-01 2026-09-30, điểm '{m[1]}'"),
    (r"^pv_.+\.json$", "D10", lambda m: "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/vi.wikipedia/all-access/user/{bài}/monthly/2016010100/2026093000"),
    (r"^meteo_(.+)\.json$", "D11", lambda m: f"https://archive-api.open-meteo.com/v1/archive … 2005-01-01 → 2025-12-31 (điểm '{m[1]}')"),
    (r"^osm_(.+)\.json$", "D12", lambda m: f"https://overpass-api.de/api/interpreter — bán kính 15 km quanh '{m[1]}'"),
]


def cite(*ids, note=""):
    """Dòng chú thích nguồn cho biểu đồ: 'Nguồn: [D2] World Bank — … · tải 25/10/2026.'"""
    parts = []
    for i in ids:
        if i == "GT":
            parts.append("[GT] giả định của nhóm")
            continue
        name, org, url, *_ = SOURCES[i]
        host = re.sub(r"^https?://(www\.)?", "", url).split("/")[0]
        parts.append(f"[{i}] {org} — {name} ({host})")
    s = "Nguồn: " + "; ".join(parts) + f" · tải {ACCESSED}."
    return s + (" " + note if note else "")


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
        if fp.name.startswith("pv_"):
            try:
                import json
                url = url.replace("{bài}", json.loads(fp.read_text(encoding="utf-8"))["items"][0]["article"])
            except Exception:
                pass
        h = hashlib.sha256(fp.read_bytes()).hexdigest()[:16]
        rows.append(dict(file=f"data/raw/{fp.name}", source=sid, request=url, bytes=fp.stat().st_size,
                         downloaded=datetime.fromtimestamp(fp.stat().st_mtime).strftime("%Y-%m-%d %H:%M"), sha256_16=h))
    with open(RAW / "_MANIFEST.csv", "w", encoding="utf-8-sig", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0]))
        w.writeheader()
        w.writerows(rows)
    return rows


def write_sources_md(rows):
    from collections import Counter
    cnt = Counter(r["source"] for r in rows)
    L = ["# Nguồn dữ liệu — data_inspect", "",
         f"Tải ngày {ACCESSED}. Mỗi tệp gốc trong `data/raw/` có dòng tương ứng trong `data/raw/_MANIFEST.csv`: "
         "đường dẫn yêu cầu chính xác, thời điểm tải, dung lượng, mã băm SHA-256 (16 ký tự đầu) để kiểm tra tệp không bị sửa.", "",
         "## Dữ liệu tải trực tiếp [D]", "",
         "| Mã | Nguồn | Tổ chức | Trang dữ liệu | Điểm truy cập (API) | Nội dung | Mức | Số tệp gốc |", "|---|---|---|---|---|---|---|---|"]
    for k, (n, o, u, api, note, lv) in SOURCES.items():
        if k.startswith("D"):
            L.append(f"| **{k}** | {n} | {o} | <{u}> | `{api}` | {note} | {lv} | {cnt.get(k, 0)} |")
    L += ["", "## Số liệu thứ cấp nhập tay [S] — tham số mô hình", "",
          "| Mã | Tài liệu | Đơn vị phát hành | Link | Con số dùng | Mức |", "|---|---|---|---|---|---|"]
    for k, (n, o, u, api, note, lv) in SOURCES.items():
        if k.startswith("S"):
            L.append(f"| **{k}** | {n} | {o} | <{u}> | {note} | {lv} |")
    L += ["", "## [GT] Giả định của nhóm", "",
          "Không có nguồn. Liệt kê trong `tools/analyze_market.py` (biến `ASSUME`, hàm `market_size`) và `tools/analyze_space.py` "
          "(trọng số `w0`, công thức tiện nghi khí hậu). Phải thay bằng số đo từ khảo sát sơ cấp.", "",
          "## Bản quyền và điều khoản", "",
          "- World Bank, IMF: CC BY 4.0. · FRED: dữ liệu công khai của U.S. EIA. · Wikipedia/Wikimedia: CC BY-SA 4.0.",
          "- OpenStreetMap: © OpenStreetMap contributors, ODbL 1.0. · Open-Meteo: CC BY 4.0 (dữ liệu ERA5 © ECMWF/Copernicus).",
          "- StatCounter: được dùng lại khi ghi nguồn \"Source: StatCounter Global Stats\". · Google Trends: dữ liệu công khai của Google, ghi nguồn khi dùng.",
          ""]
    (ROOT / "SOURCES.md").write_text("\n".join(L), encoding="utf-8")


if __name__ == "__main__":
    rows = write_manifest()
    write_sources_md(rows)
    print("manifest:", len(rows), "tệp; không rõ nguồn:", [r["file"] for r in rows if r["source"] == "?"])
