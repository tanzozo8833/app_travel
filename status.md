# LEVART — Báo cáo hiện trạng

Cập nhật: **07/09/2026** · Commit khảo sát: `a49953b` · Room version 9 · Firestore project `travel-app-844e3`

> Bản khảo sát gốc lập ngày 30/08/2026 trên commit `0437bd5`. Bản này cập nhật sau khi
> **nhóm A đã hoàn tất** và bổ sung **mục 7 — câu hỏi nghiên cứu về tối ưu, bảo mật, quy mô lớn**.

---

## Cách lập báo cáo này

Mọi con số đều đo trực tiếp, không lấy từ trí nhớ phiên làm việc:

| Nguồn | Cách lấy |
|---|---|
| Lược đồ Room | Đọc `data/local/entity/*.kt` và `LuminaDatabase.kt` |
| Lược đồ Firestore | Truy vấn máy chủ thật bằng `firebase-admin`, đọc tên trường của document có thật |
| Số document | `collection.count().get()` trên máy chủ |
| Số chuỗi chưa dịch | Đếm bằng biểu thức chính quy trên `ui/` |
| Luồng dữ liệu | Lần theo `LuminaRepository` → service → màn hình |
| Hành vi khi chạy | Cài lên máy ảo `Pixel_10`, đọc logcat, chụp màn hình |

Chỗ nào chưa kiểm chứng được, báo cáo ghi rõ **"chưa kiểm chứng"** thay vì suy đoán.

Sơ đồ cơ sở dữ liệu nằm ở hai tệp riêng:
- `database_current.mmd` — hiện trạng
- `database_target.mmd` — đích đến khi hoàn thiện

---

## 1. Tổng quan bằng số

| Chỉ số | Giá trị | Bằng chứng |
|---|---|---|
| Bảng trong Room | **4** | `LuminaDatabase.kt` — chỉ còn danh mục và cài đặt |
| Collection trên Firestore | **3 gốc + 7 con** | Truy vấn máy chủ |
| Tài khoản trên máy chủ | **11** (6 mẫu + 5 thật) | `users.count()` |
| Bài viết | **5** | `posts.count()` |
| Bình luận | **9** | Đếm qua từng bài |
| Quan hệ theo dõi | **18** | `user_follows.count()` |
| Màn hình giao diện | **15** | `ui/screens/*.kt` |
| Thành phần dùng lại | **14** | `ui/components/*.kt` |
| Luồng dữ liệu cấp cho UI | **39 StateFlow** | `LuminaViewModel.kt` |
| Chuỗi đã tách ra tài nguyên | **537** (Việt + Anh, khớp 100% khoá) | `res/values/strings.xml`, `res/values-en/strings.xml` |
| Chuỗi tiếng Việt còn trong `ui/` | **259**, trong đó chỉ **10** nằm ở vị trí hiển thị | Đếm regex; 10 chuỗi đó là tên riêng (Hoàng Sa, Trường Sa, "Tiếng Việt") và hậu tố tiền tệ `đ` |
| Kích thước APK gỡ lỗi | **33,3 MB** | `ls -l app-debug.apk` |

---

## 2. Bảng tổng hợp chức năng

Chú thích cột **Dữ liệu**: `Máy chủ` = Firestore · `Máy` = Room · `Cứng` = viết sẵn trong code · `Không có` = chưa tồn tại.

| # | Chức năng | Giao diện | Dữ liệu | Nối với nhau | Mức hoàn thiện |
|---|---|---|---|---|---|
| 1 | Đăng nhập Google | ✅ Đầy đủ | Máy chủ | ✅ | **Xong** |
| 2 | Đăng nhập qua trình duyệt | ✅ | Máy chủ | ✅ | **Xong** — chưa thử 2FA thật |
| 3 | Đăng nhập email + mật khẩu | ✅ | Máy chủ | ✅ | **Xong** — thiếu quên mật khẩu |
| 4 | Chế độ khách (ẩn danh) | ✅ | Máy chủ | ✅ | **Xong** — đã chạy thử, UID thật |
| 5 | Nâng cấp khách → Google | ✅ | Máy chủ | ✅ | **Xong** — chưa thử va chạm tài khoản |
| 6 | Xoá tài khoản | ✅ | Máy chủ | ✅ | **Xong** — chưa thử thật |
| 7 | Hồ sơ cá nhân | ✅ | Máy chủ | ⚠️ Một phần | **90%** — vài số liệu lấy từ Room |
| 8 | Trang cá nhân người khác | ✅ | Máy chủ | ✅ | **Xong** |
| 9 | Theo dõi / bỏ theo dõi | ✅ | Máy chủ | ✅ | **Xong** — 18 quan hệ mẫu |
| 10 | Bảng tin cộng đồng | ✅ | Máy chủ | ✅ | **Xong** — có phân trang, 4 trạng thái |
| 11 | Đăng bài | ✅ | Máy chủ | ❌ Ảnh | **60%** — ảnh không tải lên được |
| 12 | Bình luận | ✅ | Máy chủ | ✅ | **Xong** — chưa có trả lời lồng nhau |
| 13 | Thả tim / lưu bài | ✅ | Máy chủ | ✅ | **Xong** — cộng dồn nguyên tử |
| 14 | Bản đồ Google | ✅ | Máy | ✅ | **Xong** |
| 15 | GPS thời gian thực | ✅ | — | ✅ | **Xong** — 7 trạng thái |
| 16 | Camera + bộ lọc màu | ✅ | Máy | ⚠️ | **80%** — ảnh chỉ nằm trong máy |
| 17 | Con dấu 34 tỉnh | ✅ | **Máy + Máy chủ** | ❌ Lệch nguồn | **50%** — xem mục 4.2 |
| 18 | Chuyến đi | ✅ | Máy chủ | ✅ | **Xong** |
| 19 | Lập lịch trình bằng AI | ✅ | Máy chủ | ✅ | **Xong** — thời tiết do AI bịa |
| 20 | Chỉnh sửa lịch trình thủ công | ✅ | Máy chủ | ✅ | **Xong** |
| 21 | Tìm kiếm | ✅ | Máy | ⚠️ | **40%** — chỉ tìm địa điểm trong máy |
| 22 | Thông báo | ✅ | Máy chủ | ⚠️ | **50%** — 0 bản ghi, không có đẩy |
| 23 | Bộ sưu tập đã lưu | ✅ | Máy chủ | ⚠️ | **40%** — 0 bản ghi |
| 24 | Đa ngôn ngữ | ✅ | Máy | ❌ **Hỏng** | **20%** — xem mục 4.1 |
| 25 | Cài đặt (tối, xoá đệm) | ✅ | Máy | ✅ | **Xong** |
| 26 | Đồng bộ Google Calendar | ❌ | Không có | ❌ | **0%** |
| 27 | Thời tiết thật | ⚠️ Có ô hiển thị | **Cứng/AI bịa** | ❌ | **10%** |
| 28 | Báo cáo nội dung xấu | ❌ | Không có | ❌ | **0%** — Play Store yêu cầu |
| 29 | Thông báo đẩy | ❌ | Không có | ❌ | **0%** |
| 30 | Báo cáo sự cố | ❌ | Không có | ❌ | **0%** |

