package com.decoutkhanqindev.custom_aod.data.network.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    @SerialName("current") val current: CurrentWeatherResponse,
)

@Serializable
data class CurrentWeatherResponse(
    @SerialName("temperature_2m") val temperatureCelsius: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int,
)
