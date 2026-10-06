package com.decoutkhanqindev.custom_aod.presentation.screens.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsOnboardingFooter
import com.decoutkhanqindev.custom_aod.presentation.components.AodsOnboardingHeader
import com.decoutkhanqindev.custom_aod.presentation.components.AodsPermissionCard
import com.decoutkhanqindev.custom_aod.presentation.model.OnboardingStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionState
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PermissionContent(
    state: PermissionState,
    onIntent: (PermissionIntent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AodsOnboardingFooter(
                actionLabel = stringResource(R.string.action_get_started),
                isActionEnabled = state.isConfirmEnabled,
                onAction = { onIntent(PermissionIntent.ConfirmPermissions) },
                disabledHint = stringResource(R.string.permission_missing_hint),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = AodsTheme.spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AodsTheme.onboarding.itemGap),
        ) {
            AodsOnboardingHeader(
                step = OnboardingStepValue.PERMISSION,
                title = stringResource(R.string.permission_title),
                subtitle = stringResource(R.string.permission_subtitle),
                modifier = Modifier.padding(bottom = AodsTheme.onboarding.listTopPadding),
            )

            state.permissions.forEach { permission ->
                AodsPermissionCard(
                    permission = permission,
                    onAllowClick = { onIntent(PermissionIntent.OpenPermissionSettings(permission.permission)) },
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun PermissionContentMissingPreview() {
    AodsTheme {
        PermissionContent(
            state = PermissionState(
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = true),
                    PermissionUiModel(permission = PermissionValue.MIUI_LOCK_SCREEN, isGranted = false),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun PermissionContentReadyPreview() {
    AodsTheme {
        PermissionContent(
            state = PermissionState(
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = true),
                ),
            ),
            onIntent = {},
        )
    }
}