---

## 3. Chi tiết từng màn hình

### 3.1 Đã hoàn chỉnh

| Màn hình | Dòng | Nguồn dữ liệu | Ghi chú |
|---|---|---|---|
| `LoginScreen` | 802 | Firebase Auth | **Đã dịch đủ** — 32 `stringResource`, 0 chuỗi cứng |
| `HomeScreen` | 1016 | Firestore `posts` | Phân trang 10 bài, đủ 4 trạng thái tải/rỗng/lỗi/thành công |
| `PostDetailScreen` | 528 | Firestore | Bình luận lồng trong bài |
| `UserProfileDetailScreen` | 577 | Firestore | Có đường lùi khi thiếu hồ sơ |
| `MyTripsScreen` | 282 | Firestore `trips` | |
| `TripDetailScreen` | 420 | Firestore | |
| `InteractiveMapScreen` | 1248 | Room `provinces`, `places` | Google Maps thật |

### 3.2 Còn thiếu

| Màn hình | Dòng | Vấn đề | Bằng chứng |
|---|---|---|---|
| `CreatePostScreen` | 973 | Ảnh không tải lên máy chủ | `LuminaRepository.kt:357` — `createPost` nhận `mediaUrls` rồi ghi thẳng, không có bước tải lên |
| `CheckInScreen` | 2527 | Ảnh lưu cục bộ; tiến độ ghi vào Room | `ImageColorFilterProcessor.kt:92` — lưu vào `context.filesDir` |
| `ProfileScreen` | 836 | Số tỉnh lấy từ Room | `ProfileScreen.kt:91` — `provinces.count { it.visitedPlacesCount > 0 }` |
| ~~`SettingsScreen`~~ | 900 | ~~Đổi ngôn ngữ không có tác dụng~~ — **đã sửa** (A1) | Chạy lại: đổi VI ⇄ EN, toàn bộ giao diện đổi theo |
| `SearchScreen` | 205 | Chỉ tìm địa điểm trong Room | `SearchScreen.kt:44` — `places.filter {...}` |
| `NotificationsScreen` | 176 | Máy chủ có 0 bản ghi | Truy vấn `users/*/notifications` |
| ~~`AiPlanScreen`~~ | 1482 | ~~143 chuỗi cứng~~ — **đã tách** (A2, lượt 9) | 58 chỗ thay bằng `stringResource`; phần còn lại là dữ liệu mẫu và câu prompt gửi Gemini, giữ tiếng Việt có chủ đích |

---

## 4. Năm vấn đề có bằng chứng

### 4.1 Đổi ngôn ngữ không hoạt động — **chặn chức năng 05** · ĐÃ SỬA 06/09/2026

**Bằng chứng chạy thật:** chọn Tiếng Việt trong app, ô chọn đổi sang cờ VN, nhưng trang cá nhân vẫn hiển thị `TRAVEL PROFILE`, `Following`, `Followers`, `Provinces`. Lệnh `adb shell cmd locale get-app-locales` trả về `[]` — hệ thống chưa ghi nhận ngôn ngữ nào.

**Nguyên nhân:**
```kotlin
MainActivity.kt:46   class MainActivity : ComponentActivity()
themes.xml:4         parent="android:Theme.DeviceDefault.NoActionBar"
```
`AppCompatDelegate.setApplicationLocales()` chỉ có tác dụng khi Activity kế thừa `AppCompatActivity`, và lớp đó lại bắt buộc theme kế thừa từ AppCompat. Phải sửa **cả hai cùng lúc**.

**Ảnh hưởng:** 746 chuỗi còn lại không đáng dịch cho tới khi công tắc này hoạt động.

**Đã sửa** — commit `a805b80`. Đổi đồng thời hai chỗ:
```kotlin
MainActivity.kt   class MainActivity : AppCompatActivity()
themes.xml        parent="Theme.AppCompat.DayNight.NoActionBar"
```
Kiểm chứng lại trên máy ảo: chọn US → toàn bộ giao diện sang tiếng Anh; chọn VI → quay lại tiếng Việt.

### 4.2 Tiến độ con dấu nằm hai nơi — **lệch dữ liệu giữa các máy**

**Bằng chứng:** một lần check-in ghi vào cả hai nơi:
```
LuminaRepository.kt:472   firestorePersonalService.saveCheckIn(userId, record)   → Firestore
LuminaRepository.kt:475   database.placeDao().markAsVisitedByName(...)           → Room
LuminaRepository.kt:478   database.provinceDao().updateProgress(...)             → Room
```
Còn giao diện đọc tiến độ từ Room:
```
ProfileScreen.kt:91   provinces.count { it.visitedPlacesCount > 0 }
```

**Kiểm chứng trên máy chủ:** `users/*/check_ins` có **0 bản ghi**, trong khi trang cá nhân hiển thị **10/34 tỉnh, 29 con dấu**. Nghĩa là toàn bộ con số đang hiển thị đến từ Room, và **sẽ không theo bạn sang máy khác**.

**Cách sửa:** bỏ `visited_places_count` và `is_completed` khỏi `ROOM_provinces`, bỏ `is_visited` khỏi `ROOM_places`, thay bằng collection `users/{uid}/province_progress` trên máy chủ.

### 4.3 Ảnh không bao giờ rời khỏi máy — **lỗ hổng lớn nhất cho tính cộng đồng**

**Bằng chứng:** không có Firebase Storage trong dự án (`grep firebase-storage` không ra kết quả nào). Ảnh check-in lưu ở `context.filesDir/checkin_photos` (`ImageColorFilterProcessor.kt:92`). `createPost` ghi thẳng `mediaUrls` lên Firestore mà không tải tệp lên.

