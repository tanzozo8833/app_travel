# LEVART — Kế hoạch nghiên cứu (PLAN_RESEARCH.md)

Soạn: **13/10/2026** · Phiên bản **0.1** · Phạm vi: **Giai đoạn A — Nghiên cứu**, làm trước Giai đoạn B — Phát triển sản phẩm
Đọc kèm: `Business.md` (chiến lược, pháp lý), `ROLE.md` (ghế), `week_index.md` (lịch 52 tuần). Tệp này **không sửa** các tệp đó.

> **Thứ tự công việc từ nay:** nghiên cứu trước (biết mình đang đứng ở đâu), rồi mới phát triển sản phẩm theo vòng lặp.
> Tệp này trả lời: *nghiên cứu những gì, theo thứ tự nào, dùng mô hình nào, ra sản phẩm gì, khi nào dừng.*

**Quy ước**

- **(GT)** = giả thuyết, chưa kiểm chứng. Mọi bảng "điền sẵn" trong tệp này là (GT) để nhóm **kiểm**, không phải kết luận.
- **Mức bằng chứng** — mọi con số và nhận định trong kết quả nghiên cứu phải gắn một mức:

| Mức | Là gì | Ví dụ | Được dùng cho |
|---|---|---|---|
| **A** | Văn bản/số liệu gốc của cơ quan nhà nước, điều khoản nền tảng, dữ liệu tự đo | Nghị quyết, niên giám thống kê, điều khoản Google Play, số liệu từ app của mình | Mọi quyết định |
| **B** | Báo cáo ngành có công bố phương pháp và cỡ mẫu | Báo cáo kinh tế số thường niên, khảo sát có nêu n và cách chọn mẫu | Mọi quyết định, ghi rõ nguồn |
| **C** | Báo chí, blog dẫn lại, không truy được nguồn gốc | "Hơn 70% du khách trẻ đi tự túc" (Business.md D-03) | Chỉ để định hướng, **không** làm nền cho quyết định cửa một chiều |
| **D** | Ý kiến, suy luận của nhóm | "Gen Z thích sưu tầm con dấu" | Bắt buộc gắn (GT) |

- Nguồn phương pháp mang mã **[M-xx]**, nguồn văn bản/dữ liệu mới mang mã **[N-xx]** — xem mục 16. Nguồn đã có trong `Business.md` giữ mã cũ (ví dụ `Business.md [L-03]`).

---

## 0. Tóm tắt một trang

### 0.1 Bức tranh toàn thể

Khung tổng là **Kim cương đôi** (Double Diamond) `[M-01]`: mỗi kim cương gồm một pha **mở rộng** (thu thập nhiều) và một pha **thu hẹp** (chọn lọc, kết luận). Kim cương thứ nhất là **nghiên cứu** — hiểu đúng vấn đề. Kim cương thứ hai là **phát triển sản phẩm** — làm đúng giải pháp. Bên trong kim cương thứ nhất của Levart có **hai lượt**: lượt tại bàn (dữ liệu có sẵn) rồi lượt thực địa (hỏi người thật).

```
GIAI ĐOẠN A — NGHIÊN CỨU  (kim cương 1: hiểu đúng vấn đề)

  LƯỢT 1 · THỨ CẤP (tại bàn)          HỘI TỤ 1                      LƯỢT 2 · SƠ CẤP (thực địa)       HỘI TỤ 2
  ────────────────────────────        ──────────────────────        ──────────────────────────       ────────────────
  R0  Khung & sổ câu hỏi              R5  SWOT → TOWS               R8  Thiết kế: hình thức,         R10 Phân tích
  R1  Vĩ mô: xu hướng + PESTLE   ──►  R6  Chọn TA (STP)        ──►      mẫu, kịch bản, bảng hỏi ──►      → Báo cáo
  R2  Ngành: 5 lực + Value Net        R7  Khoảng trống dữ liệu      R9  Thực địa: phỏng vấn               → CỔNG A
      + cỡ thị trường                     → chọn phương pháp            → khảo sát (nếu cần)
  R3  Đối thủ + KIỂM TRÙNG
  R4  Nội bộ: nguồn lực, VRIO,
      chuỗi → mạng giá trị
                                                                                                          │
GIAI ĐOẠN B — PHÁT TRIỂN SẢN PHẨM  (kim cương 2: làm đúng giải pháp — vòng lặp Xây → Đo → Học)  ◄────────┘
```

### 0.2 Mười một bước, theo thứ tự

| Bước | Trả lời câu hỏi | Mô hình chính | Đầu ra | Tuần |
|---|---|---|---|---|
| **R0** | Nghiên cứu để ra **quyết định** gì? Khi nào dừng? | Bản tóm tắt nghiên cứu (research brief), sổ câu hỏi | Brief 1 trang + sổ câu hỏi v0 | 1 |
| **R1** | Môi trường lớn đang đẩy hay kéo Levart? | Quét xu hướng + **PESTLE** `[M-02][M-03]` + vòng đời điểm đến `[M-14]` | Bảng PESTLE có điểm tác động và cột "vì vậy" | 1–2 |
| **R2** | Ngành có đáng vào? Tiền chảy về ai? Ai có thể hợp tác? | **Porter 5 lực** `[M-04]` + **Mạng giá trị (Value Net)** `[M-06]` + TAM/SAM/SOM | Bảng 5 lực, bản đồ Value Net, cỡ thị trường | 2 |
| **R3** | Ai đang làm gì? **Levart có trùng không** — chức năng, tên, sở hữu trí tuệ? | Hồ sơ đối thủ, bản đồ nhóm chiến lược, strategy canvas `[M-27]`; kiểm trùng 3 lớp | Ma trận đối thủ + báo cáo kiểm trùng | 2–3 |
| **R4** | Mình có gì, mạnh thật ở đâu, đáp ứng được bao nhiêu? | Nguồn lực hữu hình/vô hình/con người `[M-10]` → **VRIO** `[M-09]`; **chuỗi giá trị** `[M-05]` → **mạng giá trị** `[M-07]` | Bảng kiểm kê có bằng chứng, VRIO, sơ đồ mạng giá trị | 3 |
| **R5** | Ghép bên ngoài với bên trong thành lựa chọn | **SWOT → TOWS** `[M-11]` | Ma trận TOWS + 2–3 phương án chiến lược | 4 |
| **R6** | Phục vụ ai trước? | **STP** `[M-12]` + ma trận hấp dẫn × khả năng phục vụ + đầu cầu `[M-13]` | TA (GT) + persona v0 | 4 |
| **R7** | Còn thiếu dữ liệu gì? Lấy bằng cách nào? **Có cần phỏng vấn không?** | Bản đồ giả định `[M-25]` + cây quyết định phương pháp | Danh sách câu hỏi phải hỏi người thật | 4 |
| **R8** | Hỏi ai, hỏi thế nào? | Thiết kế hỗn hợp tuần tự khám phá `[M-19]`; quy trình 10 bước thiết kế bảng hỏi `[M-18]` | Kế hoạch mẫu, kịch bản phỏng vấn, bảng hỏi, bản thử | 5 |
| **R9** | Thu dữ liệu thật | Phỏng vấn sâu, phỏng vấn đối tác, (khảo sát) | Bộ ghi chép + dữ liệu khảo sát | 6–9 |
| **R10** | Kết luận gì? Đi tiếp không? | Phân tích chủ đề `[M-21]`, tam giác hoá | Báo cáo nghiên cứu + quyết định **Cổng A** | 9–10 |

### 0.3 Những điểm đã điều chỉnh so với flow gốc của nhóm

Flow gốc đúng hướng — đi từ ngoài vào trong, rồi mới hỏi khách. Mười một điểm dưới đây là chỗ tôi **thêm hoặc dời** để mỗi bước dùng được đầu ra của bước trước.

| # | Flow gốc | Điều chỉnh | Vì sao |
|---|---|---|---|
| 1 | (chưa có) | Thêm **R0 — Khung** | Nghiên cứu không gắn với quyết định cụ thể thì phình vô hạn; "research hết một lần" cần biết **hết** là hết cái gì |
| 2 | Vi mô: 5 lực, "những bên có thể hợp tác cùng" | Thêm **Mạng giá trị (Value Net)** bên cạnh 5 lực | 5 lực không có ô cho bên hợp tác. Chính Porter (2008) xếp sản phẩm bổ trợ vào "yếu tố", không phải lực `[M-04]`. Value Net có ô riêng cho **bên bổ trợ** — đúng thứ nhóm muốn tìm |
| 3 | (chưa có) | Thêm **cỡ thị trường TAM/SAM/SOM** vào R2 | 5 lực cho biết ngành có dễ kiếm lời không, không cho biết thị trường **đủ lớn** không |
| 4 | "Xác định app có bị trùng không (function, tên, SHTT)" | Tách thành **3 lớp kiểm trùng** | Trùng chức năng là chuyện **cạnh tranh** (ý tưởng không được bảo hộ); trùng tên là **nhãn hiệu**; trùng biểu hiện là **quyền tác giả**. Ba lớp, ba cách xử lý |
| 5 | Phân tích nguồn lực hữu hình/vô hình | Thêm **VRIO** | Liệt kê nguồn lực chưa đủ — phải hỏi nguồn lực nào tạo **lợi thế bền** |
| 6 | Mô hình chuỗi giá trị | Dùng chuỗi giá trị làm **danh mục kiểm**, nhưng phân tích Levart như **mạng giá trị** | Chuỗi giá trị sinh ra cho doanh nghiệp sản xuất. Levart là **trung gian kết nối** người đi với bên cung; Stabell & Fjeldstad chỉ ra loại doanh nghiệp này tạo giá trị theo logic **mạng** `[M-07]` |
| 7 | "Vốn: ok, kỹ thuật: ok, hạ tầng: ok…" | Thêm **thước đo cho chữ "ok"** | Mỗi "ok" phải có bằng chứng và ngưỡng, nếu không thì là cảm giác |
| 8 | Chọn TA nằm trong phân tích nội bộ | Dời **ra sau tổng hợp (R6)** | Tiêu chí chọn TA = **mức hấp dẫn** (từ R1–R3) × **khả năng phục vụ** (từ R4). Chọn trước khi có đủ hai vế là chọn theo cảm tính |
| 9 | "Nên đi phỏng vấn hay không" | Thay bằng **cây quyết định phương pháp** (R7) | Mỗi loại câu hỏi có một phương pháp đúng. Phỏng vấn **không** trả lời được "bao nhiêu %" và "họ có thật sự trả tiền không" |
| 10 | "Xây dựng bảng hỏi" | Tách **kịch bản phỏng vấn** (định tính) và **bảng hỏi khảo sát** (định lượng); bảng hỏi làm **sau** phỏng vấn | Không phỏng vấn trước thì không biết đưa lựa chọn nào vào bảng hỏi |
| 11 | (chưa có) | Thêm **R10 + Cổng A** | Phải có một điểm kết luận và quyết định trước khi chuyển sang phát triển sản phẩm |

### 0.4 Ba nguyên tắc

1. **Thứ cấp trước sơ cấp.** Không dùng lượt phỏng vấn để hỏi điều tra cứu được `[M-18]`.
2. **Ngoài → trong → ghép → chọn → hỏi.** Mỗi bước dùng đầu ra của bước trước (mục 1.2).
3. **Đóng khung thời gian.** Lượt 1 (R0–R7) **tối đa 4 tuần**. "Research hết một lần" nghĩa là **phủ hết các ô một lần, đủ để quyết định** — không phải nghiên cứu tới khi hết câu hỏi. Tê liệt vì phân tích là rủi ro số một của kế hoạch này (mục 15).

### 0.5 Đã có sẵn — tái dùng, không làm lại

