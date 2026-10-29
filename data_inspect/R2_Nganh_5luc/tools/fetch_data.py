"""R2 — Bước 1: tải dữ liệu gốc cho Tổng quan ngành + 5 lực cạnh tranh.

Chạy: python tools/fetch_data.py [bước ...]
Bước: nso play_universe play_details play_reviews appstore appstore_reviews trends osm_provinces osm_lodging
Mọi lệnh tải đều có cache trong data/raw (xoá tệp để tải lại).
"""
import json
import re
import sys
import time
from datetime import datetime

import numpy as np
import pandas as pd
import requests

from common import PROC, RAW, UA, get

# ----------------------------------------------------------------------------------------------
# Danh sách đối thủ cốt lõi — 8 loại hình của PLAN_RESEARCH 5.1 (+ siêu ứng dụng, MXH làm sản phẩm thay thế)
# ID kiểm tay 26/10/2026: tra Google Play (play.google.com/store/search) và iTunes Search API (country=vn), đối chiếu tên nhà phát triển.
CORE = [
    # (tên, Play appId, App Store id (VN) hoặc None, loại hình)
    ("Traveloka", "com.traveloka.android", 898244857, "OTA"),
    ("Agoda", "com.agoda.mobile.consumer", 440676901, "OTA"),
    ("Booking.com", "com.booking", 367003839, "OTA"),
    ("Trip.com", "ctrip.english", 681752345, "OTA"),
    ("Klook", "com.klook", 961850126, "Sàn trải nghiệm"),
    ("Airbnb", "com.airbnb.android", 401626263, "Chỗ ở chia sẻ"),
    ("Tripadvisor", "com.tripadvisor.tripadvisor", 284876795, "Đánh giá, nội dung"),
    ("Vexere", "com.vexere.vexere", 1183279479, "Vé xe"),
    ("Mytour", "vn.mytour.apps.android", 6599837646, "OTA Việt"),
    ("iVIVU", "iVIVU.com", 1464844301, "OTA Việt"),
    ("Vietravel", "vn.com.vietravel", 623123001, "Lữ hành Việt"),
    ("Google Maps", "com.google.android.apps.maps", 585027354, "Bản đồ"),
    ("ChatGPT", "com.openai.chatgpt", 6448311069, "Trợ lý AI"),
    ("Gemini", "com.google.android.apps.bard", 6477489729, "Trợ lý AI"),
    ("Wanderlog", "com.wanderlog.android", 1476732439, "Lập lịch trình"),
    ("Polarsteps", "com.polarsteps", 947925763, "Sưu tầm, hành trình"),
    ("Been", "visited.countries.tracker.been.places.map", None, "Sưu tầm, hành trình"),
    ("Swarm", "com.foursquare.robin", 870161082, "Sưu tầm, hành trình"),
    ("AllTrails", "com.alltrails.alltrails", 405075943, "Bản đồ"),
    ("TikTok", "com.ss.android.ugc.trill", 1235601864, "Mạng xã hội"),
    ("Facebook", "com.facebook.katana", 284882215, "Mạng xã hội"),
    ("MoMo", "com.mservice.momotransfer", 918751511, "Siêu ứng dụng"),
    ("Grab", "com.grabtaxi.passenger", 647268330, "Siêu ứng dụng"),
]

# Từ khoá dựng "vũ trụ" app du lịch hiển thị ở Việt Nam (Google Play gl=vn và App Store vn)
UNIVERSE_TERMS = [
    "du lịch", "du lịch việt nam", "đặt phòng khách sạn", "homestay", "vé máy bay", "vé xe khách", "vé tàu",
    "lịch trình du lịch", "lập kế hoạch du lịch", "check in du lịch", "bản đồ du lịch", "hộ chiếu du lịch",
    "sưu tầm địa điểm", "nhật ký du lịch", "cẩm nang du lịch", "review du lịch", "tour du lịch", "thuê xe máy",
    "đi phượt", "khám phá việt nam", "tìm bạn đồng hành du lịch", "cắm trại", "trekking", "du lịch tự túc",
    "trip planner", "travel planner", "travel journal", "travel diary", "visited countries map", "travel passport stamps",
    "travel map tracker", "scratch map", "travel social network", "AI travel planner", "travel vietnam", "vietnam travel guide",
    "hostel booking", "local experiences", "travel deals", "đà lạt", "sapa", "hà giang", "phú quốc", "hạ long",
]


