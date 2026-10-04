package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainAppSection(
    language: LanguageUiModel?,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(title = stringResource(R.string.section_app))

        SettingsValueRow(
            label = stringResource(R.string.language),
            value = language?.label.orEmpty(),
            onClick = { onIntent(MainIntent.NavigateToLanguage) },
        )
    }
}
