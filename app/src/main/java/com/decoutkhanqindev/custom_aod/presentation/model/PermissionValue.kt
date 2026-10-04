package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class PermissionValue(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val isRequired: Boolean,
) {
    OVERLAY(
        titleRes = R.string.perm_overlay_title,
        descriptionRes = R.string.perm_overlay_desc,
        isRequired = true,
    ),
    MIUI_LOCK_SCREEN(
        titleRes = R.string.perm_lock_title,
        descriptionRes = R.string.perm_lock_desc,
        isRequired = true,
    ),

    // HyperOS 1.0 vẫn mở được AOD khi quyền này tắt, chỉ cần "Hiển thị trên ứng dụng khác".
    MIUI_BACKGROUND_POPUP(
        titleRes = R.string.perm_popup_title,
        descriptionRes = R.string.perm_popup_desc,
        isRequired = false,
    ),
    NOTIFICATIONS(
        titleRes = R.string.perm_notif_title,
        descriptionRes = R.string.perm_notif_desc,
        isRequired = false,
    ),

    // Chỉ cho icon thông báo, viền sáng và điều khiển nhạc; đồng hồ vẫn chạy khi thiếu.
    NOTIFICATION_ACCESS(
        titleRes = R.string.perm_notif_access_title,
        descriptionRes = R.string.perm_notif_access_desc,
        isRequired = false,
    ),
}
