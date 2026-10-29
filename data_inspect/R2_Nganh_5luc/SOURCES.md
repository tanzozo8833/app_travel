# Nguồn dữ liệu — data_inspect/R2_Nganh_5luc

Tải ngày 26/10/2026. Mỗi tệp gốc trong `data/raw/` có dòng trong `data/raw/_MANIFEST.csv` (yêu cầu chính xác, thời điểm tải, dung lượng, SHA-256 16 ký tự đầu). Lần tải mới ghi thêm vào `data/raw/_requests_log.csv`.

## Dữ liệu tải trực tiếp [D]

| Mã | Nguồn | Tổ chức | Trang dữ liệu | Điểm truy cập | Nội dung | Mức | Số tệp gốc |
|---|---|---|---|---|---|---|---|
| **D1** | Báo cáo, thông cáo tình hình kinh tế – xã hội hằng tháng/quý/năm | Cục Thống kê, Bộ Tài chính | <https://www.nso.gov.vn/> | `https://www.nso.gov.vn/wp-json/wp/v2/posts?search=…  ·  /wp-json/wp/v2/posts/{id}` | Doanh thu du lịch lữ hành; doanh thu dịch vụ lưu trú, ăn uống (luỹ kế từ đầu năm); khách quốc tế — bóc từ văn bản báo cáo | A | 402 |
| **D2** | Google Play — thông tin ứng dụng (cửa hàng Việt Nam) | Google (truy cập qua google-play-scraper 1.2.7) | <https://play.google.com/store/apps?hl=vi&gl=VN> | `google_play_scraper.search(term, lang='vi', country='vn', n_hits=30) · app(appId, lang='vi', country='vn')` | Lượt cài (ngưỡng và ước tính thực), điểm, số đánh giá, phân bố sao, ngày ra mắt, ngày cập nhật, mô tả, quảng cáo, mua trong app | B | 722 |
| **D3** | Google Play — đánh giá người dùng (tiếng Việt, cửa hàng Việt Nam) | Google (qua google-play-scraper 1.2.7) | <https://play.google.com/store/apps?hl=vi&gl=VN> | `google_play_scraper.reviews(appId, lang='vi', country='vn', sort=NEWEST, count=500)` | Tối đa 500 đánh giá mới nhất mỗi app: số sao, nội dung, ngày, có/không phản hồi của nhà phát triển | B | 24 |
| **D4** | Apple App Store — iTunes Search & Lookup API (cửa hàng Việt Nam) | Apple | <https://performance-partners.apple.com/search-api> | `https://itunes.apple.com/search?term=…&country=vn&entity=software&limit=200 · /lookup?id=…&country=vn` | Số đánh giá và điểm trung bình **của cửa hàng Việt Nam**, ngày ra mắt, ngày cập nhật, thể loại, mô tả | B | 45 |
| **D5** | Apple App Store — Customer Reviews RSS (Việt Nam) | Apple | <https://rss.applemarketingtools.com/> | `https://itunes.apple.com/vn/rss/customerreviews/page={1..10}/id={trackId}/sortby=mostrecent/json` | Tối đa 500 đánh giá gần nhất mỗi app ở cửa hàng Việt Nam | B | 154 |
| **D6** | Google Trends — Việt Nam | Google (qua pytrends 4.9.2) | <https://trends.google.com/trends/explore?geo=VN> | `pytrends interest_over_time, geo=VN, 2016-01-01 → 2026-09-30; nhóm từ khoá trong fetch_data.TREND_GROUPS` | Mức quan tâm tương đối theo tháng của thương hiệu OTA, sản phẩm thay thế, từ khoá săn giá, loại hình lưu trú | B (chỉ số tương đối) | 6 |
| **D7** | OpenStreetMap qua Overpass API (© OpenStreetMap contributors, ODbL) | OpenStreetMap | <https://www.openstreetmap.org/> | `https://overpass-api.de/api/interpreter — ranh giới admin_level=4 ở Việt Nam; nwr(area)[tourism=hotel|guest_house|hostel|motel|apartment|chalet|camp_site|…]` | Cơ sở lưu trú đã được vẽ trên bản đồ theo tỉnh: loại hình, tên, thương hiệu (brand), đơn vị vận hành, liên hệ | B (thiếu cơ sở chưa vẽ) | 39 |
| **D8** | Levart — data_inspect/R1_PESTLE (kết quả đã kiểm chứng nguồn) | Nội bộ (nguồn gốc: World Bank, Wikipedia NQ 202/2025, Google Trends) | — | `R1_PESTLE/results/summary.json` | Dân số Hà Nội, số người 20–29 tuổi, thị phần sự chú ý OTA 2019–2025, độ co giãn chi trả | A/B | 0 |

