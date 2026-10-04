# So sánh Always On AMOLED và Custom AOD

So sánh app đối thủ Always On AMOLED (`com.tomer.alwayson`, bản 3.2.0.1) với Custom AOD ở trạng thái hiện tại.

Thông tin về Always On AMOLED lấy từ 3 nguồn. Mỗi dòng ghi rõ nguồn:

- **[máy]** Đo trực tiếp trên Redmi Note 13 Pro 5G (HyperOS 1.0, Android 14) bằng `dumpsys`, `appops` và `logcat`, với bản 3.2.0.1 cài từ Play.
- **[Play]** Trang Google Play và danh sách quyền của bản 3.2.0.1 trên APKMirror.
- **[2017]** Mã nguồn mở bản 0.9.x năm 2017 (GPLv3). Bản hiện tại có thể đã khác.

Số đo phía Custom AOD là của demo FakeAOD trên cùng máy (ghi **[FakeAOD]**). Custom AOD giữ cùng cơ chế nhưng chưa chạy lại trên máy, xem README › Trạng thái kiểm thử.

"?" nghĩa là chưa kiểm chứng được.

## 1. Tổng quan

| | Always On AMOLED | Custom AOD |
|---|---|---|
| Nhà phát triển | Firehawk. Tác giả gốc Tomer Rosenfeld đã bán app vào 01/2024 [Play] | Bạn |
| Phiên bản | 3.2.0.1, cập nhật 16/06/2025 [Play] | `v1.0.0`, chưa phát hành |
| `minSdk` / `targetSdk` | 23 / 34 (Android 6 / Android 14) [máy] | 30 / 37 (Android 11 / Android 17), `compileSdk` 37 |
| Lượt tải, đánh giá | Hơn 10 triệu, 4,5★ [Play] | – |
| Kiếm tiền | Quảng cáo và gói Premium [Play] | Quảng cáo AdMob của base: quảng cáo xen kẽ ở Splash, đang là id test. Không có Premium |
| Ngôn ngữ giao diện | 23 [Play] | English và tiếng Việt, chọn trong app (lần mở đầu tiên và ở màn cài đặt) |
| Kích thước APK | 17,6 MB, gói cho nhiều kiến trúc CPU [Play] | 81,3 MB bản debug, chưa bật R8. Bản release (R8 + shrink) chưa build vì chưa có keystore |

## 2. Kỹ thuật

### 2.1 Cơ chế lõi

Phần lõi gần như giống nhau. Khác biệt chính nằm ở nút nguồn, ở cách xử lý cuộc gọi, và ở số quyền.

