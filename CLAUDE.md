# CLAUDE.md

Hướng dẫn cho Claude Code (và dev) khi làm việc trong project này. **Mọi code mới phải theo đúng rule bên dưới — không trôi về pattern Android/Compose chung chung.** Code hiện có mâu thuẫn với rule → theo rule và báo lại.

## Project

**Custom AOD** — Always On Display giả lập: mỗi lần màn hình tắt, app mở một Activity nền đen có đồng hồ che màn hình khoá rồi tự bật màn hình lại. Repo: [decoutkhanqindev/Custom-AOD](https://github.com/decoutkhanqindev/Custom-AOD).

- Dựng từ base *Android-Base* ([decoutkhanqindev/Android-Base](https://github.com/decoutkhanqindev/Android-Base)): Jetpack Compose · Clean Architecture · MVI · Navigation 3 · Koin · Coroutines/Flow. Lõi AOD port từ demo FakeAOD và refactor theo rule của base — cơ chế, các điểm không được đổi và lý do: [mục 20](#20-aod-core).
- Khung kiến trúc (Clean Arch + MVI + Nav3 + Koin) lấy từ *Lịch Việt Lộc Phát* ([decoutkhanqindev/Lich-Viet-Loc-Phat](https://github.com/decoutkhanqindev/Lich-Viet-Loc-Phat)).
- Hạ tầng dùng chung (ads + consent UMP, DataStore/Language/Network manager, CoroutineExt, Modifiers, dialog) port từ *DexReader* ([decoutkhanqindev/DexReader](https://github.com/decoutkhanqindev/DexReader)), đã đổi Hilt → Koin, navigation-compose → Navigation 3.
- Package / namespace / applicationId: `com.decoutkhanqindev.custom_aod` · single module `:app`
- minSdk 30 · compileSdk/targetSdk 37 · Kotlin 2.4.20 · AGP 9.4.1 · Gradle 9.8.0 · JDK 17 (toolchain)
- Theme: `AppTheme` luôn tối (nền đen, accent mint — bảng màu của FakeAOD) · XML theme `Theme.App`, `Theme.App.Aod`
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
├── di/AppModule.kt                # Koin: managerModule · adsModule · aodModule · repositoryModule · useCaseModule · viewModelModule
├── ads/                           # AdMob + consent UMP (không nằm trong presentation dù có Compose)
│   ├── AdsManager.kt              #   consent, init MobileAds, currentActivity, isAdShowing + các placement `by lazy`
│   ├── ad_unit/                   #   AdUnit (base) · AdUnitState · Banner/Native/Interstitial/Reward/AppOpenAdUnit
│   └── composables/               #   BannerAdView · NativeAdView (+ NativeLayoutType) · AdLoadingDialog
├── domain/                        # Business thuần Kotlin (chưa dùng: app chưa có dữ liệu nghiệp vụ — mục 20)
│   ├── model/ · repository/ · usecase/
├── data/
│   ├── local/datastore/           # DataStoreManager  — prefs app-shell + tuỳ chọn AOD
│   ├── local/locale/              # LanguageManager   — locale/ngôn ngữ
│   ├── network/connectivity/      # NetworkManager    — trạng thái mạng
│   ├── device/                    # tín hiệu thiết bị cho AOD (mục 20.3)
│   │   ├── screen/                #   ScreenStateManager    — SCREEN_OFF/USER_PRESENT, isInteractive, wake lock
│   │   ├── battery/               #   BatteryStateManager   — % pin, đang sạc, đang cắm nguồn
│   │   ├── audio/                 #   AudioStateManager     — chuông / cuộc gọi / báo thức
│   │   ├── proximity/             #   ProximityManager      — cảm biến tiệm cận
│   │   └── permission/            #   PermissionManager     — overlay, thông báo, quyền riêng của Xiaomi
│   └── repository/                # XxxRepositoryImpl : XxxRepository
├── utils/                         # CoroutineExt · NavExt (navigateTo) · ContextExt (showToast, registerSystemReceiver, mở trang cài đặt) · Tag
└── presentation/
    ├── MainActivity.kt            # requestConsent, áp locale (AppLanguageProvider), AppTheme, start AodService
    ├── aod/                       # runtime AOD: AodActivity · AodService · AodSession · BootReceiver · AodTileService (mục 20)
    ├── base/BaseViewModel.kt      # MVI <State, Intent, Effect>
    ├── components/                # Modifiers · AppLottie · AppLanguageProvider · SettingsRows · dialog/NoInternetDialog
    ├── effects/                   # LaunchedWithLifecycleEffect (collect flow theo lifecycle)
    ├── model/                     # UiModel (AodOptions, AodRules, AodSchedule, Language…) · LanguageValue · AnimationContentKey · ChargingRuleValue · ScheduleTimeValue · PermissionValue · PermissionStatusValue · WakeResultValue
    ├── navigation/                # AppDestinations (NavKey) · AppNavDisplay (+ NoInternetDialog)
    ├── screens/<feature>/         # XxxScreen · XxxContent · XxxViewModel · state/{XxxState, XxxIntent, XxxEffect}
    │   ├── language/              #   chọn ngôn ngữ: lần đầu mở app (Splash → Language → Main) và từ màn Main
    │   ├── main/                  #   cài đặt AOD: công tắc, quyền, tuỳ chọn, quy tắc hiện, ngôn ngữ, xem thử (+ MainXxxSection)
    │   └── aod/                   #   đồng hồ AOD, host là AodActivity (không nằm trong NavDisplay)
    └── theme/                     # Color · Theme · Type (bảng màu tối của FakeAOD)
```

`Placeholder.kt` trong `domain/*`, `data/repository` chỉ giữ chỗ + TODO → xoá khi layer đó có file thật. `screens/language/` là màn MVI đầy đủ gọn nhất (6 file) — copy làm khung cho màn mới; `screens/main/` có thêm `MainXxxSection.kt` (Content dài tách theo mục 10.4).

### 1.1 Luồng dữ liệu

```
User ─▶ Content ──onIntent(Intent)──▶ ViewModel ──invoke()──▶ UseCase ──▶ Repository (interface, domain)
          ▲                              │                                   │ Impl ở data, tự withContext
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
| `data` | `domain` · kotlinx.coroutines · AndroidX DataStore · `utils/` | `presentation` · Compose · `ads` |
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
| Gọi API | `data/network/api/` (ApiService, response DTO) + interceptor + Koin `single` cho OkHttp/Retrofit; map `HttpException`/`IOException` → exception domain tại Repository | DexReader `data/network/api`, `di/network/ApiModule.kt` |
| DB local | `data/local/database/` (Room: Database, Dao, Entity) | DexReader `data/local/database` |
| Nguồn dữ liệu khác (asset, thuật toán…) | `data/source/<tên>/` — `XxxDataSource` + `XxxDataSourceImpl`, chỉ `data/repository` dùng | Lịch Việt `data/source` |
| Firebase (Analytics/Crashlytics/Perf) | plugin `google-services` (+ `crashlytics`, `firebase-perf`), Firebase BOM, `app/google-services.json`; bật collection chỉ ở release: `Firebase.crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG` (tương tự analytics/perf) trong `lifecycleScope.launch(Dispatchers.IO)` | DexReader `MainActivity.setUpFirebaseSdk` |
| Onboarding | Màn sau Language ở lần đầu mở app — chuyển `saveIsFirstOpen(false)` từ Language sang cuối onboarding ([mục 5](#5-ngôn-ngữ--locale)); ad: [mục 4](#4-ads--admob--consent-ump) | DexReader `screens/onboarding` |
| Widget | Glance — `presentation/widget/` | Lịch Việt `presentation/widget` |
| Service / BroadcastReceiver | `presentation/<feature>/` cạnh Activity dùng chung state (vd `presentation/aod/`); logic quyết định ở đó, truy cập hệ thống qua manager | Custom AOD `presentation/aod` (mục 20) |

---

## 2. Manager — hạ tầng app-shell

Manager = hạ tầng runtime **không phải nghiệp vụ** (prefs của app-shell, locale, kết nối mạng, tín hiệu thiết bị). Không có UseCase/Repository bọc ngoài — UseCase chỉ forward 1 dòng là dư thừa.

| Manager | Vị trí | Cung cấp |
|---|---|---|
| `DataStoreManager` | `data/local/datastore/` | `selectedLangCode`, `isFirstOpen`, tuỳ chọn AOD (`isAodEnabled`, `isAodCustomBrightness`, `aodBrightnessPercent`, `isAodProximityEnabled`, `aodTimeoutMinutes`), quy tắc hiện (`aodMinBattery`, `aodChargingRule`, `isAodScheduleEnabled`, `aodScheduleStartMinute`, `aodScheduleEndMinute`), `aodLastWake`, `isNotificationsAsked` — mỗi key 1 `StateFlow<T?>` + `saveXxx()`; `DEFAULT_*` của tuỳ chọn AOD là `const` public để nơi đọc đồng bộ có giá trị dự phòng |
| `LanguageManager` | `data/local/locale/` | `deviceLanguageCode()`, `configurationFor(code)`, `resourcesFor(config)`, `displayNameOf(code, displayIn)` |
| `NetworkManager` | `data/network/connectivity/` | `isAvailable: StateFlow<Boolean>` |
| `ScreenStateManager` | `data/device/screen/` | `events: SharedFlow<String>` (action `SCREEN_OFF` / `USER_PRESENT`), `isInteractive`, `isDeviceSecure`, `wakeUp(holdMillis)` |
| `BatteryStateManager` | `data/device/battery/` | `levelPercent: StateFlow<Int?>`, `isCharging: StateFlow<Boolean?>`, `isPlugged: StateFlow<Boolean?>` (`EXTRA_PLUGGED`: cắm nguồn kể cả khi pin đầy / giới hạn sạc), `readLevelPercent()`, `readIsCharging()`, `readIsPlugged()` |
| `AudioStateManager` | `data/device/audio/` | `isBusy: StateFlow<Boolean?>` (chuông / cuộc gọi / báo thức), `isBusyNow()` |
| `ProximityManager` | `data/device/proximity/` | `isNear: StateFlow<Boolean>` |
| `PermissionManager` | `data/device/permission/` | `isXiaomi`, `canDrawOverlays()`, `areNotificationsEnabled()`, `needsNotificationPermission()`, `isMiuiShowWhenLockedAllowed()`, `isMiuiBackgroundStartAllowed()` |
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

## 3. Mạng — NoInternetDialog

- "Mất mạng" được **quan sát**, không suy ra từ request lỗi. `AppNavDisplay` lấy `NetworkManager` bằng `koinInject()`, collect `isAvailable` và render `if (!isNetworkAvailable) NoInternetDialog()` sau `NavDisplay` → phủ mọi màn.
- `NoInternetDialog` không tắt được (Back/ngoài vùng), 1 nút mở `Settings.Panel.ACTION_INTERNET_CONNECTIVITY` (minSdk 30 nên luôn có); tự biến mất khi có mạng lại.
- Custom AOD giữ dialog này như base: màn cài đặt cần mạng (Splash chờ consent), còn AOD vẫn chạy offline vì do `AodService` điều khiển.
- Màn hình không cần lỗi riêng "offline" — request lỗi hiện lỗi chung, dialog là tín hiệu offline duy nhất.
- App chạy được offline → bỏ dòng `NoInternetDialog()` trong `AppNavDisplay`.

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
  - Lần đầu mở app (`isFirstOpen == true`): Splash `navigateTo(LanguageDestination(isFirstOpen = true), preserveState = false)`. Không có nút back, chọn sẵn ngôn ngữ của máy (chưa có bản dịch → English); Xong gọi thêm `saveIsFirstOpen(false)` rồi `navigateTo(MainDestination, preserveState = false)`. Có onboarding thì chuyển `saveIsFirstOpen(false)` sang cuối onboarding.
  - Từ màn Main (mục "Ứng dụng › Ngôn ngữ"): `backStack.add(LanguageDestination(isFirstOpen = false))`; Xong chỉ bật khi chọn khác ngôn ngữ đang dùng, lưu xong thì quay lại.
  - Thêm bản dịch: `values-<qualifier>/strings.xml` + thêm vào `LanguageValue.TRANSLATED`.

---

## 6. Quy ước đặt tên

| Thành phần | Quy ước | Ví dụ |
|---|---|---|
| Destination | `XxxDestination` | `MainDestination`, `LanguageDestination(isFirstOpen)` |
| Màn | `XxxScreen` · `XxxContent` · `XxxViewModel` | `MainScreen` |
| MVI | `XxxState` · `XxxIntent` · `XxxEffect` | `MainState` |
| UseCase | `VerbNounUseCase` | `GetDailyMetadataUseCase` |
| Repository | `XxxRepository` / `XxxRepositoryImpl` | |
| DataSource | `XxxDataSource` / `XxxDataSourceImpl` | |
| Manager | `XxxManager` (1 class, không `Impl`) | `NetworkManager` |
| Ad unit / placement | `XxxAdUnit` · placement `<format><Place>` · name `"<format>_<place>_<floor>"` | `interSplash`, `"inter_splash_all"` |
| UiModel | `XxxUiModel` + `fun Xxx.toUiModel()` | |
| Enum giá trị UI | `XxxValue` | `LanguageValue` |
| Component bọc thư viện dùng chung | `AppXxx` | `AppLottie` |
| Package | lowercase, nhiều từ → snake_case | `ad_unit` |
| Token màu | PascalCase mô tả giá trị, alpha `<Base>Alpha<percent>` | `WhiteAlpha30` |
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
| Serialization | plugin `kotlin-serialization` | `@Serializable` cho NavKey |
| Log | Timber 5.0.1 | KHÔNG `Log.*`/`println` |
| Animation | lottie-compose 6.7.1 | Chỉ qua `AppLottie` — không gọi `LottieAnimation` trực tiếp |
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
- **Main-safe tại Repository/Manager**: `withContext(Dispatchers.IO)` cho IO, `Dispatchers.Default` cho CPU — thường qua `withContextCatching(context = Dispatchers.IO, action = { … }, catch = { … })`. UseCase/ViewModel không tự `withContext`.
- Repository được ném exception (UseCase bọc `Result`); không trả `null` để báo lỗi. Map exception thư viện (HTTP, IO, DB…) sang exception có nghĩa tại biên data.
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
- Content thuần nên `@Preview` được (preview `private`, bọc `AppTheme`, `XxxState(...)` mẫu, `onIntent = {}`).

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
- Root `AppNavDisplay` chỉ chứa màn full-screen; tab nằm ở NavDisplay lồng ([11.5](#115-nested-navigation-bottom-tab)). `NoInternetDialog` render sau `NavDisplay`.

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

- `di/AppModule.kt`: `managerModule` · `adsModule` · `aodModule` · `repositoryModule` · `useCaseModule` · `viewModelModule` → `appModules`, load ở `App.onCreate()` (`startKoin { androidContext(this@App); modules(appModules) }`).

| Loại | Khai báo | Lý do |
|---|---|---|
| Manager | `single { XxxManager(androidApplication()) }` | State holder, 1 instance |
| AdsManager | `single { AdsManager() }` | `Application`/`NetworkManager` lấy trong `AdsManager` qua `by inject()`; consent bắt đầu từ `MainActivity` (`requestConsent`) |
| AodSession | `single { AodSession(get()) }` (`aodModule`) | State dùng chung giữa `AodService` và `AodActivity` (mục 20) |
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
| `withContextCatching(context, action, catch)` | `T` | `Exception` | Repository / Manager (đổi dispatcher + map/log lỗi): `DataStoreManager.edit`, init MobileAds của `AdsManager` |
| `Flow<T>.collectCatching(action, catch)` | — (terminal) | `Exception` từ upstream **và** thân `action` | **Mọi** chỗ collect flow của manager/UseCase: ViewModel, Service, TileService, manager (`AdsManager` collect `isAvailable`) |
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
- Chỉ 2 chỗ được collect thô (grep ở [mục 18](#18-banned-patterns) không báo 2 chỗ này):
  - `viewModel.effect.collect { }` trong `LaunchedWithLifecycleEffect` của Screen (khung [mục 10.4](#104-screen-vs-content)): `SharedFlow` của `BaseViewModel` không ném lỗi.
  - `collectLatest` khi phải huỷ khối đang chạy lúc có giá trị mới — helper không có bản "latest": `AodViewModel.observeProximity` (chờ che 3 giây), `snapshotFlow` trong `Modifier.onClick`. Chỉ với `StateFlow` của manager (đã `recoverCatching` trong manager) hoặc state Compose.
- Đọc giá trị của manager **1 lần** → `.value` (đồng bộ, `null` → `DEFAULT_*`) hoặc `filterNotNull().first()` (chờ nạp xong); không cần bọc vì `StateFlow` không ném lỗi, lỗi đọc đã được manager `recoverCatching`.
- Manager: nguồn `callbackFlow` / `prefs.data` → `recoverCatching { … }` trước `shareIn`/`stateIn`; ghi hoặc IO → `withContextCatching(…)` trong `scope.launch` của manager.
- Lời gọi suspend 1 lần có thể ném → `suspendRunCatching { }.onSuccess { }.onFailure { }` — không `try/catch`, không `runCatching`.
- Hàm **đồng bộ** của hệ thống ném exception có tên (vd `startForegroundService` ném `ForegroundServiceStartNotAllowedException`) → `try/catch` đúng loại đó như `AodService.start()`; helper `-Catching` là hàm suspend, không dùng ở đây.
- Scope: `viewModelScope` (VM) · `LaunchedEffect`/`rememberCoroutineScope`/`LaunchedWithLifecycleEffect` (Compose) · `lifecycleScope` (Activity) · manager/ad unit tự tạo scope · `AodService` tự tạo scope `Dispatchers.Main.immediate`, huỷ ở `onDestroy` · `BootReceiver` tạo scope ngắn cho `goAsync()`.
- Dispatcher chọn ở Repository/Manager, không ở VM/UseCase.
- Banned: `GlobalScope`, `runBlocking` trong code app, `Thread.sleep`.

## 14. Compose & theme

- **Theme luôn tối** (bảng màu của FakeAOD — app nói về một màn hình phần lớn thời gian tắt, nên nền đen chứ không phải xám tối): `AppTheme { }` với 1 `darkColorScheme` — `primary` Mint, nền/surface `Black`, chữ `GreyED`, `onSurfaceVariant` `Grey9A`, `error` Red; không dynamic color, không theo sáng/tối của hệ thống. `Type.kt` chỉ override `bodyLarge` (font: TODO theo design). XML `Theme.App` có `windowBackground` đen để không loé trắng trước khung Compose đầu tiên.
- UI dùng **role của MaterialTheme**: `MaterialTheme.colorScheme.<role>` (alpha biến thể viết inline `colorScheme.onSurface.copy(alpha = 0.6f)`), `MaterialTheme.typography.<role>`, `MaterialTheme.shapes.<role>`. Màu cố định ngoài scheme (overlay trong suốt…) → token trong `Color.kt` (`WhiteAlpha30`, `BlackAlpha50`). `Color.Transparent` dùng thẳng. Hex chỉ ở `Color.kt` (ngoại lệ: layout/drawable XML của native ad).
- Màn AOD (`AodContent`) vẽ **không có `AppTheme`**, cả lúc chạy lẫn `@Preview`: màu và cỡ chữ là token/giá trị cố định (`GreyB4` giờ, `Grey8A` ngày, `Grey6E` pin, `Grey5A` gợi ý — xám chứ không trắng: ít sáng, ít tốn pin, ít burn-in). Bọc `AppTheme` thì `Text` nhận `lineHeight`/`letterSpacing` của `bodyLarge` và đồng hồ 76sp bị đè lên dòng ngày.
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
| Dialog mất mạng | `NoInternetDialog()` (đã gắn ở `AppNavDisplay`) | `components/dialog/` |
| Dòng cài đặt: tiêu đề mục, công tắc, radio, dòng giá trị bấm được, thanh trượt theo nấc | `SettingsSectionHeader` · `SettingsSwitchRow` · `SettingsRadioRow` · `SettingsValueRow` · `SettingsSlider(valueRange, step)` | `components/SettingsRows.kt` |
| Animation Lottie (lặp vô hạn) | `AppLottie(resId = R.raw.x, modifier = …)` | `components/AppLottie.kt` |
| Manager trong Compose | `val x: XxxManager = koinInject()` (Screen/host) | `di/AppModule.kt` |
| Banner / Native / loading ad | `BannerAdView` · `NativeAdView` · `AdLoadingDialog` | `ads/composables/` |
| Danh sách ngôn ngữ | `LanguageValue` | `presentation/model/` |
| Key `AnimatedContent` | `AnimationContentKey` | `presentation/model/` |
| Coroutine an toàn huỷ (chọn helper: [mục 13.1](#131-chọn-helper--bắt-buộc-cho-code-mới)) | `suspendRunCatching` · `withContextCatching` · `collectCatching` · `recoverCatching` | `utils/CoroutineExt.kt` |
| Điều hướng | `NavBackStack.navigateTo(dest, preserveState)` | `utils/NavExt.kt` |
| Toast | `context.showToast(message)` | `utils/ContextExt.kt` |
| Đăng ký receiver cho broadcast hệ thống (`RECEIVER_NOT_EXPORTED` từ Android 13; receiver `null` = đọc broadcast sticky) | `context.registerSystemReceiver(receiver, filter)` | `utils/ContextExt.kt` |
| Mở trang cài đặt hệ thống (Context của Activity; ROM không có trang đó thì mở Thông tin ứng dụng) | `context.openSettingsPage(intent)` · `context.openOverlaySettings()` · `context.openNotificationSettings()` · `context.openMiuiPermissionEditor()` · `context.packageUri()` | `utils/ContextExt.kt` |
| Áp ngôn ngữ đã chọn cho một Activity (`LocalConfiguration` / `LocalResources`) | `setContent { AppLanguageProvider { … } }` — tự `koinInject` `DataStoreManager` (`selectedLangCode`) và `LanguageManager` | `components/AppLanguageProvider.kt` |
| Tag log | `: Tag` → `Timber.tag(tag)` | `utils/Tag.kt` |
| Khung màn MVI | copy `screens/main/` | `presentation/screens/main/` |

Quy tắc:
- Trước khi viết composable/util mới → kiểm tra bảng trên. Dùng ở ≥ 2 nơi → `components/` (UI) hoặc `utils/` (không UI); 1 nơi → `private` trong file đó.
- Component dùng chung không tự `koinInject`/`koinViewModel`; nhận data + callback qua tham số. Ngoại lệ: host bọc cả màn của Activity (`AppNavDisplay`, `AppLanguageProvider`) được `koinInject` manager.
- Không copy-paste: logic giữa ViewModel → UseCase; UI giữa màn → component; giá trị lặp → token/`const val`.
- Không tạo top-level `val` trung gian dùng 1 lần → inline (trừ token theme, `const val` cho magic number, giá trị tính sẵn để khỏi tính lại mỗi frame).
- **File Compose (Screen / Content / component) không khai báo `val`, `const val` hay class phụ ở top-level:**
  - Số chỉ phục vụ UI viết inline, có tên tham số: `tween(durationMillis = 600)`, `20.dp`.
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
| Lottie | `res/raw/<tên>.json` | `AppLottie(resId = R.raw.x, modifier = …)` (lặp vô hạn) |
| Native ad | `res/layout/native_ad_*.xml` + `res/drawable/bg_*` | XML layout DUY NHẤT được phép |
| Launcher icon | `res/mipmap-anydpi/` + `drawable/ic_launcher_*` | Android Studio › New › Image Asset |

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
| `.collect { }` thô với flow của manager/UseCase (ViewModel, Service, TileService…) | `collectCatching(action = …, catch = …)` ([mục 13.1](#131-chọn-helper--bắt-buộc-cho-code-mới)) |
| `repeatOnLifecycle` thô rải trong từng Screen · collect effect ngoài helper lifecycle | `LaunchedWithLifecycleEffect { }` (`presentation/effects/`) · `LifecycleResumeEffect` |
| `context.getString` cho text UI | `stringResource` · `LocalResources.current.getString` |
| Hex `Color(0x…)`, `Color.White/Black`, `RoundedCornerShape(n.dp)` trong UI | `MaterialTheme.colorScheme/shapes` · token `Color.kt` |
| Text hardcode trong composable | `strings.xml` |
| `collectAsState()` | `collectAsStateWithLifecycle()` |
| `Modifier.clickable` ngoài `Modifiers.kt` | `Modifier.onClick` |
| `LottieAnimation` ngoài `AppLottie.kt` | `AppLottie` |
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
grep -rn "LottieAnimation(" app/src/main/java --include="*.kt" | grep -v "/components/AppLottie.kt"
grep -rn "LiveData\|dagger\.hilt\|androidx\.navigation\.compose" app/src/main/java --include="*.kt"
grep -rn "val .*: \(Mutable\)\?List<" app/src/main/java --include="*State.kt" --include="*UiModel.kt"
grep -rn "catch (.*: CancellationException)" app/src/main/java --include="*.kt" | grep -v "/ads/ad_unit/\|/utils/CoroutineExt.kt"
grep -rn "\.collect {" app/src/main/java --include="*.kt" | grep -v "\.effect\.collect {"
grep -rn "context\.getString\|activity\.getString" app/src/main/java --include="*.kt"
grep -rln "^import android\.\|^import androidx\." app/src/main/java --include="*.kt" | grep "/domain/"
grep -rn "import .*\.data\.repository\.\|import .*Impl$" app/src/main/java --include="*.kt" | grep "/presentation/"
grep -rn "import .*\.domain\.repository\.\|import .*Impl$" app/src/main/java --include="*ViewModel.kt"
grep -rn "Timber" app/src/main/java --include="*AdUnit.kt" | grep -v "/ad_unit/AdUnit.kt"
grep -rn "ManagerImpl\|BaseAds" app/src/main/java --include="*.kt"
```

---

## 19. Checklist thêm feature mới

1. `domain/model/` — domain model.
2. `domain/repository/XxxRepository.kt` — interface.
3. Nguồn dữ liệu (`data/network/…`, `data/local/…`, `data/source/…`) → `data/repository/XxxRepositoryImpl.kt` (main-safe, map → domain).
4. `domain/usecase/` — UseCase ([mục 8](#8-domain-layer)).
5. `di/AppModule.kt` — `repositoryModule` · `useCaseModule` · `viewModelModule`.
6. `presentation/model/XxxUiModel.kt` (+ `toUiModel()`) nếu cần.
7. `presentation/screens/xxx/` — copy khung `screens/main/` (State · Intent · Effect · ViewModel · Content · Screen).
8. `AppDestinations.kt` — `XxxDestination`; đăng ký `entry<XxxDestination>` ở `AppNavDisplay` (hoặc NavDisplay lồng).
9. Ad cho màn: placement `by lazy` trong `AdsManager` + `<PLACEMENT>_ALL_ID` ở `release {}`/`debug {}`; native/banner truyền slot từ Screen.
10. Text vào `strings.xml` (+ bản dịch nếu project có), màu cố định mới vào `Color.kt`.
11. Xoá `Placeholder.kt` của layer vừa có file thật.
12. `./gradlew :app:compileDebugKotlin` + chạy grep [mục 18](#18-banned-patterns).

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
```

### 20.2 Thành phần

| Thành phần | Vai trò |
|---|---|
| `presentation/aod/AodService` | Foreground service `specialUse`. Collect `ScreenStateManager.events`, quyết định có mở AOD không, mở AOD, đưa về màn hình khoá khi bấm nguồn, `checkLaunch()` 2 giây sau mỗi lần mở để ghi `aodLastWake`. Thông báo thường trực theo ngôn ngữ đã chọn trong app: đăng lại mỗi lần start và khi `selectedLangCode` đổi |
| `presentation/aod/AodActivity` | Host của `AodScreen`. Giữ mọi thao tác cửa sổ: cờ, độ sáng, ẩn thanh hệ thống, `renderDark()`. Áp ngôn ngữ bằng `AppLanguageProvider` |
| `presentation/aod/AodSession` | Koin `single`, chỉ dùng trên main thread. `WeakReference` tới `AodActivity`; `isShowing`, `isCovered`, `shownAt`, sleep request; `finish()` đóng AOD ngay |
| `presentation/aod/BootReceiver` | `BOOT_COMPLETED` / `MY_PACKAGE_REPLACED` → start service nếu đang bật (`goAsync()` trong lúc đọc DataStore) |
| `presentation/aod/AodTileService` | Ô Cài đặt nhanh (`TOGGLEABLE_TILE`): hiện và đảo `isAodEnabled`, start/stop service; `AodService.start()` trả `false` → mở app bằng `startActivityAndCollapse` (đang khoá → `unlockAndRun` trước) |
| `presentation/model/AodRulesUiModel` | Quy tắc hiện: `minBattery`, `chargingRule` (`ChargingRuleValue`), `schedule` (`AodScheduleUiModel`). `allows()` dùng chung cho `AodService.shouldEnter()` và `AodViewModel.checkRules()`; `DataStoreManager.currentAodRules()` đọc snapshot đồng bộ |
| `presentation/screens/aod/` | `AodViewModel` (khi nào tối, sáng lại, đóng; mỗi phút cập nhật giờ, dịch vị trí và kiểm quy tắc hiện; ẩn dòng gợi ý sau 3 giây) · `AodContent` (vẽ đồng hồ từ state, không tự đếm giờ) · `AodScreen` (collect effect, chuyển `isDark` cho Activity) |
| `presentation/screens/main/` | Cài đặt: công tắc, danh sách quyền, tuỳ chọn (độ sáng, tiệm cận, hết giờ), quy tắc hiện (nguồn điện, khung giờ, ngưỡng pin), ngôn ngữ, xem thử, kết quả lần mở gần nhất, hỏi quyền thông báo lần đầu |
| `data/device/*` | `ScreenStateManager` · `BatteryStateManager` · `AudioStateManager` · `ProximityManager` · `PermissionManager` (mục 2) |
| `DataStoreManager` | Tuỳ chọn và quy tắc hiện của AOD, `aodLastWake` (mã của `WakeResultValue`), `isNotificationsAsked` |

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

### 20.4 Chỗ khác MVI / Manager chuẩn, có lý do

- `AodActivity` không phải entry của `NavDisplay` và vẫn tự làm thao tác cửa sổ, vì ViewModel không được chạm `Context`/`Window`. `AodViewModel` chỉ quyết định `isDark`; `AodScreen` chuyển giá trị đó cho Activity qua `onDarkChange`.
- `AodService` không có UI nên không có ViewModel: logic quyết định nằm trong service, mọi truy cập hệ thống đi qua manager.
- Domain chưa dùng: tuỳ chọn AOD là prefs nên dùng thẳng `DataStoreManager` (như `isDark` của DexReader, `showCanChiOnCell` của Lịch Việt); bọc Repository/UseCase chỉ forward manager là banned (mục 18).
- `BootReceiver` là `KoinComponent` (mục 12).
- `AodContent` không bọc `AppTheme` (mục 14).
- `SettingsSwitchRow` dùng `Modifier.toggleable(role = Role.Switch)` và `SettingsRadioRow` dùng `Modifier.selectable(role = Role.RadioButton)`, không dùng `Modifier.onClick`: TalkBack đọc được trạng thái bật/tắt, đã chọn. Dòng chỉ để bấm (`SettingsValueRow`) vẫn dùng `Modifier.onClick`.
- `AodTileService` gọi `startActivityAndCollapse(Intent)` (deprecated) dưới API 34 với `@Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")`: bản nhận `PendingIntent` chỉ có từ API 34, bản nhận `Intent` chỉ ném lỗi từ Android 14.

### 20.5 Ads và AOD

- Không bao giờ load/hiện ads hay consent trên `AodActivity`: nó nằm trên màn hình khoá. Hiện tại ads chỉ được load từ màn trong `MainActivity`, và `AdsManager` chỉ được tạo khi `MainActivity` mở.
- Giới hạn đã biết: `AdsManager` giữ `currentActivity` qua `ActivityLifecycleCallbacks`, nên nếu form consent tải xong đúng lúc AOD đang hiện thì form có thể hiện trên AOD. Chưa xử lý vì `ads/` không được import `presentation/aod`. Thêm app-open ad thì phải bỏ qua `AodActivity`.

### 20.6 Thêm tính năng cho AOD

- Tín hiệu mới (thông báo, sạc…) → manager mới trong `data/device/<tên>/` theo mục 2.
- Tuỳ chọn mới → key trong `DataStoreManager` + field trong `AodOptionsUiModel` + Intent của màn Main + đọc snapshot trong `AodViewModel` (hoặc `AodActivity` nếu là thao tác cửa sổ).
- Quy tắc "khi nào hiện" mới → key trong `DataStoreManager` + field trong `AodRulesUiModel` (`allows()` và `currentAodRules()`) + Intent của màn Main: service và màn AOD tự áp dụng, không viết lại điều kiện ở chỗ khác.
- Danh sách tính năng còn thiếu so với Always On AMOLED: [COMPARISON.md](COMPARISON.md).
