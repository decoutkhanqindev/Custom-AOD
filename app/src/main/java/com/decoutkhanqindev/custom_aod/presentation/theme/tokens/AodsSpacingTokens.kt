package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class AodsSpacingTokens(
    val textGap: Dp,
    val inlineGap: Dp,
    val stackGap: Dp,
    val componentPadding: Dp,
    val sectionPadding: Dp,
    val screenPadding: Dp,
    val sectionGap: Dp,
)

val defaultAodsSpacing = AodsSpacingTokens(
    textGap = AodsPrimitiveSpacing.Xxs,
    inlineGap = AodsPrimitiveSpacing.Xs,
    stackGap = AodsPrimitiveSpacing.Sm,
    componentPadding = AodsPrimitiveSpacing.Ms,
    sectionPadding = AodsPrimitiveSpacing.Md,
    screenPadding = AodsPrimitiveSpacing.MdPlus,
    sectionGap = AodsPrimitiveSpacing.Lg,
)

val LocalAodsSpacing = staticCompositionLocalOf { defaultAodsSpacing }