| Đã có | Ở đâu | Dùng cho |
|---|---|---|
| 7 văn bản pháp lý (mạng xã hội, dữ liệu cá nhân, TMĐT, ký quỹ lữ hành, thuế) | `Business.md` mục 7, `[L-01]`–`[L-07]` | R1 — chữ L |
| Số khách nội địa, quốc tế, tổng thu 2025; số liệu Gen Z | `Business.md` `[D-01]`–`[D-03]` | R1 — chữ E, S; R2 — cỡ thị trường |
| Điều khoản Google Maps, Google Play | `Business.md` `[P-01]`–`[P-05]` | R2, R3 |
| Công thức cỡ thị trường từ dưới lên | `Business.md` mục 2.5 | R2.5 |
| Strategy canvas nháp | `Business.md` mục 4.3 | R3.3 |
| Hiện trạng kỹ thuật có số đo | `status.md` | R4 |
| Tám loại hình đối thủ, mười nhóm đối tác | Đã gom vào tệp này: mục R3.1, R2.4 | R2, R3 |

---

## 1. Xương sống của kế hoạch

### 1.1 Bảy quyết định mà nghiên cứu phải phục vụ

Mỗi bước nghiên cứu phải nuôi **ít nhất một** quyết định dưới đây. Bước nào không nuôi quyết định nào thì cắt.

| Mã | Quyết định | Loại cửa | Bước nuôi chính |
|---|---|---|---|
| **QĐ1** | Levart kinh doanh gì — định vị | Một chiều | R1, R3, R5, R10 |
| **QĐ2** | Khách hàng đầu tiên là ai — TA, đầu cầu | Một chiều | R6, R9, R10 |
| **QĐ3** | Điểm đến/tỉnh mục tiêu đầu tiên | Hai chiều nhẹ | R1, R2, R6 |
| **QĐ4** | Tên "Levart" có dùng được không | Một chiều | R3 |
| **QĐ5** | Tính năng lõi của bản thử đầu tiên | Hai chiều | R3, R4, R9, R10 |
| **QĐ6** | Dòng thu thử đầu tiên | Hai chiều | R2, R4, R9 |
| **QĐ7** | Khi nào cần pháp nhân | Một chiều | R1 (chữ L), R4 |

### 1.2 Vì sao đúng thứ tự này

| Bước | Cần đầu vào từ | Đưa đầu ra cho |
|---|---|---|
| R1 Vĩ mô | — | O/T của SWOT (R5); tiêu chí "tăng trưởng" khi chọn TA (R6) |
| R2 Ngành | R1 (xu hướng, luật) | O/T (R5); "quy mô, mức cạnh tranh" của TA (R6); danh sách đối tác (R8) |
| R3 Đối thủ | R2 (định nghĩa ngành) | O/T (R5); khoảng trống định vị (R6); QĐ4 |
| R4 Nội bộ | — (làm song song được) | S/W (R5); "khả năng phục vụ" của TA (R6) |
| R5 SWOT/TOWS | R1–R4 | Phương án chiến lược → R6 |
| R6 Chọn TA | R5 | Các ô "chưa có dữ liệu" → R7 |
| R7 Khoảng trống | R6 + sổ câu hỏi | Danh sách câu hỏi → R8 |
| R8 Thiết kế | R7 | Công cụ → R9 |
| R9 Thực địa | R8 | Dữ liệu → R10 |
| R10 Tổng hợp | R1–R9 | Trả lời QĐ1–QĐ7 → Giai đoạn B |

### 1.3 Sổ câu hỏi nghiên cứu — tài liệu chạy xuyên suốt

Một bảng duy nhất trên Google Drive. Bước nào phát sinh câu hỏi chưa trả lời được thì ghi vào đây; R7 xếp hạng; R9 trả lời; R10 đóng sổ.

| Cột | Ví dụ |
|---|---|
| Mã | Q-017 |
| Câu hỏi | Sinh viên lên kế hoạch một chuyến tự túc mất bao lâu, bằng công cụ gì? |
| Phục vụ quyết định | QĐ1, QĐ5 |
| Phát sinh ở bước | R3 |
| Phương pháp | Phỏng vấn sâu |
| Nguồn / người trả lời | P01–P15 |
| Mức bằng chứng | B (định tính, n=15) |
| Trả lời | (điền ở R10) |
| Trạng thái | Mở / Đang hỏi / Đã trả lời / Bỏ |

---

## 2. R0 — Khung nghiên cứu

**Tuần 1 · 2–3 giờ · `ĐP` chủ trì, `ALL` tham gia**

**Mục đích:** chốt nghiên cứu để quyết định gì, phạm vi tới đâu, và khi nào được dừng.

| Việc | Cách làm | Xong khi |
|---|---|---|
| Bản tóm tắt nghiên cứu (research brief) | 1 trang, theo mẫu Phụ lục A: bối cảnh · 7 quyết định (mục 1.1) · **trong phạm vi**: du lịch nội địa, người trẻ, Android, 12 tháng · **ngoài phạm vi**: khách quốc tế, gia đình, tour trọn gói · thời hạn · ngân sách (giờ + tiền) · tiêu chí dừng | Có chữ ký mọi người |
| Mở sổ câu hỏi | Ghi sẵn mọi câu hỏi nhóm đã biết là chưa trả lời được | Sổ có ≥ 20 câu |
| Phân ghế theo bước | Theo mục 14 | Mỗi bước có một tên chịu trách nhiệm |
| Thư mục Drive | `01_Nghien-cuu/` với một thư mục con cho mỗi bước R0–R10 | Link gửi nhóm |

---

## 3. R1 — Môi trường vĩ mô: xu hướng + PESTLE

**Tuần 1–2 · `TN` chủ trì (xu hướng, S, T) · `ĐP` (P, E kinh tế, L) · `VH` (E môi trường)**

**Mục đích:** biết những lực lớn ngoài tầm kiểm soát của nhóm đang mở ra cơ hội gì và chặn đường ở đâu.

### 3.1 Quét xu hướng

Phân biệt ba loại trước khi ghi — Levart **không được xây trên mốt**:

| Loại | Thời gian | Cách nhận ra |
|---|---|---|
| Siêu xu hướng | 10 năm trở lên | Xuất hiện ở nhiều ngành, nhiều nước, có số liệu chính thức |
| Xu hướng | 3–10 năm | Có số tăng đều qua nhiều năm, có doanh nghiệp đang kiếm tiền từ nó |
| Mốt | Dưới 2 năm | Bùng trên mạng xã hội, chưa có số liệu năm thứ hai |

Xu hướng ứng viên cần kiểm — tất cả là (GT):

| Xu hướng | Bằng chứng hiện có | Cần tìm |
|---|---|---|
| Du lịch tự túc tăng ở người trẻ | `Business.md [D-03]` — mức C | Báo cáo gốc có cỡ mẫu |
| Ưu tiên trải nghiệm hơn vật chất | Lý thuyết kinh tế trải nghiệm `[M-15]` | Số chi tiêu trải nghiệm của người trẻ Việt Nam |
| AI tạo sinh thay công cụ lên kế hoạch | Chưa có | Tỷ lệ người trẻ dùng ChatGPT/Gemini để lên kế hoạch đi chơi |
| Video ngắn là nơi chọn điểm đến | Mức C | Khảo sát nguồn thông tin du lịch |
| Du lịch chữa lành, du lịch chậm, đi một mình | Mức C | Google Trends 5 năm (Việt Nam) |
| Quá tải ở điểm nóng | Mức C | Số khách theo mùa của tỉnh mục tiêu |
| **Bản đồ hành chính mới — 34 tỉnh thành** | **Mức A** `[N-01]` | Xem 3.2, chữ P |

**Vòng đời điểm đến** `[M-14]` — dùng để chọn tỉnh mục tiêu (QĐ3). Butler chia vòng đời điểm du lịch thành: khám phá → tham gia → phát triển → củng cố → trì trệ → suy giảm hoặc hồi sinh. Xếp từng điểm đến ứng viên vào một pha:

- Pha **tham gia/phát triển**: nhiều homestay, quán nhỏ mới mở đang tự đi tìm khách → dễ có đối tác.
- Pha **củng cố/trì trệ**: chuỗi lớn chiếm chỗ, quá tải, giá cao → khó chen, dễ làm khách thất vọng.

### 3.2 PESTLE

Với mỗi chữ: cần trả lời gì · đã có gì · phải tìm gì · tra ở đâu.

**P — Chính trị, chính sách**

| Đã có | Mức |
|---|---|
| **Nghị quyết 202/2025/QH15** (12/06/2025): cả nước còn **34 đơn vị hành chính cấp tỉnh** (28 tỉnh, 6 thành phố), từ 63; các đơn vị mới hoạt động từ 01/07/2025 `[N-01]` | A |
| **Quyết định 147/QĐ-TTg** (22/01/2020), Chiến lược phát triển du lịch đến 2030 — báo chí dẫn mục tiêu **≥ 160 triệu lượt khách nội địa** năm 2030 `[N-02]` | C — đối chiếu toàn văn |

> **Hệ quả lớn nhất của chữ P cho Levart:** sản phẩm "34 con dấu" **sống nhờ bản đồ mới**.
> - **Cơ hội:** đây là bộ sưu tập mới, cả nước đang học lại bản đồ, có thể chưa ai chiếm ý niệm "đi đủ 34 tỉnh mới".
> - **Rủi ro:** người đi vẫn nói theo **tên cũ**. Ví dụ Hà Giang cũ nay thuộc tỉnh Tuyên Quang mới (đối chiếu `[N-01]`). Người đi kể "mình vừa đi Hà Giang", con dấu lại ghi "Tuyên Quang" — liệu họ thấy hay, hay thấy lạ? Đây là **câu hỏi phải hỏi khách**, ghi ngay vào sổ.
> - **Hệ quả cho QĐ3:** danh sách ứng viên "Hà Giang, Ninh Bình, Mộc Châu" trong `week_index.md` T02.4 đang trộn một tỉnh cũ, một tỉnh và một địa danh trong tỉnh Sơn La. Khi chọn, phải tách **điểm đến** (nơi người ta nói là đi) khỏi **tỉnh** (đơn vị con dấu).

Phải tìm: chính sách kích cầu du lịch nội địa · chương trình chuyển đổi số ngành du lịch · nghị định hướng dẫn các luật đã có ở chữ L.

**E — Kinh tế**

| Đã có | Mức |
|---|---|
| ≈ 135,5 triệu lượt khách nội địa, tổng thu > 1 triệu tỷ đồng năm 2025 `Business.md [D-01]` | A/B |
| Gen Z chi 3–5 triệu đồng/chuyến `Business.md [D-03]` | C |

Phải tìm: chi tiêu và thu nhập của sinh viên, người mới đi làm (Cục Thống kê) · giá vé máy bay, xe khách nội địa và xu hướng · quy mô du lịch trực tuyến tại Việt Nam (báo cáo kinh tế số Đông Nam Á thường niên — mức B) · lạm phát dịch vụ.

**S — Xã hội**

Phải tìm: dân số 18–29 tại Hà Nội (Cục Thống kê — mức A) · lịch nghỉ, lịch thi của sinh viên (quyết định mùa chạy chiến dịch) · thái độ với việc đi cùng người lạ (đầu vào cho "đi theo cặp/chùm") · lo ngại an toàn khi đi một mình · thời gian dùng mạng xã hội.

**T — Công nghệ**

Đã có trong `status.md`: Android, Firebase, Google Maps, Gemini.
Phải tìm: **tỷ lệ Android/iOS ở người trẻ Việt Nam** — quyết định có cần làm iOS sớm không · độ phủ 4G ở vùng núi phía Bắc — quyết định tính năng chạy khi mất mạng · mức dùng thanh toán QR · mức dùng AI tạo sinh.

**L — Pháp lý**

