package com.decoutkhanqindev.custom_aod.presentation.screens.aod.state

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel

@Immutable
data class AodState(
    val battery: BatteryUiModel? = null,
    val shiftXDp: Int = 0,
    val shiftYDp: Int = 0,
    val isDark: Boolean = false,
)
