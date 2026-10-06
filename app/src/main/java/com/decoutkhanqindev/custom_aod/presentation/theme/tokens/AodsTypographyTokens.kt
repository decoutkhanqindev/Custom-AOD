package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Immutable
data class AodsTypographyTokens(
    val fontFamily: FontFamily,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
)

// Type scale mặc định của Material 3 (kèm lineHeightStyle, platformStyle của nó), chỉ đổi font sang Inter; bodyLarge là bản riêng của app.
private val MaterialTypeScale = Typography()
private val AppFontFamily = AodsPrimitiveTypography.FontFamilyInter

val defaultAodsTypography = AodsTypographyTokens(
    fontFamily = AppFontFamily,
    displaySmall = MaterialTypeScale.displaySmall.copy(fontFamily = AppFontFamily),
    headlineLarge = MaterialTypeScale.headlineLarge.copy(fontFamily = AppFontFamily),
    headlineMedium = MaterialTypeScale.headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall = MaterialTypeScale.headlineSmall.copy(fontFamily = AppFontFamily),
    titleLarge = MaterialTypeScale.titleLarge.copy(fontFamily = AppFontFamily),
    titleMedium = MaterialTypeScale.titleMedium.copy(fontFamily = AppFontFamily),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = AodsPrimitiveTypography.FontLg,
        lineHeight = AodsPrimitiveTypography.LineHeightLg,
        letterSpacing = AodsPrimitiveTypography.LetterSpacingBody,
    ),
    bodyMedium = MaterialTypeScale.bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall = MaterialTypeScale.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = MaterialTypeScale.labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium = MaterialTypeScale.labelMedium.copy(fontFamily = AppFontFamily),
)

val LocalAodsTypography = staticCompositionLocalOf { defaultAodsTypography }
