package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.decoutkhanqindev.custom_aod.R

// Mọi quyền app dùng. isRequired: thiếu thì AOD mặc định không hiện được — màn quyền lúc mở app lần đầu và bottom sheet ở màn chính đòi đủ.
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

    // HyperOS chặn mở AOD từ nền khi thiếu quyền này, kể cả khi đã có "Hiển thị trên ứng dụng khác".
    MIUI_BACKGROUND_POPUP(
        titleRes = R.string.perm_popup_title,
        nameRes = R.string.perm_popup_name,
        descriptionRes = R.string.perm_popup_desc,
        icon = Icons.Outlined.PictureInPictureAlt,
        isRequired = true,
    ),

    // Thông báo của foreground service giữ đồng hồ chạy nền; Android 13+ phải được cho phép mới hiện.
    NOTIFICATIONS(
        titleRes = R.string.perm_notif_title,
        nameRes = R.string.perm_notif_name,
        descriptionRes = R.string.perm_notif_desc,
        icon = Icons.Outlined.Notifications,
        isRequired = true,
    ),

    // Chỉ cho icon thông báo, viền sáng và điều khiển nhạc; đồng hồ vẫn chạy khi thiếu.
    NOTIFICATION_ACCESS(
        titleRes = R.string.perm_notif_access_title,
        nameRes = R.string.perm_notif_access_title,
        descriptionRes = R.string.perm_notif_access_desc,
        icon = Icons.Outlined.NotificationsActive,
        isRequired = false,
    ),

    // Chỉ cho "Sự kiện hôm nay" và "Thời tiết": bật hai mục đó mới hỏi.
    CALENDAR(
        titleRes = R.string.perm_calendar_title,
        nameRes = R.string.perm_calendar_title,
        descriptionRes = R.string.perm_calendar_desc,
        icon = Icons.Outlined.CalendarMonth,
        isRequired = false,
    ),
    LOCATION(
        titleRes = R.string.perm_location_title,
        nameRes = R.string.perm_location_title,
        descriptionRes = R.string.perm_location_desc,
        icon = Icons.Outlined.LocationOn,
        isRequired = false,
    ),
}
