package com.decoutkhanqindev.custom_aod.presentation.screens.customize

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.Immutable
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.CalendarEventUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.MediaUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.NotificationContentUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.NotificationIconUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.WeatherConditionValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.WeatherUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodInteractionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeDraftUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

// Dữ liệu mẫu cho phần cần dữ liệu thật trên khung xem trước (thông báo, sự kiện, nhạc); chữ lấy từ strings.xml ở Compose.
@Immutable
data class CustomizePreviewSamples(
    val notificationTitle: String,
    val notificationText: String,
    val eventTitle: String,
    val mediaTitle: String,
    val mediaArtist: String,
    val icons: ImmutableList<Bitmap> = sampleIcons(),
) {
    private enum class IconShape { CIRCLE, SQUARE, TRIANGLE }

    companion object {
        private const val ICON_SIZE = 48
        private const val BATTERY_PERCENT = 72
        private const val TEMPERATURE_C = 28
        private const val TEMPERATURE_F = 82
        private const val HOUR_MILLIS = 60 * 60_000L

        // Thao tác và phím âm lượng của AOD tắt hết để không chặn thao tác của màn tùy chỉnh.
        private val PreviewInteraction = AodInteractionUiModel(
            actions = AodGestureValue.entries.associateWith { AodActionValue.NONE }
                .toImmutableMap(),
            isAutoDimEnabled = false,
            isRaiseToWakeEnabled = false,
        )

        // Hình đơn giản màu trắng: AOD tự tô lại theo màu chữ như icon thông báo thật.
        private fun sampleIcons(): ImmutableList<Bitmap> = IconShape.entries.map { shape ->
            createBitmap(ICON_SIZE, ICON_SIZE).applyCanvas {
                val paint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
                val size = ICON_SIZE.toFloat()
                val inset = size / 6
                when (shape) {
                    IconShape.CIRCLE -> drawCircle(size / 2, size / 2, size / 2 - inset, paint)
                    IconShape.SQUARE -> drawRoundRect(
                        inset,
                        inset,
                        size - inset,
                        size - inset,
                        inset,
                        inset,
                        paint
                    )

                    IconShape.TRIANGLE -> drawPath(
                        Path().apply {
                            moveTo(size / 2, inset)
                            lineTo(size - inset, size - inset)
                            lineTo(inset, size - inset)
                            close()
                        },
                        paint,
                    )
                }
            }
        }.toImmutableList()

        fun CustomizeDraftUiModel.toPreviewAodState(
            nowMillis: Long,
            isGlowing: Boolean,
            samples: CustomizePreviewSamples,
        ): AodState = AodState(
            nowMillis = nowMillis,
            appearance = appearance.clock.copy(isLandscape = false),
            interaction = PreviewInteraction,
            extras = extras,
            wallpaper = decor.wallpaper,
            background = decor.background,
            drawing = decor.drawing,
            weather = WeatherUiModel(
                temperature = if (info.isWeatherFahrenheit) TEMPERATURE_F else TEMPERATURE_C,
                condition = WeatherConditionValue.CLEAR,
                isDay = true,
                updatedAtMillis = nowMillis,
            ).takeIf { info.isWeatherEnabled },
            events = if (info.isCalendarEnabled) {
                persistentListOf(
                    CalendarEventUiModel(
                        title = samples.eventTitle,
                        beginMillis = nowMillis + HOUR_MILLIS,
                        endMillis = nowMillis + 2 * HOUR_MILLIS,
                        isAllDay = false,
                    ),
                )
            } else {
                persistentListOf()
            },
            battery = BatteryUiModel(percent = BATTERY_PERCENT, isCharging = false),
            notifications = AodNotificationsUiModel(
                icons = if (info.isNotificationIconsEnabled) {
                    samples.icons
                        .mapIndexed { index, icon ->
                            NotificationIconUiModel(
                                packageName = "sample.$index",
                                icon = icon
                            )
                        }
                        .toImmutableList()
                } else {
                    persistentListOf()
                },
                latest = NotificationContentUiModel(
                    key = "sample",
                    icon = samples.icons.first(),
                    title = samples.notificationTitle,
                    text = samples.notificationText,
                    isHidden = false,
                ).takeIf { info.isNotificationContentEnabled },
            ),
            media = MediaUiModel(
                title = samples.mediaTitle,
                artist = samples.mediaArtist,
                isPlaying = true,
                canSkipToPrevious = true,
                canSkipToNext = true,
            ).takeIf { info.isMediaControlsEnabled },
            isHintVisible = false,
            isGlowing = isGlowing,
        )
    }
}
