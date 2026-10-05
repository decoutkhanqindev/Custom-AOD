package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Immutable
data class AodsTypographyTokens(
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelMedium: TextStyle,
)

// Lấy nguyên type scale mặc định của Material 3 (kèm lineHeightStyle, platformStyle của nó) để chữ hiển thị như trước; chỉ bodyLarge là bản riêng của app.
private val MaterialTypeScale = Typography()

val defaultAodsTypography = AodsTypographyTokens(
    headlineLarge = MaterialTypeScale.headlineLarge,
    headlineMedium = MaterialTypeScale.headlineMedium,
    titleMedium = MaterialTypeScale.titleMedium,
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = AodsPrimitiveTypography.FontLg,
        lineHeight = AodsPrimitiveTypography.LineHeightLg,
        letterSpacing = AodsPrimitiveTypography.LetterSpacingBody,
    ),
    bodyMedium = MaterialTypeScale.bodyMedium,
    bodySmall = MaterialTypeScale.bodySmall,
    labelMedium = MaterialTypeScale.labelMedium,
)

val LocalAodsTypography = staticCompositionLocalOf { defaultAodsTypography }
