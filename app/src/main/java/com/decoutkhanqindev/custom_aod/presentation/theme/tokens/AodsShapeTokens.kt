package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class AodsShapeTokens(
    val small: CornerBasedShape,
    val medium: CornerBasedShape,
    val large: CornerBasedShape,
    val full: CornerBasedShape,
)

val defaultAodsShapes = AodsShapeTokens(
    small = AodsPrimitiveShape.Sm,
    medium = AodsPrimitiveShape.Md,
    large = AodsPrimitiveShape.Lg,
    full = AodsPrimitiveShape.Full,
)

val LocalAodsShapes = staticCompositionLocalOf { defaultAodsShapes }
