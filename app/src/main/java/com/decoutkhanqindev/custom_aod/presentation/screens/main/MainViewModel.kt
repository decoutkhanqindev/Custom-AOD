package com.decoutkhanqindev.custom_aod.presentation.screens.main

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.data.device.flashlight.FlashlightManager
import com.decoutkhanqindev.custom_aod.data.device.light.AmbientLightManager
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.device.pickup.PickupGestureManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.image.AodImageManager
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import com.decoutkhanqindev.custom_aod.domain.usecase.RefreshWeatherUseCase
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodExtrasUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodInteractionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodRulesUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.ScheduleTimeValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.model.aodPermissions
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodAppearance
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodExtras
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodInteraction
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodNotificationOptions
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodOptions
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodRules
import com.decoutkhanqindev.custom_aod.presentation.model.saveGestureAction
import com.decoutkhanqindev.custom_aod.presentation.model.toUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainInteractionState
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainState
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val dataStoreManager: DataStoreManager,
    private val permissionManager: PermissionManager,
    private val languageManager: LanguageManager,
    private val aodImageManager: AodImageManager,
    flashlightManager: FlashlightManager,
    ambientLightManager: AmbientLightManager,
    pickupGestureManager: PickupGestureManager,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
) : BaseViewModel<MainState, MainIntent, MainEffect>(
    initialState = MainState(
        interaction = MainInteractionState(
            isFlashlightAvailable = flashlightManager.isAvailable,
            isLightSensorAvailable = ambientLightManager.isAvailable,
            isPickupSensorAvailable = pickupGestureManager.isSupported,
        ),
    ),
), Tag {

    // Quyền lịch / vị trí đang được hỏi từ danh sách quyền (chỉ để cấp) hay từ công tắc "Sự kiện hôm nay" / "Thời tiết" (cấp xong thì bật luôn mục đó).
    private var permissionRequestedFromList: PermissionValue? = null

    init {
        observeSettings()
        observeBackground()
        observeWallpaper()
        observeExtras()
        observeDrawing()
        observeLanguage()
        observeLastWake()
    }

    override fun onIntent(intent: MainIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        if (intent.isAodRelated) showPermissionSheet()
        when (intent) {
            is MainIntent.Options -> onOptionsIntent(intent)
            is MainIntent.Appearance -> onAppearanceIntent(intent)
            is MainIntent.Extras -> onExtrasIntent(intent)
            is MainIntent.Notifications -> onNotificationsIntent(intent)
            is MainIntent.Interaction -> onInteractionIntent(intent)
            is MainIntent.Rules -> onRulesIntent(intent)
            is MainIntent.Permission -> onPermissionIntent(intent)
            is MainIntent.ToggleAod -> toggleAod(intent.isEnabled)
            is MainIntent.NavigateToLanguage ->
                viewModelScope.launch { sendEffect(MainEffect.Navigation.NavigateToLanguage) }

            is MainIntent.OpenPreview -> viewModelScope.launch { sendEffect(MainEffect.Navigation.OpenPreview) }
        }
    }

    private fun onOptionsIntent(intent: MainIntent.Options) {
        when (intent) {
            is MainIntent.Options.ToggleCustomBrightness -> dataStoreManager.saveIsAodCustomBrightness(intent.isEnabled)
            is MainIntent.Options.ChangeBrightness -> dataStoreManager.saveAodBrightnessPercent(intent.percent)
            is MainIntent.Options.ToggleProximity -> dataStoreManager.saveIsAodProximityEnabled(intent.isEnabled)
            is MainIntent.Options.ChangeTimeout -> dataStoreManager.saveAodTimeoutMinutes(intent.minutes)
        }
    }

    private fun onAppearanceIntent(intent: MainIntent.Appearance) {
        when (intent) {
            is MainIntent.Appearance.ChangeClockFace -> dataStoreManager.saveAodClockFace(intent.face.code)
            is MainIntent.Appearance.ChangeClockFont -> dataStoreManager.saveAodClockFont(intent.font.code)
            is MainIntent.Appearance.ChangeClockColor -> dataStoreManager.saveAodClockColor(intent.color.code)
            is MainIntent.Appearance.ChangeClockSize -> dataStoreManager.saveAodClockSizePercent(intent.percent)
            is MainIntent.Appearance.ToggleLandscape -> dataStoreManager.saveIsAodLandscape(intent.isEnabled)
            is MainIntent.Appearance.SelectWallpaper -> selectWallpaper(intent.wallpaper)
            is MainIntent.Appearance.OpenBackgroundPicker ->
                viewModelScope.launch { sendEffect(MainEffect.Appearance.OpenBackgroundPicker) }

            is MainIntent.Appearance.BackgroundPickerResult -> onBackgroundPickerResult(intent.uri)
            is MainIntent.Appearance.RemoveBackground -> removeBackground()
        }
    }

    private fun onExtrasIntent(intent: MainIntent.Extras) {
        when (intent) {
            is MainIntent.Extras.ToggleDate -> dataStoreManager.saveIsAodDateEnabled(intent.isEnabled)
            is MainIntent.Extras.ToggleBattery -> dataStoreManager.saveIsAodBatteryEnabled(intent.isEnabled)
            is MainIntent.Extras.ShowMemoEditor -> updateState { copy(extras = extras.copy(isMemoEditorVisible = true)) }
            is MainIntent.Extras.DismissMemoEditor ->
                updateState { copy(extras = extras.copy(isMemoEditorVisible = false)) }

            is MainIntent.Extras.ChangeMemo -> changeMemo(intent.memo)
            is MainIntent.Extras.ShowDrawingPad -> updateState { copy(extras = extras.copy(isDrawingPadVisible = true)) }
            is MainIntent.Extras.DismissDrawingPad ->
                updateState { copy(extras = extras.copy(isDrawingPadVisible = false)) }

            is MainIntent.Extras.ChangeDrawing -> changeDrawing(intent.drawing)
            is MainIntent.Extras.RemoveDrawing -> aodImageManager.removeDrawing()
            is MainIntent.Extras.ToggleCalendar -> toggleCalendar(intent.isEnabled)
            is MainIntent.Extras.ToggleWeather -> toggleWeather(intent.isEnabled)
            is MainIntent.Extras.ToggleWeatherFahrenheit -> dataStoreManager.saveIsAodWeatherFahrenheit(intent.isEnabled)
        }
    }

    private fun onNotificationsIntent(intent: MainIntent.Notifications) {
        when (intent) {
            is MainIntent.Notifications.ToggleNotificationIcons ->
                dataStoreManager.saveIsAodNotificationIconsEnabled(intent.isEnabled)

            is MainIntent.Notifications.ToggleNotificationContent ->
                dataStoreManager.saveIsAodNotificationContentEnabled(intent.isEnabled)

            is MainIntent.Notifications.ToggleEdgeGlow -> dataStoreManager.saveIsAodEdgeGlowEnabled(intent.isEnabled)
            is MainIntent.Notifications.ToggleMediaControls ->
                dataStoreManager.saveIsAodMediaControlsEnabled(intent.isEnabled)
        }
    }

    private fun onInteractionIntent(intent: MainIntent.Interaction) {
        when (intent) {
            is MainIntent.Interaction.ShowGestureActionPicker ->
                updateState { copy(interaction = interaction.copy(editingGesture = intent.gesture)) }

            is MainIntent.Interaction.DismissGestureActionPicker ->
                updateState { copy(interaction = interaction.copy(editingGesture = null)) }

            is MainIntent.Interaction.ChangeGestureAction -> changeGestureAction(intent.gesture, intent.action)
            is MainIntent.Interaction.ToggleAutoDim -> dataStoreManager.saveIsAodAutoDimEnabled(intent.isEnabled)
            is MainIntent.Interaction.ToggleRaiseToWake -> dataStoreManager.saveIsAodRaiseToWakeEnabled(intent.isEnabled)
        }
    }

    private fun onRulesIntent(intent: MainIntent.Rules) {
        when (intent) {
            is MainIntent.Rules.ChangeChargingRule -> dataStoreManager.saveAodChargingRule(intent.rule.code)
            is MainIntent.Rules.ToggleSchedule -> dataStoreManager.saveIsAodScheduleEnabled(intent.isEnabled)
            is MainIntent.Rules.ShowScheduleTimePicker ->
                updateState { copy(rules = rules.copy(editingScheduleTime = intent.time)) }

            is MainIntent.Rules.DismissScheduleTimePicker ->
                updateState { copy(rules = rules.copy(editingScheduleTime = null)) }

            is MainIntent.Rules.ChangeScheduleTime -> changeScheduleTime(intent.time, intent.minuteOfDay)
            is MainIntent.Rules.ChangeMinBattery -> dataStoreManager.saveAodMinBattery(intent.percent)
        }
    }

    private fun onPermissionIntent(intent: MainIntent.Permission) {
        when (intent) {
            is MainIntent.Permission.OpenPermissionSettings -> openPermissionSettings(intent.permission)
            is MainIntent.Permission.RefreshPermissions -> refreshPermissions()
            is MainIntent.Permission.ShowPermissionSheet -> showPermissionSheet()
            is MainIntent.Permission.DismissPermissionSheet -> dismissPermissionSheet()
            is MainIntent.Permission.NotificationPermissionResult -> onNotificationPermissionResult(intent.isGranted)
            is MainIntent.Permission.CalendarPermissionResult -> onCalendarPermissionResult(intent.isGranted)
            is MainIntent.Permission.LocationPermissionResult -> onLocationPermissionResult(intent.isGranted)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                dataStoreManager.observeAodOptions(),
                dataStoreManager.observeAodNotificationOptions(),
                dataStoreManager.observeAodAppearance(),
                dataStoreManager.observeAodInteraction(),
                dataStoreManager.observeAodRules(),
            ) { options, notificationOptions, appearance, interaction, rules ->
                Settings(
                    options = options,
                    notificationOptions = notificationOptions,
                    appearance = appearance,
                    interaction = interaction,
                    rules = rules,
                )
            }.collectCatching(
                block = { settings ->
                    updateState {
                        copy(
                            isLoading = false,
                            options = settings.options,
                            notificationOptions = settings.notificationOptions,
                            appearance = appearance.copy(settings = settings.appearance),
                            interaction = interaction.copy(settings = settings.interaction),
                            rules = rules.copy(settings = settings.rules),
                        )
                    }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeBackground() {
        viewModelScope.launch {
            aodImageManager.hasBackground.filterNotNull().collectCatching(
                block = { hasBackground -> updateState { copy(appearance = appearance.copy(hasBackground = hasBackground)) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeWallpaper() {
        viewModelScope.launch {
            dataStoreManager.aodWallpaper.filterNotNull().collectCatching(
                block = { code ->
                    updateState { copy(appearance = appearance.copy(wallpaper = WallpaperValue.fromCode(code))) }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeExtras() {
        viewModelScope.launch {
            dataStoreManager.observeAodExtras().collectCatching(
                block = { settings -> updateState { copy(extras = extras.copy(settings = settings)) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeDrawing() {
        viewModelScope.launch {
            aodImageManager.hasDrawing.filterNotNull().collectCatching(
                block = { hasDrawing -> updateState { copy(extras = extras.copy(hasDrawing = hasDrawing)) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            dataStoreManager.selectedLangCode.filterNotNull().collectCatching(
                block = { code ->
                    updateState { copy(language = LanguageValue.fromCode(code).toUiModel(languageManager)) }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeLastWake() {
        viewModelScope.launch {
            dataStoreManager.aodLastWake.filterNotNull().collectCatching(
                block = { code ->
                    updateState { copy(lastWakeMessageRes = WakeResultValue.fromCode(code).messageRes) }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Trạng thái quyền đổi ở app Cài đặt chứ không phải ở đây, nên đọc lại mỗi lần màn hình resume.
    private fun refreshPermissions() {
        updateState {
            copy(
                permission = permission.copy(permissions = permissionManager.aodPermissions()),
                extras = extras.copy(
                    hasCalendarPermission = permissionManager.hasCalendarPermission(),
                    hasLocationPermission = permissionManager.hasCoarseLocationPermission(),
                ),
            )
        }
    }

    private fun showPermissionSheet() {
        updateState { copy(permission = permission.copy(isSheetDismissed = false)) }
    }

    private fun dismissPermissionSheet() {
        updateState { copy(permission = permission.copy(isSheetDismissed = true)) }
    }

    private fun toggleAod(isEnabled: Boolean) {
        dataStoreManager.saveIsAodEnabled(isEnabled)
        viewModelScope.launch {
            sendEffect(if (isEnabled) MainEffect.Service.StartAodService else MainEffect.Service.StopAodService)
        }
    }

    // Huỷ chọn ảnh thì Photo Picker trả null: không có gì để lưu.
    private fun onBackgroundPickerResult(uri: String?) {
        if (uri == null) return
        viewModelScope.launch {
            updateState { copy(appearance = appearance.copy(isSavingBackground = true)) }
            val isSaved = aodImageManager.saveBackground(uri)
            updateState { copy(appearance = appearance.copy(isSavingBackground = false)) }
            if (isSaved) {
                dataStoreManager.saveAodWallpaper(WallpaperValue.NONE_CODE)
            } else {
                sendEffect(MainEffect.ShowMessage(R.string.background_save_failed))
            }
        }
    }

    // Mỗi lúc chỉ một ảnh nền: chọn ảnh có sẵn thì bỏ ảnh từ máy (và ngược lại), để AOD không phải chọn giữa hai ảnh.
    private fun selectWallpaper(wallpaper: WallpaperValue) {
        dataStoreManager.saveAodWallpaper(wallpaper.code)
        aodImageManager.removeBackground()
    }

    private fun removeBackground() {
        dataStoreManager.saveAodWallpaper(WallpaperValue.NONE_CODE)
        aodImageManager.removeBackground()
    }

    private fun changeGestureAction(gesture: AodGestureValue, action: AodActionValue) {
        dataStoreManager.saveGestureAction(gesture, action)
        updateState { copy(interaction = interaction.copy(editingGesture = null)) }
    }

    private fun changeMemo(memo: String) {
        dataStoreManager.saveAodMemo(memo.trim().take(AodExtrasUiModel.MEMO_MAX_LENGTH))
        updateState { copy(extras = extras.copy(isMemoEditorVisible = false)) }
    }

    private fun changeDrawing(drawing: Bitmap) {
        updateState { copy(extras = extras.copy(isDrawingPadVisible = false)) }
        viewModelScope.launch {
            if (!aodImageManager.saveDrawing(drawing)) sendEffect(MainEffect.ShowMessage(R.string.drawing_save_failed))
        }
    }

    // Bật lịch hay thời tiết khi chưa có quyền thì hỏi quyền trước; chỉ lưu "bật" khi đã được cấp, để AOD không chờ dữ liệu không bao giờ có.
    private fun toggleCalendar(isEnabled: Boolean) {
        if (isEnabled && !permissionManager.hasCalendarPermission()) {
            permissionRequestedFromList = null
            viewModelScope.launch { sendEffect(MainEffect.Permission.RequestCalendarPermission) }
        } else {
            dataStoreManager.saveIsAodCalendarEnabled(isEnabled)
        }
    }

    // Hỏi từ danh sách quyền: chỉ cấp, không bật "Sự kiện hôm nay"; bị từ chối thì mở Thông tin ứng dụng (từ chối hai lần thì hệ thống không hỏi nữa).
    private fun onCalendarPermissionResult(isGranted: Boolean) {
        val isFromList = permissionRequestedFromList == PermissionValue.CALENDAR
        permissionRequestedFromList = null
        refreshPermissions()
        viewModelScope.launch {
            when {
                isFromList -> if (!isGranted) sendEffect(MainEffect.Permission.OpenAppSettings)
                isGranted -> dataStoreManager.saveIsAodCalendarEnabled(true)
                else -> sendEffect(MainEffect.ShowMessage(R.string.calendar_permission_denied))
            }
        }
    }

    private fun toggleWeather(isEnabled: Boolean) {
        when {
            !isEnabled -> dataStoreManager.saveIsAodWeatherEnabled(false)
            !permissionManager.hasCoarseLocationPermission() -> {
                permissionRequestedFromList = null
                viewModelScope.launch { sendEffect(MainEffect.Permission.RequestLocationPermission) }
            }

            else -> enableWeather()
        }
    }

    private fun onLocationPermissionResult(isGranted: Boolean) {
        val isFromList = permissionRequestedFromList == PermissionValue.LOCATION
        permissionRequestedFromList = null
        refreshPermissions()
        when {
            isFromList -> if (!isGranted) viewModelScope.launch { sendEffect(MainEffect.Permission.OpenAppSettings) }
            isGranted -> enableWeather()
            else -> viewModelScope.launch { sendEffect(MainEffect.ShowMessage(R.string.location_permission_denied)) }
        }
    }

    // Tải ngay lúc bật (màn cài đặt đang mở nên được dùng vị trí): lần AOD đầu tiên đã có thời tiết.
    private fun enableWeather() {
        dataStoreManager.saveIsAodWeatherEnabled(true)
        viewModelScope.launch {
            refreshWeatherUseCase(nowMillis = System.currentTimeMillis(), isForced = true)
                .onFailure { e -> Timber.tag(tag).w("Could not refresh the weather: ${e.message}") }
        }
    }

    private fun changeScheduleTime(time: ScheduleTimeValue, minuteOfDay: Int) {
        when (time) {
            ScheduleTimeValue.START -> dataStoreManager.saveAodScheduleStartMinute(minuteOfDay)
            ScheduleTimeValue.END -> dataStoreManager.saveAodScheduleEndMinute(minuteOfDay)
        }
        updateState { copy(rules = rules.copy(editingScheduleTime = null)) }
    }

    private fun openPermissionSettings(permission: PermissionValue) {
        val effect = when (permission) {
            PermissionValue.OVERLAY -> MainEffect.Permission.OpenOverlaySettings
            PermissionValue.MIUI_LOCK_SCREEN,
            PermissionValue.MIUI_BACKGROUND_POPUP -> MainEffect.Permission.OpenMiuiPermissionSettings

            PermissionValue.NOTIFICATIONS ->
                if (permissionManager.needsNotificationPermission()) {
                    MainEffect.Permission.RequestNotificationPermission
                } else {
                    MainEffect.Permission.OpenNotificationSettings
                }

            PermissionValue.NOTIFICATION_ACCESS -> MainEffect.Permission.OpenNotificationAccessSettings
            PermissionValue.CALENDAR ->
                if (permissionManager.hasCalendarPermission()) {
                    MainEffect.Permission.OpenAppSettings
                } else {
                    permissionRequestedFromList = PermissionValue.CALENDAR
                    MainEffect.Permission.RequestCalendarPermission
                }

            PermissionValue.LOCATION ->
                if (permissionManager.hasCoarseLocationPermission()) {
                    MainEffect.Permission.OpenAppSettings
                } else {
                    permissionRequestedFromList = PermissionValue.LOCATION
                    MainEffect.Permission.RequestLocationPermission
                }
        }
        viewModelScope.launch { sendEffect(effect) }
    }

    // Đã cho phép thì start lại service để nó đăng lại thông báo; bị từ chối thì mở trang cài đặt thông báo (sau 2 lần từ chối hệ thống không hiện hộp thoại nữa).
    private fun onNotificationPermissionResult(isGranted: Boolean) {
        refreshPermissions()
        viewModelScope.launch {
            when {
                isGranted -> if (dataStoreManager.isAodEnabled.value == true) sendEffect(MainEffect.Service.StartAodService)
                else -> sendEffect(MainEffect.Permission.OpenNotificationSettings)
            }
        }
    }

    // Bật AOD hay áp giao diện cho đồng hồ (mặt, font, màu, cỡ, xoay ngang, ảnh nền) là lúc cần quyền: còn thiếu quyền bắt buộc
    // thì sheet nhắc quyền hiện lại dù trước đó đã bị đóng. Theme / style mới cho AOD thì thêm Intent của nó vào đây.
    private val MainIntent.isAodRelated: Boolean
        get() = when (this) {
            is MainIntent.ToggleAod -> isEnabled
            is MainIntent.Appearance.BackgroundPickerResult -> uri != null
            is MainIntent.Appearance.ChangeClockFace,
            is MainIntent.Appearance.ChangeClockFont,
            is MainIntent.Appearance.ChangeClockColor,
            is MainIntent.Appearance.ChangeClockSize,
            is MainIntent.Appearance.ToggleLandscape,
            is MainIntent.Appearance.SelectWallpaper,
            is MainIntent.Extras.ToggleDate,
            is MainIntent.Extras.ToggleBattery -> true

            else -> false
        }

    // Gom mọi nhóm cài đặt vào một lần cập nhật: màn hình chỉ hết loading khi tất cả đã đọc xong.
    private data class Settings(
        val options: AodOptionsUiModel,
        val notificationOptions: AodNotificationOptionsUiModel,
        val appearance: AodAppearanceUiModel,
        val interaction: AodInteractionUiModel,
        val rules: AodRulesUiModel,
    )
}
