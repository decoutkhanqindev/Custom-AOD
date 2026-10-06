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
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainState
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
        isFlashlightAvailable = flashlightManager.isAvailable,
        isLightSensorAvailable = ambientLightManager.isAvailable,
        isPickupSensorAvailable = pickupGestureManager.isSupported,
    ),
), Tag {

    // Hộp thoại quyền thông báo đang mở là lần hỏi tự động lúc mở app lần đầu (bị từ chối thì để yên) hay do user bấm dòng "Thông báo".
    private var isFirstLaunchNotificationRequest = false

    init {
        observeSettings()
        observeBackground()
        observeWallpaper()
        observeExtras()
        observeDrawing()
        observeLanguage()
        observeLastWake()
        checkFirstLaunchNotificationPermission()
    }

    override fun onIntent(intent: MainIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        when (intent) {
            is MainIntent.ToggleAod -> toggleAod(intent.isEnabled)
            is MainIntent.ToggleCustomBrightness -> dataStoreManager.saveIsAodCustomBrightness(intent.isEnabled)
            is MainIntent.ChangeBrightness -> dataStoreManager.saveAodBrightnessPercent(intent.percent)
            is MainIntent.ToggleProximity -> dataStoreManager.saveIsAodProximityEnabled(intent.isEnabled)
            is MainIntent.ChangeTimeout -> dataStoreManager.saveAodTimeoutMinutes(intent.minutes)
            is MainIntent.ChangeClockFace -> dataStoreManager.saveAodClockFace(intent.face.code)
            is MainIntent.ChangeClockFont -> dataStoreManager.saveAodClockFont(intent.font.code)
            is MainIntent.ChangeClockColor -> dataStoreManager.saveAodClockColor(intent.color.code)
            is MainIntent.ChangeClockSize -> dataStoreManager.saveAodClockSizePercent(intent.percent)
            is MainIntent.ToggleLandscape -> dataStoreManager.saveIsAodLandscape(intent.isEnabled)
            is MainIntent.SelectWallpaper -> selectWallpaper(intent.wallpaper)
            is MainIntent.OpenBackgroundPicker -> viewModelScope.launch { sendEffect(MainEffect.OpenBackgroundPicker) }
            is MainIntent.BackgroundPickerResult -> onBackgroundPickerResult(intent.uri)
            is MainIntent.RemoveBackground -> removeBackground()
            is MainIntent.ShowMemoEditor -> updateState { copy(isEditingMemo = true) }
            is MainIntent.DismissMemoEditor -> updateState { copy(isEditingMemo = false) }
            is MainIntent.ChangeMemo -> changeMemo(intent.memo)
            is MainIntent.ShowDrawingPad -> updateState { copy(isDrawingPadVisible = true) }
            is MainIntent.DismissDrawingPad -> updateState { copy(isDrawingPadVisible = false) }
            is MainIntent.ChangeDrawing -> changeDrawing(intent.drawing)
            is MainIntent.RemoveDrawing -> aodImageManager.removeDrawing()
            is MainIntent.ToggleCalendar -> toggleCalendar(intent.isEnabled)
            is MainIntent.CalendarPermissionResult -> onCalendarPermissionResult(intent.isGranted)
            is MainIntent.ToggleWeather -> toggleWeather(intent.isEnabled)
            is MainIntent.LocationPermissionResult -> onLocationPermissionResult(intent.isGranted)
            is MainIntent.ToggleWeatherFahrenheit -> dataStoreManager.saveIsAodWeatherFahrenheit(intent.isEnabled)
            is MainIntent.ShowGestureActionPicker -> updateState { copy(editingGesture = intent.gesture) }
            is MainIntent.DismissGestureActionPicker -> updateState { copy(editingGesture = null) }
            is MainIntent.ChangeGestureAction -> changeGestureAction(intent.gesture, intent.action)
            is MainIntent.ToggleAutoDim -> dataStoreManager.saveIsAodAutoDimEnabled(intent.isEnabled)
            is MainIntent.ToggleRaiseToWake -> dataStoreManager.saveIsAodRaiseToWakeEnabled(intent.isEnabled)
            is MainIntent.ToggleNotificationIcons -> dataStoreManager.saveIsAodNotificationIconsEnabled(intent.isEnabled)
            is MainIntent.ToggleNotificationContent -> dataStoreManager.saveIsAodNotificationContentEnabled(intent.isEnabled)
            is MainIntent.ToggleEdgeGlow -> dataStoreManager.saveIsAodEdgeGlowEnabled(intent.isEnabled)
            is MainIntent.ToggleMediaControls -> dataStoreManager.saveIsAodMediaControlsEnabled(intent.isEnabled)
            is MainIntent.ChangeChargingRule -> dataStoreManager.saveAodChargingRule(intent.rule.code)
            is MainIntent.ToggleSchedule -> dataStoreManager.saveIsAodScheduleEnabled(intent.isEnabled)
            is MainIntent.ShowScheduleTimePicker -> updateState { copy(editingScheduleTime = intent.time) }
            is MainIntent.DismissScheduleTimePicker -> updateState { copy(editingScheduleTime = null) }
            is MainIntent.ChangeScheduleTime -> changeScheduleTime(intent.time, intent.minuteOfDay)
            is MainIntent.ChangeMinBattery -> dataStoreManager.saveAodMinBattery(intent.percent)
            is MainIntent.OpenPermissionSettings -> openPermissionSettings(intent.permission)
            is MainIntent.RefreshPermissions -> refreshPermissions()
            is MainIntent.ShowPermissionSheet -> showPermissionSheet()
            is MainIntent.DismissPermissionSheet -> dismissPermissionSheet()
            is MainIntent.NotificationPermissionDialogShown -> onNotificationPermissionDialogShown()
            is MainIntent.NotificationPermissionResult -> onNotificationPermissionResult(intent.isGranted)
            is MainIntent.NavigateToLanguage -> viewModelScope.launch { sendEffect(MainEffect.NavigateToLanguage) }
            is MainIntent.OpenPreview -> viewModelScope.launch { sendEffect(MainEffect.OpenPreview) }
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
                action = { settings ->
                    updateState {
                        copy(
                            isLoading = false,
                            options = settings.options,
                            notificationOptions = settings.notificationOptions,
                            appearance = settings.appearance,
                            interaction = settings.interaction,
                            rules = settings.rules,
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
                action = { hasBackground -> updateState { copy(hasBackground = hasBackground) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeWallpaper() {
        viewModelScope.launch {
            dataStoreManager.aodWallpaper.filterNotNull().collectCatching(
                action = { code -> updateState { copy(wallpaper = WallpaperValue.fromCode(code)) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeExtras() {
        viewModelScope.launch {
            dataStoreManager.observeAodExtras().collectCatching(
                action = { extras -> updateState { copy(extras = extras) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeDrawing() {
        viewModelScope.launch {
            aodImageManager.hasDrawing.filterNotNull().collectCatching(
                action = { hasDrawing -> updateState { copy(hasDrawing = hasDrawing) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            dataStoreManager.selectedLangCode.filterNotNull().collectCatching(
                action = { code ->
                    updateState { copy(language = LanguageValue.fromCode(code).toUiModel(languageManager)) }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeLastWake() {
        viewModelScope.launch {
            dataStoreManager.aodLastWake.filterNotNull().collectCatching(
                action = { code ->
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
                permissions = permissionManager.aodPermissions(),
                hasCalendarPermission = permissionManager.hasCalendarPermission(),
                hasLocationPermission = permissionManager.hasCoarseLocationPermission(),
            )
        }
    }

    private fun showPermissionSheet() {
        updateState { copy(isPermissionSheetDismissed = false) }
    }

    private fun dismissPermissionSheet() {
        updateState { copy(isPermissionSheetDismissed = true) }
    }

    // Chỉ hỏi một lần ở lần mở app đầu tiên, để thông báo của foreground service hiện ngay từ đầu (Google Play muốn service đó dễ nhận biết).
    private fun checkFirstLaunchNotificationPermission() {
        viewModelScope.launch {
            val isAsked = dataStoreManager.isNotificationsAsked.filterNotNull().first()
            if (isAsked || !permissionManager.needsNotificationPermission()) return@launch
            dataStoreManager.saveIsNotificationsAsked(true)
            updateState { copy(isNotificationPermissionPending = true) }
        }
    }

    // Bật lại AOD là lúc cần quyền: còn thiếu quyền bắt buộc thì sheet nhắc quyền hiện lại dù trước đó đã bị đóng.
    private fun toggleAod(isEnabled: Boolean) {
        dataStoreManager.saveIsAodEnabled(isEnabled)
        if (isEnabled) showPermissionSheet()
        viewModelScope.launch {
            sendEffect(if (isEnabled) MainEffect.StartAodService else MainEffect.StopAodService)
        }
    }

    // Huỷ chọn ảnh thì Photo Picker trả null: không có gì để lưu.
    private fun onBackgroundPickerResult(uri: String?) {
        if (uri == null) return
        viewModelScope.launch {
            updateState { copy(isSavingBackground = true) }
            val isSaved = aodImageManager.saveBackground(uri)
            updateState { copy(isSavingBackground = false) }
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
        updateState { copy(editingGesture = null) }
    }

    private fun changeMemo(memo: String) {
        dataStoreManager.saveAodMemo(memo.trim().take(AodExtrasUiModel.MEMO_MAX_LENGTH))
        updateState { copy(isEditingMemo = false) }
    }

    private fun changeDrawing(drawing: Bitmap) {
        updateState { copy(isDrawingPadVisible = false) }
        viewModelScope.launch {
            if (!aodImageManager.saveDrawing(drawing)) sendEffect(MainEffect.ShowMessage(R.string.drawing_save_failed))
        }
    }

    // Bật lịch hay thời tiết khi chưa có quyền thì hỏi quyền trước; chỉ lưu "bật" khi đã được cấp, để AOD không chờ dữ liệu không bao giờ có.
    private fun toggleCalendar(isEnabled: Boolean) {
        if (isEnabled && !permissionManager.hasCalendarPermission()) {
            viewModelScope.launch { sendEffect(MainEffect.RequestCalendarPermission) }
        } else {
            dataStoreManager.saveIsAodCalendarEnabled(isEnabled)
        }
    }

    private fun onCalendarPermissionResult(isGranted: Boolean) {
        updateState { copy(hasCalendarPermission = isGranted) }
        if (isGranted) {
            dataStoreManager.saveIsAodCalendarEnabled(true)
        } else {
            viewModelScope.launch { sendEffect(MainEffect.ShowMessage(R.string.calendar_permission_denied)) }
        }
    }

    private fun toggleWeather(isEnabled: Boolean) {
        when {
            !isEnabled -> dataStoreManager.saveIsAodWeatherEnabled(false)
            !permissionManager.hasCoarseLocationPermission() ->
                viewModelScope.launch { sendEffect(MainEffect.RequestLocationPermission) }

            else -> enableWeather()
        }
    }

    private fun onLocationPermissionResult(isGranted: Boolean) {
        updateState { copy(hasLocationPermission = isGranted) }
        if (isGranted) {
            enableWeather()
        } else {
            viewModelScope.launch { sendEffect(MainEffect.ShowMessage(R.string.location_permission_denied)) }
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
        updateState { copy(editingScheduleTime = null) }
    }

    private fun openPermissionSettings(permission: PermissionValue) {
        val effect = when (permission) {
            PermissionValue.OVERLAY -> MainEffect.OpenOverlaySettings
            PermissionValue.MIUI_LOCK_SCREEN,
            PermissionValue.MIUI_BACKGROUND_POPUP -> MainEffect.OpenMiuiPermissionSettings

            PermissionValue.NOTIFICATIONS ->
                if (permissionManager.needsNotificationPermission()) {
                    isFirstLaunchNotificationRequest = false
                    MainEffect.RequestNotificationPermission
                } else {
                    MainEffect.OpenNotificationSettings
                }

            PermissionValue.NOTIFICATION_ACCESS -> MainEffect.OpenNotificationAccessSettings
        }
        viewModelScope.launch { sendEffect(effect) }
    }

    private fun onNotificationPermissionDialogShown() {
        isFirstLaunchNotificationRequest = true
        updateState { copy(isNotificationPermissionPending = false) }
    }

    // Đã cho phép thì start lại service để nó đăng lại thông báo; bị từ chối từ dòng "Thông báo" thì mở trang cài đặt (sau 2 lần từ chối hệ thống không hiện hộp thoại nữa).
    private fun onNotificationPermissionResult(isGranted: Boolean) {
        val isFirstLaunch = isFirstLaunchNotificationRequest
        isFirstLaunchNotificationRequest = false
        viewModelScope.launch {
            when {
                isGranted -> if (dataStoreManager.isAodEnabled.value == true) sendEffect(MainEffect.StartAodService)
                !isFirstLaunch -> sendEffect(MainEffect.OpenNotificationSettings)
            }
        }
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