| Hạng mục | Always On AMOLED | Custom AOD |
|---|---|---|
| Ngôn ngữ, giao diện | ? (bản 2017: Java và XML View) [2017] | Kotlin, Jetpack Compose (Material 3). Clean Architecture + MVI, Koin, Navigation 3 |
| Thành phần hiện AOD | `AlwaysOnActivity` [máy] | `AodActivity` (cửa sổ) với `AodScreen` / `AodViewModel` (MVI) |
| Cách che màn hình khóa | Activity thường (`BASE_APPLICATION`) với cờ cửa sổ `SHOW_WHEN_LOCKED`, `TURN_SCREEN_ON`, `KEEP_SCREEN_ON`, `FULLSCREEN`. Keyguard ở trạng thái occluded [máy] | Cùng cơ chế: cờ `SHOW_WHEN_LOCKED`, `TURN_SCREEN_ON`, `KEEP_SCREEN_ON`, thêm `showWhenLocked` trong manifest. Ẩn thanh hệ thống bằng `WindowInsetsController` thay vì `FULLSCREEN` |
| Chế độ mở | `singleInstance`; cờ `NEW_TASK`, `CLEAR_TASK`, `NO_HISTORY`, `EXCLUDE_FROM_RECENTS`, `NO_ANIMATION`, `NO_USER_ACTION` [máy] | Tương đương: `singleInstance`, `noHistory`, `excludeFromRecents`, `taskAffinity=""`; cờ `NEW_TASK`, `CLEAR_TASK`, `NO_ANIMATION`, `NO_USER_ACTION` |
| Mở Activity từ nền | Nhờ "Hiển thị trên ứng dụng khác" (log `BAL_ALLOW_SAW_PERMISSION`) [máy] | Giống |
| Giữ app chạy nền | `StarterService`: foreground service loại `specialUse`, kênh thông báo "Keep Alive" mức LOW [máy] | `AodService`: foreground service loại `specialUse`, kênh "Clock in the background" / "Đồng hồ chạy nền" mức LOW |
| Chạy lại sau khởi động | `BootReceiver` nhận `BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`, `MY_PACKAGE_REPLACED` [máy] | `BootReceiver` nhận `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`. Trên Xiaomi bị chặn nếu không bật "Tự khởi chạy" |
| Bật màn hình | Cờ cửa sổ `FLAG_TURN_SCREEN_ON`, log `SCREEN_ON_FLAG` [máy] | Cùng cờ cửa sổ. Trên HyperOS log ghi `TURN_ON:handleTurnScreenOn`, nhưng màn hình khóa đã bị che trước đó [FakeAOD] |
| Nhận diện khuôn mặt khi AOD hiện | Không chạy [máy] | Không chạy ở 5/6 lần mở được ghi lại [FakeAOD] |
| Thời gian từ lúc màn hình tắt đến khi AOD sáng | Khoảng 1,1 giây [máy] | Khoảng 0,6–1 giây [FakeAOD] |
| Bấm nguồn khi AOD đang hiện | Đóng AOD cũ, mở AOD mới, màn hình sáng lại với AOD sau khoảng 1 giây [máy] | Đóng AOD, bật lại màn hình khóa bằng wake lock `ACQUIRE_CAUSES_WAKEUP` (khác có chủ ý) |
| Bấm nguồn tắt rồi bật lại ngay | AOD vẫn chen lên màn hình khóa [máy] | Không mở AOD nếu màn hình đã sáng lại |
| Kết thúc AOD (hết giờ, trong túi) | Bản 2017: `DevicePolicyManager.lockNow()` hoặc root [2017]. Bản hiện tại không có Device Admin [máy]; cách làm chưa rõ | Đồng hồ chuyển sang đen, rồi để thời gian chờ của máy tắt màn hình |
| Cảm biến tiệm cận | Bản 2017: `PROXIMITY_SCREEN_OFF_WAKE_LOCK` [2017] | `ProximityManager` + `AodViewModel`: bị che 3 giây thì đen, lấy ra thì sáng lại |
| Cuộc gọi | `CallReceiver` (`PHONE_STATE`, `NEW_OUTGOING_CALL`) cộng quyền `READ_PHONE_STATE` [máy] | `noHistory` cộng `AudioStateManager` (theo dõi `AudioManager`), không cần quyền |
| Thông báo trên AOD | `NotificationListenerService` [máy] | `AodNotificationListener` (`NotificationListenerService`) + `NotificationStateManager`: icon theo app, viền sáng, điều khiển nhạc qua phiên nhạc của thông báo nhạc |
| Độ sáng | Không còn khai báo `WRITE_SETTINGS`, dù mô tả Play vẫn nhắc tới [Play] | `screenBrightness` của cửa sổ: 1–100%, hoặc theo hệ thống |
| Quy tắc khi nào hiện | ? | `AodRulesUiModel.allows()` (nguồn điện, khung giờ, ngưỡng pin), kiểm lúc màn hình tắt và trong lúc AOD hiện |
| Kiểm tra sau khi mở AOD | ? | `checkLaunch` sau 2 giây: ghi kết quả, dọn AOD bị giấu sau màn hình khóa |
| Màn hình khởi động | `PHSplashActivity` của SDK PremiumHelper [máy] | Splash của base: consent UMP rồi quảng cáo xen kẽ. AOD có splash màu đen trên Android 12+ |
| Tích hợp hệ thống | Ô Cài đặt nhanh, widget, plugin Tasker, `ToggleServiceReceiver` [máy] | Ô Cài đặt nhanh (`AodTileService`). Từ Android 15, bật từ ô phải mở app vì service không được khởi động từ nền. Chưa có widget, Tasker |

