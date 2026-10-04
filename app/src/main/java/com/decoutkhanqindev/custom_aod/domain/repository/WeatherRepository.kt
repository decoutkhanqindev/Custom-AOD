package com.decoutkhanqindev.custom_aod.domain.repository

import com.decoutkhanqindev.custom_aod.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    // Bản đã lưu của lần tải gần nhất; null khi chưa tải lần nào.
    fun observeWeather(): Flow<Weather?>

    // Lấy vị trí hiện tại rồi tải thời tiết mới; không lấy được vị trí hay không có mạng thì bản đã lưu giữ nguyên.
    suspend fun refreshWeather()
}
