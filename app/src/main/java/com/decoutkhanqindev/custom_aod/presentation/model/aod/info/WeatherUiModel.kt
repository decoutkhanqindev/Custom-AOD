package com.decoutkhanqindev.custom_aod.presentation.model.aod.info

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.decoutkhanqindev.custom_aod.domain.model.Weather
import kotlin.math.roundToInt

@Immutable
data class WeatherUiModel(
    val temperature: Int,
    val condition: WeatherConditionValue,
    val isDay: Boolean,
    val updatedAtMillis: Long,
) {
    val icon: ImageVector
        get() = if (isDay) condition.dayIcon else condition.nightIcon

    // Bản đã lưu cũ hơn MAX_AGE_MILLIS (mất quyền vị trí, mất mạng lâu) thì không hiện, để khỏi hiện thời tiết sai.
    fun isFreshAt(nowMillis: Long): Boolean = nowMillis - updatedAtMillis < MAX_AGE_MILLIS

    companion object {
        private const val MAX_AGE_MILLIS = 3 * 60 * 60_000L
    }
}

fun Weather.toUiModel(isFahrenheit: Boolean): WeatherUiModel = WeatherUiModel(
    temperature = (if (isFahrenheit) temperatureCelsius * 9 / 5 + 32 else temperatureCelsius).roundToInt(),
    condition = condition.toValue(),
    isDay = isDay,
    updatedAtMillis = updatedAtMillis,
)
