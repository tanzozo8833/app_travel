"""Bước 1 — tải toàn bộ dữ liệu gốc về data/raw, chuẩn hoá vào data/processed.

Chạy: python tools/fetch_data.py [nguồn ...]
Nguồn: worldbank imf statcounter fuel provinces destinations osrm pageviews climate trends trends_region osm
Mọi lệnh tải đều có cache: chạy lại không gọi mạng nữa (xoá tệp trong data/raw để tải mới).
"""
import io
import re
import sys
import time

import pandas as pd

from common import PROC, RAW, get

# ----------------------------------------------------------------------------------------------
# Điểm đến chính — tên tìm kiếm, bài Wikipedia tiếng Việt, tỉnh mới (NQ 202/2025)
DESTS = [
    # key,          tên tìm kiếm,    bài vi.wikipedia,          tỉnh mới,     loại
    ("sapa",       "Sa Pa",        "Sa Pa",                    "Lào Cai",    "Núi phía Bắc"),
    ("hagiang",    "Hà Giang",     "Hà Giang",     "Tuyên Quang", "Núi phía Bắc"),
    ("mocchau",    "Mộc Châu",     "Mộc Châu",                 "Sơn La",     "Núi phía Bắc"),
    ("mucangchai", "Mù Cang Chải", "Mù Cang Chải",             "Lào Cai",    "Núi phía Bắc"),
    ("caobang",    "Cao Bằng",     "Thác Bản Giốc",            "Cao Bằng",   "Núi phía Bắc"),
    ("maichau",    "Mai Châu",     "Mai Châu",                 "Phú Thọ",    "Núi phía Bắc"),
    ("tamdao",     "Tam Đảo",      "Tam Đảo",                  "Phú Thọ",    "Núi phía Bắc"),
    ("puluong",    "Pù Luông",     "Khu bảo tồn thiên nhiên Pù Luông", "Thanh Hóa", "Núi phía Bắc"),
    ("ninhbinh",   "Ninh Bình",    "Quần thể danh thắng Tràng An",                 "Ninh Bình",  "Di sản ĐB Bắc Bộ"),
    ("halong",     "Hạ Long",      "Vịnh Hạ Long",             "Quảng Ninh", "Biển miền Bắc"),
    ("catba",      "Cát Bà",       "Quần đảo Cát Bà",                   "Hải Phòng",  "Biển miền Bắc"),
    ("samson",     "Sầm Sơn",      "Sầm Sơn",                  "Thanh Hóa",  "Biển miền Bắc"),
    ("phongnha",   "Phong Nha",    "Vườn quốc gia Phong Nha – Kẻ Bàng", "Quảng Trị", "Miền Trung"),
    ("hue",        "Huế",          "Huế",                      "Huế",        "Miền Trung"),
    ("danang",     "Đà Nẵng",      "Đà Nẵng",                  "Đà Nẵng",    "Biển miền Trung"),
    ("hoian",      "Hội An",       "Hội An",                   "Đà Nẵng",    "Biển miền Trung"),
    ("quynhon",    "Quy Nhơn",     "Quy Nhơn",                 "Gia Lai",    "Biển miền Trung"),
    ("nhatrang",   "Nha Trang",    "Nha Trang",                "Khánh Hòa",  "Biển miền Trung"),
    ("dalat",      "Đà Lạt",       "Đà Lạt",                   "Lâm Đồng",   "Cao nguyên"),
    ("phuquoc",    "Phú Quốc",     "Phú Quốc",                 "An Giang",   "Biển phía Nam"),
    ("vungtau",    "Vũng Tàu",     "Vũng Tàu",                 "TP. Hồ Chí Minh", "Biển phía Nam"),
    ("condao",     "Côn Đảo",      "Côn Đảo",                  "TP. Hồ Chí Minh", "Biển phía Nam"),
]
HANOI = (21.0285, 105.8542)


def unaccent(s):
    import unicodedata
    s = s.replace("Đ", "D").replace("đ", "d")
    return "".join(c for c in unicodedata.normalize("NFD", s) if unicodedata.category(c) != "Mn")


def query(name):
    """Từ khoá Google Trends gộp các cách gõ: có dấu, không dấu, viết liền (vd. 'Sa Pa + Sa Pa + Sapa')."""
    forms = [name, unaccent(name), unaccent(name).replace(" ", "")]
    return " + ".join(dict.fromkeys(forms))


