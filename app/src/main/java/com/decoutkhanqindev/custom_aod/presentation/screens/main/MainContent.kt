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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.model.AnimationContentKey
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.WakeResultValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodRulesUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.ChargingRuleValue
import com.decoutkhanqindev.custom_aod.presentation.model.language.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.language.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainAppSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainAppearanceSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainExtrasSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainInteractionSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainNotificationsSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainOptionsSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainPermissionSheet
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainPermissionWarning
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainPermissionsSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.components.MainRulesSection
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainPermissionState
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainRulesState
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainState
import com.decoutkhanqindev.custom_aod.presentation.theme.BodyMedium
import com.decoutkhanqindev.custom_aod.presentation.theme.BodySmall
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey9A
import com.decoutkhanqindev.custom_aod.presentation.theme.HeadlineMedium
import com.decoutkhanqindev.custom_aod.presentation.theme.Theme
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

    MainPermissionSheet(
        isVisible = state.isPermissionSheetVisible,
        permissions = state.permission.requiredPermissions,
        onIntent = onIntent,
    )
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
            style = HeadlineMedium,
        )

        Text(
            text = stringResource(R.string.main_subtitle),
            modifier = Modifier.padding(top = 4.dp),
            color = Grey9A,
            style = BodyMedium,
        )

        MainPermissionWarning(
            isVisible = state.isPermissionWarningVisible,
            onIntent = onIntent,
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppSwitchRow(
            label = stringResource(R.string.opt_enabled),
            isChecked = state.options.isEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleAod(it)) },
        )

        MainPermissionsSection(
            permissions = state.permission.permissions,
            onIntent = onIntent,
        )

        MainOptionsSection(
            options = state.options,
            onIntent = onIntent,
        )

        MainAppearanceSection(
            appearance = state.appearance,
            onIntent = onIntent,
        )

        MainExtrasSection(
            extras = state.extras,
            onIntent = onIntent,
        )

        MainNotificationsSection(
            options = state.notificationOptions,
            isAccessGranted = state.permission.isNotificationAccessGranted,
            onIntent = onIntent,
        )

        MainInteractionSection(
            interaction = state.interaction,
            onIntent = onIntent,
        )

        MainRulesSection(
            rules = state.rules,
            onIntent = onIntent,
        )

        MainAppSection(
            language = state.language,
            onIntent = onIntent,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onIntent(MainIntent.OpenPreview) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.action_preview))
        }

        Text(
            text = stringResource(state.lastWakeMessageRes),
            modifier = Modifier.padding(top = 12.dp),
            color = Grey9A,
            style = BodySmall,
        )
    }
}

@Preview(widthDp = 360, heightDp = 3800)
@Composable
private fun MainContentPreview() {
    Theme {
        MainContent(
            state = MainState(
                isLoading = false,
                options = AodOptionsUiModel(brightnessPercent = 20, timeoutMinutes = 30),
                rules = MainRulesState(settings = AodRulesUiModel(chargingRule = ChargingRuleValue.PLUGGED)),
                permission = MainPermissionState(
                    permissions = persistentListOf(
                        PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = true),
                        PermissionUiModel(
                            permission = PermissionValue.MIUI_LOCK_SCREEN,
                            isGranted = false
                        ),
                        PermissionUiModel(
                            permission = PermissionValue.MIUI_BACKGROUND_POPUP,
                            isGranted = null
                        ),
                        PermissionUiModel(
                            permission = PermissionValue.NOTIFICATIONS,
                            isGranted = false
                        ),
                        PermissionUiModel(
                            permission = PermissionValue.NOTIFICATION_ACCESS,
                            isGranted = false
                        ),
                    ),
                ),
                language = LanguageUiModel(
                    language = LanguageValue.ENGLISH,
                    displayName = "English"
                ),
                lastWakeMessageRes = WakeResultValue.OK.messageRes,
            ),
            onIntent = {},
        )
    }
}
