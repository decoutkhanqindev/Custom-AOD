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

Custom AOD build được (`assembleDebug`), lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng **chưa chạy trên máy**. Lõi đã được viết lại theo MVI và manager, giai đoạn 1 (quy tắc nguồn điện, khung giờ, mức độ sáng, ô Cài đặt nhanh, màn chọn ngôn ngữ) giai đoạn 2 (icon thông báo, viền sáng, điều khiển nhạc trên đồng hồ) và giai đoạn 3 (mặt đồng hồ, font, màu, cỡ, ảnh nền, xoay ngang) mới được thêm (xem CHANGELOG), nên cần chạy lại toàn bộ mục "Kiểm tra trên máy thật".

Kết quả của demo FakeAOD trước khi chuyển sang base:

| Máy | Hệ điều hành | Kết quả |
|---|---|---|
| Redmi Note 13 Pro 5G (2312DRA50G) | HyperOS 1.0, Android 14 | Đồng hồ hiện khi tắt màn hình và che được màn hình khóa. Bấm nguồn thì về màn hình khóa, rồi mở khóa bằng khuôn mặt được |
| Máy ảo `sdk_gphone16k_x86_64` | Android 17 | Đồng hồ hiện khi tắt màn hình. Các lần thử đều nằm trong 10 giây ân hạn sau khi dùng app, nên chưa chứng minh được gì về quyền (xem lưu ý ở mục "Kiểm tra trên máy thật") |

Kết quả trên là của bản FakeAOD `targetSdk` 33. Bản `minSdk` 30 / `targetSdk` 37 của FakeAOD build được và đã rà các thay đổi hành vi (mục "targetSdk 37"), nhưng cũng chưa chạy lại trên máy.

Chưa kiểm tra:

- Toàn bộ Custom AOD trên máy thật: Splash (consent, quảng cáo test), màn chọn ngôn ngữ, màn cài đặt, AOD, ô Cài đặt nhanh, thông báo và nhạc trên đồng hồ, giao diện đồng hồ.
- Khởi động lại máy. Trên Xiaomi, app không xin quyền "Tự khởi chạy", xem mục "Giới hạn đã biết".
- Khóa máy lâu, ví dụ qua đêm.
- Cuộc gọi đến và báo thức reo khi đồng hồ đang hiện.
- Chế độ trong túi, hẹn giờ tối màn hình, ngưỡng pin, quy tắc nguồn điện, khung giờ, mức độ sáng.
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
| Truy cập thông báo | Không | Icon thông báo, viền sáng khi có thông báo mới và điều khiển nhạc trên đồng hồ. Thiếu quyền này đồng hồ vẫn chạy, chỉ không có 3 thứ đó |

"Truy cập thông báo" là quyền đặc biệt, bật ở trang riêng của hệ thống (dòng "Truy cập thông báo" trong app mở thẳng trang đó). Từ Android 13, nếu APK được cài bằng cách mở file (không qua cửa hàng hay `adb install`), công tắc này có thể bị khóa ("Cài đặt bị hạn chế"): vào Thông tin ứng dụng › menu ⋮ › "Allow restricted settings" (tên tiếng Việt tùy máy) rồi bật lại.

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

Hai service còn được bảo vệ bằng quyền của hệ thống, để chỉ hệ thống bind được. Đây không phải quyền app xin, nên không có trong danh sách trên: `AodTileService` (`BIND_QUICK_SETTINGS_TILE`), `AodNotificationListener` (`BIND_NOTIFICATION_LISTENER_SERVICE`).

## Cách dùng