## Số liệu thứ cấp nhập tay [S]

| Mã | Tài liệu | Đơn vị | Link | Con số dùng | Mức |
|---|---|---|---|---|---|
| **S1** | Vietnam Short-Term Rental Market Review 2025 | Airbtics | <https://airbtics.com/short-term-rental-market-report-2025-vietnam> | Cung chỗ ở ngắn hạn +42,76%, doanh thu +14,52%, công suất 45,33%, giá/đêm ~974.000 đ (2025) | C |
| **S2** | Chiến lược phân phối kênh đặt phòng: Mường Thanh và Vinpearl (12/05/2026) | Đỗ Công Nguyên, Tạp chí Kinh tế Tài chính | <https://nghiencuu.tapchikinhtetaichinh.vn/chien-luoc-phan-phoi-kenh-dat-phong-cua-cac-chuoi-khach-san-viet-nam-nghien-cuu-truong-hop-muong-thanh-va-vinpearl-155966.html> | OTA 58,85% lượt đặt phòng 2025; hoa hồng 15–25%; chi phí kênh trực tiếp 4,25–4,5% | C |
| **S3** | e-Conomy SEA 2025 — du lịch trực tuyến Việt Nam 4 tỷ USD (+16%), 8 tỷ USD năm 2030 | Google, Temasek, Bain (qua PhocusWire) | <https://www.phocuswire.com/vietnam-online-travel-market-double-2030> | Quy mô và tăng trưởng du lịch trực tuyến | B |
| **S4** | Vietnam Online Travel Market 2031 (tóm tắt công khai) | Mordor Intelligence (qua DayTrips Vietnam) | <https://daytripsvietnam.com/research/vietnam-online-travel-market-research/> | 2,87 tỷ USD (2025) → 4,69 tỷ USD (2031); OTA ngoại ~80% | C |
| **S5** | Khách du lịch nội địa và tổng thu 2019, 2022, 2023, 2025 | Cục Du lịch Quốc gia (qua Thông tin du lịch 12/2022; Thời báo Tài chính; Tuổi Trẻ) | <https://images.vietnamtourism.gov.vn/vn/dmdocuments/2023/ttdl2023.1.12.pdf> | 85 / 101,3 / 108 / 135,5 triệu lượt; 495 / 672 / ~1.000 nghìn tỷ | A |
| **S6** | Google Play — Service fees | Google | <https://support.google.com/googleplay/android-developer/answer/112622> | 15% cho 1 triệu USD đầu tiên mỗi năm, 30% phần trên | A |
| **S7** | Google Maps Platform — Billing & pricing FAQ | Google | <https://developers.google.com/maps/billing-and-pricing/faq> | Từ 01/03/2025: ngưỡng miễn phí theo SKU 10.000 / 5.000 / 1.000 lượt | A |
| **S8** | Decision Lab Travel Rankings 2024 (YouGov BrandIndex) | Decision Lab | <https://www.decisionlab.co/blog/vietnam-travel-landscape-2024> | Điểm cân nhắc: Traveloka 20,0; MoMo Travel 8,0; Shopee Travel 7,0 | B |
| **S9** | Klook: đặt chỗ qua creator +246% năm 2025 | Inc. Russia | <https://incrussia.ru/news/turisticheskaya-platforma-klook-privlekla-blogerov-k-prodvizheniyu-novyh-napravlenij-vo-vetname/> | Kênh creator — đối thủ mới | C |

## [GT] Giả định của nhóm

Không có nguồn. Liệt kê trong `tools/analyze_*.py` (biến `ASSUME`, từ điển tính năng, từ điển chủ đề đánh giá, ngưỡng chấm điểm lực). Phải thay bằng số đo từ phỏng vấn, khảo sát.

## Điều khoản

- Google Play, App Store: dữ liệu công khai trên trang cửa hàng; dùng cho nghiên cứu, không phân phối lại nội dung đánh giá nguyên văn quy mô lớn.
- Google Trends: dữ liệu công khai, ghi nguồn. · OpenStreetMap: ODbL 1.0. · Cục Thống kê: số liệu công bố chính thức.
