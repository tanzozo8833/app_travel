# LEVART — Bản thiết kế doanh nghiệp giai đoạn sơ khai

Cập nhật: **15/09/2026** · Phiên bản **0.1** · Trạng thái: **khung làm việc + giả thuyết**, chưa có số liệu khảo sát khách hàng thật

> Tài liệu này trả lời một câu hỏi: *"Muốn xây Levart thành một doanh nghiệp chứ không chỉ một app,
> thì bắt đầu từ đâu, phải vẽ ra được những gì, làm theo phương pháp nào, và vận hành theo quy trình
> gì để không lạc?"*
>
> Quy ước giống `status.md`: điều gì là **giả thuyết** thì đánh dấu **(GT)**; điều gì **chưa kiểm chứng**
> thì ghi rõ; điều gì lấy từ sách, luật, số liệu thì có nguồn ở **mục 11** dưới dạng `[S-xx]`, `[L-xx]`, `[D-xx]`.
> Không có nguồn nghĩa là ý kiến của người soạn. Phần pháp lý và thuế **không phải tư vấn pháp lý** —
> là danh sách việc cần hỏi luật sư / kế toán, kèm căn cứ để hỏi cho đúng.

Quan hệ với các tài liệu khác trong repo:

| Tài liệu | Là nguồn sự thật cho | Không dùng để |
|---|---|---|
| `Business.md` (tệp này) | Vì sao làm, cho ai, kiếm tiền cách nào, luật, cách tổ chức làm việc | Ghi tiến độ code |
| `status.md` | Hiện trạng sản phẩm, lỗi, việc kỹ thuật A–D, nghiên cứu tối ưu/bảo mật/quy mô (mục 7) | Quyết định chiến lược |
| `database_current.mmd`, `database_target.mmd` | Lược đồ dữ liệu | — |

---

## 0. Tóm tắt một trang

**Chẩn đoán.** Danh sách ý tưởng của nhóm (khoảng 40 mục, từ slogan tới bảo hiểm tới agent tự gọi
đối tác) không lộn xộn vì thiếu quy trình. Nó lộn xộn vì **ba thứ chưa tồn tại**:

1. **Một bản giả thuyết kinh doanh viết ra trên đúng một trang** — ai là khách, họ trả tiền cho việc gì, đến với Levart qua kênh nào, vì sao là Levart chứ không phải Google Maps + TikTok + Traveloka.
2. **Một danh sách điều-chưa-biết xếp theo độ rủi ro** — để biết việc gì đáng làm trước, việc gì để sau.
3. **Một nhịp làm việc cố định** — để câu hỏi "làm gì tiếp" không phải quyết định lại mỗi tuần.

Khi ba thứ đó có, "quy trình báo cáo và tổng hợp tài liệu" mà nhóm đề xuất tự nhiên có chỗ đứng:
tài liệu ghi lại **quyết định và bằng chứng**, không ghi lại ý tưởng.

**Nguyên lý nền.** Steve Blank định nghĩa startup là *"một tổ chức tạm thời được lập ra để đi tìm một
mô hình kinh doanh lặp lại được và mở rộng được"* `[S-03]`. Nghĩa là ở giai đoạn này công việc của
tổ chức là **TÌM**, chưa phải **VẬN HÀNH**. Quy trình của một tổ chức đi tìm khác quy trình của một
công ty đang chạy: đơn vị công việc là **thí nghiệm**, đầu ra là **bài học đã kiểm chứng**, không phải
tính năng hay doanh thu `[S-03]` `[S-04]`. Phần lớn cảm giác "không biết đang làm gì" đến từ việc
áp quy trình vận hành (phòng ban, báo cáo định kỳ) lên một tổ chức đang ở pha tìm kiếm.

**Làm gì đầu tiên — thứ tự bắt buộc, không đảo:**

| # | Việc | Xong khi | Tuần |
|---|---|---|---|
| 1 | Thoả thuận đồng sáng lập + đặt nhịp họp tuần | Có văn bản mọi người ký; có lịch họp cố định | 1 |
| 2 | Viết **Lean Canvas v0** + trả lời **5 câu hỏi Drucker** | Đúng 1 trang, mỗi ô đánh dấu (GT) | 1–2 |
| 3 | Xếp hạng giả thuyết theo rủi ro, chọn 3 cái nguy hiểm nhất | Bảng ở mục 2.4 được điền | 2 |
| 4 | Phỏng vấn **20–30 khách hàng** + **5 đối tác** thật | Có nhật ký phỏng vấn, persona v1, quyết định thị trường đầu cầu | 3–6 |
| 5 | **Một** thí nghiệm dùng thật với ≤ 50 người | Có số đo kích hoạt và quay lại thật, không phải số tải | 7–12 |
| 6 | Cổng quý 1: **tiếp / xoay / dừng** | Có biên bản quyết định | 12 |

**Không làm trong 90 ngày đầu:** đăng ký công ty, ký quỹ lữ hành, thuê KOL trả tiền, xây chatbot
riêng, xây agent tự gọi đối tác, web-hoá, xây tính năng ghép nhóm đi chung. Lý do từng mục ở **mục 9.3**.

**Bộ "bản vẽ" mà mọi doanh nghiệp đều phải có** — 9 tài liệu sống, liệt kê ở **mục 3**.

---

## 1. Gom danh sách ý tưởng thành sáu lớp

Mọi mục nhóm đã liệt kê đều rơi vào một trong sáu lớp dưới đây. Việc đầu tiên để hết lộn xộn là
**dán nhãn lớp** cho từng ý, vì mỗi lớp có câu hỏi trung tâm, người chịu trách nhiệm và thời điểm
khác nhau.

| Lớp | Câu hỏi trung tâm | Mục trong danh sách của nhóm | Ghế chịu trách nhiệm (mục 8.5) |
|---|---|---|---|
| **L1 — Mục đích & chiến lược** | Vì sao tồn tại? Thắng ở đâu, bằng gì? | tầm nhìn, slogan, tập khách hàng, phân loại loại du lịch, identity (nghĩa thương hiệu) | Người cầm tầm nhìn |
| **L2 — Khách hàng & thị trường** | Ai? Cần gì? Bao nhiêu người? Ai đang phục vụ họ? | tập khách hàng, sales "lợi ích > tiền bỏ ra", đi theo cặp/chùm | Tăng trưởng |
| **L3 — Sản phẩm & công nghệ** | Làm gì cho khách, làm bằng gì? | function, UI/UX, chatbot, auto liên hệ (agent), toàn bộ nhóm technique: tối ưu, test, bảo trì, deploy, giới hạn bộ nhớ/tốc độ/số người, consistency, AI model key, web-hoá, multiple app | Sản phẩm/Kỹ thuật |
| **L4 — Tăng trưởng & thương hiệu** | Khách biết tới và tin bằng cách nào? | marketing on platform, affiliate, KOL/talent/agency, "đã từng nhìn thấy ở đâu đó rồi tin dùng" | Tăng trưởng |
| **L5 — Vận hành & đối tác** | Lời hứa được thực hiện bằng ai, khi có sự cố thì sao? | 3rd party (gửi đồ, hotel, restaurant, phương tiện, nhà trẻ, khẩn cấp, phiên dịch, người đi kèm, HDV, quần áo/dụng cụ), chăm sóc khách hàng, hỏi đáp, hỗ trợ, tình huống/tai nạn | Vận hành |
| **L6 — Pháp lý, tài chính, tổ chức** | Được phép làm không? Tiền đi đâu? Ai làm gì, báo cho ai? | accountant, tax, insurance, policy, pháp lý, identity (nghĩa pháp nhân), quy trình, báo cáo, work together | Người điều phối |

**Ba loại phát biểu — phải phân biệt trước khi bàn:**

| Loại | Định nghĩa | Ví dụ trong Levart | Cách xử lý |
|---|---|---|---|
| **Giả thuyết (GT)** | Điều nhóm tin nhưng chưa ai ngoài nhóm xác nhận | "Gen Z muốn kết nối đi chung cho rẻ" | Thiết kế thí nghiệm, đo, rồi mới thành quyết định |
| **Quyết định** | Điều đã chốt, có ghi ngày và lý do | Tên Levart; Android trước; Firebase; chấp nhận rủi ro khoá Gemini (29/08/2026, `status.md` mục 5) | Ghi vào sổ quyết định (Phụ lục D); chỉ mở lại ở cổng quý |
| **Việc** | Hành động có người làm, có ngày xong | B2, B3, D1… trong `status.md` | Đưa vào chu kỳ (mục 8.2) |

Hầu hết 40 mục hiện nay là **giả thuyết**. Kể cả câu mô tả app hiện tại — *"Cuốn hộ chiếu du lịch
Việt Nam sống động của Gen Z"* — cũng là giả thuyết cho tới khi 20 người ngoài nhóm nói rằng họ cần nó.

---

## 2. Bắt đầu từ đâu

### 2.1 Năm câu hỏi của Drucker — bản nháp cho Levart

Peter Drucker cho rằng mọi tổ chức phải trả lời được năm câu hỏi trước khi làm bất cứ kế hoạch
nào `[S-01]`. Đây là bộ lọc rẻ nhất để biết một ý tưởng có thuộc về doanh nghiệp này không.

| Câu hỏi | Bản nháp v0 (GT — phải sửa sau phỏng vấn) |
|---|---|
| **1. Sứ mệnh của chúng ta là gì?** | Biến mỗi chuyến đi trong nước của người trẻ Việt Nam thành một **thành tựu** có thể lập kế hoạch dễ, lưu giữ được và khoe được. |
| **2. Khách hàng của chúng ta là ai?** | *Khách hàng chính:* người 18–25, đi du lịch nội địa **tự túc**, ở Hà Nội. *Khách hàng hỗ trợ:* homestay, quán ăn, dịch vụ địa phương muốn được người trẻ tìm thấy. |
| **3. Khách hàng coi trọng điều gì?** | Cần phỏng vấn. Giả thuyết: (a) đi đúng chỗ "không đụng hàng", (b) lên lịch trình nhanh, (c) có bằng chứng đã đi — ảnh đẹp, con dấu, (d) an toàn khi đi xa. |
| **4. Kết quả của chúng ta là gì?** | Số chuyến đi **được đi thật** và **được đóng dấu** nhờ Levart; sau đó mới là doanh thu. |
| **5. Kế hoạch của chúng ta là gì?** | Mục 9 — 90 ngày đầu. |

Drucker cũng cảnh báo: định nghĩa sai "chúng ta đang kinh doanh gì" là nguyên nhân thất bại phổ biến
nhất; ông gọi tập giả định về thị trường, khách hàng, năng lực là **"lý thuyết về doanh nghiệp"**
và yêu cầu kiểm tra nó định kỳ `[S-02]`. Levitt đưa ví dụ kinh điển: ngành đường sắt Mỹ suy tàn vì
tự định nghĩa mình là "kinh doanh đường sắt" thay vì "kinh doanh vận chuyển" `[S-05]`. Với Levart,
câu hỏi tương ứng là: **Levart kinh doanh "app du lịch", hay kinh doanh "thành tựu và kỷ niệm của
người trẻ khi đi khắp Việt Nam"?** Hai câu trả lời dẫn tới hai doanh nghiệp rất khác nhau — câu
sau cho phép bán hộ chiếu in, con dấu thật, sự kiện; câu trước chỉ cạnh tranh với OTA.

### 2.2 Lean Canvas v0

Lean Canvas (Ash Maurya, biến thể của Business Model Canvas cho startup) gói toàn bộ giả thuyết
kinh doanh vào **một trang, 9 ô**, để cả nhóm nhìn cùng một thứ và sửa cùng lúc `[S-08]` `[S-07]`.
Bản dưới đây là **v0 — toàn bộ là (GT)**; nhiệm vụ của 6 tuần đầu là làm nó sai ít đi.

| Ô | Nội dung v0 |
|---|---|
| **1. Phân khúc khách hàng** | Sinh viên và người mới đi làm 18–25 ở Hà Nội, đi tự túc 2–3 chuyến/năm, ngân sách 3–5 triệu/chuyến `[D-03]`. **Người dùng sớm (early adopter):** người đang cố "đi đủ" — có mục tiêu đếm tỉnh, đếm cột mốc, đếm đỉnh. |
| **2. Vấn đề** | (a) Lên kế hoạch tự túc tốn nhiều giờ, thông tin rải ở TikTok/FB/Google Maps; (b) đi rồi không có gì để "giữ" ngoài album ảnh lộn xộn; (c) thiếu kênh tin cậy cho dịch vụ nhỏ ở địa phương; (d) không biết xử lý khi gặp sự cố xa nhà. **Giải pháp hiện tại của họ:** ChatGPT/Gemini + Google Maps + hội nhóm FB + TikTok + hỏi bạn. |
| **3. Giá trị độc nhất** | *"Cuốn hộ chiếu du lịch Việt Nam đóng dấu bằng GPS thật — 34 tỉnh, một hành trình đời người."* Khái niệm tầng cao: **thành tựu, không phải tiện ích.** |
| **4. Giải pháp** | Con dấu 34 tỉnh xác nhận bằng GPS; lịch trình AI có thể sửa tay; bảng tin ảnh cùng thế hệ; sau này: gợi ý dịch vụ địa phương có kiểm chứng. |
| **5. Kênh** | Cộng đồng đại học (CLB du lịch, nhóm lớp), TikTok/Threads nội dung "đi đủ 34 tỉnh", nhóm FB du lịch tự túc, giới thiệu bạn bè qua con dấu. |
| **6. Dòng thu** | Theo thứ tự thử: hoa hồng giới thiệu (lưu trú, vé, bảo hiểm) → hộ chiếu in + khung ảnh cao cấp → tài trợ con dấu/thử thách từ thương hiệu và tỉnh → phí sàn dịch vụ bản địa. Chi tiết mục 5. |
| **7. Cấu trúc chi phí** | Firebase (Firestore/Storage/Functions), Google Maps Platform, Gemini API, Play Console, tên miền, nhãn hiệu, kế toán thuê ngoài khi có pháp nhân; **chi phí lớn nhất là thời gian của nhóm**. |
| **8. Chỉ số then chốt** | Chỉ số Sao Bắc Đẩu đề xuất: **số con dấu (check-in có ảnh) được đóng mỗi tuần**. Phễu AARRR ở mục 8.7. |
| **9. Lợi thế khó sao chép** | Chưa có. Ứng viên: bộ sưu tập con dấu của người dùng (chi phí chuyển đổi), dữ liệu địa điểm bản địa do cộng đồng đóng góp, quan hệ với dịch vụ nhỏ ở từng tỉnh. Thiel gọi đây là câu hỏi "bí mật nào bạn biết mà người khác không?" `[S-15]` — hiện nhóm chưa trả lời được, và điều đó bình thường ở tuần 1. |