def _play():
    import google_play_scraper as g
    return g


def _cache_json(name, fn):
    fp = RAW / name
    if fp.exists():
        return json.loads(fp.read_text(encoding="utf-8"))
    obj = fn()
    fp.write_text(json.dumps(obj, ensure_ascii=False, default=str), encoding="utf-8")
    _log("google-play-scraper", name)
    return obj


def _log(url, name):
    import csv
    fp = RAW / "_requests_log.csv"
    new = not fp.exists()
    with open(fp, "a", encoding="utf-8-sig", newline="") as f:
        w = csv.writer(f)
        if new:
            w.writerow(["time", "file", "url"])
        w.writerow([datetime.now().strftime("%Y-%m-%d %H:%M:%S"), f"data/raw/{name}", url])


# ----------------------------------------------------------------------------------------------
def nso():
    """Cục Thống kê — báo cáo kinh tế xã hội hằng tháng (WordPress API) → doanh thu lữ hành, lưu trú–ăn uống."""
    posts = []
    for q in ["Báo cáo tình hình kinh tế - xã hội tháng", "Tình hình kinh tế - xã hội tháng", "Báo cáo tình hình kinh tế – xã hội quý",
              "Tình hình kinh tế – xã hội quý", "Báo cáo tình hình kinh tế – xã hội năm", "Tình hình kinh tế - xã hội năm",
              "Thông cáo báo chí tình hình kinh tế", "Thông cáo báo chí về tình hình kinh tế", "Thông cáo báo chí kinh tế xã hội quý",
              "doanh thu du lịch lữ hành", "tổng mức bán lẻ hàng hóa và doanh thu dịch vụ tiêu dùng"]:
        for page in range(1, 6):
            try:
                js = get("https://www.nso.gov.vn/wp-json/wp/v2/posts",
                         params={"search": q, "per_page": 100, "page": page, "_fields": "id,date,title,link"},
                         cache=f"nso_search_{re.sub(r'[^a-z0-9]+', '_', _ascii(q))}_{page}.json")
            except RuntimeError:
                break
            if not js:
                break
            posts += js
            if len(js) < 100:
                break
    # Tìm đích danh từng kỳ: thông cáo quý I, 6 tháng, 9 tháng, cả năm của 2017–2026
    for y in range(2017, 2027):
        for q in [f"quý IV và năm {y}", f"quý III và 9 tháng năm {y}", f"quý II và 6 tháng năm {y}", f"quý I năm {y}",
                  f"tình hình kinh tế xã hội năm {y}", f"9 tháng năm {y} lữ hành"]:
            try:
                js = get("https://www.nso.gov.vn/wp-json/wp/v2/posts",
                         params={"search": q, "per_page": 50, "_fields": "id,date,title,link"},
                         cache=f"nso_search_{re.sub(r'[^a-z0-9]+', '_', _ascii(q))}.json")
                posts += js
            except RuntimeError:
                pass
            time.sleep(0.3)
    seen, keep = set(), []
    for p in posts:
        t = re.sub(r"&#\d+;", "–", p["title"]["rendered"])
        if p["id"] in seen:
            continue
        seen.add(p["id"])
        if (re.search(r"tình hình kinh tế\s*[–-]\s*xã hội", t, re.I) or re.search(r"bán lẻ|lữ hành|thương mại, dịch vụ", t, re.I))                 and "Infographic" not in t:
            keep.append(dict(id=p["id"], date=p["date"][:10], title=t, link=p["link"]))
    rows = []
    for p in sorted(keep, key=lambda x: x["date"]):
        try:
            js = get(f"https://www.nso.gov.vn/wp-json/wp/v2/posts/{p['id']}", params={"_fields": "id,date,title,content"},
                     cache=f"nso_post_{p['id']}.json")
        except RuntimeError as e:
            print("  bỏ qua", p["id"], e)
            continue
        txt = re.sub(r"<[^>]+>", " ", js["content"]["rendered"])
        txt = re.sub(r"&nbsp;|\s+", " ", txt)
        rows.append(dict(**p, text=txt))
        time.sleep(0.3)
    pd.DataFrame(rows).to_json(PROC / "nso_reports.jsonl", orient="records", lines=True, force_ascii=False)
    print("nso ok", len(rows), "báo cáo")


