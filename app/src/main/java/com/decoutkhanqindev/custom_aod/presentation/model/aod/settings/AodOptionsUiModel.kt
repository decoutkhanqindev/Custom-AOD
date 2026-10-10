package com.decoutkhanqindev.custom_aod.presentation.model.aod.settings

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

@Immutable
data class AodOptionsUiModel(
    val isEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_ENABLED,
    val isCustomBrightness: Boolean = DataStoreManager.DEFAULT_IS_AOD_CUSTOM_BRIGHTNESS,
    val brightnessPercent: Int = DataStoreManager.DEFAULT_AOD_BRIGHTNESS_PERCENT,
    val isProximityEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_PROXIMITY_ENABLED,
    val timeoutMinutes: Int = DataStoreManager.DEFAULT_AOD_TIMEOUT_MINUTES,
) {
    companion object {
        const val BRIGHTNESS_MIN_PERCENT = 1
        const val BRIGHTNESS_MAX_PERCENT = 100
        const val TIMEOUT_STEP_MINUTES = 5
        const val TIMEOUT_MAX_MINUTES = 120
    }
}

fun DataStoreManager.observeAodOptions(): Flow<AodOptionsUiModel> = combine(
    isAodEnabled.filterNotNull(),
    isAodCustomBrightness.filterNotNull(),
    aodBrightnessPercent.filterNotNull(),
    isAodProximityEnabled.filterNotNull(),
    aodTimeoutMinutes.filterNotNull(),
) { isEnabled, isCustomBrightness, brightnessPercent, isProximityEnabled, timeoutMinutes ->
    AodOptionsUiModel(
        isEnabled = isEnabled,
        isCustomBrightness = isCustomBrightness,
        brightnessPercent = brightnessPercent,
        isProximityEnabled = isProximityEnabled,
        timeoutMinutes = timeoutMinutes,
    )
}
