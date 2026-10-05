package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class AodsAdTokens(
    val loadingPadding: Dp,
    val loadingIndicatorSize: Dp,
    val loadingIndicatorStrokeWidth: Dp,
    val loadingGap: Dp,
    val closeButtonSize: Dp,
    val closeIconSize: Dp,
    val closeButtonMargin: Dp,
)

val defaultAodsAd = AodsAdTokens(
    loadingPadding = AodsPrimitiveSpacing.Lg,
    loadingIndicatorSize = AodsPrimitiveIconSize.Xl,
    loadingIndicatorStrokeWidth = AodsPrimitiveBorder.Thick,
    loadingGap = AodsPrimitiveSpacing.Md,
    closeButtonSize = AodsPrimitiveIconSize.LgPlus,
    closeIconSize = AodsPrimitiveIconSize.Ms,
    closeButtonMargin = AodsPrimitiveSpacing.Ms,
)

val LocalAodsAd = staticCompositionLocalOf { defaultAodsAd }
