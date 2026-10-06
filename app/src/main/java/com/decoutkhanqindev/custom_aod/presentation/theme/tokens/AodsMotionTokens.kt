package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class AodsMotionTokens(
    val durationShort: Int,
    val durationEnter: Int,
    val durationShimmer: Int,
    val durationShimmerHighlight: Int,
    val easeLinear: Easing,
    val pressScale: Float,
)

val defaultAodsMotion = AodsMotionTokens(
    durationShort = AodsPrimitiveMotion.Duration100,
    durationEnter = AodsPrimitiveMotion.Duration600,
    durationShimmer = AodsPrimitiveMotion.Duration1000,
    durationShimmerHighlight = AodsPrimitiveMotion.Duration1400,
    easeLinear = AodsPrimitiveMotion.EaseLinear,
    pressScale = AodsPrimitiveMotion.PressScale,
)

val LocalAodsMotion = staticCompositionLocalOf { defaultAodsMotion }