Đã có (mức A): `Business.md` mục 7 — Nghị định 147/2024 (mạng xã hội), Luật Bảo vệ dữ liệu cá nhân 2025, Luật Thương mại điện tử 2025, Nghị định 168/2017 (ký quỹ lữ hành), Nghị quyết 198/2025 (thuế).
Phải tìm: Luật Du lịch 2017 (định nghĩa lữ hành, thẻ hướng dẫn viên) · Luật Sở hữu trí tuệ — dùng ở R3 · nghị định hướng dẫn Luật Bảo vệ dữ liệu cá nhân · chính sách Google Play với app có nội dung người dùng.

**E — Môi trường**

Phải tìm: lịch thiên tai theo vùng (mùa mưa lũ, sạt lở miền núi phía Bắc; tra báo cáo về các đợt bão lũ gần đây) · quá tải và rác thải ở điểm nóng · người trẻ có quan tâm du lịch bền vững không (hay chỉ nói là có).

### 3.3 Chấm điểm và cột "vì vậy"

Mỗi yếu tố tìm được ghi một dòng:

| Yếu tố | Cơ hội / Đe doạ | Tác động (1–3) | Khả năng xảy ra (1–3) | Khi nào (ngay / 1–2 năm / 3+) | Nguồn + mức | **Vì vậy Levart nên…** |
|---|---|---|---|---|---|---|
| Bản đồ 34 tỉnh mới | Cơ hội | 3 | 3 | Ngay | `[N-01]` A | Định vị theo bản đồ mới, kiểm cách gọi tên cũ/mới với khách |

Chỉ yếu tố có **tác động × khả năng ≥ 4** mới đi vào SWOT (R5).

> **Cột "vì vậy" là bắt buộc.** PESTLE không có cột hệ quả là giấy dán tường — đúng mà vô dụng.

**Xong khi:** mỗi chữ trong PESTLE có ≥ 3 dòng có nguồn; top 5 yếu tố có câu "vì vậy"; mọi câu hỏi chưa trả lời được đã vào sổ.

---

## 4. R2 — Ngành và thị trường: 5 lực + Mạng giá trị + cỡ thị trường

**Tuần 2 · `TN` (5 lực) · `VH` (Mạng giá trị, đối tác) · `ĐP` (cỡ thị trường)**

**Mục đích:** biết ngành này có dễ kiếm lời không, tiền đang chảy về ai, ai có thể hợp tác, và thị trường đủ lớn tới đâu.

### 4.1 Định nghĩa "ngành" trước khi phân tích

Kết quả 5 lực phụ thuộc hoàn toàn vào việc vẽ ranh giới ngành ở đâu. Levart đứng giữa ba ngành:

- Ứng dụng du lịch tiêu dùng (lên kế hoạch, lưu kỷ niệm)
- Trung gian dịch vụ du lịch (OTA, sàn trải nghiệm)
- Mạng xã hội, nội dung du lịch

Đề xuất phân tích với định nghĩa: **"ứng dụng hỗ trợ người trẻ đi du lịch nội địa tự túc tại Việt Nam"**, và ghi chú ở mỗi lực chỗ nào ranh giới làm kết quả đổi.

### 4.2 Năm lực — giả thuyết khởi đầu để kiểm

| Lực | Giả thuyết ban đầu (GT) | Vì sao nghĩ vậy | Bằng chứng cần tìm |
|---|---|---|---|
| **Đối thủ mới gia nhập** | **Cao** | Làm app ngày càng rẻ; chưa cần giấy phép khi còn nhỏ. Rào cản thật duy nhất là hiệu ứng mạng và quan hệ đối tác — thứ Levart cũng chưa có | Số app du lịch mới trên Play Store 12 tháng qua |
| **Quyền lực của người dùng** | **Rất cao** | Miễn phí, đổi app không tốn gì. Ngoại lệ có thể có: bộ sưu tập con dấu đã tích luỹ tạo chi phí chuyển đổi | Kiểm ở giai đoạn Bám dính |
| **Quyền lực nhà cung cấp — công nghệ** (Google Play, Google Maps, Firebase, Gemini) | **Cao** | Levart phụ thuộc hoàn toàn; họ đổi giá hoặc đổi luật là phải chịu | Bảng giá hiện hành; lịch sử đổi giá |
| **Quyền lực nhà cung cấp — dịch vụ địa phương** (homestay, quán, thuê xe) | **Thấp** từng cá thể | Rất phân mảnh. Nhưng họ có mặt trên nhiều kênh cùng lúc (OTA, Facebook) nên không phụ thuộc Levart | Số kênh trung bình mỗi homestay đang dùng — hỏi ở phỏng vấn đối tác |
| **Sản phẩm thay thế** | **Rất cao** | ChatGPT + Google Maps + TikTok + nhóm Facebook — miễn phí, đã quen | Hỏi trong phỏng vấn: "lần gần nhất bạn lên kế hoạch bằng gì?" |
| **Cạnh tranh trong ngành** | **Trung bình–cao**, tuỳ ranh giới | OTA lớn ở mảng đặt dịch vụ; mảng "hộ chiếu/thành tựu" có thể còn trống | R3 |

**Kết luận khởi đầu (GT):** nếu Levart là "một app du lịch nữa" thì ngành **kém hấp dẫn** — người dùng mạnh, thay thế nhiều, gia nhập dễ. Ngành **hấp dẫn hơn** nếu Levart chiếm được một ngách tạo **chi phí chuyển đổi** (bộ sưu tập con dấu) và **quan hệ địa phương** (khó sao chép). Nghiên cứu phải kiểm hai điều kiện đó.

**Lưu ý cho nền tảng hai phía.** 5 lực giả định một chuỗi đi một chiều. Khi Levart có đối tác, nó trở thành nền tảng hai phía `[M-16][M-17]` — phải trả lời thêm ba câu:

1. Hiệu ứng mạng: thêm người đi có làm Levart giá trị hơn với **người đi khác** không? Với **đối tác** không?
2. Đa kênh: người đi và đối tác có dùng song song nhiều nền tảng không? (Nếu có — rất khó giữ.)
3. Gà và trứng: phía nào phải có trước? Kế hoạch hiện tại chọn **người đi trước, đối tác sau** — kiểm lại.

### 4.3 Mạng giá trị — "những bên có thể sẽ hợp tác cùng"

Mạng giá trị `[M-06]` đặt doanh nghiệp ở giữa bốn nhóm. Nhóm quan trọng mà 5 lực bỏ sót là **bên bổ trợ**: bên mà khi khách có cả sản phẩm của họ lẫn của mình thì sản phẩm của mình đáng giá hơn.

```
                              KHÁCH HÀNG
             Gen Z đi tự túc (đầu cầu) · nhóm lớp/CLB · (sau) tỉnh, thương hiệu B2B
                                  │
   ĐỐI THỦ ────────────────── LEVART ────────────────── BÊN BỔ TRỢ
   (R3)                           │                    nhà xe, ví/ngân hàng, nhà mạng,
                                  │                    bảo hiểm, trường/CLB, creator,
                                  │                    sở du lịch, ban quản lý khu du lịch
                             NHÀ CUNG CẤP
          Công nghệ: Google Play, Google Maps, Firebase, Gemini, nhà in
          Dịch vụ địa phương: 10 nhóm bên dưới
```

**Mười nhóm nhà cung cấp dịch vụ địa phương** (sẽ là đối tác):

| # | Nhóm | Ví dụ |
|---|---|---|
| 1 | Chỗ ở | Homestay nhà sàn, nhà nghỉ nhỏ, hostel, camping/glamping, ngủ nhờ nhà dân |
| 2 | Ăn uống | Quán đặc sản, cà phê view, bếp ăn chung ở homestay, người dạy nấu món bản địa |
| 3 | Di chuyển đường dài | Nhà xe tuyến Hà Nội – tỉnh |
| 4 | Di chuyển tại chỗ | Thuê xe máy, easy rider, ô tô có lái, xe đạp, đò, gara dọc đường |
| 5 | Người dẫn và trải nghiệm | Người bản địa dẫn trekking, porter, người dẫn vào bản, nghệ nhân dạy nghề, nông dân mùa vụ |
| 6 | Hoạt động mạo hiểm | Leo núi, kayak, zipline, cắm trại đêm |
| 7 | Hàng hoá và quà | Đặc sản đóng gói, đồ thủ công, cho thuê đồ phượt |
| 8 | Hậu cần nhỏ | Giữ đồ, giặt đồ |
| 9 | An toàn và hỗ trợ | Y tế, hiệu thuốc, cứu hộ — **chỉ thông tin**, không phải đối tác kiếm tiền |
| 10 | Cộng đồng và tổ chức | Admin nhóm Facebook tỉnh, creator địa phương, ban quản lý khu du lịch |

**Điểm then chốt của Mạng giá trị: cùng một bên có thể vừa là đối thủ vừa là đối tác** (hợp–cạnh, co-opetition). Ví dụ: OTA là đối thủ ở mảng đặt phòng nhưng là đối tác ở mảng affiliate. Với mỗi bên tiềm năng ghi:

| Bên | Vai trò (hợp tác / cạnh tranh / cả hai) | Họ được gì | Levart được gì | Ai cần ai hơn | Giai đoạn bắt đầu |
|---|---|---|---|---|---|

### 4.4 Cỡ thị trường

Dùng lại công thức **từ dưới lên** trong `Business.md` 2.5. Ba tầng:

| Tầng | Nghĩa | Cách tính cho Levart |
|---|---|---|
| **TAM** | Toàn bộ nhu cầu nếu chiếm 100% | Người 18–29 cả nước đi nội địa tự túc ≥ 1 lần/năm × chuyến/năm × giá trị Levart thu được mỗi chuyến |
| **SAM** | Phần với tới được bằng sản phẩm và kênh hiện có | Giới hạn ở Hà Nội, Android, người dùng mạng xã hội |
| **SOM** | Phần có thể lấy thực tế trong 12 tháng | Số sinh viên tiếp cận được qua trường/CLB × tỷ lệ chuyển đổi giả định (ghi rõ GT) |

Mỗi biến một dòng: giá trị · nguồn · mức bằng chứng. Biến chưa có số thì ghi "chưa có" và đưa vào sổ câu hỏi — **không đoán cho đủ ô**. Ước tính từ trên xuống (lấy tổng thị trường nhân %) chỉ dùng để **soát** con số từ dưới lên.

**Xong khi:** bảng 5 lực có bằng chứng cho từng lực; bản đồ Mạng giá trị có ≥ 15 bên; TAM/SAM/SOM có ≥ 3/4 biến có nguồn.

---

## 5. R3 — Đối thủ và kiểm trùng

**Tuần 2–3 · `TN` + `SP` (đối thủ) · `ĐP` (kiểm trùng pháp lý)**

**Mục đích:** biết ai đang làm gì, khoảng trống ở đâu, và **Levart có trùng ai không** — về chức năng, về tên, về sở hữu trí tuệ.

### 5.1 Tám loại hình đối thủ

> Tên app dưới đây viết từ hiểu biết đến giữa 2026 — tính năng, tình trạng còn hoạt động đều **phải kiểm khi cài thật**.

