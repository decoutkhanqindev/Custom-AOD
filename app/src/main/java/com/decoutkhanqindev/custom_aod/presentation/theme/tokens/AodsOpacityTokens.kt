package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class AodsOpacityTokens(
    val disabled: Float,
    val tint: Float,
)

// tint: nền phủ nhạt một màu vai trò (thẻ đang chọn, ô icon, dòng cảnh báo), chữ trên đó vẫn đủ tương phản.
val defaultAodsOpacity = AodsOpacityTokens(
    disabled = AodsPrimitiveOpacity.Disabled,
    tint = AodsPrimitiveOpacity.Subtle,
)

val LocalAodsOpacity = staticCompositionLocalOf { defaultAodsOpacity }
