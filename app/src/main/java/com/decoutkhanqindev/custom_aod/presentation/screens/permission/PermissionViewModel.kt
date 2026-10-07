package com.decoutkhanqindev.custom_aod.presentation.screens.permission

import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.hasRequiredPermissions
import com.decoutkhanqindev.custom_aod.presentation.model.requiredAodPermissions
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionState
import com.decoutkhanqindev.custom_aod.utils.Tag
import kotlinx.coroutines.launch
import timber.log.Timber

class PermissionViewModel(
    private val dataStoreManager: DataStoreManager,
    private val permissionManager: PermissionManager,
) : BaseViewModel<PermissionState, PermissionIntent, PermissionEffect>(
    initialState = PermissionState(permissions = permissionManager.requiredAodPermissions()),
), Tag {

    override fun onIntent(intent: PermissionIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        when (intent) {
            is PermissionIntent.RefreshPermissions -> refreshPermissions()
            is PermissionIntent.OpenPermissionSettings -> openPermissionSettings(intent.permission)
            is PermissionIntent.NotificationPermissionResult -> onNotificationPermissionResult(intent.isGranted)
            is PermissionIntent.ConfirmPermissions -> confirmPermissions()
            is PermissionIntent.NavigateBack -> viewModelScope.launch { sendEffect(PermissionEffect.NavigateBack) }
        }
    }

    // Quyền được cấp ở app Cài đặt chứ không phải ở đây, nên đọc lại mỗi lần màn hình resume.
    private fun refreshPermissions() {
        updateState { copy(permissions = permissionManager.requiredAodPermissions()) }
    }

    // Màn này chỉ có quyền bắt buộc: "Hiển thị trên ứng dụng khác", thông báo và hai quyền riêng của Xiaomi.
    private fun openPermissionSettings(permission: PermissionValue) {
        val effect = when (permission) {
            PermissionValue.OVERLAY -> PermissionEffect.OpenOverlaySettings
            PermissionValue.MIUI_LOCK_SCREEN,
            PermissionValue.MIUI_BACKGROUND_POPUP -> PermissionEffect.OpenMiuiPermissionSettings

            PermissionValue.NOTIFICATIONS ->
                if (permissionManager.needsNotificationPermission()) {
                    PermissionEffect.RequestNotificationPermission
                } else {
                    PermissionEffect.OpenNotificationSettings
                }

            else -> return
        }
        viewModelScope.launch { sendEffect(effect) }
    }

    // Đã cho phép thì start lại service để nó đăng lại thông báo; bị từ chối thì mở trang cài đặt thông báo (sau 2 lần từ chối hệ thống không hiện hộp thoại nữa).
    private fun onNotificationPermissionResult(isGranted: Boolean) {
        refreshPermissions()
        viewModelScope.launch {
            when {
                isGranted -> if (dataStoreManager.isAodEnabled.value == true) sendEffect(PermissionEffect.StartAodService)
                else -> sendEffect(PermissionEffect.OpenNotificationSettings)
            }
        }
    }

    // Đọc lại quyền ngay lúc bấm (quyền có thể vừa bị tắt mà màn chưa kịp đọc lại); onboarding đã xong từ lúc vào màn này.
    private fun confirmPermissions() {
        val permissions = permissionManager.requiredAodPermissions()
        updateState { copy(permissions = permissions) }
        if (!permissions.hasRequiredPermissions) return
        viewModelScope.launch { sendEffect(PermissionEffect.NavigateToMain) }
    }
}