def _h(s):
    import hashlib
    return hashlib.md5(s.encode('utf-8')).hexdigest()[:8]


# ----------------------------------------------------------------------------------------------
def worldbank():
    """WDI + dự báo dân số (nguồn 40) cho Việt Nam và 4 nước so sánh."""
    countries = "VNM;THA;IDN;PHL;MYS"
    wdi = {
        "NY.GDP.PCAP.PP.KD": "gdp_pc_ppp",          # GDP/người PPP, USD 2021 cố định
        "NY.GDP.PCAP.KD": "gdp_pc_const",            # GDP/người USD 2015 cố định
        "NE.CON.PRVT.PC.KD": "hh_cons_pc",           # tiêu dùng hộ/người, USD 2015 cố định
        "FP.CPI.TOTL.ZG": "cpi",
        "ST.INT.ARVL": "intl_arrivals",
        "ST.INT.DPRT": "outbound_departures",
        "ST.INT.RCPT.CD": "tourism_receipts_usd",
        "IT.NET.USER.ZS": "internet_pct",
        "SP.URB.TOTL.IN.ZS": "urban_pct",
        "SP.POP.TOTL": "pop_total",
    }
    rows = []
    for code, name in wdi.items():
        js = get(f"https://api.worldbank.org/v2/country/{countries}/indicator/{code}",
                 params={"format": "json", "per_page": 2000, "date": "1990:2025"}, cache=f"wb_{code}.json")
        for r in js[1] or []:
            rows.append((r["countryiso3code"], int(r["date"]), name, r["value"]))
    df = pd.DataFrame(rows, columns=["iso3", "year", "var", "value"]).dropna()
    df.pivot_table(index=["iso3", "year"], columns="var", values="value").reset_index() \
        .to_csv(PROC / "wb_macro.csv", index=False)

    # Dân số theo nhóm tuổi 5 năm, ước lượng + dự báo tới 2050 (Population estimates and projections)
    ages = ["1519", "2024", "2529", "3034", "3539"]
    rows = []
    for a in ages:
        for sex in ("MA", "FE"):
            code = f"SP.POP.{a}.{sex}"
            js = get(f"https://api.worldbank.org/v2/country/{countries}/indicator/{code}",
                     params={"format": "json", "per_page": 2000, "date": "1990:2050", "source": 40},
                     cache=f"wbpop_{code}.json")
            for r in js[1] or []:
                rows.append((r["countryiso3code"], int(r["date"]), a, sex, r["value"]))
    pop = pd.DataFrame(rows, columns=["iso3", "year", "age", "sex", "pop"]).dropna()
    pop.to_csv(PROC / "wb_pop_age.csv", index=False)
    tot = get(f"https://api.worldbank.org/v2/country/{countries}/indicator/SP.POP.TOTL",
              params={"format": "json", "per_page": 2000, "date": "1990:2050", "source": 40}, cache="wbpop_total40.json")
    pd.DataFrame([(r["countryiso3code"], int(r["date"]), r["value"]) for r in tot[1] if r["value"]],
                 columns=["iso3", "year", "pop_total"]).to_csv(PROC / "wb_pop_total_proj.csv", index=False)
    print("worldbank ok", len(df), len(pop))


def imf():
    """IMF WEO (DataMapper): tăng trưởng, lạm phát, GDP/người tới 2031."""
    out = []
    for code in ["NGDP_RPCH", "PCPIPCH", "NGDPDPC", "PPPPC"]:
        js = get(f"https://www.imf.org/external/datamapper/api/v1/{code}/VNM", cache=f"imf_{code}.json")
        for y, v in js["values"][code]["VNM"].items():
            out.append((int(y), code, v))
    pd.DataFrame(out, columns=["year", "var", "value"]).pivot(index="year", columns="var", values="value") \
        .reset_index().to_csv(PROC / "imf_weo_vnm.csv", index=False)
    print("imf ok")


