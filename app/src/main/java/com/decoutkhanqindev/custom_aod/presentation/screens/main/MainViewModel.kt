package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodRulesUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodScheduleUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ChargingRuleValue
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.ScheduleTimeValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import com.decoutkhanqindev.custom_aod.presentation.model.toUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainState
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val dataStoreManager: DataStoreManager,
    private val permissionManager: PermissionManager,
    private val languageManager: LanguageManager,
) : BaseViewModel<MainState, MainIntent, MainEffect>(
    initialState = MainState(),
), Tag {

    // Hộp thoại quyền thông báo đang mở là lần hỏi tự động lúc mở app lần đầu (bị từ chối thì để yên) hay do user bấm dòng "Thông báo".
    private var isFirstLaunchNotificationRequest = false

    init {
        observeSettings()
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
            is MainIntent.ToggleNotificationIcons -> dataStoreManager.saveIsAodNotificationIconsEnabled(intent.isEnabled)
            is MainIntent.ToggleEdgeGlow -> dataStoreManager.saveIsAodEdgeGlowEnabled(intent.isEnabled)
            is MainIntent.ToggleMediaControls -> dataStoreManager.saveIsAodMediaControlsEnabled(intent.isEnabled)
            is MainIntent.ChangeChargingRule -> dataStoreManager.saveAodChargingRule(intent.rule.code)
            is MainIntent.ToggleSchedule -> dataStoreManager.saveIsAodScheduleEnabled(intent.isEnabled)
            is MainIntent.ShowScheduleTimePicker -> updateState { copy(editingScheduleTime = intent.time) }
            is MainIntent.DismissScheduleTimePicker -> updateState { copy(editingScheduleTime = null) }
            is MainIntent.ChangeScheduleTime -> changeScheduleTime(intent.time, intent.minuteOfDay)
            is MainIntent.ChangeMinBattery -> dataStoreManager.saveAodMinBattery(intent.percent)
            is MainIntent.OpenPermission -> openPermission(intent.permission)
            is MainIntent.RefreshPermissions -> refreshPermissions()
            is MainIntent.NotificationPermissionRequested -> onFirstLaunchNotificationPermissionRequested()
            is MainIntent.NotificationPermissionResult -> onNotificationPermissionResult(intent.isGranted)
            is MainIntent.OpenLanguage -> viewModelScope.launch { sendEffect(MainEffect.NavigateToLanguage) }
            is MainIntent.Preview -> viewModelScope.launch { sendEffect(MainEffect.OpenPreview) }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(optionsFlow(), notificationOptionsFlow(), rulesFlow()) { options, notificationOptions, rules ->
                Triple(options, notificationOptions, rules)
            }.collectCatching(
                action = { (options, notificationOptions, rules) ->
                    updateState {
                        copy(isLoading = false, options = options, notificationOptions = notificationOptions, rules = rules)
                    }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun optionsFlow(): Flow<AodOptionsUiModel> = combine(
        dataStoreManager.isAodEnabled.filterNotNull(),
        dataStoreManager.isAodCustomBrightness.filterNotNull(),
        dataStoreManager.aodBrightnessPercent.filterNotNull(),
        dataStoreManager.isAodProximityEnabled.filterNotNull(),
        dataStoreManager.aodTimeoutMinutes.filterNotNull(),
    ) { isEnabled, isCustomBrightness, brightnessPercent, isProximityEnabled, timeoutMinutes ->
        AodOptionsUiModel(
            isEnabled = isEnabled,
            isCustomBrightness = isCustomBrightness,
            brightnessPercent = brightnessPercent,
            isProximityEnabled = isProximityEnabled,
            timeoutMinutes = timeoutMinutes,
        )
    }

    private fun notificationOptionsFlow(): Flow<AodNotificationOptionsUiModel> = combine(
        dataStoreManager.isAodNotificationIconsEnabled.filterNotNull(),
        dataStoreManager.isAodEdgeGlowEnabled.filterNotNull(),
        dataStoreManager.isAodMediaControlsEnabled.filterNotNull(),
    ) { isIconsEnabled, isEdgeGlowEnabled, isMediaControlsEnabled ->
        AodNotificationOptionsUiModel(
            isIconsEnabled = isIconsEnabled,
            isEdgeGlowEnabled = isEdgeGlowEnabled,
            isMediaControlsEnabled = isMediaControlsEnabled,
        )
    }

    private fun rulesFlow(): Flow<AodRulesUiModel> = combine(
        dataStoreManager.aodMinBattery.filterNotNull(),
        dataStoreManager.aodChargingRule.filterNotNull(),
        dataStoreManager.isAodScheduleEnabled.filterNotNull(),
        dataStoreManager.aodScheduleStartMinute.filterNotNull(),
        dataStoreManager.aodScheduleEndMinute.filterNotNull(),
    ) { minBattery, chargingRule, isScheduleEnabled, scheduleStartMinute, scheduleEndMinute ->
        AodRulesUiModel(
            minBattery = minBattery,
            chargingRule = ChargingRuleValue.fromCode(chargingRule),
            schedule = AodScheduleUiModel(
                isEnabled = isScheduleEnabled,
                startMinute = scheduleStartMinute,
                endMinute = scheduleEndMinute,
            ),
        )
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
        val permissions = buildList {
            add(PermissionUiModel(PermissionValue.OVERLAY, permissionManager.canDrawOverlays()))
            if (permissionManager.isXiaomi) {
                add(PermissionUiModel(PermissionValue.MIUI_LOCK_SCREEN, permissionManager.isMiuiShowWhenLockedAllowed()))
                add(
                    PermissionUiModel(
                        PermissionValue.MIUI_BACKGROUND_POPUP,
                        permissionManager.isMiuiBackgroundStartAllowed(),
                    ),
                )
            }
            add(PermissionUiModel(PermissionValue.NOTIFICATIONS, permissionManager.areNotificationsEnabled()))
            add(
                PermissionUiModel(
                    PermissionValue.NOTIFICATION_ACCESS,
                    permissionManager.isNotificationListenerEnabled(),
                ),
            )
        }
        updateState { copy(permissions = permissions.toImmutableList()) }
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

    private fun toggleAod(isEnabled: Boolean) {
        dataStoreManager.saveIsAodEnabled(isEnabled)
        viewModelScope.launch {
            sendEffect(if (isEnabled) MainEffect.StartAodService else MainEffect.StopAodService)
        }
    }

    private fun changeScheduleTime(time: ScheduleTimeValue, minuteOfDay: Int) {
        when (time) {
            ScheduleTimeValue.START -> dataStoreManager.saveAodScheduleStartMinute(minuteOfDay)
            ScheduleTimeValue.END -> dataStoreManager.saveAodScheduleEndMinute(minuteOfDay)
        }
        updateState { copy(editingScheduleTime = null) }
    }

    private fun openPermission(permission: PermissionValue) {
        val effect = when (permission) {
            PermissionValue.OVERLAY -> MainEffect.OpenOverlaySettings
            PermissionValue.MIUI_LOCK_SCREEN,
            PermissionValue.MIUI_BACKGROUND_POPUP -> MainEffect.OpenMiuiPermissionEditor

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

    private fun onFirstLaunchNotificationPermissionRequested() {
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
}
