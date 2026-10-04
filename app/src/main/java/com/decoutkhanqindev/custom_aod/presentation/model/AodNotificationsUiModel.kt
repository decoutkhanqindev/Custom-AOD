package com.decoutkhanqindev.custom_aod.presentation.model

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.device.notification.ActiveNotification
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class AodNotificationsUiModel(
    val icons: ImmutableList<NotificationIconUiModel> = persistentListOf(),
    val overflowCount: Int = 0,
) {
    companion object {
        const val MAX_ICONS = 5
    }
}

@Immutable
data class NotificationIconUiModel(
    val packageName: String,
    val icon: Bitmap,
)

// Một icon cho mỗi app như thanh trạng thái; danh sách đã xếp mới nhất trước nên app có thông báo mới nhất đứng đầu.
fun List<ActiveNotification>.toAodNotificationsUiModel(): AodNotificationsUiModel {
    val latestPerApp = distinctBy { notification -> notification.packageName }
    return AodNotificationsUiModel(
        icons = latestPerApp
            .take(AodNotificationsUiModel.MAX_ICONS)
            .map { notification -> NotificationIconUiModel(packageName = notification.packageName, icon = notification.icon) }
            .toImmutableList(),
        overflowCount = (latestPerApp.size - AodNotificationsUiModel.MAX_ICONS).coerceAtLeast(0),
    )
}

// Màu của app làm viền sáng; app không đặt màu, hoặc màu quá tối gần như không thấy trên nền đen, thì dùng màu mặc định (null).
fun ActiveNotification.glowColorArgb(): Int? =
    color.takeIf { it != Notification.COLOR_DEFAULT && Color.luminance(it) >= MIN_GLOW_LUMINANCE }

private const val MIN_GLOW_LUMINANCE = 0.1f
