package com.decoutkhanqindev.custom_aod.presentation.model.aod.info

import androidx.compose.runtime.Immutable

@Immutable
data class BatteryUiModel(
    val percent: Int,
    val isCharging: Boolean,
)