def statcounter():
    """StatCounter: hệ điều hành di động, hãng điện thoại, mạng xã hội — theo tháng."""
    base = "https://gs.statcounter.com/{kind}/chart.php"
    specs = {
        "os_mobile": ("os-market-share", "Mobile", "os_combined", "Operating%20System", "201201"),
        "vendor_mobile": ("vendor-market-share", "Mobile", "vendor", "Device%20Vendor", "201201"),
        "social_all": ("social-media-stats", "Desktop%20%26%20Mobile%20%26%20Tablet%20%26%20Console",
                       "social_media", "Social%20Media", "201201"),
    }
    for name, (kind, dev, st, stn, frm) in specs.items():
        dh = "mobile" if dev == "Mobile" else "desktop+mobile+tablet+console"
        url = (f"https://gs.statcounter.com/{kind}/chart.php?device={dev}&device_hidden={dh}"
               f"&statType_hidden={st}&region_hidden=VN&granularity=monthly&statType={stn}&region=Viet%20Nam"
               f"&fromInt={frm}&toInt=202609&fromMonthYear={frm[:4]}-{frm[4:]}&toMonthYear=2026-09&csv=1")
        txt = get(url, cache=f"statcounter_{name}.csv", as_json=False)
        pd.read_csv(io.StringIO(txt)).to_csv(PROC / f"statcounter_{name}.csv", index=False)
    print("statcounter ok")


def fuel():
    """FRED: giá nhiên liệu bay Gulf Coast (USD/gallon) và dầu Brent (USD/thùng), ngày → tháng."""
    for sid in ["DJFUELUSGULF", "DCOILBRENTEU"]:
        txt = get(f"https://fred.stlouisfed.org/graph/fredgraph.csv?id={sid}", cache=f"fred_{sid}.csv", as_json=False)
        d = pd.read_csv(io.StringIO(txt), na_values=".")
        d.columns = ["date", "value"]
        d["date"] = pd.to_datetime(d["date"])
        d.set_index("date")["value"].resample("MS").mean().to_frame(sid).to_csv(PROC / f"fred_{sid}.csv")
    print("fuel ok")


def _wikitext(title):
    js = get("https://vi.wikipedia.org/w/api.php",
             params=dict(action="parse", page=title, prop="wikitext", format="json", redirects=1),
             cache=f"wiki_{re.sub(r'[^A-Za-z0-9]+', '_', title)}.json")
    return js["parse"]["wikitext"]["*"]


def _coords(titles):
    out = {}
    for i in range(0, len(titles), 40):
        chunk = titles[i:i + 40]
        js = get("https://vi.wikipedia.org/w/api.php",
                 params=dict(action="query", prop="coordinates", titles="|".join(chunk), format="json",
                             redirects=1, colimit="max"),
                 cache=f"wikicoords_{_h('|'.join(chunk))}.json")
        q = js["query"]
        back = {r["to"]: r["from"] for r in q.get("redirects", [])}
        norm = {n["to"]: n["from"] for n in q.get("normalized", [])}
        for p in q["pages"].values():
            if "coordinates" in p:
                t = p["title"]
                t = back.get(t, t)
                t = norm.get(t, t)
                out[t] = (p["coordinates"][0]["lat"], p["coordinates"][0]["lon"])
    return out


def provinces():
    """34 tỉnh thành (NQ 202/2025/QH15): diện tích, dân số, vùng, toạ độ."""
    t = _wikitext("Đơn vị hành chính cấp tỉnh (Việt Nam)")
    tab = t[t.find("{|"): t.find("|}", t.find("{|"))]
    rows = tab.split("\n|-")
    recs, region = [], None
    for r in rows:
        cells = [c.strip() for c in r.split("\n|") if c.strip()]
        cells = [c for c in cells if not c.startswith("!")]
        if not cells or not re.fullmatch(r"\d+", cells[0]):
            continue
        clean = []
        for c in cells:
            if c.startswith("rowspan"):
                region = re.sub(r"\[\[[^|\]]*\|?|\]\]|<br>|'''", " ", c.split("|", 1)[1]).strip()
                region = re.sub(r"\s+", " ", region)
                continue
            clean.append(c)
        name = re.search(r"\[\[([^|\]]+)", clean[1]).group(1)
        num = lambda s: float(s.replace(".", "").replace(",", "."))
        recs.append(dict(no=int(clean[0]), province=name, region=region, area_km2=num(clean[4]),
                         pop=num(clean[5])))
    df = pd.DataFrame(recs)
    # Toạ độ: lấy từ bài Wikipedia của tỉnh (thường là trung tâm hành chính)
    titles = [("Thành phố Hồ Chí Minh" if p == "Thành phố Hồ Chí Minh" else p) for p in df.province]
    co = _coords(titles)
    df["lat"] = [co.get(t, (None, None))[0] for t in titles]
    df["lon"] = [co.get(t, (None, None))[1] for t in titles]
    df.to_csv(PROC / "provinces34.csv", index=False)
    print("provinces ok", len(df), "thiếu toạ độ:", df.lat.isna().sum())


