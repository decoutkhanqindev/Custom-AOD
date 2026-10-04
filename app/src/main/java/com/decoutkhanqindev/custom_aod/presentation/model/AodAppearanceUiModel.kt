package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager

@Immutable
data class AodAppearanceUiModel(
    val face: ClockFaceValue = ClockFaceValue.DIGITAL,
    val font: ClockFontValue = ClockFontValue.DEFAULT,
    val color: ClockColorValue = ClockColorValue.GREY,
    val sizePercent: Int = DataStoreManager.DEFAULT_AOD_CLOCK_SIZE_PERCENT,
    val isLandscape: Boolean = DataStoreManager.DEFAULT_IS_AOD_LANDSCAPE,
) {
    val scale: Float get() = sizePercent / PERCENT

    companion object {
        const val SIZE_MIN_PERCENT = 60
        const val SIZE_MAX_PERCENT = 150
        const val SIZE_STEP_PERCENT = 10
        private const val PERCENT = 100f
    }
}

fun DataStoreManager.currentAodAppearance(): AodAppearanceUiModel = AodAppearanceUiModel(
    face = ClockFaceValue.fromCode(aodClockFace.value),
    font = ClockFontValue.fromCode(aodClockFont.value),
    color = ClockColorValue.fromCode(aodClockColor.value),
    sizePercent = aodClockSizePercent.value ?: DataStoreManager.DEFAULT_AOD_CLOCK_SIZE_PERCENT,
    isLandscape = isAodLandscape.value ?: DataStoreManager.DEFAULT_IS_AOD_LANDSCAPE,
)
