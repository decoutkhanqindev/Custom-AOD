package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager

@Immutable
data class AodOptionsUiModel(
    val isEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_ENABLED,
    val isDimBrightness: Boolean = DataStoreManager.DEFAULT_IS_AOD_DIM_BRIGHTNESS,
    val isProximityEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_PROXIMITY_ENABLED,
    val timeoutMinutes: Int = DataStoreManager.DEFAULT_AOD_TIMEOUT_MINUTES,
    val minBattery: Int = DataStoreManager.DEFAULT_AOD_MIN_BATTERY,
)
