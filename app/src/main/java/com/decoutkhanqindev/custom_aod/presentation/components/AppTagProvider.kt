package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// Giống interface Tag của lớp thường nhưng cho composable: `Timber.tag(LocalTag.current)`.
val LocalTag = staticCompositionLocalOf { "App" }

@Composable
fun AppTagProvider(tag: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTag provides tag, content = content)
}
