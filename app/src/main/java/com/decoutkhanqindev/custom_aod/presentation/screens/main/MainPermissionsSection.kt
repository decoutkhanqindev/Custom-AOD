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
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionStatusValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography
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
        PermissionStatusValue.GRANTED -> AodsColors.Mint
        PermissionStatusValue.MISSING_REQUIRED -> AodsColors.Red
        PermissionStatusValue.UNKNOWN,
        PermissionStatusValue.MISSING_OPTIONAL -> AodsColors.Grey9A
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(28.dp)) {
            Icon(
                imageVector = status.icon,
                contentDescription = stringResource(status.descriptionRes),
                modifier = Modifier.size(20.dp),
                tint = statusColor,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        ) {
            Text(
                text = stringResource(permission.permission.titleRes),
                style = AodsTypography.BodyLarge,
            )

            Text(
                text = stringResource(permission.permission.descriptionRes),
                modifier = Modifier.padding(top = 2.dp),
                color = AodsColors.Grey9A,
                style = AodsTypography.BodySmall,
            )
        }

        OutlinedButton(onClick = onOpen) {
            Text(text = stringResource(R.string.action_open))
        }
    }
}
