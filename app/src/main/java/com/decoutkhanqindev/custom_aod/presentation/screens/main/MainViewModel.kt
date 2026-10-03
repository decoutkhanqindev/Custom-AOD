package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainState
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val dataStoreManager: DataStoreManager,
    private val permissionManager: PermissionManager,
) : BaseViewModel<MainState, MainIntent, MainEffect>(
    initialState = MainState(),
), Tag {

    init {
        observeOptions()
        observeLastWake()
        refreshPermissions()
        checkFirstLaunchNotificationPermission()
    }

    override fun onIntent(intent: MainIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        when (intent) {
            is MainIntent.ToggleAod -> toggleAod(intent.isEnabled)
            is MainIntent.ToggleDimBrightness -> dataStoreManager.saveIsAodDimBrightness(intent.isEnabled)
            is MainIntent.ToggleProximity -> dataStoreManager.saveIsAodProximityEnabled(intent.isEnabled)
            is MainIntent.ChangeTimeout -> dataStoreManager.saveAodTimeoutMinutes(intent.minutes)
            is MainIntent.ChangeMinBattery -> dataStoreManager.saveAodMinBattery(intent.percent)
            is MainIntent.OpenPermission -> openPermission(intent.permission)
            is MainIntent.RefreshPermissions -> refreshPermissions()
            is MainIntent.NotificationPermissionRequested ->
                updateState { copy(isNotificationPermissionPending = false) }

            is MainIntent.NotificationPermissionResult ->
                onNotificationPermissionResult(isGranted = intent.isGranted, isFirstLaunch = false)

            is MainIntent.FirstLaunchNotificationPermissionResult ->
                onNotificationPermissionResult(isGranted = intent.isGranted, isFirstLaunch = true)

            is MainIntent.Preview -> viewModelScope.launch { sendEffect(MainEffect.OpenPreview) }
        }
    }

    private fun observeOptions() {
        viewModelScope.launch {
            combine(
                dataStoreManager.isAodEnabled.filterNotNull(),
                dataStoreManager.isAodDimBrightness.filterNotNull(),
                dataStoreManager.isAodProximityEnabled.filterNotNull(),
                dataStoreManager.aodTimeoutMinutes.filterNotNull(),
                dataStoreManager.aodMinBattery.filterNotNull(),
            ) { isEnabled, isDimBrightness, isProximityEnabled, timeoutMinutes, minBattery ->
                AodOptionsUiModel(
                    isEnabled = isEnabled,
                    isDimBrightness = isDimBrightness,
                    isProximityEnabled = isProximityEnabled,
                    timeoutMinutes = timeoutMinutes,
                    minBattery = minBattery,
                )
            }.collectCatching(
                action = { options -> updateState { copy(isLoading = false, options = options) } },
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

    private fun openPermission(permission: PermissionValue) {
        val effect = when (permission) {
            PermissionValue.OVERLAY -> MainEffect.OpenOverlaySettings
            PermissionValue.MIUI_LOCK_SCREEN,
            PermissionValue.MIUI_BACKGROUND_POPUP -> MainEffect.OpenMiuiPermissionEditor

            PermissionValue.NOTIFICATIONS ->
                if (permissionManager.needsNotificationPermission()) {
                    MainEffect.RequestNotificationPermission
                } else {
                    MainEffect.OpenNotificationSettings
                }
        }
        viewModelScope.launch { sendEffect(effect) }
    }

    // Đã cho phép thì start lại service để nó đăng lại thông báo; bị từ chối từ dòng "Thông báo" thì mở trang cài đặt (sau 2 lần từ chối hệ thống không hiện hộp thoại nữa).
    private fun onNotificationPermissionResult(isGranted: Boolean, isFirstLaunch: Boolean) {
        viewModelScope.launch {
            when {
                isGranted -> if (dataStoreManager.isAodEnabled.value == true) sendEffect(MainEffect.StartAodService)
                !isFirstLaunch -> sendEffect(MainEffect.OpenNotificationSettings)
            }
        }
        refreshPermissions()
    }
}