### 2.2 Quyền

| Quyền | Always On AMOLED [Play] | Custom AOD | Ghi chú |
|---|---|---|---|
| `SYSTEM_ALERT_WINDOW` | ✓ | ✓ | Mở AOD từ nền |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | ✓ | ✓ | Giữ app chạy nền |
| `POST_NOTIFICATIONS` | ✓ | ✓ (tùy chọn) | Hiện thông báo thường trực |
| `WAKE_LOCK` | ✓ | ✓ | |
| `RECEIVE_BOOT_COMPLETED` | ✓ | ✓ | |
| `INTERNET`, `ACCESS_NETWORK_STATE` | ✓ | ✓ | Custom AOD: quảng cáo, consent, `NetworkManager` của base |
| `ACCESS_WIFI_STATE` | ✓ | – | |
| `AD_ID`, 3 quyền `ACCESS_ADSERVICES_*` | ✓ | ✓ | Do SDK quảng cáo thêm (Custom AOD: Google Mobile Ads) |
| Quyền của AppLovin | ✓ | – | |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | ✓ | – | Custom AOD chạy được ở chế độ pin mặc định của HyperOS [FakeAOD] |
| `READ_PHONE_STATE` | ✓ | – | Custom AOD nhận biết cuộc gọi qua `AudioManager` |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | ✓ | ✓ | Thông báo trên AOD. Ở Custom AOD đây là quyền bảo vệ service listener (chỉ hệ thống bind được), không phải quyền app xin, nên `aapt2` không đếm vào tổng |
| `CAMERA` | ✓ | – | Đèn pin. `CameraManager.setTorchMode()` không cần quyền này |
| `READ_CALENDAR`, `PACKAGE_USAGE_STATS`, `ACCESS_NOTIFICATION_POLICY`, `REORDER_TASKS`, `VIBRATE` | ✓ | – | Phục vụ các tính năng Custom AOD chưa có |
| `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` | ✓ | – | Ảnh nền. Custom AOD dùng Photo Picker, không cần quyền |
| `BILLING`, `CHECK_LICENSE` | ✓ | – | Mua Premium, kiểm tra bản quyền |
| `c2dm.RECEIVE`, `BIND_GET_INSTALL_REFERRER_SERVICE` | ✓ | – | Firebase Messaging, đo nguồn cài đặt |
| `DEVICE_POWER` | ✓ | – | Quyền hệ thống, app thường không được cấp. Có lẽ còn sót từ "Force doze" |
| `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | ✓ | ✓ | androidx tự sinh |
| **Tổng số quyền** | **31** | **12 + 1 tự sinh** (6 cho AOD, 6 cho quảng cáo và mạng) | Custom AOD kiểm bằng `aapt2 dump permissions` trên bản debug |

Quyền riêng của Xiaomi:

| Quyền | Always On AMOLED | Custom AOD |
|---|---|---|
| Hiển thị trên màn hình khóa (op 10020) | Bắt buộc [Play, máy] | Bắt buộc |
| Mở cửa sổ mới khi chạy nền (op 10021) | Mô tả Play yêu cầu bật [Play] | Không cần trên HyperOS 1.0 [FakeAOD] |
| Tự khởi chạy (op 10008) | Không yêu cầu; trên máy bạn đang tắt [máy] | Không xin. Hệ quả: không tự chạy lại sau khi bị đóng |

### 2.3 Thư viện bên thứ ba

| | Always On AMOLED [máy, Play] | Custom AOD |
|---|---|---|
| Quảng cáo | AppLovin, Google Ad ID, Privacy Sandbox (AdServices) | Google Mobile Ads (AdMob) + UMP consent; Google Ad ID và Privacy Sandbox do SDK thêm |
| Premium, mua hàng | PremiumHelper (Zipoapps), Google Play Billing | Không |
| Analytics, push | Firebase (Messaging), AppMetrica (tiến trình riêng `:AppMetrica`) | Không |
| Khác | WorkManager, WebView sandbox | AndroidX, Compose, Navigation 3, Koin, DataStore, Timber, Lottie, kotlinx-collections-immutable |
| Data safety | Có thể chia sẻ ID thiết bị với bên thứ ba | Google Mobile Ads dùng mã quảng cáo, phải khai báo trong Data safety. Không có analytics |

## 3. Tính năng

✓ có, ✗ chưa có, ⚠️ có một phần, ? chưa kiểm chứng.

| Nhóm | Tính năng | Always On AMOLED | Custom AOD |
|---|---|---|---|
| Hiển thị | Giờ (12/24 giờ theo máy) | ✓ | ✓ |
| | Ngày | ✓ | ✓ |
| | Pin và trạng thái sạc | ✓ | ✓ |
| | Thông báo: icon và nội dung | ✓ [Play] | ⚠️ chỉ icon, mỗi app một icon (không hiện nội dung trên màn hình khóa) |
| | Viền sáng (edge glow) khi có thông báo mới | ✓ [Play] | ✓ (theo màu của app, khoảng 4 giây) |
| | Điều khiển nhạc | ✓ [Play] | ✓ (tên bài, nghệ sĩ, Bài trước / Phát–Tạm dừng / Bài tiếp theo) |
| | Memo luôn hiện | ✓ [Play] | ✗ |
| | Ghi chú, vẽ nhanh | ✓ [Play] | ✗ |
| | Thời tiết | ✓ [Play] | ✗ |
| | Lịch | ? (suy ra từ quyền `READ_CALENDAR`) | ✗ |
| Tùy biến | Mặt đồng hồ: Digital S7, Classic 24H, Analog S7, Analog Pebble… | ✓ [Play] | ✓ (4 mặt: Số, Số xếp chồng, Kim, Kim tối giản) |
| | Font, màu, cỡ chữ | ✓ [Play] | ✓ (4 họ font hệ thống, 8 màu, cỡ 60–150%) |
| | Ảnh nền, wallpaper AMOLED | ✓ [Play] | ⚠️ ảnh tự chọn bằng Photo Picker, hiện mờ 50%; chưa có bộ ảnh AMOLED sẵn |
| | Độ sáng | ✓ chỉnh mức [2017] | ✓ (1–100%, hoặc theo hệ thống) |
| | Tự giảm sáng khi trời tối (cảm biến ánh sáng) | ✓ [Play] | ✓ (dưới 5 lux giảm về 1%, chờ 2 giây để không nhấp nháy) |
| | Ép xoay ngang, dùng làm đồng hồ đêm | ✓ [Play] | ✓ (ngang theo cảm biến, bố cục đồng hồ trái, thông tin phải) |
| Tương tác | Chạm 2 lần để thoát | ✓ [Play] | ✓ |
| | Vuốt lên/xuống, phím âm lượng, phím back | ✓ [Play] | ✓ (6 thao tác, mỗi thao tác chọn 1 trong 7 hành động) |
| | Nút nguồn khi AOD đang hiện | Mở lại AOD [máy] | Về màn hình khóa |
| | Bật đèn pin bằng cử chỉ | ✓ [2017] | ✓ (gán cho bất kỳ thao tác nào, không cần quyền `CAMERA`) |
| | Nhấc máy để bật (raise to wake) | ✓ [Play] | ⚠️ hiện lại đồng hồ sau khi đã tối; chỉ máy có cảm biến nhấc máy chuẩn của Android |
| Pin và quy tắc | Dịch vị trí chống burn-in | ✓ [Play] | ✓ (mỗi phút) |
| | Chế độ trong túi | ✓ [Play] | ✓ (đen, sáng lại khi lấy ra) |
| | Bỏ qua khi pin dưới ngưỡng | ✓ [2017] | ✓ |
| | Chỉ khi đang sạc, hoặc chỉ khi dùng pin | ✓ [2017] | ✓ (theo việc cắm nguồn, kể cả khi pin đầy hoặc giới hạn sạc) |
| | Kết thúc sau X phút | ✓ [2017] | ✓ (tối màn hình) |
| | Lịch theo giờ | ? | ✓ (mặc định 07:00–23:00, khung qua nửa đêm được) |
| Hệ thống | Tự đóng khi mở khóa | ✓ | ✓ |
| | Nhường chỗ cho cuộc gọi | ✓ (`READ_PHONE_STATE`) | ✓ (không cần quyền) |
| | Không mở AOD khi báo thức đang reo | ? | ✓ |
| | Không tự mở khóa bằng khuôn mặt khi AOD hiện | ✓ [máy] | ✓ [FakeAOD] |
| | Xem thử AOD trong app | ? | ✓ |
| | Màn hình quyền có trạng thái và nút mở đúng trang | ? | ✓ |
| | Dòng chẩn đoán lần mở gần nhất | ? | ✓ |
| | Chọn ngôn ngữ trong app | ? | ✓ (English, tiếng Việt) |
| Tích hợp | Tasker | ✓ [máy] | ✗ |
| | Ô Cài đặt nhanh | ✓ [máy] | ✓ (Android 15+: bật từ ô thì mở app) |
| | Widget bật/tắt | ✓ [máy] | ✗ |
| | Greenify, Force doze (cần root hoặc ADB) | ✓ [Play] | ✗ (đã lỗi thời) |
| Kinh doanh | Quảng cáo | ✓ | ⚠️ hạ tầng AdMob có, đang là id test |
| | Premium | ✓ | ✗ |

## 4. Kết luận

- **Lõi kỹ thuật ngang app đối thủ:**
  - Cùng cách che màn hình khóa (Activity với cờ cửa sổ), cùng cách mở từ nền (quyền "Hiển thị trên ứng dụng khác"), cùng cách giữ chạy nền (foreground service `specialUse`).
  - Custom AOD còn xử lý tốt hơn hai trường hợp: bấm nguồn tắt rồi bật lại ngay, và không mở AOD khi báo thức đang reo.
  - Các số đo là của FakeAOD. Custom AOD giữ cùng cơ chế sau khi refactor, nhưng phải chạy lại trên máy để xác nhận.
- **Ít quyền hơn:** 12 so với 31. Trong 12 quyền, 6 cho AOD và 6 cho quảng cáo và mạng của base. Không có analytics, Premium hay quyền điện thoại.
- **Khác có chủ ý:** nút nguồn đưa về màn hình khóa thay vì mở lại AOD.
- **Giai đoạn 1 (2026-10-04) đã thêm**, không cần quyền mới: luật theo sạc, lịch theo giờ, chỉnh mức độ sáng, ô Cài đặt nhanh, màn chọn ngôn ngữ. Chưa chạy trên máy.
- **Giai đoạn 2 (2026-10-04) đã thêm**, cần quyền đặc biệt "Truy cập thông báo": icon thông báo, viền sáng, điều khiển nhạc. Khác đối thủ: chỉ hiện icon, không hiện nội dung thông báo. Chưa chạy trên máy.
- **Giai đoạn 3 (2026-10-04) đã thêm**, không cần quyền mới: 4 mặt đồng hồ, font, màu, cỡ, ảnh nền, xoay ngang làm đồng hồ đêm. Chưa chạy trên máy.
- **Giai đoạn 4 (2026-10-04) đã thêm**, không cần quyền mới: hành động cho thao tác và phím, đèn pin, tự giảm sáng, nhấc máy để hiện lại đồng hồ. Chưa chạy trên máy.
- **Khoảng trống lớn nhất còn lại là nội dung và tích hợp.** Theo mức ảnh hưởng tới người dùng, nên làm theo thứ tự:
  1. Widget bật/tắt, tùy chọn hiện nội dung thông báo.
  2. Memo, ghi chú, lịch, thời tiết.
  3. Tasker, Premium.
