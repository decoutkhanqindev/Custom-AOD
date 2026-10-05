package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AodsPickerTokens(
    val verticalPadding: Dp,
    val swatchSize: Dp,
    val swatchBorderWidth: Dp,
    val swatchCheckSize: Dp,
    val thumbnailWidth: Dp,
    val thumbnailHeight: Dp,
    val thumbnailGap: Dp,
    val thumbnailItemPadding: Dp,
    val thumbnailLabelTopPadding: Dp,
    val thumbnailBorderWidth: Dp,
    val thumbnailSelectedBorderWidth: Dp,
    val thumbnailCheckSize: Dp,
    val thumbnailCheckPadding: Dp,
)

val defaultAodsPicker = AodsPickerTokens(
    verticalPadding = AodsPrimitiveSpacing.Sm,
    swatchSize = AodsPrimitiveIconSize.Lg,
    swatchBorderWidth = AodsPrimitiveBorder.Medium,
    swatchCheckSize = AodsPrimitiveIconSize.SmPlus,
    thumbnailWidth = 60.dp,
    thumbnailHeight = 132.dp,
    thumbnailGap = AodsPrimitiveSpacing.Ms,
    thumbnailItemPadding = AodsPrimitiveSpacing.Xs,
    thumbnailLabelTopPadding = AodsPrimitiveSpacing.XsPlus,
    thumbnailBorderWidth = AodsPrimitiveBorder.Thin,
    thumbnailSelectedBorderWidth = AodsPrimitiveBorder.Medium,
    thumbnailCheckSize = AodsPrimitiveIconSize.Sm,
    thumbnailCheckPadding = AodsPrimitiveSpacing.XsPlus,
)

val LocalAodsPicker = staticCompositionLocalOf { defaultAodsPicker }
