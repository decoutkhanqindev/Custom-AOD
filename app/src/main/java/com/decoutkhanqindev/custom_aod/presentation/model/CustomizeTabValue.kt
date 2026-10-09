package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.decoutkhanqindev.custom_aod.R

// Mỗi tab chỉnh một phần trên AOD; permission = quyền phải có thì phần đó mới hiện được trên AOD thật.
@Immutable
enum class CustomizeTabValue(
    val cluster: CustomizeClusterValue,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    val permission: PermissionValue? = null,
) {
    CLOCK(CustomizeClusterValue.APPEARANCE, Icons.Outlined.Schedule, R.string.customize_tab_clock),
    DATE(CustomizeClusterValue.APPEARANCE, Icons.Outlined.CalendarToday, R.string.customize_tab_date),
    BATTERY(CustomizeClusterValue.APPEARANCE, Icons.Outlined.BatteryFull, R.string.customize_tab_battery),
    BACKGROUND(CustomizeClusterValue.DECOR, Icons.Outlined.Wallpaper, R.string.customize_tab_background),
    DRAWING(CustomizeClusterValue.DECOR, Icons.Outlined.Brush, R.string.customize_tab_drawing),
    MEMO(CustomizeClusterValue.DECOR, Icons.Outlined.EditNote, R.string.customize_tab_memo),
    NOTIFICATIONS(
        CustomizeClusterValue.INFO,
        Icons.Outlined.Notifications,
        R.string.customize_tab_notifications,
        PermissionValue.NOTIFICATION_ACCESS,
    ),
    EVENTS(CustomizeClusterValue.INFO, Icons.Outlined.Event, R.string.customize_tab_events, PermissionValue.CALENDAR),
    WEATHER(CustomizeClusterValue.INFO, Icons.Outlined.WbSunny, R.string.customize_tab_weather, PermissionValue.LOCATION),
    MEDIA(
        CustomizeClusterValue.INFO,
        Icons.Outlined.MusicNote,
        R.string.customize_tab_media,
        PermissionValue.NOTIFICATION_ACCESS,
    ),
    EFFECTS(
        CustomizeClusterValue.EFFECT,
        Icons.Outlined.AutoAwesome,
        R.string.customize_tab_effects,
        PermissionValue.NOTIFICATION_ACCESS,
    ),
}
