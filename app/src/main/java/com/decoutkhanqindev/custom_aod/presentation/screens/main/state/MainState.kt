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
    val appearance: MainAppearanceState = MainAppearanceState(),
    val extras: MainExtrasState = MainExtrasState(),
    val interaction: MainInteractionState = MainInteractionState(),
    val rules: MainRulesState = MainRulesState(),
    val permission: MainPermissionState = MainPermissionState(),
    val language: LanguageUiModel? = null,
    @param:StringRes val lastWakeMessageRes: Int = WakeResultValue.UNKNOWN.messageRes,
) {
    // Lần đầu mở app đã có màn quyền riêng (vào màn đó là xong onboarding); về sau thiếu quyền bắt buộc thì nhắc ở màn chính.
    private val isMissingRequiredPermissions: Boolean
        get() = !isLoading && permission.permissions.isNotEmpty() && !permission.permissions.hasRequiredPermissions

    // Đóng sheet thì nó không tự hiện lại tới lần mở app sau (ViewModel tạo mới) hay lần bật AOD / áp giao diện cho đồng hồ;
    // trong lúc đó dòng cảnh báo ở đầu màn chính mở lại được sheet.
    val isPermissionSheetVisible: Boolean
        get() = isMissingRequiredPermissions && !permission.isSheetDismissed

    val isPermissionWarningVisible: Boolean
        get() = isMissingRequiredPermissions && permission.isSheetDismissed
}

@Immutable
data class MainAppearanceState(
    val settings: AodAppearanceUiModel = AodAppearanceUiModel(),
    val wallpaper: WallpaperValue? = null,
    val hasBackground: Boolean = false,
    val isSavingBackground: Boolean = false,
)

@Immutable
data class MainExtrasState(
    val settings: AodExtrasUiModel = AodExtrasUiModel(),
    val hasDrawing: Boolean = false,
    val isMemoEditorVisible: Boolean = false,
    val isDrawingPadVisible: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
)

@Immutable
data class MainInteractionState(
    val settings: AodInteractionUiModel = AodInteractionUiModel(),
    val editingGesture: AodGestureValue? = null,
    val isFlashlightAvailable: Boolean = false,
    val isLightSensorAvailable: Boolean = false,
    val isPickupSensorAvailable: Boolean = false,
)

@Immutable
data class MainRulesState(
    val settings: AodRulesUiModel = AodRulesUiModel(),
    val editingScheduleTime: ScheduleTimeValue? = null,
)

@Immutable
data class MainPermissionState(
    val permissions: ImmutableList<PermissionUiModel> = persistentListOf(),
    val isSheetDismissed: Boolean = false,
) {
    val isNotificationAccessGranted: Boolean
        get() = permissions.any { it.permission == PermissionValue.NOTIFICATION_ACCESS && it.isGranted == true }

    val requiredPermissions: ImmutableList<PermissionUiModel>
        get() = permissions.filter { it.permission.isRequired }.toImmutableList()
}
