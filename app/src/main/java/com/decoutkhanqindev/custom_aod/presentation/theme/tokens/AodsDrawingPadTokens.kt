package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Immutable
data class AodsDrawingPadTokens(
    val background: Color,
    val stroke: Color,
    val hint: Color,
    val border: Color,
    val borderWidth: Dp,
    val strokeWidth: Dp,
)

val defaultAodsDrawingPad = AodsDrawingPadTokens(
    background = AodsPrimitiveColors.Black,
    stroke = AodsPrimitiveColors.White,
    hint = AodsPrimitiveColors.Grey6E,
    border = AodsPrimitiveColors.NeutralVariant60,
    borderWidth = AodsPrimitiveBorder.Thin,
    strokeWidth = AodsPrimitiveSpacing.Sm,
)

val LocalAodsDrawingPad = staticCompositionLocalOf { defaultAodsDrawingPad }
