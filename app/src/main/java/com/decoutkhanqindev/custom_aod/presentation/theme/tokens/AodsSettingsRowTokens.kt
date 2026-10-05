package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class AodsSettingsRowTokens(
    val minHeight: Dp,
    val compactMinHeight: Dp,
    val labelEndPadding: Dp,
    val radioLabelStartPadding: Dp,
    val sectionHeaderTopPadding: Dp,
    val sectionHeaderBottomPadding: Dp,
    val sectionLabelTopPadding: Dp,
    val sectionLabelBottomPadding: Dp,
    val sliderTopPadding: Dp,
    val statusVerticalPadding: Dp,
    val statusIconSlotWidth: Dp,
    val statusIconSize: Dp,
    val statusTextEndPadding: Dp,
)

val defaultAodsSettingsRow = AodsSettingsRowTokens(
    minHeight = AodsPrimitiveSpacing.XxxlPlus,
    compactMinHeight = AodsPrimitiveSpacing.Xxxl,
    labelEndPadding = AodsPrimitiveSpacing.Md,
    radioLabelStartPadding = AodsPrimitiveSpacing.Ms,
    sectionHeaderTopPadding = AodsPrimitiveSpacing.LgPlus,
    sectionHeaderBottomPadding = AodsPrimitiveSpacing.Xs,
    sectionLabelTopPadding = AodsPrimitiveSpacing.Ms,
    sectionLabelBottomPadding = AodsPrimitiveSpacing.Xs,
    sliderTopPadding = AodsPrimitiveSpacing.Ms,
    statusVerticalPadding = AodsPrimitiveSpacing.SmPlus,
    statusIconSlotWidth = AodsPrimitiveSpacing.LgPlus,
    statusIconSize = AodsPrimitiveIconSize.Ms,
    statusTextEndPadding = AodsPrimitiveSpacing.Ms,
)

val LocalAodsSettingsRow = staticCompositionLocalOf { defaultAodsSettingsRow }
