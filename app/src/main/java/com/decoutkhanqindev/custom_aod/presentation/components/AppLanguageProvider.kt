package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import org.koin.compose.koinInject

@Composable
fun AppLanguageProvider(
    languageCode: String,
    content: @Composable () -> Unit,
) {
    val languageManager: LanguageManager = koinInject()
    val configuration = remember(languageCode) {
        languageManager.configurationFor(languageCode)
    }
    val resources = remember(configuration) {
        languageManager.resourcesFor(configuration)
    }

    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalResources provides resources,
        content = content,
    )
}
