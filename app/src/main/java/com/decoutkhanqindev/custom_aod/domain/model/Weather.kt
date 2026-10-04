package com.decoutkhanqindev.custom_aod.domain.model

data class Weather(
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val updatedAtMillis: Long,
)
