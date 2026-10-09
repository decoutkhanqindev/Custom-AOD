package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// App luôn tối: một bộ màu, không theo sáng/tối của hệ thống. UI đọc thẳng AodsColors / AodsTypography / AodsShapes;
// MaterialTheme ở đây chỉ để component Material 3 (Button, Switch, TopAppBar…) ra đúng màu, chữ, bo góc của app.
private val AodsColorScheme = darkColorScheme(
    primary = AodsColors.Mint,
    onPrimary = AodsColors.Black,
    background = AodsColors.Black,
    onBackground = AodsColors.GreyED,
    surface = AodsColors.Black,
    onSurface = AodsColors.GreyED,
    surfaceVariant = AodsColors.NeutralVariant30,
    onSurfaceVariant = AodsColors.Grey9A,
    surfaceContainer = AodsColors.Neutral12,
    outline = AodsColors.NeutralVariant60,
    outlineVariant = AodsColors.NeutralVariant30,
    error = AodsColors.Red,
)

@Composable
fun AodsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AodsColorScheme,
        typography = AodsTypography.Material,
        shapes = AodsShapes.Material,
        content = content,
    )
}
