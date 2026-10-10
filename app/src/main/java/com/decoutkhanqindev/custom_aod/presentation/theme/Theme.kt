package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// App luôn tối: một bộ màu, không theo sáng/tối của hệ thống. UI đọc thẳng Colors / Typography / Shapes;
// MaterialTheme ở đây chỉ để component Material 3 (Button, Switch, TopAppBar…) ra đúng màu, chữ, bo góc của app.
private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = Black,
    background = Black,
    onBackground = GreyED,
    surface = Black,
    onSurface = GreyED,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = Grey9A,
    surfaceContainer = Neutral12,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    error = Red,
)

@Composable
fun Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTypography,
        shapes = MaterialShapes,
        content = content,
    )
}