def play_universe():
    g = _play()
    hits = []
    for term in UNIVERSE_TERMS:
        r = _cache_json(f"play_search_{re.sub(r'[^a-z0-9]+', '_', _ascii(term))}.json",
                        lambda term=term: g.search(term, lang="vi", country="vn", n_hits=30))
        for x in r:
            hits.append(dict(term=term, appId=x["appId"], title=x["title"], developer=x.get("developer"),
                             score=x.get("score"), installs=x.get("installs"), genre=x.get("genre")))
        time.sleep(0.8)
    # Tìm appId cho đối thủ cốt lõi chưa biết ID
    for name, pid, aid, typ in CORE:
        if pid is None:
            r = _cache_json(f"play_search_core_{re.sub(r'[^a-z0-9]+', '_', _ascii(name))}.json",
                            lambda name=name: g.search(name, lang="vi", country="vn", n_hits=5))
            for x in r[:3]:
                hits.append(dict(term=f"core:{name}", appId=x["appId"], title=x["title"], developer=x.get("developer"),
                                 score=x.get("score"), installs=x.get("installs"), genre=x.get("genre")))
            time.sleep(0.8)
    pd.DataFrame(hits).to_csv(PROC / "play_search_hits.csv", index=False)
    print("play_universe ok", len(hits), "lượt, ", pd.DataFrame(hits).appId.nunique(), "app")


def _ascii(s):
    import unicodedata
    s = s.replace("Đ", "D").replace("đ", "d").lower()
    return "".join(c for c in unicodedata.normalize("NFD", s) if unicodedata.category(c) != "Mn")


def core_ids():
    """appId Play cho đối thủ cốt lõi: ID cố định, hoặc kết quả tìm kiếm đầu tiên có tên khớp."""
    hits = pd.read_csv(PROC / "play_search_hits.csv")
    out = {}
    for name, pid, aid, typ in CORE:
        if pid:
            out[name] = pid
            continue
        h = hits[hits.term == f"core:{name}"]
        m = h[h.title.str.lower().str.contains(_ascii(name).split(".")[0][:5], regex=False) |
              h.title.map(_ascii).str.contains(_ascii(name)[:5], regex=False)]
        out[name] = (m.iloc[0].appId if len(m) else (h.iloc[0].appId if len(h) else None))
    return out


def play_details():
    g = _play()
    hits = pd.read_csv(PROC / "play_search_hits.csv")
    ids = list(dict.fromkeys(list(hits.appId) + [v for v in core_ids().values() if v]))
    rows = []
    for i, a in enumerate(ids):
        try:
            d = _cache_json(f"play_app_{a}.json", lambda a=a: g.app(a, lang="vi", country="vn"))
        except Exception as e:
            print("  lỗi", a, repr(e)[:60])
            continue
        rows.append({k: d.get(k) for k in ["appId", "title", "developer", "developerCountry", "genre", "genreId", "installs",
                                           "minInstalls", "realInstalls", "score", "ratings", "reviews", "histogram",
                                           "released", "updated", "version", "free", "price", "offersIAP", "containsAds",
                                           "description", "summary", "contentRating"]})
        if i % 50 == 0:
            print("  play_details", i, "/", len(ids))
        time.sleep(0.4)
    df = pd.DataFrame(rows)
    df["histogram"] = df.histogram.map(lambda h: json.dumps(h) if h is not None else None)
    df.to_json(PROC / "play_apps.jsonl", orient="records", lines=True, force_ascii=False)
    print("play_details ok", len(df))


