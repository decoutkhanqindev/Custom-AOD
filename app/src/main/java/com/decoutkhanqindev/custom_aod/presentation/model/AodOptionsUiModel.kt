package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager

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
