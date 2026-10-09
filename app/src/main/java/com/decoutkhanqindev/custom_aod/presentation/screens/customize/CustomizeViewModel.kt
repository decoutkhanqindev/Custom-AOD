package com.decoutkhanqindev.custom_aod.presentation.screens.customize

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.image.AodImageManager
import com.decoutkhanqindev.custom_aod.domain.usecase.RefreshWeatherUseCase
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodExtrasUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeDecorUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeDraftUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeEffectsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeGuideStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeInfoUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.model.hasRequiredPermissions
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodAppearance
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodExtras
import com.decoutkhanqindev.custom_aod.presentation.model.observeAodNotificationOptions
import com.decoutkhanqindev.custom_aod.presentation.model.requiredAodPermissions
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeState
import com.decoutkhanqindev.custom_aod.utils.Tag
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class CustomizeViewModel(
    private val dataStoreManager: DataStoreManager,
    private val permissionManager: PermissionManager,
    private val aodImageManager: AodImageManager,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
) : BaseViewModel<CustomizeState, CustomizeIntent, CustomizeEffect>(
    initialState = CustomizeState(),
), Tag {

    private var initialDraft = CustomizeDraftUiModel()
    private val undoStack = ArrayDeque<CustomizeDraftUiModel>()
    private val redoStack = ArrayDeque<CustomizeDraftUiModel>()

    // Mục cần quyền mà user vừa bật: chỉ ghi vào bản nháp khi quyền đã được cấp.
    private var pendingChange: (CustomizeDraftUiModel.() -> CustomizeDraftUiModel)? = null
    private var glowPreviewJob: Job? = null

    init {
        loadDraft()
        tickClock()
        showGuideOnce()
    }

    override fun onIntent(intent: CustomizeIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        when (intent) {
            is CustomizeIntent.Panel -> onPanelIntent(intent)
            is CustomizeIntent.Appearance -> onAppearanceIntent(intent)
            is CustomizeIntent.Decor -> onDecorIntent(intent)
            is CustomizeIntent.Info -> onInfoIntent(intent)
            is CustomizeIntent.Effects -> onEffectsIntent(intent)
            is CustomizeIntent.History -> onHistoryIntent(intent)
            is CustomizeIntent.Permission -> onPermissionIntent(intent)
            is CustomizeIntent.Guide -> onGuideIntent(intent)
            is CustomizeIntent.ConfirmCustomization -> confirmCustomization()
            is CustomizeIntent.SkipCustomization -> viewModelScope.launch { navigateNext() }
            is CustomizeIntent.NavigateBack ->
                viewModelScope.launch { sendEffect(CustomizeEffect.Navigation.NavigateBack) }
        }
    }

    // Bấm lại tab đang mở thì thu panel lại để nhìn trọn AOD.
    private fun onPanelIntent(intent: CustomizeIntent.Panel) {
        when (intent) {
            is CustomizeIntent.Panel.SelectTab ->
                updateState { copy(selectedTab = if (selectedTab == intent.tab) null else intent.tab) }

            is CustomizeIntent.Panel.ClosePanel -> updateState { copy(selectedTab = null) }
        }
    }

    private fun onAppearanceIntent(intent: CustomizeIntent.Appearance) {
        when (intent) {
            is CustomizeIntent.Appearance.ChangeClockFace -> editAppearance { copy(clock = clock.copy(face = intent.face)) }
            is CustomizeIntent.Appearance.ChangeClockFont -> editAppearance { copy(clock = clock.copy(font = intent.font)) }
            is CustomizeIntent.Appearance.ChangeClockColor ->
                editAppearance { copy(clock = clock.copy(color = intent.color)) }

            is CustomizeIntent.Appearance.ChangeClockSize ->
                editAppearance { copy(clock = clock.copy(sizePercent = intent.percent)) }

            is CustomizeIntent.Appearance.ToggleDate -> editAppearance { copy(isDateEnabled = intent.isEnabled) }
            is CustomizeIntent.Appearance.ToggleBattery -> editAppearance { copy(isBatteryEnabled = intent.isEnabled) }
        }
    }

    private fun onDecorIntent(intent: CustomizeIntent.Decor) {
        when (intent) {
            is CustomizeIntent.Decor.SelectWallpaper -> editDecor { copy(wallpaper = intent.wallpaper, background = null) }
            is CustomizeIntent.Decor.OpenBackgroundPicker ->
                viewModelScope.launch { sendEffect(CustomizeEffect.Decor.OpenBackgroundPicker) }

            is CustomizeIntent.Decor.BackgroundPickerResult -> onBackgroundPickerResult(intent.uri)
            is CustomizeIntent.Decor.RemoveBackground -> editDecor { copy(wallpaper = null, background = null) }
            is CustomizeIntent.Decor.ShowDrawingPad -> updateState { copy(decor = decor.copy(isDrawingPadVisible = true)) }
            is CustomizeIntent.Decor.DismissDrawingPad ->
                updateState { copy(decor = decor.copy(isDrawingPadVisible = false)) }

            is CustomizeIntent.Decor.ChangeDrawing -> {
                updateState { copy(decor = decor.copy(isDrawingPadVisible = false)) }
                editDecor { copy(drawing = intent.drawing) }
            }

            is CustomizeIntent.Decor.RemoveDrawing -> editDecor { copy(drawing = null) }
            is CustomizeIntent.Decor.ShowMemoEditor -> updateState { copy(decor = decor.copy(isMemoEditorVisible = true)) }
            is CustomizeIntent.Decor.DismissMemoEditor ->
                updateState { copy(decor = decor.copy(isMemoEditorVisible = false)) }

            is CustomizeIntent.Decor.ChangeMemo -> {
                updateState { copy(decor = decor.copy(isMemoEditorVisible = false)) }
                editDecor { copy(memo = intent.memo.trim().take(AodExtrasUiModel.MEMO_MAX_LENGTH)) }
            }
        }
    }

    private fun onInfoIntent(intent: CustomizeIntent.Info) {
        when (intent) {
            is CustomizeIntent.Info.ToggleNotificationIcons ->
                toggleInfo(PermissionValue.NOTIFICATION_ACCESS, intent.isEnabled) { copy(isNotificationIconsEnabled = it) }

            is CustomizeIntent.Info.ToggleNotificationContent ->
                toggleInfo(PermissionValue.NOTIFICATION_ACCESS, intent.isEnabled) {
                    copy(isNotificationContentEnabled = it)
                }

            is CustomizeIntent.Info.ToggleCalendar ->
                toggleInfo(PermissionValue.CALENDAR, intent.isEnabled) { copy(isCalendarEnabled = it) }

            is CustomizeIntent.Info.ToggleWeather ->
                toggleInfo(PermissionValue.LOCATION, intent.isEnabled) { copy(isWeatherEnabled = it) }

            is CustomizeIntent.Info.ToggleWeatherFahrenheit ->
                edit { copy(info = info.copy(isWeatherFahrenheit = intent.isEnabled)) }

            is CustomizeIntent.Info.ToggleMediaControls ->
                toggleInfo(PermissionValue.NOTIFICATION_ACCESS, intent.isEnabled) { copy(isMediaControlsEnabled = it) }
        }
    }

    private fun onEffectsIntent(intent: CustomizeIntent.Effects) {
        when (intent) {
            is CustomizeIntent.Effects.ToggleEdgeGlow ->
                toggleWithPermission(PermissionValue.NOTIFICATION_ACCESS, intent.isEnabled) { isEnabled ->
                    copy(effects = effects.copy(isEdgeGlowEnabled = isEnabled))
                }

            is CustomizeIntent.Effects.PreviewEdgeGlow -> previewEdgeGlow()
        }
    }

    private fun onHistoryIntent(intent: CustomizeIntent.History) {
        when (intent) {
            is CustomizeIntent.History.UndoChange -> undo()
            is CustomizeIntent.History.RedoChange -> redo()
            is CustomizeIntent.History.ShowResetConfirm ->
                updateState { copy(history = history.copy(isResetConfirmVisible = true)) }

            is CustomizeIntent.History.DismissResetConfirm ->
                updateState { copy(history = history.copy(isResetConfirmVisible = false)) }

            is CustomizeIntent.History.ResetCustomization -> {
                updateState { copy(history = history.copy(isResetConfirmVisible = false)) }
                edit { CustomizeDraftUiModel() }
            }
        }
    }

    private fun onPermissionIntent(intent: CustomizeIntent.Permission) {
        when (intent) {
            is CustomizeIntent.Permission.OpenPermissionSettings -> openPermissionSettings(intent.permission)
            is CustomizeIntent.Permission.DismissPermissionSheet -> dismissPermissionSheet()
            is CustomizeIntent.Permission.CalendarPermissionResult ->
                onPermissionResult(intent.isGranted, R.string.calendar_permission_denied)

            is CustomizeIntent.Permission.LocationPermissionResult ->
                onPermissionResult(intent.isGranted, R.string.location_permission_denied)

            is CustomizeIntent.Permission.RefreshPermissions -> refreshPermissions()
        }
    }

    private fun onGuideIntent(intent: CustomizeIntent.Guide) {
        when (intent) {
            is CustomizeIntent.Guide.ShowGuide ->
                updateState { copy(guideStep = CustomizeGuideStepValue.entries.first(), selectedTab = null) }

            is CustomizeIntent.Guide.ShowNextGuideStep -> {
                val next = state.value.guideStep?.next
                if (next == null) dismissGuide() else updateState { copy(guideStep = next) }
            }

            is CustomizeIntent.Guide.DismissGuide -> dismissGuide()
        }
    }

    // Bản nháp bắt đầu từ cài đặt đang có (lần đầu mở app là mặc định); mục cần quyền chỉ tính là bật khi đã có quyền.
    private fun loadDraft() {
        viewModelScope.launch {
            val clock = dataStoreManager.observeAodAppearance().first()
            val extras = dataStoreManager.observeAodExtras().first()
            val notificationOptions = dataStoreManager.observeAodNotificationOptions().first()
            val wallpaperCode = dataStoreManager.aodWallpaper.filterNotNull().first()
            val hasNotificationAccess = isGranted(PermissionValue.NOTIFICATION_ACCESS)
            initialDraft = CustomizeDraftUiModel(
                appearance = CustomizeAppearanceUiModel(
                    clock = clock,
                    isDateEnabled = extras.isDateEnabled,
                    isBatteryEnabled = extras.isBatteryEnabled,
                ),
                decor = CustomizeDecorUiModel(
                    wallpaper = WallpaperValue.fromCode(wallpaperCode),
                    background = aodImageManager.loadBackground(),
                    drawing = aodImageManager.loadDrawing(),
                    memo = extras.memo,
                ),
                info = CustomizeInfoUiModel(
                    isNotificationIconsEnabled = notificationOptions.isIconsEnabled && hasNotificationAccess,
                    isNotificationContentEnabled = notificationOptions.isContentEnabled && hasNotificationAccess,
                    isCalendarEnabled = extras.isCalendarEnabled && isGranted(PermissionValue.CALENDAR),
                    isWeatherEnabled = extras.isWeatherEnabled && isGranted(PermissionValue.LOCATION),
                    isWeatherFahrenheit = extras.isWeatherFahrenheit,
                    isMediaControlsEnabled = notificationOptions.isMediaControlsEnabled && hasNotificationAccess,
                ),
                effects = CustomizeEffectsUiModel(
                    isEdgeGlowEnabled = notificationOptions.isEdgeGlowEnabled && hasNotificationAccess,
                ),
            )
            undoStack.clear()
            redoStack.clear()
            publish(initialDraft)
            updateState { copy(isLoading = false) }
        }
    }

    // Đồng hồ xem trước nhảy đúng đầu mỗi phút như AOD thật.
    private fun tickClock() {
        viewModelScope.launch {
            while (isActive) {
                val nowMillis = System.currentTimeMillis()
                updateState { copy(nowMillis = nowMillis) }
                delay(MINUTE_MILLIS - nowMillis % MINUTE_MILLIS)
            }
        }
    }

    private fun showGuideOnce() {
        viewModelScope.launch {
            if (dataStoreManager.isCustomizeGuideShown.filterNotNull().first()) return@launch
            updateState { copy(guideStep = CustomizeGuideStepValue.entries.first()) }
        }
    }

    private fun editAppearance(transform: CustomizeAppearanceUiModel.() -> CustomizeAppearanceUiModel) =
        edit { copy(appearance = appearance.transform()) }

    private fun editDecor(transform: CustomizeDecorUiModel.() -> CustomizeDecorUiModel) =
        edit { copy(decor = decor.transform()) }

    // Mỗi thay đổi đẩy bản trước vào lịch sử hoàn tác; thay đổi mới thì bỏ lịch sử làm lại.
    private fun edit(transform: CustomizeDraftUiModel.() -> CustomizeDraftUiModel) {
        val current = state.value.draft
        val next = current.transform()
        if (next == current) return
        undoStack.addLast(current)
        if (undoStack.size > MAX_HISTORY) undoStack.removeFirst()
        redoStack.clear()
        publish(next)
    }

    private fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(state.value.draft)
        publish(previous)
    }

    private fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(state.value.draft)
        publish(next)
    }

    private fun publish(draft: CustomizeDraftUiModel) {
        updateState {
            copy(
                draft = draft,
                history = history.copy(
                    hasChanges = draft != initialDraft,
                    isDefault = draft == CustomizeDraftUiModel(),
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty(),
                ),
            )
        }
    }

    // Ảnh giữ trong bản nháp (chưa ghi file); giải mã ở IO vì ảnh gốc có thể rất lớn.
    private fun onBackgroundPickerResult(uri: String?) {
        if (uri == null) return
        viewModelScope.launch {
            updateState { copy(decor = decor.copy(isLoadingBackground = true)) }
            val background = aodImageManager.decodeBackground(uri)
            updateState { copy(decor = decor.copy(isLoadingBackground = false)) }
            if (background == null) {
                sendEffect(CustomizeEffect.ShowMessage(R.string.background_save_failed))
            } else {
                editDecor { copy(background = background, wallpaper = null) }
            }
        }
    }

    private fun toggleInfo(
        permission: PermissionValue,
        isEnabled: Boolean,
        change: CustomizeInfoUiModel.(Boolean) -> CustomizeInfoUiModel,
    ) = toggleWithPermission(permission, isEnabled) { enabled -> copy(info = info.change(enabled)) }

    // Tắt thì ghi ngay; bật khi chưa có quyền thì hiện sheet quyền, cấp xong mới bật.
    private fun toggleWithPermission(
        permission: PermissionValue,
        isEnabled: Boolean,
        change: CustomizeDraftUiModel.(Boolean) -> CustomizeDraftUiModel,
    ) {
        if (!isEnabled || isGranted(permission)) {
            edit { change(isEnabled) }
            return
        }
        pendingChange = { change(true) }
        updateState { copy(permissionRequest = permission) }
    }

    private fun openPermissionSettings(permission: PermissionValue) {
        val effect = when (permission) {
            PermissionValue.CALENDAR -> CustomizeEffect.Permission.RequestCalendarPermission
            PermissionValue.LOCATION -> CustomizeEffect.Permission.RequestLocationPermission
            PermissionValue.NOTIFICATION_ACCESS -> CustomizeEffect.Permission.OpenNotificationAccessSettings
            else -> return
        }
        viewModelScope.launch { sendEffect(effect) }
    }

    private fun onPermissionResult(isGranted: Boolean, @StringRes deniedMessageRes: Int) {
        if (isGranted) {
            applyPendingChange()
        } else {
            viewModelScope.launch { sendEffect(CustomizeEffect.ShowMessage(deniedMessageRes)) }
        }
    }

    // Truy cập thông báo cấp ở app Cài đặt: quay lại màn này thì kiểm lại.
    private fun refreshPermissions() {
        val permission = state.value.permissionRequest ?: return
        if (isGranted(permission)) applyPendingChange()
    }

    private fun applyPendingChange() {
        pendingChange?.let { change -> edit(change) }
        dismissPermissionSheet()
    }

    private fun dismissPermissionSheet() {
        pendingChange = null
        updateState { copy(permissionRequest = null) }
    }

    private fun isGranted(permission: PermissionValue): Boolean = when (permission) {
        PermissionValue.NOTIFICATION_ACCESS -> permissionManager.isNotificationListenerEnabled()
        PermissionValue.CALENDAR -> permissionManager.hasCalendarPermission()
        PermissionValue.LOCATION -> permissionManager.hasCoarseLocationPermission()
        else -> true
    }

    private fun previewEdgeGlow() {
        glowPreviewJob?.cancel()
        glowPreviewJob = viewModelScope.launch {
            updateState { copy(effects = effects.copy(isGlowPreviewing = true)) }
            delay(GLOW_PREVIEW_MILLIS)
            updateState { copy(effects = effects.copy(isGlowPreviewing = false)) }
        }
    }

    private fun dismissGuide() {
        dataStoreManager.saveIsCustomizeGuideShown(true)
        updateState { copy(guideStep = null) }
    }

    // Chỉ bấm được khi bản nháp đã khác lúc vào màn; ghi phần đã đổi rồi sang bước sau.
    private fun confirmCustomization() {
        if (state.value.isApplying || !state.value.history.hasChanges) return
        viewModelScope.launch {
            updateState { copy(isApplying = true, selectedTab = null) }
            val draft = state.value.draft
            save(draft, initialDraft)
            updateState { copy(isApplying = false) }
            navigateNext()
        }
    }

    // Đủ quyền bắt buộc thì vào thẳng Main, không thì sang màn quyền; Bỏ qua thì không ghi gì, giữ nguyên cài đặt.
    private suspend fun navigateNext() {
        val isReady = permissionManager.requiredAodPermissions().hasRequiredPermissions
        sendEffect(if (isReady) CustomizeEffect.Navigation.NavigateToMain else CustomizeEffect.Navigation.NavigateToPermission)
    }

    // Chỉ ghi phần đã đổi: mục cần quyền user không đụng tới giữ cờ đã lưu (vd icon thông báo mặc định bật nhưng chưa có quyền).
    private suspend fun save(draft: CustomizeDraftUiModel, initial: CustomizeDraftUiModel) {
        saveAppearance(draft.appearance, initial.appearance)
        saveInfo(draft.info, initial.info)
        if (draft.effects.isEdgeGlowEnabled != initial.effects.isEdgeGlowEnabled) {
            dataStoreManager.saveIsAodEdgeGlowEnabled(draft.effects.isEdgeGlowEnabled)
        }
        if (draft.decor.memo != initial.decor.memo) dataStoreManager.saveAodMemo(draft.decor.memo)
        saveBackground(draft.decor, initial.decor)
        saveDrawing(draft.decor.drawing, initial.decor.drawing)
    }

    private fun saveAppearance(draft: CustomizeAppearanceUiModel, initial: CustomizeAppearanceUiModel) {
        with(dataStoreManager) {
            if (draft.clock.face != initial.clock.face) saveAodClockFace(draft.clock.face.code)
            if (draft.clock.font != initial.clock.font) saveAodClockFont(draft.clock.font.code)
            if (draft.clock.color != initial.clock.color) saveAodClockColor(draft.clock.color.code)
            if (draft.clock.sizePercent != initial.clock.sizePercent) saveAodClockSizePercent(draft.clock.sizePercent)
            if (draft.isDateEnabled != initial.isDateEnabled) saveIsAodDateEnabled(draft.isDateEnabled)
            if (draft.isBatteryEnabled != initial.isBatteryEnabled) saveIsAodBatteryEnabled(draft.isBatteryEnabled)
        }
    }

    private suspend fun saveInfo(draft: CustomizeInfoUiModel, initial: CustomizeInfoUiModel) {
        with(dataStoreManager) {
            if (draft.isNotificationIconsEnabled != initial.isNotificationIconsEnabled) {
                saveIsAodNotificationIconsEnabled(draft.isNotificationIconsEnabled)
            }
            if (draft.isNotificationContentEnabled != initial.isNotificationContentEnabled) {
                saveIsAodNotificationContentEnabled(draft.isNotificationContentEnabled)
            }
            if (draft.isMediaControlsEnabled != initial.isMediaControlsEnabled) {
                saveIsAodMediaControlsEnabled(draft.isMediaControlsEnabled)
            }
            if (draft.isCalendarEnabled != initial.isCalendarEnabled) saveIsAodCalendarEnabled(draft.isCalendarEnabled)
            if (draft.isWeatherFahrenheit != initial.isWeatherFahrenheit) {
                saveIsAodWeatherFahrenheit(draft.isWeatherFahrenheit)
            }
            if (draft.isWeatherEnabled != initial.isWeatherEnabled) saveIsAodWeatherEnabled(draft.isWeatherEnabled)
        }
        if (draft.isWeatherEnabled && !initial.isWeatherEnabled) {
            refreshWeatherUseCase(nowMillis = System.currentTimeMillis(), isForced = true)
                .onFailure { e -> Timber.tag(tag).w("Could not refresh the weather: ${e.message}") }
        }
    }

    private suspend fun saveBackground(draft: CustomizeDecorUiModel, initial: CustomizeDecorUiModel) {
        if (draft.wallpaper == initial.wallpaper && draft.background === initial.background) return
        val background = draft.background
        val wallpaper = draft.wallpaper
        when {
            background != null -> {
                if (aodImageManager.saveBackground(background)) {
                    dataStoreManager.saveAodWallpaper(WallpaperValue.NONE_CODE)
                } else {
                    sendEffect(CustomizeEffect.ShowMessage(R.string.background_save_failed))
                }
            }

            wallpaper != null -> {
                dataStoreManager.saveAodWallpaper(wallpaper.code)
                aodImageManager.removeBackground()
            }

            else -> {
                dataStoreManager.saveAodWallpaper(WallpaperValue.NONE_CODE)
                aodImageManager.removeBackground()
            }
        }
    }

    private suspend fun saveDrawing(drawing: Bitmap?, initialDrawing: Bitmap?) {
        if (drawing === initialDrawing) return
        if (drawing == null) {
            aodImageManager.removeDrawing()
        } else if (!aodImageManager.saveDrawing(drawing)) {
            sendEffect(CustomizeEffect.ShowMessage(R.string.drawing_save_failed))
        }
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
        const val GLOW_PREVIEW_MILLIS = 3_000L
        const val MAX_HISTORY = 50
    }
}
