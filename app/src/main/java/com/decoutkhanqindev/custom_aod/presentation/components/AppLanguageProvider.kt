package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import org.koin.compose.koinInject

@Composable
fun AppLanguageProvider(content: @Composable () -> Unit) {
    val dataStoreManager: DataStoreManager = koinInject()
    val languageManager: LanguageManager = koinInject()
    val selectedLangCode by dataStoreManager.selectedLangCode.collectAsStateWithLifecycle()
    val languageCode = LanguageValue.fromCode(selectedLangCode).code
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
