# Custom AOD

Always On Display giả lập cho những máy Android mà AOD có sẵn tự tắt sau vài giây (ví dụ nhiều máy Xiaomi chỉ có chế độ 10 giây).

Ứng dụng không dùng chế độ AOD thật của hệ thống. Mỗi khi màn hình tắt, app mở một Activity nền đen có đồng hồ lên trên màn hình khóa, và Activity đó tự bật màn hình lên lại. Trên màn OLED, điểm ảnh đen không phát sáng nên phần lớn màn hình vẫn tắt. Đây cũng là cách app Always On AMOLED trên Google Play đang làm, kể cả việc dùng quyền "Hiển thị trên ứng dụng khác".

- Repo: [decoutkhanqindev/Custom-AOD](https://github.com/decoutkhanqindev/Custom-AOD).
- Dựng từ base [Android-Base](https://github.com/decoutkhanqindev/Android-Base) (Compose · Clean Architecture · MVI · Navigation 3 · Koin · Coroutines/Flow · AdMob + UMP). Lõi AOD lấy từ demo FakeAOD rồi refactor theo rule của base.

Tài liệu khác:

- [CLAUDE.md](CLAUDE.md): rule kiến trúc của base, và mục 20 "AOD core" — luồng chạy, các điểm không được đổi và lý do. Claude Code tự đọc file này.
- [CHANGELOG.md](CHANGELOG.md): các thay đổi, kể cả lịch sử của FakeAOD.
- [COMPARISON.md](COMPARISON.md): so sánh kỹ thuật và tính năng với Always On AMOLED.

## Trạng thái kiểm thử

Custom AOD build được (`assembleDebug`), lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng **chưa chạy trên máy**. Lõi đã được viết lại theo MVI và manager (xem CHANGELOG), nên cần chạy lại toàn bộ mục "Kiểm tra trên máy thật".

Kết quả của demo FakeAOD trước khi chuyển sang base:

| Máy | Hệ điều hành | Kết quả |
|---|---|---|
| Redmi Note 13 Pro 5G (2312DRA50G) | HyperOS 1.0, Android 14 | Đồng hồ hiện khi tắt màn hình và che được màn hình khóa. Bấm nguồn thì về màn hình khóa, rồi mở khóa bằng khuôn mặt được |
| Máy ảo `sdk_gphone16k_x86_64` | Android 17 | Đồng hồ hiện khi tắt màn hình. Các lần thử đều nằm trong 10 giây ân hạn sau khi dùng app, nên chưa chứng minh được gì về quyền (xem lưu ý ở mục "Kiểm tra trên máy thật") |

Kết quả trên là của bản FakeAOD `targetSdk` 33. Bản `minSdk` 30 / `targetSdk` 37 của FakeAOD build được và đã rà các thay đổi hành vi (mục "targetSdk 37"), nhưng cũng chưa chạy lại trên máy.

Chưa kiểm tra:

- Toàn bộ Custom AOD trên máy thật: Splash (consent, quảng cáo test), màn cài đặt, AOD.
- Khởi động lại máy. Trên Xiaomi, app không xin quyền "Tự khởi chạy", xem mục "Giới hạn đã biết".
- Khóa máy lâu, ví dụ qua đêm.
- Cuộc gọi đến và báo thức reo khi đồng hồ đang hiện.
- Chế độ trong túi, hẹn giờ tối màn hình, ngưỡng pin.
- Android 11 đến 13, và máy của các hãng khác.

## Build và cài đặt

1. Mở thư mục này bằng Android Studio bản hỗ trợ AGP 9.4.1. Chờ Gradle sync, cài SDK Platform 37 (Android 17) nếu được hỏi.
2. Chạy app lên máy bằng nút Run, hoặc dùng dòng lệnh:
   ```
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

Khi chạy bằng dòng lệnh:

- `gradle/gradle-daemon-jvm.properties` yêu cầu JDK 25 cho Gradle daemon. Cách đơn giản nhất là trỏ `JAVA_HOME` tới JDK đi kèm Android Studio (thư mục `jbr`). Code được biên dịch bằng toolchain JDK 17 (`kotlin { jvmToolchain(17) }`); máy chưa có JDK 17 thì plugin foojay tự tải.
- `local.properties` cần `sdk.dir`. Android Studio tự tạo; dùng dòng lệnh thì tự thêm.
- Nếu cắm nhiều máy, thêm `-s <serial>` sau `adb`, ví dụ `adb -s emulator-5554 install -r ...`.
- `install -r` giữ nguyên các quyền đã cấp. Gỡ app rồi cài lại thì mất hết, kể cả hai quyền bắt buộc.
- Bản release (`./gradlew assembleRelease`, R8 + shrink) cần thông tin ký trong `local.properties`, xem mục "Dựng từ Android-Base".

| Lệnh | Dùng để |
|---|---|
| `./gradlew :app:compileDebugKotlin` | Kiểm tra nhanh, ưu tiên dùng |
| `./gradlew assembleDebug` · `./gradlew installDebug` | Build và cài bản debug |
| `./gradlew assembleRelease` | Bản release, cần signing |
| `./gradlew lint` | Lint |

Phiên bản đang dùng, khai báo trong `gradle/libs.versions.toml`:

| Thành phần | Phiên bản |
|---|---|
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.8.0 |
| Kotlin, plugin Compose compiler, plugin serialization | 2.4.20 |
| Compose BOM | 2026.09.00 |
| activity-compose | 1.13.0 |
| Lifecycle (runtime, compose, viewmodel) | 2.11.0 |
| Navigation 3 | 1.2.0 (lifecycle-viewmodel-navigation3 2.11.0) |
| Koin | 4.2.2 |
| kotlinx-coroutines | 1.11.0 |
| DataStore Preferences | 1.2.1 |
| Google Mobile Ads / UMP | 25.5.0 / 4.0.0 |
| Timber · Lottie · kotlinx-collections-immutable | 5.0.1 · 6.7.1 · 0.5.2 |
| `compileSdk` / `minSdk` / `targetSdk` | 37 / 30 / 37 (Android 17 / Android 11 / Android 17) |

Từ AGP 9, Kotlin được tích hợp sẵn nên project không có plugin `org.jetbrains.kotlin.android`.

## Cấp quyền

Mở app, màn hình chính liệt kê từng quyền kèm trạng thái và nút mở đúng trang cài đặt. Trạng thái được đọc lại mỗi khi quay về app. Nếu ROM không có trang cài đặt đó, app mở trang "Thông tin ứng dụng" thay vì báo lỗi.

Lần đầu mở app trên Android 13 trở lên, app hỏi quyền thông báo bằng hộp thoại của hệ thống. App chỉ hỏi một lần, sau đó đổi ở dòng "Thông báo". Quyền này không bắt buộc để AOD chạy. Nó giúp thông báo "Hiện đồng hồ mỗi khi màn hình tắt" của foreground service hiện trên thanh thông báo, để người dùng biết app đang chạy nền, như chính sách Google Play yêu cầu.

| Quyền | Bắt buộc | Dùng để làm gì |
|---|---|---|
| Hiển thị trên ứng dụng khác | Có | Cho phép app tự mở đồng hồ khi đang chạy nền. App không vẽ cửa sổ nổi nào |
| Xiaomi: Hiển thị trên màn hình khóa | Có | Thiếu quyền này, HyperOS giữ đồng hồ ở phía sau màn hình khóa |
| Xiaomi: Mở cửa sổ mới khi chạy nền | Không | Trên HyperOS 1.0, đồng hồ vẫn mở được khi quyền này tắt. Chỉ bật nếu ROM khác chặn |
| Thông báo | Không | Chỉ để hiện thông báo thường trực của service |

Không cần dịch vụ trợ năng, thông báo toàn màn hình, "Sửa đổi cài đặt hệ thống", "Không hạn chế pin" hay "Tự khởi chạy" của Xiaomi.

Vì sao không cần "Không hạn chế pin":

- Foreground service giữ app ở mức ưu tiên cao (nhóm standby `ACTIVE`). Doze và App Standby chỉ hạn chế mạng, báo thức và job của app đang rảnh, không chặn `SCREEN_OFF` gửi tới service đang chạy. Máy cũng thoát Doze ngay khi màn hình sáng.
- Trên HyperOS 1.0, AOD vẫn chạy khi chế độ pin của app là mặc định (`miui_auto`).
- Nút xin quyền này trên HyperOS không đổi danh sách miễn tối ưu pin của Android, mà chỉ mở trang pin riêng của Xiaomi. Vì vậy app cũng không đọc được trạng thái thật để hiện.
- Nếu sau một đêm khóa máy mà AOD không còn hiện, nghĩa là HyperOS đã đóng app. Khi đó vào Thông tin ứng dụng → Tiết kiệm pin → Không hạn chế.

Trên Xiaomi nên làm thêm:

- Tắt AOD có sẵn của máy (Cài đặt → Màn hình khóa → Màn hình luôn bật) để hai chế độ không chồng lên nhau.
- Khóa app trong màn hình đa nhiệm. Vuốt app khỏi đa nhiệm thì HyperOS đóng cả service.

### Quyền khai báo trong manifest

Danh sách sau khi gộp manifest của các thư viện (kiểm bằng `aapt2 dump permissions` trên bản debug):

| Quyền | Loại | Dùng ở đâu |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Quyền đặc biệt, người dùng bật | `AodService` mở `AodActivity` từ nền |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Tự cấp | `AodService` chạy dạng foreground service loại `specialUse` |
| `POST_NOTIFICATIONS` | Hỏi lúc chạy (Android 13+) | Thông báo thường trực của service |
| `WAKE_LOCK` | Tự cấp | Bật lại màn hình khóa khi bấm nguồn lúc đồng hồ đang hiện |
| `RECEIVE_BOOT_COMPLETED` | Tự cấp | `BootReceiver` chạy lại service sau khi khởi động máy |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Tự cấp | Quảng cáo và consent (AdMob, UMP), `NetworkManager` của base |
| `com.google.android.gms.permission.AD_ID` | Tự cấp, do Google Mobile Ads thêm | Mã quảng cáo |
| `ACCESS_ADSERVICES_AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`, `ACCESS_ADSERVICES_TOPICS` | Tự cấp, do Google Mobile Ads thêm | Privacy Sandbox trên Android |

Thư viện androidx tự thêm một quyền nội bộ `com.decoutkhanqindev.custom_aod.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.

## Cách dùng

- **Mở app:** Splash xin consent (form UMP, chỉ hiện khi cần) rồi hiện quảng cáo xen kẽ (đang là id test của Google), sau đó vào màn hình chính. Splash cần mạng; mất mạng thì hộp thoại "Không có kết nối Internet" chặn màn hình.
- **Màn hình tắt** (nút nguồn hoặc hết thời gian chờ): khoảng 1 giây sau, đồng hồ hiện lên. Phần này không cần mạng.
- **Bấm nút nguồn khi đồng hồ đang hiện:** màn hình nháy tắt rồi sáng lên màn hình khóa, giống AOD thật. Bấm nguồn lần nữa thì màn hình tắt và đồng hồ hiện lại.
- **Bấm nguồn bật màn hình lại ngay sau khi vừa tắt:** vào màn hình khóa, đồng hồ không chen vào.
- **Chạm 2 lần:** thoát đồng hồ, về màn hình khóa. Mở khóa bằng khuôn mặt hoặc vân tay từ đây.
- **Mở khóa:** đồng hồ tự đóng.
- **Có cuộc gọi đến hoặc báo thức reo:** màn hình cuộc gọi, báo thức nằm trên đồng hồ, và đồng hồ tự đóng.
- **Hết thời gian đã chọn, máy trong túi, hoặc pin yếu:** đồng hồ chuyển sang đen. Sau đó màn hình tự tắt theo thời gian chờ của máy.
- **Nút "Xem thử" trong app:** hiện đồng hồ ngay mà không cần khóa máy.

Đồng hồ hiện giờ (theo định dạng 12 hoặc 24 giờ của máy), ngày, và phần trăm pin kèm trạng thái sạc. Dòng "Chạm 2 lần để thoát" hiện 3 giây rồi mờ đi. Mỗi phút, nội dung dịch sang một vị trí ngẫu nhiên để chống burn-in.

Ngôn ngữ: app theo ngôn ngữ đã chọn trong app (`DataStoreManager.selectedLangCode`), mặc định English. Bản dịch tiếng Việt đã có trong `values-vi`, nhưng màn chọn ngôn ngữ còn là TODO của base, nên hiện tại giao diện, đồng hồ và thông báo đều là English. Định dạng 12/24 giờ vẫn theo cài đặt của máy.

## Tùy chọn trong app

Lưu trong `DataStoreManager` (file `custom_aod_prefs`), mỗi tùy chọn một key:

| Tùy chọn | Key | Mặc định | Tác dụng |
|---|---|---|---|
| Hiện đồng hồ khi màn hình tắt | `is_aod_enabled` | Bật | Tắt thì service dừng hẳn |
| Giảm độ sáng khi đồng hồ đang hiện | `is_aod_dim_brightness` | Bật | Đặt độ sáng cửa sổ đồng hồ ở mức thấp nhất (`screenBrightness = 0.01`) |
| Tối màn hình khi máy ở trong túi hoặc bị úp | `is_aod_proximity_enabled` | Bật | Cảm biến tiệm cận bị che liên tục 3 giây thì đồng hồ chuyển sang đen |
| Tối màn hình sau | `aod_timeout_minutes` | Không bao giờ | Từ 0 (không bao giờ) đến 120 phút, mỗi nấc 5 phút |
| Bỏ qua đồng hồ khi pin dưới | `aod_min_battery` | 15% | Từ 0 (tắt) đến 50%, mỗi nấc 5%. Khi đang sạc thì không áp dụng |

Dòng cuối màn hình cho biết lần gần nhất đồng hồ có mở được trên màn hình khóa hay không (key `aod_last_wake`). Kết quả được ghi 2 giây sau mỗi lần mở (xem `checkLaunch` ở mục "Các quyết định thiết kế"). Key `is_notifications_asked` đánh dấu đã hỏi quyền thông báo ở lần mở đầu tiên.

AOD đọc các tùy chọn một lần lúc mở. Đổi tùy chọn thì lần AOD sau mới áp dụng.

## Sơ đồ hoạt động

```
màn hình tắt ──▶ AodService ──startActivity──▶ AodActivity (che màn hình khóa, bật màn hình)
     ▲                                              │
     │              chạm 2 lần / mở khóa / cuộc gọi / báo thức ──▶ đóng, về màn hình khóa
     │                                              │
     │                 nút nguồn ──▶ đóng, bật màn hình lên màn hình khóa
     │                                              │
     └──────────── màn hình khóa tắt (nút nguồn, hết giờ chờ) ◀──┘

hết giờ / trong túi / pin yếu ──▶ đen hoàn toàn ──▶ hết thời gian chờ của máy ──▶ màn hình tắt, không mở lại
```

Sơ đồ theo từng lớp (manager, ViewModel, Activity) ở [CLAUDE.md › 20.1](CLAUDE.md#201-luồng).

## Cấu trúc mã nguồn

Đường dẫn tính từ `app/src/main/java/com/decoutkhanqindev/custom_aod/`.

| File | Vai trò |
|---|---|
| `presentation/aod/AodService.kt` | Foreground service: nhận `SCREEN_OFF` và `USER_PRESENT`, quyết định có hiện AOD không, mở AOD, đưa về màn hình khóa khi bấm nguồn, ghi kết quả mỗi lần mở, thông báo thường trực theo ngôn ngữ của app |
| `presentation/aod/AodActivity.kt` | Host của màn AOD: cờ cửa sổ `FLAG_SHOW_WHEN_LOCKED`, `FLAG_TURN_SCREEN_ON`, `FLAG_KEEP_SCREEN_ON`; độ sáng; ẩn thanh hệ thống; chuyển sang đen khi ViewModel báo |
| `presentation/aod/AodSession.kt` | Trạng thái dùng chung giữa service và Activity (Koin `single`) |
| `presentation/aod/BootReceiver.kt` | Chạy lại service sau khi khởi động máy hoặc cập nhật app |
| `presentation/screens/aod/AodViewModel.kt` | Logic của AOD: pin, chống burn-in mỗi phút, hết giờ, trong túi, pin yếu, đóng khi có cuộc gọi hoặc báo thức, chạm 2 lần |
| `presentation/screens/aod/AodContent.kt` · `AodScreen.kt` · `state/` | Giao diện đồng hồ (giờ, ngày, pin, gợi ý, dịch vị trí) và phần nối với ViewModel |
| `presentation/screens/main/` | Màn hình cấp quyền và tùy chọn (MVI), hỏi quyền thông báo ở lần mở đầu tiên |
| `presentation/model/` | `AodOptionsUiModel`, `BatteryUiModel`, `PermissionValue`, `PermissionUiModel`, `WakeResultValue` |
| `presentation/components/AppLanguageProvider.kt` | Áp ngôn ngữ đã chọn cho `MainActivity` và `AodActivity` |
| `presentation/MainActivity.kt` | Activity chính: consent, ngôn ngữ, theme, khởi động service |
| `data/device/screen/ScreenStateManager.kt` | Sự kiện tắt màn hình / mở khóa, màn hình có đang sáng, có khóa bảo mật, wake lock bật lại màn hình |
| `data/device/battery/BatteryStateManager.kt` | Phần trăm pin và trạng thái sạc |
| `data/device/audio/AudioStateManager.kt` | Đang có chuông, cuộc gọi hoặc báo thức |
| `data/device/proximity/ProximityManager.kt` | Cảm biến tiệm cận |
| `data/device/permission/PermissionManager.kt` | Đọc quyền overlay, thông báo, và quyền riêng của Xiaomi |
| `data/local/datastore/DataStoreManager.kt` | Lưu tùy chọn AOD cùng các prefs của base |
| `utils/ContextExt.kt` | `registerSystemReceiver` |
| `res/values-v31/themes.xml` | Splash màu đen cho AOD trên Android 12+, thay vì icon app |
| `res/xml/data_extraction_rules.xml` | Không sao lưu và không chuyển dữ liệu app sang máy mới (Android 12+) |

Từ FakeAOD sang Custom AOD:

| FakeAOD | Custom AOD |
|---|---|
| `AodService.kt` | `presentation/aod/AodService.kt` + `ScreenStateManager`, `AudioStateManager`, `BatteryStateManager` |
| `AodActivity.kt` | `presentation/aod/AodActivity.kt` (cửa sổ) + `presentation/screens/aod/AodViewModel.kt` (logic) |
| `AodSession.kt` | `presentation/aod/AodSession.kt`, từ `object` thành Koin `single` |
| `BootReceiver.kt` | `presentation/aod/BootReceiver.kt` |
| `ProximityGate.kt` | `ProximityManager` (cảm biến) + `AodViewModel` (đếm 3 giây, sáng lại) |
| `ui/AodScreen.kt` | `presentation/screens/aod/AodContent.kt` |
| `ui/SettingsScreen.kt` | `presentation/screens/main/` |
| `ui/Theme.kt` | `presentation/theme/Theme.kt` + `Color.kt` |
| `AodPrefs.kt` | Các key trong `DataStoreManager` |
| `MiuiPerm.kt` | `PermissionManager` (đọc quyền) + `MainScreen` (mở trang "Quyền khác") |
| `SystemPages.kt` | `utils/ContextExt.kt` (`registerSystemReceiver`) + `MainScreen` (mở trang cài đặt) |
| `MainActivity.kt` | `presentation/MainActivity.kt` của base, thêm khởi động service |

## Các quyết định thiết kế

- **Vì sao dùng Activity, không dùng overlay:** overlay, kể cả `TYPE_ACCESSIBILITY_OVERLAY` của dịch vụ trợ năng, chỉ vẽ đè lên màn hình khóa. Với hệ thống, màn hình khóa vẫn đang hiện, nên có 3 hệ quả. Mở khóa bằng khuôn mặt chạy ngay mỗi lần AOD hiện. Vòng vân tay của Xiaomi nằm trên AOD. Màn hình khóa lóe lên trước khi overlay vẽ xong. Activity `showWhenLocked` thì che (occlude) màn hình khóa thật sự, giống Always On AMOLED.
- **Vì sao dùng foreground service:** `ACTION_SCREEN_OFF` chỉ được gửi tới receiver đăng ký lúc app đang chạy, nên phải có một thành phần sống lâu giữ receiver đó. Foreground service không cần quyền đặc biệt nào, chỉ cần một thông báo thường trực ở mức thấp. Service được khởi động khi mở app hoặc bật công tắc, không phụ thuộc quyền thông báo.
- **Vì sao cần "Hiển thị trên ứng dụng khác":** từ Android 10, app chạy nền không được tự mở Activity, và quyền này là một ngoại lệ. Nhờ nó, service mở AOD ngay lúc màn hình còn tắt.
  - Cách thay thế không cần quyền này là thông báo toàn màn hình, đã thử ở bản trước. Nhưng khi đó SystemUI bật màn hình trước rồi mới mở AOD. Kết quả là màn hình khóa lộ ra khoảng 0,2 giây, và MIUI bắt đầu nhận diện khuôn mặt rồi mới hủy.
- **Vì sao bật màn hình bằng cờ cửa sổ, không dùng `android:turnScreenOn`:**
  - Khai báo `turnScreenOn` trong manifest thì Android bật màn hình ngay khi Activity khởi động, trước khi màn hình khóa bị che. Trên Xiaomi, nhận diện khuôn mặt chạy ở mọi lần mở AOD, và màn hình khóa lộ ra khoảng 0,3–0,45 giây.
  - `AodActivity` dùng cờ cửa sổ `FLAG_SHOW_WHEN_LOCKED | FLAG_TURN_SCREEN_ON`, giống hệt cửa sổ của Always On AMOLED. Cờ cửa sổ chỉ có tác dụng khi cửa sổ đã tồn tại, nên màn hình chỉ sáng khi màn hình khóa đã bị che. Trên HyperOS 1.0, log vẫn ghi lý do bật màn hình là `TURN_ON:handleTurnScreenOn`. Nhưng nhận diện khuôn mặt không còn chạy ở 5/6 lần mở được ghi lại. Lần còn lại chạy 0,14 giây rồi bị hủy, ngay sau một lần vừa ở màn hình khóa.
  - `showWhenLocked` vẫn khai báo trong manifest, vì đó là điều kiện để Activity được mở trên màn hình khóa.
- **Nút nguồn:** app không bắt được phím nguồn, nên bấm nguồn khi đồng hồ đang hiện thì hệ thống tắt màn hình. Vì AOD giữ màn hình sáng, một lần tắt màn hình lúc AOD đang hiện chỉ có thể do nút nguồn. Khi đó service đóng AOD và bật lại màn hình bằng wake lock `ACQUIRE_CAUSES_WAKEUP` (`ScreenStateManager.wakeUp`), nên màn hình khóa hiện ra. Bản Always On AMOLED mã nguồn mở năm 2017 cũng làm như vậy. Bản hiện tại của Always On AMOLED thì mở lại AOD.
- **Bật lại màn hình ngay sau khi vừa tắt:** sự kiện `SCREEN_OFF` đến trễ khoảng 0,2–0,8 giây. Nếu lúc đó màn hình đã sáng lại (người dùng vừa bấm nguồn), service không mở AOD nữa. Vì vậy `ScreenStateManager.events` là `SharedFlow`: dùng `StateFlow` thì một lần tắt rồi bật lại ngay sẽ bị gộp mất.
- **Kiểm tra sau khi mở (`checkLaunch`):** 2 giây sau mỗi lần mở, service xem đồng hồ có đang hiện và màn hình có sáng không. Kết quả hiện ở dòng cuối màn hình app. Nếu không, service đóng Activity, vì trên Xiaomi thiếu "Hiển thị trên màn hình khóa" thì Activity bị giấu sau màn hình khóa và sẽ hiện ra sau lần mở khóa kế tiếp.
- **Không tự tắt màn hình:** app thường không có cách tắt màn hình, việc đó cần dịch vụ trợ năng hoặc quyền quản trị thiết bị. Khi cần kết thúc (hết giờ, trong túi, pin yếu), AOD chuyển sang đen hoàn toàn ở độ sáng thấp nhất và bỏ `FLAG_KEEP_SCREEN_ON`. Sau đó thời gian chờ của máy sẽ tắt màn hình. Lần tắt đó được đánh dấu là chủ ý, nên AOD không mở lại. Trong túi, cảm biến tiệm cận đang bị che thì service cũng không bật lại màn hình khóa.
- **Độ sáng:** chỉnh bằng `screenBrightness` của cửa sổ AOD, không cần quyền "Sửa đổi cài đặt hệ thống". Độ sáng được đặt trong `onCreate`, trước khi cửa sổ hiện, để khung đầu tiên đã đúng mức. Pin cũng được đọc đồng bộ lúc mở để khung đầu tiên có ngay dòng pin.
- **Cuộc gọi và báo thức:** màn hình cuộc gọi, báo thức là Activity mở sau nên tự nằm trên AOD, và `noHistory` đóng AOD khi đó. App còn theo dõi `AudioManager` (`AudioStateManager`) để đóng sớm hơn, và không mở AOD khi đang có chuông, cuộc gọi hay báo thức. Không cần quyền `READ_PHONE_STATE`.
- **Kiến trúc theo base:** tùy chọn là prefs nên nằm thẳng trong `DataStoreManager` (base cấm bọc manager bằng Repository/UseCase chỉ để chuyển tiếp). Mọi tín hiệu thiết bị đi qua manager trong `data/device/`; các manager này chỉ đăng ký receiver hoặc cảm biến khi có người dùng tới, để service chạy nền không nhận `BATTERY_CHANGED` liên tục. Logic của màn đồng hồ nằm trong `AodViewModel`; mọi thao tác với cửa sổ nằm ở `AodActivity`, vì ViewModel không được giữ `Context`. Chi tiết và các ngoại lệ ở [CLAUDE.md › 20](CLAUDE.md#20-aod-core).

## Kiểm tra trên máy thật

1. Mở app: Splash có hiện form consent (bản debug giả lập vùng EEA) và quảng cáo test, rồi vào màn hình chính không.
2. Cấp các quyền bắt buộc. Nếu đã cho phép thông báo, kiểm tra đã có thông báo "Shows the clock each time the screen turns off".
3. Bấm "Xem thử": đồng hồ có phủ kín cả thanh trạng thái và thanh điều hướng không.
4. Khóa máy: đồng hồ có hiện sau khoảng 1 giây không, có thấy màn hình khóa lóe lên không. Xem dòng trạng thái ở cuối màn hình chính của app.
5. Nhìn vào máy khi đồng hồ đang hiện: khuôn mặt không được tự mở khóa.
6. Bấm nút nguồn khi đồng hồ đang hiện: màn hình có nháy tắt rồi lên màn hình khóa không.
7. Ở màn hình khóa, bấm nguồn hai lần liên tiếp (tắt rồi bật lại ngay): phải vào màn hình khóa, không bị đồng hồ che.
8. Chạm 2 lần: về màn hình khóa, rồi mở khóa bằng khuôn mặt hoặc vân tay.
9. Gọi vào máy khi đồng hồ đang hiện, cả khi bật chuông và khi để im lặng.
10. Che cảm biến tiệm cận 3 giây: đồng hồ chuyển sang đen. Bỏ tay ra trước khi màn hình tắt: đồng hồ hiện lại.
11. Đổi tùy chọn, đóng hẳn app rồi mở lại: tùy chọn còn nguyên.
12. Tắt mạng rồi mở app: hộp thoại mất mạng chặn màn hình. Khóa máy: đồng hồ vẫn hiện.
13. Khởi động lại máy, chưa mở app, rồi khóa máy: xem đồng hồ có hiện không.

Lưu ý khi test trên máy ảo hoặc ngay sau khi vừa dùng app: trong 10 giây sau khi app vừa mở hoặc đóng một Activity, Android cho phép mở Activity từ nền mà không cần quyền gì (log ghi `BAL_ALLOW_GRACE_PERIOD`). Vì vậy đồng hồ có thể hiện dù chưa bật "Hiển thị trên ứng dụng khác". Muốn test đúng thì về màn hình chính, đợi hơn 10 giây rồi mới khóa máy.

Xem log khi có vấn đề:

```
adb logcat -s AodService AodViewModel MainViewModel ActivityTaskManager PowerManagerService
```

Log của app đi qua Timber nên chỉ có ở bản debug. Cách đọc log:

- `START u0 {... AodActivity}` cho biết đồng hồ đã được mở. Lý do ở cuối dòng:
  - `BAL_ALLOW_SAW_PERMISSION`: nhờ quyền "Hiển thị trên ứng dụng khác".
  - `BAL_ALLOW_GRACE_PERIOD`: nhờ khoảng 10 giây vừa nói ở trên.
- `Abort background activity starts` kèm `result code=102`: hệ thống chặn không cho mở đồng hồ.
- `MIUILOG- Permission Denied Activity KeyguardLocked`: thiếu quyền "Hiển thị trên màn hình khóa" của Xiaomi.
- `MIUILOG- Reject RestartService`: HyperOS không cho service tự chạy lại, vì app không có quyền "Tự khởi chạy".
- `Waking up ... customaod:lockscreen`: service bật lại màn hình khóa sau khi bấm nguồn.

## Giới hạn đã biết

- **Tốn pin hơn AOD thật.** Màn hình chạy ở chế độ bình thường, không phải chế độ doze tiết kiệm điện. Chỉ nên dùng trên màn OLED.
- **Nhịp tắt rồi sáng:** màn hình tắt khoảng 1 giây rồi mới sáng lại với đồng hồ. Always On AMOLED cũng vậy.
- **Nút nguồn nháy màn hình:** đang AOD bấm nguồn thì màn hình tắt khoảng 0,5–1 giây rồi mới lên màn hình khóa, vì app không bắt được phím nguồn. Bấm thêm lần nữa trong lúc đang nháy vẫn về màn hình khóa.
- **Không tắt màn hình ngay được** khi hết giờ, trong túi hoặc pin yếu. Màn hình đen cho tới khi hết thời gian chờ của máy.
- **Khuôn mặt và vân tay** không dùng được khi đồng hồ đang hiện. Cần chạm 2 lần hoặc bấm nguồn để về màn hình khóa trước.
- **Có một thông báo thường trực** của foreground service, ở mức thấp.
- **Trên Xiaomi, app không tự chạy lại** sau khi bị đóng (vuốt khỏi đa nhiệm, khởi động lại máy, cập nhật app), vì app không xin quyền "Tự khởi chạy". HyperOS chặn việc khởi động lại service (log: `MIUILOG- Reject RestartService ... AodService`). AOD chỉ chạy lại khi mở app. Muốn tự chạy lại thì người dùng bật "Tự khởi chạy" trong cài đặt app. `BootReceiver` vẫn được giữ cho các máy Android khác.
- **Mã quyền của Xiaomi** (10020, 10021) không có tài liệu chính thức và có thể đổi theo phiên bản HyperOS. Khi đó app hiện dấu "?" thay vì trạng thái.
- **Màn hình cài đặt cần mạng** (Splash chờ consent, hộp thoại mất mạng của base). AOD không cần mạng.
- **Giao diện đang là English** cho tới khi có màn chọn ngôn ngữ.
- **Form consent có thể hiện trên AOD** nếu nó tải xong đúng lúc AOD đang mở (rất hiếm), xem CLAUDE.md › 20.5.

## targetSdk 37

App target Android 17 (API 37), chạy từ Android 11 (API 30) trở lên. Đây là các thay đổi hành vi theo `targetSdk` có liên quan tới app, đã rà từ Android 14 tới 17, và cách app xử lý:

| Từ | Thay đổi | Ảnh hưởng tới app |
|---|---|---|
| Android 14 (34) | Foreground service phải khai báo loại | Đã có `specialUse`, quyền `FOREGROUND_SERVICE_SPECIAL_USE` và property `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` |
| Android 14 (34) | Receiver đăng ký lúc chạy phải ghi rõ có export hay không | `registerSystemReceiver` luôn dùng `RECEIVER_NOT_EXPORTED`. App chỉ nhận broadcast của hệ thống |
| Android 14 (34) | Siết intent ngầm và `PendingIntent` | App chỉ dùng intent tường minh và `FLAG_IMMUTABLE` |
| Android 14–17 | Tài liệu ghi `ACQUIRE_CAUSES_WAKEUP` "sẽ cần" quyền `TURN_SCREEN_ON` | AOSP chưa áp dụng cho `targetSdk` nào (`@EnabledSince(CUR_DEVELOPMENT)`). Wake lock bật lại màn hình khóa vẫn chạy |
| Android 15 (35) | Bắt buộc vẽ tràn viền (edge-to-edge) | `MainActivity` gọi `enableEdgeToEdge()`, màn hình dùng `Scaffold` và inset của nó. `AodActivity` tự ẩn thanh hệ thống |
| Android 15 (35) | App có quyền "Hiển thị trên ứng dụng khác" chỉ được khởi động foreground service từ nền khi đang có overlay hiển thị | Service chỉ được khởi động khi app ở tiền cảnh, hoặc từ `BOOT_COMPLETED` và `MY_PACKAGE_REPLACED` là hai trường hợp được miễn |
| Android 15 (35) | `BOOT_COMPLETED` không được khởi động một số loại foreground service | `specialUse` không thuộc danh sách bị chặn |
| Android 16 (36) | Bỏ tùy chọn tắt edge-to-edge; bật sẵn predictive back | App không override `onBackPressed`. Back trong app do Navigation 3 xử lý |
| Android 16–17 | Màn hình lớn (sw ≥ 600dp) bỏ qua khóa hướng xoay và giới hạn kích thước | `MainActivity` khóa dọc (theo base), nên trên màn hình lớn hệ thống bỏ qua khóa này. `AodActivity` không khóa hướng xoay |
| Android 17 (37) | Siết mở Activity từ nền qua `PendingIntent` và `IntentSender` (`MODE_BACKGROUND_ACTIVITY_START_ALLOWED`) | App không dùng đường này. AOD được mở trực tiếp nhờ quyền "Hiển thị trên ứng dụng khác" |
| Android 17 (37) | Siết âm thanh khi chạy nền: phát, xin audio focus, đổi âm lượng | `AudioStateManager` chỉ đọc trạng thái (`getMode`, `getActivePlaybackConfigurations`) và đăng ký listener |
| Android 17 (37) | Không sửa được field `static final` bằng reflection | `PermissionManager` chỉ gọi method ẩn `checkOpNoThrow`. Method này được đánh dấu `@UnsupportedAppUsage` nhưng không giới hạn `targetSdk` |

Nếu đưa lên Google Play:

- Khai báo loại foreground service `specialUse` trong Play Console, gồm mô tả chức năng, ảnh hưởng nếu service bị hoãn hoặc bị ngắt, và video minh họa.
- `targetSdk` 37 đã đạt yêu cầu tối thiểu 36 mà Play áp dụng từ 31/08/2026.
- Có quảng cáo: khai báo "Chứa quảng cáo" và mục Data safety cho mã quảng cáo (`AD_ID`) mà Google Mobile Ads dùng.

## Dựng từ Android-Base

Project được tạo theo các bước "Tạo project mới từ base" của Android-Base:

- Package / namespace / `applicationId` `com.decoutkhanqindev.android_base` → `com.decoutkhanqindev.custom_aod`.
- `rootProject.name` `Custom-AOD`, `app_name` "Custom AOD", file DataStore `custom_aod_prefs`, `minSdk` 26 → 30.
- Theme thay bằng bảng màu tối của FakeAOD; launcher icon thay bằng icon đồng hồ của FakeAOD.
- Git: remote `origin` trỏ về repo Custom-AOD; lịch sử 2 commit của base vẫn giữ.

Còn phải làm (tìm `TODO` trong Android Studio › View › Tool Windows › TODO):

| Việc | Ở đâu |
|---|---|
| AdMob App ID thật | `AndroidManifest.xml` › meta-data `com.google.android.gms.ads.APPLICATION_ID` |
| Ad unit id thật cho từng placement | `app/build.gradle.kts` › `release { buildConfigField("String", "<PLACEMENT>_ALL_ID", …) }` (debug giữ test id) |
| Form consent | AdMob console › Privacy & messaging › publish message cho App ID thật |
| Màn chọn ngôn ngữ, onboarding | `SplashScreen` (`isFirstOpen`), CLAUDE.md › 5 |
| Font, type scale | `presentation/theme/Type.kt` |

`local.properties` (đã gitignore):

```properties
sdk.dir=...

# Ký release
signing.store.file=/duong/dan/keystore.jks
signing.store.password=...
signing.key.alias=...
signing.key.password=...

# (tuỳ chọn) hash test device để test ad id thật trên máy thật, cách nhau dấu phẩy
admob.test.device.ids=HASH_1,HASH_2
```

## Hướng mở rộng

Các tính năng Always On AMOLED có mà app này chưa có được liệt kê trong [COMPARISON.md](COMPARISON.md). Ưu tiên gợi ý:

- Hiện biểu tượng thông báo bằng `NotificationListenerService`.
- Luật theo sạc (chỉ khi sạc, chỉ khi dùng pin) và lịch tắt ban đêm theo giờ.
- Nhiều kiểu mặt đồng hồ, cho chọn font, màu và ảnh nền.
- Nhận diện cuộc gọi chắc chắn hơn bằng `READ_PHONE_STATE` và `TelephonyCallback`, nếu chấp nhận thêm một quyền.
- Màn chọn ngôn ngữ để dùng bản dịch tiếng Việt đã có.
