package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class AodsElevationTokens(
    val low: Dp,
)

val defaultAodsElevation = AodsElevationTokens(
    low = AodsPrimitiveElevation.Sm,
)

val LocalAodsElevation = staticCompositionLocalOf { defaultAodsElevation }