| # | Loại hình | Câu hỏi | Ví dụ để kiểm |
|---|---|---|---|
| 1 | **Sưu tầm, hộ chiếu địa điểm** — gần Levart nhất | Đã có ai chiếm ý niệm "đi đủ, đóng dấu"? | Polarsteps, Swarm, Geocaching, NomadMania, Been; sổ đóng dấu ở đền chùa và nhà ga Nhật Bản; **tra Play Store các app "check-in tỉnh thành" của Việt Nam** |
| 2 | Lập lịch trình, trợ lý AI | Người ta dùng thật hay chỉ thử một lần? | Wanderlog, TripIt, Mindtrip; ChatGPT, Gemini |
| 3 | Bản đồ, lưu địa điểm | Thứ khách đang dùng thật thay cho Levart | Google Maps (danh sách đã lưu, dòng thời gian), AllTrails, Komoot |
| 4 | Nội dung, mạng xã hội | Người trẻ quyết định đi đâu ở đâu? | TikTok, nhóm Facebook, Threads, Instagram, Xiaohongshu |
| 5 | OTA, sàn trải nghiệm | Ai đang ăn phần tiền Levart định lấy? | Booking, Agoda, Traveloka, Klook, Vexere, Airbnb Experiences |
| 6 | Gom nhà cung cấp nhỏ lẻ | Họ thuyết phục, kiểm chất lượng, xác nhận giao dịch với quán nhỏ thế nào? | Bounce, Stasher (gửi đồ), Too Good To Go, Grab |
| 7 | Thành tựu và thói quen (ngoài du lịch) | Cái gì khiến người ta quay lại hằng tuần? | Strava, Untappd, Duolingo, Pokémon GO |
| 8 | Sản phẩm vật lý từ dữ liệu số | Bán bản in giá bao nhiêu, đóng gói thế nào? | Sách hành trình in của Polarsteps, photobook |

### 5.2 Hồ sơ đối thủ

Mỗi đối thủ một hồ sơ (mẫu Phụ lục D): khách mục tiêu · việc họ giải cho khách · tính năng lõi · cách kiếm tiền và giá · lực kéo (lượt tải, điểm và **số lượng** đánh giá trên store) · điểm mạnh · điểm yếu · họ sẽ phản ứng thế nào nếu Levart lớn lên.

> **Mẹo rẻ nhất:** đọc **50 đánh giá 1–2 sao gần nhất** của mỗi app đối thủ. Đó là phỏng vấn khách hàng miễn phí — người dùng tự viết ra nỗi đau, bằng lời của họ. Ghi các nỗi đau lặp lại vào sổ câu hỏi để kiểm ở R9.

### 5.3 Bản đồ nhóm chiến lược và strategy canvas

- **Bản đồ nhóm chiến lược** `[M-04]`: đặt đối thủ lên hai trục, tìm ô trống. Gợi ý trục ngang **"chỉ thông tin ↔ bán giao dịch"**, trục dọc **"công cụ cá nhân ↔ cộng đồng"**.
- **Strategy canvas** `[M-27]`: cập nhật bản nháp trong `Business.md` 4.3 bằng dữ liệu thật vừa thu.

Đầu ra cần có: **một câu trả lời "Levart định thắng ở khoảng trống nào"** — đầu vào cho QĐ1.

### 5.4 Kiểm trùng — ba lớp

| Lớp | Câu hỏi | Bản chất | Cách kiểm | Nếu trùng thì |
|---|---|---|---|---|
| **Tên** | Có ai đã dùng "Levart" hoặc tên dễ nhầm trong cùng lĩnh vực? | **Pháp lý — nhãn hiệu** | Tra cơ sở dữ liệu nhãn hiệu của Cục Sở hữu trí tuệ trên nền WIPO Publish `[N-04]` (chỉ truy cập từ IP Việt Nam) · WIPO Global Brand Database `[N-05]` · Google Play, App Store · tên miền · tên tài khoản TikTok, Facebook, Instagram · tìm cả biến thể phát âm gần giống. Nhóm Nice dự kiến 9, 35, 39, 41, 42 — xác nhận với người hiểu SHTT | Trùng trong cùng nhóm → **đổi tên trước khi in ấn, chạy nội dung, giới thiệu tên với người được phỏng vấn**. Đây là quyết định cửa một chiều (QĐ4) |
| **Chức năng** | Đã có ai làm "hộ chiếu 34 tỉnh / check-in GPS / lịch trình AI"? | **Cạnh tranh** — ý tưởng và chức năng không được bảo hộ độc quyền | Ma trận tính năng từ 5.2 | **Không phải lý do dừng.** Là lý do phải trả lời rõ "Levart khác ở đâu, vì sao khách chọn" → đưa sang R5 |
| **Biểu hiện** | (a) Levart có đang dùng thứ gì của người khác không? (b) Thứ của Levart được bảo hộ ra sao? | **Quyền tác giả** | (a) Kiểm giấy phép từng tài nguyên đang dùng: ảnh mẫu, font, icon, thư viện. (b) Chương trình máy tính được bảo hộ dưới dạng **quyền tác giả**, không được bảo hộ với danh nghĩa sáng chế — Luật SHTT Điều 59 khoản 2 `[N-03]`, đối chiếu bản sửa đổi 2022 | (a) Thay tài nguyên không rõ giấy phép. (b) Xem phần bảo vệ bên dưới |

**Bảo vệ cái của mình** (đưa vào danh mục pháp lý):

- Nhãn hiệu: **chữ "Levart"** và **hình con dấu** (nếu dùng làm biểu tượng).
- Quyền tác giả với mã nguồn và thiết kế con dấu tự phát sinh khi tạo ra, nhưng nên **lưu bằng chứng ngày tạo**; có thể đăng ký tại cơ quan bản quyền để làm bằng chứng khi tranh chấp.
- Giữ chỗ tên miền và tên tài khoản mạng xã hội càng sớm càng tốt — rẻ nhất trong mọi việc.

**Xong khi:** ≥ 8 hồ sơ đối thủ (mỗi loại hình ≥ 1); bản đồ nhóm chiến lược có ô trống được chỉ ra; báo cáo kiểm trùng 3 lớp có ảnh chụp kết quả tra cứu; QĐ4 có kết luận sơ bộ.

---

## 6. R4 — Phân tích nội bộ

**Tuần 3 · `ĐP` (tài chính, năng lực) · `SP` (công nghệ) · `TN` (VRIO). Làm song song với R2–R3 được.**

**Mục đích:** biết mình thật sự có gì, cái nào là lợi thế, và đáp ứng được bao nhiêu việc.

### 6.1 Kiểm kê nguồn lực

Ba nhóm theo Grant `[M-10]`. Mỗi dòng phải có **bằng chứng**, không có bằng chứng thì ghi "chưa kiểm chứng".

**Hữu hình**

| Nguồn lực | Hiện trạng | Bằng chứng |
|---|---|---|
| Tài chính | Tiền túi của nhóm; dự án Firebase đã bật dịch vụ lưu ảnh (yêu cầu gói trả theo mức dùng) | `status.md` mục 8 — số tiền cụ thể chưa kiểm chứng |
| Công nghệ, hạ tầng | App Android 15 màn hình; đăng nhập, bảng tin, lưu ảnh lên máy chủ, bản đồ, lịch trình AI chạy được; 11 tài khoản, 5 bài trên máy chủ | `status.md` mục 1, 2, 8 |
| Khả năng chịu tải | Chưa từng thử với người dùng thật | `status.md` mục 7.3 |
| Thiết bị | Cần điện thoại Android thật để kiểm các tình huống máy ảo không làm được | `status.md` mục 6 — có hay chưa: chưa kiểm chứng |

**Vô hình**

| Nguồn lực | Hiện trạng |
|---|---|
| Thương hiệu | Tên Levart — **chưa đăng ký**; khái niệm "hộ chiếu, con dấu 34 tỉnh" |
| Sở hữu trí tuệ | Mã nguồn app; thiết kế con dấu — xác nhận quyền sở hữu và giấy phép tài nguyên ở R3 |
| Dữ liệu | Danh mục tỉnh và địa điểm có sẵn trong app; **chưa có dữ liệu người dùng thật** |
| Quan hệ | Mạng lưới sinh viên Bách khoa Hà Nội và các trường lân cận; **chưa có đối tác** |
| Văn hoá làm việc | Kỷ luật bằng chứng đã có ở phần kỹ thuật (`status.md`) |

**Con người**

| Nguồn lực | Hiện trạng |
|---|---|
| Kỹ năng | Android, tích hợp AI — có bằng chứng (app chạy). Marketing, bán hàng đối tác, pháp lý, tài chính — **chưa có bằng chứng** |
| Thời gian | Tổng giờ/tuần theo bảng giờ của nhóm (`ROLE.md`) |
| Hiểu khách | Nhóm là người trẻ — **vừa là lợi thế** (hiểu ngôn ngữ, kênh) **vừa là bẫy** (tưởng mình là khách) |

### 6.2 Chữ "ok" nghĩa là gì

Flow gốc ghi "vốn: ok, kỹ thuật: ok, hạ tầng: ok, khả năng đáp ứng: ok". Mỗi chữ "ok" phải qua một ngưỡng đo được:

| Nguồn lực | "Ok" nghĩa là | Bằng chứng cần |
|---|---|---|
| **Vốn** | Chi được chi phí cố định 6 tháng + một lô in thử mà không ảnh hưởng sinh hoạt của thành viên | Dòng tiền v0 + sổ quỹ |
| **Kỹ thuật** | Làm xong các việc chặn thí nghiệm (con dấu theo tài khoản, báo cáo nội dung, đo phễu) trong ≤ 6 tuần | Ước lượng giờ của `SP`, đối chiếu `status.md` |
| **Hạ tầng** | Một nhóm 150 người dùng trong một tuần không gặp lỗi chặn; đo được chi phí trên mỗi người | Chưa có — chỉ biết sau khi có người dùng thật |
| **Khả năng đáp ứng** | Tổng giờ thật ≥ khối lượng mỗi chu kỳ; mỗi ghế có ≥ 1 người | Bảng giờ × danh sách việc |
| **Marketing, bán hàng** | Có người từng làm nội dung có số đo, hoặc từng gọi bán hàng | Hồ sơ thành viên |
| **Pháp lý, tài chính** | Có người hiểu luật tư vấn được; có người đọc được dòng tiền | Tên người cụ thể |

> **Nhận định của người soạn:** ở nhóm khởi nghiệp sinh viên, ràng buộc thật thường là **giờ của con người**, không phải vốn hay kỹ thuật (giả thuyết G8 trong `Business.md` 2.4). Kiểm kê nguồn lực phải đo giờ trước tiên.

### 6.3 VRIO

Hỏi bốn câu cho từng nguồn lực `[M-09]`: có **giá trị** (V) với khách không · có **hiếm** (R) không · có **khó bắt chước** (I) không · tổ chức có **khai thác được** (O) không. Bảng điền sẵn là (GT):

| Nguồn lực | V | R | I | O | Hàm ý cạnh tranh |
|---|---|---|---|---|---|
| App Android hiện có | Có | Không | Không | Có | **Ngang bằng** — không phải lợi thế |
| Lịch trình AI | Có | Không | Không | Có | **Ngang bằng** — ChatGPT miễn phí làm được |
| Khái niệm con dấu GPS 34 tỉnh | Có (GT) | Có thể — kiểm ở R3 | Không — dễ sao chép | Có | **Lợi thế tạm thời** — chỉ giữ được bằng tốc độ |
| Tên và hình con dấu, khi đã đăng ký | Có | Có | Có — luật bảo hộ | Cần quy trình | **Lợi thế bền nếu** xây được độ nhận biết |
| Dữ liệu địa điểm bản địa do cộng đồng đóng góp | Có | Có | Có — tích luỹ theo thời gian | Chưa có | **Lợi thế bền tiềm năng** |
| Quan hệ đối tác theo tỉnh | Có | Có | Khá — tốn thời gian xây | Chưa có | **Lợi thế bền tiềm năng** |
| Mạng lưới sinh viên Hà Nội | Có | Một phần | Một phần | Có | **Lợi thế khởi động**, không bền |

**Kết luận khởi đầu (GT):** mã nguồn **không** phải lợi thế. Lợi thế bền chỉ có thể nằm ở những thứ **tích luỹ dần** — dữ liệu, cộng đồng, quan hệ, thương hiệu. Câu hỏi cho R9: khách có thật sự coi trọng những thứ đó không?

