package com.decoutkhanqindev.custom_aod.domain.usecase

import com.decoutkhanqindev.custom_aod.domain.model.Weather
import com.decoutkhanqindev.custom_aod.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class ObserveWeatherUseCase(private val repository: WeatherRepository) {
    operator fun invoke(): Flow<Weather?> = repository.observeWeather()
}