def destinations():
    co = _coords([d[2] for d in DESTS])
    df = pd.DataFrame(DESTS, columns=["key", "name", "wiki", "province", "type"])
    df["lat"] = [co.get(w, (None, None))[0] for w in df.wiki]
    df["lon"] = [co.get(w, (None, None))[1] for w in df.wiki]
    # Bài không có toạ độ → dịch vụ geocoding của Open-Meteo (dữ liệu GeoNames)
    for i, d in df[df.lat.isna()].iterrows():
        js = get("https://geocoding-api.open-meteo.com/v1/search",
                 params={"name": d["name"], "count": 10, "language": "vi", "countryCode": "VN"},
                 cache=f"geocode_{d.key}.json")
        res = js.get("results") or []
        if res:
            best = max(res, key=lambda r: r.get("population") or 0)
            df.loc[i, ["lat", "lon"]] = best["latitude"], best["longitude"]
    # Kiểm tay: geocoder trả nhầm sang địa danh trùng tên (đã đối chiếu bản đồ)
    MANUAL = {"maichau": (20.663, 105.083), "ninhbinh": (20.253, 105.917), "puluong": (20.437, 105.155)}
    for k, (la, lo) in MANUAL.items():
        df.loc[df.key == k, ["lat", "lon"]] = la, lo
    df.to_csv(PROC / "destinations.csv", index=False)
    print("destinations ok; thiếu toạ độ:", list(df[df.lat.isna()].name))


def osrm():
    """Khoảng cách, thời gian lái xe từ Hà Nội tới từng điểm đến và từng tỉnh (OSRM công khai)."""
    for name in ["destinations", "provinces34"]:
        df = pd.read_csv(PROC / f"{name}.csv")
        pts = [HANOI] + list(zip(df.lat, df.lon))
        coords = ";".join(f"{lon:.5f},{lat:.5f}" for lat, lon in pts)
        js = get(f"https://router.project-osrm.org/table/v1/driving/{coords}",
                 params={"sources": "0", "annotations": "duration,distance"}, cache=f"osrm_{name}.json")
        df["road_km"] = [d / 1000 if d is not None else None for d in js["distances"][0][1:]]
        df["drive_h"] = [d / 3600 if d is not None else None for d in js["durations"][0][1:]]
        df.to_csv(PROC / f"{name}.csv", index=False)
    print("osrm ok")


def pageviews():
    """Lượt xem bài Wikipedia tiếng Việt theo tháng (2016–2026): tín hiệu quan tâm có thật, công khai."""
    df = pd.read_csv(PROC / "destinations.csv")
    prov = pd.read_csv(PROC / "provinces34.csv")
    arts = list(df.wiki) + list(prov.province)
    out = []
    for a in dict.fromkeys(arts):
        slug = a.replace(" ", "_")
        try:
            js = get(f"https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/vi.wikipedia/all-access/user/"
                     f"{requests_quote(slug)}/monthly/2016010100/2026093000",
                     cache=f"pv_{re.sub(r'[^A-Za-z0-9]+', '_', slug)}_{_h(slug)}.json")
        except RuntimeError as e:
            print("  bỏ qua", a, e)
            continue
        for it in js["items"]:
            out.append((a, pd.to_datetime(it["timestamp"][:8]), it["views"]))
        time.sleep(0.1)
    pd.DataFrame(out, columns=["article", "month", "views"]).to_csv(PROC / "pageviews.csv", index=False)
    print("pageviews ok", len(out))


def requests_quote(s):
    from urllib.parse import quote
    return quote(s, safe="")