**Hệ quả:**
- Người khác mở bài của bạn thấy **ảnh vỡ**, vì đường dẫn chỉ tồn tại trên máy bạn
- Gỡ app là **mất sạch ảnh**
- 5 bài mẫu hiện dùng ảnh Unsplash nên trông vẫn bình thường — che mất vấn đề

**Cách sửa:** thêm Firebase Storage, tải ảnh lên trước rồi mới lưu đường dẫn công khai vào `mediaUrls`.

### 4.4 Thời tiết do AI bịa

**Bằng chứng:** không có lệnh gọi dịch vụ thời tiết nào trong toàn bộ mã nguồn. `WeatherSummary` chỉ xuất hiện ở bộ chuyển đổi Room và trong dữ liệu chuyến đi do Gemini sinh ra (`users/*/trips` có trường `weatherSummary`, `isAiGenerated: true`).

**Hệ quả:** ô thời tiết trong lịch trình hiển thị con số nghe hợp lý nhưng không liên quan gì tới thời tiết thật.

### 4.5 Số liệu thống kê do máy khách tự ghi

**Bằng chứng:** `firestore.rules` cho phép mỗi người ghi mọi trường vào hồ sơ của mình:
```
allow write: if isOwner(userId);
```
`visitedProvincesCount`, `totalSavesCount`, `tripsCompletedCount` đều nằm trong tài liệu đó.

**Hệ quả:** một người am hiểu có thể sửa thẳng số liệu của mình. Chưa nguy hiểm khi chưa có bảng xếp hạng, nhưng phải chuyển sang máy chủ tự tính trước khi có.

---

## 5. Việc còn phải làm

Xếp theo thứ tự phụ thuộc — làm sai thứ tự sẽ phải làm lại.

### Nhóm A — Sửa lỗi đang chặn · **ĐÃ XONG 07/09/2026**

| # | Việc | Thay đổi CSDL | Kết quả |
|---|---|---|---|
| A1 | Sửa công tắc đổi ngôn ngữ | Không | **Xong** — `a805b80`. `AppCompatActivity` + theme AppCompat, sửa cùng lúc |
| A2 | Dịch nốt 746 chuỗi, 12 màn hình | Không | **Xong** — 9 lượt + 1 lượt quét sạch. `strings.xml` 144 → **537** mục, VI/EN khớp 100% khoá. 6 chuỗi đếm chuyển sang `<plurals>` để tiếng Anh không còn "1 places" |

Ba việc phát sinh trong lúc làm nhóm A, đã xử lý luôn:

| Việc | Vì sao | Commit |
|---|---|---|
| Thanh điều hướng bỏ chữ, chỉ còn icon | Chủ dự án yêu cầu | `LuminaBottomBar.kt` — nhãn chuyển sang `contentDescription` để trình đọc màn hình vẫn đọc được |
| Băng báo lỗi GPS nói rõ lý do | Trước đó chỉ hiện một câu chung chung | `5b551c3` — thêm `enum GpsIssue`, mỗi lý do có nút hành động riêng |
| Thẻ chuyến đi hiện điểm đến kế tiếp thật | Trước đó hiện chuỗi cố định | `TripCard.kt` — tính bằng `Calendar` chứ không dùng `java.time` (minSdk 24, dự án chưa bật desugaring) |

### Nhóm B — Đưa dữ liệu về một nguồn

| # | Việc | Thay đổi CSDL |
|---|---|---|
| B1 | Thêm Firebase Storage, tải ảnh lên trước khi lưu | `mediaUrls` và `photoUrl` bắt buộc là đường dẫn Storage; thêm `mediaThumbUrls`; thêm bảng `ROOM_pending_uploads` cho hàng đợi offline |
| B2 | Chuyển tiến độ tỉnh lên máy chủ | Bỏ `visited_places_count`, `is_completed` khỏi `ROOM_provinces`; bỏ `is_visited` khỏi `ROOM_places`; thêm `users/{uid}/province_progress` |
| B3 | Máy chủ tự tính số liệu thống kê | Thêm Cloud Functions; siết luật để máy khách không ghi được các trường đếm |
| B4 | Thêm khoá thật cho check-in | Thêm `placeId` vào `FS_user_check_ins` thay vì so tên |

### Nhóm C — Hoàn thiện chức năng

| # | Việc | Thay đổi CSDL |
|---|---|---|
| C1 | Đồng bộ Google Calendar | Thêm `calendarEventIds`, `calendarSyncedAt` vào `FS_user_trips` |
| C2 | Thời tiết thật | Thêm dịch vụ thời tiết; `weatherSummary` kèm mốc thời gian |
| C3 | Thông báo đẩy | Thêm `fcmToken` vào `FS_users`; thêm `actorId`, `targetType`, `targetId`, `createdAt` vào thông báo |
| C4 | Tìm kiếm mở rộng | Thêm `FS_place_catalog`; tìm cả người dùng và bài viết |
| C5 | Bộ sưu tập hoạt động | Thêm `boardId` vào `FS_post_saves`; thêm `itemCount` |
| C6 | Bài Gần bạn theo toạ độ | Thêm `lat`, `lng` vào `FS_posts` |
| C7 | Trả lời bình luận | Thêm `parentCommentId` |

### Nhóm D — Điều kiện phát hành

| # | Việc | Ghi chú |
|---|---|---|
| D1 | Báo cáo nội dung xấu, chặn người dùng | Thêm `FS_reports`, `isBanned`, `isDeleted`, `visibility` — **Google Play bắt buộc với app có nội dung người dùng** |
| D2 | Chốt `applicationId` | Hiện là `com.aistudio.luminavietnam.trav`, chuỗi sinh tự động. Sau khi lên store **không đổi được** |
| D3 | Tạo keystore phát hành | Bản release hiện **không build được** |
| D4 | Bật R8 | Đo được: giảm 60% class, 61% method |
| D5 | Chính sách riêng tư + khai báo Data safety | Bắt buộc vì dùng camera, vị trí, ảnh |
| D6 | Báo cáo sự cố (Crashlytics) | Hiện app văng trên máy người dùng thì không ai biết |
| D7 | Đường dẫn web yêu cầu xoá tài khoản | Bắt buộc với app có đăng nhập |
| D8 | **Bật App Check bằng Play Integrity** | `firebase-appcheck-recaptcha` đang có trong `build.gradle.kts` nhưng `grep -rn "AppCheck" app/src/main/java` **rỗng** — chưa hề khởi tạo. Ngoài ra `recaptcha` là provider của web, Android phải dùng `firebase-appcheck-playintegrity`. Xem mục 7.2 |

