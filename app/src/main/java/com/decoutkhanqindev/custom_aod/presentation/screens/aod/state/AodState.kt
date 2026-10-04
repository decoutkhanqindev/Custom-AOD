package com.decoutkhanqindev.custom_aod.presentation.screens.aod.state

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodInteractionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.MediaUiModel

@Immutable
data class AodState(
    val nowMillis: Long = 0L,
    val appearance: AodAppearanceUiModel = AodAppearanceUiModel(),
    val interaction: AodInteractionUiModel = AodInteractionUiModel(),
    val background: Bitmap? = null,
    val battery: BatteryUiModel? = null,
    val notifications: AodNotificationsUiModel = AodNotificationsUiModel(),
    val media: MediaUiModel? = null,
    val isFlashlightOn: Boolean = false,
    val shiftXDp: Int = 0,
    val shiftYDp: Int = 0,
    val isHintVisible: Boolean = true,
    val isGlowing: Boolean = false,
    @param:ColorInt val glowColorArgb: Int? = null,
    val isDimmed: Boolean = false,
    val isDark: Boolean = false,
) {
    // Dòng "Chạm 2 lần để thoát" chỉ đúng khi chạm 2 lần vẫn là về màn hình khoá.
    val isExitHintVisible: Boolean
        get() = isHintVisible && interaction.actionOf(AodGestureValue.DOUBLE_TAP) == AodActionValue.CLOSE
}
