package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = Black,
    background = Black,
    onBackground = GreyED,
    surface = Black,
    onSurface = GreyED,
    onSurfaceVariant = Grey9A,
    error = Red,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