### 6.4 Chuỗi giá trị → mạng giá trị

**Bước 1 — Dùng chuỗi giá trị của Porter `[M-05]` làm danh mục kiểm**, để không sót hoạt động nào:

| Nhóm | Hoạt động | Ở Levart là |
|---|---|---|
| Chính | Đầu vào | Thu thập dữ liệu địa điểm, tuyển đối tác |
| | Vận hành | App, AI lập lịch, xác thực GPS, kiểm duyệt nội dung |
| | Đầu ra | Phân phối qua cửa hàng ứng dụng, chia sẻ con dấu ra ngoài |
| | Marketing và bán hàng | Nội dung, kênh, creator |
| | Dịch vụ | Hỗ trợ người dùng, thông tin khẩn cấp |
| Hỗ trợ | Hạ tầng tổ chức | Pháp lý, kế toán, quy trình |
| | Nhân sự | Ghế, giờ, thoả thuận |
| | Phát triển công nghệ | App, dữ liệu |
| | Thu mua | Firebase, Google Maps, AI, nhà in |

**Bước 2 — Phân tích Levart như mạng giá trị** `[M-07]`. Levart không sản xuất rồi giao hàng; nó **kết nối** người đi với bên cung. Doanh nghiệp loại này có ba hoạt động chính:

| Hoạt động | Ở Levart là | Câu hỏi phân tích |
|---|---|---|
| **Mở rộng mạng lưới và quản lý cam kết** | Tuyển người dùng và đối tác; điều khoản; ưu đãi | Tốn bao nhiêu giờ để có một người dùng kích hoạt? Một đối tác? |
| **Cung cấp dịch vụ kết nối** | Gợi ý, ghép nhóm, xác nhận ưu đãi, đóng dấu | Khoảnh khắc nào khách thấy giá trị? Đối tác thấy giá trị? |
| **Vận hành hạ tầng mạng lưới** | App, máy chủ, dữ liệu | Chi phí tăng theo người dùng thế nào? |

Giá trị của mạng lưới tăng theo số thành viên. Vì vậy câu hỏi quan trọng nhất ở bước này là **hoạt động nào Levart tự làm, hoạt động nào giao đối tác** — đối chiếu quy tắc tự xây / hợp tác / tổng hợp trong `Business.md` 6.1.

### 6.5 Năng lực đáp ứng

Bảng kỹ năng: mỗi người một dòng, mỗi kỹ năng một cột (phát triển, thiết kế, nội dung, phỏng vấn, bán hàng, pháp lý, tài chính), chấm 0–3, kèm giờ/tuần. So với khối lượng việc trong `week_index.md` → danh sách thiếu hụt → quyết định cần tìm thêm người hay cố vấn ở đâu.

**Xong khi:** bảng kiểm kê có bằng chứng từng dòng; mỗi chữ "ok" có ngưỡng và trạng thái; bảng VRIO; sơ đồ mạng giá trị có cột tự làm/đối tác; bảng kỹ năng đủ người.

---

## 7. R5 — Tổng hợp: SWOT → TOWS

**Tuần 4 · một buổi 3 giờ cùng R6, R7 · `TN` chủ trì, `ALL`**

**Mục đích:** ghép bên ngoài với bên trong thành các phương án chiến lược để lựa chọn.

**Quy tắc vào SWOT:** mỗi ô **phải trỏ về một dòng** của R1–R4 (mã dòng + mức bằng chứng). Không có mã thì không vào. SWOT làm bằng cách "ngồi nghĩ" là lỗi phổ biến nhất của công cụ này.

| | Lấy từ |
|---|---|
| **S** Điểm mạnh, **W** Điểm yếu | R4 |
| **O** Cơ hội, **T** Đe doạ | R1, R2, R3 |

**TOWS** `[M-11]` biến SWOT thành hành động — đây mới là phần có giá trị:

| | **O — Cơ hội** | **T — Đe doạ** |
|---|---|---|
| **S — Điểm mạnh** | **SO:** dùng điểm mạnh để nắm cơ hội. Ví dụ (GT): nhóm làm app nhanh + bản đồ 34 tỉnh mới chưa ai sưu tầm → ra hộ chiếu 34 tỉnh trước mùa du lịch | **ST:** dùng điểm mạnh để chặn đe doạ |
| **W — Điểm yếu** | **WO:** dùng cơ hội để khắc phục điểm yếu. Ví dụ (GT): chưa có đối tác + homestay nhỏ đang tự tìm khách trẻ → đổi khách lấy ưu đãi, chưa cần tiền | **WT:** phòng thủ — tránh chỗ vừa yếu vừa bị đe doạ |

**Đầu ra:** 2–3 phương án chiến lược, mỗi phương án một đoạn. Ví dụ hướng (GT): **A** — ngách "hộ chiếu, thành tựu" · **B** — trợ lý lịch trình · **C** — sàn dịch vụ bản địa. Đánh giá từng phương án theo: khớp TOWS · cần nguồn lực gì · rủi ro lớn nhất · cần dữ liệu gì để chọn.

---

## 8. R6 — Chọn TA (STP)

**Tuần 4 · `TT` chủ trì**

**Mục đích:** chọn **một** nhóm khách để phục vụ trước, có lý do bằng số.

### 8.1 Phân khúc

Biến số phân khúc cho du lịch `[M-12]`:

| Loại | Biến số |
|---|---|
| Nhân khẩu học | Tuổi; đang học / mới đi làm; thu nhập |
| Địa lý | Nơi xuất phát |
| Hành vi | Tần suất đi; tự túc hay tour; cách lên kế hoạch; kênh đặt dịch vụ |
| Tâm lý | Động cơ: khám phá, chữa lành, chụp ảnh, thử thách, kết nối |
| Dịp | Cuối tuần, lễ, hè |

Phân khúc ứng viên (GT): sinh viên Hà Nội đi tự túc · người mới đi làm ở Hà Nội · nhóm lớp/CLB đi tập thể · "thợ săn tỉnh" (người đang đếm số tỉnh đã đi) · dân phượt xe máy · khách quốc tế đi ba lô (ngoài phạm vi — chỉ để so sánh).

### 8.2 Chấm điểm: hấp dẫn × khả năng phục vụ

| Phân khúc | Quy mô | Tăng trưởng | Khả năng chi trả | Mức đau | Mức cạnh tranh | **Hấp dẫn** | Tiếp cận được bằng kênh mình có | Khớp nguồn lực (VRIO) | **Phục vụ được** |
|---|---|---|---|---|---|---|---|---|---|
| Sinh viên HN tự túc | ? | ? | ? | ? | ? | | 3 | ? | |

- Mỗi tiêu chí chấm 1–3 và **ghi nguồn**. Tiêu chí hấp dẫn lấy từ R1–R3; tiêu chí phục vụ lấy từ R4.
- **Ô nào chưa có dữ liệu thì ghi "?"** — chính các ô "?" là **danh sách câu hỏi phải đi hỏi** ở R7. Đây là cách nối tự nhiên từ chọn TA sang phỏng vấn.

### 8.3 Chọn và định vị sơ bộ

- Chọn đầu cầu theo tiêu chí của Aulet `[M-13]`: khách **mua giống nhau**, **nói chuyện với nhau**, **nhóm tiếp cận được**, có đủ tiền và động lực.
- Persona v0 (GT): một trang.
- Định vị sơ bộ theo trình tự Dunford `[M-28]`: khách đang dùng gì thay thế → Levart có gì độc nhất → điều đó mang lại giá trị gì → ai quan tâm nhất tới giá trị đó → Levart thuộc hạng mục nào.

**Xong khi:** bảng chấm điểm đủ phân khúc; TA được chọn có lý do; persona v0; mọi ô "?" đã vào sổ câu hỏi.

---

## 9. R7 — Khoảng trống dữ liệu, chọn phương pháp, có cần phỏng vấn không

**Tuần 4 · `ĐP` chủ trì, `TT`**

**Mục đích:** biết chính xác còn thiếu dữ liệu gì, và với từng thứ thì lấy bằng cách nào.

### 9.1 Xếp hạng câu hỏi

Gom mọi câu "Mở" trong sổ + mọi ô "?" của R6. Đặt từng câu lên bản đồ giả định `[M-25]` hai trục:

```
              QUAN TRỌNG VỚI QUYẾT ĐỊNH
                        ▲
       Kiểm NGAY        │      Đã đủ — dùng luôn
   (quan trọng, chưa    │   (quan trọng, đã có bằng chứng)
    có bằng chứng)      │
  ──────────────────────┼──────────────────────► ĐÃ CÓ BẰNG CHỨNG
       Ghi lại, để sau  │      Bỏ qua
                        │
```

Chỉ ô **"Kiểm ngay"** đi tiếp vào R8.

### 9.2 Cây quyết định phương pháp

```
Câu hỏi
 ├─ Có dữ liệu thứ cấp mức A hoặc B?                → TRA CỨU (quay lại R1–R3) — không phỏng vấn
 ├─ Hỏi "vì sao / như thế nào / cảm thấy gì"?       → PHỎNG VẤN SÂU (định tính)
 ├─ Hỏi về bên cung, ngành, quy định?               → PHỎNG VẤN CHUYÊN GIA, ĐỐI TÁC
 ├─ Hỏi "bao nhiêu % / phân bố / so sánh nhóm"?      → KHẢO SÁT (định lượng) — làm SAU phỏng vấn sâu
 └─ Hỏi "họ có thật sự làm / trả tiền không"?        → THỬ NGHIỆM HÀNH VI (đặt trước, trang đích)
                                                       — phỏng vấn KHÔNG trả lời được; chuyển sang Giai đoạn B
```

### 9.3 Ví dụ: dữ liệu nào cần phỏng vấn, dữ liệu nào không

| Câu hỏi | Đã có? | Phương pháp |
|---|---|---|
| Năm 2025 Việt Nam có bao nhiêu lượt khách nội địa? | Có — `Business.md [D-01]`, mức A/B | Tra cứu — **không** phỏng vấn |
| Hà Nội có bao nhiêu người 18–29 tuổi? | Có ở Cục Thống kê, mức A | Tra cứu |
| App X có tính năng đóng dấu không? | Cài app là biết | Tra cứu (R3) |
| Vì sao sinh viên chọn tự túc thay vì tour? | Báo chí nói là "có", không nói "vì sao" | **Phỏng vấn sâu** |
| Lên kế hoạch một chuyến mất bao lâu, bằng công cụ gì, khó chịu ở bước nào? | Không | **Phỏng vấn sâu** |
| Có ai đang đếm số tỉnh đã đi? Đếm bằng gì? | Không | **Phỏng vấn sâu** |
| Kể chuyến đi theo tên tỉnh cũ hay mới? Con dấu theo tỉnh mới có lạ không? | Không | **Phỏng vấn sâu**, rồi khảo sát |
| Chủ homestay tìm khách trẻ bằng kênh nào, trả bao nhiêu? | Không | **Phỏng vấn đối tác** |
| Bao nhiêu % sinh viên đi tự túc ≥ 1 lần/năm? | Chỉ có số báo chí mức C | **Khảo sát** (sau phỏng vấn) |
| Tỷ lệ Android/iOS trong TA? | Có thể có báo cáo mức B; nếu không | **Khảo sát** |
| Có ai trả tiền mua hộ chiếu in không? | Không — và **phỏng vấn không trả lời được** | **Thử nghiệm đặt trước** ở Giai đoạn B |

### 9.4 Có nên đi phỏng vấn không?

**Phỏng vấn khi cả ba điều kiện đúng:**

