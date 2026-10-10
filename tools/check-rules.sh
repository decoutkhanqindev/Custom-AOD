#!/usr/bin/env bash
# Kiểm tra rule của CLAUDE.md bằng grep. Chạy ở bất kỳ đâu: bash tools/check-rules.sh
# Mỗi rule phải ra 0 dòng. Rule vi phạm in ra các dòng sai kèm cách sửa; thoát mã 1 nếu có vi phạm.
# Thêm rule mới: một dòng `check "rule" "thay bằng gì" 'lệnh in ra các dòng vi phạm'` (dùng $SRC / $PKG).

cd "$(dirname "$0")/.." || exit 2

SRC=app/src/main/java
PKG=$SRC/com/decoutkhanqindev/custom_aod
total=0
violations=0

check() {
  local out
  total=$((total + 1))
  out=$(eval "$3" 2>/dev/null)
  if [ -n "$out" ]; then
    violations=$((violations + 1))
    printf '[FAIL] %s\n       -> %s\n%s\n\n' "$1" "$2" "$(printf '%s\n' "$out" | sed 's/^/       /')"
  fi
}

# --- Theme và UI -------------------------------------------------------------------------------
check "Hex / Color.White / Color.Black ngoài theme" \
  "màu theo tên trong presentation/theme/Colors.kt" \
  'grep -rn "Color(0x\|Color\.White\|Color\.Black" $SRC --include="*.kt" | grep -v "/presentation/theme/"'
check "RoundedCornerShape(...) ngoài theme" \
  "RoundedCornerShape16dp... trong Shapes.kt, hoặc CircleShape" \
  'grep -rn "RoundedCornerShape(" $SRC --include="*.kt" | grep -v "/presentation/theme/"'
check "presentation/theme có file lạ" \
  "chỉ Theme / Colors / Typography / Shapes" \
  'ls $PKG/presentation/theme | grep -v "^\(Theme\|Colors\|Typography\|Shapes\)\.kt$"'
check "Tiền tố Aods" \
  "tên thường: Colors, Typography, Shapes, Theme" \
  'grep -rn "Aods[A-Z]" $SRC --include="*.kt"'
check "Tầng token / đường vòng của theme" \
  "đọc thẳng Colors / Typography / Shapes, số dp / sp / thời lượng viết thẳng" \
  'grep -rn "LocalAods\|Aods[A-Za-z]*Tokens\b\|AodsPrimitive" $SRC --include="*.kt"'
check "Accessor Theme.xxx" \
  "import từng giá trị top-level của theme" \
  'grep -rn "Theme\.[a-z]" $SRC --include="*.kt"'
check "MaterialTheme.* ngoài theme" \
  "Colors / Typography / Shapes" \
  'grep -rn "MaterialTheme\." $SRC --include="*.kt" | grep -v "/presentation/theme/"'
check ".copy(...) trên màu theme" \
  "biến thể có sẵn <Màu>Alpha<NN> trong Colors.kt" \
  'grep -rnE "\b(Black|White|Mint|Red|Blue|Purple|Orange|Pink|GreyED|GreyB4|Grey9A|Grey8A|Grey6E|Grey5A|Neutral12|NeutralVariant30|NeutralVariant60)\.copy\(" $SRC --include="*.kt" | grep -v "/presentation/theme/"'
check "Modifier.clickable ngoài AppModifiers" \
  "Modifier.onClick" \
  'grep -rn "\.clickable(" $SRC --include="*.kt" | grep -v "/components/AppModifiers.kt"'
check "LottieAnimation ngoài AppLottie" \
  "AppLottie" \
  'grep -rn "LottieAnimation(" $SRC --include="*.kt" | grep -v "/components/AppLottie.kt"'
check "context.getString / activity.getString cho text UI" \
  "stringResource hoặc LocalResources.current.getString" \
  'grep -rn "context\.getString\|activity\.getString" $SRC --include="*.kt"'

# --- Component, tên và access --------------------------------------------------------------------
check "Hàm composable dùng chung không có tiền tố App" \
  "AppXxx (trừ components/aod/)" \
  'grep -rnE "^fun [A-Z]" $PKG/presentation/components --include="*.kt" | grep -v "/components/aod/\|fun App\|fun Modifier\."'
check "File trong presentation/components không tên AppXxx.kt" \
  "đặt tên file AppXxx.kt (trừ components/aod/)" \
  'find $PKG/presentation/components -name "*.kt" -not -path "*/components/aod/*" -not -name "App*.kt"'
check "Dùng internal" \
  "private hoặc public" \
  'grep -rn "^internal " $SRC --include="*.kt"'