- **Mở app:** Splash xin consent (form UMP, chỉ hiện khi cần) rồi hiện quảng cáo xen kẽ (đang là id test của Google), sau đó vào màn hình chính. Splash cần mạng; mất mạng thì hộp thoại "Không có kết nối Internet" chặn màn hình.
- **Lần đầu mở app:** sau Splash là màn chọn ngôn ngữ, đã chọn sẵn ngôn ngữ của máy (ngôn ngữ của máy chưa có bản dịch thì chọn English). Bấm "Xong" để vào màn hình chính; những lần mở sau đi thẳng vào màn hình chính. Đổi lại ở mục "Ứng dụng › Ngôn ngữ" của màn hình chính.
- **Ô Cài đặt nhanh "Custom AOD":** thêm vào bằng cách kéo bảng Cài đặt nhanh xuống rồi bấm sửa. Chạm để bật hoặc tắt đồng hồ (giống công tắc trong app). Từ Android 15, và trên Android 12–14 khi chưa cấp "Hiển thị trên ứng dụng khác", hệ thống không cho service khởi động từ ô này, nên khi bật, app sẽ mở lên để khởi động service (đang khóa máy thì phải mở khóa trước). Tắt thì không cần mở app.
- **Màn hình tắt** (nút nguồn hoặc hết thời gian chờ): khoảng 1 giây sau, đồng hồ hiện lên. Phần này không cần mạng.
- **Bấm nút nguồn khi đồng hồ đang hiện:** màn hình nháy tắt rồi sáng lên màn hình khóa, giống AOD thật. Bấm nguồn lần nữa thì màn hình tắt và đồng hồ hiện lại.
- **Bấm nguồn bật màn hình lại ngay sau khi vừa tắt:** vào màn hình khóa, đồng hồ không chen vào.
- **Chạm 2 lần:** thoát đồng hồ, về màn hình khóa. Mở khóa bằng khuôn mặt hoặc vân tay từ đây.
- **Mở khóa:** đồng hồ tự đóng.
- **Có cuộc gọi đến hoặc báo thức reo:** màn hình cuộc gọi, báo thức nằm trên đồng hồ, và đồng hồ tự đóng.
- **Hết thời gian đã chọn, máy trong túi, pin yếu, hết khung giờ, hoặc cắm/rút sạc trái với quy tắc nguồn điện:** đồng hồ chuyển sang đen. Sau đó màn hình tự tắt theo thời gian chờ của máy.
- **Ngoài khung giờ, sai quy tắc nguồn điện, hoặc pin dưới ngưỡng lúc màn hình tắt:** đồng hồ không hiện, màn hình tắt như bình thường.
- **Nút "Xem thử" trong app:** hiện đồng hồ ngay mà không cần khóa máy.
- **Thông báo** (cần quyền "Truy cập thông báo"): dưới ngày là icon của các app đang có thông báo, mỗi app một icon, tối đa 5, còn lại hiện "+N". Chỉ có icon, không có nội dung. Có thông báo mới khi đồng hồ đang hiện thì 4 cạnh màn hình sáng nhấp nháy khoảng 4 giây, theo màu của app đó.
- **Nhạc** (cần quyền "Truy cập thông báo"): khi có app nhạc đang phát hoặc tạm dừng, đồng hồ hiện tên bài, nghệ sĩ và 3 nút Bài trước, Phát/Tạm dừng, Bài tiếp theo. Như chạm 2 lần, nút bị bỏ qua khi cảm biến tiệm cận đang bị che.
- **Giao diện đồng hồ:** 4 mặt (Số, Số xếp chồng, Kim, Kim tối giản), 4 font, 8 màu, cỡ 60–150%, ảnh nền tự chọn. Bật "Luôn xoay ngang" để dùng làm đồng hồ đêm khi dựng máy: đồng hồ bên trái, ngày, pin và nhạc bên phải.

Đồng hồ hiện giờ (theo định dạng 12 hoặc 24 giờ của máy), ngày, và phần trăm pin kèm trạng thái sạc. Dòng "Chạm 2 lần để thoát" hiện 3 giây rồi mờ đi. Mỗi phút, nội dung dịch sang một vị trí ngẫu nhiên để chống burn-in.

Ngôn ngữ: giao diện, đồng hồ và thông báo theo ngôn ngữ chọn trong app (key `selected_lang_code`, mặc định English), không theo ngôn ngữ của máy. Có English và Tiếng Việt; tên ngôn ngữ trong danh sách viết bằng chính ngôn ngữ đó. Key `is_first_open` đánh dấu đã qua màn chọn ngôn ngữ ở lần mở đầu tiên. Định dạng 12/24 giờ vẫn theo cài đặt của máy.

## Tùy chọn trong app

Lưu trong `DataStoreManager` (file `custom_aod_prefs`), mỗi tùy chọn một key:

| Tùy chọn | Key | Mặc định | Tác dụng |
|---|---|---|---|
| Hiện đồng hồ khi màn hình tắt | `is_aod_enabled` | Bật | Tắt thì service dừng hẳn. Ô Cài đặt nhanh đổi cùng key này |
| Dùng độ sáng riêng cho đồng hồ | `is_aod_custom_brightness` | Bật | Bật: dùng mức độ sáng bên dưới. Tắt: cửa sổ đồng hồ theo độ sáng của hệ thống |
| Độ sáng | `aod_brightness_percent` | 1% | Từ 1 đến 100%, đặt `screenBrightness` của cửa sổ đồng hồ bằng phần trăm / 100. 1% bằng mức "giảm độ sáng" của bản trước |
| Tối màn hình khi máy ở trong túi hoặc bị úp | `is_aod_proximity_enabled` | Bật | Cảm biến tiệm cận bị che liên tục 3 giây thì đồng hồ chuyển sang đen |
| Tối màn hình sau | `aod_timeout_minutes` | Không bao giờ | Từ 0 (không bao giờ) đến 120 phút, mỗi nấc 5 phút |

Mục "Thông báo trên đồng hồ", chỉ có tác dụng khi đã cấp quyền "Truy cập thông báo":

| Tùy chọn | Key | Mặc định | Tác dụng |
|---|---|---|---|
| Hiện biểu tượng thông báo | `is_aod_notification_icons_enabled` | Bật | Icon của app có thông báo, như thanh trạng thái. Bỏ thông báo thường trực, im lặng, thông báo nhạc, và thông báo bị ẩn trên màn hình khóa hoặc bị Không làm phiền ẩn khỏi màn hình chờ |
| Viền sáng khi có thông báo mới | `is_aod_edge_glow_enabled` | Bật | Chỉ khi đồng hồ đang hiện (không tối) và thông báo không bị Không làm phiền chặn |
| Điều khiển nhạc | `is_aod_media_controls_enabled` | Bật | Lấy từ thông báo nhạc mới nhất, như trình phát trên màn hình khóa của hệ thống |

Mục "Giao diện đồng hồ":

| Tùy chọn | Key | Mặc định | Tác dụng |
|---|---|---|---|
| Mặt đồng hồ | `aod_clock_face` | Số | Số / Số xếp chồng (giờ trên, phút dưới) / Kim (có vạch giờ) / Kim tối giản |
| Font chữ | `aod_clock_font` | Mặc định | Mặc định / Có chân / Đơn cách / Viết tay — họ font có sẵn của hệ thống, áp cho giờ và ngày |
| Màu | `aod_clock_color` | Xám | 8 màu dịu trên nền đen, áp cho số giờ hoặc kim. Ngày, pin, gợi ý vẫn màu xám |
| Cỡ đồng hồ | `aod_clock_size_percent` | 100% | Từ 60 đến 150%, mỗi nấc 10% |
| Luôn xoay ngang (đồng hồ đêm) | `is_aod_landscape` | Tắt | Đồng hồ luôn nằm ngang theo chiều dựng máy |
| Ảnh nền | (file `aod_background.jpg` trong bộ nhớ riêng của app) | Không có | Chọn bằng Photo Picker, không cần quyền đọc ảnh. App lưu một bản sao đã thu về cỡ màn hình; ảnh hiện mờ 50% sau đồng hồ |

Mục "Khi nào hiện" (quy tắc). Đồng hồ chỉ hiện khi đủ mọi quy tắc:

| Quy tắc | Key | Mặc định | Tác dụng |
|---|---|---|---|
| Nguồn điện | `aod_charging_rule` | Luôn hiện | Luôn hiện / Chỉ khi đang cắm sạc / Chỉ khi dùng pin. "Đang cắm" tính theo việc có cắm nguồn (`EXTRA_PLUGGED`), nên pin đầy hoặc máy dừng sạc ở 80% vẫn tính là đang cắm |
| Chỉ hiện trong khung giờ | `is_aod_schedule_enabled` | Bật | Tắt thì hiện cả ngày |
| Bắt đầu, Kết thúc | `aod_schedule_start_minute`, `aod_schedule_end_minute` | 07:00, 23:00 | Lưu theo phút trong ngày. Bắt đầu sau kết thúc là khung giờ qua nửa đêm (ví dụ 22:00–06:00). Bắt đầu trùng kết thúc là cả ngày |
| Bỏ qua đồng hồ khi pin dưới | `aod_min_battery` | 15% | Từ 0 (tắt) đến 50%, mỗi nấc 5%. Khi đang sạc thì không áp dụng |