1. Có ít nhất một câu hỏi ưu tiên cao thuộc loại "vì sao / như thế nào".
2. Một quyết định cửa một chiều phụ thuộc vào câu trả lời đó.
3. Không có dữ liệu mức A/B trả lời được.

**Với Levart hiện tại: có.** QĐ1, QĐ2, QĐ5 đều phụ thuộc câu hỏi loại "vì sao / như thế nào" của một nhóm khách hẹp mà không nguồn thứ cấp nào trả lời. Nhưng **phạm vi phỏng vấn chỉ bằng đúng danh sách ở 9.1** — không hơn.

**Xong khi:** mọi câu ô "Kiểm ngay" có phương pháp; danh sách câu hỏi phỏng vấn và câu hỏi khảo sát tách riêng; danh sách câu hỏi chuyển sang Giai đoạn B tách riêng.

---

## 10. R8 — Thiết kế nghiên cứu sơ cấp

**Tuần 5 · `TT` chủ trì · `VH` (kịch bản đối tác) · `ĐP` (đồng ý, dữ liệu cá nhân)**

**Thiết kế tổng:** **hỗn hợp tuần tự khám phá** `[M-19]` — định tính trước để biết **hỏi gì**, định lượng sau để biết **bao nhiêu**.

### 10.1 Hình thức phỏng vấn — chọn đúng công cụ

| Hình thức | Dùng khi | Không dùng khi | Cỡ | Levart |
|---|---|---|---|---|
| **Phỏng vấn sâu 1–1**, bán cấu trúc | Hiểu hành vi, động cơ, nỗi đau | Đo tỷ lệ | 12–15 / phân khúc | **Chính** |
| Phỏng vấn chuyển đổi (người vừa đổi cách làm) `[M-31]` | Người vừa bỏ tour sang tự túc, vừa đổi app | — | 5–8 | Lồng vào phỏng vấn sâu |
| Phỏng vấn chuyên gia, đối tác | Bên cung, ngành, quy định | Để hỏi thay cho khách | 5–8 | Có (`VH`) |
| Đi cùng, quan sát tại chỗ | Thấy hành vi thật, không qua lời kể | Khi thiếu giờ | 1–2 chuyến | Có, ít |
| Nhật ký chuyến đi qua Zalo | Ghi trong lúc đi, chống quên | — | 3–5 người | Tuỳ chọn |
| Thảo luận nhóm (focus group) `[M-32]` | Phản ứng với tên, hình, ý tưởng sáng tạo | Hành vi cá nhân, sẵn sàng trả tiền — tâm lý bầy đàn làm sai lệch | 6–8 / nhóm | **Hạn chế** — chỉ để thử tên, hình |
| Khảo sát trực tuyến | Đo tỷ lệ, so nhóm | Hỏi "vì sao" | ≥ 150 | Sau phỏng vấn sâu |

### 10.2 Chọn mẫu

- **Chọn mẫu có chủ đích** theo tiêu chí TA ở R6, qua **bảng sàng lọc** 5 câu (Phụ lục G).
- **Hạn ngạch** để đa dạng: giới; năm học hoặc năm đi làm; tần suất đi; từng/chưa từng đi tour.
- **Điểm bão hoà:** trong một nhóm tương đối đồng nhất, chủ đề mới thường cạn sau khoảng **12 cuộc** `[M-20]` → đặt 12–15 cho phân khúc chính; **dừng khi 3 cuộc liên tiếp không ra chủ đề mới**.
- **Đối tác:** 5–8 người (homestay, quán, thuê xe, người dẫn, admin nhóm Facebook tỉnh).
- **Chống thiên kiến lịch sự:** tối đa **1/3** là người quen trực tiếp; còn lại qua giới thiệu.
- **Khảo sát** — cỡ mẫu theo Cochran `[M-22]`: `n = z² × p(1−p) / e²`, với độ tin cậy 95% (z = 1,96) và p = 0,5:

| Sai số cho phép | Cỡ mẫu cần |
|---|---|
| ± 5% | 385 |
| ± 7% | 196 |
| ± 8% | 150 |

> Mẫu lấy thuận tiện qua trường **không đại diện** cho "người trẻ Việt Nam". Dùng để so các nhóm **bên trong TA**, ghi rõ hạn chế trong báo cáo, không suy rộng.

### 10.3 Kịch bản phỏng vấn sâu (bán cấu trúc, 30–40 phút)

Kịch bản đi theo hình **phễu**: từ chung tới riêng, từ chuyện đã xảy ra tới cảm xúc. Bảy câu hỏi đích của nhóm nằm ở khối 1–6; khối 7 lấy từ danh sách R7.

| Khối | Phút | Mục tiêu (câu hỏi đích của nhóm) | Hỏi thế nào | Gợi hỏi sâu |
|---|---|---|---|---|
| 0 Mở đầu | 2 | Tạo tin cậy | "Mình đang tìm hiểu cách người trẻ đi du lịch. Không có đúng sai. Mình xin ghi âm nhé?" | — |
| 1 Hồ sơ | 3 | **Đặc điểm của khách** | Tuổi, học/làm, hay đi với ai, ngân sách chuyến gần nhất | — |
| 2 Lịch sử | 5 | **Tần suất đi du lịch** | "12 tháng qua bạn đi mấy chuyến? Kể chuyến gần nhất." | Đi đâu, mấy ngày, quyết định từ lúc nào |
| 3 Thông tin | 5 | **Thông tin có được từ đâu** | "Bạn biết tới chỗ đó từ đâu? Lên kế hoạch bằng gì, mất bao lâu?" | Ghi **tên từng công cụ** |
| 4 Hình thức | 5 | **Đi thế nào, cảm giác ra sao** + **tại sao** | "Bạn đi tự túc, đi tour hay đi nhóm? Vì sao chọn vậy? Lúc đó thấy thế nào?" | "Vì sao" hỏi tiếp 2–3 tầng |
| 5 Trải nghiệm | 8 | **Thú vị và chưa hài lòng** — nếu qua bên thứ ba thì hỏi rõ | "Điều đáng nhớ nhất? Điều khó chịu nhất?" Nếu qua bên thứ ba: "Bên nào? Khâu nào hỏng? Bạn đã làm gì? Có tốn tiền để giải quyết không?" | Ghi nguyên văn cảm xúc |
| 6 Dịch vụ nên có | 4 | **Dịch vụ nên có** | "Lúc đó bạn ước có ai hoặc cái gì giúp?" — **không** hỏi "nếu app có X bạn dùng không" | Gom thành danh sách dịch vụ cho vòng 2 |
| 7 Bổ sung từ R7 | 5 | Câu hỏi "Kiểm ngay" | Ví dụ: "Ảnh chuyến đó giờ ở đâu? Bạn có đếm số tỉnh đã đi không?" · "Khi kể chuyến đó, bạn nói tên tỉnh nào?" · "Đã bao giờ đi cùng người mới quen qua mạng chưa?" | — |
| 8 Đóng | 3 | Mở rộng mẫu | "Ai giống bạn mà mình nên nói chuyện?" | — |

**Kỹ thuật hỏi sâu:** "kể thêm đi" · "lần gần nhất là khi nào?" · hỏi "vì sao" nhiều tầng · im lặng 5 giây để người kia nói tiếp.

**Ba quy tắc** `[M-24]`: hỏi **quá khứ cụ thể**, không hỏi tương lai giả định · không giới thiệu ý tưởng Levart trước khối cuối · ghi **nguyên văn**, không diễn giải.

**Kịch bản phỏng vấn đối tác** (`VH`, 15–20 phút): khách trẻ của anh/chị đến từ đâu · đang dùng kênh nào, tốn bao nhiêu · khó chịu nhất về khách hoặc về kênh · mùa nào vắng · "nếu có 30 bạn trẻ tới trong tháng, anh/chị sẵn lòng làm gì?" — mục đích là **nghe**, không bán.

### 10.4 Bảng hỏi khảo sát — làm sau phỏng vấn

**Quy trình 10 bước** `[M-18]`:

1. Xác định thông tin cần — từ R7 **và từ kết quả phỏng vấn sâu**
2. Chọn cách thu — biểu mẫu trực tuyến
3. Kiểm nội dung từng câu — câu này có thật sự cần không?
4. Xử lý "không biết, không muốn trả lời" — thêm lựa chọn phù hợp
5. Chọn dạng câu — đóng, mở, thang đo
6. Viết câu chữ
7. Sắp thứ tự — phễu từ chung tới riêng; câu nhạy cảm và nhân khẩu học **để cuối**
8. Bố cục, hình thức
9. Dựng biểu mẫu
10. **Thử nghiệm** rồi sửa

**Cấu trúc gợi ý** — ≤ 10 phút, ≤ 25 câu:

| Phần | Nội dung |
|---|---|
| A | Sàng lọc — có thuộc TA không |
| B | Hành vi du lịch 12 tháng qua — số chuyến, điểm đến, đi với ai |
| C | Nguồn thông tin, cách lên kế hoạch |
| D | Hình thức đi — tự túc, tour, nhóm |
| E | Nỗi đau — **danh sách lấy từ phỏng vấn**, cho xếp hạng hoặc thang Likert 5 mức `[M-23]` |
| F | Giải pháp đang dùng, mức hài lòng |
| G | Thiết bị (Android/iOS), mạng xã hội dùng nhiều nhất |
| H | Nhân khẩu học |

**Lỗi câu hỏi thường gặp:**

| Lỗi | Câu sai | Câu sửa |
|---|---|---|
| Dẫn dắt | "Bạn có thấy lên kế hoạch du lịch rất mất thời gian không?" | "Chuyến gần nhất bạn lên kế hoạch mất bao lâu?" |
| Hai ý một câu | "App du lịch có nhanh và dễ dùng không?" | Tách thành hai câu |
| Từ mơ hồ | "Bạn có thường xuyên đi du lịch?" | "12 tháng qua bạn đi mấy chuyến?" |
| Giả định tương lai | "Bạn có dùng app đóng dấu tỉnh không?" | Hỏi hành vi đã có; ý định để dành cho thử nghiệm ở Giai đoạn B |
| Thang đo lệch | Rất hài lòng / Hài lòng / Khá hài lòng | Thang cân đối hai phía |
| Thiếu lối ra | Chỉ có các lựa chọn cố định | Thêm "Khác" và "Không áp dụng" |

**Thử nghiệm bảng hỏi:** 5–10 người, kèm **phỏng vấn nhận thức** — hỏi "bạn hiểu câu này là đang hỏi gì?" để bắt câu bị hiểu sai trước khi phát diện rộng.

### 10.5 Đạo đức và dữ liệu cá nhân

Ghi âm và ghi chép phỏng vấn là **dữ liệu cá nhân** — Luật Bảo vệ dữ liệu cá nhân 2025 áp dụng (`Business.md [L-02]`):

- Xin đồng ý rõ ràng trước khi ghi âm (mẫu 5 dòng — Phụ lục H): mục đích, ai được xem, lưu bao lâu, quyền rút lại.
- Mã hoá tên người tham gia (P01, P02…) trong mọi tài liệu phân tích.
- Lưu trên **Google Drive chung của nhóm**, không để trên máy cá nhân.
- Đặt hạn xoá ghi âm sau khi đã chép xong (nhóm chốt số ngày).
- Khảo sát không bắt buộc số điện thoại; nếu thu để mời phỏng vấn tiếp thì để ở câu riêng và xin đồng ý riêng.

**Xong khi:** kế hoạch mẫu và bảng sàng lọc; kịch bản phỏng vấn sâu và kịch bản đối tác; **2 phỏng vấn thử** đã chạy và kịch bản đã sửa; mẫu đồng ý; danh sách ≥ 30 người tiềm năng.

---

## 11. R9 — Thực địa

**Tuần 6–9 · `TT` điều phối · `ALL` phỏng vấn · `VH` đối tác**

