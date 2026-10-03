# Changelog

Các thay đổi đáng chú ý của Custom AOD. Định dạng dựa theo [Keep a Changelog](https://keepachangelog.com/vi/1.1.0/).

Project chưa phát hành (`versionName` vẫn là `v1.0.0` của base), nên các thay đổi được gom theo ngày. Lịch sử của demo FakeAOD, nơi lõi AOD được làm ra trước khi chuyển sang base, giữ ở cuối file. Mã nguồn demo FakeAOD không nằm trong repo này.

## [Chưa phát hành] – 2026-10-03 – Chuyển sang Android-Base

Lõi AOD của FakeAOD được đưa vào base [Android-Base](https://github.com/decoutkhanqindev/Android-Base) (repo Custom-AOD) và refactor theo rule của base: tùy chọn vào `DataStoreManager`, tín hiệu thiết bị thành manager, màn đồng hồ và màn cài đặt theo MVI. Cơ chế lõi giữ như FakeAOD; các chỗ hành vi khác ghi ở mục "Khác FakeAOD". Bản này build được, lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy.

### Thêm

- Toàn bộ hạ tầng của base:
  - Koin, Navigation 3 (Splash → Main), MVI (`BaseViewModel`), DataStore, Timber.
  - AdMob + consent UMP, với quảng cáo xen kẽ ở Splash (đang là id test của Google).
  - `NoInternetDialog`, và hạ tầng đổi ngôn ngữ trong app (`LanguageManager`, `LanguageValue`). Màn chọn ngôn ngữ chưa có.
- Manager tín hiệu thiết bị trong `data/device/`:
  - `ScreenStateManager`: sự kiện `SCREEN_OFF` / `USER_PRESENT` dạng `SharedFlow`, `isInteractive`, `isDeviceSecure`, wake lock bật lại màn hình khóa.
  - `BatteryStateManager`: phần trăm pin và trạng thái sạc, kèm hàm đọc đồng bộ từ broadcast sticky.
  - `AudioStateManager`: `isBusy` (chuông, cuộc gọi, báo thức) và `isBusyNow()`.
  - `ProximityManager`: `isNear`.
  - `PermissionManager`: quyền overlay, thông báo, và hai quyền riêng của Xiaomi.
- Màn AOD theo MVI trong `presentation/screens/aod/`: `AodViewModel`, `AodContent`, `AodScreen`, `state/`.
- Màn cài đặt theo MVI trong `presentation/screens/main/`, kèm `AodOptionsUiModel`, `BatteryUiModel`, `PermissionValue`, `PermissionStatusValue`, `PermissionUiModel`, `WakeResultValue`.
- `AppLanguageProvider`, dùng chung cho `MainActivity` và `AodActivity`.
- Trạng thái quyền hiện bằng Material icon có `contentDescription` (Đã cấp / Chưa cấp / Không rõ), thay cho ký tự ✓ ✕ ? –.
- Bản dịch tiếng Việt cho các chuỗi của base.
- `CLAUDE.md` mục 20 "AOD core": luồng, thành phần, các điểm không được đổi và lý do, ngoại lệ so với MVI chuẩn, lưu ý về ads.

### Thay đổi

- Định danh:
  - Package `com.example.fakeaod` → `com.decoutkhanqindev.custom_aod`.
  - Tên app "Fake AOD" → "Custom AOD".
  - Wake lock `fakeaod:lockscreen` → `customaod:lockscreen`.
  - Extra xem thử đổi theo package mới.
- Lưu tùy chọn: SharedPreferences `aod` (`AodPrefs`) → DataStore `custom_aod_prefs` (`DataStoreManager`), mỗi tùy chọn một key. Dữ liệu của FakeAOD không được chuyển sang, vì đây là app khác package.
- `AodService`:
  - Receiver chuyển vào `ScreenStateManager`.
  - `Handler` → coroutine. Kiểm tra sau khi mở đổi tên `launchCheck` → `checkLaunch()`, vẫn sau 2 giây.
  - `Log` → Timber.
- `AodActivity`:
  - Logic chuyển vào `AodViewModel`: pin, tick mỗi phút (giờ hiện tại và dịch vị trí), ẩn dòng gợi ý sau 3 giây, hết giờ, tiệm cận 3 giây, âm thanh, chạm 2 lần.
  - `AodContent` chỉ vẽ theo state, không còn tự đếm giờ (`produceState`) hay hẹn giờ ẩn dòng gợi ý.
  - Activity chỉ còn cờ cửa sổ, độ sáng, ẩn thanh hệ thống, và chuyển sang đen theo `state.isDark`.
- `AodSession`: `object` → Koin `single`. `isCovered` đọc từ `ProximityManager`.
- Các file khác:
  - `ProximityGate` → `ProximityManager` cộng `AodViewModel`.
  - `MiuiPerm` → `PermissionManager` (đọc quyền) cộng `utils/ContextExt.kt` (`openMiuiPermissionEditor`).
  - `SystemPages` → `utils/ContextExt.kt` (`registerSystemReceiver`, `openSettingsPage`, `openOverlaySettings`, `openNotificationSettings`, `packageUri`).
- File Compose không còn hằng số top-level:
  - Pattern giờ, ngày → `strings.xml` (`translatable="false"`).
  - Bước và giới hạn thanh trượt → companion của `AodOptionsUiModel`.
  - Trạng thái quyền → `PermissionStatusValue`.
  - Intent của trang "Quyền khác" (Xiaomi) → `utils/ContextExt.kt`.
  - Thời lượng animation viết inline (`tween(durationMillis = …)`).
- Hằng số `_MS` → `_MILLIS`, giữ nguyên giá trị.
- Theme:
  - `FakeAodTheme` → `AppTheme` của base, cùng bảng màu. Màu thành token trong `Color.kt`.
  - Trạng thái "không rõ" của quyền dùng `onSurfaceVariant` (`#9A9A9A`, trước là `#8A8A8A`).
- Vị trí đồng hồ dùng `Modifier.offset` dạng lambda (lint `UseOfNonLambdaOffsetOverload` của FakeAOD).
- Đồng hồ mờ dần khi chuyển sang đen và hiện dần khi sáng lại (`AnimatedVisibility`), thay vì tắt bật tức thì. Lý do là rule "state đổi phải có animation" của base.
- Những phần của base được chỉnh:
  - `minSdk` 26 → 30. `NoInternetDialog` bỏ nhánh dưới API 29.
  - `Theme.App` dùng nền đen. `MainActivity` gọi `enableEdgeToEdge` với icon thanh hệ thống màu sáng, và khởi động `AodService` nếu đang bật.
  - Thay `backup_rules.xml` mẫu bằng `allowBackup="false"` cộng `data_extraction_rules.xml` của FakeAOD.
  - Launcher icon thay bằng icon đồng hồ của FakeAOD.
- `CLAUDE.md`: lệnh grep số 5 loại trừ `presentation/effects/`. `LaunchedWithLifecycleEffect.kt` bắt buộc dùng `repeatOnLifecycle`, nên trước đó grep này báo lỗi ngay trên base gốc.
- Git: remote `origin` đổi từ Android-Base sang Custom-AOD.

### Khác FakeAOD

- Giao diện, đồng hồ (ngày, gợi ý, pin) và thông báo đi theo ngôn ngữ chọn trong app, mặc định English. FakeAOD theo ngôn ngữ của máy.
- Mở app phải qua Splash (consent, quảng cáo) trước màn cài đặt, và cần mạng.
- Service đọc pin từ `BATTERY_CHANGED`, cùng nguồn với màn AOD: phần trăm từ level/scale, đang sạc khi trạng thái là CHARGING hoặc FULL. Trước đó service đọc `BatteryManager.CAPACITY` và `isCharging`.
- Dưới Android 12, `AudioStateManager` hỏi lại mode âm thanh mỗi phút. FakeAOD hỏi trong tick mỗi phút của AOD, và bỏ qua khi pin yếu hoặc khi xem thử.
- AOD đóng khi trạng thái "bận" (chuông, cuộc gọi, báo thức) chuyển sang true sau lúc mở. FakeAOD đóng ở mỗi lần callback báo có âm thanh khẩn trong danh sách.
- Thanh trượt vẫn lưu ở từng nấc như FakeAOD, nhưng vị trí hiển thị phải chờ DataStore ghi xong.
- Màn cài đặt khóa dọc, theo base.
- Log của app chỉ có ở bản debug, vì Timber chỉ được gắn ở debug.

### Xóa

- `AodPrefs`, `MiuiPerm`, `SystemPages`, `ProximityGate`, `ui/Theme.kt`: thay bằng các thành phần ở trên.
- `values/colors.xml`: dùng `@android:color/black`.
- `res/xml/backup_rules.xml` của base.

---

## FakeAOD – 2026-10-03

Lịch sử của demo FakeAOD, chép nguyên từ CHANGELOG của demo. Tên file và đường dẫn trong mục này là của FakeAOD.

AOD chuyển từ overlay của dịch vụ trợ năng sang Activity hiện trên màn hình khóa, giống cách Always On AMOLED làm. Số quyền người dùng phải bật giảm còn 2. Đã chạy thử trên Redmi Note 13 Pro 5G (HyperOS 1.0, Android 14) và máy ảo Android 17.

### Thêm

- `AodService`: foreground service loại `specialUse`.
  - Giữ receiver `SCREEN_OFF` và `USER_PRESENT`, mở AOD mỗi khi màn hình tắt.
  - Sau mỗi lần mở 2 giây, ghi lại kết quả (`launchCheck`).
- `AodActivity`: màn hình AOD bằng Compose.
  - Che màn hình khóa: `showWhenLocked` trong manifest, cộng cờ cửa sổ `FLAG_SHOW_WHEN_LOCKED | FLAG_TURN_SCREEN_ON | FLAG_KEEP_SCREEN_ON`.
  - Ẩn thanh hệ thống, đọc pin, dịch vị trí mỗi phút để chống burn-in.
- `AodSession`: trạng thái dùng chung giữa service và Activity.
- `BootReceiver`: chạy lại service sau `BOOT_COMPLETED` và `MY_PACKAGE_REPLACED`.
- Bấm nút nguồn khi đồng hồ đang hiện thì về màn hình khóa. App bật lại màn hình bằng wake lock `ACQUIRE_CAUSES_WAKEUP`.
- Không mở AOD nếu màn hình đã sáng lại trước khi `SCREEN_OFF` tới, tức là khi bấm nguồn hai lần nhanh.
- Không mở AOD khi đang có chuông, cuộc gọi hoặc báo thức. Có kiểm tra cả `activePlaybackConfigurations`.
- Chế độ đen thay cho tắt màn hình khi hết giờ, khi trong túi, hoặc khi pin yếu. Lấy máy khỏi túi trước khi màn hình tắt thì đồng hồ sáng lại.
- Không bật lại màn hình khóa khi cảm biến tiệm cận đang bị che.
- Lần đầu mở app trên Android 13+, hỏi quyền thông báo một lần.
- Service đăng lại thông báo thường trực mỗi khi nhận lệnh start, để thông báo hiện ngay sau khi người dùng cho phép.
- Splash màu đen cho AOD trên Android 12+ (`res/values-v31/themes.xml`), thay vì icon app.
- Danh sách quyền mới trên màn hình chính:
  - "Hiển thị trên ứng dụng khác" (bắt buộc).
  - Xiaomi "Hiển thị trên màn hình khóa" (bắt buộc).
  - Xiaomi "Mở cửa sổ mới khi chạy nền" (tùy chọn).
  - "Thông báo" (tùy chọn).
- `CHANGELOG.md` và `COMPARISON.md` (so sánh kỹ thuật và tính năng với Always On AMOLED).
- `res/xml/data_extraction_rules.xml` và `android:fullBackupContent="false"`. Từ Android 12, `allowBackup="false"` không còn chặn việc chuyển dữ liệu sang máy mới.

### Thay đổi

- AOD không còn là cửa sổ `TYPE_ACCESSIBILITY_OVERLAY` mà là Activity che màn hình khóa. Vì vậy:
  - Nhận diện khuôn mặt không còn tự chạy khi đồng hồ đang hiện.
  - Màn hình khóa không còn lộ ra.
  - Màn hình cuộc gọi và báo thức tự nằm trên đồng hồ.
- Màn hình được bật bằng cờ cửa sổ `FLAG_TURN_SCREEN_ON`, thay cho cờ trên overlay cộng wake lock.
- Độ sáng chỉnh qua `screenBrightness` của cửa sổ, không ghi vào cài đặt hệ thống nữa.
- Nút nguồn khi đồng hồ đang hiện:
  - Bản đầu: tắt hẳn màn hình.
  - Bản trung gian: mở lại AOD.
  - Bản hiện tại: về màn hình khóa.
- "Tắt hẳn màn hình sau X phút" đổi thành "Tối màn hình sau X phút". "Tắt khi máy ở trong túi" đổi thành "Tối màn hình khi máy ở trong túi".
- Đồng hồ ở định dạng 12 giờ hiện "5:15" thay vì "05:15".
- `ProximityGate` báo cả lúc bị che và lúc được lấy ra.
- `SystemPages.kt`: thêm `registerSystemReceiver`, bỏ `isAodServiceEnabled`.
- `minSdk` 28 → 30 (Android 11), `targetSdk` 33 → 37 (Android 17), `compileSdk` 36 → 37. Các thay đổi hành vi từ Android 14 tới 17 đã được rà, ghi trong mục "targetSdk 37" của README.
  - `AodActivity.hideSystemBars()` bỏ nhánh `systemUiVisibility` cho Android dưới 11.
  - `setDecorFitsSystemWindows` chỉ còn được gọi dưới API 35.
  - `res/mipmap-anydpi-v26` đổi tên thành `res/mipmap-anydpi`, vì `-v26` thừa khi `minSdk` là 30.
- Lý do cũ để giữ `targetSdk = 33` (quyền `TURN_SCREEN_ON`) không còn đúng: AOSP chưa áp dụng yêu cầu này cho `targetSdk` nào, kể cả 37.
- Sửa theo lint sau khi nâng SDK:
  - `BootReceiver` kiểm tra action trước khi xử lý.
  - `ModeWatcher` dùng `@RequiresApi` thay cho `@TargetApi`.
  - `registerSystemReceiver` ghi rõ vì sao Android dưới 13 không cần cờ export.
  - Lint còn 0 lỗi và 12 cảnh báo: có bản mới của thư viện, gợi ý dùng hàm KTX, và một gợi ý tối ưu `Modifier.offset`.
- README viết lại theo kiến trúc mới. Thêm các phần: trạng thái kiểm thử, quyền trong manifest, tùy chọn trong app, cách đọc log.

### Xóa

- Dịch vụ trợ năng: `AodAccessibilityService.kt` và `res/xml/aod_a11y.xml`.
- `AodOverlay.kt`, `ScreenWaker.kt`, `WakeActivity.kt` (đánh thức dự phòng bằng thông báo toàn màn hình), `Brightness.kt` (ghi độ sáng hệ thống).
- Quyền `WRITE_SETTINGS`, `TURN_SCREEN_ON`, `USE_FULL_SCREEN_INTENT`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
- Các dòng quyền "Dịch vụ trợ năng", "Sửa đổi cài đặt hệ thống", "Thông báo toàn màn hình", "Không hạn chế pin" và Xiaomi "Tự khởi chạy"; hàm `MiuiPerm.openAutostart`.
- Broadcast `ACTION_PREVIEW`. Trên Android 9–12, receiver này bị export, nên app khác có thể bật đồng hồ. Nút "Xem thử" giờ mở thẳng Activity.
- Dependency `androidx.savedstate`, vì chỉ overlay đã xóa dùng tới.
- Khối `lint { disable += listOf("ExpiredTargetSdkVersion", "OldTargetApi") }` trong `app/build.gradle.kts`.

### Sửa lỗi

- Mở khóa bằng khuôn mặt chạy mỗi lần đồng hồ hiện, và có thể mở khóa luôn. Nguyên nhân: overlay không che được màn hình khóa.
- Màn hình khóa lóe lên 0,2–0,45 giây trước khi đồng hồ vẽ xong.
- Bấm nguồn tắt rồi bật lại ngay vẫn bị đồng hồ đè lên màn hình khóa.
- Đồng hồ có thể đè lên báo thức đang reo nếu màn hình tắt đúng lúc đó.

### Diễn biến trong phiên

Thứ tự các bước đã làm, và lý do mỗi bước bị thay thế:

1. **Bản ban đầu:** overlay của dịch vụ trợ năng, wake lock, và thông báo toàn màn hình làm đường dự phòng.
   - Overlay vẽ đục nhưng màn hình khóa vẫn hoạt động bên dưới.
   - Nhận diện khuôn mặt chạy ở mỗi lần hiện, vòng vân tay của Xiaomi nằm trên đồng hồ, và màn hình khóa lóe lên khoảng 0,3 giây.
2. **Activity mở bằng thông báo toàn màn hình:** chỉ cần quyền thông báo và "Hiển thị trên màn hình khóa".
   - SystemUI bật màn hình trước rồi mới mở Activity, nên màn hình khóa vẫn lộ khoảng 0,2 giây.
   - Nhận diện khuôn mặt chạy 0,15–0,3 giây rồi mới bị hủy.
3. **Activity mở bằng "Hiển thị trên ứng dụng khác",** như Always On AMOLED.
   - Service mở đồng hồ lúc màn hình còn tắt, và nút nguồn về màn hình khóa.
   - Log cho thấy `android:turnScreenOn` trong manifest vẫn bật màn hình quá sớm: màn hình khóa lộ 0,3–0,45 giây, và nhận diện khuôn mặt chạy ở mọi lần mở.
4. **Đổi sang cờ cửa sổ** giống Always On AMOLED.
   - Nhận diện khuôn mặt không còn chạy ở 5/6 lần mở được ghi lại.
   - Quyền "Mở cửa sổ mới khi chạy nền" của Xiaomi chuyển thành tùy chọn, vì không cần trên HyperOS 1.0.
5. **Dọn quyền:** bỏ "Không hạn chế pin" và "Tự khởi chạy" sau khi test trên máy. Hệ quả đã ghi trong README: trên Xiaomi, app không tự chạy lại sau khi bị đóng.
6. **Thêm bước hỏi quyền thông báo** ở lần mở app đầu tiên.
7. **Nâng SDK:** `minSdk` lên 30, `targetSdk` và `compileSdk` lên 37. Rà các thay đổi hành vi từ Android 14 tới 17: không có thay đổi nào chặn cơ chế lõi.
8. **Chuyển sang Android-Base:** xem mục đầu file.
