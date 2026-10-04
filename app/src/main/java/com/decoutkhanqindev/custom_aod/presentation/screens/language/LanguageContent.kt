package com.decoutkhanqindev.custom_aod.presentation.screens.language

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsRadioRow
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageState
import com.decoutkhanqindev.custom_aod.presentation.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageContent(
    state: LanguageState,
    onIntent: (LanguageIntent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.language)) },
                navigationIcon = {
                    if (!state.isFirstOpen) {
                        IconButton(onClick = { onIntent(LanguageIntent.NavigateBack) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = { onIntent(LanguageIntent.ConfirmLanguage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                enabled = state.isConfirmEnabled,
            ) {
                Text(text = stringResource(R.string.action_done))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
        ) {
            items(items = state.languages, key = { it.language.name }) { language ->
                SettingsRadioRow(
                    label = language.label,
                    isSelected = language.language == state.selectedLanguage,
                    onClick = { onIntent(LanguageIntent.SelectLanguage(language.language)) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun LanguageContentPreview() {
    AppTheme {
        LanguageContent(
            state = LanguageState(
                languages = persistentListOf(
                    LanguageUiModel(language = LanguageValue.VIETNAMESE, displayName = "Tiếng Việt"),
                    LanguageUiModel(language = LanguageValue.ENGLISH, displayName = "English"),
                ),
                appliedLanguage = LanguageValue.ENGLISH,
                selectedLanguage = LanguageValue.VIETNAMESE,
            ),
            onIntent = {},
        )
    }
}