Dòng cuối màn hình cho biết lần gần nhất đồng hồ có mở được trên màn hình khóa hay không (key `aod_last_wake`). Kết quả được ghi 2 giây sau mỗi lần mở (xem `checkLaunch` ở mục "Các quyết định thiết kế"). Key `is_notifications_asked` đánh dấu đã hỏi quyền thông báo ở lần mở đầu tiên.

AOD đọc các tùy chọn và quy tắc một lần lúc mở. Đổi chúng thì lần AOD sau mới áp dụng. Trong lúc đồng hồ đang hiện, quy tắc vẫn được kiểm lại mỗi phút và mỗi khi pin hoặc nguồn cắm thay đổi; sai quy tắc thì đồng hồ chuyển sang đen. "Xem thử" không áp dụng quy tắc.

## Sơ đồ hoạt động

```
màn hình tắt ──▶ AodService ──startActivity──▶ AodActivity (che màn hình khóa, bật màn hình)
     ▲                                              │
     │              chạm 2 lần / mở khóa / cuộc gọi / báo thức ──▶ đóng, về màn hình khóa
     │                                              │
     │                 nút nguồn ──▶ đóng, bật màn hình lên màn hình khóa
     │                                              │
     └──────────── màn hình khóa tắt (nút nguồn, hết giờ chờ) ◀──┘

hết giờ / trong túi / sai quy tắc (pin yếu, hết khung giờ, nguồn điện) ──▶ đen hoàn toàn ──▶ hết thời gian chờ của máy ──▶ màn hình tắt, không mở lại

ô Cài đặt nhanh ──▶ AodTileService ──▶ bật/tắt AodService (bị chặn ──▶ mở app để khởi động)

thông báo ──▶ AodNotificationListener ──▶ NotificationStateManager ──▶ icon, viền sáng, nhạc trên đồng hồ
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
| `presentation/aod/AodTileService.kt` | Ô Cài đặt nhanh bật/tắt đồng hồ; mở app khi hệ thống không cho khởi động service |
| `presentation/screens/aod/AodViewModel.kt` | Logic của AOD: giờ hiện tại và chống burn-in (cập nhật mỗi phút), pin, ẩn dòng gợi ý sau 3 giây, hết giờ, trong túi, quy tắc hiện, đóng khi có cuộc gọi hoặc báo thức, chạm 2 lần |
| `presentation/screens/aod/AodContent.kt` · `AodScreen.kt` · `state/` | Giao diện đồng hồ (4 mặt, ngày, icon thông báo, pin, nhạc, gợi ý, viền sáng, ảnh nền, bố cục ngang, dịch vị trí) và phần nối với ViewModel |
| `presentation/aod/AodNotificationListener.kt` | Nhận thông báo từ hệ thống (khi đã cấp "Truy cập thông báo") và chuyển cho `NotificationStateManager` |
| `presentation/screens/main/` | Màn hình cài đặt (MVI): quyền, tùy chọn, quy tắc hiện, ngôn ngữ, xem thử; hỏi quyền thông báo ở lần mở đầu tiên. Mỗi mục một file `MainXxxSection.kt` |
| `presentation/screens/language/` | Màn chọn ngôn ngữ (MVI): lần mở đầu tiên và từ màn hình cài đặt |
| `presentation/model/` | `AodOptionsUiModel`, `AodRulesUiModel` (quy tắc hiện, dùng chung cho service và màn AOD), `AodScheduleUiModel`, `ChargingRuleValue`, `ScheduleTimeValue`, `LanguageUiModel`, `BatteryUiModel`, `PermissionValue`, `PermissionStatusValue`, `PermissionUiModel`, `WakeResultValue` |
| `presentation/components/AppLanguageProvider.kt` | Áp ngôn ngữ đã chọn cho `MainActivity` và `AodActivity` |
| `presentation/components/SettingsRows.kt` | Các dòng cài đặt dùng chung: tiêu đề mục, công tắc, radio, dòng giá trị, thanh trượt |
| `presentation/MainActivity.kt` | Activity chính: consent, ngôn ngữ, theme, khởi động service |
| `data/device/screen/ScreenStateManager.kt` | Sự kiện tắt màn hình / mở khóa, màn hình có đang sáng, có khóa bảo mật, wake lock bật lại màn hình |
| `data/device/battery/BatteryStateManager.kt` | Phần trăm pin, trạng thái sạc, có đang cắm nguồn |
| `data/device/audio/AudioStateManager.kt` | Đang có chuông, cuộc gọi hoặc báo thức |
| `data/device/proximity/ProximityManager.kt` | Cảm biến tiệm cận |
| `data/device/notification/NotificationStateManager.kt` | Lọc thông báo hiện được trên đồng hồ, nạp sẵn icon, báo thông báo mới, giữ phiên nhạc của thông báo nhạc mới nhất |
| `data/device/media/MediaStateManager.kt` | Bài đang phát và các nút điều khiển nhạc |
| `data/device/permission/PermissionManager.kt` | Đọc quyền overlay, thông báo, truy cập thông báo, và quyền riêng của Xiaomi |
| `data/local/datastore/DataStoreManager.kt` | Lưu tùy chọn AOD cùng các prefs của base |
| `data/local/background/BackgroundImageManager.kt` | Lưu, đọc, xóa ảnh nền (bản sao đã thu nhỏ) |
| `utils/ContextExt.kt` | `registerSystemReceiver`; mở trang cài đặt: `openSettingsPage` (ROM không có trang đó thì mở Thông tin ứng dụng), `openOverlaySettings`, `openNotificationSettings`, `openNotificationListenerSettings`, `openMiuiPermissionSettings`; `packageUri` |
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
| `MiuiPerm.kt` | `PermissionManager` (đọc quyền) + `utils/ContextExt.kt` (`openMiuiPermissionSettings`, mở trang "Quyền khác") |
| `SystemPages.kt` | `utils/ContextExt.kt` (`registerSystemReceiver`, `openSettingsPage`, `openOverlaySettings`, `openNotificationSettings`, `packageUri`) |
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
- **Không tự tắt màn hình:** app thường không có cách tắt màn hình, việc đó cần dịch vụ trợ năng hoặc quyền quản trị thiết bị. Khi cần kết thúc (hết giờ, trong túi, sai quy tắc hiện), AOD chuyển sang đen hoàn toàn ở độ sáng thấp nhất và bỏ `FLAG_KEEP_SCREEN_ON`. Sau đó thời gian chờ của máy sẽ tắt màn hình. Lần tắt đó được đánh dấu là chủ ý, nên AOD không mở lại. Trong túi, cảm biến tiệm cận đang bị che thì service cũng không bật lại màn hình khóa.
- **Độ sáng:** chỉnh bằng `screenBrightness` của cửa sổ AOD (1–100%, hoặc theo hệ thống khi tắt độ sáng riêng), không cần quyền "Sửa đổi cài đặt hệ thống" và không đổi độ sáng của máy. Độ sáng được đặt trong `onCreate`, trước khi cửa sổ hiện, để khung đầu tiên đã đúng mức. Pin cũng được đọc đồng bộ lúc mở để khung đầu tiên có ngay dòng pin.
- **Thông báo trên đồng hồ:** app chỉ nhận được thông báo qua `NotificationListenerService` do hệ thống bind, nên cần quyền "Truy cập thông báo". Thông báo được lọc giống màn hình chờ của hệ thống, và chỉ hiện icon vì đồng hồ nằm trên màn hình khóa. Icon được nạp sẵn ở luồng nền mỗi khi có thông báo, nên khung đầu tiên của đồng hồ đã có icon. Nhạc lấy từ phiên nhạc trong thông báo nhạc mới nhất, giống trình phát trên màn hình khóa của hệ thống; không hiện ảnh bìa vì sáng và dễ burn-in. Viền sáng vẽ bằng gradient vì làm mờ (`blur`) cần Android 12.
- **Giao diện đồng hồ:** đọc một lần lúc mở AOD, để khung đầu tiên đã đúng mặt, font, màu, cỡ. Hướng ngang được đặt trước khi cửa sổ hiện. Bố cục ngang hay dọc theo kích thước thật của màn hình, nên tự xoay của hệ thống cũng hiển thị đúng. Ảnh nền là bản sao đã thu nhỏ, vì Photo Picker chỉ cho đọc ảnh tạm thời; nó được giải mã ở luồng nền rồi hiện dần, không làm chậm lúc đồng hồ hiện. Font là các họ font có sẵn của hệ thống, không thêm file font.
- **Quy tắc hiện ở một chỗ:** nguồn điện, khung giờ và ngưỡng pin nằm trong `AodRulesUiModel.allows()`. Service gọi hàm này lúc màn hình tắt để quyết định có mở AOD không; `AodViewModel` gọi lại khi pin, nguồn cắm đổi và mỗi phút để chuyển AOD đang hiện sang đen. Không đọc được trạng thái pin hay nguồn cắm thì không chặn AOD.
- **Ô Cài đặt nhanh mở app trên Android 15:** từ Android 15, app có quyền "Hiển thị trên ứng dụng khác" chỉ được khởi động foreground service từ nền khi đang có cửa sổ nổi hiển thị, và ô Cài đặt nhanh không được miễn. Khi `startForegroundService` bị chặn, ô mở app (`startActivityAndCollapse`, mở khóa trước nếu đang khóa) và `MainActivity` khởi động service từ tiền cảnh. Tắt AOD từ ô thì chỉ cần dừng service, không cần mở app.
- **Cuộc gọi và báo thức:** màn hình cuộc gọi, báo thức là Activity mở sau nên tự nằm trên AOD, và `noHistory` đóng AOD khi đó. App còn theo dõi `AudioManager` (`AudioStateManager`) để đóng sớm hơn, và không mở AOD khi đang có chuông, cuộc gọi hay báo thức. Không cần quyền `READ_PHONE_STATE`.
- **Kiến trúc theo base:** tùy chọn là prefs nên nằm thẳng trong `DataStoreManager` (base cấm bọc manager bằng Repository/UseCase chỉ để chuyển tiếp). Mọi tín hiệu thiết bị đi qua manager trong `data/device/`; các manager này chỉ đăng ký receiver hoặc cảm biến khi có người dùng tới, để service chạy nền không nhận `BATTERY_CHANGED` liên tục. Logic của màn đồng hồ nằm trong `AodViewModel`; mọi thao tác với cửa sổ nằm ở `AodActivity`, vì ViewModel không được giữ `Context`. Chi tiết và các ngoại lệ ở [CLAUDE.md › 20](CLAUDE.md#20-aod-core).

## Kiểm tra trên máy thật

1. Mở app lần đầu (gỡ app hoặc xóa dữ liệu trước): Splash có hiện form consent (bản debug giả lập vùng EEA) và quảng cáo test không, rồi tới màn chọn ngôn ngữ, đã chọn sẵn ngôn ngữ của máy, không có nút back. Chọn "Tiếng Việt", bấm "Xong": vào màn hình chính bằng tiếng Việt. Đóng hẳn app rồi mở lại: đi thẳng vào màn hình chính.
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
14. Độ sáng: kéo thanh độ sáng lên 50% rồi bấm "Xem thử", đồng hồ sáng hơn rõ. Tắt "Dùng độ sáng riêng": đồng hồ theo độ sáng của máy.
15. Nguồn điện "Chỉ khi đang cắm sạc": rút sạc rồi khóa máy, đồng hồ không hiện; cắm sạc rồi khóa máy, đồng hồ hiện; rút sạc khi đồng hồ đang hiện, đồng hồ chuyển sang đen. Làm ngược lại với "Chỉ khi dùng pin". Máy có giới hạn sạc 80%: khi đã dừng ở 80% vẫn phải tính là đang cắm.
16. Khung giờ: đặt Bắt đầu và Kết thúc quanh giờ hiện tại (ví dụ đang 10:05, đặt 09:00–10:07). Khóa máy trước 10:07 thì đồng hồ hiện, tới 10:07 thì chuyển sang đen; khóa máy sau 10:07 thì đồng hồ không hiện. Thử thêm khung qua nửa đêm (Bắt đầu sau Kết thúc). Tắt "Chỉ hiện trong khung giờ": hiện cả ngày.
17. Ô Cài đặt nhanh: thêm ô "Custom AOD", chạm để tắt rồi bật. Công tắc trong app đổi theo. Trên Android 15 trở lên, bật từ ô thì app mở lên (đang khóa máy thì hỏi mở khóa trước), sau đó khóa máy thì đồng hồ hiện. Trên Android 12–14 đã cấp quyền overlay thì bật được mà không mở app.
18. Đổi ngôn ngữ ở "Ứng dụng › Ngôn ngữ": nút "Xong" chỉ bật khi chọn khác ngôn ngữ đang dùng; đổi xong, màn hình chính, đồng hồ và thông báo của service đổi theo.
19. Cấp "Truy cập thông báo" từ dòng trong mục Quyền: trang hệ thống mở đúng công tắc của app; quay về app, dòng này chuyển sang "Đã cấp" và dòng nhắc trong mục "Thông báo trên đồng hồ" biến mất.
20. Icon thông báo: tự gửi tin nhắn tới máy (vd Zalo, Messenger), khóa máy: icon của app hiện dưới ngày. Có thông báo từ hơn 5 app thì hiện "+N". Xóa thông báo rồi khóa lại: icon mất. Thông báo im lặng, thông báo thường trực (vd đang tải xuống) và thông báo của chính app không hiện.
21. Viền sáng: khi đồng hồ đang hiện, gửi tin nhắn tới máy: 4 cạnh sáng nhấp nháy khoảng 4 giây. Bật Không làm phiền rồi gửi lại: không sáng. Đồng hồ đã chuyển sang đen thì không sáng.
22. Nhạc: phát nhạc (Spotify, YouTube Music…) rồi khóa máy: hiện tên bài, nghệ sĩ và 3 nút; bấm Tạm dừng/Phát và Bài tiếp theo phải điều khiển đúng app. Che cảm biến tiệm cận rồi bấm: không có tác dụng.
23. Tắt từng công tắc trong "Thông báo trên đồng hồ": lần AOD sau không còn phần tương ứng. Thu hồi "Truy cập thông báo": icon và nhạc biến mất, đồng hồ vẫn chạy.
24. Mặt đồng hồ: thử lần lượt 4 mặt bằng "Xem thử", rồi khóa máy với từng mặt. Mặt kim phải chỉ đúng giờ và nhích mỗi phút; mặt số theo đúng 12/24 giờ của máy.
25. Font, màu, cỡ: đổi từng tùy chọn rồi bấm "Xem thử". Ở cỡ 150%, đồng hồ, ngày, pin và nhạc không bị che hay tràn màn hình, cả khi dịch vị trí chống burn-in.
26. Ảnh nền: chọn một ảnh dọc và một ảnh ngang lớn (vd ảnh chụp 50 MP): dòng "Ảnh nền" chuyển sang "Đang lưu…" rồi "Đổi ảnh". Khóa máy: ảnh hiện dần sau đồng hồ, mờ, không méo. Đóng app, mở lại: ảnh nền vẫn còn. "Bỏ ảnh nền": lần AOD sau nền đen.
27. Luôn xoay ngang: bật, dựng máy nằm ngang rồi khóa máy: đồng hồ hiện ngang, đồng hồ bên trái, thông tin bên phải; lật ngược máy thì đồng hồ xoay theo. Mở khóa: màn hình khóa và app trở về dọc như cũ.
28. Tự xoay của hệ thống (tắt "Luôn xoay ngang", bật tự xoay của máy): xoay máy khi đồng hồ đang hiện, bố cục chuyển ngang/dọc đúng.

Lưu ý khi test trên máy ảo hoặc ngay sau khi vừa dùng app: trong 10 giây sau khi app vừa mở hoặc đóng một Activity, Android cho phép mở Activity từ nền mà không cần quyền gì (log ghi `BAL_ALLOW_GRACE_PERIOD`). Vì vậy đồng hồ có thể hiện dù chưa bật "Hiển thị trên ứng dụng khác". Muốn test đúng thì về màn hình chính, đợi hơn 10 giây rồi mới khóa máy.

Xem log khi có vấn đề:

```
adb logcat -s AodService AodViewModel AodTileService MainViewModel ActivityTaskManager PowerManagerService
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
- **Ô Cài đặt nhanh trên Android 15 trở lên** phải mở app mỗi lần bật AOD (xem "Các quyết định thiết kế"), đang khóa máy thì phải mở khóa trước.
- **Thông báo bật lên (heads-up)** của hệ thống vẫn có thể hiện đè lên đồng hồ khi có thông báo mới; app không chặn được.
- **Icon thông báo vẫn hiện khi đã tắt "Hiện thông báo trên màn hình khóa" của hệ thống** (chỉ icon, không nội dung). Thông báo hoặc kênh đặt ẩn hẳn trên màn hình khóa thì không hiện; riêng tùy chỉnh theo kênh chỉ đọc được từ Android 12. Muốn ẩn hết thì tắt "Hiện biểu tượng thông báo".
- **Ảnh nền đứng yên** (chỉ đồng hồ dịch vị trí), nên để lâu dễ burn-in và tốn pin hơn nền đen; nên chọn ảnh tối.
- **Xoay ngang** có thể thấy màn hình xoay lúc đồng hồ hiện và lúc mở khóa. Từ Android 16, màn hình lớn (sw ≥ 600dp) bỏ qua yêu cầu xoay này.
- **Font tùy ROM:** "Viết tay" hay "Có chân" là họ font chung, mỗi hãng gán một font khác nhau; máy không có font mỏng thì dùng độ đậm gần nhất.
- **Trên Xiaomi, phần thông báo cũng cần app đang chạy:** app bị đóng mà không có "Tự khởi chạy" thì hệ thống có thể không bind lại `AodNotificationListener` cho tới khi mở app.
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
| Android 15 (35) | App có quyền "Hiển thị trên ứng dụng khác" chỉ được khởi động foreground service từ nền khi đang có overlay hiển thị | Service chỉ được khởi động khi app ở tiền cảnh, hoặc từ `BOOT_COMPLETED` và `MY_PACKAGE_REPLACED` là hai trường hợp được miễn. Ô Cài đặt nhanh không được miễn: `AodService.start()` bắt lỗi và ô mở app |
| Android 15 (35) | `BOOT_COMPLETED` không được khởi động một số loại foreground service | `specialUse` không thuộc danh sách bị chặn |
| Android 16 (36) | Bỏ tùy chọn tắt edge-to-edge; bật sẵn predictive back | App không override `onBackPressed`. Back trong app do Navigation 3 xử lý |
| Android 16–17 | Màn hình lớn (sw ≥ 600dp) bỏ qua khóa hướng xoay và giới hạn kích thước | `MainActivity` khóa dọc (theo base), nên trên màn hình lớn hệ thống bỏ qua khóa này. `AodActivity` chỉ khóa ngang khi bật "Luôn xoay ngang", và màn hình lớn cũng bỏ qua khóa đó |
| Android 17 (37) | Siết mở Activity từ nền qua `PendingIntent` và `IntentSender` (`MODE_BACKGROUND_ACTIVITY_START_ALLOWED`) | App không dùng đường này. AOD được mở trực tiếp nhờ quyền "Hiển thị trên ứng dụng khác" |
| Android 17 (37) | Siết âm thanh khi chạy nền: phát, xin audio focus, đổi âm lượng | `AudioStateManager` chỉ đọc trạng thái (`getMode`, `getActivePlaybackConfigurations`) và đăng ký listener |
| Android 17 (37) | Không sửa được field `static final` bằng reflection | `PermissionManager` chỉ gọi method ẩn `checkOpNoThrow`. Method này được đánh dấu `@UnsupportedAppUsage` nhưng không giới hạn `targetSdk` |

