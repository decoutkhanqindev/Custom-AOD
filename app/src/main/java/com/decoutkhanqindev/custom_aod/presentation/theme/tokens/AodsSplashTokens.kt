package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class AodsSplashTokens(
    val iconSize: Dp,
    val contentPadding: Dp,
)

// Icon 144dp đúng giữa cửa sổ: trùng icon của splash hệ thống từ Android 12 nên lúc chuyển màn icon không nhảy.
val defaultAodsSplash = AodsSplashTokens(
    iconSize = AodsPrimitiveIconSize.Massive,
    contentPadding = AodsPrimitiveSpacing.Xl,
)

val LocalAodsSplash = staticCompositionLocalOf { defaultAodsSplash }
