package com.decoutkhanqindev.custom_aod.presentation.screens.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.language.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainAppSection(
    language: LanguageUiModel?,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppSectionHeader(title = stringResource(R.string.section_app))

        AppValueRow(
            label = stringResource(R.string.language),
            value = language?.label.orEmpty(),
            onClick = { onIntent(MainIntent.NavigateToLanguage) },
        )
    }
}