Nếu đưa lên Google Play:

- Khai báo loại foreground service `specialUse` trong Play Console, gồm mô tả chức năng, ảnh hưởng nếu service bị hoãn hoặc bị ngắt, và video minh họa.
- `targetSdk` 37 đã đạt yêu cầu tối thiểu 36 mà Play áp dụng từ 31/08/2026.
- Có quảng cáo: khai báo "Chứa quảng cáo" và mục Data safety cho mã quảng cáo (`AD_ID`) mà Google Mobile Ads dùng.
- `NotificationListenerService` đọc được mọi thông báo: mô tả trên Play nên nói rõ chức năng dùng nó (icon, viền sáng, nhạc trên đồng hồ), và chính sách quyền riêng tư nên ghi rằng dữ liệu thông báo chỉ được xử lý trên máy, không gửi đi đâu.

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
| Onboarding (nếu cần) | Sau màn chọn ngôn ngữ ở lần mở đầu tiên, CLAUDE.md › 5 |
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

- Cử chỉ vuốt, phím âm lượng, đèn pin, tự giảm sáng theo cảm biến ánh sáng.
- Tùy chọn hiện nội dung thông báo mới nhất (tên app, tiêu đề) cho ai không ngại hiện trên màn hình khóa.
- Nhận diện cuộc gọi chắc chắn hơn bằng `READ_PHONE_STATE` và `TelephonyCallback`, nếu chấp nhận thêm một quyền.

Đã làm ở giai đoạn 1: quy tắc nguồn điện, khung giờ, mức độ sáng, ô Cài đặt nhanh, màn chọn ngôn ngữ. Giai đoạn 2: icon thông báo, viền sáng, điều khiển nhạc. Giai đoạn 3: mặt đồng hồ, font, màu, cỡ, ảnh nền, xoay ngang làm đồng hồ đêm.
