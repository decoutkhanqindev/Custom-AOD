package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class AodsSplashTokens(
    val iconSize: Dp,
    val textOffset: Dp,
    val contentPadding: Dp,
)

// Splash hệ thống từ Android 12 vẽ lớp foreground của icon (khung 108) cỡ 288dp giữa cửa sổ; splash của app vẽ đúng drawable đó
// cùng cỡ, cùng chỗ nên lúc chuyển màn icon không nhảy. Tên app nằm dưới tâm một đoạn textOffset, ngay dưới hình điện thoại.
val defaultAodsSplash = AodsSplashTokens(
    iconSize = AodsPrimitiveIconSize.Gigantic,
    textOffset = AodsPrimitiveSpacing.Colossal,
    contentPadding = AodsPrimitiveSpacing.Xl,
)

val LocalAodsSplash = staticCompositionLocalOf { defaultAodsSplash }