### 2.3 Việc-cần-làm của khách hàng (Jobs To Be Done) và bản đồ giá trị

Christensen: khách hàng không mua sản phẩm, họ **"thuê"** sản phẩm để làm một việc trong một hoàn
cảnh cụ thể `[S-06]`. Osterwalder: giá trị chỉ tồn tại khi **nỗi đau / lợi ích** của khách khớp với
**thuốc giảm đau / tạo lợi ích** của sản phẩm `[S-09]`. Bốn "việc" ứng viên cho Levart — mỗi cái là
một GT phải nghe được từ miệng khách:

| Việc cần làm (GT) | Hoàn cảnh | Nỗi đau hiện tại | Levart làm gì |
|---|---|---|---|
| **Lên kế hoạch một chuyến tự túc tới nơi chưa từng đến** | Tối trước khi đi, 2–4 người, ngân sách chặt | Tốn 3–5 giờ gom thông tin từ 5 nguồn; sợ bỏ lỡ chỗ hay | Lịch trình AI + sửa tay + gợi ý địa điểm ngoài luồng |
| **Có bằng chứng "tôi đã đi" mà bạn bè công nhận** | Sau chuyến đi | Ảnh trôi trên story 24 giờ; không có gì tích luỹ | Con dấu GPS 34 tỉnh, hồ sơ du lịch (Travel CV) |
| **Tìm người đi cùng hợp gu để rẻ hơn và vui hơn** | Trước chuyến đi, đi lẻ | Nhóm FB lạ, không biết tin ai | (Sau) ghép nhóm có xác thực |
| **Biết phải làm gì khi gặp sự cố xa nhà** | Đang đi, mất mạng/lạc/tai nạn | Không ai chỉ; hoảng | Trang khẩn cấp offline, số gọi, bệnh viện gần nhất |

### 2.4 Xếp hạng giả thuyết theo rủi ro — làm cái nguy hiểm nhất trước

Ries đề nghị kiểm chứng **giả thuyết giá trị** (khách có cần không) trước **giả thuyết tăng trưởng**
(khách có lan không) `[S-04]`. Higham đi xa hơn: thay vì xây MVP, hãy xây **bài kiểm tra cho giả thuyết
rủi ro nhất** (Riskiest Assumption Test) `[S-25]`. Bảng dưới là điểm khởi đầu; nhóm chấm lại ở tuần 2.

| # | Giả thuyết | Nếu sai thì | Bằng chứng hiện có | Cách kiểm rẻ nhất | Ưu tiên |
|---|---|---|---|---|---|
| G1 | Người trẻ **quay lại** app sau chuyến đi đầu vì con dấu/hộ chiếu | Không có gì để giữ người dùng; chỉ còn là tool dùng một lần | **Không có** — máy chủ mới 11 tài khoản, 5 bài (`status.md` mục 1) | Cohort 30 người dùng thật, đo quay lại sau 7 và 30 ngày; khảo sát Sean Ellis (mục 8.7) | **1** |
| G2 | Người trẻ chọn Levart để **lên lịch trình** thay ChatGPT/Gemini + Google Maps | Tính năng AI không phải lý do tải app | Không có | Phỏng vấn: "lần gần nhất bạn lên kế hoạch bằng gì, mất bao lâu?" | **2** |
| G3 | Dịch vụ địa phương **chịu trả hoa hồng/phí** để được người trẻ tìm thấy | Mô hình sàn không có bên cung | Không có | 5 cuộc nói chuyện với chủ homestay/quán ở 1 tỉnh | **3** |
| G4 | Người dùng **chịu trả tiền** cho hộ chiếu in/khung/AI nâng cao | Không có dòng thu B2C | Không có | Trang đặt trước (pre-order) hộ chiếu in, đếm người bấm "đặt" | 4 |
| G5 | Nội dung "đi đủ 34 tỉnh" **lan** trên TikTok/Threads | Kênh tăng trưởng chính không hoạt động | Không có | 10 video trong 4 tuần, đo lượt xem → cài → check-in | 5 |
| G6 | Người trẻ **muốn ghép nhóm** đi chung với người lạ có xác thực | Ý tưởng "đi theo cặp/chùm" không có cầu | Chưa có; báo chí ghi xu hướng đi tự túc >70% `[D-03]` nhưng không nói về ghép nhóm | Hỏi trong phỏng vấn; thử ghép tay 1 nhóm qua Zalo | 6 |
| G7 | Có thể **vận hành hợp pháp** với bảng tin + vị trí GPS mà không cần giấy phép ở giai đoạn nhỏ | Phải lập doanh nghiệp và xin phép sớm hơn dự tính | Có căn cứ luật, mục 7 | Đối chiếu ngưỡng NĐ 147 và Luật BVDLCN với luật sư (1 buổi) | 7 |
| G8 | Nhóm **duy trì được** nhịp làm việc bán thời gian ≥ 6 tháng | Mọi thứ trên vô nghĩa | Lịch sử làm việc: hai đợt dồn 28–29/08 và 07–08/09/2026, giữa hai đợt trống 9 ngày | Nhịp tuần ở mục 8; đo bằng số tuần liên tiếp họp đủ | **0** |

### 2.5 Chọn thị trường đầu cầu (beachhead)

Aulet (MIT) và Moore đều khẳng định: startup không đủ sức phục vụ "mọi người trẻ Việt Nam"; phải chọn
**một** phân khúc đủ nhỏ để thống trị, rồi lan sang phân khúc kế `[S-11]` `[S-10]`. Tiêu chí của Aulet
cho một đầu cầu tốt: khách trong phân khúc **mua giống nhau**, **nói chuyện với nhau** (truyền miệng),
**đội ngũ tiếp cận được**, và có đủ tiền/động lực.

**Đề xuất đầu cầu (GT):** sinh viên năm 2–4 và người mới tốt nghiệp ≤ 2 năm, ở **Hà Nội**, đã đi tự túc
≥ 1 lần trong 12 tháng, điểm đến quen thuộc: Hà Giang, Ninh Bình, Mộc Châu, Sa Pa, Hạ Long (GT về
điểm đến — phải xác nhận qua phỏng vấn).

Vì sao đây chứ không phải phân khúc khác:

| Tiêu chí Aulet | Phân khúc này | Phân khúc "khách quốc tế" | Phân khúc "gia đình có con nhỏ" |
|---|---|---|---|
| Tiếp cận được bằng 0 đồng | **Có** — nhóm đang ở trong đúng môi trường (Bách khoa Hà Nội và các trường lân cận) | Không — cần kênh tiếng Anh, Klook/Booking đã chiếm | Không — khác thế hệ, khác kênh |
| Nói chuyện với nhau | **Có** — cùng lớp, CLB, nhóm chat | Yếu | Trung bình |
| Mua giống nhau | **Có** — ngân sách 3–5 triệu, ưu tiên trải nghiệm `[D-03]` | Khác nhau theo quốc tịch | Ưu tiên an toàn, tiện nghi |
| Khớp với con dấu 34 tỉnh | **Cao** — "mỗi chuyến đi là một thành tựu cá nhân" `[D-03]` | Thấp — họ đi 1 lần | Thấp |

Kết luận: các mục **nhà trẻ**, **phiên dịch**, **HDV cho khách quốc tế** trong danh sách nằm **ngoài
đầu cầu**. Không xoá, nhưng đưa vào "Bãi đỗ ý tưởng" (Phụ lục F) và không tốn phút họp nào cho chúng
trong 2 quý đầu.

**Đo cỡ thị trường — chỉ tính từ dưới lên, không nhân phần trăm cho đẹp.** Aulet yêu cầu TAM tính
từ số người × giá trị/người/năm `[S-11]`. Với Levart, công thức đúng là:

```
Doanh thu tiềm năng/năm
  = N (số người 18–29 ở VN đi nội địa tự túc ≥ 1 lần/năm)   ← chưa có số; tra Cục Thống kê + Cục Du lịch
  × chuyến/người/năm                                          ← 2–3 theo báo chí [D-03], chưa kiểm chứng nguồn gốc khảo sát
  × giá trị dịch vụ đặt QUA Levart mỗi chuyến                ← chưa biết; phụ thuộc G3, G4
  × tỷ lệ hoa hồng hoặc giá bán                              ← chưa biết; đọc điều khoản affiliate
```

Bối cảnh trần: năm 2025 Việt Nam có khoảng **135,5 triệu lượt khách nội địa**, **gần 21,2 triệu lượt
khách quốc tế**, tổng thu du lịch **lần đầu vượt 1 triệu tỷ đồng** `[D-01]` `[D-02]`. Con số này
**không phải TAM của Levart** — Levart không thu được tiền vé máy bay hay tiền phòng; nó chỉ nói thị
trường đủ lớn để không phải lo về trần trong nhiều năm. Điền được bốn biến trên là một **việc** cụ thể
cho tuần 3–6.

---

## 3. Chín bản vẽ mà mọi doanh nghiệp đều phải có

Đây là bộ **tài liệu sống** — mỗi cái đúng một trang (trừ nhật ký), có chủ sở hữu, có ngày cập nhật,
và được mở ra ở đúng nhịp họp (mục 8). Không có bản nào trong số này thì nhóm đang "làm việc" nhưng
không "xây doanh nghiệp".

| # | Bản vẽ | Trả lời câu hỏi | Mẫu / phương pháp | Nguồn | Chủ sở hữu | Nhịp cập nhật |
|---|---|---|---|---|---|---|
| 1 | **Tầm nhìn – Sứ mệnh – Giá trị cốt lõi** (kèm BHAG 10 năm) | Vì sao chúng ta tồn tại, đi về đâu, không bao giờ làm gì | Core ideology + BHAG của Collins & Porras; "Why" của Sinek | `[S-12]` `[S-13]` | Người cầm tầm nhìn | Quý |
| 2 | **Lean Canvas** | Mô hình kinh doanh trên 1 trang | Maurya | `[S-08]` | Người cầm tầm nhìn | Sau mỗi chu kỳ 6 tuần |
| 3 | **Persona + đầu cầu + cỡ thị trường từ dưới lên** | Khách là ai, bao nhiêu, ở đâu | Aulet bước 1–5; Moore | `[S-11]` `[S-10]` | Tăng trưởng | Sau đợt phỏng vấn |
| 4 | **Nhật ký khám phá khách hàng** | Khách nói gì (nguyên văn), ta học được gì | Customer Discovery của Blank; kỹ thuật hỏi của Fitzpatrick | `[S-03]` `[S-14]` | Tăng trưởng | Sau mỗi cuộc |
| 5 | **Định vị + bộ nhận diện đặc trưng** | Levart là "duy nhất" cái gì, người ta nhận ra nó bằng gì | Dunford; Ries & Trout; tài sản thương hiệu đặc trưng của Sharp | `[S-16]` `[S-17]` `[S-18]` | Người cầm tầm nhìn | Quý |
| 6 | **Lộ trình theo giai đoạn + Bảng chỉ số** | Đang ở giai đoạn nào, chỉ số nào quyết định, cổng kế tiếp là gì | 5 giai đoạn Lean Analytics; AARRR; OKR | `[S-19]` `[S-20]` `[S-21]` | Người điều phối | Tuần (số), Quý (giai đoạn) |
| 7 | **Sổ rủi ro + Pre-mortem + Danh mục pháp lý** | Cái gì giết được chúng ta, luật nào áp lên chúng ta | Pre-mortem của Klein; bảng rủi ro; mục 7 | `[S-22]` | Người điều phối | Quý |
| 8 | **Sổ quyết định** | Đã chốt gì, khi nào, vì sao, chốt trong bối cảnh nào | Architecture Decision Record của Nygard, dùng cho cả quyết định kinh doanh | `[S-23]` | Ai ra quyết định | Mỗi quyết định |
| 9 | **Sơ đồ trách nhiệm + Thoả thuận đồng sáng lập** | Ai giữ ghế nào, chia gì, rời đi thì sao | Accountability Chart của Wickman; ba chữ R của Wasserman; Slicing Pie của Moyer | `[S-24]` `[S-26]` `[S-27]` | Người điều phối | Khi có người vào/ra |

Hai tài liệu kỹ thuật (`status.md`, `database_*.mmd`) là **bản vẽ số 10** — đã có, đã tốt, giữ nguyên
cách làm.

### 3.1 Tầm nhìn, sứ mệnh, slogan — cách làm, không phải câu chữ

