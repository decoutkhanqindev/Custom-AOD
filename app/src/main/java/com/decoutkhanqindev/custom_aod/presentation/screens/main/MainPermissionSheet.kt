package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsPermissionCard
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPermissionSheet(
    isVisible: Boolean,
    permissions: ImmutableList<PermissionUiModel>,
    onIntent: (MainIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShown by remember { mutableStateOf(isVisible) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            isShown = true
        } else if (isShown) {
            sheetState.hide()
            isShown = false
        }
    }

    if (isShown) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(MainIntent.DismissPermissionSheet) },
            sheetState = sheetState,
            containerColor = AodsTheme.colors.surfaceContainer,
        ) {
            PermissionSheetBody(permissions = permissions, onIntent = onIntent)
        }
    }
}

@Composable
fun MainPermissionWarning(
    isVisible: Boolean,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = isVisible, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AodsTheme.spacing.sectionPadding)
                .clip(AodsTheme.onboarding.cardShape)
                .background(AodsTheme.colors.error.copy(alpha = AodsTheme.opacity.tint))
                .padding(start = AodsTheme.onboarding.cardPadding, end = AodsTheme.spacing.stackGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(AodsTheme.onboarding.iconSize),
                tint = AodsTheme.colors.error,
            )

            Text(
                text = stringResource(R.string.permission_sheet_title),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AodsTheme.spacing.componentPadding),
                color = AodsTheme.colors.onSurface,
                style = AodsTheme.typography.bodyLarge,
            )

            TextButton(onClick = { onIntent(MainIntent.ShowPermissionSheet) }) {
                Text(text = stringResource(R.string.action_allow))
            }
        }
    }
}

@Composable
private fun PermissionSheetBody(
    permissions: ImmutableList<PermissionUiModel>,
    onIntent: (MainIntent) -> Unit,
) {
    val onboarding = AodsTheme.onboarding

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AodsTheme.spacing.screenPadding)
            .padding(bottom = onboarding.sheetBottomPadding),
    ) {
        Text(
            text = stringResource(R.string.permission_sheet_title),
            modifier = Modifier.semantics { heading() },
            color = AodsTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = AodsTheme.typography.titleLarge,
        )

        Text(
            text = stringResource(R.string.permission_sheet_desc),
            modifier = Modifier.padding(top = onboarding.subtitleTopPadding),
            color = AodsTheme.colors.onSurfaceVariant,
            style = AodsTheme.typography.bodyMedium,
        )

        permissions.forEach { permission ->
            AodsPermissionCard(
                permission = permission,
                onAllowClick = { onIntent(MainIntent.OpenPermissionSettings(permission.permission)) },
                modifier = Modifier.padding(top = onboarding.itemGap),
                containerColor = AodsTheme.colors.background,
            )
        }

        TextButton(
            onClick = { onIntent(MainIntent.DismissPermissionSheet) },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = onboarding.actionTopPadding),
        ) {
            Text(text = stringResource(R.string.action_later))
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun PermissionSheetBodyPreview() {
    AodsTheme {
        Column(modifier = Modifier.background(AodsTheme.colors.surfaceContainer)) {
            PermissionSheetBody(
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = false),
                    PermissionUiModel(permission = PermissionValue.MIUI_LOCK_SCREEN, isGranted = true),
                ),
                onIntent = {},
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun MainPermissionWarningPreview() {
    AodsTheme {
        MainPermissionWarning(isVisible = true, onIntent = {})
    }
}
