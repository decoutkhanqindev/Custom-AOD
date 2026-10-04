package com.decoutkhanqindev.custom_aod.data.mapper

import com.decoutkhanqindev.custom_aod.domain.model.WeatherCondition

// Mã thời tiết WMO mà Open-Meteo trả về; mã lạ coi như trời nhiều mây.
fun weatherConditionOf(wmoCode: Int): WeatherCondition = when (wmoCode) {
    0 -> WeatherCondition.CLEAR
    1, 2 -> WeatherCondition.PARTLY_CLOUDY
    45, 48 -> WeatherCondition.FOG
    51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
    61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
    71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
    95, 96, 99 -> WeatherCondition.THUNDERSTORM
    else -> WeatherCondition.CLOUDY
}
