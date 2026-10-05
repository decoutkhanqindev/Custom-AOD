package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionStatusValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MainPermissionsSection(
    permissions: ImmutableList<PermissionUiModel>,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AodsSectionHeader(title = stringResource(R.string.section_permissions))

        permissions.forEach { permission ->
            PermissionRow(
                permission = permission,
                onOpen = { onIntent(MainIntent.OpenPermissionSettings(permission.permission)) },
            )
        }
    }
}

@Composable
private fun PermissionRow(
    permission: PermissionUiModel,
    onOpen: () -> Unit,
) {
    val status = permission.status
    val statusColor = when (status) {
        PermissionStatusValue.GRANTED -> AodsTheme.colors.primary
        PermissionStatusValue.MISSING_REQUIRED -> AodsTheme.colors.error
        PermissionStatusValue.UNKNOWN,
        PermissionStatusValue.MISSING_OPTIONAL -> AodsTheme.colors.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AodsTheme.settingsRow.statusVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(AodsTheme.settingsRow.statusIconSlotWidth)) {
            Icon(
                imageVector = status.icon,
                contentDescription = stringResource(status.descriptionRes),
                modifier = Modifier.size(AodsTheme.settingsRow.statusIconSize),
                tint = statusColor,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = AodsTheme.settingsRow.statusTextEndPadding),
        ) {
            Text(
                text = stringResource(permission.permission.titleRes),
                style = AodsTheme.typography.bodyLarge,
            )

            Text(
                text = stringResource(permission.permission.descriptionRes),
                modifier = Modifier.padding(top = AodsTheme.spacing.textGap),
                color = AodsTheme.colors.onSurfaceVariant,
                style = AodsTheme.typography.bodySmall,
            )
        }

        OutlinedButton(onClick = onOpen) {
            Text(text = stringResource(R.string.action_open))
        }
    }
}