Collins & Porras tách hai thứ hay bị lẫn: **tư tưởng cốt lõi** (không đổi: giá trị + mục đích) và
**tương lai hình dung** (BHAG — mục tiêu lớn, táo bạo, 10–30 năm, đo được) `[S-12]`. Sinek đặt "Why"
trước "How" và "What" `[S-13]`. Kawasaki phân biệt **mantra** (3–4 chữ, dùng nội bộ, trả lời "chúng
ta làm gì cho ai") với **slogan** (dùng cho khách) `[S-28]`. Anh em Heath cho công thức để một câu
"dính": đơn giản, bất ngờ, cụ thể, đáng tin, gợi cảm xúc, có câu chuyện `[S-29]`.

Bản nháp v0 để nhóm **bác bỏ**, không phải để dùng:

| Thành phần | Nháp v0 (GT) |
|---|---|
| Mục đích cốt lõi | Để người trẻ Việt Nam **biết đất nước mình bằng đôi chân**, không chỉ qua màn hình. |
| Giá trị cốt lõi (chọn 3, không hơn) | Đi thật – đóng dấu thật (không giả GPS). Tôn trọng người bản địa. Bằng chứng trước, cảm tính sau (đã là văn hoá làm việc của nhóm). |
| BHAG | Đến năm 2036, **1 triệu người** đã đóng đủ 34 con dấu bằng Levart. |
| Mantra nội bộ | *"Đi để đóng dấu."* |
| Định vị kiểu "duy nhất" (Neumeier) | Levart là cuốn hộ chiếu du lịch **duy nhất** đóng dấu bằng GPS thật cho 34 tỉnh Việt Nam, dành cho người trẻ đi tự túc muốn biến mỗi chuyến đi thành thành tựu `[S-30]`. |
| Slogan cho khách | Chưa chốt. Ba hướng để thử: (a) "34 tỉnh. Một cuốn hộ chiếu." (b) "Việt Nam, từng con dấu." (c) "Đi thật. Dấu thật." |

**Cách chốt slogan:** in ba câu lên ba ảnh màn hình giống nhau, cho 20 người trong đầu cầu xem mỗi ảnh
5 giây, hỏi "app này làm gì?" và "bạn nhớ câu nào?". Chọn câu có tỷ lệ hiểu đúng và nhớ cao nhất. Không
biểu quyết trong nhóm — nhóm không phải khách.

---

## 4. Chiến lược: chọn nơi để thắng

### 4.1 Năm lựa chọn nối tiếp (Playing to Win)

Lafley & Martin tóm chiến lược thành năm lựa chọn phải **nhất quán với nhau** `[S-31]`. Rumelt gọi
phần cốt lõi là **chẩn đoán → chính sách dẫn đường → hành động nhất quán**; chiến lược tồi là
chiến lược liệt kê mục tiêu mà không có chẩn đoán `[S-32]`. Porter nhắc: chiến lược là **chọn không
làm gì** `[S-33]`.

| Lựa chọn | Bản nháp v0 (GT) | Chọn **không** làm |
|---|---|---|
| **Khát vọng thắng** | Trở thành cách mặc định người trẻ Việt Nam ghi lại và khoe hành trình đi khắp 34 tỉnh | Không cố thành OTA; không cố thành TikTok du lịch |
| **Chơi ở đâu** | Gen Z Hà Nội, du lịch nội địa tự túc, 5 tỉnh gần | Không khách quốc tế, không gia đình, không tour trọn gói — trong 2 quý đầu |
| **Thắng bằng gì** | Con dấu GPS (tài sản đặc trưng, không ai có) + lịch trình cho địa điểm ngoài luồng + cộng đồng cùng thế hệ | Không cạnh tranh về giá phòng, không cạnh tranh về đánh giá số đông |
| **Năng lực cần có** | Dữ liệu địa điểm bản địa; Android/AI; quan hệ với dịch vụ nhỏ theo tỉnh; sản xuất nội dung ngắn | Chưa cần: đội sale, đội CS 24/7, đội pháp chế |
| **Hệ thống quản trị** | Mục 8 | — |

### 4.2 Ba cách định vị — và đề xuất

| Định vị | Đối thủ trực diện | Nhận xét |
|---|---|---|
| **A. "Hộ chiếu du lịch Việt Nam"** — hạng mục mới | Chưa có ai chiếm tên hạng mục này ở Việt Nam (**chưa kiểm chứng** — cần tra store và nhãn hiệu) | Ries & Trout: nếu không thể là số một trong hạng mục, hãy tạo hạng mục mà mình là số một `[S-17]`. Sách *Play Bigger* gọi đây là thiết kế hạng mục `[S-34]`. |
| **B. "Trợ lý lịch trình AI"** | ChatGPT, Gemini, Google Maps, Wanderlog (**chưa kiểm chứng** mức dùng ở VN) | Biển đỏ. Mô hình nền có lợi thế dữ liệu và phân phối; Levart chỉ thắng nếu có dữ liệu địa điểm bản địa mà họ không có. |
| **C. "Mạng xã hội du lịch"** | TikTok, Instagram, Threads, nhóm Facebook | Biển đỏ nhất. Thêm gánh pháp lý mạng xã hội (mục 7). |

**Đề xuất:** dùng **A làm mũi nhọn**, **B làm công cụ kéo người vào**, **C là hào nước xây sau**.
Chris Dixon gọi công thức này là *"đến vì công cụ, ở lại vì mạng lưới"* `[S-35]`: người dùng tải vì
lịch trình AI và con dấu (công cụ cá nhân, có ích ngay cả khi chưa ai khác dùng), ở lại vì bạn bè cùng
đóng dấu. Andrew Chen bổ sung: mạng lưới phải khởi động từ một **"mạng nguyên tử"** nhỏ đủ để tự
duy trì — với Levart là **một trường đại học** hoặc **một chuyến đi của một lớp**, không phải "Gen Z
Việt Nam" `[S-36]`.

### 4.3 Bản đồ chiến lược (Blue Ocean) — thêm gì, bỏ gì

Kim & Mauborgne: vẽ các yếu tố ngành đang cạnh tranh, rồi quyết định **Loại bỏ – Giảm – Tăng – Tạo mới**
để tránh đánh nhau trên cùng trục `[S-37]`. Bản nháp:

| Yếu tố cạnh tranh của ngành | OTA (Traveloka, Booking…) | Mạng xã hội (TikTok…) | **Levart v0** |
|---|---|---|---|
| Giá và đặt phòng/vé | Rất cao | — | **Loại bỏ** (chuyển sang đối tác qua link) |
| Đánh giá số đông | Cao | Trung bình | **Giảm** (dùng dấu GPS thật thay sao ảo) |
| Bản đồ và chỉ đường | Trung bình | Thấp | Ngang (Google Maps) |
| Lịch trình | Thấp | — | **Tăng** |
| Ảnh và lan truyền | Thấp | Rất cao | Trung bình |
| **Thành tựu tích luỹ (con dấu, hộ chiếu)** | — | — | **Tạo mới** |
| **Địa điểm ngoài luồng do người đi thật đóng góp** | Thấp | Rải rác | **Tăng mạnh** |
| **An toàn và khẩn cấp cho người đi tự túc** | — | — | **Tạo mới** |
| **Ghép nhóm có xác thực** | — | Nhóm FB, không xác thực | **Tạo mới (sau)** |

### 4.4 Thương hiệu: cơ chế đằng sau "đã từng nhìn thấy ở đâu đó rồi tin dùng"

Trực giác marketing mà nhóm nêu — *khách đã thấy thương hiệu này ở đâu đó, nên tin và dùng* — là mô
tả chính xác của hai cơ chế đã được đo bằng thực nghiệm:

- **Hiệu ứng tiếp xúc đơn thuần** (Zajonc, 1968): người ta thích và tin hơn thứ họ đã gặp nhiều lần,
  kể cả khi không nhớ đã gặp `[S-38]`.
- **Độ sẵn có trong tâm trí + độ sẵn có vật lý** (Sharp, Ehrenberg-Bass): thương hiệu lớn không lớn
  vì khách trung thành hơn, mà vì **được nghĩ tới nhiều hơn trong nhiều tình huống mua hơn** và **dễ
  mua hơn**; công cụ để được nghĩ tới là **tài sản đặc trưng** — hình, màu, biểu tượng lặp lại nhất
  quán `[S-18]`. Với Levart, tài sản đặc trưng ứng viên là **hình con dấu tỉnh** — xuất hiện trong
  app, trên ảnh chia sẻ, trên hộ chiếu in, trên sticker ở quán đối tác. Đây là lý do mọi nội dung KOL,
  mọi video, mọi ảnh feed phải mang con dấu đó, chứ không phải logo chữ.
- **Bằng chứng xã hội** (Cialdini): thấy người giống mình đã dùng là lý do dùng `[S-39]`.

**Vậy KOL / talent / agency có cần không, và quản lý thế nào?** Cần — nhưng **đúng thứ tự**:

| Giai đoạn (mục 8.6) | Việc làm về thương hiệu | Lý do |
|---|---|---|
| Thấu cảm & Bám dính (quý 1–2) | **Không** chi tiền KOL. Người sáng lập tự làm nội dung, tự đi đóng dấu, tự trả lời từng bình luận. Đổi sản phẩm (hộ chiếu in, khung ảnh) lấy nội dung từ 5–10 **micro-creator** là bạn bè hoặc người đang "đi đủ 34 tỉnh". | Paul Graham: việc không mở rộng được (tự tay tuyển từng người dùng) là việc **bắt buộc** ở đầu; Airbnb tự đi chụp ảnh từng căn hộ `[S-40]`. Binet & Field: hiệu quả ngắn hạn cần kích hoạt trực tiếp, thương hiệu dài hạn cần 60% ngân sách — nhưng chỉ khi **đã có sản phẩm người ta muốn** `[S-41]`. |
| Lan truyền (quý 3+) | Chương trình creator có quy tắc rõ (tỷ lệ chia, nội dung bắt buộc có con dấu, quyền dùng lại nội dung). Chọn kênh bằng **Bullseye**: liệt kê 19 kênh, thử 3, dồn vào 1 `[S-42]`. | Weinberg & Mares: startup thất bại về kênh không vì thiếu kênh mà vì rải mỏng. |
| Doanh thu & Mở rộng | Lúc đó mới bàn "agency nội dung", "talent", "quản lý KOL" như một **năng lực** — thuê ngoài trước, tự xây sau khi chứng minh được CAC. | Xây năng lực trước khi có nhu cầu là chi phí chết. |

"**Revolutions**" của app — thay đổi lớn về hướng — chỉ được đề xuất ở **cổng quý** kèm bằng chứng
(mục 8.2). Giữa quý, mọi ý tưởng lớn đi vào Bãi đỗ (Phụ lục F). Đây là biện pháp kỹ thuật chống
"tuần này làm A, tuần sau bỏ A".

---

## 5. Mô hình kinh doanh và tiền

### 5.1 Các dòng thu — thứ tự thử

Nguyên tắc từ Gurley khi đánh giá sàn/marketplace: sàn chỉ đáng làm khi **tần suất giao dịch cao**,
**thị trường phân mảnh** ở cả hai bên, và nền tảng **tạo giá trị thật cho giao dịch**, không chỉ đứng
giữa `[S-43]`. Parker, Van Alstyne & Choudary: bài toán "gà–trứng" giải bằng cách **tự làm bên cung
trước** hoặc **đứng trên một sản phẩm đã có ích một phía** `[S-44]`. Lenny Rachitsky khảo sát các
app tiêu dùng lớn: gần như tất cả lấy 1.000 người dùng đầu bằng tay — đi tới nơi khách đang ở, mời
từng người `[S-45]`.

| Thứ tự | Dòng thu | Cách hoạt động | Điều kiện pháp lý (mục 7) | Rủi ro chính |
|---|---|---|---|---|
| 1 | **Hoa hồng giới thiệu (affiliate)** — lưu trú, vé xe/máy bay, bảo hiểm du lịch, đồ dùng | Levart hiển thị gợi ý, người dùng bấm ra trang đối tác, Levart nhận hoa hồng. Các chương trình affiliate của OTA và sàn TMĐT **có tồn tại** (Booking, Agoda, Klook, Traveloka, Shopee) — **điều kiện và tỷ lệ chưa kiểm chứng**, phải đọc từng chương trình. | Nếu Levart bị coi là mạng xã hội thì **phát sinh doanh thu cần giấy phép MXH** `[L-03]` → phải có pháp nhân trước khi bật. Thu nhập affiliate về cá nhân trước khi có công ty là thu nhập chịu thuế TNCN — hỏi kế toán. | Biên mỏng; phụ thuộc đối tác đổi điều khoản |
| 2 | **Sản phẩm vật lý gắn với số** — hộ chiếu in, bộ con dấu, sticker | Sản xuất theo đơn, bán trong app và tại sự kiện. Kevin Kelly: 1.000 người hâm mộ thật sự đủ nuôi một dự án nhỏ `[S-46]`. | Bán hàng → doanh nghiệp hoặc hộ kinh doanh; hoá đơn điện tử. | Tồn kho; vận chuyển |
| 3 | **Cao cấp trong app** — khung ảnh, lịch trình AI nâng cao, hồ sơ du lịch in được | Mua trong ứng dụng qua Google Play Billing. | Google Play thu phí nền tảng; doanh nghiệp để nhận tiền. | Tỷ lệ chuyển đổi thấp ở người dùng trẻ |
| 4 | **B2B/B2G** — tỉnh, trung tâm xúc tiến du lịch, thương hiệu tài trợ "thử thách con dấu" | Bán chiến dịch: "đóng dấu 5 điểm ở tỉnh X nhận quà". | Hợp đồng dịch vụ quảng cáo; doanh nghiệp. | Chu kỳ bán dài; cần số người dùng thật để chào |
| 5 | **Phí sàn dịch vụ bản địa** — HDV địa phương, người đi kèm, gửi đồ, thuê xe | Đặt và thanh toán trong app, Levart giữ tỷ lệ. | Nền tảng TMĐT trung gian theo Luật TMĐT 2025 `[L-06]`; nếu Levart **tự tổ chức** chương trình du lịch → giấy phép lữ hành + ký quỹ 100 triệu (nội địa) `[L-05]`; HDV phải có thẻ theo Luật Du lịch 2017. | Nặng nhất về pháp lý, tin cậy, an toàn → **giai đoạn 4+** |

**Kết luận:** 90 ngày đầu **không bật dòng thu nào**. Quý 2 thử **1** (link ra ngoài, đo lượt bấm
trước, doanh thu sau) và **2** (đặt trước hộ chiếu in để kiểm G4). Dòng 5 là đích xa và là lý do
chính phải có pháp nhân, không phải điểm bắt đầu.

### 5.2 "Lợi ích lớn hơn tiền bỏ ra" — đây là việc của sản phẩm, đúng như nhóm nhận định

| Nguyên lý | Nguồn | Áp vào Levart |
|---|---|---|
| Khách mua "cái lỗ", không mua "mũi khoan" | Levitt `[S-05]` | Không bán "app có AI"; bán "chuyến đi Hà Giang 3 ngày xong kế hoạch trong 10 phút, có 4 con dấu mang về" |
| Bán theo phía cầu: hiểu lực **đẩy** (khó chịu hiện tại), lực **kéo** (hình ảnh tương lai), **lo lắng** và **thói quen** giữ họ lại | Moesta `[S-47]` | Lo lắng lớn nhất khi đi tự túc là an toàn và bị "chặt chém" → trang khẩn cấp và giá tham khảo từ người đi thật là lý lẽ bán hàng, không chỉ là tính năng |
| Định giá trước khi xây, không xây xong rồi mới hỏi giá | Ramanujam & Tacke `[S-48]` | Trang đặt trước hộ chiếu in với giá thật ở quý 2 |
| Người ta sợ mất hơn thích được (loss aversion) | Kahneman & Tversky `[S-49]` | Thông điệp "đừng để chuyến đi trôi mất" mạnh hơn "hãy lưu kỷ niệm" |
| Chứng minh giá trị bằng số ngay trong app | — | Sau mỗi chuyến, Levart hiện "chuyến này bạn tiết kiệm X giờ lên kế hoạch, Y đồng nhờ ưu đãi đối tác, +N con dấu" — đây là **tính năng** phục vụ **bán hàng** |

**Kinh tế đơn vị** (điền dần, không bịa): chi phí có được một người dùng theo kênh (CAC), doanh thu
trung bình trên người dùng (ARPU), thời gian sống. Mốc ngành để so: **LTV ≥ 3 × CAC** `[S-50]`. Ở
quý 1 con số duy nhất đo được là **CAC bằng thời gian** (giờ công của nhóm trên mỗi người dùng kích
hoạt) — cứ ghi, đó là dữ liệu thật.

---

## 6. Vận hành và đối tác — Levart làm gì, đối tác làm gì

### 6.1 Quy tắc quyết định: Tự xây – Hợp tác – Tổng hợp thông tin

Coase (1937) giải thích doanh nghiệp tự làm một việc khi chi phí giao dịch với bên ngoài cao hơn chi
phí tự làm `[S-51]`. Moore tách **cốt lõi** (tạo khác biệt) khỏi **bối cảnh** (phải có nhưng không tạo
khác biệt) `[S-10]`. Quy tắc cho Levart:

- **Tự xây** chỉ những gì tạo tài sản đặc trưng: con dấu GPS, hộ chiếu, lịch trình, dữ liệu địa điểm
  bản địa do cộng đồng góp.
- **Hợp tác** mọi thứ cần giấy phép, vốn, hoặc gánh trách nhiệm pháp lý: lưu trú, vận chuyển, bảo hiểm,
  hướng dẫn viên, cấp cứu.
- **Tổng hợp thông tin** từ nguồn được phép: Google Places API **theo đúng điều khoản** (mục 6.3), dữ
  liệu mở của cơ quan du lịch, đóng góp của người dùng.

### 6.2 Từng loại đối tác trong danh sách của nhóm

| Loại | Vai trò của Levart | Mô hình tiền | Rủi ro / pháp lý | Giai đoạn |
|---|---|---|---|---|
| Khách sạn, homestay | Gợi ý + link | Affiliate | Thấp | Q2 |
| Nhà hàng, món ăn | Tổng hợp (Places API) + gợi ý từ check-in thật; RAG sau khi có dữ liệu riêng | Tài trợ/nổi bật (sau) | Trả tiền để "nổi bật" làm hỏng lòng tin — nếu làm, phải ghi rõ "được tài trợ" | Q1 (thông tin), Q3+ (tiền) |
| Phương tiện: xe khách, tàu, máy bay | Link đặt vé | Affiliate | Thấp | Q2 |
| Thuê xe máy tại điểm đến | Danh bạ đối tác đã kiểm chứng theo tỉnh | Giới thiệu | Tai nạn xe máy là rủi ro lớn nhất với người trẻ đi tự túc (**chưa kiểm chứng bằng số liệu**) → nội dung an toàn, khuyến cáo bằng lái, bảo hiểm | Q2–Q3 |
| Gửi đồ | Đối tác địa phương (quán cà phê, homestay) | Giới thiệu | Mất đồ → trách nhiệm phải nằm ở hợp đồng với đối tác, không ở Levart | Q3 |
| Dịch vụ khẩn cấp | **Thông tin + điều hướng**, không cung cấp dịch vụ | — | Levart không phải nhà cung cấp; ghi rõ trong điều khoản | **Q1** |
| Bảo hiểm du lịch | Link tới kênh bán online của hãng, hoặc làm đại lý tổ chức | Affiliate / hoa hồng đại lý | Đại lý bảo hiểm là ngành có điều kiện theo Luật Kinh doanh bảo hiểm 2022 — **hỏi luật sư trước khi nhận hoa hồng đại lý**; link giới thiệu thuần thì nhẹ hơn (**chưa kiểm chứng**) | Q3 |
| Hướng dẫn viên, người đi kèm bản địa | Ghép tay cho vài nhóm trước; sàn sau | Phí sàn | HDV cần thẻ; ghép người lạ cần xác thực, quy tắc, cơ chế báo cáo | Q4+ |
| Phiên dịch, nhà trẻ | Ngoài đầu cầu | — | — | Bãi đỗ |
| Quần áo, dụng cụ | Link sàn TMĐT | Affiliate | Thấp | Q2 |

### 6.3 "Cào dữ liệu Google Maps" — không được, và cách làm đúng

Điều khoản Google Maps Platform cấm **scraping** và cấm **lưu đệm** nội dung Maps ngoài các ngoại lệ
ghi rõ; ngoại lệ: **Place ID được lưu vô thời hạn**, **toạ độ được lưu đệm tối đa 30 ngày liên tục**
`[P-01]` `[P-02]`. Vậy thiết kế đúng cho tính năng "agent tự tìm dịch vụ gần đó":

1. Gọi **Places API** theo ngữ cảnh (gần biển, gần điểm check-in), chỉ **lưu Place ID** vào danh mục
   địa điểm riêng của Levart cùng dữ liệu **do Levart tự tạo** (ghi chú của người dùng, ảnh check-in,
   đánh giá nội bộ).
2. Mỗi lần hiển thị, lấy lại chi tiết từ API bằng Place ID — không sao chép đánh giá, ảnh của Google
   vào máy chủ mình.
3. Phần "tự gọi điện cho đối tác": gọi/nhắn tự động tới doanh nghiệp là hành vi chịu điều chỉnh của quy
   định chống cuộc gọi/tin nhắn rác (Nghị định 91/2020/NĐ-CP — **đối chiếu lại**); đối tác phải đồng
   ý trước. Trong 2 quý đầu, **người sáng lập gọi tay** — vừa hợp pháp, vừa là phỏng vấn khách hàng
   phía cung (G3).

### 6.4 Tình huống, ngoại lệ, tai nạn — bảng khởi đầu

Shostack đề xuất vẽ **bản thiết kế dịch vụ** (service blueprint): hành động của khách ↔ hành động
nhìn thấy của ta ↔ hành động sau hậu trường ↔ hệ thống hỗ trợ `[S-52]`. Klein đề xuất **pre-mortem**:
giả định dự án đã thất bại, mỗi người viết lý do trong 2 phút `[S-22]`. Bảng dưới là điểm xuất phát
cho trang "Khẩn cấp" và cho sổ rủi ro:

| Nhóm | Tình huống | Levart làm gì trong app | Ranh giới trách nhiệm |
|---|---|---|---|
| Sức khoẻ | Tai nạn xe máy; ngộ độc; say nắng; đuối nước; dị ứng | Nút gọi **115**; bệnh viện/trạm y tế gần nhất từ Places; thẻ "người liên hệ khẩn cấp" + nhóm máu tự khai; chia sẻ vị trí cho người thân | Chỉ thông tin; không tư vấn y tế |
| An ninh | Mất giấy tờ, mất điện thoại, cướp giật, lừa giá | Nút gọi **113**; hướng dẫn trình báo; mẹo giá tham khảo từ người đi thật | Không can dự tranh chấp |
| Thiên tai, giao thông | Mưa lũ, sạt lở (đặc biệt vùng núi phía Bắc mùa mưa); lỡ xe/tàu/máy bay; hết phòng | Cảnh báo thời tiết thật (app chưa có, đang trong lộ trình sản phẩm); danh bạ nhà xe; gợi ý phương án B | Không bảo đảm dịch vụ đối tác |
| Cháy nổ, cứu nạn | Cháy homestay; lạc khi trekking | Nút gọi **114**; gửi toạ độ | — |
| Pháp lý khi đi | Không mang căn cước → không nhận phòng; bay drone nơi cấm; vào khu biên giới cần giấy phép | Nhắc trước chuyến đi theo điểm đến (**nội dung cần luật sư/địa phương xác nhận**) | Thông tin tham khảo |
| Số | Mất mạng, hết pin | Trang khẩn cấp **hoạt động offline**; bài đăng lúc mất mạng tự gửi khi có mạng (đã làm) | — |

Ba số khẩn cấp quốc gia 113 (công an), 114 (cứu hoả, cứu nạn), 115 (cấp cứu) là dữ kiện phổ thông;
số đường dây nóng du lịch theo tỉnh **phải tra và kiểm chứng từng số** trước khi đưa vào app.

### 6.5 "Đi theo cặp / chùm" — ghép người: làm tay trước, xây sau

- **Cầu:** chưa có bằng chứng (G6). Hỏi trong 30 cuộc phỏng vấn trước.
- **Cách thử rẻ:** nhóm tự ghép **một** chuyến đi chung cho 6–8 người quen-của-quen qua Zalo, quan
  sát họ cần gì để tin nhau (Graham `[S-40]`; Spinks về xây cộng đồng từ nhóm nhỏ `[S-53]`).
- **Nếu xây thành tính năng:** tối thiểu phải có xác thực số điện thoại (cũng là yêu cầu của Nghị định
  147 `[L-03]`), quy tắc cộng đồng, báo cáo/chặn (D1 trong `status.md`), tuỳ chọn nhóm cùng giới, chia
  sẻ vị trí. Levart là **người kết nối**, không **tổ chức** chuyến đi — nếu Levart lên chương trình và
  thu tiền chuyến đi thì rơi vào kinh doanh lữ hành `[L-05]`.

### 6.6 Phân loại loại du lịch — là công cụ phân khúc, không phải quyết định sản phẩm

Kotler gọi quy trình này là **phân khúc → chọn mục tiêu → định vị** (STP) `[S-54]`. Phân loại phải
**rút ra từ phỏng vấn**, không từ suy luận. Bản khởi đầu để hỏi: nghỉ dưỡng; khám phá văn hoá – ẩm
thực; trekking – mạo hiểm; check-in – chụp ảnh; chữa lành – wellness; lễ hội – sự kiện; phượt xe
máy; công tác kết hợp du lịch. Dữ liệu chuyến đi trong app đã có chỗ ghi "phong cách chuyến đi" —
sau phỏng vấn, chốt danh mục và dùng chính chỗ đó để đo phân khúc nào kích hoạt và quay lại nhiều nhất.

---

## 7. Pháp lý, thuế, kế toán — danh mục theo giai đoạn (Việt Nam, 09/2026)

> **Không phải tư vấn pháp lý.** Đây là danh sách câu hỏi kèm căn cứ để hỏi luật sư và kế toán cho
> đúng chỗ. Mỗi dòng ghi **điều gì kích hoạt nghĩa vụ**, để nhóm biết **khi nào** phải làm, không làm
> sớm tốn tiền, không làm muộn bị phạt.

### 7.1 Ba luật mới áp trực tiếp lên Levart

| Văn bản | Hiệu lực | Áp lên Levart vì | Điều phải làm | Nguồn |
|---|---|---|---|---|
| **Luật Bảo vệ dữ liệu cá nhân 91/2025/QH15** (thay Nghị định 13/2023) | 01/01/2026 | Levart thu **vị trí GPS** (Nghị định 13 xếp vị trí qua dịch vụ định vị là **dữ liệu nhạy cảm**; Luật 2025 kế thừa khái niệm này — **đối chiếu danh mục tại Luật**), ảnh, hồ sơ | Xin **đồng ý riêng** cho dữ liệu nhạy cảm; chính sách quyền riêng tư (cũng là D5 của Play); quyền xoá/sửa/rút đồng ý; **hồ sơ đánh giá tác động xử lý dữ liệu** gửi Bộ Công an (**thời hạn và mẫu theo nghị định hướng dẫn — hỏi luật sư**); **không** yêu cầu người dùng gửi ảnh/video giấy tờ tuỳ thân để xác thực | `[L-01]` `[L-02]` |
| **Nghị định 147/2024/NĐ-CP** về Internet và thông tin trên mạng | 25/12/2024 | Levart có bảng tin, bình luận, theo dõi → là **mạng xã hội** theo nghĩa pháp lý | Dưới ngưỡng (**< 10.000 lượt truy cập/tháng và < 1.000 người dùng thường xuyên/tháng**): chỉ **thông báo**; từ ngưỡng: **giấy phép**, và chủ thể được cấp phải là **doanh nghiệp thành lập theo pháp luật Việt Nam**, có tên miền đăng ký. **Chỉ mạng xã hội có giấy phép mới được livestream và có hoạt động phát sinh doanh thu.** Phải **xác thực tài khoản bằng số điện thoại** (hoặc số định danh); chỉ tài khoản đã xác thực mới được đăng bài/bình luận (**đối chiếu điều khoản cụ thể**). | `[L-03]` `[L-04]` |
| **Luật Thương mại điện tử 122/2025/QH15** | 01/07/2026 | Nếu Levart có **chức năng đặt hàng** hoặc đứng trung gian giao dịch (dòng thu 5) | Nghĩa vụ nền tảng trung gian: định danh người bán, lưu dữ liệu hợp đồng **≥ 3 năm** (≥ 1 năm với doanh nghiệp siêu nhỏ, startup, hộ kinh doanh), trách nhiệm khi hàng không đúng mô tả. Link ra ngoài kiểu affiliate **có thể** không thuộc phạm vi — hỏi luật sư. | `[L-06]` |

**Hệ quả trực tiếp cho sản phẩm hiện tại:**
- **Chế độ khách (ẩn danh) hiện được phép đăng bài và bình luận** — kiểm chứng 15/09/2026 trên luật
  truy cập máy chủ: mọi tài khoản đã đăng nhập, kể cả ẩn danh, đều được tạo bài. Khi vượt ngưỡng
  Nghị định 147, điều này trái yêu cầu xác thực → **chính sách kinh doanh cần chốt: khách chỉ xem,
  muốn đăng phải xác thực số điện thoại.** Giao nhóm sản phẩm như một việc; không bàn kỹ thuật ở đây.
- **Bật bất kỳ dòng thu nào** = phải có doanh nghiệp + xét giấy phép MXH trước. Đây là **mốc chuyển
  pháp nhân**, không phải "khi nào tiện".

### 7.2 Danh mục theo giai đoạn

| Giai đoạn | Kích hoạt khi | Việc bắt buộc | Việc nên làm | Căn cứ |
|---|---|---|---|---|
| **0 — Nhóm bạn góp sức** (hiện tại) | Có ≥ 2 người cùng làm | **Thoả thuận đồng sáng lập**: vai trò, chia phần (Slicing Pie phù hợp khi đóng góp thời gian không đều và bán thời gian), điều kiện rời đi, **chuyển giao quyền sở hữu trí tuệ** (code, tên, hình ảnh) cho dự án | **Tra cứu nhãn hiệu "Levart"** trên Cục Sở hữu trí tuệ và WIPO trước khi in bất cứ gì; mua tên miền `.vn` và `.com`; sổ quỹ chung (Google Sheet) | `[S-26]` `[S-27]` |
| **1 — Có người dùng thật, chưa doanh thu** | Người ngoài nhóm cài app | Chính sách quyền riêng tư + Data safety (D5); đồng ý riêng cho vị trí; báo cáo/chặn nội dung (D1); trang web yêu cầu xoá tài khoản (D7); **thông báo** cung cấp MXH nếu áp dụng ở ngưỡng thấp (**hỏi luật sư có cần khi < 1.000 người**) | Đăng ký nhãn hiệu (nhóm 9, 35, 39, 41, 42 — **xác nhận với đại diện SHTT**); **Play Console**: tài khoản cá nhân lập sau 13/11/2023 phải chạy thử nghiệm kín **12 người liên tục 14 ngày** trước khi phát hành — biến 12 người này thành cohort phỏng vấn | `[L-01]` `[L-03]` `[P-03]` `[P-04]` `[P-05]` |
| **2 — Bắt đầu có doanh thu** | Bấm nút bật affiliate / bán hộ chiếu in | **Thành lập doanh nghiệp** (TNHH hai thành viên trở lên nếu 2–4 người; cổ phần nếu định gọi vốn và ESOP — hỏi luật sư); mã ngành gợi ý: lập trình (6201), cổng thông tin (6312), quảng cáo (7310), bán lẻ qua Internet (4791), đại lý du lịch (7911) — **không** đăng ký điều hành tour (7912) khi chưa định ký quỹ; hoá đơn điện tử; **kế toán thuê ngoài**; giấy phép MXH nếu có doanh thu từ nền tảng; xác thực số điện thoại | Hưởng **miễn thuế TNDN 3 năm** cho doanh nghiệp nhỏ và vừa kể từ ngày cấp đăng ký lần đầu (Nghị quyết 198/2025/QH15 — **kế toán xác nhận điều kiện**); **lệ phí môn bài đã bỏ từ 01/01/2026**. **Hộ kinh doanh không phù hợp**: không được cấp phép MXH, không nhận đầu tư, và từ 2026 phải kê khai theo doanh thu thật như doanh nghiệp | `[L-03]` `[L-07]` |
| **3 — Sàn dịch vụ, tự tổ chức trải nghiệm** | Thanh toán trong app cho dịch vụ bên thứ ba | Nghĩa vụ nền tảng TMĐT trung gian; nếu tự tổ chức chương trình du lịch: **giấy phép lữ hành nội địa + ký quỹ 100 triệu đồng** (250/500 triệu cho quốc tế); HDV có thẻ; đại lý bảo hiểm có điều kiện | Điều khoản dịch vụ ba bên; bảo hiểm trách nhiệm | `[L-05]` `[L-06]` |
| **4 — Gọi vốn, mở rộng** | Có nhà đầu tư | Công ty cổ phần; hợp đồng đầu tư; ESOP; sở hữu trí tuệ đứng tên công ty | Chuẩn bị bản kế hoạch 10 slide theo khung Sequoia `[S-55]` | — |

### 7.3 Kế toán và thuế — tối thiểu ở từng giai đoạn

| Giai đoạn | Làm gì | Ai làm |
|---|---|---|
| 0–1 | Một **sổ quỹ** ghi mọi khoản chi (Firebase, Maps, tên miền, in ấn) và ai ứng — đây là dữ liệu cho Slicing Pie | Người điều phối |
| 2 | Kế toán dịch vụ theo tháng; hoá đơn điện tử; kê khai thuế GTGT và TNDN theo quý; theo dõi miễn TNDN 3 năm | Thuê ngoài |
| 3+ | Kế toán nội bộ khi giao dịch qua sàn đủ nhiều; hoá đơn cho từng giao dịch ba bên | Tuyển |

Nguồn hỗ trợ khởi nghiệp nên tìm hiểu (**chưa kiểm chứng điều kiện hiện tại**): hệ sinh thái Bách khoa
Hà Nội (BK Holdings, quỹ BK Fund), Techfest Vietnam (Bộ Khoa học và Công nghệ), Trung tâm Đổi mới
sáng tạo Quốc gia (NIC). Một cuộc nói chuyện với văn phòng hỗ trợ sinh viên khởi nghiệp của trường
là việc rẻ nhất để bắt đầu.

---

## 8. Hệ điều hành làm việc — trả lời câu hỏi "quy trình nghiệp vụ"

Đây là phần nhóm hỏi nhiều nhất: *muốn một cái gì đó có tổ chức, có quy trình về nghiệp vụ, để không
lộn xộn, không phải hỏi "đang làm gì, bắt đầu từ đâu, làm gì tiếp".* Câu trả lời không phải một quy
trình cho từng ý tưởng; là **một hệ điều hành** gồm 7 bộ phận, dùng chung cho mọi ý tưởng.

Gerber: người sáng lập phải **làm việc trên doanh nghiệp**, không chỉ **trong doanh nghiệp** — nghĩa
là dành thời gian cố định để xây hệ thống chứ không chỉ làm việc của hệ thống `[S-56]`. Wickman (EOS)
và Harnish (Rockefeller Habits) cùng kết luận: cái tạo ra trật tự là **nhịp** (tuần, quý, năm) và **một
trang kế hoạch** ai cũng thấy `[S-24]` `[S-57]`.

### 8.1 Bảy nguyên tắc

1. **Một nguồn sự thật**: repo này cho tài liệu; một thư mục Drive cho tệp nặng (ảnh phỏng vấn, bản
   thiết kế). Không có "bản trong máy tôi".
2. **Mọi việc bắt nguồn từ một giả thuyết hoặc một quyết định đã ghi**. Việc không truy được về đâu
   thì không làm.
3. **Nhịp cố định thắng nỗ lực ngẫu hứng**. Họp tuần không huỷ; nếu chỉ còn 2 người vẫn họp.
4. **Bằng chứng trước, cảm tính sau** — đã là văn hoá của nhóm ở phần code (`status.md`: "không dùng
   trí nhớ, cái gì cũng cần bằng chứng"); mở rộng nguyên văn sang phần kinh doanh.
5. **Chỉ số học được, không phải chỉ số phù phiếm**: số cài không phải kết quả; số người quay lại là
   kết quả (Ries gọi là *innovation accounting* `[S-04]`).
6. **Cửa hai chiều đi nhanh, cửa một chiều đi chậm** (Bezos): quyết định đảo được thì một người quyết
   trong ngày; quyết định không đảo được (tên, pháp nhân, chia phần, applicationId) thì viết ADR và
   đợi họp `[S-58]`.
7. **Xong là có bằng chứng**: một việc kinh doanh "xong" khi có tệp/ảnh/số trong repo, giống như một
   việc code "xong" khi build, cài, chạy thật.

### 8.2 Ba tầng nhịp

| Tầng | Chu kỳ | Việc | Đầu ra | Nguồn |
|---|---|---|---|---|
| **Quý** | 12 tuần | Rà 9 bản vẽ; đọc lại chiến lược (mục 4.1); chốt **3 Rocks** (mục tiêu quý — không hơn 3 khi nhóm ≤ 5 người); quyết định **tiếp / xoay / dừng** theo cổng giai đoạn (8.6) | Biên bản cổng quý; Rocks quý mới | Wickman `[S-24]`; OKR của Grove/Doerr `[S-21]` |
| **Chu kỳ 6 + 2 tuần** | 6 tuần làm, 2 tuần "nguội" (dọn dẹp, học, viết) | **Bàn cược**: chọn 1–3 "cược" đã được **định hình** (pitch 1 trang: vấn đề, khẩu vị thời gian, phác thảo giải pháp, hố sâu, không-làm). Mỗi cược là một **thẻ thí nghiệm** (Phụ lục B) | Cược xong hoặc **bị cắt** đúng hạn — không gia hạn | Shape Up của Singer `[S-59]` |
| **Tuần** | 60–90 phút, cố định ngày giờ | Cấu trúc **Level 10** (Wickman): 5' mở đầu · 5' bảng số · 5' Rocks · 5' tin từ khách/đối tác · 5' việc tuần trước · **60' Nhận diện–Thảo luận–Giải quyết** 3 vấn đề lớn nhất · 5' kết: việc tuần này, ai, hạn | Danh sách việc tuần (≤ 7 việc), có tên người | `[S-24]` |
| **Ngày** (bất đồng bộ) | 3 dòng trên nhóm chat | Hôm qua xong gì · hôm nay làm gì · vướng gì | — | — |

Vì sao **6 tuần** chứ không phải sprint 1–2 tuần: nhóm làm bán thời gian, một sprint 2 tuần chỉ có
≈ 10–20 giờ công/người, không đủ để xong một thí nghiệm có ý nghĩa; 6 tuần cho phép một cược thật
(ví dụ: "30 người dùng thật đóng ≥ 1 con dấu và 30% quay lại sau 2 tuần"). Khẩu vị thời gian **cố
định**, phạm vi **co lại** — ngược với cách làm thông thường.

### 8.3 Dòng công việc chuẩn — từ ý tưởng đến tài liệu

```
Ý tưởng ──▶ Bãi đỗ (Phụ lục F)            mọi ý đều vào đây, 1 dòng, không tranh luận
   │
   ▼  (ở bàn cược, nếu được chọn)
Định hình ──▶ Pitch 1 trang                vấn đề · khẩu vị · phác thảo · hố sâu · không-làm
   │
   ▼
Giả thuyết ──▶ Thẻ thí nghiệm (Phụ lục B)   "Chúng tôi tin X. Sẽ đúng nếu đo được Y ≥ Z trong T tuần."
   │
   ▼
Chạy ──▶ Xây / phỏng vấn / thử tay          nhật ký hằng ngày 3 dòng
   │
   ▼
Đo ──▶ Bảng số tuần                          số thật, có ảnh chụp / truy vấn máy chủ / bản ghi phỏng vấn
   │
   ▼
Học ──▶ Quyết định (Phụ lục D)               tiếp · sửa · bỏ — ghi ngày, lý do, bằng chứng
   │
   ▼
Tài liệu ──▶ cập nhật bản vẽ liên quan       Lean Canvas / persona / sổ rủi ro / status.md
```

Đây chính là vòng **Xây – Đo – Học** của Ries `[S-04]`, thêm bước "Định hình" của Shape Up để không
xây thứ chưa nghĩ kỹ, và bước "Tài liệu" để trả lời đề xuất của nhóm về báo cáo và tổng hợp tài liệu:
**tài liệu là đầu ra của vòng lặp, không phải một việc riêng.**

### 8.4 Cấu trúc thư mục — một chỗ cho mọi thứ

Đề xuất theo tinh thần PARA của Forte (sắp theo **khả năng hành động**, không theo chủ đề) `[S-60]`:

```
app_travel/
├─ Business.md                 ← tệp này: chiến lược, mô hình, luật, hệ điều hành
├─ status.md                   ← sự thật về sản phẩm (giữ nguyên)
├─ database_*.mmd
└─ docs/
   ├─ 00_quyet-dinh/           ← mỗi quyết định một tệp: 2026-09-15-ten-quyet-dinh.md (Phụ lục D)
   ├─ 01_khach-hang/           ← nhật ký phỏng vấn (1 tệp/cuộc), persona, kịch bản hỏi (Phụ lục A)
   ├─ 02_thi-nghiem/           ← thẻ thí nghiệm + kết quả (Phụ lục B)
   ├─ 03_tang-truong/          ← lịch nội dung, danh sách kênh Bullseye, creator, số theo kênh
   ├─ 04_doi-tac/              ← bảng đối tác (tên, tỉnh, người liên hệ, trạng thái, ghi chú cuộc gọi)
   ├─ 05_phap-ly-tai-chinh/    ← thoả thuận đồng sáng lập, sổ quỹ, danh mục luật, câu hỏi cho luật sư
   ├─ 06_hop/                  ← biên bản họp tuần (1 tệp/tuần), biên bản cổng quý
   ├─ 07_bai-do/               ← bãi đỗ ý tưởng (1 tệp, thêm dòng)
   └─ 99_luu-tru/              ← thứ đã xong hoặc đã bỏ
```

Quy tắc đặt tên: `YYYY-MM-DD-ten-ngan.md`. Tệp nặng (ghi âm, ảnh) để Drive, dán link.

### 8.5 Ghế và trách nhiệm — kể cả khi chỉ có 2–4 người

Wickman: vẽ **ghế** trước, xếp **người** sau; một người có thể ngồi nhiều ghế, nhưng **mỗi ghế chỉ
một người chịu trách nhiệm cuối** `[S-24]`. Wasserman: cãi nhau về vai trò và chia phần là nguyên
nhân tan nhóm hàng đầu; giải quyết **trước** khi có gì để chia `[S-26]`.

| Ghế | Chịu trách nhiệm cuối về | Lớp (mục 1) | Đầu ra hằng tuần |
|---|---|---|---|
| **Người cầm tầm nhìn** | Chiến lược, định vị, quan hệ lớn, "revolutions" | L1 | Không có việc tuần cố định; sở hữu cổng quý |
| **Người điều phối** | Nhịp họp, bảng số, sổ quyết định, tiền, luật; nói "không" với việc ngoài Rocks | L6 | Bảng số + biên bản họp |
| **Sản phẩm / Kỹ thuật** | `status.md`, chất lượng, phát hành | L3 | Việc kỹ thuật theo chu kỳ |
| **Tăng trưởng** | Phỏng vấn, nội dung, kênh, cộng đồng | L2, L4 | Số phỏng vấn, số nội dung, số kích hoạt |
| **Vận hành / Đối tác** | Danh sách đối tác, khẩn cấp, CS | L5 | Số cuộc gọi đối tác, phản hồi người dùng |

Ma trận **RACI** (ai Làm, ai Chịu trách nhiệm, ai được Hỏi, ai được Báo) cho từng Rock — 1 bảng nhỏ ở
đầu mỗi quý.

### 8.6 Giai đoạn và cổng — biết mình đang ở đâu

Croll & Yoskovitz chia đời startup thành **5 giai đoạn**, mỗi giai đoạn chỉ có **một chỉ số quan trọng
nhất** (One Metric That Matters); đổi chỉ số trước khi qua cổng là tự lừa mình `[S-19]`.

| Giai đoạn | Câu hỏi | Chỉ số duy nhất | Cổng để sang giai đoạn kế (nháp — nhóm chốt số ở tuần 2) | Levart hiện ở |
|---|---|---|---|---|
| **1. Thấu cảm** | Vấn đề có thật không, với ai? | Số phỏng vấn có bài học ghi lại | ≥ 30 cuộc; ≥ 60% nêu cùng một nỗi đau không cần gợi; persona v1 | **Đây** |
| **2. Bám dính** | Người ta có quay lại không? | Tỷ lệ quay lại sau 7/30 ngày của cohort | Khảo sát Sean Ellis: ≥ 40% trả lời "rất thất vọng nếu không còn Levart" `[S-61]`; quay lại 30 ngày đạt ngưỡng nhóm tự đặt sau cohort đầu | — |
| **3. Lan truyền** | Người dùng có kéo người dùng không? | Hệ số mời (K) hoặc % cài từ chia sẻ con dấu | K đo được và tăng 3 chu kỳ liên tiếp | — |
| **4. Doanh thu** | Có ai trả tiền không, lời không? | ARPU, LTV/CAC | LTV ≥ 3 × CAC trên ít nhất một kênh `[S-50]` | — |
| **5. Mở rộng** | Nhân lên được không? | Tăng trưởng theo kênh/tỉnh | — | — |

Andreessen: thứ duy nhất quan trọng là **khớp sản phẩm – thị trường**; trước đó mọi thứ khác (đội,
marketing, quy trình) đều thứ yếu `[S-62]`. Balfour mở rộng thành **bốn khớp** phải cùng đúng: thị
trường–sản phẩm, sản phẩm–kênh, kênh–mô hình, mô hình–thị trường `[S-63]` — hữu ích khi bàn affiliate
(mô hình biên mỏng chỉ khớp với kênh rẻ như cộng đồng, không khớp với KOL trả tiền).

### 8.7 Bảng số tuần — phễu AARRR gắn vào sự kiện thật

McClure đề xuất 5 tầng phễu `[S-20]`. App đã có công cụ đo hành vi người dùng (tích hợp từ
28/08/2026), nên mỗi tầng phải là **một hành vi đo được**, không phải ước lượng; đặt tên kỹ thuật cho
từng hành vi là việc của nhóm sản phẩm:

| Tầng | Định nghĩa kinh doanh cho Levart | Câu hỏi kinh doanh | Ghi chú |
|---|---|---|---|
| **Acquisition** | Mở app lần đầu, biết tới từ kênh nào | Kênh nào mang người tới? | Không phải chỉ số kết quả |
| **Activation** | **Đóng con dấu đầu tiên có ảnh** trong 7 ngày | Bao nhiêu % người tải chạm được giá trị? | Đây là "khoảnh khắc giá trị" |
| **Retention** | Mở lại và đóng dấu/xem hộ chiếu sau 7 và 30 ngày | Người ta có quay lại không? | Chỉ số của giai đoạn 2 |
| **Referral** | Chia sẻ con dấu/hộ chiếu ra ngoài; bạn bè cài theo | Người dùng có kéo người dùng không? | Chỉ số của giai đoạn 3 |
| **Revenue** | Bấm sang đối tác; mua | Có ai trả tiền không? | Giai đoạn 4 |

**Chỉ số Sao Bắc Đẩu đề xuất:** *số con dấu có ảnh được đóng mỗi tuần* — vì nó gắn với giá trị cốt lõi,
tăng khi cả kích hoạt và quay lại tăng, và không thể "mua" bằng quảng cáo.

**Khảo sát khớp sản phẩm–thị trường** (Sean Ellis): sau 2–4 tuần dùng, hỏi *"Bạn sẽ cảm thấy thế nào
nếu không còn dùng được Levart?"* với 4 lựa chọn; ≥ 40% chọn "rất thất vọng" là dấu hiệu khớp `[S-61]`.
Gửi cho cohort đầu ở tuần 10–12.

### 8.8 Định nghĩa "xong" cho việc kinh doanh

| Loại việc | Xong khi |
|---|---|
| Phỏng vấn | Có tệp trong `01_khach-hang/`, có ≥ 3 câu trích nguyên văn, có 1 dòng "học được gì" |
| Thí nghiệm | Thẻ có mục "Kết quả" điền số thật + link bằng chứng, và có quyết định tiếp/sửa/bỏ |
| Cuộc gọi đối tác | Dòng trong bảng đối tác có trạng thái + ghi chú + việc kế tiếp |
| Quyết định | Tệp ADR có ngày, bối cảnh, lựa chọn đã cân, lý do, hệ quả |
| Nội dung | Đã đăng, có link, có số sau 7 ngày ghi vào `03_tang-truong/` |

---

## 9. Lộ trình 90 ngày đầu

Mục tiêu duy nhất của 90 ngày: **qua cổng giai đoạn 1 (Thấu cảm) và có cohort đầu cho giai đoạn 2.**
Không phải phát hành, không phải doanh thu.

### 9.1 Ba chu kỳ

| Tuần | Chu kỳ | Cược (≤ 3) | Bằng chứng xong |
|---|---|---|---|
| **1–2** | Nền móng | (1) Thoả thuận đồng sáng lập + sơ đồ ghế; (2) 9 bản vẽ ở mức v0 (Lean Canvas, tầm nhìn, sổ rủi ro, sổ quyết định, thư mục); (3) kịch bản phỏng vấn + danh sách 40 người sẽ hỏi | Tệp trong `05_`, `00_`, `01_`; lịch họp tuần đã diễn ra 2 lần |
| **3–6** | Thấu cảm | (1) **30 phỏng vấn** khách trong đầu cầu; (2) **5 cuộc nói chuyện** với chủ homestay/quán ở 1 tỉnh mục tiêu; (3) bảng so sánh đối thủ thật (cài, dùng, chụp màn hình 6 app) + 1 buổi với luật sư về Nghị định 147 và Luật BVDLCN | Persona v1; JTBD v1; quyết định đầu cầu (ADR); danh mục pháp lý v1; Lean Canvas v1 |
| **7–12** | Cohort đầu | (1) Thử nghiệm kín trên Play với **≥ 12 người ≥ 14 ngày** (đồng thời thoả yêu cầu Play `[P-03]`) mở rộng lên **30–50 người** qua CLB/nhóm lớp; đo kích hoạt và quay lại; (2) **10 nội dung ngắn** "đi đủ 34 tỉnh" trên 1–2 kênh, đo cài từ kênh; (3) nhóm sản phẩm chỉ làm việc kỹ thuật **chặn thí nghiệm** — con dấu phải theo người dùng sang máy khác, có cơ chế báo cáo nội dung xấu, có chính sách riêng tư, đo được phễu 8.7 (chi tiết kỹ thuật nằm trong `status.md`) | Bảng số 6 tuần; khảo sát Sean Ellis; biên bản cổng quý 1 |

### 9.2 Cổng quý 1 — ba lối ra

| Kết quả | Quyết định | Quý 2 làm gì |
|---|---|---|
| ≥ 40% "rất thất vọng"; quay lại 30 ngày đạt ngưỡng đã đặt; ≥ 3 đối tác đồng ý thử | **Tiếp** | Giai đoạn 2→3: mở rộng cohort lên 1 trường; thử dòng thu 1 và 2; chuẩn bị pháp nhân |
| Người ta thích lịch trình nhưng không đóng dấu, hoặc ngược lại | **Xoay** | Viết lại Lean Canvas quanh việc-cần-làm đã được xác nhận; giữ đầu cầu |
| < 20% "rất thất vọng"; không ai quay lại; đối tác không quan tâm | **Dừng hoặc đổi đầu cầu** | Tổng kết bài học; quyết định trung thực. Đây không phải thất bại — đây là mục đích của giai đoạn tìm kiếm |

### 9.3 Không làm trong 90 ngày — và vì sao

| Việc | Vì sao chưa |
|---|---|
| Đăng ký công ty | Chưa có doanh thu, chưa vượt ngưỡng MXH; lập sớm là gánh kế toán hằng tháng mà chưa có gì để hạch toán. Kích hoạt ở giai đoạn 2 (mục 7.2) |
| Ký quỹ lữ hành, xin phép MXH | Chưa có hành vi kích hoạt nghĩa vụ |
| Thuê KOL trả tiền | Chưa có sản phẩm được chứng minh giữ người → tiền đổ vào rổ thủng (mục 4.4) |
| Chatbot riêng | Chưa biết người dùng hỏi gì; Gemini trong app đã đủ để học; xây riêng là tối ưu thứ chưa đo |
| Agent tự gọi đối tác | Chưa có 3 đối tác thật; cào Maps bị cấm; gọi tự động cần đồng ý (mục 6.3) |
| Web-hoá, đa nền tảng | Đầu cầu dùng Android/iOS; web chỉ cần **trang tĩnh** cho chính sách riêng tư (D5), xoá tài khoản (D7), và tên miền cho hồ sơ MXH — làm trang tĩnh, không làm web app |
| Ghép nhóm đi chung | Chưa có bằng chứng cầu (G6); nếu có, ghép tay trước |
| Tối ưu hiệu năng, chuẩn bị quy mô lớn | Máy chủ mới có 11 tài khoản — chưa có gì để chống tải. Việc kỹ thuật rẻ, không đổi dữ liệu thì làm khi rảnh; phần quy mô lớn đợi có tải thật (chi tiết trong `status.md`) |

---

## 10. Bảng đối chiếu — từng mục nhóm đã nêu

| Mục trong danh sách của nhóm | Lớp | Đây là | Giai đoạn | Việc nhỏ nhất đầu tiên | Phương pháp / nguồn |
|---|---|---|---|---|---|
| Tối ưu, test, bảo trì, deploy | L3 | Việc kỹ thuật | Liên tục | Không bàn trong tài liệu này; nhóm sản phẩm theo lộ trình riêng | `status.md` |
| Expand limit: bộ nhớ, tốc độ, số người | L3 | Việc tương lai | Giai đoạn 5 | Không làm gì cho tới khi có > 1.000 người dùng thật | `status.md` mục 7.3 |
| Web-hoá | L3 | GT | Quý 3+ | Chỉ một trang web tĩnh: chính sách riêng tư, xoá tài khoản, tên miền cho hồ sơ pháp lý | Mục 9.3 |
| Dữ liệu consistent | L3 | Việc kỹ thuật | Trước cohort đầu | Yêu cầu kinh doanh duy nhất: con dấu phải theo người dùng sang máy khác | `status.md` nhóm B |
| Work together | L6 | Hệ thống | Tuần 1 | Họp tuần đầu tiên; thư mục 8.4 | Mục 8 |
| AI model key nào | L3 | Quyết định có yếu tố tiền và pháp lý | Quý 1 | Yêu cầu kinh doanh: khoá dịch vụ AI không được để người ngoài dùng chùa — chi phí phát sinh không trần; cách làm là việc của nhóm sản phẩm | `status.md` mục 7.2 |
| UI/UX | L3 | Việc | Sau phỏng vấn | Không sửa giao diện trước khi có 30 phỏng vấn — sửa theo nỗi đau đã nghe | Torres `[S-64]` |
| Chatbot | L3 | GT | Quý 3+ | Ghi lại 100 câu người dùng hỏi thật trước | Mục 9.3 |
| Multiple app integrate | L3 | GT | Quý 4+ | "Tích hợp" duy nhất cần bây giờ là chia sẻ con dấu ra mạng xã hội | — |
| Giao diện, function | L3 | Việc | Theo cược | Mỗi chức năng phải truy về một việc-cần-làm ở 2.3 | Cagan `[S-65]` |
| Slogan | L1 | GT | Quý 1 | Thử 3 câu với 20 người (3.1) | Kawasaki, Heath, Neumeier |
| Tầm nhìn | L1 | Bản vẽ | Tuần 1 | Viết v0 theo 3.1, ký tên | Collins & Porras, Sinek |
| Tập khách hàng | L2 | GT | Tuần 1–6 | Chốt đầu cầu (2.5), xác nhận qua phỏng vấn | Aulet, Moore |
| Marketing app on platform | L4 | GT | Quý 2 | Tối ưu trang Play (ảnh, mô tả có con dấu) sau khi có định vị v1 | Bullseye |
| Marketing affiliate | L4/L5 | GT | Quý 2 | Đọc điều khoản 3 chương trình affiliate; đo lượt bấm trước | Mục 5.1 |
| Accountant | L6 | Việc | Giai đoạn 2 | Sổ quỹ ngay bây giờ | Mục 7.3 |
| Tax | L6 | Việc | Giai đoạn 2 | Hỏi kế toán về miễn TNDN 3 năm và TNCN affiliate | `[L-07]` |
| Insurance (3rd party?) | L5 | GT | Quý 3 | Đúng — bên thứ ba. Hỏi luật sư về đại lý vs giới thiệu | Mục 6.2 |
| Sales: lợi ích > tiền | L2 | Nguyên lý sản phẩm | Quý 2 | Tính năng "chuyến này bạn tiết kiệm…" | Levitt, Moesta, Ramanujam |
| Chăm sóc khách hàng, hỏi đáp, hỗ trợ | L5 | Việc | Quý 1 | Người sáng lập trả lời tay mọi phản hồi; ghi 20 câu hỏi hay gặp → FAQ | Graham `[S-40]` |
| Tình huống / tai nạn | L5 | Bản vẽ | Quý 1 | Bảng 6.4 → trang Khẩn cấp offline | Shostack, Klein |
| Policy, pháp lý | L6 | Danh mục | Quý 1 (buổi luật sư), Giai đoạn 2 (pháp nhân) | Mục 7 | `[L-01..07]` |
| Identity | L1/L6 | Ba nghĩa | Tuần 1–6 | **Thương hiệu**: tra nhãn hiệu Levart. **Pháp nhân**: giai đoạn 2. **Người dùng**: xác thực SĐT theo NĐ 147, không thu ảnh giấy tờ | Mục 4.4, 7 |
| Talent, KOL, agency, ý tưởng mới | L4 | Năng lực tương lai | Quý 3+ | Micro-creator đổi sản phẩm; chương trình creator sau | Mục 4.4 |
| Revolutions (đổi hướng app) | L1 | Quy trình | Cổng quý | Chỉ đề xuất ở cổng quý kèm bằng chứng | Mục 8.2 |
| 3rd party — toàn bộ danh sách | L5 | GT | Q1–Q4+ | Bảng 6.2 | Coase, Moore, Gurley |
| Auto liên hệ (agent) | L3 | GT | Quý 3+ | Gọi tay 5 đối tác trước; thiết kế theo điều khoản Places | Mục 6.3 |
| Phân loại loại du lịch | L1/L2 | Công cụ phân khúc | Sau phỏng vấn | Hỏi trong phỏng vấn; chốt `styleTags` | Kotler STP |
| Đi theo cặp/chùm | L2 | GT | Quý 3+ | Hỏi G6; ghép tay 1 chuyến | Graham, Spinks, Chen |
| "Mọi người hiểu vision và giai đoạn" | L6 | Đúng, là bước 1 | Tuần 1 | Bản vẽ 1 + bảng 8.6 dán ở đầu mọi cuộc họp | — |
| "Quy trình báo cáo, tổng hợp tài liệu" | L6 | Đúng, nhưng là **đầu ra** của vòng lặp | Tuần 1 | Thư mục 8.4 + mẫu Phụ lục C | Mục 8.3 |

---

## 11. Nguồn tham khảo

Mỗi nguồn ghi **đã dùng cho điều gì**. Sách không có link; văn bản luật, số liệu và điều khoản nền tảng
có link đã tra ngày 15/09/2026.

### A. Sách và bài viết nền tảng `[S-xx]`

| Mã | Tác giả — tác phẩm (năm) | Dùng cho |
|---|---|---|
| S-01 | Peter F. Drucker — *The Five Most Important Questions You Will Ever Ask About Your Organization* (1993; bản 2008) | Năm câu hỏi khởi đầu (2.1) |
| S-02 | Peter F. Drucker — "The Theory of the Business", *Harvard Business Review* (1994) — https://hbr.org/1994/09/the-theory-of-the-business | Kiểm tra định kỳ giả định về doanh nghiệp |
| S-03 | Steve Blank — *The Four Steps to the Epiphany* (2005); Blank & Bob Dorf — *The Startup Owner's Manual* (2012) | Định nghĩa startup; Khám phá khách hàng; tổ chức "đi tìm" |
| S-04 | Eric Ries — *The Lean Startup* (2011) | Xây–Đo–Học; giả thuyết giá trị/tăng trưởng; innovation accounting |
| S-05 | Theodore Levitt — "Marketing Myopia", *HBR* (1960) — https://hbr.org/2004/07/marketing-myopia | "Kinh doanh gì?"; bán cái lỗ không bán mũi khoan |
| S-06 | Clayton Christensen, Taddy Hall, Karen Dillon, David Duncan — *Competing Against Luck* (2016); bài "Know Your Customers' Jobs to Be Done", *HBR* (2016) — https://hbr.org/2016/09/know-your-customers-jobs-to-be-done | Việc-cần-làm (2.3) |
| S-07 | Alexander Osterwalder & Yves Pigneur — *Business Model Generation* (2010) | Business Model Canvas |
| S-08 | Ash Maurya — *Running Lean* (2012) | Lean Canvas (2.2) |
| S-09 | Osterwalder, Pigneur, Bernarda, Smith — *Value Proposition Design* (2014) | Bản đồ giá trị (2.3) |
| S-10 | Geoffrey A. Moore — *Crossing the Chasm* (1991); *Dealing with Darwin* (2005) | Đầu cầu; cốt lõi vs bối cảnh |
| S-11 | Bill Aulet — *Disciplined Entrepreneurship* (2013) | 24 bước; chọn đầu cầu; TAM từ dưới lên (2.5) |
| S-12 | Jim Collins & Jerry Porras — *Built to Last* (1994); "Building Your Company's Vision", *HBR* (1996) — https://hbr.org/1996/09/building-your-companys-vision | Tư tưởng cốt lõi, BHAG (3.1) |
| S-13 | Simon Sinek — *Start With Why* (2009) | Vòng tròn vàng |
| S-14 | Rob Fitzpatrick — *The Mom Test* (2013) | Cách hỏi khách không bị nói dối (Phụ lục A) |
| S-15 | Peter Thiel & Blake Masters — *Zero to One* (2014) | Câu hỏi "bí mật"; độc quyền nhỏ trước |
| S-16 | April Dunford — *Obviously Awesome* (2019) | Định vị theo bối cảnh cạnh tranh |
| S-17 | Al Ries & Jack Trout — *Positioning: The Battle for Your Mind* (1981); *The 22 Immutable Laws of Marketing* (1993) | Luật hạng mục (4.2) |
| S-18 | Byron Sharp — *How Brands Grow* (2010); Romaniuk & Sharp — *How Brands Grow Part 2* (2015) | Độ sẵn có trong tâm trí; tài sản đặc trưng (4.4) |
| S-19 | Alistair Croll & Benjamin Yoskovitz — *Lean Analytics* (2013) | 5 giai đoạn; One Metric That Matters (8.6) |
| S-20 | Dave McClure — "Startup Metrics for Pirates" (2007) | Phễu AARRR (8.7) |
| S-21 | Andrew Grove — *High Output Management* (1983); John Doerr — *Measure What Matters* (2018) | OKR theo quý |
| S-22 | Gary Klein — "Performing a Project Premortem", *HBR* (2007) — https://hbr.org/2007/09/performing-a-project-premortem | Pre-mortem (6.4) |
| S-23 | Michael Nygard — "Documenting Architecture Decisions" (2011) — https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions | Sổ quyết định (Phụ lục D) |
| S-24 | Gino Wickman — *Traction: Get a Grip on Your Business* (2012) | EOS: Rocks, Level 10, Accountability Chart (8.2, 8.5) |
| S-25 | Rik Higham — "The MVP is dead. Long live the RAT" (2016) — https://hackernoon.com/the-mvp-is-dead-long-live-the-rat-233d5d16ab02 | Kiểm giả thuyết rủi ro nhất (2.4) |
| S-26 | Noam Wasserman — *The Founder's Dilemmas* (2012) | Vai trò, quan hệ, phần thưởng giữa đồng sáng lập |
| S-27 | Mike Moyer — *Slicing Pie* (2012) | Chia phần động theo đóng góp thật, hợp với nhóm bán thời gian |
| S-28 | Guy Kawasaki — *The Art of the Start* (2004) | Mantra vs slogan |
| S-29 | Chip Heath & Dan Heath — *Made to Stick* (2007) | Công thức câu "dính" |
| S-30 | Marty Neumeier — *Zag* (2006) | Phát biểu "duy nhất" (onliness) |
| S-31 | A.G. Lafley & Roger L. Martin — *Playing to Win* (2013) | Năm lựa chọn chiến lược (4.1) |
| S-32 | Richard Rumelt — *Good Strategy Bad Strategy* (2011) | Hạt nhân chiến lược |
| S-33 | Michael E. Porter — "What Is Strategy?", *HBR* (1996) — https://hbr.org/1996/11/what-is-strategy | Chiến lược là chọn không làm gì |
| S-34 | Al Ramadan, Dave Peterson, Christopher Lochhead, Kevin Maney — *Play Bigger* (2016) | Thiết kế hạng mục |
| S-35 | Chris Dixon — "Come for the tool, stay for the network" (2015) — https://cdixon.org/2015/01/31/come-for-the-tool-stay-for-the-network | Công cụ trước, mạng lưới sau (4.2) |
| S-36 | Andrew Chen — *The Cold Start Problem* (2021) | Mạng nguyên tử |
| S-37 | W. Chan Kim & Renée Mauborgne — *Blue Ocean Strategy* (2005) | Bản đồ chiến lược, Loại bỏ–Giảm–Tăng–Tạo (4.3) |
| S-38 | Robert Zajonc — "Attitudinal Effects of Mere Exposure", *Journal of Personality and Social Psychology* (1968) | Hiệu ứng tiếp xúc đơn thuần |
| S-39 | Robert Cialdini — *Influence* (1984) | Bằng chứng xã hội |
| S-40 | Paul Graham — "Do Things That Don't Scale" (2013) — http://paulgraham.com/ds.html | Tuyển người dùng bằng tay; ví dụ Airbnb |
| S-41 | Les Binet & Peter Field — *The Long and the Short of It* (2013) | Cân bằng thương hiệu / kích hoạt |
| S-42 | Gabriel Weinberg & Justin Mares — *Traction* (2015) | Bullseye, 19 kênh |
| S-43 | Bill Gurley — "All Markets Are Not Created Equal: 10 Factors to Consider When Evaluating Digital Marketplaces" (2012) — https://abovethecrowd.com/2012/11/13/all-markets-are-not-created-equal-10-factors-to-consider-when-evaluating-digital-marketplaces/ | Điều kiện để sàn đáng làm (5.1) |
| S-44 | Geoffrey Parker, Marshall Van Alstyne, Sangeet Paul Choudary — *Platform Revolution* (2016) | Gà–trứng |
| S-45 | Lenny Rachitsky — "How the biggest consumer apps got their first 1,000 users" (2020) — https://www.lennysnewsletter.com/p/how-the-biggest-consumer-apps-got | 1.000 người dùng đầu bằng tay |
| S-46 | Kevin Kelly — "1,000 True Fans" (2008) — https://kk.org/thetechnium/1000-true-fans/ | Sản phẩm vật lý cho người hâm mộ |
| S-47 | Bob Moesta — *Demand-Side Sales 101* (2020) | Bán theo phía cầu (5.2) |
| S-48 | Madhavan Ramanujam & Georg Tacke — *Monetizing Innovation* (2016) | Định giá trước khi xây |
| S-49 | Daniel Kahneman & Amos Tversky — "Prospect Theory" (1979); Kahneman — *Thinking, Fast and Slow* (2011) | Sợ mất hơn thích được |
| S-50 | David Skok — "SaaS Metrics 2.0" (For Entrepreneurs) | Mốc LTV ≥ 3 × CAC |
| S-51 | Ronald Coase — "The Nature of the Firm", *Economica* (1937) | Tự làm hay thuê ngoài (6.1) |
| S-52 | G. Lynn Shostack — "Designing Services That Deliver", *HBR* (1984) — https://hbr.org/1984/01/designing-services-that-deliver | Bản thiết kế dịch vụ |
| S-53 | David Spinks — *The Business of Belonging* (2021) | Xây cộng đồng từ nhóm nhỏ |
| S-54 | Philip Kotler & Kevin Lane Keller — *Marketing Management* (15th ed., 2016) | STP |
| S-55 | Sequoia Capital — "Writing a Business Plan" — https://www.sequoiacap.com/article/writing-a-business-plan/ | Khung 10 slide khi cần gọi vốn |
| S-56 | Michael E. Gerber — *The E-Myth Revisited* (1995) | Làm việc trên doanh nghiệp |
| S-57 | Verne Harnish — *Mastering the Rockefeller Habits* (2002); *Scaling Up* (2014) | Nhịp họp; kế hoạch một trang |
| S-58 | Jeff Bezos — Thư gửi cổ đông Amazon 1997 và 2015 | Day 1; cửa một chiều / hai chiều |
| S-59 | Ryan Singer — *Shape Up* (Basecamp, 2019) — https://basecamp.com/shapeup | Chu kỳ 6 tuần, bàn cược, khẩu vị (8.2) |
| S-60 | Tiago Forte — *Building a Second Brain* (2022); phương pháp PARA | Cấu trúc thư mục (8.4) |
| S-61 | Sean Ellis — khảo sát khớp sản phẩm–thị trường (ngưỡng 40%) — https://pmfsurvey.com/ ; Ellis & Morgan Brown — *Hacking Growth* (2017) | Cổng giai đoạn 2 (8.6) |
| S-62 | Marc Andreessen — "The only thing that matters" (2007) — https://pmarchive.com/guide_to_startups_part4.html | Khớp sản phẩm–thị trường trước mọi thứ |
| S-63 | Brian Balfour — "Four Fits" (2017) — https://brianbalfour.com/four-fits-growth-framework | Bốn khớp |
| S-64 | Teresa Torres — *Continuous Discovery Habits* (2021) | Phỏng vấn khách hằng tuần; cây cơ hội–giải pháp |
| S-65 | Marty Cagan — *Inspired* (2nd ed., 2017) | Khám phá sản phẩm trước khi giao |

### B. Văn bản pháp luật Việt Nam `[L-xx]`

| Mã | Văn bản | Dùng cho | Nguồn tra |
|---|---|---|---|
| L-01 | Nghị định 13/2023/NĐ-CP về bảo vệ dữ liệu cá nhân (hiệu lực 01/07/2023) — vị trí qua dịch vụ định vị là dữ liệu nhạy cảm | Mục 7.1 | https://thuvienphapluat.vn/van-ban/Cong-nghe-thong-tin/Nghi-dinh-13-2023-ND-CP-bao-ve-du-lieu-ca-nhan-465185.aspx ; https://xaydungchinhsach.chinhphu.vn/the-nao-la-du-lieu-ca-nhan-nhay-cam-119230417203904046.htm |
| L-02 | Luật Bảo vệ dữ liệu cá nhân số 91/2025/QH15 (thông qua 26/06/2025, hiệu lực 01/01/2026) | Mục 7.1 | https://thuvienphapluat.vn/van-ban/Bo-may-hanh-chinh/Luat-Bao-ve-du-lieu-ca-nhan-2025-so-91-2025-QH15-625628.aspx ; https://vneconomy.vn/nhung-diem-moi-trong-luat-bao-ve-du-lieu-ca-nhan-tu-112026.htm ; https://bocongan.gov.vn/chinh-sach-phap-luat/bai-viet/luat-bao-ve-du-lieu-ca-nhan-chinh-thuc-co-hieu-luc-thi-hanh-tu-ngay-01-01-2026-1767186124 |
| L-03 | Nghị định 147/2024/NĐ-CP quản lý, cung cấp, sử dụng dịch vụ Internet và thông tin trên mạng (hiệu lực 25/12/2024) — ngưỡng cấp phép MXH, xác thực tài khoản, điều kiện chủ thể | Mục 7.1, 7.2 | https://vanban.chinhphu.vn/?pageid=27160&docid=211654 ; https://vietnamnet.vn/mang-xa-hoi-duoi-1000-nguoi-su-dung-khong-can-phai-xin-cap-phep-2343248.html ; https://thuvienphapluat.vn/chinh-sach-phap-luat-moi/vn/ho-tro-phap-luat/chinh-sach-moi/75756/thu-tuc-cap-giay-phep-cung-cap-dich-vu-mang-xa-hoi-tu-25-12-2024 |
| L-04 | Bài tổng hợp về xác thực tài khoản MXH bằng số điện thoại theo NĐ 147 | Mục 7.1 | https://vietnamnet.vn/nghi-dinh-147-ve-quan-ly-internet-va-thong-tin-tren-mang-chinh-thuc-co-hieu-luc-2356099.html |
| L-05 | Nghị định 168/2017/NĐ-CP (Điều 14) — mức ký quỹ lữ hành 100 / 250 / 500 triệu đồng, áp dụng lại từ 01/01/2024 sau thời gian giảm theo NĐ 94/2021 | Mục 5.1, 7.2 | https://bvhttdl.gov.vn/tu-ngay-01-01-2024-thuc-hien-muc-ky-quy-kinh-doanh-dich-vu-lu-hanh-theo-nghi-dinh-168-2017-nd-cp-20231229105147508.htm |
| L-06 | Luật Thương mại điện tử số 122/2025/QH15 (thông qua 10/12/2025, hiệu lực 01/07/2026) | Mục 7.1 | https://congthuong.vn/luat-thuong-mai-dien-tu-siet-quan-ly-mo-loi-thi-truong-so-444207.html ; https://baochinhphu.vn/luat-thuong-mai-dien-tu-lap-day-khoang-trong-phap-ly-cho-kinh-te-so-bao-ve-nguoi-tieu-dung-102260106142452861.htm |
| L-07 | Nghị quyết 198/2025/QH15 về cơ chế, chính sách đặc biệt phát triển kinh tế tư nhân (17/05/2025) — miễn TNDN 3 năm cho DNNVV; bỏ lệ phí môn bài và thuế khoán từ 01/01/2026 | Mục 7.2 | https://xaydungchinhsach.chinhphu.vn/nghi-quyet-198-2025-qh15-ve-mot-so-co-che-chinh-sach-dac-biet-phat-trien-kinh-te-tu-nhan-119250517191622422.htm ; https://thuvienphapluat.vn/phap-luat-viec-lam/tuyen-dung-viec-lam/nghi-quyet-198-chinh-thuc-bo-thue-khoan-doi-voi-ho-kinh-doanh-tu-ngay-1-1-2026-muc-tieu-phat-trien-cua-kinh-te-tu-nhan-den-nam-2030-27839.html |

Văn bản **được nhắc tới nhưng chưa tra trong lần này** (phải kiểm chứng trước khi dựa vào): Luật Du
lịch 2017 (định nghĩa kinh doanh lữ hành, thẻ HDV); Luật Kinh doanh bảo hiểm 2022 (điều kiện đại lý);
Nghị định 91/2020/NĐ-CP (chống tin nhắn, cuộc gọi rác); Luật Doanh nghiệp 2020 và sửa đổi 2025 (loại
hình công ty); nghị định hướng dẫn Luật BVDLCN 2025 (hồ sơ đánh giá tác động).

### C. Số liệu thị trường `[D-xx]`

| Mã | Số liệu | Nguồn |
|---|---|---|
| D-01 | Năm 2025: ≈ 135,5 triệu lượt khách nội địa; tổng thu du lịch lần đầu vượt 1 triệu tỷ đồng | Bộ VHTTDL — https://bvhttdl.gov.vn/du-lich-viet-nam-nam-2025-dau-moc-but-pha-khang-dinh-vi-the-moi-20260108102401774.htm |
| D-02 | Năm 2025: gần 21,2 triệu lượt khách quốc tế, tăng 20,4%; Trung Quốc 5,28 triệu, Hàn Quốc 4,33 triệu | Cục Thống kê qua VietNamNet — https://vietnamnet.vn/khach-quoc-te-cao-ky-luc-du-lich-viet-nam-nam-2025-thang-lon-2479099.html |
| D-03 | Gen Z Việt Nam: đi 2–3 lần/năm; chi 3–5 triệu/chuyến; > 70% du khách trẻ chọn tự túc; 54% dự định đi nhiều hơn năm 2026; xem mỗi chuyến đi là "thành tựu cá nhân". **Lưu ý:** đây là số báo chí dẫn từ các khảo sát của nền tảng đặt phòng; chưa tra tới báo cáo gốc — dùng để định hướng, không dùng để tính TAM | VnEconomy — https://vneconomy.vn/du-khach-gen-z-se-tao-buoc-ngoat-lon-cho-nganh-du-lich.htm ; Tuổi Trẻ — https://tuoitre.vn/du-lich-viet-nam-sap-lap-ky-luc-moi-gen-z-xem-moi-chuyen-di-la-thanh-tuu-ca-nhan-20251201123446818.htm ; Báo Quảng Ninh — https://baoquangninh.vn/du-lich-tu-tuc-len-ngoi-trong-nam-2025-3353277.html |

### D. Điều khoản và chính sách nền tảng `[P-xx]`

| Mã | Nội dung | Nguồn |
|---|---|---|
| P-01 | Google Maps Platform Service Specific Terms — cấm scraping (3.2.3a), hạn chế lưu đệm (3.2.3b) | https://cloud.google.com/maps-platform/terms/maps-service-terms |
| P-02 | Places API policies — Place ID lưu vô thời hạn; toạ độ lưu đệm tối đa 30 ngày | https://developers.google.com/maps/documentation/places/web-service/policies |
| P-03 | Play Console — tài khoản cá nhân lập sau 13/11/2023 phải thử nghiệm kín ≥ 12 người liên tục ≥ 14 ngày trước khi xin phát hành (giảm từ 20 xuống 12 ngày 11/12/2024) | https://support.google.com/googleplay/android-developer/answer/14151465 |
| P-04 | Google Play — chính sách nội dung do người dùng tạo (báo cáo, chặn) — tương ứng D1 trong `status.md` | https://support.google.com/googleplay/android-developer/answer/9876937 |
| P-05 | Google Play — yêu cầu cung cấp đường dẫn xoá tài khoản — tương ứng D7 | https://support.google.com/googleplay/android-developer/answer/13327111 |

---

## Phụ lục A — Kịch bản phỏng vấn khách hàng (30 phút)

Nguyên tắc của Fitzpatrick `[S-14]`: **hỏi về đời họ, không hỏi về ý tưởng của mình**; hỏi về **quá
khứ cụ thể**, không hỏi về tương lai giả định; lời khen là dữ liệu rác, **hành vi đã làm và tiền đã
chi** là dữ liệu vàng. Không mở app cho họ xem trước phút 25.

**Mở (2'):** "Mình đang tìm hiểu cách người trẻ đi du lịch tự túc. Không có câu trả lời đúng sai. Kể
cho mình nghe về chuyến đi gần nhất của bạn được không?"

**Về chuyến đi gần nhất (12'):**
1. Đi đâu, với ai, bao lâu, quyết định đi từ lúc nào?
2. Lên kế hoạch thế nào? Dùng app/nhóm/người nào? Mất khoảng bao lâu? *(để họ kể; ghi tên từng công cụ)*
3. Khoảnh khắc **bực nhất** trước và trong chuyến đi là gì? Lúc đó bạn làm gì?
4. Có trả tiền cho thứ gì để đỡ bực không? Bao nhiêu?
5. Có gặp sự cố nào không (lạc, mưa, xe, giấy tờ, sức khoẻ)? Xử lý thế nào?

**Về "sau chuyến đi" (6'):**
6. Ảnh chuyến đó giờ ở đâu? Có đăng gì không? Đăng ở đâu, vì sao chỗ đó?
7. Bạn đã đi được bao nhiêu tỉnh? Có đếm không? Có ai bạn biết đang cố "đi đủ" không?
8. Có thứ gì bạn **giữ lại** từ các chuyến đi không (vé, sổ, huy hiệu)?

**Về người đi cùng (4'):**
9. Đã bao giờ đi cùng người mới quen qua mạng chưa? Kể. Nếu chưa — vì sao?

**Đóng (5'):** Cho xem app 3 phút, **không giải thích**, quan sát họ bấm gì. Hỏi: "Nếu ngày mai app
này biến mất, bạn thấy sao?" Rồi: "Bạn biết ai cũng hay đi tự túc mà mình nên nói chuyện không?"

**Sau buổi (5', bắt buộc):** ghi tệp `01_khach-hang/YYYY-MM-DD-ten.md` với: 3 câu trích nguyên văn,
công cụ họ dùng, tiền họ đã chi, 1 dòng "học được gì", 1 dòng "giả thuyết nào bị lay".

## Phụ lục B — Thẻ thí nghiệm

```
# Thẻ thí nghiệm: <tên ngắn>
Ngày tạo: YYYY-MM-DD · Chủ: <tên> · Chu kỳ: <số> · Giả thuyết gốc: G<n> (mục 2.4)

## Chúng tôi tin rằng
<phát biểu một câu>

## Sẽ biết là đúng nếu
đo được <chỉ số> đạt <ngưỡng> trong <thời gian> trên <bao nhiêu người>

## Cách làm (rẻ nhất có thể)
- ...

## Khẩu vị thời gian
<số tuần — cố định, hết là dừng>

## Không làm
- ...

## Kết quả (điền sau)
Số đo: ... · Bằng chứng: <link ảnh/truy vấn/bản ghi>
Kết luận: tiếp / sửa / bỏ · Quyết định: 00_quyet-dinh/<tệp>
```

## Phụ lục C — Họp tuần và báo cáo tuần

**Chương trình họp Level 10 (Wickman `[S-24]`), 90 phút, cố định:**

| Phút | Mục | Quy tắc |
|---|---|---|
| 0–5 | Mở đầu | Mỗi người 1 tin tốt cá nhân, 1 tin tốt công việc |
| 5–10 | Bảng số | Đọc số, **không** thảo luận; số lệch mục tiêu → ghi vào danh sách vấn đề |
| 10–15 | Rocks | Từng Rock: "đúng tiến độ" / "lệch" — một từ; lệch → danh sách vấn đề |
| 15–20 | Tin từ khách và đối tác | 1 câu/tin; cần bàn → danh sách vấn đề |
| 20–25 | Việc tuần trước | Xong / chưa; chưa → danh sách vấn đề |
| 25–85 | **Nhận diện – Thảo luận – Giải quyết** | Chọn 3 vấn đề quan trọng nhất; giải quyết **hẳn** từng cái; kết quả là **việc có tên người và hạn** |
| 85–90 | Kết | Đọc lại việc tuần này; ai cần biết gì (báo ra ngoài); mỗi người chấm buổi họp 1–10 |

**Báo cáo tuần** (1 tệp `06_hop/YYYY-Www.md`, ≤ 1 trang, người điều phối viết trong 15 phút sau họp):

```
# Tuần <số> — <ngày>
## Số (so với tuần trước)
phỏng vấn: n (+x) · người dùng kích hoạt: n · quay lại 7 ngày: n% · nội dung đăng: n · đối tác liên hệ: n
## Rocks quý: R1 đúng/lệch · R2 … · R3 …
## Học được gì tuần này (≤ 3 dòng, có bằng chứng)
## Quyết định đã ghi (link ADR)
## Việc tuần này (≤ 7, tên người, hạn)
## Vướng (cần ai)
```

## Phụ lục D — Bản ghi quyết định (ADR cho kinh doanh)

```
# QĐ-<số>: <tên quyết định>
Ngày: YYYY-MM-DD · Người quyết: <tên> · Loại cửa: một chiều / hai chiều
## Bối cảnh
<vấn đề, ràng buộc, số liệu lúc đó>
## Các lựa chọn đã cân
1. ... — ưu / nhược
2. ...
## Quyết định
<chọn gì>
## Vì sao
<lý do, bằng chứng, nguồn>
## Hệ quả và điều kiện mở lại
<cái gì đổi; khi nào được xem lại — thường là cổng quý>
```

Ba quyết định đã có mà nên chép vào sổ ngay để làm mẫu: (1) đổi tên thành Levart (08/2026);
(2) chấp nhận rủi ro khoá Gemini trong APK (29/08/2026, `status.md` mục 5); (3) chọn B1 bản đầy đủ
thay bản gọn (07/09/2026, `status.md` mục 8).

## Phụ lục E — Kế hoạch một trang (dán đầu mọi cuộc họp quý)

```
LEVART — MỘT TRANG                                          Cập nhật: ____
─────────────────────────────────────────────────────────────────────────
GIÁ TRỊ CỐT LÕI (3):   1. ____  2. ____  3. ____
MỤC ĐÍCH:              ______________________________________________
BHAG (10 năm):         ______________________________________________
ĐẦU CẦU (12 tháng):    ai ____ · ở đâu ____ · việc-cần-làm ____
GIAI ĐOẠN HIỆN TẠI:    Thấu cảm / Bám dính / Lan truyền / Doanh thu / Mở rộng
CHỈ SỐ DUY NHẤT:       ____ hiện ____ · cổng ____
ROCKS QUÝ NÀY (3):     1. ____ (ai) 2. ____ (ai) 3. ____ (ai)
KHÔNG LÀM QUÝ NÀY:     ______________________________________________
RỦI RO SỐ 1:           ____ · cách giảm ____
GHẾ:                   Tầm nhìn ____ · Điều phối ____ · Sản phẩm ____ · Tăng trưởng ____ · Vận hành ____
```

## Phụ lục F — Bãi đỗ ý tưởng (mẫu)

Một tệp `07_bai-do/bai-do.md`, mỗi ý một dòng, **không tranh luận khi thêm**; chỉ mở ra ở bàn cược.

```
| Ngày | Ý tưởng (1 dòng) | Lớp | Giả thuyết liên quan | Người nêu | Trạng thái |
|---|---|---|---|---|---|
| 2026-09-15 | Chatbot du lịch riêng | L3 | G2 | — | Đỗ — chờ 100 câu hỏi thật |
| 2026-09-15 | Agent tự gọi đối tác gần biển | L3 | G3 | — | Đỗ — chờ 3 đối tác thật |
| 2026-09-15 | Ghép nhóm đi chung cho rẻ | L2 | G6 | — | Đỗ — hỏi trong phỏng vấn |
| 2026-09-15 | Dịch vụ nhà trẻ, phiên dịch | L5 | — | — | Đỗ — ngoài đầu cầu |
| 2026-09-15 | Web-hoá | L3 | — | — | Đỗ — chỉ làm trang tĩnh D5/D7 |
| 2026-09-15 | Hộ chiếu in + con dấu thật | L2 | G4 | — | Ứng viên cược quý 2 |
```

---

*Phiên bản kế tiếp của tệp này (0.2) nên được viết sau đợt phỏng vấn đầu — khi đó phần lớn (GT) trong
mục 2 sẽ hoặc có bằng chứng, hoặc bị gạch. Đó là dấu hiệu tài liệu đang sống.*