def play_reviews(n=500):
    g = _play()
    ids = core_ids()
    rows = []
    for name, a in ids.items():
        if not a:
            continue
        try:
            rv = _cache_json(f"play_reviews_{a}.json",
                             lambda a=a: g.reviews(a, lang="vi", country="vn", sort=g.Sort.NEWEST, count=n)[0])
        except Exception as e:
            print("  lỗi review", a, repr(e)[:60])
            continue
        for r in rv:
            rows.append(dict(app=name, appId=a, score=r["score"], at=r["at"], content=r["content"], thumbsUp=r.get("thumbsUpCount"),
                             replied=bool(r.get("replyContent")), repliedAt=r.get("repliedAt"), version=r.get("reviewCreatedVersion")))
        time.sleep(1.0)
    pd.DataFrame(rows).to_csv(PROC / "play_reviews.csv.gz", index=False, compression="gzip")
    print("play_reviews ok", len(rows))


def appstore():
    """iTunes Search API (country=vn): vũ trụ app + đối thủ cốt lõi; số đánh giá là của cửa hàng Việt Nam."""
    rows = []
    for term in UNIVERSE_TERMS:
        js = get("https://itunes.apple.com/search", params=dict(term=term, country="vn", entity="software", limit=200, lang="vi_vn"),
                 cache=f"itunes_search_{re.sub(r'[^a-z0-9]+', '_', _ascii(term))}.json")
        for x in js["results"]:
            rows.append(dict(term=term, **{k: x.get(k) for k in ["trackId", "trackName", "sellerName", "primaryGenreName", "genres",
                                                                 "price", "averageUserRating", "userRatingCount", "releaseDate",
                                                                 "currentVersionReleaseDate", "version", "description", "bundleId"]}))
        time.sleep(1.5)
    ids = [c[2] for c in CORE if c[2]]
    js = get("https://itunes.apple.com/lookup", params=dict(id=",".join(map(str, ids)), country="vn"), cache="itunes_lookup_core.json")
    for x in js["results"]:
        rows.append(dict(term="core", **{k: x.get(k) for k in ["trackId", "trackName", "sellerName", "primaryGenreName", "genres",
                                                               "price", "averageUserRating", "userRatingCount", "releaseDate",
                                                               "currentVersionReleaseDate", "version", "description", "bundleId"]}))
    df = pd.DataFrame(rows)
    df["genres"] = df.genres.map(lambda g: "|".join(g) if isinstance(g, list) else g)
    df.to_json(PROC / "appstore_apps.jsonl", orient="records", lines=True, force_ascii=False)
    print("appstore ok", len(df), "dòng,", df.trackId.nunique(), "app")


def appstore_reviews(pages=10):
    rows = []
    for name, pid, aid, typ in CORE:
        if not aid:
            continue
        for p in range(1, pages + 1):
            try:
                js = get(f"https://itunes.apple.com/vn/rss/customerreviews/page={p}/id={aid}/sortby=mostrecent/json",
                         cache=f"itunes_rss_{aid}_{p}.json", retries=2)
            except RuntimeError:
                break
            ent = js.get("feed", {}).get("entry", [])
            if isinstance(ent, dict):
                ent = [ent]
            ent = [e for e in ent if "im:rating" in e]
            if not ent:
                break
            for e in ent:
                rows.append(dict(app=name, trackId=aid, score=int(e["im:rating"]["label"]), at=e.get("updated", {}).get("label"),
                                 title=e.get("title", {}).get("label"), content=e.get("content", {}).get("label"),
                                 version=e.get("im:version", {}).get("label")))
            time.sleep(0.6)
    pd.DataFrame(rows).to_csv(PROC / "appstore_reviews.csv.gz", index=False, compression="gzip")
    print("appstore_reviews ok", len(rows))