def climate():
    """Open-Meteo (ERA5): khí hậu ngày 2005–2025 cho từng điểm đến → khí hậu trung bình theo tháng."""
    df = pd.read_csv(PROC / "destinations.csv")
    allm = []
    for _, d in df.iterrows():
        js = get("https://archive-api.open-meteo.com/v1/archive",
                 params=dict(latitude=round(d.lat, 3), longitude=round(d.lon, 3), start_date="2005-01-01",
                             end_date="2025-12-31", daily="temperature_2m_max,temperature_2m_min,precipitation_sum",
                             timezone="Asia/Bangkok"),
                 cache=f"meteo_{d.key}.json")
        x = pd.DataFrame(js["daily"])
        x["time"] = pd.to_datetime(x["time"])
        x["key"] = d.key
        allm.append(x)
        if not (RAW / f"meteo_{d.key}.json").stat().st_mtime > time.time() - 5:
            continue
        time.sleep(25)   # giới hạn Open-Meteo: ~600 "lượt quy đổi"/phút, mỗi yêu cầu 21 năm ≈ 160 lượt
    pd.concat(allm).to_csv(PROC / "climate_daily.csv.gz", index=False, compression="gzip")
    print("climate ok")


# ----------------------------------------------------------------------------------------------
# Google Trends — cần nghỉ giữa các lượt để tránh bị chặn (HTTP 429)
TREND_GROUPS = {
    # nhóm: (từ khoá…) — mỗi nhóm ≤5 từ, có "du lịch" làm mốc chung để quy đổi giữa các nhóm
    "behaviour": ["du lịch", "du lịch tự túc", "homestay", "phượt", "review"],
    "motive": ["du lịch", "chữa lành", "cắm trại", "trekking", "du lịch một mình"],
    "planning": ["du lịch", "lịch trình", "chatgpt", "google maps", "gemini"],
    "transport": ["du lịch", "vé máy bay", "xe khách", "vé tàu", "thuê xe máy"],
    "booking": ["du lịch", "agoda", "traveloka", "booking", "klook"],
}


def _trends_client():
    from pytrends.request import TrendReq
    return TrendReq(hl="vi-VN", tz=-420, timeout=(10, 40))  # không dùng retries: lỗi với urllib3 2.x


def _trend_call(fn, cache):
    fp = RAW / cache
    if fp.exists():
        return pd.read_csv(fp, index_col=0)
    for i in range(6):
        try:
            df = fn()
            df.to_csv(fp)
            time.sleep(12)
            return df
        except Exception as e:
            print("   trends thử lại", i, repr(e)[:80])
            time.sleep(20 * (i + 1))
    raise RuntimeError("Google Trends từ chối " + cache)


def trends():
    p = _trends_client()
    tf = "2016-01-01 2026-09-30"
    frames = []
    for g, kws in TREND_GROUPS.items():
        def run(kws=kws):
            p.build_payload(kws, geo="VN", timeframe=tf)
            return p.interest_over_time().drop(columns="isPartial", errors="ignore")
        d = _trend_call(run, f"trends_{g}.csv")
        frames.append((g, d))
    # Điểm đến: mỗi lượt 4 điểm + neo Đà Lạt; từ khoá gộp có dấu/không dấu/viết liền
    anchor_q = query("Đà Lạt")
    names = [d[1] for d in DESTS if d[1] != "Đà Lạt"]
    q2name = {query(d[1]): d[1] for d in DESTS}
    for i in range(0, len(names), 4):
        kws = [anchor_q] + [query(n) for n in names[i:i + 4]]
        def run(kws=kws):
            p.build_payload(kws, geo="VN", timeframe=tf)
            return p.interest_over_time().drop(columns="isPartial", errors="ignore")
        d = _trend_call(run, f"trends_dest{i // 4}.csv")
        frames.append((f"dest{i // 4}", d.rename(columns=q2name)))
    # Quy đổi về cùng thang: mỗi nhóm chia cho mốc chung
    out = []
    for g, d in frames:
        anchor = "du lịch" if "du lịch" in d.columns else "Đà Lạt"
        for c in d.columns:
            out.append(pd.DataFrame({"month": pd.to_datetime(d.index), "group": g, "kw": c, "anchor": anchor,
                                     "raw": d[c].values, "anchor_raw": d[anchor].values}))
    pd.concat(out).to_csv(PROC / "trends_long.csv", index=False)
    print("trends ok")


