# Changelog

Các thay đổi đáng chú ý của Custom AOD. Định dạng dựa theo [Keep a Changelog](https://keepachangelog.com/vi/1.1.0/).

Project chưa phát hành (`versionName` vẫn là `v1.0.0` của base), nên các thay đổi được gom theo ngày. Lịch sử của demo FakeAOD, nơi lõi AOD được làm ra trước khi chuyển sang base, giữ ở cuối file. Mã nguồn demo FakeAOD không nằm trong repo này.

## [Chưa phát hành] – 2026-10-09 – UI đọc thẳng AodsColors / AodsTypography / AodsShapes

Commit `e743565` cho UI đọc theme qua `MaterialTheme.colorScheme / .typography / .shapes`; bản này đổi lại cho UI đọc thẳng các object, tránh đường vòng. Build được (`compileDebugKotlin`), lint 0 lỗi (không có cảnh báo mới), 27 lệnh grep của CLAUDE.md ra 0 dòng, chưa chạy trên máy.

### Thay đổi

- UI (22 file) đọc thẳng `AodsColors.Mint`, `AodsTypography.BodyLarge`, `AodsShapes.RoundedCornerShape16dp`; màu có alpha dùng lại biến thể đặt tên sẵn (`MintAlpha12`, `RedAlpha12`, `WhiteAlpha30`, `BlackAlpha50`). Đọc được cả trong lambda vẽ / `onClick` nên bỏ các biến gán tạm.
- `AodsTypography` / `AodsShapes` có lại các `TextStyle` / `RoundedCornerShape<N>dp`, cộng `Material` cho cầu nối. `AodsTheme { }` chỉ bọc `MaterialTheme` cho component Material 3; bỏ `inverseSurface` / `scrim` thêm riêng cho UI.
- Bỏ `AodsAodTheme`: màn AOD lại không bọc theme (`AodActivity`, preview giữ nguyên như trước `e743565`). Chữ gợi ý trong bảng vẽ trở lại Grey6E như ban đầu.
- Bottom sheet quyền ở màn chính dùng cùng giao diện màn quyền: tiêu đề / mô tả của onboarding (`AodsOnboardingHeader`), các thẻ quyền (bấm thẻ là mở cài đặt của quyền đó) và một nút Để sau rộng hết, cao 56dp.
- `LaunchedEffect` không gọi hàm suspend đổi sang `SideEffect` có key (Compose runtime 1.12): `requestFocus()` ở `AodContent`, `onDarkChange` / `onDimChange` ở `AodScreen`, nạp quảng cáo / điều hướng ở `SplashScreen`. Giữ `LaunchedEffect` ở `Modifiers.onClick` (`collectLatest`, `animateTo`) và `MainPermissionSheet` (`sheetState.hide()`). CLAUDE.md mục 14, agent `compose-implementer`, skill `compose-optimizer` ghi quy tắc này.
- CLAUDE.md (mục 14, 18), agent `compose-implementer`, skill `design-tokens` / `compose-optimizer`, README cập nhật theo. Lệnh grep: cấm `MaterialTheme.*` ngoài `theme/`, cấm `.copy(` trên `AodsColors` ngoài `theme/`, cấm `AodsTheme.xxx`; bỏ lệnh cấm gọi thẳng các object ngoài `theme/`.

## [Chưa phát hành] – 2026-10-07 – Quyền bắt buộc, chỉnh UI onboarding, theme theo pattern Lich-Viet-Loc-Phat, script icon

Siết lại bộ quyền bắt buộc theo thử nghiệm trên máy Xiaomi, chỉnh giao diện splash / màn ngôn ngữ / màn quyền, theme đổi sang pattern phẳng của Lich-Viet-Loc-Phat (bỏ tầng token), đưa script sinh icon / Lottie vào repo. Không thêm quyền Manifest, không thêm thư viện. Bản này build được (`compileDebugKotlin`), lint 0 lỗi (không có cảnh báo mới), 27 lệnh grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy.

### Thêm

- `tools/brand/`: script Python sinh icon app (`generate_launcher_icon.py`) và Lottie của icon (`generate_edge_light_lottie.py`) từ cùng một bộ hình học (`brand_geometry.py`); đổi màu viền, tốc độ, độ dài vệt sáng… chỉ cần sửa thông số rồi chạy lại. File sinh ra giống hệt bản đang dùng (icon chỉ thêm dòng ghi chú "sinh bằng script").
- `PermissionValue` có đủ mọi quyền app dùng: thêm lịch và vị trí (không bắt buộc). Danh sách quyền ở màn chính hiện cả hai; bấm thì xin quyền (chỉ cấp, không tự bật "Sự kiện hôm nay" / "Thời tiết"), bị từ chối thì mở Thông tin ứng dụng.
- Màn quyền có nút back trên top bar để quay về màn ngôn ngữ.
- `AodsOnboardingTopBar`: top bar của onboarding, chỉ báo bước nằm giữa top bar (không cuộn theo nội dung nữa), kèm nút back khi cần.
- `NavBackStack.navigateBack()` (`utils/NavExt.kt`): quay lại nhưng không bao giờ pop màn cuối cùng (back stack rỗng thì NavDisplay crash), bấm back liên tiếp cũng an toàn. Dùng cho `onBack` của `AppNavDisplay` và nút back của Language / Permission; CLAUDE.md cấm gọi `backStack.removeLastOrNull()` thẳng (thêm lệnh grep thứ 25).

### Thay đổi

- Quyền bắt buộc: thêm Thông báo (thông báo của service giữ đồng hồ chạy nền) và, trên Xiaomi, "Cửa sổ bật lên khi chạy nền" (HyperOS chặn mở AOD từ nền khi thiếu). Màn quyền xin thông báo bằng hộp thoại hệ thống, bị từ chối thì mở trang cài đặt thông báo.
- Bỏ việc tự hỏi quyền thông báo khi vào màn chính (và cờ `is_notifications_asked`).
- Onboarding: bấm Tiếp tục ở Language lần đầu là xong onboarding; thoát ở màn quyền thì lần sau vào thẳng màn chính, thiếu quyền thì màn chính nhắc bằng bottom sheet.
- Bottom sheet quyền ở màn chính hiện mỗi khi thiếu quyền bắt buộc (không còn phụ thuộc công tắc AOD) và hiện lại khi bật AOD hay áp giao diện cho đồng hồ (mặt, font, màu, cỡ, xoay ngang, ảnh nền), kể cả khi đã đóng trước đó.
- Splash: Lottie và chữ chung một cột ở giữa màn hình, chữ mở dần ra làm cả cụm dồn lên giữa. Lottie thu nhỏ từ 288dp còn 200dp, bỏ placeholder.
- Tiêu đề màn ngôn ngữ và màn quyền căn giữa; chỉ báo bước chuyển từ nội dung lên top bar.
- Thẻ quyền gọn hơn (padding 12dp, icon 40dp, mô tả chữ nhỏ), nội dung căn giữa theo chiều dọc. Bên phải trong thẻ là nút tròn filled màu primary, cùng cỡ với icon quyền: mũi tên khi chưa cấp, dấu tích khi đã cấp. Quyền không kiểm tra được vẫn có dòng nhắc trong thẻ, bấm thẻ để mở Cài đặt (bỏ nút "Mở cài đặt" riêng).
- Thẻ quyền bấm được cả thẻ khi quyền chưa cấp (trước chỉ bấm được nút); nút tròn bên phải giờ chỉ để báo trạng thái.
- Bottom sheet quyền: tiêu đề và mô tả căn giữa; nút "Để sau" rộng hết chiều ngang, chữ căn giữa, cao bằng nút chính của onboarding (chữ vốn cùng cỡ `labelLarge` với nút đó).
- Theme theo pattern của Lich-Viet-Loc-Phat cho đơn giản: `presentation/theme/` phẳng, mỗi file một `internal object` cùng tên — `AodsColors` (màu, biến thể alpha `MintAlpha12`, `RedAlpha12`, `WhiteAlpha30`, `BlackAlpha50`), `AodsTypography` (font Inter, các `TextStyle`, `Material` cho cầu nối), `AodsShapes` (`RoundedCornerShape8dp` / `12dp` / `16dp`). UI đọc qua `AodsTheme.colors / .typography / .shapes` (object trỏ thẳng tới các object trên, không đổi tên, không `CompositionLocal`), không gọi thẳng các object ngoài package `theme`; hàm `AodsTheme { }` chỉ bọc `MaterialTheme`. Khoảng cách, cỡ icon, viền, kích thước, `sp`, thời lượng, alpha viết số thẳng (`16.dp`, `14.sp`, `tween(durationMillis = 600)`), hình tròn dùng `CircleShape`. Bỏ thư mục `tokens/` cùng mọi tầng token: semantic (`AodsColorTokens`, `AodsSpacingTokens`…), token riêng của component / màn (`AodsOnboardingTokens`, `AodsClockTokens`…), `LocalAods…`, và các object `AodsPrimitive*`. Giao diện giữ nguyên. CLAUDE.md, agent `compose-implementer`, skill `design-tokens` / `compose-optimizer`, README cập nhật theo; lệnh grep: thư mục `theme/` chỉ có `AodsTheme` / `AodsColors` / `AodsTypography` / `AodsShapes` / `AodsBrush`, không gọi thẳng `AodsColors` / `AodsTypography` / `AodsShapes` ngoài `theme/`, không `AodsPrimitive` / tầng token, không `.copy(` trên màu theme trong UI; bỏ 2 lệnh cấm số `dp` / `sp` và `durationMillis` viết thẳng.

### Ghi chú

- "Tắt màn hình không thấy AOD" lúc thử là do đang ngoài khung giờ hiện (mặc định 07:00–23:00, mục Quy tắc hiện), không phải lỗi code.

## [Chưa phát hành] – 2026-10-06 – Onboarding mới, font Inter, icon app

Làm lại giao diện Splash và Language theo hướng skill ui-ux-pro-max gợi ý (style Dark Mode OLED, giữ màu mint), thêm màn Permission sau Language ở lần đầu mở app. Không thêm quyền, không thêm thư viện. Bản này build được (`compileDebugKotlin`), lint 0 lỗi (không có cảnh báo mới), 24 lệnh grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy.

### Thêm

- Màn Permission (`screens/permission/`, bước 2/2 của onboarding): chỉ các quyền bắt buộc để AOD mặc định chạy — "Hiển thị trên ứng dụng khác", thêm "Hiển thị trên màn hình khóa" trên máy Xiaomi. Nút Bắt đầu chỉ bật khi đủ quyền; quyền mà máy không cho kiểm tra (op của Xiaomi) thì không chặn, chỉ nhắc mở Cài đặt. Quay lại từ Cài đặt thì tự đọc lại quyền.
- Các lần mở app sau: AOD đang bật mà thiếu quyền bắt buộc thì màn chính hiện bottom sheet để cấp quyền. Đóng sheet thì còn dòng cảnh báo ở đầu màn chính để mở lại; bật lại công tắc AOD cũng mở lại sheet; đủ quyền thì cả hai tự ẩn.
- Component dùng chung `AodsOnboardingHeader` / `AodsOnboardingFooter` (chỉ báo bước, một nút chính, dòng nhắc khi nút bị khoá) và `AodsPermissionCard`; token `AodsOnboardingTokens`; thứ tự bước `OnboardingStepValue`.
- Font Inter (bản variable của Google Fonts, giấy phép OFL, khoảng 856 KB) cho toàn bộ giao diện app, kể cả component Material 3. Màn AOD giữ font đồng hồ user chọn.
- Icon app mới: điện thoại tắt màn hình có viền sáng gradient mint → xanh → tím, đồng hồ 7 đoạn xếp chồng và ba chấm thông báo; thêm lớp đơn sắc cho icon theo chủ đề (Android 13+). Small icon của thông báo vẫn là `ic_aod`.
- Animation Lottie `res/raw/lottie_device_edge_light.json` (37 KB, lặp 2,4 giây): ánh sáng kiểu LED lướt theo viền máy — nằm sát trên viền, độ rộng đều, cùng dải màu, độ sáng mịn với đuôi dài và mép trước tắt dần (44 lớp mỏng chồng nhau, đầu nét phẳng) nên trông như chính viền đang sáng lên; aura chuyển màu thở phía sau; cùng khung 108 với icon nên khung đầu trùng icon tĩnh. `AodsLottie` thêm tham số `placeholder` để vẽ thứ khác trong lúc file đang nạp.

### Thay đổi

- Splash: icon động (Lottie viền sáng) 288dp đúng tâm cửa sổ, khung đầu trùng splash hệ thống; tên app và câu giới thiệu hiện dần bên dưới; giữ thanh tải và dòng "có thể có quảng cáo".
- Language: mỗi ngôn ngữ là một thẻ chọn lớn (cờ, viền, radio, đổi màu có animation); lần đầu mở app có chỉ báo "Bước 1/2" và nút Tiếp tục.
- Cờ "lần đầu mở app" chỉ xoá khi đã đủ quyền bắt buộc (ở Language nếu máy đã có sẵn quyền, không thì ở màn Permission), nên thoát giữa chừng thì lần sau đi lại onboarding.
- `PermissionValue` thêm tên ngắn và icon; danh sách quyền dùng chung `PermissionManager.aodPermissions()` / `requiredAodPermissions()`.
- CLAUDE.md: luồng onboarding (mục 5), font Inter và splash (mục 14), component mới (mục 15), icon app, quy tắc tự làm thay vì tạo agent (mục 21).

## [Chưa phát hành] – 2026-10-06 – Skill ui-ux-pro-max

Chuẩn bị cho đợt làm lại UI (style, theme). Không đổi code app.

### Thêm

- `.claude/skills/ui-ux-pro-max/` từ nextlevelbuilder/ui-ux-pro-max-skill (MIT, bản 2.13.0): tra style, bảng màu, cặp font, luật UX / accessibility và hướng dẫn riêng cho Jetpack Compose bằng script Python (chỉ thư viện chuẩn). Đường dẫn script sửa cho bản cài trong project, bỏ test của skill, thêm mục "Project notes (Custom-AOD)". `CLAUDE.md` mục 21.

## [Chưa phát hành] – 2026-10-05 – Bộ skill Claude, design system Aods

Đưa bộ skill của kotlin-accelerator-ai vào project và refactor UI theo chuẩn token của skill. Giá trị hình ảnh giữ nguyên nên giao diện không đổi. Không thêm quyền và thư viện. Bản này build được (`assembleDebug`), lint 0 lỗi (không có cảnh báo mới), 24 lệnh grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy.

### Thêm

- `.claude/`: agent `compose-implementer` và 8 skill: `idea-pipeline` → `prd-pipeline` → `design-spec` → `design-tokens` (từ ý tưởng đến token rồi dựng màn), `compose-optimizer`, `kotlin-review`, `unit-testing`, `gradle-module`. Bỏ DRE (dre-kt), Supabase và prefix `kta-` của bản gốc. Đã sửa theo project: MVI + Navigation 3, 1 module `:app`, theme luôn tối, CLAUDE.md là chuẩn, mỗi skill có mục "Project notes (Custom-AOD)". `CLAUDE.md` mục 21.
- Design system `Aods` trong `presentation/theme/`: token Primitive (`AodsPrimitiveColors`, `Spacing`, `IconSize`, `Shape`, `Border`, `Typography`, `Motion`, `Opacity`, `Elevation`) → Semantic (`AodsColorTokens`, `AodsSpacingTokens`, `AodsShapeTokens`, `AodsTypographyTokens`, `AodsMotionTokens`, `AodsOpacityTokens`, `AodsElevationTokens`) → component/domain (`AodsSettingsRowTokens`, `AodsPickerTokens`, `AodsDrawingPadTokens`, `AodsClockTokens`, `AodsSplashTokens`, `AodsAdTokens`). `AodsTheme { }` cấp token và nối sang Material 3; đọc bằng `AodsTheme.colors/.spacing/…`.
- `DataStoreManager.observeAodOptions/NotificationOptions/Appearance/Interaction/Rules/Extras()` trong các file UiModel, cạnh `currentAodXxx()`.
- 3 lệnh grep mới ở `CLAUDE.md` mục 18: số `dp`/`sp`, `durationMillis` viết thẳng, `MaterialTheme.*`/`CircleShape` ngoài `presentation/theme/`.

### Thay đổi

- Component dùng chung mang prefix `Aods`: `SettingsRows.kt` → `AodsSettingsRows.kt` (`AodsSectionHeader`, `AodsSectionLabel`, `AodsSwitchRow`, `AodsRadioRow`, `AodsValueRow`, `AodsSliderRow`), `AppLottie` → `AodsLottie`, `NoInternetDialog` → `AodsNoInternetDialog`, `AppTheme` → `AodsTheme`.
- Mọi màn (Main, Language, Splash, AOD), `Modifiers.kt`, composable quảng cáo và `ClockColorValue` đọc token thay cho `MaterialTheme.*`, màu trong `Color.kt` và số viết thẳng.
- `AodContent.kt` (934 dòng) tách thành `AodContent`, `AodClockFace`, `AodDetails`, `AodExtras`, `AodNotifications`, `AodMediaControls`, `AodBackdrop`, `AodTimeFormat`; `MainAppearanceSection` tách picker sang `MainAppearancePickers.kt`; `MainExtrasSection` tách hộp thoại sang `MainExtrasDialogs.kt`; `MainViewModel` bỏ 5 hàm dựng luồng cài đặt (dùng `observeAodXxx()`).
- `.gitignore`: `.claude/settings.local.json`, `__pycache__/`.

### Xóa

- `presentation/theme/Color.kt`, `Theme.kt`, `Type.kt` (thay bằng token).

## [Chưa phát hành] – 2026-10-05 – Nội dung thông báo, ảnh nền AMOLED có sẵn

Hai khoảng trống còn lại so với Always On AMOLED. Không thêm quyền và thư viện. Bản này build được (`assembleDebug`), lint 0 lỗi (không có cảnh báo mới), grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy (các bước kiểm tra mới: README › Kiểm tra trên máy thật, bước 40–41).

### Thêm

- "Hiện nội dung thông báo mới nhất" (`is_aod_notification_content_enabled`, mặc định tắt): tiêu đề và nội dung của thông báo mới nhất dưới các icon. `NotificationStateManager` che nội dung đúng như màn hình khóa của hệ thống: đọc "hiện thông báo trên màn hình khóa", "hiện nội dung nhạy cảm", khóa bảo mật, chính sách quản lý thiết bị, `visibility` của thông báo và tùy chỉnh theo kênh; bị che thì dùng `publicVersion`, không có thì AOD ghi "Nội dung đã ẩn". `ActiveNotification` có thêm `content` (`NotificationContent`).
- 6 ảnh nền AMOLED có sẵn (`WallpaperValue`, key `aod_wallpaper`): Sao đêm, Cực quang, Nhật thực, Sóng, Tinh vân, Chân trời. Vector `res/drawable/img_wallpaper_*.xml`, AOD vẽ thẳng (không lưu file), mờ 50%, bố cục ngang giữ phần có chi tiết. Màn cài đặt có hàng ảnh xem trước để chọn.
- `CLAUDE.md`: bất biến 38 (nội dung thông báo), 39 (ảnh nền có sẵn), resource ảnh nền (mục 16), cách thêm ảnh nền (mục 20.6).

### Thay đổi

- Mục ảnh nền của màn cài đặt: nhãn "Ảnh nền", hàng ảnh có sẵn, dòng "Ảnh từ máy", "Bỏ ảnh nền" bỏ cả hai. Mỗi lúc chỉ một ảnh nền: chọn ảnh có sẵn thì xóa ảnh từ máy, và ngược lại.
- `AodViewModel` theo dõi thông báo khi bật icon hoặc nội dung; `toAodNotificationsUiModel(options)` nhận tùy chọn thông báo.

## [Chưa phát hành] – 2026-10-04 – Giai đoạn 5: thông tin thêm trên đồng hồ

Thời tiết, sự kiện hôm nay, ghi nhớ, hình vẽ nhanh. Thêm 2 quyền hỏi lúc chạy, chỉ hỏi khi bật tùy chọn tương ứng (`READ_CALENDAR`, `ACCESS_COARSE_LOCATION`), và thư viện Retrofit 3.0.0 + converter kotlinx-serialization, kotlinx-serialization-json 1.11.0 cho API thời tiết Open-Meteo. Lần đầu dùng tầng domain (UseCase, Repository). Bỏ qua widget và Tasker. Bản này build được (`assembleDebug`, R8 của bản release), lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy (các bước kiểm tra mới: README › Kiểm tra trên máy thật, bước 35–39).

### Thêm

- Thời tiết (`is_aod_weather_enabled`, `is_aod_weather_fahrenheit`): icon trời và nhiệt độ cạnh ngày, °C hoặc °F. `DeviceLocationManager` (`data/device/location/`) lấy vị trí gần đúng một lần; `OpenMeteoApiService` (`data/network/api/`) nhận toạ độ đã làm tròn 2 chữ số thập phân; `WeatherRepositoryImpl` lưu bản mới vào DataStore (`weather_temperature_celsius`, `weather_code`, `is_weather_day`, `weather_updated_at_millis`) để đồng hồ hiện ngay mà không chờ mạng. `RefreshWeatherUseCase` chỉ gọi mạng khi bản đã lưu cũ từ 30 phút; AOD đang hiện thử lại mỗi 10 phút; bản cũ từ 3 giờ không hiện. Mã thời tiết WMO gom thành 8 nhóm (`WeatherCondition`), icon ngày và đêm.
- Sự kiện hôm nay (`is_aod_calendar_enabled`): tối đa 2 sự kiện còn lại trong ngày từ `CalendarContract.Instances` (`CalendarRepositoryImpl`, `GetUpcomingEventsUseCase`), sự kiện cả ngày so theo ngày ghi trên lịch. AOD đọc lúc mở và mỗi 15 phút, sự kiện đã kết thúc rời đồng hồ ở tick mỗi phút.
- Ghi nhớ (`aod_memo`, tối đa 120 ký tự, hiện tối đa 3 dòng) và hình vẽ nhanh (file `aod_drawing.png`, PNG nền trong suốt, tô theo màu đồng hồ).
- Màn cài đặt: mục "Thông tin thêm trên đồng hồ" (`MainExtrasSection`), hộp thoại nhập ghi nhớ có bộ đếm ký tự, khung vẽ (Lưu, Xóa hết, Hủy). Bật lịch hoặc thời tiết thì hỏi quyền, chỉ lưu "bật" khi được cấp; bật thời tiết thì tải ngay. `SettingsValueRow` có thêm dòng mô tả (hiện nội dung ghi nhớ).
- Tầng domain: `Weather`, `WeatherCondition`, `CalendarEvent`; `WeatherRepository`, `CalendarRepository`; `ObserveWeatherUseCase`, `RefreshWeatherUseCase`, `GetUpcomingEventsUseCase`. Koin: `networkModule` (`Json`, `Retrofit`, `OpenMeteoApiService`), `repositoryModule`, `useCaseModule`.
- `CLAUDE.md`: tầng domain và data (mục 1, 9), manager mới (mục 2), thư viện (mục 7.1), DI (mục 12), quy tắc RepositoryImpl dùng `withContextCatching` và không có exception riêng của domain (mục 9, 13, 13.1, 18 kèm lệnh grep chặn `withContext(` thô), bất biến 33–37, cách thêm thông tin cho đồng hồ (mục 20.6).

### Thay đổi

- `BackgroundImageManager` (`data/local/background/`) → `AodImageManager` (`data/local/image/`), giữ cả ảnh nền lẫn hình vẽ: `hasImage`, `saveImage`, `loadImage`, `removeImage` → `hasBackground`, `saveBackground`, `loadBackground`, `removeBackground`; thêm `hasDrawing`, `saveDrawing`, `loadDrawing`, `removeDrawing`.
- `PermissionManager`: thêm `hasCalendarPermission()`, `hasCoarseLocationPermission()`.
- `AodViewModel` nhận thêm 3 UseCase; `MainViewModel` nhận thêm `RefreshWeatherUseCase`.
- Splash: icon app (glyph đồng hồ màu mint) ở chính giữa màn hình, cỡ 144dp như icon của splash hệ thống từ Android 12, tên app nằm ngay dưới; thêm preview.

### Xóa

- `Placeholder.kt` của `domain/model`, `domain/repository`, `domain/usecase`, `data/repository`.

## [Chưa phát hành] – 2026-10-04 – Giai đoạn 4: thao tác và cảm biến

Gán hành động cho thao tác và phím, đèn pin, tự giảm sáng theo cảm biến ánh sáng, nhấc máy để hiện lại đồng hồ. Không thêm quyền và thư viện: đèn pin dùng `setTorchMode`, không cần quyền `CAMERA`. Bản này build được, lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy (các bước kiểm tra mới: README › Kiểm tra trên máy thật, bước 29–34).

### Thêm

- Hành động cho 6 thao tác (`AodGestureValue`): chạm 2 lần, vuốt lên, vuốt xuống, phím tăng/giảm âm lượng, phím quay lại. 7 hành động (`AodActionValue`): không làm gì, về màn hình khóa, tối màn hình, bật/tắt đèn pin, phát/tạm dừng, bài trước, bài tiếp theo. Mặc định giữ cách chạy trước đây (chạm 2 lần và quay lại về màn hình khóa). Keys `aod_double_tap_action`, `aod_swipe_up_action`, `aod_swipe_down_action`, `aod_volume_up_action`, `aod_volume_down_action`, `aod_back_action`.
- `FlashlightManager` (`data/device/flashlight/`): bật/tắt đèn pin, theo dõi trạng thái đèn từ mọi nguồn; AOD hiện icon khi đèn đang bật.
- `AmbientLightManager` (`data/device/light/`) và tùy chọn "Tự giảm sáng khi phòng tối" (`is_aod_auto_dim_enabled`): dưới 5 lux giảm về 1%, trên 20 lux trở lại, phải giữ 2 giây mới đổi. `AodActivity.renderDim()`.
- `PickupGestureManager` (`data/device/pickup/`) và tùy chọn "Nhấc máy để hiện lại đồng hồ" (`is_aod_raise_to_wake_enabled`): cảm biến `android.sensor.pick_up_gesture`. `AodService` chỉ chờ nhấc máy sau khi đồng hồ tối theo chủ ý và màn hình tắt, không chờ khi máy trong túi.
- Màn cài đặt: mục "Thao tác và cảm biến" (`MainInteractionSection`), hộp thoại chọn hành động cho từng thao tác. `SettingsSwitchRow` có thêm dòng mô tả và trạng thái tắt (máy không có cảm biến).
- `collectLatestCatching` trong `utils/CoroutineExt.kt`: như `collectCatching` nhưng huỷ khối đang chạy khi có giá trị mới. `AodViewModel.observeProximity` và `observeAmbientLight` dùng hàm này thay cho `collectLatest` thô.
- `CLAUDE.md`: 3 manager mới (mục 2), `collectLatestCatching` (mục 13, 13.1, 15, 18 kèm lệnh grep chặn `.collectLatest {` thô), bất biến 29–32, cách thêm thao tác hoặc hành động (mục 20.6).

### Thay đổi

- `AodIntent.DoubleTap` → `PerformGesture(gesture)`: ViewModel tra hành động đã gán. Dòng "Chạm 2 lần để thoát" chỉ hiện khi chạm 2 lần vẫn là về màn hình khóa (`AodState.isExitHintVisible`).
- `AodContent` giữ focus để nhận phím âm lượng; `AodScreen` xử lý phím quay lại bằng `BackHandler`.
- `AodService.launch()` → `launchAod()`.
- `AodViewModel` nhận thêm `FlashlightManager`, `AmbientLightManager`; `MainViewModel` nhận thêm 3 manager mới để biết máy có đèn pin và cảm biến không.

## [Chưa phát hành] – 2026-10-04 – Giai đoạn 3: giao diện đồng hồ

Mặt đồng hồ, font, màu, cỡ, ảnh nền, xoay ngang làm đồng hồ đêm. Không thêm quyền và thư viện: ảnh nền chọn bằng Photo Picker (không cần quyền đọc ảnh), font là họ font của hệ thống. Bản này build được, lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy (các bước kiểm tra mới: README › Kiểm tra trên máy thật, bước 24–28).

### Thêm

- 4 mặt đồng hồ (`ClockFaceValue`): Số (như trước), Số xếp chồng (giờ trên, phút dưới), Kim (có vạch giờ), Kim tối giản. Mặt kim vẽ bằng `Canvas`, nhích mỗi phút.
- Font (`ClockFontValue`): Mặc định, Có chân, Đơn cách, Viết tay — họ font chung của hệ thống, áp cho giờ và ngày. Dòng chọn font trong cài đặt hiện bằng chính font đó.
- Màu (`ClockColorValue`): 8 màu dịu trên nền đen (token mới `Blue`, `Purple`, `Orange`, `Pink` trong `Color.kt`), áp cho số giờ hoặc kim; mặc định xám như trước.
- Cỡ đồng hồ 60–150%, mỗi nấc 10%.
- Ảnh nền: `BackgroundImageManager` (`data/local/background/`) lưu bản sao JPEG đã thu về cỡ màn hình (Photo Picker chỉ cho đọc tạm). AOD giải mã ở luồng nền rồi hiện dần, mờ 50%, ẩn khi tối.
- "Luôn xoay ngang (đồng hồ đêm)": `AodActivity` khóa ngang theo cảm biến trước khi cửa sổ hiện. Bố cục ngang (đồng hồ trái, thông tin phải) chọn theo kích thước thật, nên cũng đúng khi hệ thống tự xoay.
- Màn cài đặt: mục "Giao diện đồng hồ" (`MainAppearanceSection`), bảng chọn màu dạng ô tròn. Keys `aod_clock_face`, `aod_clock_font`, `aod_clock_color`, `aod_clock_size_percent`, `is_aod_landscape`.
- `SettingsLabel` (nhãn nhóm dùng chung) và tham số `labelFontFamily` của `SettingsRadioRow`.
- `CLAUDE.md`: manager mới (mục 2), thành phần (mục 20.2), bất biến 25–28, cách thêm mặt đồng hồ hoặc màu (mục 20.6).

### Thay đổi

- `AodContent` tách giờ (`AodClockFace`) khỏi ngày và phần thông tin (`AodDetails`). Ở bố cục ngang, hai biên độ dịch chống burn-in đổi chỗ cho nhau.
- `AodViewModel` đọc giao diện trong `initialState` và tải ảnh nền lúc mở; nhận thêm `BackgroundImageManager`. `MainViewModel` nhận thêm `BackgroundImageManager`, gom 4 nhóm cài đặt vào một lần cập nhật state.
- Nhãn "Nguồn điện" của mục quy tắc dùng `SettingsLabel`.
- Đồng nhất tên Intent/Effect theo quy tắc mới (CLAUDE.md mục 10.2.1, kèm lệnh grep chặn tên ở thì quá khứ):
  - `MainIntent`: `Preview` → `OpenPreview`, `OpenLanguage` → `NavigateToLanguage`, `OpenPermission` → `OpenPermissionSettings`, `PickBackground` → `OpenBackgroundPicker`, `BackgroundPicked` → `BackgroundPickerResult`, `NotificationPermissionRequested` → `NotificationPermissionDialogShown`.
  - `MainEffect`: `OpenMiuiPermissionEditor` → `OpenMiuiPermissionSettings`, `LaunchBackgroundPicker` → `OpenBackgroundPicker`. Hàm `Context.openMiuiPermissionEditor()` → `openMiuiPermissionSettings()`.
  - `LanguageIntent`: `Done` → `ConfirmLanguage`, `Back` → `NavigateBack`; `LanguageState.isDoneEnabled` → `isConfirmEnabled`.
  - `AodIntent`: `MediaPlayPause` → `PlayPauseMedia`, `MediaSkipPrevious` → `SkipToPreviousTrack`, `MediaSkipNext` → `SkipToNextTrack`; `AodEffect.Close` → `CloseAod`.
  - Hàm private trong ViewModel đổi theo: `openPermissionSettings()`, `onBackgroundPickerResult()`, `onNotificationPermissionDialogShown()`, `confirmLanguage()`, `closeAod()`.

## [Chưa phát hành] – 2026-10-04 – Giai đoạn 2: thông báo trên đồng hồ

Ba tính năng dùng chung quyền "Truy cập thông báo" (`NotificationListenerService`): icon thông báo, viền sáng khi có thông báo mới, điều khiển nhạc. Không thêm thư viện và không thêm `uses-permission`; listener chỉ được khai báo là service do hệ thống bind. Bản này build được (`assembleDebug`), lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy (các bước kiểm tra mới: README › Kiểm tra trên máy thật, bước 19–23).

### Thêm

- `AodNotificationListener` (`presentation/aod/`): `NotificationListenerService`, hệ thống chỉ bind khi user đã cấp quyền. Callback chỉ đọc danh sách thông báo đang có và `RankingMap` rồi chuyển cho manager.
- `NotificationStateManager` (`data/device/notification/`):
  - Lọc thông báo như màn hình chờ của hệ thống: bỏ thông báo của chính app, thường trực, tóm tắt nhóm, thông báo nhạc, im lặng, bị Không làm phiền ẩn khỏi màn hình chờ, ẩn trên màn hình khóa, của app bị tạm ngưng.
  - Nạp sẵn icon trên một luồng nền, cache theo (package, resource), để khung đầu tiên của đồng hồ đã có icon.
  - `alerts`: báo thông báo mới, hoặc cập nhật không đặt "chỉ báo một lần", khi không bị Không làm phiền chặn.
  - `mediaSessionToken`: phiên nhạc của thông báo nhạc mới nhất.
- `MediaStateManager` (`data/device/media/`): `MediaController` từ token đó (không cần quyền riêng), cho tên bài, nghệ sĩ, đang phát, và Phát/Tạm dừng, Bài trước, Bài tiếp theo.
- Màn đồng hồ:
  - Hàng icon dưới ngày, mỗi app một icon, tối đa 5, còn lại "+N". Chỉ icon, không nội dung.
  - Viền sáng ở 4 cạnh, nhấp nháy khoảng 4 giây theo màu của app (không đặt hoặc quá tối thì dùng màu mint), chỉ khi đồng hồ không tối.
  - Tên bài, nghệ sĩ và 3 nút nhạc; nút bị bỏ qua khi cảm biến tiệm cận đang bị che, như chạm 2 lần.
- Màn cài đặt:
  - Mục "Thông báo trên đồng hồ" với 3 công tắc, mặc định bật (keys `is_aod_notification_icons_enabled`, `is_aod_edge_glow_enabled`, `is_aod_media_controls_enabled`), kèm dòng nhắc khi chưa có quyền.
  - Dòng quyền "Truy cập thông báo" (không bắt buộc), mở thẳng trang bật quyền của app (`openNotificationListenerSettings`, ROM không có trang đó thì mở danh sách).
- `CLAUDE.md`: 2 manager mới và ngoại lệ của chúng (mục 2), luồng và thành phần (mục 20.1, 20.2), bất biến 20–24.

### Thay đổi

- `AodViewModel` nhận thêm `NotificationStateManager` và `MediaStateManager`. Chạm 2 lần và nút nhạc cùng bị bỏ qua khi cảm biến tiệm cận đang bị che.
- Màn Main có thêm `MainNotificationsSection`; `MainState` có `notificationOptions` và `isNotificationAccessGranted`.

## [Chưa phát hành] – 2026-10-04 – Giai đoạn 1: quy tắc hiện, độ sáng, ô Cài đặt nhanh, ngôn ngữ

Năm tính năng đầu trong danh sách còn thiếu so với Always On AMOLED ([COMPARISON.md](COMPARISON.md)). Không thêm quyền và thư viện nào. Bản này build được (`assembleDebug`), lint 0 lỗi, grep của CLAUDE.md ra 0 dòng, nhưng chưa chạy trên máy (các bước kiểm tra mới: README › Kiểm tra trên máy thật, bước 1 và 14–18).

### Thêm

- Quy tắc nguồn điện (key `aod_charging_rule`): Luôn hiện / Chỉ khi đang cắm sạc / Chỉ khi dùng pin. "Đang cắm" đọc từ `BatteryManager.EXTRA_PLUGGED` (`BatteryStateManager.isPlugged`, `readIsPlugged()`), nên pin đầy hoặc máy dừng sạc ở 80% vẫn tính là đang cắm.
- Khung giờ (keys `is_aod_schedule_enabled`, `aod_schedule_start_minute`, `aod_schedule_end_minute`): mặc định bật, 07:00–23:00, chỉnh bằng `TimePicker` của Material 3. Bắt đầu sau kết thúc là khung qua nửa đêm; bắt đầu trùng kết thúc là cả ngày; tắt khung giờ là hiện cả ngày.
- Mức độ sáng (keys `is_aod_custom_brightness`, `aod_brightness_percent`): công tắc cùng thanh trượt 1–100%, mặc định bật ở 1% (bằng mức "giảm độ sáng" cũ). Tắt thì đồng hồ theo độ sáng của hệ thống.
- `AodRulesUiModel` (kèm `AodScheduleUiModel`, `ChargingRuleValue`, `ScheduleTimeValue`, `DataStoreManager.currentAodRules()`): một hàm `allows()` cho nguồn điện, khung giờ và ngưỡng pin, dùng chung cho `AodService.shouldEnter()` và `AodViewModel.checkRules()`. Đồng hồ đang hiện mà sai quy tắc (rút hoặc cắm sạc, hết khung giờ, pin yếu) thì chuyển sang đen, kiểm lại khi pin, nguồn cắm đổi và mỗi phút.
- Ô Cài đặt nhanh `AodTileService` (`TOGGLEABLE_TILE`): bật/tắt AOD, trạng thái theo `is_aod_enabled`. Hệ thống chặn khởi động foreground service từ ô (Android 15 trở lên) thì ô mở app bằng `startActivityAndCollapse` (đang khóa thì `unlockAndRun` trước) để `MainActivity` khởi động service.
- Màn chọn ngôn ngữ `presentation/screens/language/` (MVI, `LanguageDestination(isFirstOpen)`): English và Tiếng Việt (`LanguageValue.TRANSLATED`), tên viết bằng chính ngôn ngữ đó (`LanguageUiModel`), ngôn ngữ của máy lên đầu.
  - Lần đầu mở app: Splash → Language (chọn sẵn ngôn ngữ của máy, không có nút back) → Main. Bấm "Xong" lưu ngôn ngữ và `is_first_open = false`; thay TODO của base trong `SplashScreen`.
  - Từ màn Main: mục "Ứng dụng › Ngôn ngữ". "Xong" chỉ bật khi chọn khác ngôn ngữ đang dùng.
- `presentation/components/SettingsRows.kt`: `SettingsSectionHeader`, `SettingsSwitchRow`, `SettingsRadioRow`, `SettingsValueRow`, `SettingsSlider`, dùng chung cho màn Main và Language.
- `CLAUDE.md`:
  - Mục 13.1 "Chọn helper": collect flow của manager/UseCase phải dùng `collectCatching`, các chỗ được giữ collect thô, khi nào dùng `suspendRunCatching`, `withContextCatching`, `recoverCatching`.
  - Mục 18 thêm lệnh grep `\.collect {` thô.
  - Mục 20 thêm bất biến 17–19 (quy tắc hiện, `EXTRA_PLUGGED`, khung giờ) và ô Cài đặt nhanh.

### Thay đổi

- Màn Main tách thành `MainContent` cùng `MainPermissionsSection`, `MainOptionsSection`, `MainRulesSection` (mục mới "Khi nào hiện": nguồn điện, khung giờ, ngưỡng pin) và `MainAppSection` (ngôn ngữ). Ngưỡng pin chuyển từ "Tùy chọn" sang "Khi nào hiện" (`AodOptionsUiModel.minBattery` → `AodRulesUiModel.minBattery`).
- `MainScreen` nhận `backStack` (mở màn Language); `MainViewModel` nhận thêm `LanguageManager`.
- `AodActivity`: độ sáng là `aod_brightness_percent / 100` khi bật độ sáng riêng, ngược lại `BRIGHTNESS_OVERRIDE_NONE`. Vẫn đặt trong `onCreate`, trước khi cửa sổ hiện.
- `AodService.start()` trả `Boolean`: `false` khi hệ thống chặn khởi động foreground service.
- Collector của `AodService` (sự kiện màn hình, ngôn ngữ), `AodViewModel` (pin, nguồn cắm, âm thanh) và `AodTileService` dùng `collectCatching` thay vì `.collect { }` thô. Lỗi chỉ dừng collector đó và được log, không làm crash app. Collector tiệm cận giữ `collectLatest`, vì cần huỷ lần chờ 3 giây khi giá trị đổi.

### Xóa

- Key `is_aod_dim_brightness` (`saveIsAodDimBrightness`) và chuỗi `opt_dim`: thay bằng độ sáng riêng 1–100%. Giá trị đã lưu của key cũ không được chuyển sang, vì app chưa phát hành.

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
- `AppLanguageProvider`, dùng chung cho `MainActivity` và `AodActivity`. Component tự lấy `DataStoreManager` và `LanguageManager` qua `koinInject`, Activity chỉ cần `AppLanguageProvider { … }`.
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
