# CLAUDE.md

Hướng dẫn cho Claude Code (và dev) khi làm việc trong project này. **Mọi code mới phải theo đúng rule bên dưới — không trôi về pattern Android/Compose chung chung.** Code hiện có mâu thuẫn với rule → theo rule và báo lại.

## Project

**Custom AOD** — Always On Display giả lập: mỗi lần màn hình tắt, app mở một Activity nền đen có đồng hồ che màn hình khoá rồi tự bật màn hình lại. Repo: [decoutkhanqindev/Custom-AOD](https://github.com/decoutkhanqindev/Custom-AOD).

- Dựng từ base *Android-Base* ([decoutkhanqindev/Android-Base](https://github.com/decoutkhanqindev/Android-Base)): Jetpack Compose · Clean Architecture · MVI · Navigation 3 · Koin · Coroutines/Flow. Lõi AOD port từ demo FakeAOD và refactor theo rule của base — cơ chế, các điểm không được đổi và lý do: [mục 20](#20-aod-core).
- Khung kiến trúc (Clean Arch + MVI + Nav3 + Koin) lấy từ *Lịch Việt Lộc Phát* ([decoutkhanqindev/Lich-Viet-Loc-Phat](https://github.com/decoutkhanqindev/Lich-Viet-Loc-Phat)).
- Hạ tầng dùng chung (ads + consent UMP, DataStore/Language/Network manager, CoroutineExt, Modifiers, dialog) port từ *DexReader* ([decoutkhanqindev/DexReader](https://github.com/decoutkhanqindev/DexReader)), đã đổi Hilt → Koin, navigation-compose → Navigation 3.
- Package / namespace / applicationId: `com.decoutkhanqindev.custom_aod` · single module `:app`
- minSdk 30 · compileSdk/targetSdk 37 · Kotlin 2.4.20 · AGP 9.4.1 · Gradle 9.8.0 · JDK 17 (toolchain)
- Theme: design system `Aods` (token 3 tầng, `AodsTheme`), luôn tối (nền đen, accent mint — bảng màu của FakeAOD) · XML theme `Theme.App`, `Theme.App.Aod`
- **Skeleton**: chỗ còn phải implement đều có `// TODO` → Android Studio › View › Tool Windows › **TODO**. Cách build, cấp quyền, test trên máy: [README.md](README.md).

## Commands

| Mục đích | Lệnh |
|---|---|
| Verify nhanh (ưu tiên) | `./gradlew :app:compileDebugKotlin` |
| Build / cài debug | `./gradlew assembleDebug` · `./gradlew installDebug` |
| Build release (R8 + shrink) | `./gradlew assembleRelease` — cần signing trong `local.properties` (README) |
| Unit test | `./gradlew testDebugUnitTest` · `./gradlew test --tests "*.ClassName"` |
| Lint | `./gradlew lint` |
| Dependency tree | `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` |

> Build treo → `./gradlew --stop`. Đổi tên/di chuyển thư mục `res/` mà build báo thiếu resource dù file vẫn còn → cache Gradle cũ: `./gradlew --stop` rồi `./gradlew clean assembleDebug --no-configuration-cache`.

## Hard rules ⚠️

- **Git — KHÔNG tự chạy khi chưa được phép:** `git commit` · `git push` / `--force` · `git reset --hard` · `git rebase` · `git merge` · `git branch -D`.
- Thêm dependency, permission Manifest, hoặc xoá code/resource đang dùng → hỏi trước.
- Xong việc phải: `./gradlew :app:compileDebugKotlin` pass + chạy [grep kiểm tra](#18-banned-patterns) ra 0 kết quả.

---

## 1. Kiến trúc tổng quan

Clean Architecture + MVI trong **1 module `:app`** — ranh giới layer giữ bằng **package**, nên rule import bên dưới là bắt buộc (compiler không chặn hộ).

```
com.decoutkhanqindev.custom_aod/
├── App.kt                         # Application: Timber, Koin, (TODO) SDK khác
├── di/AppModule.kt                # Koin: managerModule · networkModule · adsModule · aodModule · repositoryModule · useCaseModule · viewModelModule
├── ads/                           # AdMob + consent UMP (không nằm trong presentation dù có Compose)
│   ├── AdsManager.kt              #   consent, init MobileAds, currentActivity, isAdShowing + các placement `by lazy`
│   ├── ad_unit/                   #   AdUnit (base) · AdUnitState · Banner/Native/Interstitial/Reward/AppOpenAdUnit
│   └── composables/               #   BannerAdView · NativeAdView (+ NativeLayoutType) · AdLoadingDialog
├── domain/                        # Business thuần Kotlin: thời tiết, sự kiện lịch (mục 20)
│   ├── model/                     #   Weather · WeatherCondition · CalendarEvent
│   ├── repository/                #   WeatherRepository · CalendarRepository (interface)
│   └── usecase/                   #   ObserveWeatherUseCase · RefreshWeatherUseCase · GetUpcomingEventsUseCase
├── data/
│   ├── local/datastore/           # DataStoreManager  — prefs app-shell + tuỳ chọn AOD
│   ├── local/locale/              # LanguageManager   — locale/ngôn ngữ
│   ├── local/image/               # AodImageManager   — ảnh nền + hình vẽ nhanh của AOD (file trong filesDir)
│   ├── network/connectivity/      # NetworkManager    — trạng thái mạng
│   ├── network/api/               # OpenMeteoApiService (Retrofit) · response/ (DTO @Serializable)
│   ├── mapper/                    # mã thời tiết WMO → WeatherCondition
│   ├── device/                    # tín hiệu thiết bị cho AOD (mục 20.3)
│   │   ├── screen/                #   ScreenStateManager    — SCREEN_OFF/USER_PRESENT, isInteractive, wake lock
│   │   ├── battery/               #   BatteryStateManager   — % pin, đang sạc, đang cắm nguồn
│   │   ├── audio/                 #   AudioStateManager     — chuông / cuộc gọi / báo thức
│   │   ├── proximity/             #   ProximityManager      — cảm biến tiệm cận
│   │   ├── notification/          #   NotificationStateManager — thông báo hiện được trên AOD (nội dung theo quy tắc của màn hình khoá), thông báo mới, token phiên nhạc
│   │   ├── media/                 #   MediaStateManager     — bài đang phát + điều khiển (từ token của thông báo nhạc)
│   │   ├── flashlight/            #   FlashlightManager     — đèn pin (setTorchMode, không cần quyền CAMERA)
│   │   ├── light/                 #   AmbientLightManager   — cảm biến ánh sáng
│   │   ├── pickup/                #   PickupGestureManager  — cảm biến nhấc máy (android.sensor.pick_up_gesture)
│   │   ├── location/              #   DeviceLocationManager — vị trí gần đúng 1 lần (cho thời tiết)
│   │   └── permission/            #   PermissionManager     — overlay, thông báo, truy cập thông báo, quyền riêng của Xiaomi
│   └── repository/                # WeatherRepositoryImpl (Open-Meteo + bản lưu trong DataStore) · CalendarRepositoryImpl (CalendarContract)
├── utils/                         # CoroutineExt · NavExt (navigateTo) · ContextExt (showToast, registerSystemReceiver, mở trang cài đặt) · Tag
└── presentation/
    ├── MainActivity.kt            # requestConsent, áp locale (AppLanguageProvider), AodsTheme, start AodService
    ├── aod/                       # runtime AOD: AodActivity · AodService · AodSession · BootReceiver · AodTileService · AodNotificationListener (mục 20)
    ├── base/BaseViewModel.kt      # MVI <State, Intent, Effect>
    ├── components/                # Modifiers · AodsLottie · AodsSettingsRows (AodsSectionHeader, AodsSwitchRow…) · AppLanguageProvider · dialog/AodsNoInternetDialog
    ├── effects/                   # LaunchedWithLifecycleEffect (collect flow theo lifecycle)
    ├── model/                     # UiModel (AodOptions, AodAppearance, AodInteraction, AodNotificationOptions, AodExtras, AodRules, AodSchedule, AodNotifications, Media, Weather, CalendarEvent, Language…) · LanguageValue · AnimationContentKey · AodGestureValue · AodActionValue · ClockFaceValue · ClockFontValue · ClockColorValue · WallpaperValue · WeatherConditionValue · ChargingRuleValue · ScheduleTimeValue · PermissionValue · PermissionStatusValue · WakeResultValue
    ├── navigation/                # AppDestinations (NavKey) · AppNavDisplay (+ AodsNoInternetDialog)
    ├── screens/<feature>/         # XxxScreen · XxxContent · XxxViewModel · state/{XxxState, XxxIntent, XxxEffect}
    │   ├── language/              #   chọn ngôn ngữ: lần đầu mở app (Splash → Language → Permission → Main) và từ màn Main
    │   ├── permission/            #   bước 2 của onboarding lần đầu: chỉ quyền bắt buộc cho AOD, đủ quyền mới vào Main
    │   ├── main/                  #   cài đặt AOD: công tắc, quyền, tuỳ chọn, giao diện, thông tin thêm (ghi nhớ, hình vẽ, lịch, thời tiết), thông báo trên đồng hồ, thao tác và cảm biến, quy tắc hiện, ngôn ngữ, xem thử (+ MainXxxSection)
    │   └── aod/                   #   đồng hồ AOD, host là AodActivity (không nằm trong NavDisplay): AodContent · AodClockFace · AodDetails · AodExtras · AodNotifications · AodMediaControls · AodBackdrop · AodTimeFormat
    └── theme/                     # AodsTheme (+ cầu nối Material 3) · tokens/ (AodsPrimitive* → Aods*Tokens semantic → token component/domain)
```

`screens/language/` là màn MVI đầy đủ gọn nhất (6 file) — copy làm khung cho màn mới; `screens/main/` có thêm `MainXxxSection.kt` (Content dài tách theo mục 10.4).

### 1.1 Luồng dữ liệu

```
User ─▶ Content ──onIntent(Intent)──▶ ViewModel ──invoke()──▶ UseCase ──▶ Repository (interface, domain)
          ▲                              │                                   │ Impl ở data, withContextCatching
          │ state: StateFlow<State> ─────┤                                   ▼
Screen ◀──┴─ effect: SharedFlow<Effect> ─┘                    DataSource · API · DB · Manager

App-shell (ngôn ngữ, cờ lần đầu mở, mạng, ads) ── Manager ──▶ Screen (koinInject) / ViewModel / Repository (inject thẳng)
```

### 1.2 Dependency rule (hướng import giữa các layer)

```
presentation ──▶ domain ◀── data
      │  ╲         ▲         ╱
      │   ╲─────── di ──────╱          utils: layer nào cũng dùng được; utils KHÔNG import layer nào
      └──▶ Manager ở data/ + ads/   (ngoại lệ được chấp nhận — xem mục 2)
```

| Layer | Được import | KHÔNG được import |
|---|---|---|
| `domain` | Kotlin stdlib · kotlinx.coroutines (`Flow`) · `java.time` · Timber · `utils/` | `android.*` · `androidx.*` · Compose · Koin · `data` · `presentation` · `ads` |
| `data` | `domain` · `android.*` · kotlinx.coroutines · AndroidX DataStore · Retrofit + kotlinx-serialization (`data/network/api`) · `utils/` | `presentation` · Compose · `ads` |
| `presentation` | `domain` (UseCase, model) · `presentation/*` · `utils/` · AndroidX/Compose · Manager trong `data/` · `ads/` | `data` Impl / Repository / DataSource |
| `ads` | `NetworkManager` (inject trong `AdsManager`) · `AdsManager` (inject trong `AdUnit`) · `utils/` · `presentation/components` + `presentation/theme` (chỉ trong `ads/composables`) | `domain` · ViewModel · screen |
| `di` | mọi layer | — |
| `utils` | Kotlin · AndroidX thuần | mọi package của app |

Hệ quả bắt buộc:
- **Dữ liệu nghiệp vụ**: ViewModel → UseCase → Repository (interface ở `domain`, Impl ở `data`, bind ở `di`). ViewModel không gọi Repository/DataSource.
- **State hạ tầng app-shell** (ngôn ngữ, cờ lần đầu mở, mạng, ads): đi qua **Manager**, inject thẳng nơi dùng — không bọc UseCase/Repository (mục 2).
- Model qua biên layer phải map: DTO/Entity → domain model (mapper ở `data`) → UiModel (`toUiModel()` ở `presentation/model`).

### 1.3 Phần tuỳ project (chưa có trong base — thêm khi cần)

| Cần | Làm | Tham khảo |
|---|---|---|
| Gọi API | **Đã có:** `data/network/api/` (`XxxApiService` + `response/` DTO `@Serializable`) + `networkModule` (`Json`, `Retrofit` với converter kotlinx-serialization, `create(XxxApiService)`). Không interceptor, không exception riêng của domain: lỗi xử lý ở Impl ([mục 9](#9-data-layer)) | `OpenMeteoApiService` · `WeatherRepositoryImpl` |
| DB local | `data/local/database/` (Room: Database, Dao, Entity) | DexReader `data/local/database` |
| Nguồn dữ liệu khác (asset, thuật toán…) | `data/source/<tên>/` — `XxxDataSource` + `XxxDataSourceImpl`, chỉ `data/repository` dùng | Lịch Việt `data/source` |
| Firebase (Analytics/Crashlytics/Perf) | plugin `google-services` (+ `crashlytics`, `firebase-perf`), Firebase BOM, `app/google-services.json`; bật collection chỉ ở release: `Firebase.crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG` (tương tự analytics/perf) trong `lifecycleScope.launch(Dispatchers.IO)` | DexReader `MainActivity.setUpFirebaseSdk` |
| Onboarding | **Đã có:** Language → Permission, bước đánh số theo `OnboardingStepValue`, khung chung `AodsOnboardingHeader` / `AodsOnboardingFooter`. Thêm bước: thêm vào `OnboardingStepValue`, `saveIsFirstOpen(false)` luôn ở bước cuối ([mục 5](#5-ngôn-ngữ--locale)); ad: [mục 4](#4-ads--admob--consent-ump) | `screens/permission/` · DexReader `screens/onboarding` |
| Widget | Glance — `presentation/widget/` | Lịch Việt `presentation/widget` |
| Service / BroadcastReceiver | `presentation/<feature>/` cạnh Activity dùng chung state (vd `presentation/aod/`); logic quyết định ở đó, truy cập hệ thống qua manager | Custom AOD `presentation/aod` (mục 20) |

---

## 2. Manager — hạ tầng app-shell

Manager = hạ tầng runtime **không phải nghiệp vụ** (prefs của app-shell, locale, kết nối mạng, tín hiệu thiết bị). Không có UseCase/Repository bọc ngoài — UseCase chỉ forward 1 dòng là dư thừa.

| Manager | Vị trí | Cung cấp |
|---|---|---|
| `DataStoreManager` | `data/local/datastore/` | `selectedLangCode`, `isFirstOpen`, tuỳ chọn AOD (`isAodEnabled`, `isAodCustomBrightness`, `aodBrightnessPercent`, `isAodProximityEnabled`, `aodTimeoutMinutes`), giao diện đồng hồ (`aodClockFace`, `aodClockFont`, `aodClockColor`, `aodClockSizePercent`, `isAodLandscape`, `aodWallpaper`), thao tác và cảm biến (`aodDoubleTapAction`, `aodSwipeUpAction`, `aodSwipeDownAction`, `aodVolumeUpAction`, `aodVolumeDownAction`, `aodBackAction`, `isAodAutoDimEnabled`, `isAodRaiseToWakeEnabled`), thông báo trên đồng hồ (`isAodNotificationIconsEnabled`, `isAodNotificationContentEnabled`, `isAodEdgeGlowEnabled`, `isAodMediaControlsEnabled`), quy tắc hiện (`aodMinBattery`, `aodChargingRule`, `isAodScheduleEnabled`, `aodScheduleStartMinute`, `aodScheduleEndMinute`), thông tin thêm (`aodMemo`, `isAodCalendarEnabled`, `isAodWeatherEnabled`, `isAodWeatherFahrenheit`), bản thời tiết đã lưu (`weatherTemperatureCelsius`, `weatherCode`, `isWeatherDay`, `weatherUpdatedAtMillis` — ghi cùng 1 lần `edit` bằng `saveWeather(…)`), `aodLastWake`, `isNotificationsAsked` — mỗi key 1 `StateFlow<T?>` + `saveXxx()`; `DEFAULT_*` của tuỳ chọn AOD là `const` public để nơi đọc đồng bộ có giá trị dự phòng |
| `LanguageManager` | `data/local/locale/` | `deviceLanguageCode()`, `configurationFor(code)`, `resourcesFor(config)`, `displayNameOf(code, displayIn)` |
| `AodImageManager` | `data/local/image/` | `hasBackground` · `hasDrawing: StateFlow<Boolean?>`, `saveBackground(uri): Boolean` (suspend, chép và thu nhỏ ảnh từ Photo Picker), `saveDrawing(bitmap): Boolean` (suspend, PNG nền trong suốt), `loadBackground()` · `loadDrawing(): Bitmap?` (suspend), `removeBackground()`, `removeDrawing()` |
| `NetworkManager` | `data/network/connectivity/` | `isAvailable: StateFlow<Boolean>` |
| `ScreenStateManager` | `data/device/screen/` | `events: SharedFlow<String>` (action `SCREEN_OFF` / `USER_PRESENT`), `isInteractive`, `isDeviceSecure`, `wakeUp(holdMillis)` |
| `BatteryStateManager` | `data/device/battery/` | `levelPercent: StateFlow<Int?>`, `isCharging: StateFlow<Boolean?>`, `isPlugged: StateFlow<Boolean?>` (`EXTRA_PLUGGED`: cắm nguồn kể cả khi pin đầy / giới hạn sạc), `readLevelPercent()`, `readIsCharging()`, `readIsPlugged()` |
| `AudioStateManager` | `data/device/audio/` | `isBusy: StateFlow<Boolean?>` (chuông / cuộc gọi / báo thức), `isBusyNow()` |
| `ProximityManager` | `data/device/proximity/` | `isNear: StateFlow<Boolean>` |
| `NotificationStateManager` | `data/device/notification/` | `notifications: StateFlow<List<ActiveNotification>>` (thông báo hiện được trên AOD, mới nhất trước, icon đã nạp sẵn, `content` chỉ là phần màn hình khoá cho hiện), `alerts: SharedFlow<ActiveNotification>` (thông báo mới, cho viền sáng), `mediaSessionToken: StateFlow<MediaSession.Token?>`; nhận dữ liệu qua `onListenerConnected` · `onNotificationPosted` · `onNotificationsChanged` · `onListenerDisconnected` |
| `MediaStateManager` | `data/device/media/` | `playback: StateFlow<MediaPlayback?>` (tên bài, nghệ sĩ, đang phát, có bài trước/tiếp), `playPause()`, `skipToPrevious()`, `skipToNext()` |
| `FlashlightManager` | `data/device/flashlight/` | `isAvailable`, `isOn: StateFlow<Boolean>` (trạng thái đèn do bất kỳ đâu bật), `toggle()` |
| `AmbientLightManager` | `data/device/light/` | `isAvailable`, `lux: StateFlow<Float?>` |
| `PickupGestureManager` | `data/device/pickup/` | `isSupported`, `pickups: Flow<Unit>` (lạnh: phát một lần khi nhấc máy, huỷ collect là huỷ chờ) |
| `DeviceLocationManager` | `data/device/location/` | `currentCoarseLocation(): Coordinates?` (suspend, IO: vị trí gần đúng 1 lần, chờ tối đa 10 giây rồi lấy vị trí đã biết gần nhất; không có quyền hoặc tắt định vị → `null`) |
| `PermissionManager` | `data/device/permission/` | `isXiaomi`, `canDrawOverlays()`, `areNotificationsEnabled()`, `needsNotificationPermission()`, `isNotificationListenerEnabled()`, `isMiuiShowWhenLockedAllowed()`, `isMiuiBackgroundStartAllowed()`, `hasCalendarPermission()`, `hasCoarseLocationPermission()` |
| `AdsManager` | `ads/` | consent, init MobileAds, current activity, `isAdShowing`, placement (mục 4) |

Rule:
- Mỗi manager = **1 class cụ thể** `XxxManager` trong `data/<area>/<tên>/` — không interface + `Impl`, không base class chỉ có 1 lớp con. Koin `single { XxxManager(androidApplication()) }`. API chỉ dùng primitive/ISO code (không nhận/trả enum của presentation).
- Manager là **state holder**: tự tạo scope `CoroutineScope(SupervisorJob() + Dispatchers.IO/Default)`, expose `StateFlow` nóng. Không trả `Result` qua biên manager — lỗi xử lý trong manager (`recoverCatching` → giá trị mặc định, `withContextCatching` → log).
- Nơi dùng:
  - Composable: `val xxxManager: XxxManager = koinInject()` ngay tại nơi dùng — **chỉ ở Screen** (hoặc host bọc cả màn như `AppNavDisplay`, `AppLanguageProvider`), Content nhận giá trị qua tham số. Không tự viết CompositionLocal để truyền manager. Activity: `by inject()`.
  - ViewModel: inject manager qua constructor (vd refetch khi `selectedLangCode` đổi).
  - Repository: inject manager (vd đọc `selectedLangCode.filterNotNull().first()` — **1 lần mỗi hàm**, không đọc trong vòng map).
- Signal hạ tầng mới (pin, mạng tính phí…) → manager mới cùng pattern, KHÔNG làm domain repository/UseCase/`object` singleton trong `utils/`.
- Manager tín hiệu thiết bị (`data/device/`) dùng `WhileSubscribed(replayExpirationMillis = 0)`: receiver/cảm biến chỉ đăng ký khi có người collect và giá trị về mặc định khi hết người collect (không đọc nhầm giá trị của lần AOD trước). Hai ngoại lệ có chủ ý so với "StateFlow nóng":
  - Sự kiện không được gộp → `SharedFlow` (`ScreenStateManager.events`): một lần tắt rồi bật lại màn hình ngay phải tới đủ cả `SCREEN_OFF`.
  - Nơi cần giá trị **đúng lúc này** (service lúc `SCREEN_OFF`, khung đầu tiên của AOD) đọc đồng bộ: `isInteractive`, `isBusyNow()`, `readLevelPercent()`… thay vì giữ receiver chạy suốt (`BATTERY_CHANGED` gửi rất thường xuyên).
- `NotificationStateManager` và `MediaStateManager` là ngoại lệ có chủ ý:
  - Không dùng `WhileSubscribed`: app không tự đăng ký nhận thông báo được, chỉ `AodNotificationListener` do hệ thống bind mới nhận, nên listener **đẩy** dữ liệu vào manager (`MutableStateFlow` nóng); `MediaStateManager` nóng theo `mediaSessionToken`. Nhờ vậy khung đầu tiên của AOD có ngay icon và bài nhạc.
  - API trả data class của data layer (`ActiveNotification`, `MediaPlayback`) vì một thông báo không biểu diễn được bằng primitive. Không phải model của presentation; ViewModel vẫn map sang UiModel.

**DataStoreManager**
- **Mỗi key 1 `StateFlow`, chỉ primitive, chỉ giá trị đã lưu** (không gộp data class — `StateFlow` so sánh cả object, consumer phải `distinctUntilChanged` lại). Không enum, không mapper trong store.
- Thêm key: `const val XXX_KEY` + `DEFAULT_XXX` trong `companion object` → `private val xxxKey = xxxPreferencesKey(XXX_KEY)` → `val xxx = xxxKey.asStateFlow(default = DEFAULT_XXX)` → `fun saveXxx(value) { edit { it[xxxKey] = value } }`.
- Flow khởi đầu `null` = "chưa đọc xong" (`SharingStarted.Eagerly`). Cần chờ giá trị thật → `filterNotNull().first()`; UI tự quyết khi `null`.
- `save*` là `fun` thường, launch trên scope của manager → **không bị huỷ khi user rời màn** (không save qua `rememberCoroutineScope`). Không set giá trị tạm — chờ DataStore emit lại.
- Phải là **1 instance** (Koin `single`) — `preferencesDataStore` delegate trùng file sẽ crash.

**NetworkManager** — chi tiết bắt buộc giữ:
- `callbackFlow` quanh `registerDefaultNetworkCallback`; "có mạng" = `NET_CAPABILITY_INTERNET && NET_CAPABILITY_VALIDATED` (Wi-Fi không ra Internet = mất mạng).
- Emit từ `onCapabilitiesChanged` + `onLost` + 1 lần đọc đầu; **không** emit ở `onAvailable` (chưa có capabilities). `onLost` gửi `false` thẳng — **không** đọc `activeNetwork` trong callback (vẫn trả network đang mất).
- `debounce { if (it) 0 else 500ms }` → chuyển Wi-Fi ↔ 4G không nháy dialog; `distinctUntilChanged`; `recoverCatching { emit(true) }` (observer hỏng không được khoá user sau dialog); `stateIn(WhileSubscribed(5_000), initialValue = true)`.
- Cần permission `ACCESS_NETWORK_STATE`.

---

## 3. Mạng — AodsNoInternetDialog

- "Mất mạng" được **quan sát**, không suy ra từ request lỗi. `AppNavDisplay` lấy `NetworkManager` bằng `koinInject()`, collect `isAvailable` và render `if (!isNetworkAvailable) AodsNoInternetDialog()` sau `NavDisplay` → phủ mọi màn.
- `AodsNoInternetDialog` không tắt được (Back/ngoài vùng), 1 nút mở `Settings.Panel.ACTION_INTERNET_CONNECTIVITY` (minSdk 30 nên luôn có); tự biến mất khi có mạng lại.
- Custom AOD giữ dialog này như base: màn cài đặt cần mạng (Splash chờ consent), còn AOD vẫn chạy offline vì do `AodService` điều khiển.
- Màn hình không cần lỗi riêng "offline" — request lỗi hiện lỗi chung, dialog là tín hiệu offline duy nhất.
- App chạy được offline → bỏ dòng `AodsNoInternetDialog()` trong `AppNavDisplay`.

---

## 4. Ads — AdMob + consent UMP

### 4.1 AdsManager & consent
- **`AdsManager`** (`KoinComponent` + `ActivityLifecycleCallbacks` + `Tag`; Koin `single { AdsManager() }`, tạo lazy) — **1 class** chứa toàn bộ hạ tầng ads + khai báo placement, không base class. Member của instance:
  - `application`, `networkManager` lấy bằng `by inject()` (không qua constructor);
  - consent: `requestConsent(activity)`, `isConsentGathered`, `canRequestAds`;
  - init MobileAds: `isMobileAdsInitialized` (idempotent qua `AtomicBoolean`);
  - `isNetworkAvailable`, `isAdShowing` (`internal set`), `currentActivity`, lifecycle callback, test device ids, `scope` (`private`, `CoroutineScope(SupervisorJob() + Dispatchers.Main)` — chỉ cho consent/init MobileAds; init tự chuyển sang `Dispatchers.IO` bên trong).
- State nằm trên **instance `AdsManager`** (Koin `single` → 1 instance), không dùng `companion object`. Màn hình đọc `adsManager.isConsentGathered` / `isMobileAdsInitialized` / `canRequestAds`; `adsManager.isAdShowing` là nguồn duy nhất cho "đang có ad full-screen" (dùng cho app-open khi resume…).
- `AdsManager` đăng ký `ActivityLifecycleCallbacks` trong `init` (1 lần — chỉ có 1 `AdsManager`, không unregister) để giữ `currentActivity` (form consent dùng).
- `MainActivity.onCreate` gọi `adsManager.requestConsent(this)` ngay sau `super.onCreate`.
- Consent xin **mỗi lần mở app** (rule của Google) qua `requestConsent(activity)`, cờ `isConsentRequested` theo process (Activity recreate không tạo thêm collector, process mới thì xin lại): collect `networkManager.isAvailable` → mỗi lần có mạng gọi `gatherConsent()` (offline lần đầu tự thử lại khi có mạng). Không dựa vào `onActivityCreated` — AdsManager tạo lazy nên ra đời sau callback đó.
- `gatherConsent`: nếu consent cũ còn hiệu lực (`canRequestAds()`) → init SDK ngay song song; `requestConsentInfoUpdate` → `loadAndShowConsentFormIfRequired`. **Cả nhánh thành công lẫn lỗi** đều về `consentGatheringComplete()` → init nếu `canRequestAds` + bật `isConsentGathered` ⇒ luồng luôn kết thúc, Splash không treo.
- `MobileAds.initialize` chạy `Dispatchers.IO`, idempotent qua `AtomicBoolean`. Test device: hash trong `local.properties` `admob.test.device.ids=hash1,hash2` (không commit); debug thêm `DEBUG_GEOGRAPHY_EEA` để thử form ở mọi nơi.
- API cho màn hình: `isConsentGathered: StateFlow<Boolean>`, `isMobileAdsInitialized: StateFlow<Boolean>`, `canRequestAds: Boolean`, `isAdShowing`. Bảng quyết định (Splash đang làm):

| consentGathered | canRequestAds | initialized | Làm |
|---|---|---|---|
| false | — | — | chờ (form có thể đang hiện) |
| true | false | — | đi tiếp không ads |
| true | true | false | chờ |
| true | true | true | `interSplash.load()` → LOADED: `show` · FAILED: đi tiếp |

- Nội dung form consent cấu hình ở AdMob console (Privacy & messaging) cho App ID thật — chưa publish thì UMP lỗi 3 *publisher misconfiguration* (luồng vẫn đi tiếp). Chưa có mục "quản lý quyền riêng tư" (`privacyOptionsRequirementStatus` / `showPrivacyOptionsForm`) — Google yêu cầu cho user EEA, thêm khi review policy cần.

### 4.2 AdUnit
- `floors: List<Pair<adUnitId, name>>` xếp **high floor trước** (`listOf(HIGH_ID to "inter_home_high", ALL_ID to "inter_home_all")`); placement 1 id = list 1 phần tử. Mỗi `load()` bắt đầu lại từ floor 0; fail → floor kế; hết floor → `FAILED`. Không lặp id trong list.
- `AdUnit(floors)` là `KoinComponent`, lấy `AdsManager` bằng `private val adsManager: AdsManager by inject()` → từ `AdsManager` unit chỉ dùng `isAdShowing` (ghi) và `canRequestAds` / `isNetworkAvailable` (đọc trong `load()`). Constructor chỉ nhận `floors` — không truyền provider/manager/callback.
- Mỗi unit có **scope riêng** `CoroutineScope(SupervisorJob() + Dispatchers.Main)` — Mobile Ads SDK bắt buộc load/show trên **main thread**, tuyệt đối không chạy request trên `Dispatchers.IO`.
- Unit full-screen (Interstitial/Reward/AppOpen) gán `isAdShowing` (property của `AdUnit` trỏ thẳng tới `adsManager.isAdShowing`): `true` khi `onAdShowedFullScreenContent`/`onAdImpression`, `false` khi đóng/lỗi hiển thị — không cần callback `onShowed/onClosed` ra ngoài.
- `load()`: guard `LOADING`/`LOADED` → no-op (giữ preload) · `resetWaterfall()` · không consent → `FAILED` · không mạng → `FAILED` · request. `NONE`/`FAILED`/`IMPRESSION` đều load lại được. Không có state `NO_NETWORK`, không tự retry khi có mạng — caller gọi `load()` lại.
- ⚠️ Ad full-screen (Interstitial/Reward/AppOpen) **không** gọi `load()` lại khi đang `IMPRESSION` (đang hiện) — ad mới sẽ bị `onAdDismissed…` xoá; cần guard riêng ở caller.
- Mỗi request bọc `withTimeout(AdUnit.LOAD_TIMEOUT = 20s)` — timeout = fail thật. Generation counter bỏ kết quả về muộn.
- `release()`: bỏ kết quả đang chờ (generation), giải phóng object SDK, về `NONE` — unit singleton load lại được ở lần vào màn sau. `destroy()` (terminal): `scope.cancel()` + `release()` — huỷ scope riêng của unit (hiện chưa có chỗ gọi).
- Subclass mới chỉ implement `requestLoad(context, generation)` (callback SDK → `suspendCancellableCoroutine`) + `releaseAd()`; `catch (TimeoutCancellationException)` phải đứng trước `catch (CancellationException) { throw e }`.
- Log: mọi action của unit (Loading/Loaded/Timeout/Failed/Showed/Impression/Closed/Released…) gọi `log("Action")` của `AdUnit` → `inter_splash_all (ca-app-pub-…/…) - Loading` (tag = tên class unit). Không gọi `Timber` trực tiếp trong unit con — log luôn phải kèm ad unit id để biết floor nào đang load/hiện.

### 4.3 Placement & id
- Mỗi placement = 1 `val xxx by lazy { XxxAdUnit(floors = listOf(BuildConfig.XXX_ALL_ID to "xxx_all")) }` trong `AdsManager` (hiện có `interSplash`). **Không** tạo AdUnit inline trong composable.
- Id mỗi placement = `buildConfigField("String", "<PLACEMENT>_ALL_ID", …)` khai báo ở **cả `release {}` (id thật) lẫn `debug {}` (test id Google)** trong `app/build.gradle.kts` → debug không bao giờ hiện/click ad thật. 5 test id chung `BuildConfig.ADMOB_{BANNER,NATIVE,INTERSTITIAL,REWARDED,APP_OPEN}_TEST_ID` để nối placement trước khi có id thật.
- Manifest: meta-data `com.google.android.gms.ads.APPLICATION_ID` (đang là app id mẫu — TODO thay), `AdActivity` dùng `@style/AdTheme`, `NATIVE_AD_DEBUGGER_ENABLED=false`.

### 4.4 Composable ad
- Nhận `adUnit: () -> XxxAdUnit` (lambda). Tự lo load/release bằng **1** `DisposableEffect(adUnit()) { adUnit().load(context); onDispose { adUnit().release() } }` — key là **chính unit** (không phải `Unit`) để đổi unit trong cùng slot thì release đúng unit cũ. Compose màn = preload, không có bước `preload()` riêng. Effect khai báo **trước** các `return` sớm theo state.
- `BannerAdView`: thêm `LifecycleResumeEffect(adUnit())` → `resume()`/`pause()`.
- `NativeAdView(adUnit, layoutType, modifier, isCloseVisible, onCloseClick)`: `NativeLayoutType.{MEDIA_4_3, MEDIA_16_9, FULL_SCREEN}` → layout XML `res/layout/native_ad_*.xml`. Card ẩn khi `NONE`/`FAILED`; `FULL_SCREEN` không ẩn (giữ nút đóng). Màu áp lúc runtime từ `MaterialTheme.colorScheme` (XML chỉ là baseline light, không `values-night`), drawable phải `mutate()`. Thêm layout = 1 entry enum + 1 nhánh `when`. Composable trùng tên class `NativeAdView` của Google là cố ý — không alias.
- `AdLoadingDialog(adUnit)`: chỉ hiện khi unit `LOADING`, không tự load.
- Chỗ đặt: ad full-screen load/show ở **Screen** (cần `LocalActivity.current`); banner/native nằm trong UI thì Screen truyền slot `@Composable () -> Unit` xuống Content (Content không `koinInject()`).
- Splash có dòng `may_contain_ads` dưới thanh loading (policy ad lúc mở app).

---

## 5. Ngôn ngữ / locale

- `LanguageValue` (`presentation/model/`): 64 ngôn ngữ (`code` ISO + `flag`), `DEFAULT = ENGLISH`, `TRANSLATED` (ngôn ngữ đã có bản dịch — hiện English, Tiếng Việt), `fromCode(code)`, `displayNamesFor(displayIn, languageManager)`, `sortedForDisplay(deviceLanguageCode, displayNames)` (ngôn ngữ máy → English → theo tên), `labelFor(...)`. `LanguageUiModel` (`toUiModel(languageManager)`): cờ + tên viết bằng chính ngôn ngữ đó.
- Ngôn ngữ đã chọn lưu dạng ISO code ở `DataStoreManager.selectedLangCode` (mặc định `"en"`). `AppLanguageProvider` (bọc `setContent` của `MainActivity` và `AodActivity`) collect → `languageManager.configurationFor(code)` + `resourcesFor(config)` → provide `LocalConfiguration`/`LocalResources` ⇒ `stringResource()` đổi ngôn ngữ ngay, không recreate Activity. Thông báo của `AodService` lấy chuỗi qua `resourcesFor(...)` và đăng lại khi `selectedLangCode` đổi.
- Text UI phải qua `stringResource(...)` hoặc `LocalResources.current.getString(...)` — **không** `context.getString`/`activity.getString` (Context của Activity bỏ qua locale override). Toast: `context.showToast(resources.getString(R.string.x))` với `val resources = LocalResources.current`.
- Ngôn ngữ đang áp dụng trong Compose: `LanguageValue.fromCode(LocalConfiguration.current.locales[0].toLanguageTag())`.
- `LocalConfiguration` bị override từ config của Application (theo ngôn ngữ) → **không** đọc kích thước màn hình từ nó; dùng `LocalWindowInfo` / `BoxWithConstraints`.
- Bản dịch: `res/values-<qualifier>/strings.xml`. Code khác qualifier: `id → values-in`, `he → values-iw`, `zh-hk → values-zh-rHK`, `es-la → values-es-rLA`, `pt-br → values-pt-rBR`, `fil → values-b+fil`; còn lại `values-<code>`. `generateLocaleConfig = true` + `res/resources.properties` (`unqualifiedResLocale=en-US`) sinh danh sách ngôn ngữ cho Android 13+. Thiếu bản dịch → rơi về `values/` (English).
- **Giữ `bundle { language { enableSplit = false } }`** trong `app/build.gradle.kts`: phát hành AAB mà bật split thì máy chỉ nhận ngôn ngữ trùng ngôn ngữ hệ thống → đổi sang ngôn ngữ khác trong app sẽ hiện English (lint `AppBundleLocaleChanges`).
- Màn chọn ngôn ngữ `screens/language/` (`LanguageDestination(isFirstOpen)`): chỉ liệt kê `LanguageValue.TRANSLATED`, ngôn ngữ máy lên đầu rồi tới English. Lựa chọn tạm là state của màn; chỉ nút Xong mới gọi `saveSelectedLangCode(language.code)`.
  - Lần đầu mở app (`isFirstOpen == true`): Splash `navigateTo(LanguageDestination(isFirstOpen = true), preserveState = false)`. Không có nút back, chọn sẵn ngôn ngữ của máy (chưa có bản dịch → English); Nút Tiếp tục: còn thiếu quyền bắt buộc (`requiredAodPermissions()`) thì `navigateTo(PermissionDestination)` (Language vẫn ở dưới stack, Back quay lại bước 1); đủ rồi thì `saveIsFirstOpen(false)` + `navigateTo(MainDestination, preserveState = false)`.
  - Màn Permission (bước 2, `screens/permission/`): chỉ quyền bắt buộc, đọc lại mỗi lần resume; Bắt đầu chỉ bật khi đủ quyền (quyền không đọc được trạng thái — op Xiaomi trả `null` — không chặn), bấm thì đọc lại quyền, đủ mới `saveIsFirstOpen(false)` + `navigateTo(MainDestination, preserveState = false)`. Thoát giữa chừng thì lần sau đi lại từ Language.
  - Các lần mở sau: Splash → Main. AOD đang bật mà thiếu quyền bắt buộc thì Main hiện `MainPermissionSheet`; đóng sheet thì thôi tới lần mở app sau, trong lúc đó còn `MainPermissionWarning` ở đầu Main để mở lại, bật lại công tắc AOD cũng mở lại sheet.
  - Từ màn Main (mục "Ứng dụng › Ngôn ngữ"): `backStack.add(LanguageDestination(isFirstOpen = false))`; Xong chỉ bật khi chọn khác ngôn ngữ đang dùng, lưu xong thì quay lại.
  - Thêm bản dịch: `values-<qualifier>/strings.xml` + thêm vào `LanguageValue.TRANSLATED`.

---

## 6. Quy ước đặt tên

| Thành phần | Quy ước | Ví dụ |
|---|---|---|
| Destination | `XxxDestination` | `MainDestination`, `LanguageDestination(isFirstOpen)` |
| Màn | `XxxScreen` · `XxxContent` · `XxxViewModel` | `MainScreen` |
| MVI | `XxxState` · `XxxIntent` · `XxxEffect`; tên từng Intent/Effect theo [mục 10.2.1](#1021-đặt-tên-intent--effect) | `MainState`, `MainIntent.ToggleAod`, `MainEffect.OpenOverlaySettings` |
| UseCase | `VerbNounUseCase` | `GetDailyMetadataUseCase` |
| Repository | `XxxRepository` / `XxxRepositoryImpl` | |
| DataSource | `XxxDataSource` / `XxxDataSourceImpl` | |
| Manager | `XxxManager` (1 class, không `Impl`) | `NetworkManager` |
| Ad unit / placement | `XxxAdUnit` · placement `<format><Place>` · name `"<format>_<place>_<floor>"` | `interSplash`, `"inter_splash_all"` |
| UiModel | `XxxUiModel` + `fun Xxx.toUiModel()` | |
| Enum giá trị UI | `XxxValue` | `LanguageValue` |
| Component dùng chung (design system, `presentation/components/`) | `AodsXxx` | `AodsSwitchRow`, `AodsLottie`, `AodsNoInternetDialog` |
| Package | lowercase, nhiều từ → snake_case | `ad_unit` |
| Token | Primitive `AodsPrimitive<Loại>.<Tên>` (giá trị thô) · semantic `Aods<Loại>Tokens` · component/domain `Aods<Component>Tokens` · `default…` / `darkAodsColors` · `LocalAods<Tên>` · đọc qua `AodsTheme.<tên>` | `AodsPrimitiveColors.Mint`, `AodsTheme.colors.primary`, `AodsTheme.clock.text` |
| String | snake_case theo nội dung; prefix màn khi trùng/mơ hồ | `no_internet_connection` |
| Hằng số | `UPPER_SNAKE_CASE` `const val` (`companion object` trong class, `private const val` trong file **không phải Compose**; file Compose không có hằng số top-level — xem mục 15) | `LOAD_TIMEOUT` |

---

## 7. Dependency (thư viện)

### 7.1 Stack

| Nhóm | Thư viện | Rule |
|---|---|---|
| UI | Compose BOM 2026.09.00 · Material3 · material-icons-extended · ui-tooling | Lib Compose **không ghi version** — BOM quyết định |
| Navigation | navigation3-runtime / -ui 1.2.0 · lifecycle-viewmodel-navigation3 2.11.0 | KHÔNG navigation-compose |
| DI | Koin 4.2.2 (`koin-android`, `koin-androidx-compose`) | KHÔNG Hilt/Dagger |
| Async | kotlinx-coroutines-android 1.11.0 | Flow/StateFlow/SharedFlow — KHÔNG LiveData/RxJava |
| Lifecycle | lifecycle-runtime-ktx / -runtime-compose / -viewmodel-compose 2.11.0 · activity-compose 1.13.0 · core-ktx 1.19.1 | |
| Storage | datastore-preferences 1.2.1 | Qua `DataStoreManager` |
| Ads | play-services-ads 25.5.0 · user-messaging-platform 4.0.0 · constraintlayout 2.2.2 (layout NativeAdView) | Qua `AdsManager` |
| Collections | kotlinx-collections-immutable 0.5.2 | `ImmutableList` trong State/UiModel |
| Serialization | plugin `kotlin-serialization` · kotlinx-serialization-json 1.11.0 | `@Serializable` cho NavKey và DTO của API |
| Network | retrofit 3.0.0 · converter-kotlinx-serialization (cùng version Retrofit) | Chỉ trong `data/network/api` + `networkModule`; OkHttp đi kèm Retrofit, không khai báo riêng |
| Log | Timber 5.0.1 | KHÔNG `Log.*`/`println` |
| Animation | lottie-compose 6.7.1 | Chỉ qua `AodsLottie` — không gọi `LottieAnimation` trực tiếp |
| Test | junit 4.13.2 · kotlinx-coroutines-test · androidx.test · compose ui-test | |

### 7.2 Rule thêm / sửa dependency
1. Mọi version ở `gradle/libs.versions.toml`: version key camelCase (`[versions]`), alias kebab-case `group-artifact` (`[libraries]`), plugin ở `[plugins]` → dùng `libs.xxx` / `alias(libs.plugins.xxx)`. **Không inline version.**
2. Lib AndroidX Compose không ghi version (theo BOM). Nâng Compose = nâng BOM.
3. Ưu tiên lib chính chủ (AndroidX, Kotlin, Google); lib mới phải có lý do + hỏi trước; không thêm lib trùng chức năng.
4. R8: không thêm keep rule rộng cho lib đã có consumer rules (Koin, Coroutines, Compose, Lottie, kotlinx.serialization, Navigation 3, AdMob…). `proguard-rules.pro` chỉ chứa rule cần thiết (hiện: strip call static `Timber.v/d/i`). Đổi rule → test release: điều hướng, khôi phục back stack sau process death, load ad.
5. Secret / id theo môi trường (keystore, test device id, API key) → `local.properties` (gitignore) → `localProperties.getProperty("…")` trong `app/build.gradle.kts` → `signingConfigs` / `buildConfigField`. Ad unit id thật khai báo trong `release {}` của `app/build.gradle.kts`.

---

## 8. Domain layer

- **Model** — `data class` bất biến (`val`), thuần Kotlin. Logic thuần của model đặt trong model/`companion object` — VM gọi, không viết lại.
- **Repository** — `interface XxxRepository`. Lấy 1 lần → `suspend fun`; dữ liệu đổi theo thời gian → `fun observeXxx(): Flow<T>`. Default param khai báo ở interface, Impl không khai lại. Không lộ type của data (DTO, Entity).
- **UseCase** — `VerbNounUseCase`, 1 class / 1 nghiệp vụ, constructor nhận interface repository:

```kotlin
class GetXxxUseCase(private val repository: XxxRepository) {
    suspend operator fun invoke(id: Int): Result<Xxx> = suspendRunCatching { repository.getXxx(id) }
}

class ObserveXxxUseCase(private val repository: XxxRepository) {
    operator fun invoke(): Flow<List<Xxx>> = repository.observeXxx()
}
```

| Loại | Chữ ký | Bắt lỗi |
|---|---|---|
| 1 lần (suspend) | `suspend operator fun invoke(...): Result<T>` | `suspendRunCatching { }` — **không** `runCatching` (nuốt `CancellationException`) |
| đồng bộ | `operator fun invoke(...): Result<T>` | `runCatching { }` |
| observe | `operator fun invoke(...): Flow<T>` | **không** bọc `Result` (lỗi Flow là terminal) — VM dùng `collectCatching` |

- UseCase không giữ state, không chọn dispatcher, không biết UI (`Context`, `@StringRes`, UiModel). Logic dùng chung nhiều VM → UseCase; UseCase được gọi UseCase khác.

## 9. Data layer

- `XxxRepositoryImpl(...) : XxxRepository` trong `data/repository/`; nguồn dữ liệu ở `data/local/…`, `data/network/…`, `data/source/…`.
- **Main-safe tại Repository/Manager**, đổi dispatcher **qua `withContextCatching(context = Dispatchers.IO, action = { … }, catch = { … })`** (IO; CPU → `Dispatchers.Default`) — không `withContext` thô ngoài `CoroutineExt.kt` (grep [mục 18](#18-banned-patterns)). Hàm suspend của Retrofit đã main-safe → `withContextCatching(action, catch)` không truyền `context`. UseCase/ViewModel không tự đổi dispatcher.
- **Không có exception riêng của domain.** Lỗi đã biết của nguồn dữ liệu (mất quyền, mất mạng, chưa định vị được, API lỗi) xử lý ngay ở Impl, trong `catch` của `withContextCatching`: `Timber.w` + giá trị dự phòng có nghĩa (danh sách rỗng, giữ bản đã lưu) — `CalendarRepositoryImpl.getEvents`, `WeatherRepositoryImpl.refreshWeather`. Lỗi lọt ra ngoài vẫn được UseCase bọc `Result` (`suspendRunCatching`). Không trả `null` để báo lỗi.
- Observe từ callback → `callbackFlow { …; awaitClose { unregister } }` + `distinctUntilChanged()`; flow cần sống tiếp sau lỗi → `recoverCatching { emit(default) }` trước `stateIn`.
- Mapper DTO/Entity → domain đặt trong `data` (extension `toDomain()` cạnh DTO hoặc `object XxxMapper`).

---

## 10. Presentation — MVI

Luồng một chiều: Content gửi `Intent` → ViewModel → `updateState { }` (State) / `sendEffect()` (Effect) → Screen collect → Content render.

### 10.1 `BaseViewModel<S, I, E>`

| API | Dùng để |
|---|---|
| `state: StateFlow<S>` | UI đọc (`collectAsStateWithLifecycle()`) |
| `effect: SharedFlow<E>` | Sự kiện 1 lần (Screen collect trong `LaunchedWithLifecycleEffect`) |
| `abstract fun onIntent(intent: I)` | Điểm vào DUY NHẤT từ UI |
| `protected fun updateState(block: S.() -> S)` | Đổi state atomic |
| `protected suspend fun sendEffect(effect: E)` | Gọi trong `viewModelScope.launch { }` |

### 10.2 State · Intent · Effect (mỗi loại 1 file trong `screens/<feature>/state/`)

```kotlin
@Immutable
data class XxxState(
    val isLoading: Boolean = true,
    val items: ImmutableList<XxxUiModel> = persistentListOf(),
    val showDeleteDialog: Boolean = false,
    val error: String? = null,
)

sealed interface XxxIntent {
    data object Refresh : XxxIntent
    data class SelectItem(val id: Int) : XxxIntent
    data object ShowDeleteDialog : XxxIntent
    data object DismissDeleteDialog : XxxIntent
}

sealed interface XxxEffect {
    data class NavigateToDetail(val id: Int) : XxxEffect
    data class ShowMessage(@param:StringRes val messageRes: Int) : XxxEffect
}
```

- **State**: `@Immutable data class`, mọi field có default, list = `ImmutableList`, chỉ type stable. Nguồn sự thật duy nhất của UI — dialog/bottom sheet đang mở cũng là field (`showXxx`) + cặp Intent `ShowXxx`/`DismissXxx`.
- **Intent**: `sealed interface`, đặt theo hành động user; `data object` khi không có tham số.
- **Effect**: `sealed interface`, chỉ cho việc 1 lần không thuộc state (navigate, toast, mở Intent hệ thống, show ad). Màn không có effect → `E = Nothing`.
- Effect là `SharedFlow` không replay: phát khi không có collector (app ở nền) sẽ **mất** → thứ bắt buộc user phải thấy thì đưa vào State.

#### 10.2.1 Đặt tên Intent / Effect

Mục tiêu: đọc tên là biết việc, không phải mở ViewModel ra xem. **Động từ đứng đầu, có đối tượng**; cùng một loại việc thì luôn dùng cùng một động từ.

**Intent** — user vừa làm gì (hoặc Screen báo lại kết quả từ hệ thống):

| Loại | Mẫu | Ví dụ |
|---|---|---|
| Công tắc | `Toggle<Tính năng>(isEnabled)` — mang giá trị mới, ViewModel không tự đảo | `ToggleAod`, `ToggleSchedule` |
| Đổi một giá trị (thanh trượt, radio, chọn giờ) | `Change<Giá trị>(value)` | `ChangeBrightness(percent)`, `ChangeClockFace(face)`, `ChangeScheduleTime(time, minuteOfDay)` |
| Chọn 1 mục, chưa lưu | `Select<Mục>(item)` | `SelectLanguage(language)` |
| Nút xác nhận (Xong, OK) | `Confirm<Thứ được xác nhận>` | `ConfirmLanguage` |
| Sang màn khác trong app · quay lại | `NavigateTo<Màn>` · `NavigateBack` | `NavigateToLanguage` |
| Mở thứ ngoài màn (trang hệ thống, picker, Activity khác) | `Open<Đích>`; trang cài đặt kết thúc bằng `Settings` | `OpenPermissionSettings(permission)`, `OpenBackgroundPicker`, `OpenPreview` |
| Dialog là field của State | `Show<Dialog>` / `Dismiss<Dialog>` | `ShowScheduleTimePicker` / `DismissScheduleTimePicker` |
| Lệnh khác | `<Động từ><Đối tượng>` | `RemoveBackground`, `RefreshPermissions`, `PlayPauseMedia`, `SkipToNextTrack` |
| Thao tác / phím mà user tự gán hành động | `PerformGesture(gesture)` — ViewModel tra hành động đã gán | `PerformGesture(AodGestureValue.SWIPE_UP)` |
| Screen báo kết quả từ hệ thống (ActivityResult, hộp thoại quyền) | `<Thứ>Result(value)` | `NotificationPermissionResult(isGranted)`, `BackgroundPickerResult(uri)` |
| Screen báo đã hiện thứ State yêu cầu | `<Thứ>Shown` | `NotificationPermissionDialogShown` |

**Effect** — việc Screen phải làm ngay, động từ mệnh lệnh:

| Loại | Mẫu | Ví dụ |
|---|---|---|
| Điều hướng trong app | `NavigateTo<Màn>` · `NavigateBack` | `NavigateToLanguage`, `NavigateToMain` |
| Mở thứ ngoài màn | `Open<Đích>`; trang cài đặt kết thúc bằng `Settings` | `OpenOverlaySettings`, `OpenMiuiPermissionSettings`, `OpenNotificationAccessSettings`, `OpenBackgroundPicker`, `OpenPreview` |
| Hộp thoại quyền của hệ thống | `Request<Quyền>Permission` | `RequestNotificationPermission` |
| Service | `Start<Service>` / `Stop<Service>` | `StartAodService`, `StopAodService` |
| Toast | `ShowMessage(@StringRes messageRes)` | |
| Đóng màn đang hiện | `Close<Màn>` | `CloseAod` |

Quy ước chung:
- Intent chỉ để phát ra một Effect thì **trùng tên** Effect đó (`OpenPreview` → `OpenPreview`, `NavigateBack` → `NavigateBack`): đọc Screen là thấy Intent nào dẫn tới Effect nào.
- Không dùng: danh từ trơn (`Preview` → `OpenPreview`), nhãn nút (`Done` → `ConfirmLanguage`, `Back` → `NavigateBack`), thì quá khứ cho việc user làm (`BackgroundPicked` → `BackgroundPickerResult`), thiếu đối tượng (`Close` → `CloseAod`), hai động từ cho cùng một việc (`Pick…` rồi `Launch…` → đều là `Open…`).
- Hàm private xử lý Intent trong ViewModel cùng tên với Intent: `ConfirmLanguage` → `confirmLanguage()`, `OpenPermissionSettings` → `openPermissionSettings()`; Intent kết quả → `on<Thứ>Result()` / `on<Thứ>Shown()`.
- Tham số: Boolean là `isXxx`; số có đơn vị trong tên (`percent`, `minutes`, `minuteOfDay`); enum theo tên kiểu (`face`, `rule`, `language`). Field Boolean của State theo cùng động từ: `isConfirmEnabled` cho nút của `ConfirmLanguage`.

### 10.3 ViewModel

```kotlin
class XxxViewModel(
    private val getXxx: GetXxxUseCase,
    private val observeYyy: ObserveYyyUseCase,
) : BaseViewModel<XxxState, XxxIntent, XxxEffect>(initialState = XxxState()), Tag {

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            observeYyy().collectCatching(
                action = { yyy -> updateState { copy(yyy = yyy.toUiModel()) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    override fun onIntent(intent: XxxIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        when (intent) {
            is XxxIntent.Refresh -> load()
            is XxxIntent.SelectItem -> viewModelScope.launch { sendEffect(XxxEffect.NavigateToDetail(intent.id)) }
            is XxxIntent.ShowDeleteDialog -> updateState { copy(showDeleteDialog = true) }
            is XxxIntent.DismissDeleteDialog -> updateState { copy(showDeleteDialog = false) }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            getXxx()
                .onSuccess { data ->
                    updateState { copy(isLoading = false, items = data.map { it.toUiModel() }.toImmutableList()) }
                }
                .onFailure { e -> updateState { copy(isLoading = false, error = e.message) } }
        }
    }
}
```

- Constructor nhận UseCase (+ Manager khi cần state hạ tầng). KHÔNG nhận `Context` Activity, `NavBackStack`, Repository, DataSource, `AdsManager`.
- `onIntent` là cửa duy nhất UI gọi vào; `when (intent)` exhaustive, không `else`. Hàm public khác chỉ để nhận args khởi tạo ([11.4](#114-truyền-args)).
- Chỉ đổi state qua `updateState { copy(...) }`; đọc state hiện tại bằng `state.value`.
- Tác vụ có thể bị gọi chồng → giữ `Job?` và `cancel()` job cũ trước khi launch.
- Flow của UseCase/manager → `collectCatching(action = …, catch = …)` (tham số đặt tên, `action` trước; cần return sớm trong catch → nhãn `catch@`) — chọn helper theo [mục 13.1](#131-chọn-helper--bắt-buộc-cho-code-mới).
- Map domain → UiModel trong VM (`toUiModel()`), không map trong Content. Text hiển thị cho user → `@StringRes` (Effect `ShowMessage` hoặc field `@StringRes` trong State).

### 10.4 Screen vs Content

```kotlin
@Composable
fun XxxScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val viewModel: XxxViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is XxxEffect.NavigateToDetail -> backStack.add(DetailDestination(effect.id))
                is XxxEffect.ShowMessage -> context.showToast(resources.getString(effect.messageRes))
            }
        }
    }

    XxxContent(state = state, onIntent = viewModel::onIntent)
}
```

| File | Trách nhiệm | Không được |
|---|---|---|
| `XxxScreen.kt` | Lấy VM (Koin), collect `state` + `effect`, điều hướng, lấy manager bằng `koinInject()`, load/show ad full-screen, side effect, gửi Intent từ callback ngoài UI | Vẽ UI chi tiết |
| `XxxContent.kt` | UI thuần: nhận `state` + `onIntent` (+ slot ad nếu có); sub-composable `private` cùng file | Biết ViewModel/Koin (`koinInject`, `koinViewModel`)/NavBackStack/Activity; gọi UseCase |
| `XxxViewModel.kt` | Logic UI, gọi UseCase, giữ State | Import Compose UI, `Context`, build text hiển thị |

- **Lifecycle effect**: collect effect/flow 1 lần → `LaunchedWithLifecycleEffect { }` (`presentation/effects/` — bọc `repeatOnLifecycle(STARTED)`: chạy khi ≥ STARTED, huỷ khi < STARTED, tự restart khi quay lại; không cần tự quản `Job`). Việc gắn với RESUMED (show ad full-screen khi quay lại, `resume()/pause()` banner, reload khi màn resume) → `LifecycleResumeEffect`. Luôn collect flow theo lifecycle qua helper này — KHÔNG rải `repeatOnLifecycle` thô trong từng Screen.
- Màn không có state/logic riêng (Splash) được phép chỉ có Screen + Content.
- Content dài → tách `XxxYyySection.kt` cùng folder màn; dùng ≥ 2 màn → `presentation/components/`.
- Content thuần nên `@Preview` được (preview `private`, bọc `AodsTheme`, `XxxState(...)` mẫu, `onIntent = {}`; theme luôn tối nên không cần preview sáng).

### 10.5 UiModel & giá trị UI
- `@Immutable data class XxxUiModel` ở `presentation/model/`, mapper extension cùng file `fun Xxx.toUiModel()`; list con → `.toImmutableList()`.
- Enum giá trị UI `XxxValue` (vd `LanguageValue`) và type UI dùng chung (`AnimationContentKey { Loading, Error, Content }` làm `targetState` cho `AnimatedContent`) cũng ở `presentation/model/`.

---

## 11. Navigation 3

Lib: `navigation3-runtime`, `navigation3-ui`, `lifecycle-viewmodel-navigation3`. KHÔNG navigation-compose (`NavHost`, `NavController`, route string).

### 11.1 Destination
- `presentation/navigation/AppDestinations.kt`: `@Serializable` + `: NavKey`, tên `XxxDestination`. Không args → `data object`; có args → `data class` với field primitive/`@Serializable` (back stack lưu qua process death). Không nhét domain model — chỉ id/primitive.

### 11.2 NavDisplay
- `AppNavDisplay` = `rememberNavBackStack(SplashDestination)` + `NavDisplay(entries = rememberDecoratedNavEntries(...))` với **đủ 2 decorator**: `rememberSaveableStateHolderNavEntryDecorator()` (thiếu → mất `rememberSaveable`) + `rememberViewModelStoreNavEntryDecorator()` (thiếu → ViewModel không clear khi pop).
- Màn mới = thêm `entry<XxxDestination> { dest -> XxxScreen(...) }`. `onBack` chỉ pop khi `backStack.size > 1`.
- Root `AppNavDisplay` chỉ chứa màn full-screen; tab nằm ở NavDisplay lồng ([11.5](#115-nested-navigation-bottom-tab)). `AodsNoInternetDialog` render sau `NavDisplay`.

### 11.3 Điều hướng
**Chỉ Screen (hoặc host NavDisplay) đụng `backStack`** — ViewModel bắn Effect, Screen thực thi.

| Mục đích | Gọi |
|---|---|
| Thay cả stack (Splash → Main, logout, xong onboarding) | `backStack.navigateTo(dest, preserveState = false)` |
| Đổi tab / single-top | `backStack.navigateTo(dest)` — đã có → pop về đó (args khác thì thay); chưa có → stack thành `[root, dest]` |
| Mở màn chi tiết chồng lên | `backStack.add(dest)` |
| Quay lại | `backStack.removeLastOrNull()` |

⚠️ `navigateTo(dest)` (preserveState = true) **xoá mọi màn giữa root và dest** — mở màn chi tiết nhiều tầng dùng `backStack.add`. Double-tap đã được chặn bởi debounce của `Modifier.onClick`.

### 11.4 Truyền args
1. Entry đọc args từ `dest` → Screen: `entry<DetailDestination> { dest -> DetailScreen(id = dest.id) }`.
2. Vào ViewModel: VM gắn entry (`koinViewModel`) → `koinViewModel { parametersOf(id) }` + `viewModel { (id: Int) -> DetailViewModel(id, get()) }` (đang dùng: `LanguageScreen` → `LanguageViewModel(isFirstOpen, …)`); VM sống lâu hơn entry (`koinActivityViewModel`) → hàm `setXxx(arg)` gọi trong `LaunchedEffect(arg)`.

### 11.5 Nested navigation (bottom tab)

```kotlin
@Composable
fun MainScreen() {
    val backStack = rememberNavBackStack(HomeDestination)

    Scaffold(
        bottomBar = {
            AppBottomNavBar(
                currentDestination = backStack.lastOrNull(),
                onNavigateTo = { backStack.navigateTo(it) },
            )
        },
    ) { innerPadding ->
        MainNavDisplay(backStack = backStack, modifier = Modifier.padding(innerPadding))
    }
}
```

- `MainNavDisplay(backStack)` = `NavDisplay` (đủ 2 decorator) chỉ chứa `entry<>` của tab; `onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }`.
- `enum class Tab(val destination: NavKey, val icon: ImageVector, @param:StringRes val labelRes: Int)` private trong `AppBottomNavBar`; tab đang chọn so theo `::class`.
- Đổi tab pop entry tab khác → VM tab cần giữ state dùng `koinActivityViewModel()`.

### 11.6 ViewModel scope

| Lấy VM bằng | Gắn với | Dùng khi |
|---|---|---|
| `koinViewModel()` | NavEntry — clear khi entry bị pop | Mặc định |
| `koinActivityViewModel()` | Activity | Màn tab giữ state khi đổi tab; VM dùng chung nhiều màn |

---

## 12. DI — Koin

- `di/AppModule.kt`: `managerModule` · `networkModule` · `adsModule` · `aodModule` · `repositoryModule` · `useCaseModule` · `viewModelModule` → `appModules`, load ở `App.onCreate()` (`startKoin { androidContext(this@App); modules(appModules) }`).

| Loại | Khai báo | Lý do |
|---|---|---|
| Manager | `single { XxxManager(androidApplication()) }` | State holder, 1 instance |
| AdsManager | `single { AdsManager() }` | `Application`/`NetworkManager` lấy trong `AdsManager` qua `by inject()`; consent bắt đầu từ `MainActivity` (`requestConsent`) |
| AodSession | `single { AodSession(get()) }` (`aodModule`) | State dùng chung giữa `AodService` và `AodActivity` (mục 20) |
| Network (`networkModule`) | `single { Json { ignoreUnknownKeys = true } }` · `single { Retrofit.Builder()…addConverterFactory(get<Json>().asConverterFactory(…)).build() }` · `single { get<Retrofit>().create(XxxApiService::class.java) }` | 1 client cho cả app; API trả thừa trường không làm lỗi |
| DataSource / Repository | `single<XxxRepository> { XxxRepositoryImpl(get()) }` | Giữ cache/kết nối |
| UseCase | `factory { GetXxxUseCase(get()) }` | Không state, rẻ |
| ViewModel | `viewModel { XxxViewModel(get()) }` · có args: `viewModel { (isPreview: Boolean) -> AodViewModel(isPreview, get(), …) }` | Theo ViewModelStore |

- Constructor injection ở mọi layer; không `KoinComponent` trong domain/data. Ngoại lệ: `AdsManager` là `KoinComponent` (`by inject()` `Application` + `NetworkManager`), `AdUnit` là `KoinComponent` (`by inject()` `AdsManager`) — placement khai báo gọn `XxxAdUnit(floors = …)`, không truyền dependency qua constructor; `BootReceiver` là `KoinComponent` vì hệ thống tạo receiver và Koin không có `by inject()` cho `BroadcastReceiver`.
- Activity / Service: `private val x: X by inject()` (`org.koin.android.ext.android.inject`). Composable: manager qua `koinInject()` tại nơi dùng (Screen/host), không truyền manager xuống Content; VM qua `koinViewModel()`/`koinActivityViewModel()` chỉ ở Screen.
- Tên class `XxxRepositoryImpl` / `XxxDataSourceImpl` chỉ xuất hiện trong `di/` và `data/`.

## 13. Coroutines — `utils/CoroutineExt.kt`

| Hàm | Trả về | Bắt | Dùng ở |
|---|---|---|---|
| `suspendRunCatching { }` | `Result<T>` | `Throwable` | UseCase 1 lần; lời gọi suspend 1 lần có thể ném mà nơi gọi cần `Result` |
| `withContextCatching(context, action, catch)` | `T` | `Exception` | Repository / Manager (đổi dispatcher + log lỗi, trả giá trị dự phòng): `DataStoreManager.edit`, init MobileAds của `AdsManager`, `AodImageManager`, `DeviceLocationManager`, `CalendarRepositoryImpl.getEvents`, `WeatherRepositoryImpl.refreshWeather` |
| `Flow<T>.collectCatching(action, catch)` | — (terminal) | `Exception` từ upstream **và** thân `action` | **Mọi** chỗ collect flow của manager/UseCase: ViewModel, Service, TileService, manager (`AdsManager` collect `isAvailable`) |
| `Flow<T>.collectLatestCatching(action, catch)` | — (terminal) | Như `collectCatching`; khối cũ bị huỷ khi có giá trị mới không tính là lỗi | Collect phải huỷ khối đang chạy khi có giá trị mới (chờ rồi mới làm): `AodViewModel.observeProximity`, `observeAmbientLight` |
| `Flow<T>.recoverCatching { }` | `Flow<T>` (intermediate) | `Throwable` | Flow phải sống tiếp: trước `shareIn`/`stateIn` trong manager (`callbackFlow` của receiver/cảm biến, `prefs.data`) |

- Hậu tố **`-Catching` = rethrow `CancellationException`, bắt phần còn lại**. Vì vậy ViewModel/Repository **không** tự viết `catch (c: CancellationException) { throw c }` — gọi helper. Ngoại lệ duy nhất: ad unit (phải bắt `TimeoutCancellationException` trước).

### 13.1 Chọn helper — bắt buộc cho code mới

- **Collect flow của manager/UseCase → `collectCatching`, không `.collect { }` thô** — trong ViewModel (`init`), `AodService.onCreate`, `AodTileService.onStartListening`… Lỗi ở `action` hay upstream chỉ dừng collector đó và được log; collect thô thì lỗi lọt ra scope (`SupervisorJob` không có handler → crash app). Mẫu, lớp cần log implement `Tag`:
  ```kotlin
  viewModelScope.launch {
      batteryStateManager.isPlugged.filterNotNull().collectCatching(
          action = { isPlugged -> … },
          catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
      )
  }
  ```
- Cần huỷ khối đang chạy khi có giá trị mới (chờ một lúc rồi mới làm) → `collectLatestCatching(action = …, catch = …)`, không `.collectLatest { }` thô: chờ che 3 giây của tiệm cận, chờ ánh sáng ổn định 2 giây.
- Chỉ 2 chỗ được collect thô (grep ở [mục 18](#18-banned-patterns) loại trừ đúng 2 chỗ này):
  - `viewModel.effect.collect { }` trong `LaunchedWithLifecycleEffect` của Screen (khung [mục 10.4](#104-screen-vs-content)): `SharedFlow` của `BaseViewModel` không ném lỗi.
  - `snapshotFlow { … }.collectLatest { }` trong `Modifier.onClick` (`components/Modifiers.kt`, code của base): chỉ đọc state Compose, không ném lỗi.
- Đọc giá trị của manager **1 lần** → `.value` (đồng bộ, `null` → `DEFAULT_*`) hoặc `filterNotNull().first()` (chờ nạp xong); không cần bọc vì `StateFlow` không ném lỗi, lỗi đọc đã được manager `recoverCatching`.
- Manager: nguồn `callbackFlow` / `prefs.data` → `recoverCatching { … }` trước `shareIn`/`stateIn`; ghi hoặc IO → `withContextCatching(…)` trong `scope.launch` của manager.
- Lời gọi suspend 1 lần có thể ném → `suspendRunCatching { }.onSuccess { }.onFailure { }` — không `try/catch`, không `runCatching`.
- **RepositoryImpl dùng helper như Manager:** đổi dispatcher hoặc gọi nguồn có thể ném (`ContentResolver`, Retrofit, vị trí) → `withContextCatching(context = …, action = { … }, catch = { e -> Timber.tag(tag).w(…); giá trị dự phòng })`; lớp implement `Tag` để log. Không `withContext` / `try-catch` thô trong Impl.
- Hàm **đồng bộ** của hệ thống ném exception có tên (vd `startForegroundService` ném `ForegroundServiceStartNotAllowedException`) → `try/catch` đúng loại đó như `AodService.start()`; helper `-Catching` là hàm suspend, không dùng ở đây.
- Scope: `viewModelScope` (VM) · `LaunchedEffect`/`rememberCoroutineScope`/`LaunchedWithLifecycleEffect` (Compose) · `lifecycleScope` (Activity) · manager/ad unit tự tạo scope · `AodService` tự tạo scope `Dispatchers.Main.immediate`, huỷ ở `onDestroy` · `BootReceiver` tạo scope ngắn cho `goAsync()`.
- Dispatcher chọn ở Repository/Manager, không ở VM/UseCase.
- Banned: `GlobalScope`, `runBlocking` trong code app, `Thread.sleep`.

## 14. Compose & theme

- **Design system `Aods`, luôn tối** (bảng màu của FakeAOD — app nói về một màn hình phần lớn thời gian tắt, nên nền đen chứ không phải xám tối), token 3 tầng theo skill `design-tokens` (mục 21), trong `presentation/theme/`:
  - **Primitive** `tokens/AodsPrimitive*` — giá trị thô: `Colors` (màu FakeAOD + 3 màu trung tính mặc định của Material 3 mà app vẫn dùng), `Spacing`, `IconSize`, `Shape`, `Border`, `Typography`, `Motion`, `Opacity`, `Elevation`. Hex chỉ có ở `AodsPrimitiveColors.kt`.
  - **Semantic** `Aods{Colors,Spacing,Shape,Typography,Motion,Opacity,Elevation}Tokens` — tên theo vai trò (`colors.primary`, `spacing.screenPadding`, `typography.bodyLarge`…); mỗi loại 1 `@Immutable data class` + giá trị `default…` (màu: `darkAodsColors`, chỉ một bộ) + `LocalAods…` (`staticCompositionLocalOf`).
  - **Component / domain** — giá trị riêng của một component hay một màn: `AodsSettingsRowTokens`, `AodsPickerTokens`, `AodsDrawingPadTokens`, `AodsClockTokens` (màn AOD), `AodsSplashTokens`, `AodsOnboardingTokens` (Language, Permission, thẻ quyền, sheet và dòng cảnh báo quyền), `AodsAdTokens`.
  - `AodsTheme { }` cấp mọi `LocalAods…` và bọc `MaterialTheme` (cầu nối màu, chữ, bo góc) để component Material 3 khớp token. Đọc token bằng `AodsTheme.colors/.spacing/.shapes/.typography/.motion/.opacity/.elevation/.settingsRow/.picker/.drawingPad/.clock/.splash/.onboarding/.ad`. Không dynamic color, không theo sáng/tối của hệ thống. Chữ của app là **Inter** (`res/font/inter_variable.ttf`, bản variable của Google Fonts, giấy phép OFL; `AodsPrimitiveTypography.FontFamilyInter`, độ đậm chọn bằng trục `wght`) cho mọi style, cả style Material 3 không có token (cầu nối đổi font cho đủ 15 style); cỡ chữ theo type scale mặc định của Material 3, riêng `bodyLarge` của app. Màn AOD không dùng font này (font đồng hồ do user chọn, mục 20).
  - XML `Theme.App` có `windowBackground` đen để không loé trắng trước khung Compose đầu tiên. Splash của app phát `R.raw.lottie_device_edge_light` qua `AodsLottie` cỡ 288dp (`AodsTheme.splash.iconSize`) đúng giữa cửa sổ, trong lúc file đang nạp thì vẽ `ic_launcher_foreground` (placeholder): splash hệ thống từ Android 12 vẽ đúng lớp foreground đó, cỡ đó (icon thích ứng có nền đen trùng nền cửa sổ), và khung đầu của Lottie trùng icon tĩnh, nên chuyển sang Splash của app icon không nhảy rồi mới có viền sáng chạy và aura. Tên app và câu giới thiệu hiện dần, nằm dưới tâm một đoạn `textOffset`; dưới đáy là thanh tải và dòng `may_contain_ads`.
- **UI chỉ đọc token qua `AodsTheme.*`**: không `MaterialTheme.*`, không số `dp`/`sp`, thời lượng animation hay alpha viết thẳng, không `CircleShape`/`RoundedCornerShape`, không hex (grep mục 18). Thiếu token → thêm vào đúng tầng trong `presentation/theme/tokens/` (giá trị thô ở Primitive, tên theo vai trò ở Semantic, giá trị chỉ một component/màn dùng ở token component) rồi mới dùng. Dùng thẳng được: `Color.Transparent`, `1f`/`0f`, số hình học trong `Canvas` (góc, tỉ lệ kim), `maxLines`. Hàm không phải composable (vd vẽ bitmap) nhận token qua tham số.
- Màn AOD (`screens/aod/`) **không bọc `AodsTheme`**, cả lúc chạy lẫn `@Preview`: token đọc từ giá trị mặc định của `LocalAods…` — `AodsTheme.clock`: chữ `text` (Grey8A), `textMuted` (Grey6E), `textHint` (Grey5A), nút `control` (GreyB4) — xám chứ không trắng: ít sáng, ít tốn pin, ít burn-in; màu giờ theo `ClockColorValue` user chọn; cỡ chữ nhân `appearance.scale`. Bọc theme thì `Text` nhận `LocalTextStyle` = `bodyLarge` (lineHeight, letterSpacing) và đồng hồ 76sp bị đè lên dòng ngày, nên cỡ chữ của màn này là `TextUnit` (`fontSize = AodsTheme.clock.…`), không phải `TextStyle`.
- `MainActivity`: `enableEdgeToEdge(SystemBarStyle.dark(…))` (theme luôn tối nên icon thanh hệ thống luôn sáng), khoá dọc, `ComposeUiFlags.isBypassUnfocusableComposeViewEnabled = false` (đặt trước `super.onCreate`, giữ như DexReader), Manifest `adjustResize` → mỗi màn tự xử lý inset (`Scaffold` innerPadding, `navigationBarsPadding()`, `imePadding()`).
- **Stability**: State/UiModel `@Immutable`; list → `ImmutableList`; truyền `viewModel::onIntent`; không truyền `MutableState`/ViewModel/`NavBackStack` xuống Content.
- `remember { }` cache; `remember(key) { }` khi input là tham số; `derivedStateOf { }` **chỉ** khi input là Compose `State`:
  ```kotlin
  val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } } // ✅ input là State
  val isEmpty = remember(items) { items.isEmpty() }                                 // ✅ input là tham số
  ```
- Side effect chỉ trong `LaunchedEffect` / `DisposableEffect` / `LaunchedWithLifecycleEffect` / `LifecycleResumeEffect`; dọn dẹp ở `onDispose` / `onPauseOrDispose` (helper lifecycle tự huỷ coroutine khi rời STARTED).
- State đổi phải có animation (`AnimatedVisibility`, `AnimatedContent`, `animate*AsState`, `Crossfade`), không snap.
- Component nhận `modifier: Modifier = Modifier` (tham số optional đầu tiên) và áp vào node gốc.

## 15. Reuse — có sẵn, dùng trước khi viết mới

| Cần | Dùng | File |
|---|---|---|
| Lớp cơ sở MVI | `BaseViewModel<S, I, E>` | `presentation/base/` |
| Collect effect/flow theo lifecycle (STARTED) | `LaunchedWithLifecycleEffect { }` | `presentation/effects/` |
| Click (scale 0.95 + ripple + **debounce 300ms**, clip khi truyền shape) | `Modifier.onClick(shape = …, ripple = …) { }` | `components/Modifiers.kt` |
| Skeleton loading | `Modifier.shimmerLoading(backgroundColor, shimmerColor, shape, isEnable)` · `Modifier.shimmerHighlight(...)` | `components/Modifiers.kt` |
| Nền mờ dần (sau nút đáy) | `Modifier.blurBackground(alphas = persistentListOf(0f, 0f, 1f, 1f))` | `components/Modifiers.kt` |
| Dialog mất mạng | `AodsNoInternetDialog()` (đã gắn ở `AppNavDisplay`) | `components/dialog/` |
| Dòng cài đặt: tiêu đề mục, nhãn nhóm, công tắc (kèm mô tả, tắt khi máy không hỗ trợ), radio (chữ theo font tuỳ chọn), dòng giá trị bấm được, thanh trượt theo nấc | `AodsSectionHeader` · `AodsSectionLabel` · `AodsSwitchRow(description, isEnabled)` · `AodsRadioRow(labelFontFamily)` · `AodsValueRow(description)` · `AodsSliderRow(valueRange, step)` | `components/AodsSettingsRows.kt` |
| Animation Lottie (lặp vô hạn; `placeholder` vẽ trong lúc file đang nạp) | `AodsLottie(resId = R.raw.x, modifier = …, placeholder = { … })`. Có sẵn `R.raw.lottie_device_edge_light`: điện thoại tắt màn, vệt sáng chạy quanh viền máy, aura thở phía sau, đồng hồ xếp chồng — cùng khung 108 với icon app | `components/AodsLottie.kt` |
| Onboarding: tiêu đề có chỉ báo bước · chân trang một nút chính (kèm dòng nhắc khi nút bị khoá) | `AodsOnboardingHeader(step, title, subtitle)` · `AodsOnboardingFooter(actionLabel, isActionEnabled, onAction, disabledHint)` | `components/AodsOnboarding.kt` |
| Thẻ một quyền: icon, tên, mô tả, trạng thái (đã cấp / nút Cho phép / không kiểm tra được + Mở cài đặt) | `AodsPermissionCard(permission, onAllowClick, containerColor)` | `components/AodsPermissionCard.kt` |
| Danh sách quyền của AOD | `PermissionManager.aodPermissions()` · `requiredAodPermissions()` · `List<PermissionUiModel>.hasRequiredPermissions` | `presentation/model/PermissionUiModel.kt` |
| Token hình ảnh (màu, khoảng cách, chữ, bo góc, motion, kích thước riêng của component) | `AodsTheme.colors` · `.spacing` · `.typography` · `.shapes` · `.motion` · `.opacity` · `.elevation` · `.settingsRow` · `.picker` · `.drawingPad` · `.clock` · `.splash` · `.onboarding` · `.ad` | `presentation/theme/` |
| UiModel từ tuỳ chọn trong DataStore: đọc 1 lần / theo dõi | `DataStoreManager.currentAodXxx()` · `DataStoreManager.observeAodXxx()` | `presentation/model/AodXxxUiModel.kt` |
| Manager trong Compose | `val x: XxxManager = koinInject()` (Screen/host) | `di/AppModule.kt` |
| Banner / Native / loading ad | `BannerAdView` · `NativeAdView` · `AdLoadingDialog` | `ads/composables/` |
| Danh sách ngôn ngữ | `LanguageValue` | `presentation/model/` |
| Key `AnimatedContent` | `AnimationContentKey` | `presentation/model/` |
| Coroutine an toàn huỷ (chọn helper: [mục 13.1](#131-chọn-helper--bắt-buộc-cho-code-mới)) | `suspendRunCatching` · `withContextCatching` · `collectCatching` · `collectLatestCatching` · `recoverCatching` | `utils/CoroutineExt.kt` |
| Điều hướng | `NavBackStack.navigateTo(dest, preserveState)` | `utils/NavExt.kt` |
| Toast | `context.showToast(message)` | `utils/ContextExt.kt` |
| Đăng ký receiver cho broadcast hệ thống (`RECEIVER_NOT_EXPORTED` từ Android 13; receiver `null` = đọc broadcast sticky) | `context.registerSystemReceiver(receiver, filter)` | `utils/ContextExt.kt` |
| Mở trang cài đặt hệ thống (Context của Activity; ROM không có trang đó thì mở Thông tin ứng dụng) | `context.openSettingsPage(intent)` · `context.openOverlaySettings()` · `context.openNotificationSettings()` · `context.openNotificationListenerSettings(component)` · `context.openMiuiPermissionSettings()` · `context.packageUri()` | `utils/ContextExt.kt` |
| Áp ngôn ngữ đã chọn cho một Activity (`LocalConfiguration` / `LocalResources`) | `setContent { AppLanguageProvider { … } }` — tự `koinInject` `DataStoreManager` (`selectedLangCode`) và `LanguageManager` | `components/AppLanguageProvider.kt` |
| Tag log | `: Tag` → `Timber.tag(tag)` | `utils/Tag.kt` |
| Khung màn MVI | copy `screens/main/` | `presentation/screens/main/` |

Quy tắc:
- Trước khi viết composable/util mới → kiểm tra bảng trên. Dùng ở ≥ 2 nơi → `components/` (UI) hoặc `utils/` (không UI); 1 nơi → `private` trong file đó.
- Component dùng chung không tự `koinInject`/`koinViewModel`; nhận data + callback qua tham số. Ngoại lệ: host bọc cả màn của Activity (`AppNavDisplay`, `AppLanguageProvider`) được `koinInject` manager.
- Không copy-paste: logic giữa ViewModel → UseCase; UI giữa màn → component; giá trị lặp → token/`const val`.
- Không tạo top-level `val` trung gian dùng 1 lần → inline (trừ token theme, `const val` cho magic number, giá trị tính sẵn để khỏi tính lại mỗi frame).
- **File Compose (Screen / Content / component) không khai báo `val`, `const val` hay class phụ ở top-level:**
  - Giá trị hình ảnh (`dp`, `sp`, thời lượng, alpha, màu, shape) lấy từ token `AodsTheme.*` (mục 14), không viết số trong file UI.
  - Hằng số của logic → `companion object` của ViewModel (vd `HINT_VISIBLE_MILLIS`); của model → companion của UiModel (vd `AodOptionsUiModel.TIMEOUT_STEP_MINUTES`).
  - Pattern định dạng → `strings.xml` với `translatable="false"`.
  - Intent / hằng số của hệ thống → hàm trong `utils/ContextExt.kt`.
  - Code cũ của base còn 2 chỗ chưa đổi: `ShimmerCosA` / `ShimmerSinA` trong `Modifiers.kt`, `NativeAdColors` trong `NativeAdView.kt`. Token theme trong `presentation/theme/` không tính.

## 16. Resources

| Loại | Vị trí | Quy tắc |
|---|---|---|
| String | `res/values/strings.xml` (English mặc định) + `values-<qualifier>/` | Không hardcode text trong composable → `stringResource(R.string.x, …)`; placeholder `%1$d`/`%1$s`; ngoại lệ: nội suy thuần số/dấu |
| Icon | Material Icons hoặc `res/drawable/ic_<tên>.xml` (vector) | |
| Ảnh | `res/drawable/img_<tên>.webp` | |
| Lottie | `res/raw/lottie_<tên>.json` | `AodsLottie(resId = R.raw.x, modifier = …)` (lặp vô hạn). Hình động của icon app (`lottie_device_edge_light`) giữ đúng toạ độ khung 108 của `ic_launcher_foreground` để khung đầu trùng icon tĩnh. **Không dùng hiệu ứng Gaussian Blur của Lottie**: lottie-android vẽ nó bằng `BlurMaskFilter(độ mờ / 2)` tính theo px màn hình, không theo cỡ animation, nên mỗi cỡ (và bản xem trước lottie-web) mờ một kiểu; làm mờ / quầng sáng bằng nhiều lớp nét rộng dần, nhạt dần |
| Native ad | `res/layout/native_ad_*.xml` + `res/drawable/bg_*` | XML layout DUY NHẤT được phép |
| Launcher icon | `res/mipmap-anydpi/` + `drawable/ic_launcher_foreground.xml` (điện thoại tắt màn, viền sáng gradient mint → xanh → tím, đồng hồ 7 đoạn xếp chồng, ba chấm thông báo) · `ic_launcher_monochrome.xml` (icon theo chủ đề) · nền đen `ic_launcher_background.xml`. Small icon của thông báo vẫn là `ic_aod` (phẳng một màu) | Sửa thẳng vector: giữ trong vùng an toàn 66dp giữa khung 108, mỗi `pathData` dưới 800 ký tự |
| Ảnh nền có sẵn | `res/drawable/img_wallpaper_<tên>.xml` (vector) | Nền đen, chi tiết mờ, chừa giữa cho đồng hồ; viewport 1080×2400 (9:20), khai báo 90×200dp (lint `VectorRaster`); mỗi `pathData` dưới 800 ký tự (lint `VectorPath`) |

## 17. Logging, comment, test

- Class cần log implement `Tag` → `Timber.tag(tag).d/e(...)`; lỗi `Timber.tag(tag).e(throwable.stackTraceToString())`. `Timber.DebugTree` chỉ plant ở debug.
- Không comment mô tả "làm gì"; UI không comment. Chỉ giữ 1 dòng **tại sao** cho invariant không hiển nhiên (vd `onLost` của NetworkManager, `mutate()` drawable) và `// TODO:` cho chỗ project phải làm.
- Test: `app/src/test/java/<package>/…`; `kotlinx-coroutines-test` (`runTest`, `Dispatchers.setMain` cho VM). Fake tại biên **interface Repository** — không mock UseCase/class final; Manager là class cụ thể nên VM phụ thuộc manager test bằng instrumented test. Ưu tiên: UseCase, ViewModel (Intent → State/Effect), mapper.

## 18. Banned patterns

| Banned | Thay bằng |
|---|---|
| Hilt / Dagger · CompositionLocal tự viết để truyền manager · Koin trong Content | Koin `koinInject()` / `koinViewModel()` tại Screen/host |
| LiveData, RxJava | `StateFlow` / `SharedFlow` / `Flow` |
| navigation-compose | Navigation 3 |
| XML layout (trừ native ad) | Compose |
| `GlobalScope`, `runBlocking`, `Thread.sleep` | scope có lifecycle, `delay` |
| `Log.*`, `println` | Timber |
| `List<T>` / `MutableList` trong State/UiModel | `ImmutableList<T>` |
| ViewModel gọi Repository/DataSource/Impl | UseCase (dữ liệu nghiệp vụ) · Manager (hạ tầng) |
| Manager dạng interface + `Impl` · base class chỉ có 1 lớp con (vd `BaseAds`) | 1 class cụ thể (`XxxManager`, `AdsManager`) |
| UseCase/Repository chỉ forward Manager | Inject Manager trực tiếp |
| `runCatching` trong suspend · `catch (CancellationException)` tự viết | `suspendRunCatching` · `withContextCatching` · `collectCatching` · `recoverCatching` |
| `withContext(…)` thô (Repository, Manager) · exception riêng của domain | `withContextCatching(context = …, action = …, catch = …)` — lỗi xử lý ngay ở Impl ([mục 9](#9-data-layer)) |
| `.collect { }` / `.collectLatest { }` thô với flow của manager/UseCase (ViewModel, Service, TileService…) | `collectCatching` / `collectLatestCatching(action = …, catch = …)` ([mục 13.1](#131-chọn-helper--bắt-buộc-cho-code-mới)) |
| Intent/Effect ở thì quá khứ, danh từ trơn, theo nhãn nút (`BackgroundPicked`, `Preview`, `Done`) | Mẫu ở [mục 10.2.1](#1021-đặt-tên-intent--effect) (`BackgroundPickerResult`, `OpenPreview`, `ConfirmLanguage`) |
| `repeatOnLifecycle` thô rải trong từng Screen · collect effect ngoài helper lifecycle | `LaunchedWithLifecycleEffect { }` (`presentation/effects/`) · `LifecycleResumeEffect` |
| `context.getString` cho text UI | `stringResource` · `LocalResources.current.getString` |
| Hex `Color(0x…)`, `Color.White/Black`, `RoundedCornerShape` / `CircleShape`, số `dp`/`sp`, `durationMillis` viết thẳng, `MaterialTheme.*` trong UI | Token `AodsTheme.*` (mục 14) |
| Text hardcode trong composable | `strings.xml` |
| `collectAsState()` | `collectAsStateWithLifecycle()` |
| `Modifier.clickable` ngoài `Modifiers.kt` | `Modifier.onClick` |
| `LottieAnimation` ngoài `AodsLottie.kt` | `AodsLottie` |
| `Timber` trực tiếp trong ad unit con | `log("Action")` của `AdUnit` (tự kèm ad unit id) |
| Tạo `AdUnit` trong composable · ad id hardcode trong code | placement `by lazy` trong `AdsManager` · `BuildConfig.<PLACEMENT>_ALL_ID` |

Kiểm tra trước khi báo xong — mọi lệnh phải ra **0 dòng**:

```bash
grep -rn "Color(0x\|Color\.White\|Color\.Black" app/src/main/java --include="*.kt" | grep -v "/presentation/theme/"
grep -rn "RoundedCornerShape(" app/src/main/java --include="*.kt" | grep -v "/presentation/theme/"
grep -rn "Log\.[vdiwe](\|println(" app/src/main/java --include="*.kt"
grep -rn "GlobalScope\|runBlocking\|Thread\.sleep" app/src/main/java --include="*.kt"
grep -rn "collectAsState()\|repeatOnLifecycle" app/src/main/java --include="*.kt" | grep -v "/presentation/effects/"
grep -rn "koinInject\|koinViewModel\|koinActivityViewModel\|staticCompositionLocalOf" app/src/main/java --include="*Content.kt"
grep -rn "\.clickable(" app/src/main/java --include="*.kt" | grep -v "/components/Modifiers.kt"
grep -rn "LottieAnimation(" app/src/main/java --include="*.kt" | grep -v "/components/AodsLottie.kt"
grep -rn "LiveData\|dagger\.hilt\|androidx\.navigation\.compose" app/src/main/java --include="*.kt"
grep -rn "val .*: \(Mutable\)\?List<" app/src/main/java --include="*State.kt" --include="*UiModel.kt"
grep -rn "catch (.*: CancellationException)" app/src/main/java --include="*.kt" | grep -v "/ads/ad_unit/\|/utils/CoroutineExt.kt"
grep -rn "\.collect {" app/src/main/java --include="*.kt" | grep -v "\.effect\.collect {"
grep -rn "\.collectLatest {" app/src/main/java --include="*.kt" | grep -v "/components/Modifiers.kt"
grep -rnE "data (object|class) [A-Za-z]+ed\b" app/src/main/java --include="*Intent.kt" --include="*Effect.kt"
grep -rn "context\.getString\|activity\.getString" app/src/main/java --include="*.kt"
grep -rln "^import android\.\|^import androidx\." app/src/main/java --include="*.kt" | grep "/domain/"
grep -rn "import .*\.data\.repository\.\|import .*Impl$" app/src/main/java --include="*.kt" | grep "/presentation/"
grep -rn "import .*\.domain\.repository\.\|import .*Impl$" app/src/main/java --include="*ViewModel.kt"
grep -rn "Timber" app/src/main/java --include="*AdUnit.kt" | grep -v "/ad_unit/AdUnit.kt"
grep -rn "ManagerImpl\|BaseAds" app/src/main/java --include="*.kt"
grep -rn "withContext(" app/src/main/java --include="*.kt" | grep -v "/utils/CoroutineExt.kt"
grep -rnE "[0-9]\.(dp|sp)\b" app/src/main/java --include="*.kt" | grep -v "/presentation/theme/"
grep -rn "durationMillis = [0-9]" app/src/main/java --include="*.kt" | grep -v "/presentation/theme/"
grep -rn "MaterialTheme\.\|CircleShape" app/src/main/java --include="*.kt" | grep -v "/presentation/theme/"
```

---

## 19. Checklist thêm feature mới

1. `domain/model/` — domain model.
2. `domain/repository/XxxRepository.kt` — interface.
3. Nguồn dữ liệu (`data/network/…`, `data/local/…`, `data/source/…`) → `data/repository/XxxRepositoryImpl.kt` (main-safe và bắt lỗi qua `withContextCatching`, map → domain).
4. `domain/usecase/` — UseCase ([mục 8](#8-domain-layer)).
5. `di/AppModule.kt` — `repositoryModule` · `useCaseModule` · `viewModelModule` (+ `networkModule` nếu thêm ApiService).
6. `presentation/model/XxxUiModel.kt` (+ `toUiModel()`) nếu cần.
7. `presentation/screens/xxx/` — copy khung `screens/main/` (State · Intent · Effect · ViewModel · Content · Screen).
8. `AppDestinations.kt` — `XxxDestination`; đăng ký `entry<XxxDestination>` ở `AppNavDisplay` (hoặc NavDisplay lồng).
9. Ad cho màn: placement `by lazy` trong `AdsManager` + `<PLACEMENT>_ALL_ID` ở `release {}`/`debug {}`; native/banner truyền slot từ Screen.
10. Text vào `strings.xml` (+ bản dịch nếu project có); màu, khoảng cách, cỡ chữ, thời lượng mới vào token `presentation/theme/tokens/` (mục 14).
11. `./gradlew :app:compileDebugKotlin` + chạy grep [mục 18](#18-banned-patterns).

---

## 20. AOD core

Lõi AOD port từ demo FakeAOD (đã chạy trên Redmi Note 13 Pro 5G, HyperOS 1.0, Android 14) rồi refactor theo rule của base: tuỳ chọn → `DataStoreManager`, tín hiệu thiết bị → manager trong `data/device/`, màn đồng hồ → MVI. README mô tả cho người dùng và cách test; mục này là những gì dev **không được đổi** và lý do. Đổi bất kỳ điểm nào ở 20.3 → test lại trên máy thật theo README › Kiểm tra trên máy thật.

### 20.1 Luồng

```
màn hình tắt ─▶ ScreenStateManager.events ─▶ AodService.onScreenOff()
                                                 │ shouldEnter(): đang bật · màn hình vẫn tắt · không chuông/cuộc gọi/báo thức
                                                 │                · AodRulesUiModel.allows(): nguồn điện · khung giờ · pin đủ
                                                 ▼
                                  startActivity(AodActivity) — được phép từ nền nhờ "Hiển thị trên ứng dụng khác"
                                                 │ cờ cửa sổ SHOW_WHEN_LOCKED | TURN_SCREEN_ON: che màn hình khoá rồi mới sáng
                                                 ▼
             AodActivity ── AodScreen ── AodViewModel (pin · cắm nguồn · tiệm cận · âm thanh · hết giờ · mỗi phút)
                                                 │ state.isDark ─▶ AodActivity.renderDark(): KEEP_SCREEN_ON, độ sáng, requestSleep
                                                 ▼
nút nguồn khi AOD hiện ─▶ SCREEN_OFF ─▶ AodService: ScreenStateManager.wakeUp() → màn hình khoá
chạm 2 lần · mở khoá (USER_PRESENT) · cuộc gọi · báo thức ─▶ đóng AOD
hết giờ · trong túi · sai quy tắc hiện ─▶ đen ─▶ giờ chờ của máy tắt màn hình ─▶ SCREEN_OFF có sleep request ─▶ không mở lại
ô Cài đặt nhanh ─▶ AodTileService: lưu isAodEnabled, start/stop AodService (start bị chặn ─▶ mở app để MainActivity start)
thông báo ─▶ AodNotificationListener (hệ thống bind) ─▶ NotificationStateManager: lọc, nạp icon ─▶ notifications · alerts · mediaSessionToken
mediaSessionToken ─▶ MediaStateManager ─▶ playback
AodViewModel: icon (notifications) · viền sáng (alerts, khi không tối) · nhạc (playback; nút ─▶ transportControls)
chạm 2 lần · vuốt · phím âm lượng · phím back ─▶ AodIntent.PerformGesture ─▶ hành động user gán (về màn hình khoá, tối, đèn pin, nhạc)
phòng tối (AmbientLightManager) ─▶ isDimmed ─▶ AodActivity.renderDim(): độ sáng 1%
tối theo chủ ý + màn hình tắt ─▶ AodService chờ PickupGestureManager.pickups ─▶ nhấc máy ─▶ shouldEnter() ─▶ launchAod()
AodViewModel lúc mở + mỗi phút: thời tiết (bản đã lưu ngay; RefreshWeatherUseCase ─▶ DeviceLocationManager ─▶ Open-Meteo ─▶ DataStore)
                                · sự kiện hôm nay (GetUpcomingEventsUseCase ─▶ CalendarContract.Instances) · ghi nhớ · hình vẽ (AodImageManager)
```

### 20.2 Thành phần

| Thành phần | Vai trò |
|---|---|
| `presentation/aod/AodService` | Foreground service `specialUse`. Collect `ScreenStateManager.events`, quyết định có mở AOD không, mở AOD, đưa về màn hình khoá khi bấm nguồn, `checkLaunch()` 2 giây sau mỗi lần mở để ghi `aodLastWake`. Thông báo thường trực theo ngôn ngữ đã chọn trong app: đăng lại mỗi lần start và khi `selectedLangCode` đổi |
| `presentation/aod/AodActivity` | Host của `AodScreen`. Giữ mọi thao tác cửa sổ: cờ, độ sáng, ẩn thanh hệ thống, `renderDark()`. Áp ngôn ngữ bằng `AppLanguageProvider` |
| `presentation/aod/AodSession` | Koin `single`, chỉ dùng trên main thread. `WeakReference` tới `AodActivity`; `isShowing`, `isCovered`, `shownAt`, sleep request; `finish()` đóng AOD ngay |
| `presentation/aod/BootReceiver` | `BOOT_COMPLETED` / `MY_PACKAGE_REPLACED` → start service nếu đang bật (`goAsync()` trong lúc đọc DataStore) |
| `presentation/aod/AodNotificationListener` | `NotificationListenerService`, hệ thống chỉ bind khi user đã cấp "Truy cập thông báo". Callback (main thread) chỉ đọc `activeNotifications` + `RankingMap` rồi chuyển cho `NotificationStateManager`; `openAccessSettings(context)` mở trang cấp quyền |
| `AodImageManager` | Ảnh nền (bản sao JPEG đã thu về cạnh dài của màn hình) và hình vẽ nhanh (PNG nền trong suốt) trong `filesDir`; `loadBackground()` · `loadDrawing()` cho AOD |
| `domain/` · `data/repository/` | Thời tiết: `ObserveWeatherUseCase` · `RefreshWeatherUseCase` → `WeatherRepository` (`WeatherRepositoryImpl`: `DeviceLocationManager` → `OpenMeteoApiService` → bản lưu trong `DataStoreManager`). Sự kiện: `GetUpcomingEventsUseCase` → `CalendarRepository` (`CalendarRepositoryImpl`: `CalendarContract.Instances`) |
| `FlashlightManager` · `AmbientLightManager` · `PickupGestureManager` | Đèn pin, ánh sáng phòng, nhấc máy (mục 2) |
| `NotificationStateManager` · `MediaStateManager` | Lọc thông báo như màn hình chờ của hệ thống, nạp icon ở luồng nền, che nội dung như màn hình khoá, báo thông báo mới; điều khiển nhạc bằng `MediaController` từ token của thông báo nhạc (mục 2) |
| `presentation/aod/AodTileService` | Ô Cài đặt nhanh (`TOGGLEABLE_TILE`): hiện và đảo `isAodEnabled`, start/stop service; `AodService.start()` trả `false` → mở app bằng `startActivityAndCollapse` (đang khoá → `unlockAndRun` trước) |
| `presentation/model/AodRulesUiModel` | Quy tắc hiện: `minBattery`, `chargingRule` (`ChargingRuleValue`), `schedule` (`AodScheduleUiModel`). `allows()` dùng chung cho `AodService.shouldEnter()` và `AodViewModel.checkRules()`; `DataStoreManager.currentAodRules()` đọc snapshot đồng bộ |
| `presentation/screens/aod/` | `AodViewModel` (khi nào tối, sáng lại, đóng; mỗi phút cập nhật giờ, dịch vị trí và kiểm quy tắc hiện; ẩn dòng gợi ý sau 3 giây; icon thông báo, viền sáng 4 giây, nút nhạc; hành động của thao tác, đèn pin, tự giảm sáng; thời tiết, sự kiện, ghi nhớ, hình vẽ) · `AodContent` (bố cục, nhận chạm, vuốt, phím âm lượng; vẽ đồng hồ từ state, không tự đếm giờ) cùng các phần tách file: `AodClockFace`, `AodDetails`, `AodExtras` (thời tiết, sự kiện, ghi nhớ, hình vẽ), `AodNotifications`, `AodMediaControls`, `AodBackdrop` (ảnh nền, viền sáng), `AodTimeFormat` · `AodScreen` (collect effect, phím back, chuyển `isDark`/`isDimmed` cho Activity) |
| `presentation/screens/main/` | Cài đặt: công tắc, danh sách quyền, tuỳ chọn (độ sáng, tiệm cận, hết giờ), giao diện (mặt, font, màu, cỡ, xoay ngang, ảnh nền có sẵn hoặc từ máy), thông tin thêm (ghi nhớ, hình vẽ nhanh, sự kiện hôm nay, thời tiết, °F; hỏi quyền lịch và vị trí khi bật), thông báo trên đồng hồ (icon, nội dung, viền sáng, nhạc), thao tác và cảm biến (hành động cho 6 thao tác, tự giảm sáng, nhấc máy), quy tắc hiện (nguồn điện, khung giờ, ngưỡng pin), ngôn ngữ, xem thử, kết quả lần mở gần nhất, hỏi quyền thông báo lần đầu |
| `presentation/screens/permission/` | Bước 2 của onboarding lần đầu (mục 5): quyền bắt buộc, đủ quyền mới vào Main. Về sau thiếu quyền thì `MainPermissionSheet` + `MainPermissionWarning` ở Main |
| `data/device/*` | `ScreenStateManager` · `BatteryStateManager` · `AudioStateManager` · `ProximityManager` · `PermissionManager` (mục 2) |
| `DataStoreManager` | Tuỳ chọn, thông báo trên đồng hồ, thông tin thêm và quy tắc hiện của AOD, bản thời tiết đã lưu, `aodLastWake` (mã của `WakeResultValue`), `isNotificationsAsked` |

### 20.3 Bất biến

1. **Activity che màn hình khoá, không dùng overlay.** Overlay (kể cả `TYPE_ACCESSIBILITY_OVERLAY`) chỉ vẽ đè: mở khoá bằng khuôn mặt chạy mỗi lần AOD hiện, vòng vân tay của Xiaomi nằm trên AOD, màn hình khoá loé lên trước khi vẽ xong. `android:showWhenLocked` trong manifest là điều kiện để Activity được mở trên màn hình khoá.
2. **Bật màn hình bằng cờ cửa sổ `FLAG_SHOW_WHEN_LOCKED | FLAG_TURN_SCREEN_ON` ở đầu `onCreate`**, không dùng `android:turnScreenOn` hay `setTurnScreenOn()`. Hai cách kia bật màn hình ngay khi Activity khởi động, trước khi màn hình khoá bị che: trên HyperOS màn hình khoá lộ 0,3–0,45 giây và nhận diện khuôn mặt chạy ở mọi lần mở. Với cờ cửa sổ, nhận diện khuôn mặt không chạy ở 5/6 lần mở được ghi lại.
3. **Mở từ nền nhờ `SYSTEM_ALERT_WINDOW`** (log `BAL_ALLOW_SAW_PERMISSION`), lúc màn hình còn tắt. Không dùng thông báo toàn màn hình: SystemUI bật màn hình trước rồi mới mở AOD, màn hình khoá lộ khoảng 0,2 giây.
4. **Foreground service `specialUse`** giữ receiver `SCREEN_OFF` (broadcast này chỉ tới receiver đăng ký lúc chạy). Chỉ start từ: `MainActivity` (tiền cảnh), công tắc trên màn Main, `BootReceiver` (được miễn), ô Cài đặt nhanh. Từ Android 15, app có quyền "Hiển thị trên ứng dụng khác" chỉ được start foreground service từ nền khi đang có overlay hiển thị, và ô Cài đặt nhanh không được miễn: `AodService.start()` bắt `IllegalStateException` (`ForegroundServiceStartNotAllowedException`) và trả `false`, `AodTileService` khi đó mở app để `MainActivity` start. Đừng thêm chỗ start khác.
5. **`AodSession` đồng bộ trên main thread.** Lúc `SCREEN_OFF`, service đọc `isShowing`, `isCovered`, `consumeSleepRequest()` rồi `finish()` trong cùng một lần xử lý. Không chuyển thành StateFlow hay sự kiện bất đồng bộ.
6. **Thứ tự `when` trong `onScreenOff()` là có chủ ý:** sleep request (tối theo chủ ý) → bị che (ROM tự tắt màn hình trong túi) → AOD đang hiện (= nút nguồn, vì AOD giữ màn hình sáng) → `shouldEnter()`. Không mở AOD nếu `isInteractive` đã true lúc `SCREEN_OFF` tới: `SCREEN_OFF` đến trễ 0,2–0,8 giây, người dùng vừa bấm nguồn bật lại.
7. **`ScreenStateManager.events` là `SharedFlow`**, không phải `StateFlow`: StateFlow gộp mất một lần tắt rồi bật lại ngay.
8. **App không tự tắt màn hình được** (cần trợ năng hoặc quản trị thiết bị). Hết giờ, trong túi, sai quy tắc hiện (pin yếu, ngoài khung giờ, sai nguồn điện) → `isDark`: đen ở `BRIGHTNESS_OVERRIDE_OFF`, bỏ `FLAG_KEEP_SCREEN_ON`, `requestSleep()`; giờ chờ của máy tắt màn hình và service không mở lại.
9. **Khung đầu tiên phải đúng:** độ sáng đặt trong `onCreate` trước khi cửa sổ hiện (độ sáng riêng: `aodBrightnessPercent / 100`; tắt: `BRIGHTNESS_OVERRIDE_NONE` = theo hệ thống); pin ban đầu đọc đồng bộ (`readLevelPercent()`) để dòng pin có ngay, bố cục không nhảy khi màn hình vừa sáng. Tuỳ chọn được đọc đồng bộ từ `.value` của `DataStoreManager` (đã nạp vì service hoặc `MainActivity` tạo manager từ trước); `null` → `DEFAULT_*`.
10. **`USER_PRESENT` trong 1,5 giây sau khi AOD hiện**, trên máy không có PIN/hình vẽ, là do chính app bật màn hình → không đóng AOD.
11. **`checkLaunch()` sau 2 giây:** AOD không hiện hoặc màn hình không sáng → ghi `FAILED` và `finish()`, vì trên Xiaomi thiếu "Hiển thị trên màn hình khoá" thì Activity nằm sau màn hình khoá và sẽ hiện ra sau lần mở khoá kế tiếp.
12. **Tiệm cận:** bị che liên tục 3 giây mới tối (bàn tay lướt qua bị bỏ qua); chỉ tối do che mới sáng lại khi lấy máy ra; chạm 2 lần bị bỏ qua khi cảm biến đang bị che (vải cọ trong túi).
13. **Âm thanh:** AOD đóng khi `isBusy` chuyển sang `true` sau khi AOD đã mở (`drop(1)` bỏ trạng thái lúc vừa đăng ký, giống callback của core chỉ báo khi có thay đổi); service không mở AOD khi `isBusyNow()`. Không cần `READ_PHONE_STATE`. Dưới Android 12 không có listener cho mode nên mode được hỏi lại mỗi phút.
14. **Manifest của `AodActivity`:** `singleInstance`, `noHistory`, `excludeFromRecents`, `taskAffinity=""`, `configChanges` đủ để không bị tạo lại; mở với `NEW_TASK | CLEAR_TASK | NO_ANIMATION | NO_USER_ACTION` (giống Always On AMOLED). `noHistory` đóng AOD khi màn hình cuộc gọi hoặc báo thức mở lên trên.
15. **Theme `Theme.App.Aod`:** không có animation cửa sổ, cutout `shortEdges`, splash Android 12+ màu đen (`values-v31`) để không loé icon app trước đồng hồ.
16. **Không backup** (`allowBackup="false"` + `data_extraction_rules.xml`): `isNotificationsAsked` mà sang máy mới thì máy mới không bao giờ được hỏi quyền thông báo.
17. **Quy tắc hiện kiểm ở 2 nơi bằng cùng `AodRulesUiModel.allows()`:** `AodService.shouldEnter()` lúc `SCREEN_OFF` (đọc đồng bộ `readLevelPercent()`, `readIsCharging()`, `readIsPlugged()`, giờ hiện tại) và `AodViewModel.checkRules()` khi pin hoặc nguồn cắm đổi và mỗi phút. AOD đang hiện mà sai quy tắc → `goDark()` chứ không đóng: đóng lúc màn hình đang sáng sẽ lộ màn hình khoá. Không đọc được trạng thái (`null`) thì không chặn. Xem thử không kiểm quy tắc.
18. **"Đang cắm nguồn" theo `EXTRA_PLUGGED`**, không theo `isCharging`: pin đầy hoặc máy giới hạn sạc (dừng ở 80%) vẫn là đang cắm. Ngưỡng pin vẫn bỏ qua khi `isCharging` như FakeAOD.
19. **Khung giờ lưu theo phút trong ngày** (`aodScheduleStartMinute`, `aodScheduleEndMinute`, giờ địa phương): bắt đầu < kết thúc → `[bắt đầu, kết thúc)`; bắt đầu > kết thúc → qua nửa đêm; bằng nhau → cả ngày; tắt khung giờ → luôn hiện. Mặc định bật, 07:00–23:00.
20. **Thông báo trên AOD được lọc như màn hình chờ của hệ thống** (`NotificationStateManager.isShownOnAmbient`): bỏ thông báo của chính app, thường trực (`isOngoing`, `FLAG_FOREGROUND_SERVICE`), tóm tắt nhóm, thông báo nhạc (đã có điều khiển nhạc), im lặng (importance dưới `DEFAULT`), bị Không làm phiền ẩn khỏi màn hình chờ (`SUPPRESSED_EFFECT_AMBIENT`), ẩn trên màn hình khoá (`VISIBILITY_SECRET`; tuỳ chỉnh theo kênh chỉ đọc được từ Android 12), của app bị tạm ngưng. Hiện **icon, mỗi app một icon** (tối đa 5, còn lại "+N"); nội dung chỉ khi user bật và theo đúng quy tắc của màn hình khoá (bất biến 38).
21. **Icon nạp sẵn trong manager**, trên 1 luồng nền (`limitedParallelism(1)`: giữ thứ tự callback, nạp resource của app khác không chặn main thread), cache theo (package, resource). Listener được hệ thống cho thấy package của app gửi thông báo, nên `Icon.loadDrawable` đọc được resource của app đó.
22. **Viền sáng** khi `alerts` phát: thông báo mới, hoặc cập nhật không đặt `FLAG_ONLY_ALERT_ONCE`, và không bị Không làm phiền chặn (`matchesInterruptionFilter`). Chỉ khi AOD không tối (tối = đang chờ máy tắt màn hình); tắt sau 4 giây hoặc khi chuyển sang tối. Vẽ bằng gradient ở 4 cạnh, không dùng `Modifier.blur` (cần API 31, minSdk là 30). Màu là `Notification.color` của app; không đặt hoặc quá tối thì dùng `Mint`.
23. **Nhạc lấy từ token của thông báo nhạc mới nhất** (`EXTRA_MEDIA_SESSION`), cách trình phát trên màn hình khoá của hệ thống chọn phiên nhạc; dùng token không cần quyền riêng. Chỉ có tên bài, nghệ sĩ và 3 nút, không có ảnh bìa (sáng, dễ burn-in). Nút nhạc bị bỏ qua khi cảm biến tiệm cận bị che, như chạm 2 lần.
24. **Listener không khai báo `default_filter_types`:** thông báo nhạc thường thuộc loại im lặng hoặc thường trực, lọc theo loại thì mất điều khiển nhạc.
25. **Giao diện đọc 1 lần lúc mở** (`currentAodAppearance()` trong `initialState`) để khung đầu tiên đúng mặt, font, màu, cỡ. Hướng ngang (`isAodLandscape`) đặt trong `AodActivity.onCreate` trước khi cửa sổ hiện (`SCREEN_ORIENTATION_SENSOR_LANDSCAPE`, cả lúc xem thử; màn hình lớn từ Android 16 bỏ qua). Bố cục ngang hay dọc theo kích thước thật (`BoxWithConstraints`), không theo tuỳ chọn, nên tự xoay của hệ thống cũng đúng; ở bố cục ngang hai biên độ dịch chống burn-in đổi chỗ cho nhau (trục dài dịch nhiều hơn).
26. **Mặt đồng hồ chỉ đổi mỗi phút**, không có kim giây: mặt kim vẽ bằng `Canvas` từ `nowMillis`; mặt số dùng pattern `translatable="false"` trong `strings.xml` như trước, không có SA/CH.
27. **Ảnh nền:** Photo Picker chỉ cho đọc tạm, nên `AodImageManager.saveBackground()` lưu bản sao JPEG đã thu về cạnh dài của màn hình (ghi file tạm rồi đổi tên). AOD giải mã ở luồng IO rồi hiện dần (`Crossfade`), không chặn khung đầu tiên; vẽ với `alpha = 0.5` để chữ nổi trên ảnh, ẩn khi tối. Ảnh nền đứng yên nên dễ burn-in hơn đồng hồ (README › Giới hạn). Mỗi lúc chỉ một nguồn ảnh nền: ảnh có sẵn (bất biến 39) hoặc ảnh từ máy; chọn cái này thì bỏ cái kia.
28. **Font của đồng hồ AOD là họ font chung của hệ thống** (`FontFamily.Default/Serif/Monospace/Cursive`): không thêm file font hay thư viện cho đồng hồ; font cụ thể do ROM chọn. Inter (mục 14) chỉ là font giao diện app, không áp vào màn AOD.
29. **Thao tác và phím** (`AodGestureValue` → `AodActionValue`, mỗi thao tác một key DataStore, đọc 1 lần lúc mở): chạm 2 lần, vuốt lên/xuống (quá 80dp), phím tăng/giảm âm lượng, phím back. Mặc định giữ cách chạy trước đây: chạm 2 lần và back về màn hình khoá, còn lại không làm gì. Phím âm lượng chỉ bị chặn khi đã gán hành động, nếu không vẫn chỉnh âm lượng. `AodContent` giữ focus (`focusRequester` + `focusable`) để nhận phím âm lượng qua `onPreviewKeyEvent`; back qua `BackHandler` ở `AodScreen`. Mọi thao tác bị bỏ qua khi cảm biến tiệm cận đang bị che. Dòng "Chạm 2 lần để thoát" chỉ hiện khi chạm 2 lần vẫn là về màn hình khoá.
30. **Đèn pin** bằng `CameraManager.setTorchMode`, không cần quyền `CAMERA`; trạng thái theo `TorchCallback` (đèn bật từ bất kỳ đâu), AOD hiện icon khi đèn đang bật. Đóng AOD không tắt đèn, giống ô Đèn pin của hệ thống.
31. **Tự giảm sáng** (`AmbientLightManager`, chỉ đăng ký khi AOD hiện): ≤ 5 lux là phòng tối, ≥ 20 lux là sáng, giữa hai ngưỡng thì giữ nguyên; mức mới phải giữ 2 giây (`collectLatestCatching`) mới đổi. Phòng tối → `AodActivity.renderDim()` đặt độ sáng cửa sổ 1%; đang tối hẳn (`isDark`) thì `renderDark` quyết định.
32. **Nhấc máy để hiện lại đồng hồ** chỉ dùng cảm biến chuẩn `android.sensor.pick_up_gesture` (wake-up, one-shot, chạy trên chip cảm biến). `AodService` chỉ chờ nhấc máy khi `SCREEN_OFF` đến lúc đồng hồ đã tối theo chủ ý mà **không** bị che: tối vì trong túi thì không chờ, để đi bộ không làm sáng màn hình trong túi. Nhấc máy → `shouldEnter()` (quy tắc vẫn áp dụng) → `launchAod()`; `SCREEN_OFF` tiếp theo huỷ lần chờ cũ.
33. **Thông tin thêm đọc 1 lần lúc mở** (`currentAodExtras()` trong `initialState`): ghi nhớ, bật/tắt sự kiện, thời tiết, °F. Dưới đồng hồ theo thứ tự: ngày + thời tiết cùng một dòng → sự kiện → ghi nhớ → hình vẽ → icon thông báo → pin → đèn pin → nhạc. Tắt tuỳ chọn thì AOD không đọc lịch, không gọi mạng.
34. **Thời tiết** (Open-Meteo, không cần API key, không gửi gì ngoài toạ độ): chỉ quyền `ACCESS_COARSE_LOCATION`, xin khi user bật "Thời tiết" và chỉ lưu "bật" khi đã có quyền. Vị trí lấy 1 lần (`DeviceLocationManager`: fused từ Android 12, không có thì nhà cung cấp mạng; chờ tối đa 10 giây rồi dùng vị trí đã biết gần nhất), **làm tròn 2 chữ số thập phân (khoảng 1 km) trước khi gửi**. Kết quả lưu trong DataStore (`saveWeather` ghi 4 key trong 1 lần `edit`): AOD hiện bản đã lưu ngay, tải bản mới ngầm. Chỉ gọi mạng khi bản đã lưu cũ từ 30 phút (`RefreshWeatherUseCase`); AOD đang hiện thử lại mỗi 10 phút (đồng hồ đêm lúc sạc); bản cũ từ 3 giờ không hiện (`WeatherUiModel.isFreshAt`, kiểm lại mỗi phút). Chỉ tải khi app ở tiền cảnh (AOD đang hiện, màn cài đặt lúc bật — `isForced = true`): không có quyền vị trí nền, không có job chạy ngầm. Lỗi (mất mạng, chưa định vị được, API lỗi) → `WeatherRepositoryImpl` log và giữ bản cũ. Icon ngày/đêm theo `is_day` của lần tải.
35. **Sự kiện hôm nay** (`READ_CALENDAR`, xin khi user bật): `CalendarContract.Instances` từ lúc này tới hết ngày (có cả sự kiện lặp lại và đang diễn ra), chỉ lịch đang hiện trong ứng dụng Lịch, bỏ sự kiện user đã từ chối; sự kiện cả ngày trước rồi theo giờ bắt đầu; tối đa 2. Sự kiện cả ngày lưu theo nửa đêm UTC → so theo ngày ghi trên lịch với ngày ở múi giờ của máy. AOD đọc lúc mở và mỗi 15 phút; sự kiện đã kết thúc rời đồng hồ ở tick mỗi phút. Đọc lỗi (vd quyền bị thu hồi) → `CalendarRepositoryImpl` trả danh sách rỗng. Chỉ hiện giờ và tên sự kiện (1 dòng), không hiện địa điểm hay ghi chú: AOD nằm trên màn hình khoá.
36. **Ghi nhớ:** tối đa 120 ký tự (`AodExtrasUiModel.MEMO_MAX_LENGTH`, chặn ngay ở ô nhập), bỏ khoảng trắng hai đầu khi lưu; AOD hiện tối đa 3 dòng, căn giữa, cùng font đồng hồ; rỗng thì không hiện.
37. **Hình vẽ nhanh:** vẽ trên khung vuông nền đen trong dialog, nét trắng 8dp; lưu PNG nền trong suốt cỡ bằng khung vẽ (`AodImageManager.saveDrawing`, ghi file tạm rồi đổi tên). AOD hiện 120dp, tô theo màu đồng hồ (`ColorFilter.tint`), nên đổi màu đồng hồ không phải vẽ lại. Mở lại khung là vẽ hình mới, không sửa hình cũ.
38. **Nội dung thông báo** (tùy chọn, mặc định tắt): chỉ thông báo mới nhất có chữ, 1 dòng tiêu đề + tối đa 2 dòng nội dung, dưới các icon. `NotificationStateManager` quyết định phần được hiện theo đúng màn hình khoá của hệ thống, nên chữ nhạy cảm không bao giờ ra khỏi manager:
    - tắt "Hiện thông báo trên màn hình khoá" (`lock_screen_show_notifications`) → che hết;
    - máy có khoá bảo mật thì che thông báo `VISIBILITY_PRIVATE` khi tắt "nội dung nhạy cảm" (`lock_screen_allow_private_notifications`, hoặc quản lý thiết bị đặt `KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS`), và che kênh user đặt ẩn nội dung (`lockscreenVisibilityOverride`, từ Android 12); máy không có khoá bảo mật thì không che, như hệ thống;
    - bị che thì dùng `publicVersion` app tự soạn; không có thì AOD ghi "Nội dung đã ẩn". Hai cài đặt trên là key ẩn (đọc được, không có hằng số public): ROM chặn đọc → coi như tắt.
    Đọc lại cài đặt mỗi lần danh sách thông báo đổi, không theo dõi riêng.
39. **Ảnh nền có sẵn** (`WallpaperValue`, key `aod_wallpaper`, 0 = không dùng): vector nền đen trong `res/drawable/img_wallpaper_*.xml`, AOD vẽ thẳng bằng `painterResource` (không lưu file, không vỡ gradient như JPEG, có ngay từ khung đầu vì đọc trong `initialState`), `alpha = 0.5` như ảnh từ máy. Bố cục ngang cắt ảnh dọc theo `WallpaperValue.alignment` (giữ phần có chi tiết: cực quang ở trên, sóng ở dưới). Đã chọn ảnh có sẵn thì `loadBackground()` không đọc file. Thêm ảnh mới dùng code mới, không dùng lại code đã có (giá trị cũ của user sẽ trỏ sai).

### 20.4 Chỗ khác MVI / Manager chuẩn, có lý do

- `AodActivity` không phải entry của `NavDisplay` và vẫn tự làm thao tác cửa sổ, vì ViewModel không được chạm `Context`/`Window`. `AodViewModel` chỉ quyết định `isDark`; `AodScreen` chuyển giá trị đó cho Activity qua `onDarkChange`.
- `AodService` không có UI nên không có ViewModel: logic quyết định nằm trong service, mọi truy cập hệ thống đi qua manager.
- Domain chỉ dùng cho dữ liệu lấy từ nguồn ngoài app (thời tiết từ Open-Meteo, sự kiện từ `CalendarContract`): ViewModel → UseCase → Repository. Tuỳ chọn AOD (kể cả ghi nhớ, bật/tắt sự kiện và thời tiết) vẫn là prefs nên dùng thẳng `DataStoreManager` (như `isDark` của DexReader, `showCanChiOnCell` của Lịch Việt); ảnh nền và hình vẽ là file của app → `AodImageManager`. Bọc Repository/UseCase chỉ forward manager là banned (mục 18).
- Không có exception riêng của domain (khác mục 1.3 của base): lỗi của nguồn dữ liệu được Impl xử lý tại chỗ bằng `withContextCatching` (mục 9), vì AOD chỉ cần biết "có dữ liệu để hiện hay không".
- `BootReceiver` là `KoinComponent` (mục 12).
- `AodNotificationListener` nằm ở `presentation/aod/` như các thành phần hệ thống khác (lấy manager bằng `by inject()`), còn lọc và nạp icon nằm trong `NotificationStateManager`, vì data layer không được dùng Koin như service locator.
- Màn AOD không bọc `AodsTheme` (mục 14).
- `AodsSwitchRow` dùng `Modifier.toggleable(role = Role.Switch)` và `AodsRadioRow` dùng `Modifier.selectable(role = Role.RadioButton)`, không dùng `Modifier.onClick`: TalkBack đọc được trạng thái bật/tắt, đã chọn. Dòng chỉ để bấm (`AodsValueRow`) vẫn dùng `Modifier.onClick`.
- `AodContent` có `focusable()` ở node gốc: Compose chỉ chuyển phím cứng tới node đang có focus, không có focus thì phím âm lượng không tới được `onPreviewKeyEvent`.
- `AodTileService` gọi `startActivityAndCollapse(Intent)` (deprecated) dưới API 34 với `@Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")`: bản nhận `PendingIntent` chỉ có từ API 34, bản nhận `Intent` chỉ ném lỗi từ Android 14.

### 20.5 Ads và AOD

- Không bao giờ load/hiện ads hay consent trên `AodActivity`: nó nằm trên màn hình khoá. Hiện tại ads chỉ được load từ màn trong `MainActivity`, và `AdsManager` chỉ được tạo khi `MainActivity` mở.
- Giới hạn đã biết: `AdsManager` giữ `currentActivity` qua `ActivityLifecycleCallbacks`, nên nếu form consent tải xong đúng lúc AOD đang hiện thì form có thể hiện trên AOD. Chưa xử lý vì `ads/` không được import `presentation/aod`. Thêm app-open ad thì phải bỏ qua `AodActivity`.

### 20.6 Thêm tính năng cho AOD

- Tín hiệu mới → manager mới trong `data/device/<tên>/` theo mục 2. Cần thêm dữ liệu từ thông báo (vd nội dung) → thêm vào `NotificationStateManager`, không đăng ký listener thứ hai.
- Tuỳ chọn mới → key trong `DataStoreManager` + field trong `AodOptionsUiModel` + Intent của màn Main + đọc snapshot trong `AodViewModel` (hoặc `AodActivity` nếu là thao tác cửa sổ).
- Quy tắc "khi nào hiện" mới → key trong `DataStoreManager` + field trong `AodRulesUiModel` (`allows()` và `currentAodRules()`) + Intent của màn Main: service và màn AOD tự áp dụng, không viết lại điều kiện ở chỗ khác.
- Mặt đồng hồ mới → entry trong `ClockFaceValue` + nhánh trong `AodClockFace` (`AodClockFace.kt`). Màu mới → `AodsPrimitiveColors` + entry trong `ClockColorValue`. Giá trị hình ảnh mới của màn AOD → field trong `AodsClockTokens`. Ảnh nền có sẵn mới → vector `img_wallpaper_<tên>.xml` (mục 16) + entry trong `WallpaperValue` (code mới, `alignment`) + chuỗi tên.
- Hành động mới cho thao tác → entry trong `AodActionValue` + nhánh trong `AodViewModel.performGesture`. Thao tác mới → entry trong `AodGestureValue` + key DataStore + nhánh trong `gestureActionCode` / `saveGestureAction` + nơi phát `PerformGesture`.
- Thông tin thêm trên đồng hồ (kiểu ghi nhớ, sự kiện, thời tiết) → key trong `DataStoreManager` + field trong `AodExtrasUiModel` (`currentAodExtras()`) + Intent của màn Main + composable trong `AodExtras.kt` gọi từ `AodDetails`; dữ liệu lấy từ nguồn ngoài → domain model + Repository + UseCase ([mục 8](#8-domain-layer), [mục 9](#9-data-layer)), quyền runtime xin lúc user bật tuỳ chọn.
- Danh sách tính năng còn thiếu so với Always On AMOLED: [COMPARISON.md](COMPARISON.md).

---

## 21. Skills & agent (`.claude/`)

Bộ skill và agent `compose-implementer` lấy từ kotlin-accelerator-ai, bỏ DRE (dre-kt) và Supabase, đã sửa theo project này: MVI (`BaseViewModel<State, Intent, Effect>`, Screen + Content), Navigation 3, 1 module `:app`, theme luôn tối, thêm/bớt thư viện phải hỏi trước. **CLAUDE.md là chuẩn**: skill và CLAUDE.md khác nhau thì theo CLAUDE.md; rule mới của user ghi vào CLAUDE.md, không ghi vào skill.

Bản gốc có prefix `kta-`, ở đây đã bỏ. 5 tên đổi hẳn — 4 tên trùng skill / agent đã có ngoài project (`~/.claude`, plugin), `module` thì quá chung: `kta-code-review` → `kotlin-review`, `kta-compose-design-tokens` → `design-tokens`, `kta-unit-test` → `unit-testing`, `kta-module` → `gradle-module`, agent `kta-compose-developer` → `compose-implementer`. Lấy bản mới từ kotlin-accelerator-ai về thì đổi tên (cả chỗ nhắc tên trong skill) theo đúng bảng này.

`ui-ux-pro-max` lấy từ [nextlevelbuilder/ui-ux-pro-max-skill](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) (MIT, bản 2.13.0, commit `477bcb2`; bản clone ở `C:\Android Development\ui-ux-pro-max-skill`): chép nguyên thư mục skill trừ `scripts/tests/`, kèm `LICENSE`; đường dẫn script đổi từ `${CLAUDE_PLUGIN_ROOT}/…` (chỉ có khi cài dạng plugin) sang `.claude/skills/ui-ux-pro-max/scripts/search.py`, giống bộ cài `uipro init --ai claude`. Lấy bản mới: `git pull` ở bản clone rồi chép lại đúng cách này, giữ mục Project notes.

| Skill / agent | Dùng khi |
|---|---|
| `idea-pipeline` → `prd-pipeline` → `design-spec` | Ý tưởng app hay tính năng mới → PRD từng tính năng → spec từng màn cho Stitch / Figma / Claude Design |
| `ui-ux-pro-max` | Chọn hướng UI trước khi làm: style, bảng màu, cặp font, mật độ, luật UX / accessibility (tra dữ liệu CSV bằng script, luôn `--stack jetpack-compose`); chọn xong thì chuyển thành token `Aods`, không dán giá trị vào code UI |
| `design-tokens` | Sinh token 3 tầng từ Stitch / Figma / Claude Design (chỉ nhận 3 nguồn này), dựng lần lượt các màn trong `plans/…/screens-todo.md` |
| Agent `compose-implementer` | Viết UI Compose theo spec (do skill token hoặc optimizer giao); chỉ UI và ViewModel mỏng, xong phải compile + grep mục 18 |
| `compose-optimizer` | Tối ưu đúng 1 composable: recomposition, animation, tách nhỏ (hỏi user chọn rồi giao agent làm) |
| `kotlin-review` | Review code Kotlin (diff, file, branch, PR); chạy cả các lệnh grep ở mục 18 |
| `unit-testing` | Test cho UseCase, mapper / UiModel, ViewModel (JUnit 4 + kotlinx-coroutines-test; MockK, Turbine chưa có — hỏi trước khi thêm) |
| `gradle-module` | Chỉ khi user muốn tách module (hiện cố ý giữ 1 module) |

- Kết quả trung gian của các skill (idea brief, PRD, design spec, design system của `ui-ux-pro-max`, danh sách màn cần dựng, báo cáo) nằm trong `plans/` ở gốc project.
- Script Python của skill chạy bằng `python …` (Python 3, chỉ thư viện chuẩn trừ `extract-colors-from-image.py` cần Pillow).
- Đổi skill thì giữ mục "Project notes (Custom-AOD)" trong từng skill khớp với CLAUDE.md.
- **Mặc định tự làm, không tạo agent**: agent mới phải đọc lại toàn bộ ngữ cảnh, tốn token và thời gian. Skill có bước giao cho agent `compose-implementer` (`design-tokens`, `compose-optimizer`) thì tự làm theo đúng yêu cầu trong file agent đó. Chỉ dùng agent khi user bảo; agent đang chạy cùng việc thì gửi tiếp cho nó (SendMessage), không tạo agent mới.