| Tuần | Việc |
|---|---|
| 6–7 | Phỏng vấn sâu — mỗi người 2 cuộc/tuần; `VH` phỏng vấn đối tác; 1 chuyến đi cùng nếu có dịp |
| 8 | Phỏng vấn sâu cuối; **phân tích nhanh** để chốt danh sách lựa chọn cho bảng hỏi; thử nghiệm bảng hỏi |
| 9 | Khảo sát mở 10–14 ngày |

**Mỗi cuộc phỏng vấn:**

1. **Trước:** sàng lọc · hẹn · gửi mẫu đồng ý.
2. **Trong:** hai người — một người hỏi, một người ghi.
3. **Trong 24 giờ sau:** ghi tệp theo mẫu (Phụ lục I) — các cột theo kịch bản + ≥ 3 câu trích nguyên văn + 1 dòng "học được gì" + 1 dòng "giả thuyết nào bị lay" · cập nhật sổ câu hỏi.

**Kiểm chất lượng hằng tuần:** `TT` đọc mọi tệp, trả lại tệp thiếu trích dẫn · theo dõi **bảng bão hoà** — mỗi cuộc ra thêm bao nhiêu chủ đề mới.

**Khảo sát:** phát qua nhóm lớp, CLB, nhóm Facebook · thêm một câu kiểm tra sự chú ý · theo dõi tỷ lệ hoàn thành · làm sạch dữ liệu: loại câu trả lời quá nhanh, trùng lặp, ngoài TA.

---

## 12. R10 — Phân tích, tổng hợp, báo cáo, Cổng A

**Tuần 9–10 · `TN` chủ trì báo cáo · `TT` phân tích định tính · `ĐP` phân tích định lượng**

### 12.1 Phân tích định tính

Phân tích chủ đề sáu bước `[M-21]`: làm quen dữ liệu → gắn mã → tìm chủ đề → rà soát chủ đề → định nghĩa và đặt tên → viết. Công cụ: **sơ đồ ái lực** `[M-30]` — mỗi ý một thẻ, gom cụm, đặt tên cụm bằng **lời của khách**.

Đầu ra:

- Top nỗi đau có số: bao nhiêu người **tự nêu** (không cần gợi) · tần suất · mức hài lòng với giải pháp hiện tại · bao nhiêu người **đã từng chi tiền** cho nó
- Persona v1
- Bản đồ hành trình trước – trong – sau chuyến đi
- Các câu việc-cần-làm của khách

### 12.2 Phân tích định lượng

Thống kê mô tả · bảng chéo theo phân khúc · kiểm định khác biệt giữa nhóm chỉ khi cỡ mẫu đủ · **mọi con số kèm sai số**.

### 12.3 Tam giác hoá

Một kết luận được coi là **mạnh** khi ít nhất 2 trong 3 nguồn — thứ cấp, phỏng vấn, khảo sát — cùng chỉ về một hướng. Nguồn mâu thuẫn nhau thì **ghi rõ**, không giấu.

### 12.4 Báo cáo nghiên cứu (≤ 15 trang)

Tóm tắt 1 trang · câu hỏi và phương pháp · vĩ mô · ngành và đối thủ · kiểm trùng · nội bộ · SWOT/TOWS · TA và persona · phát hiện từ thực địa · **trả lời QĐ1–QĐ7** · hạn chế của nghiên cứu · đề xuất cho Giai đoạn B.

### 12.5 Cổng A — đi tiếp hay không

Tiêu chí đề xuất (nhóm chốt ở R0):

| Tiêu chí | Ngưỡng |
|---|---|
| Bảy quyết định | Mỗi quyết định có câu trả lời mức bằng chứng ≥ B, **hoặc** ghi rõ "chưa đủ — kiểm bằng thử nghiệm ở Giai đoạn B" |
| Nỗi đau | ≥ 60% người phỏng vấn trong TA **tự nêu** cùng một nỗi đau |
| Tên | Kiểm trùng sạch, hoặc đã có tên thay thế |
| Danh sách chuyển tiếp | Có danh sách giả thuyết phải thử bằng hành vi ở Giai đoạn B |

| Kết quả | Lối ra |
|---|---|
| Đạt | **Sang Giai đoạn B** |
| Nỗi đau không tập trung | Quay lại **R6** — chọn TA khác, phỏng vấn bổ sung |
| Không có nỗi đau đủ mạnh ở mọi phân khúc | **Dừng** hoặc đổi hướng — ghi bài học |

Sau Cổng A: cập nhật `Business.md` lên 0.2 (Lean Canvas v1, mục 2 chuyển giả thuyết thành bằng chứng).

---

## 13. Cầu nối sang Giai đoạn B — Phát triển sản phẩm

Giai đoạn B là kim cương thứ hai, chạy theo **vòng lặp**:

```
Báo cáo nghiên cứu
  → Phát biểu vấn đề: "Làm sao để [TA] [làm được việc X] mà không [nỗi đau Y]?"
  → Cây cơ hội – giải pháp [M-26]: mỗi nỗi đau → vài hướng giải → mỗi hướng một thí nghiệm nhỏ
  → Vòng Xây → Đo → Học, chu kỳ 6 tuần (Business.md 8.2)
```

**Vòng đầu tiên của Giai đoạn B** gồm những câu hỏi mà phỏng vấn ở Giai đoạn A **không trả lời được**:

- **Phỏng vấn giải pháp và giá** — bảy khối: xác nhận nỗi đau · kiểm định vị trong 5 giây · tiền đã tiêu 12 tháng qua · ngân sách giả để ép đánh đổi · bốn câu hỏi độ nhạy giá Van Westendorp `[M-29]` · **thử cam kết thật** (để lại số điện thoại) · đóng.
- **Thử nghiệm hành vi** — trang đặt trước hộ chiếu in, đo bao nhiêu người thật sự đăng ký.

**Nghiên cứu không dừng ở Cổng A.** Trong Giai đoạn B vẫn giữ nhịp phỏng vấn đều đặn hằng tuần `[M-26]` — để mỗi quyết định sản phẩm luôn có tệp phỏng vấn đứng sau.

---

## 14. Lịch 10 tuần và phân ghế

| Tuần | `TN` | `ĐP` | `TT` | `VH` | `SP` |
|---|---|---|---|---|---|
| **1** | R0; R1 xu hướng, S, T | **R0 chủ trì**; R1 P, E kinh tế, L | R0 | R1 E môi trường | R0 |
| **2** | R2 5 lực; R3 đối thủ | R2 cỡ thị trường | R3 đọc đánh giá đối thủ | **R2 Mạng giá trị, đối tác** | R3 ma trận tính năng |
| **3** | R3 bản đồ nhóm chiến lược; R4 VRIO | **R3 kiểm trùng**; R4 tài chính, năng lực | R4 bảng kỹ năng | R4 quan hệ | **R4 công nghệ** |
| **4** | **R5 chủ trì** | **R7 chủ trì** | **R6 chủ trì** | R5–R7 | R5–R7 |
| | ▲ **Cổng giữa** — kết thúc lượt thứ cấp | | | | |
| **5** | Duyệt kịch bản | R8 đồng ý, dữ liệu cá nhân | **R8 chủ trì**; 2 phỏng vấn thử | R8 kịch bản đối tác | R8 |
| **6–7** | 2 phỏng vấn/tuần | 2 phỏng vấn/tuần | **R9 điều phối** + 2/tuần | Phỏng vấn đối tác | 2 phỏng vấn/tuần |
| **8** | Phỏng vấn cuối | Bảng hỏi | Phân tích nhanh; chốt bảng hỏi | Đối tác cuối | Dựng biểu mẫu |
| **9** | Bắt đầu báo cáo | Chạy khảo sát | **R10 định tính** | Tổng hợp đối tác | — |
| **10** | **Báo cáo + Cổng A** | R10 định lượng | Persona v1 | — | — |

**Cổng giữa (cuối tuần 4):** R1–R7 đã xong. Nếu kiểm trùng phát hiện tên Levart trùng nặng thì **xử lý trước khi tuyển người phỏng vấn** — vì sẽ phải giới thiệu tên với họ.

**Quan hệ với `week_index.md`:** lịch hiện tại trong `week_index.md` (và bảng Excel) cho phỏng vấn bắt đầu từ T3. Kế hoạch này chèn khoảng 4 tuần nghiên cứu tại bàn **trước** phỏng vấn. Nhiều đầu mục cũ đã khớp sẵn với các bước ở đây:

| Đầu mục trong `week_index.md` | Bước tương ứng |
|---|---|
| T02.2 So sánh 6 app đối thủ | R3 |
| T02.3 Số liệu du lịch | R1, R2.4 |
| T02.4 Chọn tỉnh mục tiêu | R1 (vòng đời điểm đến, bản đồ mới), R6 |
| T02.5 Danh sách đối tác | R2.3 |
| T04 Tra nhãn hiệu, tên miền | R3.4 |
| T05 Buổi hỏi luật | R1 chữ L, R3.4 |
| T03–T06 Phỏng vấn | R8–R9 |

Nếu nhóm theo kế hoạch này thì `week_index.md` và bảng Excel cần xếp lại thứ tự — **tệp này chưa sửa hai tệp đó**.

---

## 15. Rủi ro của chính kế hoạch nghiên cứu

| Rủi ro | Dấu hiệu | Cách chặn |
|---|---|---|
| **Tê liệt vì phân tích** | Lượt thứ cấp kéo quá 4 tuần; "tìm thêm chút nữa" | Đóng khung thời gian; tiêu chí "đủ để quyết định" viết ở R0 |
| **Thiên kiến xác nhận** | Chỉ tìm số liệu ủng hộ ý tưởng | Mỗi bước có ô "bằng chứng ngược"; luân phiên một người đóng vai phản biện |
| **PESTLE, SWOT thành giấy dán tường** | Không có cột "vì vậy"; ô SWOT không có mã nguồn | Cột hệ quả bắt buộc; ô không có mã không được vào |
| **Mẫu thuận tiện** | Toàn phỏng vấn bạn thân | ≤ 1/3 người quen trực tiếp; bảng sàng lọc |
| **Hỏi khách điều tra cứu được** | Phí lượt phỏng vấn cho câu có sẵn số | Cây quyết định R7 |
| **Tin lời nói về tương lai** | "Mình sẽ dùng", "mình sẽ trả" | Chỉ hỏi hành vi đã xảy ra; ý định thì kiểm bằng thử nghiệm ở Giai đoạn B |
| **Số liệu mức C thành nền quyết định** | Trích số báo chí cho quyết định lớn | Mức bằng chứng bắt buộc cho mọi con số |
| **Rò rỉ dữ liệu cá nhân** | Ghi âm nằm trên điện thoại cá nhân | Drive chung, mã hoá tên, xoá theo hạn |

---

## 16. Nguồn

### Phương pháp `[M-xx]`