### Đã chấp nhận rủi ro

| Việc | Trạng thái |
|---|---|
| Khoá Gemini nằm trong `classes.dex` | **Chủ dự án chấp nhận** — bỏ qua theo quyết định ngày 29/08/2026 |

---

## 6. Chưa kiểm chứng

Những mục sau đã đọc code thấy đúng nhưng **chưa chạy thử thực tế**, không nên coi là đã xong:

| Mục | Vì sao chưa kiểm chứng được |
|---|---|
| Camera chụp ảnh thật | Máy ảo không có ống kính thật |
| Chống hết bộ nhớ với ảnh 50MP | Ảnh máy ảo quá nhỏ |
| GPS ở trạng thái tín hiệu kém, hết thời gian chờ | Máy ảo luôn trả toạ độ tức thì |
| Đăng nhập trình duyệt với xác thực 2 lớp | Cần tài khoản thật bật 2FA |
| Xoá tài khoản | Thao tác không hoàn tác được |
| Nâng cấp khách khi tài khoản Google đã tồn tại | Cần dựng sẵn tình huống va chạm |

Tất cả đều cần **điện thoại Android thật cắm qua USB**. Máy ảo đã hỏng ba lần trong quá trình làm việc và không tái hiện được các tình huống trên.

---

## 7. Câu hỏi nghiên cứu — tối ưu, bảo mật, quy mô lớn

> **Trạng thái: CHƯA TRIỂN KHAI.** Mục này ghi lại ba câu hỏi chủ dự án đặt ra ngày
> **07/09/2026** và kết quả tra cứu tài liệu chính thức. Đây là *tài liệu tham khảo để
> quyết định*, không phải việc đang làm. Nhóm 7.3 (quy mô lớn) đặc biệt là chuyện tương lai —
> hiện máy chủ mới có 11 tài khoản và 5 bài viết, chưa có gì để phải chống tải.

**Ba câu hỏi:**

1. Có những kỹ thuật tối ưu nào để app giữ nguyên chức năng nhưng chạy nhẹ và mượt hơn?
2. Có những kỹ thuật bảo mật nào cần áp dụng?
3. Khi lượng người dùng lớn thì cần chuẩn bị gì?

Mỗi phương pháp bên dưới đều ghi **tên gọi chính thức**, **cách dùng**, **nguồn**, và
**có áp dụng cho Levart không** — kết luận cuối dựa trên số đo thật của dự án, không suy đoán.

### 7.0 Số đo hiện tại làm mốc so sánh

Đo ngày 07/09/2026 trên commit `a49953b`:

| Chỉ số | Giá trị | Cách đo |
|---|---|---|
| Kích thước APK gỡ lỗi | **33,3 MB** | `ls -l app-debug.apk` |
| Số tệp `.dex` trong APK | **14** | `unzip -l app-debug.apk \| grep dex` |
| Tổng dung lượng `.dex` | **~91 MB** (chưa nén) | Cộng cột kích thước |
| `isMinifyEnabled` bản release | **false** | `app/build.gradle.kts:46` |
| Số icon Material thực sự dùng | **103** | `grep -o "Icons\.<biến thể>\.<tên>" \| sort -u` |
| Thư viện icon đang nhúng | `material-icons-extended` (toàn bộ) | `app/build.gradle.kts` |
| Số danh sách cuộn (`Lazy*`) | **53** | `grep -c` |
| Lệnh `items(...)` có khai báo `key` | **5 / 28** | `grep` |
| Compose BOM | `2024.09.00` | `gradle/libs.versions.toml:13` |
| Coil | `2.7.0` | `gradle/libs.versions.toml:22` |
| Firebase BOM | `34.15.0` | `gradle/libs.versions.toml:40` |
| Room | `2.7.0`, schema version **9** | `LuminaDatabase.kt:21` |

---

### 7.1 Kỹ thuật tối ưu hiệu năng và dung lượng

