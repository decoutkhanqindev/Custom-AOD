package com.decoutkhanqindev.custom_aod.presentation.screens.language

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsOnboardingFooter
import com.decoutkhanqindev.custom_aod.presentation.components.AodsOnboardingHeader
import com.decoutkhanqindev.custom_aod.presentation.components.AodsOnboardingTopBar
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.model.OnboardingStepValue
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageState
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
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
            if (state.isFirstOpen) {
                AodsOnboardingTopBar(step = OnboardingStepValue.LANGUAGE)
            } else {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.language)) },
                    navigationIcon = {
                        IconButton(onClick = { onIntent(LanguageIntent.NavigateBack) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            AodsOnboardingFooter(
                actionLabel = stringResource(if (state.isFirstOpen) R.string.action_continue else R.string.action_done),
                isActionEnabled = state.isConfirmEnabled,
                onAction = { onIntent(LanguageIntent.ConfirmLanguage) },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .selectableGroup(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isFirstOpen) {
                item(key = OnboardingStepValue.LANGUAGE.name) {
                    AodsOnboardingHeader(
                        title = stringResource(R.string.language_title),
                        subtitle = stringResource(R.string.language_subtitle),
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(
                                top = 8.dp,
                                bottom = 12.dp,
                            ),
                    )
                }
            }

            items(items = state.languages, key = { it.language.name }) { language ->
                LanguageOption(
                    language = language,
                    isSelected = language.language == state.selectedLanguage,
                    onClick = { onIntent(LanguageIntent.SelectLanguage(language.language)) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    language: LanguageUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        label = "LanguageOptionContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "LanguageOptionBorder",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "LanguageOptionBorderWidth",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .border(width = borderWidth, color = borderColor, shape = MaterialTheme.shapes.large)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = language.language.flag,
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = language.displayName,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )

        RadioButton(selected = isSelected, onClick = null)
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun LanguageContentFirstOpenPreview() {
    AodsTheme {
        LanguageContent(
            state = LanguageState(
                isFirstOpen = true,
                languages = persistentListOf(
                    LanguageUiModel(language = LanguageValue.VIETNAMESE, displayName = "Tiếng Việt"),
                    LanguageUiModel(language = LanguageValue.ENGLISH, displayName = "English"),
                ),
                selectedLanguage = LanguageValue.VIETNAMESE,
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun LanguageContentFromMainPreview() {
    AodsTheme {
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