def trends_region():
    """Mức quan tâm theo tỉnh (cũ, 63) cho từng điểm đến → ma trận nơi-tìm × nơi-đến cho mô hình trọng lực."""
    p = _trends_client()
    out = []
    for key, name, *_ in DESTS:
        def run(name=name):
            p.build_payload([query(name)], geo="VN", timeframe="2023-01-01 2026-09-30")
            return p.interest_by_region(resolution="REGION", inc_low_vol=True, inc_geo_code=True)
        d = _trend_call(run, f"trendsreg_{key}.csv")
        d = d.reset_index()
        d.columns = ["region", "geo", "value"]
        d["dest"] = key
        out.append(d)
    pd.concat(out).to_csv(PROC / "trends_region.csv", index=False)
    print("trends_region ok")


def region_coords():
    """Toạ độ các vùng Google Trends (63 tỉnh cũ) — lấy từ bài Wikipedia tên tỉnh."""
    reg = pd.read_csv(PROC / "trends_region.csv")[["region", "geo"]].drop_duplicates()
    fix = {"Ho Chi Minh City": "Thành phố Hồ Chí Minh", "Thua Thien Hue": "Huế", "Hanoi": "Hà Nội"}
    titles = []
    for r in reg.region:
        t = fix.get(r, r)
        t = t if t.startswith(("Thành phố", "Huế", "Hà Nội")) else (t if " " in t else t)
        titles.append(t)
    # Google trả tên tiếng Việt khi hl=vi-VN; thêm tiền tố "Tỉnh" cho tỉnh cũ đã sáp nhập
    co = _coords(titles)
    miss = [t for t in titles if t not in co]
    co2 = _coords([f"{t} (tỉnh)" for t in miss]) if miss else {}
    co3 = _coords([f"Tỉnh {t}" for t in miss]) if miss else {}
    lat, lon = [], []
    for t in titles:
        c = co.get(t) or co2.get(f"{t} (tỉnh)") or co3.get(f"Tỉnh {t}")
        lat.append(c[0] if c else None)
        lon.append(c[1] if c else None)
    reg["lat"], reg["lon"] = lat, lon
    # Tên vùng Google không khớp bài Wikipedia → toạ độ tỉnh lỵ cũ, nhập tay
    MANUAL = {"Hà Nam": (20.54, 105.92), "Hồ Chí Minh": (10.78, 106.70), "Thừa Thiên Huế": (16.46, 107.59),
              "tỉnh Kiên Giang": (10.01, 105.08), "Đăk Nông": (12.00, 107.69)}
    for k, (la, lo) in MANUAL.items():
        reg.loc[reg.region == k, ["lat", "lon"]] = la, lo
    reg.to_csv(PROC / "trends_regions_coords.csv", index=False)
    print("region coords ok; thiếu:", list(reg[reg.lat.isna()].region))


def osm():
    """OpenStreetMap: đếm cơ sở lưu trú, điểm tham quan quanh mỗi điểm đến (bán kính 15 km)."""
    df = pd.read_csv(PROC / "destinations.csv")
    servers = ["https://overpass-api.de/api/interpreter", "https://overpass.kumi.systems/api/interpreter",
               "https://overpass.private.coffee/api/interpreter"]
    rows = []
    for _, d in df.iterrows():
        q = f"""[out:json][timeout:120];
(nwr["tourism"~"hotel|guest_house|hostel|motel|apartment|chalet|camp_site"](around:15000,{d.lat},{d.lon}););out count;
(nwr["tourism"~"attraction|viewpoint|museum"](around:15000,{d.lat},{d.lon}););out count;
(nwr["amenity"~"restaurant|cafe|fast_food"](around:15000,{d.lat},{d.lon}););out count;"""
        js = None
        for s in servers:
            try:
                js = get(s, params={"data": q}, cache=f"osm_{d.key}.json", retries=2, sleep=5)
                break
            except RuntimeError:
                continue
        if js is None:
            print("  osm lỗi", d.key)
            continue
        c = [int(e["tags"]["total"]) for e in js["elements"]]
        rows.append(dict(key=d.key, lodging=c[0], attractions=c[1], food=c[2]))
        time.sleep(2)
    pd.DataFrame(rows).to_csv(PROC / "osm_supply.csv", index=False)
    print("osm ok", len(rows))


STEPS = ["worldbank", "imf", "statcounter", "fuel", "provinces", "destinations", "osrm", "pageviews",
         "climate", "trends", "trends_region", "region_coords", "osm"]

if __name__ == "__main__":
    todo = sys.argv[1:] or STEPS
    for s in todo:
        try:
            globals()[s]()
        except Exception as e:
            print("!!", s, "lỗi:", repr(e)[:300])
