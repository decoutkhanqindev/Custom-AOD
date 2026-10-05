package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

@Immutable
data class AodExtrasUiModel(
    val memo: String = DataStoreManager.DEFAULT_AOD_MEMO,
    val isCalendarEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_CALENDAR_ENABLED,
    val isWeatherEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_WEATHER_ENABLED,
    val isWeatherFahrenheit: Boolean = DataStoreManager.DEFAULT_IS_AOD_WEATHER_FAHRENHEIT,
) {
    companion object {
        const val MEMO_MAX_LENGTH = 120
    }
}

fun DataStoreManager.currentAodExtras(): AodExtrasUiModel = AodExtrasUiModel(
    memo = aodMemo.value ?: DataStoreManager.DEFAULT_AOD_MEMO,
    isCalendarEnabled = isAodCalendarEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_CALENDAR_ENABLED,
    isWeatherEnabled = isAodWeatherEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_WEATHER_ENABLED,
    isWeatherFahrenheit = isAodWeatherFahrenheit.value ?: DataStoreManager.DEFAULT_IS_AOD_WEATHER_FAHRENHEIT,
)

fun DataStoreManager.observeAodExtras(): Flow<AodExtrasUiModel> = combine(
    aodMemo.filterNotNull(),
    isAodCalendarEnabled.filterNotNull(),
    isAodWeatherEnabled.filterNotNull(),
    isAodWeatherFahrenheit.filterNotNull(),
) { memo, isCalendarEnabled, isWeatherEnabled, isWeatherFahrenheit ->
    AodExtrasUiModel(
        memo = memo,
        isCalendarEnabled = isCalendarEnabled,
        isWeatherEnabled = isWeatherEnabled,
        isWeatherFahrenheit = isWeatherFahrenheit,
    )
}
