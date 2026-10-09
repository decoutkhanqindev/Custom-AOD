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
    val isDateEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_DATE_ENABLED,
    val isBatteryEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_BATTERY_ENABLED,
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
    isDateEnabled = isAodDateEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_DATE_ENABLED,
    isBatteryEnabled = isAodBatteryEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_BATTERY_ENABLED,
)

// combine có kiểu chỉ nhận tối đa 5 flow nên ghép hai lớp.
fun DataStoreManager.observeAodExtras(): Flow<AodExtrasUiModel> = combine(
    combine(
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
    },
    isAodDateEnabled.filterNotNull(),
    isAodBatteryEnabled.filterNotNull(),
) { extras, isDateEnabled, isBatteryEnabled ->
    extras.copy(isDateEnabled = isDateEnabled, isBatteryEnabled = isBatteryEnabled)
}
