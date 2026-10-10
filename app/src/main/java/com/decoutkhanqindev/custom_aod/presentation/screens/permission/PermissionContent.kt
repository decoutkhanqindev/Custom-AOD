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
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.onboarding.AppOnboardingFooter
import com.decoutkhanqindev.custom_aod.presentation.components.onboarding.AppOnboardingHeader
import com.decoutkhanqindev.custom_aod.presentation.components.onboarding.AppOnboardingTopBar
import com.decoutkhanqindev.custom_aod.presentation.components.permission.AppPermissionCard
import com.decoutkhanqindev.custom_aod.presentation.model.onboarding.OnboardingStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionState
import com.decoutkhanqindev.custom_aod.presentation.theme.Theme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PermissionContent(
    state: PermissionState,
    onIntent: (PermissionIntent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppOnboardingTopBar(
                step = OnboardingStepValue.PERMISSION,
                onNavigateBack = { onIntent(PermissionIntent.NavigateBack) },
            )
        },
        bottomBar = {
            AppOnboardingFooter(
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppOnboardingHeader(
                title = stringResource(R.string.permission_title),
                subtitle = stringResource(R.string.permission_subtitle),
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 12.dp,
                ),
            )

            state.permissions.forEach { permission ->
                AppPermissionCard(
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
    Theme {
        PermissionContent(
            state = PermissionState(
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = true),
                    PermissionUiModel(
                        permission = PermissionValue.MIUI_LOCK_SCREEN,
                        isGranted = false
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun PermissionContentReadyPreview() {
    Theme {
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