| # | Tên phương pháp | Giải quyết vấn đề gì | Cách dùng | Nguồn |
|---|---|---|---|---|
| P1 | **R8 full mode** (code shrinking / tree shaking + resource shrinking) | Xoá code và tài nguyên không ai gọi tới; gộp lớp, nội tuyến hàm | Đặt `isMinifyEnabled = true` và `isShrinkResources = true` trong khối `release`, dùng `proguard-android-optimize.txt`. R8 mặc định chạy full mode từ AGP 8.0 | [Enable app optimization with R8](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization) · [Use R8 to shrink, optimize, and fast-track your app](https://developer.android.com/blog/posts/use-r8-to-shrink-optimize-and-fast-track-your-app) |
| P2 | **Optimized resource shrinking** | Bản resource shrinking thường không đụng tới `.dex`; bản "optimized" lần được tham chiếu xuyên biên giới dex ↔ tài nguyên nên xoá được cả hai | Bật cùng R8; Google công bố cùng bài blog 09/2025 | [Improve app performance with optimized resource shrinking](https://android-developers.googleblog.com/2025/09/improve-app-performance-with-optimized-resource-shrinking.html) |
| P3 | **R8 Configuration Analyzer** | Tìm các luật `-keep` thừa đang chặn R8 tối ưu | Công cụ có sẵn trong AGP, chạy để soi `proguard-rules.pro` | [Use R8 Configuration Analyzer](https://developer.android.com/topic/performance/app-optimization/r8-configuration-analyzer) |
| P4 | **Baseline Profiles** (Profile Guided Optimization — PGO) | Lần chạy đầu ART phải thông dịch/JIT; profile cho phép biên dịch AOT sẵn các đường code quan trọng | Thêm module `:baselineprofile`, dùng plugin `androidx.baselineprofile`, viết `MacrobenchmarkRule` mô phỏng luồng khởi động + cuộn danh sách, tệp sinh ra đóng vào APK/AAB | [Baseline Profiles overview](https://developer.android.com/topic/performance/baselineprofiles/overview) · [Create Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile) |
| P5 | **Startup Profiles** | Báo cho `d8` biết lớp/hàm nào cần nhét vào `classes.dex` đầu tiên, app nạp ít tệp hơn khi khởi động | Sinh cùng lúc với Baseline Profile, khai báo trong khối `baselineProfile { }` | [Baseline Profiles overview](https://developer.android.com/topic/performance/baselineprofiles/overview) |
| P6 | **Macrobenchmark** | Đo có bằng chứng thay vì cảm tính; chạy lặp, xoá cache đĩa giữa các lần, xuất trace | Module `androidTest` riêng, `MacrobenchmarkRule` với `StartupTimingMetric`, `FrameTimingMetric` | [Benchmark Baseline Profiles with Macrobenchmark](https://developer.android.com/topic/performance/baselineprofiles/measure-baselineprofile) |
| P7 | **Strong skipping mode** (Compose) | Composable có tham số "không ổn định" bị vẽ lại thừa; chế độ này cho phép bỏ qua tất cả composable restartable | Bật mặc định từ Compose compiler 2.0.20 — dự án dùng Kotlin 2.2.10 nên **đã có sẵn**. Việc cần làm là kiểm chứng bằng báo cáo compiler | [Strong skipping mode](https://developer.android.com/develop/ui/compose/performance/stability/strongskipping) |
| P8 | **Compose compiler metrics / stability report** | Chỉ ra chính xác composable nào không skippable và tham số nào unstable | Bật `composeCompiler { reportsDestination = ... ; metricsDestination = ... }`, đọc tệp `*-composables.txt` | [Diagnose stability issues](https://developer.android.com/develop/ui/compose/performance/stability/diagnose) · [Fix stability issues](https://developer.android.com/develop/ui/compose/performance/stability/fix) |
| P9 | **`key` cho danh sách cuộn** | Không có `key`, Compose so khớp phần tử theo vị trí; thêm/xoá giữa danh sách làm vẽ lại toàn bộ | `items(list, key = { it.id })` cho mọi `LazyColumn` / `LazyRow` / `LazyVerticalGrid` | [Jetpack Compose Performance](https://developer.android.com/develop/ui/compose/performance) |
| P10 | **Ba pha của Compose** (composition → layout → drawing) | Đọc state ở pha sớm gây vẽ lại cả cây; đọc trễ (lambda) chỉ chạy lại pha sau | Ví dụ `Modifier.offset { }` thay `Modifier.offset()`, `graphicsLayer { }` cho animation | [Compose phases and performance](https://developer.android.com/develop/ui/compose/performance/phases) |
| P11 | **Android App Bundle (AAB)** | Play sinh APK riêng cho từng cấu hình máy, người dùng chỉ tải phần cần dùng | Nộp `.aab` thay `.apk`; `./gradlew :app:bundleRelease` | [Reduce your app size](https://developer.android.com/topic/performance/reduce-apk-size) |

**Kết luận cho Levart** (dựa trên số đo mục 7.0, không suy đoán):

- **P1 + P2 là đòn bẩy lớn nhất và rẻ nhất.** Bản release hiện `isMinifyEnabled = false`
  (`app/build.gradle.kts:46`). Phép đo trước đó trong dự án cho thấy bật R8 giảm **60% số class,
  61% số method**. Việc này đã nằm sẵn ở **D4** trong mục 5.
- **Bỏ `material-icons-extended`** là món dễ thứ hai: app dùng **103 icon** nhưng đang nhúng
  trọn bộ thư viện. Cách làm: copy 103 icon cần dùng thành `ImageVector` trong dự án, hoặc dùng
  file SVG/vector drawable, rồi bỏ dependency.
- **P9 áp dụng ngay được**: chỉ **5/28** lệnh `items(...)` có `key`.
- **P4/P5/P6 nên làm sau P1**, vì Baseline Profile sinh trên bản chưa rút gọn sẽ phải sinh lại.
- **P7 đã có sẵn** nhờ Kotlin 2.2.10 — không cần làm gì, nhưng **P8 nên chạy để xác nhận**.
- Hai dependency cần rà lại vì có thể không dùng: `firebase-ai` và
  `firebase-appcheck-recaptcha` (xem 7.2 — thư viện này có trong `build.gradle.kts` nhưng
  `grep -rn "AppCheck" app/src/main/java` trả về **rỗng**, nghĩa là **chưa hề được khởi tạo**).

---

### 7.2 Kỹ thuật bảo mật

| # | Tên phương pháp | Giải quyết vấn đề gì | Cách dùng | Nguồn |
|---|---|---|---|---|
| S1 | **Firebase App Check + Play Integrity API** | Chặn client giả mạo gọi vào Firestore / Gemini; chống gian lận hoá đơn, giả mạo app, đầu độc dữ liệu | Thêm `firebase-appcheck-playintegrity`, gọi `FirebaseAppCheck.getInstance().installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())` trong `Application.onCreate`, rồi bật enforcement trong Firebase Console | [Get started using App Check with Play Integrity on Android](https://firebase.google.com/docs/app-check/android/play-integrity-provider) · [Overview of the Play Integrity API](https://developer.android.com/google/play/integrity/overview) |
| S2 | **App Check cho Gemini / Firebase AI Logic** | Khoá Gemini nằm trong `classes.dex` (rủi ro đã ghi ở mục 5) — App Check khiến khoá bị trích xuất cũng không gọi được từ app khác | Trang tài liệu riêng của Firebase cho đúng tình huống này | [Prevent Gemini API abuse with Firebase App Check](https://firebase.google.com/docs/ai-logic/app-check) |
| S3 | **Firestore Security Rules** — mô hình từ chối mặc định | Luật sai là lỗ hổng phổ biến nhất của app Firebase | Đã áp dụng: `isOwner(userId)`, `diff(resource.data).affectedKeys().hasOnly([...])`. Cần bổ sung khi làm B3 để client không ghi được trường đếm | [Best practices for Cloud Firestore](https://firebase.google.com/docs/firestore/best-practices) |
| S4 | **OWASP MASVS v2.0 + MASTG + MAS Checklist** | Bộ tiêu chuẩn ngành để rà soát có hệ thống thay vì rà theo cảm tính. Bản 2.0 bỏ ba mức L1/L2/R, chuyển thành "security testing profiles" | Dùng MAS Checklist đối chiếu từng nhóm (STORAGE, CRYPTO, AUTH, NETWORK, PLATFORM, CODE, RESILIENCE, PRIVACY) | [OWASP MASVS](https://mas.owasp.org/MASVS/) · [OWASP MASTG](https://mas.owasp.org/MASTG/) |
| S5 | **Network Security Configuration** | Khai báo chính sách TLS theo tên miền bằng tệp XML, không cần sửa code | `res/xml/network_security_config.xml`, trỏ từ `android:networkSecurityConfig` trong manifest | [Network security configuration](https://developer.android.com/privacy-and-security/security-config) |
| S6 | **Certificate pinning** — *ghi lại kèm cảnh báo* | Ghim chứng chỉ để chống tấn công trung gian | **Google khuyến cáo KHÔNG dùng cho app Android**: khi máy chủ đổi CA, app đã ghim sẽ không kết nối được cho tới khi người dùng cập nhật. Nếu vẫn làm, phải có nhiều pin dự phòng và hạn dùng ngắn | [Security with network protocols](https://developer.android.com/privacy-and-security/security-ssl) |
| S7 | **EncryptedSharedPreferences / EncryptedFile** (Jetpack Security) | Mã hoá cục bộ token, khoá, dữ liệu nhạy cảm | Levart hiện lưu `lumina_auth_prefs` bằng `SharedPreferences` thường. Lưu ý: androidx.security-crypto đã ngừng phát triển; hướng đi hiện tại là **Preferences DataStore** (có `SharedPreferencesMigration` để chuyển) | [Security (Jetpack)](https://developer.android.com/jetpack/androidx/releases/security) · [Working with Preferences DataStore](https://developer.android.com/codelabs/android-preferences-datastore) |
| S8 | **Android Photo Picker + Selected Photos Access** | Từ Android 14, app xin `READ_MEDIA_IMAGES` sẽ bị người dùng chỉ cho chọn vài ảnh. Photo Picker **không cần xin quyền lưu trữ nào** | Dùng `ActivityResultContracts.PickVisualMedia`; nếu tự làm gallery thì phải xử lý `READ_MEDIA_VISUAL_USER_SELECTED` | [Photo picker](https://developer.android.com/training/data-storage/shared/photo-picker) · [Grant partial access to photos and videos](https://developer.android.com/about/versions/14/changes/partial-photo-video-access) |
| S9 | **Security checklist chính thức của Android** | Danh sách rà soát nền: lưu trữ, IPC, quyền, WebView, mã hoá | Đối chiếu trước khi phát hành | [Security checklist](https://developer.android.com/privacy-and-security/security-tips) · [Improve your app's security](https://developer.android.com/privacy-and-security/security-best-practices) |
| S10 | **Play Data safety declaration** | Bắt buộc khi nộp app; khai sai bị gỡ | Firebase có trang liệt kê sẵn dữ liệu từng SDK thu thập | [Prepare for Google Play's data disclosure requirements](https://firebase.google.com/docs/android/play-data-disclosure) |

**Phát hiện có bằng chứng khi tra cứu mục này:**

> `app/build.gradle.kts` có `implementation(libs.firebase.appcheck.recaptcha)`, nhưng
> `grep -rn "AppCheck\|appCheck" app/src/main/java --include=*.kt` trả về **rỗng**.
> Nghĩa là App Check **chưa từng được khởi tạo** — thư viện chỉ làm nặng APK chứ không bảo vệ gì.
> Ngoài ra `recaptcha` là provider dành cho **web**; Android phải dùng
> `firebase-appcheck-playintegrity`. Đây là việc cần sửa, đã thêm vào mục 5 nhóm D.

---

### 7.3 Khi lượng người dùng lớn — chuẩn bị cho tương lai

> Chưa cần làm bây giờ. Ghi lại để khi cần thì biết đọc ở đâu.

| # | Tên phương pháp | Giải quyết vấn đề gì | Cách dùng | Nguồn |
|---|---|---|---|---|
| L1 | **Quy tắc 500/50/5** | Firestore chia lại vùng dữ liệu dần dần; dồn tải đột ngột gây lỗi tranh chấp | Bắt đầu **500 thao tác/giây** trên một collection, sau đó tăng **tối đa 50% mỗi 5 phút** | [Best practices for Cloud Firestore](https://firebase.google.com/docs/firestore/best-practices) |
| L2 | **Tránh hotspotting** | Ghi/đọc dồn vào các document có ID gần nhau về thứ tự từ điển → tranh chấp, chậm | Không dùng ID tăng dần (`Customer1`, `Customer2`…); nếu index một trường tăng đều (ví dụ timestamp) mà không truy vấn theo nó thì **miễn index** trường đó | [Best practices for Cloud Firestore](https://firebase.google.com/docs/firestore/best-practices) |
| L3 | **Key Visualizer** | Công cụ chẩn đoán, vẽ bản đồ nhiệt để tìm chỗ nghẽn | Có sẵn trong Cloud Console của Firestore | [Best practices for Cloud Firestore](https://firebase.google.com/docs/firestore/best-practices) |
| L4 | **Distributed counters (sharded counter)** | Một document chỉ chịu được khoảng **1 lượt ghi bền vững/giây**. Đếm like, lượt theo dõi, lượt xem sẽ nghẽn | Tách bộ đếm thành subcollection `shards`; giá trị = tổng các shard. Thông lượng ghi tăng tuyến tính theo số shard | [Distributed counters](https://firebase.google.com/docs/firestore/solutions/counters) |
| L5 | **Firebase Extension: Distributed Counter** | Bản dựng sẵn của L4, có worker nền gộp shard về document tổng | Cài extension, client chỉ ghi vào shard riêng. Extension công bố mức tối đa **10.000 cập nhật/giây** | [Distributed Counter — Firebase Extensions Hub](https://firebase.google.com/products/extensions/firestore-counter) |
| L6 | **Roll-up document** | L4 làm đọc đắt lên (phải đọc mọi shard) | Giữ thêm một document tổng, cập nhật theo nhịp chậm; client đọc document đó | [Distributed counters](https://firebase.google.com/docs/firestore/solutions/counters) |
| L7 | **Hiểu chi phí đọc/ghi ở quy mô lớn** | Tính tiền và độ trễ của Firestore phụ thuộc thiết kế index và truy vấn | Đọc trước khi thiết kế lại lược đồ cho B1–B4 | [Understand reads and writes at scale](https://firebase.google.com/docs/firestore/understand-reads-writes-scale) |
| L8 | **Real-time queries at scale** | `snapshotListener` mở quá nhiều hoặc phạm vi quá rộng sẽ tốn kém khi đông người | Tài liệu riêng về giới hạn và cách thiết kế listener | [Understand Real-time queries at scale](https://firebase.google.com/docs/firestore/real-time_queries_at_scale) |
| L9 | **Android vitals** (chỉ số chất lượng bắt buộc của Play) | Vượt ngưỡng "bad behavior", Play **giảm hiển thị** app và có thể cảnh báo trên trang store | Ngưỡng ANR: **≥ 0,47%** người dùng hoạt động hằng ngày gặp ANR (toàn bộ máy), hoặc **≥ 8%** trên một dòng máy. Wake lock coi là quá mức nếu giữ ≥ 2 giờ trong 24 giờ, ngưỡng 5% | [Android vitals](https://developer.android.com/topic/performance/vitals) · [Play Console technical quality requirements](https://support.google.com/googleplay/android-developer/answer/17492799) |
| L10 | **Play Integrity API ở quy mô lớn** | Khi đông người dùng thì mới có động cơ để bị lạm dụng | Google công bố app dùng Play Integrity có mức sử dụng trái phép **thấp hơn trung bình 80%** | [Stronger threat detection, simpler integration](https://developer.android.com/blog/posts/stronger-threat-detection-simpler-integration-protect-your-growth-with-the-play-integrity-api) |

**Chỗ Levart sẽ đụng trần trước tiên** (suy ra từ code hiện tại, chưa đo tải thật):

| Nơi | Vì sao | Đối chiếu |
|---|---|---|
| `FieldValue.increment()` trên `posts/{id}.likeCount` | Một document, một lượt ghi bền vững/giây. Một bài viết viral là nghẽn ngay | L4 / L5 |
| `users/{uid}.followerCount` | Người dùng nổi tiếng bị nhiều người theo dõi cùng lúc | L4 |
| Bài viết sắp xếp theo `createdAt` giảm dần | Trường tăng đều, đúng mô tả hotspotting của L2 | L2 |
| Không phân trang bảng tin | Hiện tải toàn bộ; 5 bài thì không sao, 50.000 bài thì hỏng | L7 |

---

### 7.4 Cách kiểm chứng — không có số đo thì không kết luận

| Muốn biết | Dùng công cụ | Nguồn |
|---|---|---|
| Khởi động nhanh chậm ra sao, cuộn có giật không | Macrobenchmark (`StartupTimingMetric`, `FrameTimingMetric`) | [Benchmark Baseline Profiles with Macrobenchmark](https://developer.android.com/topic/performance/baselineprofiles/measure-baselineprofile) |
| Composable nào bị vẽ lại thừa | Compose compiler metrics | [Diagnose stability issues](https://developer.android.com/develop/ui/compose/performance/stability/diagnose) |
| Luật `-keep` nào đang cản R8 | R8 Configuration Analyzer | [Use R8 Configuration Analyzer](https://developer.android.com/topic/performance/app-optimization/r8-configuration-analyzer) |
| Người dùng thật gặp crash/ANR gì | Android vitals + Crashlytics | [Debug crashes quickly using Android vitals and Firebase Crashlytics](https://developer.android.com/distribute/best-practices/launch/debug-crashes) |
| Firestore nghẽn ở đâu | Key Visualizer | [Best practices for Cloud Firestore](https://firebase.google.com/docs/firestore/best-practices) |
| Rà soát bảo mật có hệ thống | OWASP MAS Checklist | [OWASP MASVS](https://mas.owasp.org/MASVS/) |

### 7.5 Thứ tự đề xuất nếu quyết định làm

Xếp theo **lợi ích / công sức**, và theo ràng buộc phụ thuộc (làm sai thứ tự phải làm lại):

1. **P1 + P2** — bật R8 và optimized resource shrinking (đã là D4).
2. **Bỏ `material-icons-extended`**, giữ 103 icon đang dùng.
3. **P9** — thêm `key` cho 23 lệnh `items(...)` còn thiếu.
4. **S1 + S2** — App Check bằng Play Integrity; đồng thời gỡ `firebase-appcheck-recaptcha` chết.
5. **P8** — chạy Compose compiler metrics để biết còn chỗ nào đáng sửa.
6. **P4 + P5 + P6** — Baseline Profile, Startup Profile, đo bằng Macrobenchmark. Làm **sau** bước 1.
7. **P11** — nộp AAB thay APK (đi kèm D2, D3).
8. Nhóm 7.3 — **chỉ khi có người dùng thật**, và chỉ sau khi B1–B4 xong.

> Bước 1–3 không đổi lược đồ dữ liệu, không đổi chức năng, làm được ngay.
> Bước 4 đổi cấu hình máy chủ, cần bật enforcement trong Firebase Console.
> Bước 8 đổi lược đồ, phải làm sau nhóm B.

---

## 8. B1 — đã triển khai xong phần máy khách, chờ bật bucket trên máy chủ

**Chốt ngày 07/09/2026, triển khai ngày 08/09/2026** — commit `7f6e31d`.
Toàn bộ chín bước phía máy khách đã xong và chạy thử trên máy ảo. Còn đúng một việc
nằm ngoài tầm code: **bật Cloud Storage cho dự án trên Firebase Console.**

### Phương án đã duyệt: B1 bản đầy đủ

Nén ảnh + sinh thumbnail + hàng đợi tải lên khi mất mạng.

```
CreatePostScreen
   └─ chọn ảnh (Photo Picker)
        └─ nén về ≤1600px, sinh thumb 400px
             └─ ghi hàng đợi Room: pending_uploads
                  └─ WorkManager (retry, backoff)
                       └─ Storage: posts/{uid}/{postId}/{n}.jpg
                            └─ Firestore: mediaUrls + mediaThumbUrls

Mất mạng  → bài nằm hàng đợi, có mạng tự đăng
Đổi máy   → ảnh hiện bình thường
```

**Vì sao chọn bản đầy đủ thay vì bản gọn:** hàng đợi offline đòi thêm bảng Room và đổi
luồng đăng bài. Làm bản gọn trước rồi thêm sau nghĩa là viết lại luồng đăng bài hai lần.

### Việc phải làm, theo thứ tự

| Bước | Nội dung | Ghi chú |
|---|---|---|
| 1 | Thêm `firebase-storage` vào `libs.versions.toml` + `build.gradle.kts` | Hiện `grep -n "storage"` trong hai tệp này trả về **rỗng** |
| 2 | Viết luật bảo mật cho Storage | Chỉ chủ sở hữu được ghi vào `posts/{uid}/**`; giới hạn kiểu tệp và dung lượng |
| 3 | Nén ảnh trước khi tải | `BitmapFactory` với `inJustDecodeBounds` + `inSampleSize`. Bắt cả `OutOfMemoryError` — lớp này kế thừa `Error`, **không phải** `Exception` |
| 4 | Thêm bảng Room `pending_uploads` | Room lên version **10**. Nhớ: **không đổi tên** `lumina_vietnam_travel.db` |
| 5 | Thêm WorkManager, worker tải lên có retry + backoff | Dependency mới |
| 6 | Sửa `LuminaRepository.kt:357` `createPost` | Hiện nhận `mediaUrls` rồi ghi thẳng, không có bước tải lên |
| 7 | Sửa ảnh check-in | `ImageColorFilterProcessor.kt:92` đang lưu vào `context.filesDir`, cũng phải đẩy lên Storage |
| 8 | Thêm `mediaThumbUrls` vào `FS_posts`; bảng tin đọc thumb, màn chi tiết đọc ảnh gốc | Đổi lược đồ, cập nhật `database_target.mmd` |
| 9 | Chuyển sang Android Photo Picker | Xem S8 mục 7.2 — không cần xin quyền lưu trữ nào |

### Câu hỏi còn treo — phải trả lời trước bước 1

> **Dự án `travel-app-844e3` đã ở gói Blaze chưa?**

Từ 09/2024, tạo bucket Cloud Storage mặc định **bắt buộc dự án phải ở gói Blaze**
(trả theo mức dùng). Dự án ở gói Spark sẽ nhận lỗi **402 hoặc 403** khi gọi tới bucket.
Bucket vẫn giữ hạn mức miễn phí, nhưng phải gắn thẻ thanh toán vào dự án.

Nguồn: [Default bucket and billing requirements for Cloud Storage for Firebase after September 2024](https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024)

Chỗ kiểm tra: Firebase Console → dự án `travel-app-844e3` → góc dưới bên trái, dòng ghi gói hiện tại.

**Nếu chưa ở Blaze thì bước 1–2 không chạy được**, nhưng bước 3, 4, 5 (nén ảnh, bảng hàng đợi,
worker) vẫn viết trước được vì không đụng tới máy chủ.

---

### 8.1 Kết quả chạy thử ngày 08/09/2026

Chạy hết luồng thật trên máy ảo `Pixel_10`: đẩy một ảnh vào thư viện, mở màn soạn bài,
chọn ảnh bằng Photo Picker, nhập nội dung, bấm Đăng.

**Nén ảnh — đo trên tệp thật trong `filesDir/upload_staging`:**

| Tệp | Kích thước | So với gốc |
|---|---|---|
| Ảnh gốc (PNG 1080×2424) | 1.406.446 B | — |
| `*_full.jpg` (cạnh dài ≤1600) | 183.327 B | **−87,0%** |
| `*_thumb.jpg` (cạnh dài ≤400) | 19.368 B | **−98,6%** |

Bảng tin đọc bản thumbnail, nghĩa là mỗi thẻ tải **19 KB** thay vì **1,4 MB**.

**Hàng đợi — chạy đúng như thiết kế:**

Firebase trả về **404 Not Found** khi tải lên. Worker không văng, trả về `RETRY`,
bài nằm lại trong hàng đợi, và bảng tin hiện **"1 bài đang chờ tải lên"**.
Đây chính là hành vi mong muốn khi máy chủ chưa sẵn sàng — bài không biến mất không dấu vết.

**Câu hỏi treo ở mục 8 đã có lời giải bằng bằng chứng chạy thật:**

> Mã **404** (chứ không phải 402/403) nghĩa là dự án `travel-app-844e3`
> **chưa hề tạo bucket Cloud Storage nào.**

Việc cần làm, chỉ trên Firebase Console, không cần sửa code:

1. Mở Firebase Console → dự án `travel-app-844e3` → **Storage** → **Get started**.
2. Nếu bị chặn thì nâng dự án lên gói **Blaze** (bắt buộc từ 09/2024). Bucket vẫn giữ hạn mức miễn phí.
3. Vào tab **Rules**, dán nguyên nội dung tệp `storage.rules` ở gốc dự án.
4. Mở lại app. Bài đang kẹt trong hàng đợi sẽ tự tải lên, không phải soạn lại.

### 8.2 Bốn lỗi phát hiện khi chạy thử và đã sửa

| Lỗi | Nguyên nhân |
|---|---|
| Mọi ảnh đều bị coi là hỏng | Với `inJustDecodeBounds = true` thì `decodeStream()` **luôn** trả về `null`. Đặt `?: return null` sau khối `use{}` là bắt nhầm kết quả giải mã thay vì luồng đọc |
| Room ném `cannot verify the data integrity` | Thêm cột `remote_urls` mà giữ nguyên version 10. `fallbackToDestructiveMigration()` **không** cứu được khi số version không đổi — Room so hash lược đồ. Đã tăng lên 11 |
| Ảnh mẫu biến mất khi trộn với ảnh máy | Bộ lọc chỉ giữ `content://` và `file://`, bỏ luôn `http(s)://`. Nay giữ cả hai, ảnh mẫu đứng trước |
| Băng "đang chờ tải lên" đè lên tiêu đề hộ chiếu | Đặt chung `Box` với `ProvinceStoryTray`; `Box` xếp chồng các phần tử. Đã tách thành item riêng của lưới |

### 8.3 Việc B1 còn nợ lại

| Việc | Vì sao chưa làm |
|---|---|
| Xoá ảnh trên Storage khi người dùng xoá bài | `StorageService.deletePostMedia` đã viết nhưng **chưa nối vào `deletePost`** |
| Màn hình xem và huỷ bài đang kẹt | `UploadQueue.cancel` và `observePending` đã có, nhưng giao diện mới chỉ có băng đếm số |
| Kiểm chứng với ảnh 50 MP thật | Máy ảo không có ảnh đủ lớn — vẫn nằm ở mục 6 "chưa kiểm chứng" |
| `attachedTrip` không được lưu qua hàng đợi | Không phải hồi quy: `FirestorePostService.createPost` vốn **chưa bao giờ** ghi trường này vào Firestore |
