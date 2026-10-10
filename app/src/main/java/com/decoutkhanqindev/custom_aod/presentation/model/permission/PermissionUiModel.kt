package com.decoutkhanqindev.custom_aod.presentation.model.permission

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class PermissionUiModel(
    val permission: PermissionValue,
    val isGranted: Boolean?,
) {
    val status: PermissionStatusValue
        get() = when {
            isGranted == true -> PermissionStatusValue.GRANTED
            isGranted == null -> PermissionStatusValue.UNKNOWN
            permission.isRequired -> PermissionStatusValue.MISSING_REQUIRED
            else -> PermissionStatusValue.MISSING_OPTIONAL
        }
}

// Mọi quyền app dùng, quyền bắt buộc đứng trước; hai quyền riêng của Xiaomi chỉ có trên máy Xiaomi. Trạng thái đổi ở app Cài đặt nên mỗi lần cần thì đọc lại.
fun PermissionManager.aodPermissions(): ImmutableList<PermissionUiModel> = buildList {
    add(PermissionUiModel(PermissionValue.OVERLAY, canDrawOverlays()))
    if (isXiaomi) {
        add(PermissionUiModel(PermissionValue.MIUI_LOCK_SCREEN, isMiuiShowWhenLockedAllowed()))
        add(
            PermissionUiModel(
                PermissionValue.MIUI_BACKGROUND_POPUP,
                isMiuiBackgroundStartAllowed()
            )
        )
    }
    add(PermissionUiModel(PermissionValue.NOTIFICATIONS, areNotificationsEnabled()))
    add(PermissionUiModel(PermissionValue.NOTIFICATION_ACCESS, isNotificationListenerEnabled()))
    add(PermissionUiModel(PermissionValue.CALENDAR, hasCalendarPermission()))
    add(PermissionUiModel(PermissionValue.LOCATION, hasCoarseLocationPermission()))
}.toImmutableList()

// Điều kiện để tự kéo app về sau khi user cấp quyền ở app Cài đặt. Quyền không đọc được (op của Xiaomi bị đánh số lại) coi như không chặn,
// cùng cách với hasRequiredPermissions.
fun PermissionManager.isGranted(permission: PermissionValue): Boolean = when (permission) {
    PermissionValue.OVERLAY -> canDrawOverlays()
    PermissionValue.MIUI_LOCK_SCREEN -> isMiuiShowWhenLockedAllowed() != false
    PermissionValue.MIUI_BACKGROUND_POPUP -> isMiuiBackgroundStartAllowed() != false
    PermissionValue.NOTIFICATIONS -> areNotificationsEnabled()
    PermissionValue.NOTIFICATION_ACCESS -> isNotificationListenerEnabled()
    PermissionValue.CALENDAR -> hasCalendarPermission()
    PermissionValue.LOCATION -> hasCoarseLocationPermission()
}

// Trang của Xiaomi chứa cả hai quyền: kéo về khi cả hai đã bật.
fun PermissionManager.areMiuiPermissionsGranted(): Boolean =
    isGranted(PermissionValue.MIUI_LOCK_SCREEN) && isGranted(PermissionValue.MIUI_BACKGROUND_POPUP)

fun PermissionManager.requiredAodPermissions(): ImmutableList<PermissionUiModel> =
    aodPermissions().filter { it.permission.isRequired }.toImmutableList()

// Quyền không đọc được trạng thái (op của Xiaomi bị đánh số lại theo bản HyperOS) thì không chặn, để user không kẹt mãi ở màn quyền.
val List<PermissionUiModel>.hasRequiredPermissions: Boolean
    get() = none { it.permission.isRequired && it.isGranted == false }