check "ViewModel tự implement Tag" \
  "BaseViewModel đã implement Tag" \
  'grep -rn "utils\.Tag\|, Tag" $SRC --include="*ViewModel.kt" | grep -v "/base/BaseViewModel.kt"'
check "Intent / Effect đặt tên thì quá khứ" \
  "mẫu ở CLAUDE.md › Presentation: MVI (BackgroundPickerResult, OpenPreview, ConfirmLanguage...)" \
  'grep -rnE "data (object|class) [A-Za-z]+ed\b" $SRC --include="*Intent.kt" --include="*Effect.kt"'

# --- MVI, Compose, Koin -------------------------------------------------------------------------
check "Koin / CompositionLocal tự viết trong Content" \
  "Koin chỉ ở Screen hoặc host; Content nhận giá trị qua tham số" \
  'grep -rn "koinInject\|koinViewModel\|koinActivityViewModel\|staticCompositionLocalOf" $SRC --include="*Content.kt"'
check "collectAsState() / repeatOnLifecycle ngoài presentation/effects" \
  "collectAsStateWithLifecycle() · LaunchedWithLifecycleEffect" \
  'grep -rn "collectAsState()\|repeatOnLifecycle" $SRC --include="*.kt" | grep -v "/presentation/effects/"'
check "State / UiModel dùng List / MutableList" \
  "ImmutableList" \
  'grep -rn "val .*: \(Mutable\)\?List<" $SRC --include="*State.kt" --include="*UiModel.kt"'
check "backStack.remove..." \
  "backStack.navigateBack()" \
  'grep -rn "backStack\.remove" $SRC --include="*.kt"'
check "LiveData / Hilt / navigation-compose" \
  "StateFlow · Koin · Navigation 3" \
  'grep -rn "LiveData\|dagger\.hilt\|androidx\.navigation\.compose" $SRC --include="*.kt"'

# --- Coroutines -----------------------------------------------------------------------------------
check "GlobalScope / runBlocking / Thread.sleep" \
  "scope có lifecycle, delay" \
  'grep -rn "GlobalScope\|runBlocking\|Thread\.sleep" $SRC --include="*.kt"'
check "catch (CancellationException) tự viết" \
  "helper -Catching trong utils/CoroutineExt.kt (ngoại lệ: ads/ad_unit)" \
  'grep -rn "catch (.*: CancellationException)" $SRC --include="*.kt" | grep -v "/ads/ad_unit/\|/utils/CoroutineExt.kt"'
check ".collect { } thô" \
  "collectCatching(block = ..., catch = ...)" \
  'grep -rn "\.collect {" $SRC --include="*.kt"'
check ".collectLatest { } thô" \
  "collectLatestCatching(block = ..., catch = ...) (ngoại lệ: snapshotFlow trong AppModifiers)" \
  'grep -rn "\.collectLatest {" $SRC --include="*.kt" | grep -v "/components/AppModifiers.kt"'
check "withContext(...) thô" \
  "withContextCatching(context = ..., block = ..., catch = ...)" \
  'grep -rn "withContext(" $SRC --include="*.kt" | grep -v "/utils/CoroutineExt.kt"'

# --- Layer, log, ads ------------------------------------------------------------------------------
check "Log.* / println" \
  "Timber.tag(tag)" \
  'grep -rn "Log\.[vdiwe](\|println(" $SRC --include="*.kt"'
check "domain import android / androidx" \
  "domain chỉ Kotlin thuần" \
  'grep -rln "^import android\.\|^import androidx\." $SRC --include="*.kt" | grep "/domain/"'
check "presentation import Repository Impl / data repository" \
  "UseCase (nghiệp vụ) hoặc Manager (hạ tầng)" \
  'grep -rn "import .*\.data\.repository\.\|import .*Impl$" $SRC --include="*.kt" | grep "/presentation/"'
check "ViewModel import domain repository / Impl" \
  "UseCase hoặc Manager" \
  'grep -rn "import .*\.domain\.repository\.\|import .*Impl$" $SRC --include="*ViewModel.kt"'
check "Timber trực tiếp trong ad unit con" \
  "log(\"Action\") của AdUnit" \
  'grep -rn "Timber" $SRC --include="*AdUnit.kt" | grep -v "/ad_unit/AdUnit.kt"'
check "ManagerImpl / BaseAds" \
  "1 class cụ thể XxxManager / AdsManager" \
  'grep -rn "ManagerImpl\|BaseAds" $SRC --include="*.kt"'

if [ "$violations" -eq 0 ]; then
  echo "[OK] $total rule, 0 vi phạm"
else
  echo "[FAIL] $violations/$total rule bị vi phạm"
  exit 1
fi
