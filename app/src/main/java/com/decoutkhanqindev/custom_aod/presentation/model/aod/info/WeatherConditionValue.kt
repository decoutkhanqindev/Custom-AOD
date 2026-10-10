package com.decoutkhanqindev.custom_aod.presentation.model.aod.info

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.domain.model.WeatherCondition

@Immutable
enum class WeatherConditionValue(
    val dayIcon: ImageVector,
    val nightIcon: ImageVector,
    @param:StringRes val labelRes: Int,
) {
    CLEAR(
        dayIcon = Icons.Default.WbSunny,
        nightIcon = Icons.Default.NightsStay,
        labelRes = R.string.weather_clear
    ),
    PARTLY_CLOUDY(
        dayIcon = Icons.Default.WbCloudy,
        nightIcon = Icons.Default.NightsStay,
        labelRes = R.string.weather_partly_cloudy,
    ),
    CLOUDY(
        dayIcon = Icons.Default.Cloud,
        nightIcon = Icons.Default.Cloud,
        labelRes = R.string.weather_cloudy
    ),
    FOG(
        dayIcon = Icons.Default.Dehaze,
        nightIcon = Icons.Default.Dehaze,
        labelRes = R.string.weather_fog
    ),
    DRIZZLE(
        dayIcon = Icons.Default.Grain,
        nightIcon = Icons.Default.Grain,
        labelRes = R.string.weather_drizzle
    ),
    RAIN(
        dayIcon = Icons.Default.Umbrella,
        nightIcon = Icons.Default.Umbrella,
        labelRes = R.string.weather_rain
    ),
    SNOW(
        dayIcon = Icons.Default.AcUnit,
        nightIcon = Icons.Default.AcUnit,
        labelRes = R.string.weather_snow
    ),
    THUNDERSTORM(
        dayIcon = Icons.Default.Thunderstorm,
        nightIcon = Icons.Default.Thunderstorm,
        labelRes = R.string.weather_thunderstorm,
    ),
}

fun WeatherCondition.toValue(): WeatherConditionValue = when (this) {
    WeatherCondition.CLEAR -> WeatherConditionValue.CLEAR
    WeatherCondition.PARTLY_CLOUDY -> WeatherConditionValue.PARTLY_CLOUDY
    WeatherCondition.CLOUDY -> WeatherConditionValue.CLOUDY
    WeatherCondition.FOG -> WeatherConditionValue.FOG
    WeatherCondition.DRIZZLE -> WeatherConditionValue.DRIZZLE
    WeatherCondition.RAIN -> WeatherConditionValue.RAIN
    WeatherCondition.SNOW -> WeatherConditionValue.SNOW
    WeatherCondition.THUNDERSTORM -> WeatherConditionValue.THUNDERSTORM
}
