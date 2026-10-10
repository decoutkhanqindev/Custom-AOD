package com.decoutkhanqindev.custom_aod.data.repository

import com.decoutkhanqindev.custom_aod.data.device.location.DeviceLocationManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.mapper.weatherConditionOf
import com.decoutkhanqindev.custom_aod.data.network.api.OpenMeteoApiService
import com.decoutkhanqindev.custom_aod.domain.model.Weather
import com.decoutkhanqindev.custom_aod.domain.repository.WeatherRepository
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.withContextCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber
import kotlin.math.roundToInt

// Bản thời tiết gần nhất lưu trong DataStore: AOD có số liệu ngay cả khi mất mạng hay chưa định vị xong.
class WeatherRepositoryImpl(
    private val openMeteoApiService: OpenMeteoApiService,
    private val deviceLocationManager: DeviceLocationManager,
    private val dataStoreManager: DataStoreManager,
) : WeatherRepository, Tag {

    override fun observeWeather(): Flow<Weather?> = combine(
        dataStoreManager.weatherTemperatureCelsius.filterNotNull(),
        dataStoreManager.weatherCode.filterNotNull(),
        dataStoreManager.isWeatherDay.filterNotNull(),
        dataStoreManager.weatherUpdatedAtMillis.filterNotNull(),
    ) { temperatureCelsius, code, isDay, updatedAtMillis ->
        if (updatedAtMillis == DataStoreManager.DEFAULT_WEATHER_UPDATED_AT_MILLIS) {
            null
        } else {
            Weather(
                temperatureCelsius = temperatureCelsius,
                condition = weatherConditionOf(code),
                isDay = isDay,
                updatedAtMillis = updatedAtMillis,
            )
        }
    }

    // Lỗi (chưa định vị được, mất mạng, API lỗi) thì giữ bản đã lưu; hàm suspend của Retrofit tự chạy ngoài main thread nên không đổi dispatcher.
    override suspend fun refreshWeather(): Unit = withContextCatching(
        block = {
            val coordinates = checkNotNull(deviceLocationManager.currentCoarseLocation()) {
                "Location is unavailable (no permission, or location is off)"
            }
            val current = openMeteoApiService.getForecast(
                latitude = coordinates.latitude.roundedCoordinate(),
                longitude = coordinates.longitude.roundedCoordinate(),
                current = OpenMeteoApiService.CURRENT_WEATHER_FIELDS,
            ).current
            dataStoreManager.saveWeather(
                temperatureCelsius = current.temperatureCelsius,
                code = current.weatherCode,
                isDay = current.isDay == OPEN_METEO_DAY,
                updatedAtMillis = System.currentTimeMillis(),
            )
        },
        catch = { e -> Timber.tag(tag).w("Could not refresh the weather: ${e.message}") },
    )

    // Làm tròn 2 chữ số thập phân (khoảng 1 km) trước khi gửi: đủ cho thời tiết, không gửi vị trí chính xác ra ngoài.
    private fun Double.roundedCoordinate(): Double =
        (this * COORDINATE_SCALE).roundToInt() / COORDINATE_SCALE

    companion object {
        private const val COORDINATE_SCALE = 100.0
        private const val OPEN_METEO_DAY = 1
    }
}
