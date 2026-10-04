package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.model.AnimationContentKey
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodRulesUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ChargingRuleValue
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainState
import com.decoutkhanqindev.custom_aod.presentation.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MainContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AnimatedContent(
            targetState = if (state.isLoading) AnimationContentKey.Loading else AnimationContentKey.Content,
            label = "MainContent",
        ) { contentKey ->
            when (contentKey) {
                AnimationContentKey.Content -> MainSettings(
                    state = state,
                    onIntent = onIntent,
                    contentPadding = innerPadding,
                )

                AnimationContentKey.Loading,
                AnimationContentKey.Error -> Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun MainSettings(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = stringResource(R.string.main_subtitle),
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSwitchRow(
            label = stringResource(R.string.opt_enabled),
            isChecked = state.options.isEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleAod(it)) },
        )

        MainPermissionsSection(
            permissions = state.permissions,
            onIntent = onIntent,
        )

        MainOptionsSection(
            options = state.options,
            onIntent = onIntent,
        )

        MainRulesSection(
            rules = state.rules,
            editingScheduleTime = state.editingScheduleTime,
            onIntent = onIntent,
        )

        MainAppSection(
            language = state.language,
            onIntent = onIntent,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onIntent(MainIntent.Preview) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.action_preview))
        }

        Text(
            text = stringResource(state.lastWakeMessageRes),
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Preview(widthDp = 360, heightDp = 1600)
@Composable
private fun MainContentPreview() {
    AppTheme {
        MainContent(
            state = MainState(
                isLoading = false,
                options = AodOptionsUiModel(brightnessPercent = 20, timeoutMinutes = 30),
                rules = AodRulesUiModel(chargingRule = ChargingRuleValue.PLUGGED),
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = true),
                    PermissionUiModel(permission = PermissionValue.MIUI_LOCK_SCREEN, isGranted = false),
                    PermissionUiModel(permission = PermissionValue.MIUI_BACKGROUND_POPUP, isGranted = null),
                    PermissionUiModel(permission = PermissionValue.NOTIFICATIONS, isGranted = false),
                ),
                language = LanguageUiModel(language = LanguageValue.ENGLISH, displayName = "English"),
                lastWakeMessageRes = WakeResultValue.OK.messageRes,
            ),
            onIntent = {},
        )
    }
}
