package com.decoutkhanqindev.custom_aod.domain.usecase

import com.decoutkhanqindev.custom_aod.domain.repository.WeatherRepository
import com.decoutkhanqindev.custom_aod.utils.suspendRunCatching
import kotlinx.coroutines.flow.first

// Thời tiết hiện tại chỉ đổi theo từng chục phút: bản đã lưu còn mới thì không gọi mạng và không bật định vị lại.
class RefreshWeatherUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(nowMillis: Long, isForced: Boolean = false): Result<Unit> = suspendRunCatching {
        val updatedAtMillis = repository.observeWeather().first()?.updatedAtMillis ?: 0L
        if (isForced || nowMillis - updatedAtMillis >= MIN_REFRESH_INTERVAL_MILLIS) repository.refreshWeather()
    }

    companion object {
        private const val MIN_REFRESH_INTERVAL_MILLIS = 30 * 60_000L
    }
}