# ----------------------------------------------------------------------------------------------
TREND_GROUPS = {
    # nhóm: từ khoá (≤5); "agoda" là mốc chung để quy đổi giữa các nhóm thương hiệu
    "ota_global": ["agoda", "traveloka", "booking.com", "trip.com", "klook"],
    "ota_local": ["agoda", "mytour", "ivivu", "vntrip", "luxstay"],
    "other_brands": ["agoda", "vexere", "airbnb", "vietravel", "saigontourist"],
    "substitutes": ["du lịch", "chatgpt", "google maps", "tour du lịch", "lịch trình"],
    "price_seeking": ["du lịch", "mã giảm giá", "khách sạn giá rẻ", "vé máy bay giá rẻ", "voucher du lịch"],
    "supply": ["du lịch", "homestay", "khách sạn", "nhà nghỉ", "resort"],
}


def trends():
    from pytrends.request import TrendReq
    p = TrendReq(hl="vi-VN", tz=-420, timeout=(10, 40))   # không dùng retries: lỗi với urllib3 2.x
    out = []
    for gname, kws in TREND_GROUPS.items():
        fp = RAW / f"trends_{gname}.csv"
        if fp.exists():
            d = pd.read_csv(fp, index_col=0)
        else:
            for i in range(6):
                try:
                    p.build_payload(kws, geo="VN", timeframe="2016-01-01 2026-09-30")
                    d = p.interest_over_time().drop(columns="isPartial", errors="ignore")
                    d.to_csv(fp)
                    _log(f"Google Trends interest_over_time geo=VN 2016-01-01 2026-09-30 kw={kws}", fp.name)
                    time.sleep(12)
                    break
                except Exception as e:
                    print("   trends thử lại", gname, i, repr(e)[:60])
                    time.sleep(30 * (i + 1))
            else:
                print("!! bỏ nhóm", gname)
                continue
        anchor = kws[0]
        for c in d.columns:
            out.append(pd.DataFrame({"month": pd.to_datetime(d.index), "group": gname, "kw": c, "anchor": anchor,
                                     "raw": d[c].values, "anchor_raw": d[anchor].values}))
    pd.concat(out).to_csv(PROC / "trends_long.csv", index=False)
    print("trends ok")


# ----------------------------------------------------------------------------------------------
OVERPASS = ["https://overpass-api.de/api/interpreter", "https://overpass.kumi.systems/api/interpreter",
            "https://overpass.private.coffee/api/interpreter"]


def _overpass(q, cache):
    for s in OVERPASS:
        try:
            return get(s, params={"data": q}, cache=cache, retries=2, sleep=8, timeout=300)
        except RuntimeError as e:
            print("   overpass", s.split("/")[2], "lỗi")
    raise RuntimeError("Overpass không phản hồi")


def osm_provinces():
    q = """[out:json][timeout:180];area["ISO3166-1"="VN"][admin_level=2]->.vn;
rel(area.vn)["boundary"="administrative"]["admin_level"="4"];out tags;"""
    js = _overpass(q, "osm_provinces.json")
    rows = [dict(rel_id=e["id"], name=e["tags"].get("name"), name_en=e["tags"].get("name:en"),
                 iso=e["tags"].get("ISO3166-2"), population=e["tags"].get("population")) for e in js["elements"]]
    pd.DataFrame(rows).to_csv(PROC / "osm_provinces.csv", index=False)
    print("osm_provinces ok", len(rows))


