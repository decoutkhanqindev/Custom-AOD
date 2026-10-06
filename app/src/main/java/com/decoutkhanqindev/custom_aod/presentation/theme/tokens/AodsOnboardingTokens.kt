package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

// Màn chọn ngôn ngữ, màn quyền, thẻ quyền (AodsPermissionCard) và bottom sheet / dòng cảnh báo quyền ở màn chính.
@Immutable
data class AodsOnboardingTokens(
    val headerTopPadding: Dp,
    val stepPillHorizontalPadding: Dp,
    val stepPillVerticalPadding: Dp,
    val titleTopPadding: Dp,
    val subtitleTopPadding: Dp,
    val listTopPadding: Dp,
    val itemGap: Dp,
    val cardShape: CornerBasedShape,
    val cardPadding: Dp,
    val cardBorderWidth: Dp,
    val selectedBorderWidth: Dp,
    val optionMinHeight: Dp,
    val contentGap: Dp,
    val iconContainerSize: Dp,
    val iconSize: Dp,
    val actionTopPadding: Dp,
    val buttonMinHeight: Dp,
    val hintBottomPadding: Dp,
    val sheetBottomPadding: Dp,
)

val defaultAodsOnboarding = AodsOnboardingTokens(
    headerTopPadding = AodsPrimitiveSpacing.Xl,
    stepPillHorizontalPadding = AodsPrimitiveSpacing.Ms,
    stepPillVerticalPadding = AodsPrimitiveSpacing.Xs,
    titleTopPadding = AodsPrimitiveSpacing.Md,
    subtitleTopPadding = AodsPrimitiveSpacing.Sm,
    listTopPadding = AodsPrimitiveSpacing.Ms,
    itemGap = AodsPrimitiveSpacing.Ms,
    cardShape = AodsPrimitiveShape.Lg,
    cardPadding = AodsPrimitiveSpacing.Md,
    cardBorderWidth = AodsPrimitiveBorder.Thin,
    selectedBorderWidth = AodsPrimitiveBorder.Medium,
    optionMinHeight = AodsPrimitiveSpacing.Huge,
    contentGap = AodsPrimitiveSpacing.Md,
    iconContainerSize = AodsPrimitiveIconSize.Xxl,
    iconSize = AodsPrimitiveIconSize.Md,
    actionTopPadding = AodsPrimitiveSpacing.Md,
    buttonMinHeight = AodsPrimitiveSpacing.XxxlPlus,
    hintBottomPadding = AodsPrimitiveSpacing.Sm,
    sheetBottomPadding = AodsPrimitiveSpacing.Lg,
)

val LocalAodsOnboarding = staticCompositionLocalOf { defaultAodsOnboarding }
