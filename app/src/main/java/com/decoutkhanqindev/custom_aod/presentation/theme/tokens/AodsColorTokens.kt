package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AodsColorTokens(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val shimmer: Color,
    val overlay: Color,
    val onOverlay: Color,
)

val darkAodsColors = AodsColorTokens(
    primary = AodsPrimitiveColors.Mint,
    onPrimary = AodsPrimitiveColors.Black,
    background = AodsPrimitiveColors.Black,
    onBackground = AodsPrimitiveColors.GreyED,
    surface = AodsPrimitiveColors.Black,
    onSurface = AodsPrimitiveColors.GreyED,
    surfaceVariant = AodsPrimitiveColors.NeutralVariant30,
    onSurfaceVariant = AodsPrimitiveColors.Grey9A,
    surfaceContainer = AodsPrimitiveColors.Neutral12,
    outline = AodsPrimitiveColors.NeutralVariant60,
    outlineVariant = AodsPrimitiveColors.NeutralVariant30,
    error = AodsPrimitiveColors.Red,
    shimmer = AodsPrimitiveColors.White.copy(alpha = AodsPrimitiveOpacity.Faint),
    overlay = AodsPrimitiveColors.Black.copy(alpha = AodsPrimitiveOpacity.Half),
    onOverlay = AodsPrimitiveColors.White,
)

val LocalAodsColors = staticCompositionLocalOf { darkAodsColors }