| Mã | Nguồn | Dùng cho |
|---|---|---|
| M-01 | UK Design Council (2005) — mô hình Kim cương đôi (Double Diamond) | Khung tổng (0.1) |
| M-02 | Aguilar, F. J. (1967). *Scanning the Business Environment*. Macmillan | Gốc của khung quét môi trường → PEST/PESTLE |
| M-03 | Johnson, G., Whittington, R., Scholes, K. et al. *Exploring Strategy* (nhiều phiên bản) | PESTEL, phân tích môi trường |
| M-04 | Porter, M. E. (1979). "How Competitive Forces Shape Strategy", *HBR*; (1980) *Competitive Strategy*; (2008) "The Five Competitive Forces That Shape Strategy", *HBR* 86(1) | 5 lực; nhóm chiến lược; sản phẩm bổ trợ là "yếu tố", không phải lực |
| M-05 | Porter, M. E. (1985). *Competitive Advantage*. Free Press | Chuỗi giá trị |
| M-06 | Brandenburger, A. & Nalebuff, B. (1996). *Co-opetition*. Doubleday | Mạng giá trị (Value Net), bên bổ trợ, hợp–cạnh |
| M-07 | Stabell, C. B. & Fjeldstad, Ø. D. (1998). "Configuring value for competitive advantage: on chains, shops, and networks", *Strategic Management Journal* 19(5): 413–437 | Chuỗi → mạng giá trị cho doanh nghiệp trung gian |
| M-08 | Wernerfelt, B. (1984). "A Resource-Based View of the Firm", *Strategic Management Journal* 5(2): 171–180 | Nền tảng của cách nhìn theo nguồn lực |
| M-09 | Barney, J. (1991). "Firm Resources and Sustained Competitive Advantage", *Journal of Management* 17(1): 99–120; Barney (1995) "Looking Inside for Competitive Advantage", *Academy of Management Executive* 9(4) | VRIO |
| M-10 | Grant, R. M. (1991). "The Resource-Based Theory of Competitive Advantage", *California Management Review* 33(3): 114–135 | Phân loại nguồn lực hữu hình, vô hình, con người |
| M-11 | Weihrich, H. (1982). "The TOWS Matrix — A Tool for Situational Analysis", *Long Range Planning* 15(2): 54–66 | TOWS |
| M-12 | Kotler, P. & Keller, K. L. *Marketing Management* (`Business.md [S-54]`) | STP |
| M-13 | Aulet, B. (2013). *Disciplined Entrepreneurship* (`Business.md [S-11]`) | Đầu cầu, cỡ thị trường từ dưới lên |
| M-14 | Butler, R. W. (1980). "The concept of a tourist area cycle of evolution", *Canadian Geographer* 24(1): 5–12 | Vòng đời điểm đến |
| M-15 | Pine, B. J. & Gilmore, J. H. (1998). "Welcome to the Experience Economy", *HBR* 76(4) | Xu hướng kinh tế trải nghiệm |
| M-16 | Rochet, J.-C. & Tirole, J. (2003). "Platform Competition in Two-Sided Markets", *Journal of the European Economic Association* 1(4): 990–1029 | Nền tảng hai phía |
| M-17 | Parker, G., Van Alstyne, M. & Choudary, S. P. (2016). *Platform Revolution* (`Business.md [S-44]`) | Hiệu ứng mạng, gà và trứng |
| M-18 | Malhotra, N. K. *Marketing Research: An Applied Orientation* (Pearson, nhiều phiên bản) | Quy trình nghiên cứu; thứ cấp trước sơ cấp; 10 bước thiết kế bảng hỏi |
| M-19 | Creswell, J. W. & Plano Clark, V. L. *Designing and Conducting Mixed Methods Research* (SAGE) | Thiết kế hỗn hợp tuần tự khám phá |
| M-20 | Guest, G., Bunce, A. & Johnson, L. (2006). "How Many Interviews Are Enough?", *Field Methods* 18(1): 59–82 | Điểm bão hoà ~12 phỏng vấn |
| M-21 | Braun, V. & Clarke, V. (2006). "Using thematic analysis in psychology", *Qualitative Research in Psychology* 3(2): 77–101 | Phân tích chủ đề 6 bước |
| M-22 | Cochran, W. G. (1977). *Sampling Techniques* (3rd ed.). Wiley | Công thức cỡ mẫu |
| M-23 | Likert, R. (1932). "A Technique for the Measurement of Attitudes", *Archives of Psychology* 140 | Thang Likert |
| M-24 | Fitzpatrick, R. (2013). *The Mom Test* (`Business.md [S-14]`) | Quy tắc hỏi không bị nói dối |
| M-25 | Bland, D. J. & Osterwalder, A. (2019). *Testing Business Ideas*. Wiley | Bản đồ giả định: quan trọng × bằng chứng |
| M-26 | Torres, T. (2021). *Continuous Discovery Habits* (`Business.md [S-64]`) | Cây cơ hội – giải pháp; phỏng vấn đều đặn |
| M-27 | Kim, W. C. & Mauborgne, R. (2005). *Blue Ocean Strategy* (`Business.md [S-37]`) | Strategy canvas |
| M-28 | Dunford, A. (2019). *Obviously Awesome* (`Business.md [S-16]`) | Trình tự định vị |
| M-29 | Van Westendorp, P. (1976). "NSS Price Sensitivity Meter", ESOMAR Congress | Bốn câu hỏi độ nhạy giá |
| M-30 | Kawakita, J. — phương pháp KJ (sơ đồ ái lực) | Gom cụm dữ liệu định tính |
| M-31 | Moesta, B. — phỏng vấn chuyển đổi theo việc-cần-làm (`Business.md [S-47]`) | Phỏng vấn người vừa đổi cách làm |
| M-32 | Krueger, R. A. & Casey, M. A. *Focus Groups: A Practical Guide for Applied Research* (SAGE) | Thảo luận nhóm — khi nào dùng, khi nào không |

### Văn bản và dữ liệu mới `[N-xx]` — tra ngày 13/10/2026

| Mã | Nguồn | Dùng cho |
|---|---|---|
| N-01 | Nghị quyết 202/2025/QH15 về sắp xếp đơn vị hành chính cấp tỉnh (12/06/2025) — [toàn văn](https://xaydungchinhsach.chinhphu.vn/toan-van-nghi-quyet-so-202-2025-qh15-ve-sap-xep-don-vi-hanh-chinh-cap-tinh-119250612174148722.htm) · [Thư viện Pháp luật](https://thuvienphapluat.vn/van-ban/Bo-may-hanh-chinh/Nghi-quyet-202-2025-QH15-sap-xep-don-vi-hanh-chinh-cap-tinh-648951.aspx) | R1 chữ P — bản đồ 34 tỉnh |
| N-02 | Quyết định 147/QĐ-TTg phê duyệt Chiến lược phát triển du lịch Việt Nam đến năm 2030 (22/01/2020) — [Cổng văn bản Chính phủ](https://vanban.chinhphu.vn/default.aspx?pageid=27160&docid=198927) | R1 chữ P, E |
| N-03 | Luật Sở hữu trí tuệ, Điều 59 — chương trình máy tính không được bảo hộ với danh nghĩa sáng chế — [Thư viện Pháp luật](https://thuvienphapluat.vn/phap-luat/chuong-trinh-may-tinh-la-gi-chuong-trinh-may-tinh-co-duoc-bao-ho-voi-danh-nghia-sang-che-khong-587311-130298.html); đối chiếu bản sửa đổi 2022 | R3.4 |
| N-04 | Cục Sở hữu trí tuệ — hướng dẫn tra cứu nhãn hiệu trên WIPO Publish — [ipvietnam.gov.vn](https://ipvietnam.gov.vn/en/tap-huan-tisc_ip-hub/-/asset_publisher/khKPTduzK8mq/content/huong-dan-su-dung-thu-vien-so-ve-so-huu-cong-nghiep-tren-nen-tang-wipo-publish-e-tra-cuu-nhan-hieu-nop-on-tai-viet-nam) | R3.4 |
| N-05 | WIPO Global Brand Database — [branddb.wipo.int](https://branddb.wipo.int) | R3.4 |

Nguồn dữ liệu **nên tra** ở R1–R2, chưa tra trong lần soạn này: Cục Thống kê (dân số theo tuổi và tỉnh, chi tiêu hộ gia đình) · Cục Du lịch Quốc gia (số liệu tháng/năm) · báo cáo kinh tế số Đông Nam Á thường niên (du lịch trực tuyến) · Google Trends · các công ty khảo sát người tiêu dùng Việt Nam có công bố phương pháp.

---

## Phụ lục — Mẫu biểu

### A. Bản tóm tắt nghiên cứu (1 trang)

```
LEVART — BẢN TÓM TẮT NGHIÊN CỨU                                   Ngày: ____
Bối cảnh (3 dòng): ____
Quyết định cần ra:  QĐ1 ____  QĐ2 ____  …  QĐ7 ____
Trong phạm vi:      ____
Ngoài phạm vi:      ____
Thời hạn:           Lượt 1 xong ____ · Lượt 2 xong ____ · Cổng A ____
Ngân sách:          ____ giờ/tuần · ____ đồng
Tiêu chí dừng:      ____
Người ký:           ____
```

### B. Sổ câu hỏi nghiên cứu

`Mã · Câu hỏi · Phục vụ QĐ · Phát sinh ở bước · Phương pháp · Nguồn / người trả lời · Mức bằng chứng · Trả lời · Trạng thái`

### C. Bảng PESTLE

`Chữ · Yếu tố · Cơ hội/Đe doạ · Tác động 1–3 · Khả năng 1–3 · Khi nào · Nguồn + mức · Vì vậy Levart nên…`

### D. Hồ sơ đối thủ

`Tên · Loại hình (1–8) · Khách mục tiêu · Việc họ giải · Tính năng lõi · Cách kiếm tiền, giá · Lượt tải, điểm, số đánh giá · Mạnh · Yếu (từ đánh giá 1–2 sao) · Phản ứng nếu Levart lớn · Ảnh chụp`

### E. Kiểm kê nguồn lực + VRIO

`Nhóm (hữu hình / vô hình / con người) · Nguồn lực · Hiện trạng · Bằng chứng · V · R · I · O · Hàm ý cạnh tranh`

### F. Chấm điểm TA

`Phân khúc · Quy mô · Tăng trưởng · Khả năng chi trả · Mức đau · Mức cạnh tranh · Hấp dẫn · Tiếp cận · Khớp nguồn lực · Phục vụ được · Nguồn từng ô`

### G. Bảng sàng lọc (5 câu)

1. Bạn bao nhiêu tuổi? *(loại nếu ngoài khoảng TA)*
2. Bạn đang học hay đi làm? Ở đâu?
3. 12 tháng qua bạn đi du lịch trong nước mấy chuyến? *(cần ≥ 1)*
4. Chuyến gần nhất bạn đi tự túc, đi tour hay đi theo nhóm?
5. Bạn có làm trong ngành du lịch hoặc phát triển app du lịch không? *(loại nếu có)*

### H. Mẫu đồng ý phỏng vấn (5 dòng)

```
Mình là ____, thuộc nhóm dự án Levart. Buổi trò chuyện ~30 phút để tìm hiểu cách người trẻ đi du lịch.
Mình xin ghi âm để ghi chép cho đúng. Chỉ thành viên nhóm được nghe, tên bạn sẽ được mã hoá.
Ghi âm sẽ được xoá sau ____ ngày, khi đã chép xong.
Bạn có thể dừng bất kỳ lúc nào và yêu cầu xoá dữ liệu của mình.
Bạn đồng ý chứ?   ☐ Đồng ý   ☐ Không ghi âm, chỉ ghi chép   ☐ Không đồng ý
```

### I. Mẫu ghi chép phỏng vấn

```
Mã: P__ · Ngày: ____ · Người hỏi: ____ · Người ghi: ____ · Hình thức: gặp / gọi video
Hồ sơ:                ____
Tần suất:             ____ chuyến / 12 tháng · chuyến gần nhất: ____
Nguồn thông tin:      ____ (ghi tên từng công cụ)
Hình thức đi + vì sao: ____
Trải nghiệm tốt:      ____
Chưa hài lòng:        ____ (bên thứ ba nào, khâu nào, đã làm gì, có tốn tiền không)
Dịch vụ ước có:       ____
Câu hỏi R7:           ____
3 câu trích nguyên văn: "____" · "____" · "____"
Học được gì (1 dòng): ____
Giả thuyết nào bị lay (1 dòng): ____
```

---

*Phiên bản kế tiếp (0.2) viết sau Cổng giữa — khi R1–R7 đã có số thật và các bảng (GT) đã được kiểm hoặc gạch.*
