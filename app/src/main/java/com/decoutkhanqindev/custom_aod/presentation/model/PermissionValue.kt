package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class PermissionValue(
    @param:StringRes val titleRes: Int,
    @param:StringRes val nameRes: Int,
    @param:StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val isRequired: Boolean,
) {
    OVERLAY(
        titleRes = R.string.perm_overlay_title,
        nameRes = R.string.perm_overlay_name,
        descriptionRes = R.string.perm_overlay_desc,
        icon = Icons.Outlined.Layers,
        isRequired = true,
    ),
    MIUI_LOCK_SCREEN(
        titleRes = R.string.perm_lock_title,
        nameRes = R.string.perm_lock_name,
        descriptionRes = R.string.perm_lock_desc,
        icon = Icons.Outlined.Lock,
        isRequired = true,
    ),

    // HyperOS 1.0 vẫn mở được AOD khi quyền này tắt, chỉ cần "Hiển thị trên ứng dụng khác".
    MIUI_BACKGROUND_POPUP(
        titleRes = R.string.perm_popup_title,
        nameRes = R.string.perm_popup_name,
        descriptionRes = R.string.perm_popup_desc,
        icon = Icons.Outlined.PictureInPictureAlt,
        isRequired = true,
    ),
    NOTIFICATIONS(
        titleRes = R.string.perm_notif_title,
        nameRes = R.string.perm_notif_title,
        descriptionRes = R.string.perm_notif_desc,
        icon = Icons.Outlined.Notifications,
        isRequired = false,
    ),

    // Chỉ cho icon thông báo, viền sáng và điều khiển nhạc; đồng hồ vẫn chạy khi thiếu.
    NOTIFICATION_ACCESS(
        titleRes = R.string.perm_notif_access_title,
        nameRes = R.string.perm_notif_access_title,
        descriptionRes = R.string.perm_notif_access_desc,
        icon = Icons.Outlined.NotificationsActive,
        isRequired = false,
    ),
}
