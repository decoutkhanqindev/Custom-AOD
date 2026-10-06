package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodExtrasUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodInteractionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodRulesUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.ScheduleTimeValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.model.hasRequiredPermissions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class MainState(
    val isLoading: Boolean = true,
    val options: AodOptionsUiModel = AodOptionsUiModel(),
    val notificationOptions: AodNotificationOptionsUiModel = AodNotificationOptionsUiModel(),
    val appearance: AodAppearanceUiModel = AodAppearanceUiModel(),
    val hasBackground: Boolean = false,
    val isSavingBackground: Boolean = false,
    val wallpaper: WallpaperValue? = null,
    val extras: AodExtrasUiModel = AodExtrasUiModel(),
    val hasDrawing: Boolean = false,
    val isEditingMemo: Boolean = false,
    val isDrawingPadVisible: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val interaction: AodInteractionUiModel = AodInteractionUiModel(),
    val editingGesture: AodGestureValue? = null,
    val isFlashlightAvailable: Boolean = false,
    val isLightSensorAvailable: Boolean = false,
    val isPickupSensorAvailable: Boolean = false,
    val rules: AodRulesUiModel = AodRulesUiModel(),
    val permissions: ImmutableList<PermissionUiModel> = persistentListOf(),
    val language: LanguageUiModel? = null,
    val editingScheduleTime: ScheduleTimeValue? = null,
    @param:StringRes val lastWakeMessageRes: Int = WakeResultValue.UNKNOWN.messageRes,
    val isNotificationPermissionPending: Boolean = false,
    val isPermissionSheetDismissed: Boolean = false,
) {
    val isNotificationAccessGranted: Boolean
        get() = permissions.any { it.permission == PermissionValue.NOTIFICATION_ACCESS && it.isGranted == true }

    val requiredPermissions: ImmutableList<PermissionUiModel>
        get() = permissions.filter { it.permission.isRequired }.toImmutableList()

    // Lần đầu mở app đã có màn quyền riêng; về sau AOD đang bật mà thiếu quyền bắt buộc thì nhắc ở màn chính.
    private val isMissingRequiredPermissions: Boolean
        get() = !isLoading && options.isEnabled && permissions.isNotEmpty() && !permissions.hasRequiredPermissions

    // Đóng sheet thì nó không tự hiện lại tới lần mở app sau (ViewModel tạo mới) hay lần bật lại AOD;
    // trong lúc đó dòng cảnh báo ở đầu màn chính mở lại được sheet.
    val isPermissionSheetVisible: Boolean
        get() = isMissingRequiredPermissions && !isPermissionSheetDismissed

    val isPermissionWarningVisible: Boolean
        get() = isMissingRequiredPermissions && isPermissionSheetDismissed
}
