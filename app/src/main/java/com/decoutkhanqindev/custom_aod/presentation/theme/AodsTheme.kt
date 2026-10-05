package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsAdTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsClockTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsColorTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsDrawingPadTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsElevationTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsMotionTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsOpacityTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsPickerTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsSettingsRowTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsShapeTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsSpacingTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsSplashTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.AodsTypographyTokens
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsAd
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsClock
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsDrawingPad
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsElevation
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsMotion
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsOpacity
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsPicker
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsSettingsRow
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsShapes
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsSpacing
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsSplash
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.LocalAodsTypography
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.darkAodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsAd
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsClock
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsDrawingPad
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsElevation
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsMotion
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsOpacity
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsPicker
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsSettingsRow
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsShapes
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsSpacing
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsSplash
import com.decoutkhanqindev.custom_aod.presentation.theme.tokens.defaultAodsTypography

object AodsTheme {
    val colors: AodsColorTokens
        @Composable @ReadOnlyComposable get() = LocalAodsColors.current

    val spacing: AodsSpacingTokens
        @Composable @ReadOnlyComposable get() = LocalAodsSpacing.current

    val shapes: AodsShapeTokens
        @Composable @ReadOnlyComposable get() = LocalAodsShapes.current

    val typography: AodsTypographyTokens
        @Composable @ReadOnlyComposable get() = LocalAodsTypography.current

    val motion: AodsMotionTokens
        @Composable @ReadOnlyComposable get() = LocalAodsMotion.current

    val opacity: AodsOpacityTokens
        @Composable @ReadOnlyComposable get() = LocalAodsOpacity.current

    val elevation: AodsElevationTokens
        @Composable @ReadOnlyComposable get() = LocalAodsElevation.current

    val settingsRow: AodsSettingsRowTokens
        @Composable @ReadOnlyComposable get() = LocalAodsSettingsRow.current

    val picker: AodsPickerTokens
        @Composable @ReadOnlyComposable get() = LocalAodsPicker.current

    val drawingPad: AodsDrawingPadTokens
        @Composable @ReadOnlyComposable get() = LocalAodsDrawingPad.current

    val clock: AodsClockTokens
        @Composable @ReadOnlyComposable get() = LocalAodsClock.current

    val splash: AodsSplashTokens
        @Composable @ReadOnlyComposable get() = LocalAodsSplash.current

    val ad: AodsAdTokens
        @Composable @ReadOnlyComposable get() = LocalAodsAd.current
}

// App luôn tối: một bộ màu, không theo sáng/tối của hệ thống. Component Material 3 nhận màu, chữ, bo góc qua cầu nối bên dưới.
@Composable
fun AodsTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAodsColors provides darkAodsColors,
        LocalAodsSpacing provides defaultAodsSpacing,
        LocalAodsShapes provides defaultAodsShapes,
        LocalAodsTypography provides defaultAodsTypography,
        LocalAodsMotion provides defaultAodsMotion,
        LocalAodsOpacity provides defaultAodsOpacity,
        LocalAodsElevation provides defaultAodsElevation,
        LocalAodsSettingsRow provides defaultAodsSettingsRow,
        LocalAodsPicker provides defaultAodsPicker,
        LocalAodsDrawingPad provides defaultAodsDrawingPad,
        LocalAodsClock provides defaultAodsClock,
        LocalAodsSplash provides defaultAodsSplash,
        LocalAodsAd provides defaultAodsAd,
    ) {
        MaterialTheme(
            colorScheme = darkAodsColors.toMaterial3ColorScheme(),
            typography = defaultAodsTypography.toMaterial3Typography(),
            shapes = defaultAodsShapes.toMaterial3Shapes(),
            content = content,
        )
    }
}

private fun AodsColorTokens.toMaterial3ColorScheme(): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    surfaceContainer = surfaceContainer,
    outline = outline,
    outlineVariant = outlineVariant,
    error = error,
)

private fun AodsTypographyTokens.toMaterial3Typography(): Typography = Typography(
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    titleMedium = titleMedium,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelMedium = labelMedium,
)

private fun AodsShapeTokens.toMaterial3Shapes(): Shapes = Shapes(
    small = small,
    medium = medium,
    large = large,
)
