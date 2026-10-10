package com.decoutkhanqindev.custom_aod.presentation.model.aod.info

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.device.notification.ActiveNotification
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodNotificationOptionsUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class AodNotificationsUiModel(
    val icons: ImmutableList<NotificationIconUiModel> = persistentListOf(),
    val overflowCount: Int = 0,
    val latest: NotificationContentUiModel? = null,
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

// Thông báo mới nhất có chữ để hiện; isHidden = màn hình khoá của hệ thống đang che nội dung của nó.
@Immutable
data class NotificationContentUiModel(
    val key: String,
    val icon: Bitmap,
    val title: String,
    val text: String,
    val isHidden: Boolean,
)

// Một icon cho mỗi app như thanh trạng thái; danh sách đã xếp mới nhất trước nên app có thông báo mới nhất đứng đầu.
fun List<ActiveNotification>.toAodNotificationsUiModel(options: AodNotificationOptionsUiModel): AodNotificationsUiModel {
    val latestPerApp =
        if (options.isIconsEnabled) distinctBy { notification -> notification.packageName } else emptyList()
    return AodNotificationsUiModel(
        icons = latestPerApp
            .take(AodNotificationsUiModel.MAX_ICONS)
            .map { notification ->
                NotificationIconUiModel(
                    packageName = notification.packageName,
                    icon = notification.icon
                )
            }
            .toImmutableList(),
        overflowCount = (latestPerApp.size - AodNotificationsUiModel.MAX_ICONS).coerceAtLeast(0),
        latest = if (options.isContentEnabled) firstNotNullOfOrNull { it.toContentUiModel() } else null,
    )
}

// Không có tiêu đề thì đưa nội dung lên dòng tiêu đề; thông báo không có chữ nào thì bỏ qua để lấy thông báo kế tiếp.
private fun ActiveNotification.toContentUiModel(): NotificationContentUiModel? {
    val shownContent = content
        ?: return NotificationContentUiModel(
            key = key,
            icon = icon,
            title = "",
            text = "",
            isHidden = true
        )
    if (shownContent.title.isEmpty() && shownContent.text.isEmpty()) return null
    return NotificationContentUiModel(
        key = key,
        icon = icon,
        title = shownContent.title.ifEmpty { shownContent.text },
        text = if (shownContent.title.isEmpty()) "" else shownContent.text,
        isHidden = false,
    )
}

// Màu của app làm viền sáng; app không đặt màu, hoặc màu quá tối gần như không thấy trên nền đen, thì dùng màu mặc định (null).
fun ActiveNotification.glowColorArgb(): Int? =
    color.takeIf { it != Notification.COLOR_DEFAULT && Color.luminance(it) >= MIN_GLOW_LUMINANCE }

private const val MIN_GLOW_LUMINANCE = 0.1f
