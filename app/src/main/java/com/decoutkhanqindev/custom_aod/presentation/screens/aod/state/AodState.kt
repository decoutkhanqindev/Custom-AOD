package com.decoutkhanqindev.custom_aod.presentation.screens.aod.state

import androidx.annotation.ColorInt
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.MediaUiModel

@Immutable
data class AodState(
    val nowMillis: Long = 0L,
    val battery: BatteryUiModel? = null,
    val notifications: AodNotificationsUiModel = AodNotificationsUiModel(),
    val media: MediaUiModel? = null,
    val shiftXDp: Int = 0,
    val shiftYDp: Int = 0,
    val isHintVisible: Boolean = true,
    val isGlowing: Boolean = false,
    @param:ColorInt val glowColorArgb: Int? = null,
    val isDark: Boolean = false,
)
