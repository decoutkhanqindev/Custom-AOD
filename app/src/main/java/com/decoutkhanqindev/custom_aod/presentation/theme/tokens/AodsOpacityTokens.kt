package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class AodsOpacityTokens(
    val disabled: Float,
)

val defaultAodsOpacity = AodsOpacityTokens(
    disabled = AodsPrimitiveOpacity.Disabled,
)

val LocalAodsOpacity = staticCompositionLocalOf { defaultAodsOpacity }