LODGING_TAG = '["tourism"~"^(hotel|guest_house|hostel|motel|apartment|chalet|camp_site|alpine_hut|resort)$"]'


def _lodging_by_polygon(rel_id):
    """Dự phòng khi Overpass chưa dựng 'area' cho ranh giới tỉnh mới (truy vấn area trả 0 phần tử):
    lấy hình học ranh giới → truy vấn theo khung bao → giữ điểm nằm trong đa giác (đếm giao chẵn–lẻ trên mọi cạnh)."""
    g = _overpass(f"[out:json][timeout:180];rel({rel_id});out geom;", f"osm_boundary_{rel_id}.json")
    segs = []
    for m in g["elements"][0].get("members", []):
        if m.get("type") == "way" and m.get("role") in ("outer", "inner") and m.get("geometry"):
            pts = [(p["lon"], p["lat"]) for p in m["geometry"]]
            segs += list(zip(pts[:-1], pts[1:]))
    S = np.array(segs)                                    # (n, 2, 2): cạnh (x1,y1)-(x2,y2)
    w, s_, e_, n = S[..., 0].min(), S[..., 1].min(), S[..., 0].max(), S[..., 1].max()
    js = _overpass(f"[out:json][timeout:240];nwr{LODGING_TAG}({s_},{w},{n},{e_});out center tags;", f"osm_lodging_bbox_{rel_id}.json")
    x1, y1, x2, y2 = S[:, 0, 0], S[:, 0, 1], S[:, 1, 0], S[:, 1, 1]
    keep = []
    for el in js["elements"]:
        c = el.get("center") or {"lon": el.get("lon"), "lat": el.get("lat")}
        if c["lon"] is None:
            continue
        x, y = c["lon"], c["lat"]
        cross = ((y1 > y) != (y2 > y)) & (x < (x2 - x1) * (y - y1) / np.where(y2 == y1, 1e-12, y2 - y1) + x1)
        if cross.sum() % 2 == 1:
            keep.append(el)
    print(f"   rel {rel_id}: khung bao {len(js['elements'])} → trong ranh giới {len(keep)}")
    return {"elements": keep, "method": "polygon"}


def osm_lodging():
    prov = pd.read_csv(PROC / "osm_provinces.csv")
    rows = []
    for _, r in prov.iterrows():
        aid = 3600000000 + int(r.rel_id)
        q = f"""[out:json][timeout:240];area({aid})->.a;
nwr(area.a){LODGING_TAG};out tags;"""
        try:
            js = _overpass(q, f"osm_lodging_{r.rel_id}.json")
            if not js["elements"]:
                js = _lodging_by_polygon(r.rel_id)
        except RuntimeError:
            print("  bỏ tỉnh", r["name"])
            continue
        for e in js["elements"]:
            t = e.get("tags", {})
            rows.append(dict(rel_id=r.rel_id, province=r["name"], osm_type=e["type"], osm_id=e["id"], tourism=t.get("tourism"),
                             name=t.get("name"), brand=t.get("brand"), brand_wikidata=t.get("brand:wikidata"),
                             operator=t.get("operator"), stars=t.get("stars"), rooms=t.get("rooms"),
                             phone=bool(t.get("phone") or t.get("contact:phone")), website=bool(t.get("website") or t.get("contact:website")),
                             facebook=bool(t.get("contact:facebook") or t.get("facebook"))))
        time.sleep(3)
    pd.DataFrame(rows).to_csv(PROC / "osm_lodging.csv.gz", index=False, compression="gzip")
    print("osm_lodging ok", len(rows))


STEPS = ["nso", "play_universe", "play_details", "play_reviews", "appstore", "appstore_reviews", "trends",
         "osm_provinces", "osm_lodging"]

if __name__ == "__main__":
    for s in sys.argv[1:] or STEPS:
        try:
            globals()[s]()
        except Exception as e:
            print("!!", s, "lỗi:", repr(e)[:300])
