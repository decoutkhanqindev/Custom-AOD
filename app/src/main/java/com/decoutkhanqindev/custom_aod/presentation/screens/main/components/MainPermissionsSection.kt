package com.decoutkhanqindev.custom_aod.presentation.screens.main.components

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
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionStatusValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.BodyLarge
import com.decoutkhanqindev.custom_aod.presentation.theme.BodySmall
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey9A
import com.decoutkhanqindev.custom_aod.presentation.theme.Mint
import com.decoutkhanqindev.custom_aod.presentation.theme.Red
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MainPermissionsSection(
    permissions: ImmutableList<PermissionUiModel>,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppSectionHeader(title = stringResource(R.string.section_permissions))

        permissions.forEach { permission ->
            PermissionRow(
                permission = permission,
                onOpen = { onIntent(MainIntent.Permission.OpenPermissionSettings(permission.permission)) },
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
        PermissionStatusValue.GRANTED -> Mint
        PermissionStatusValue.MISSING_REQUIRED -> Red
        PermissionStatusValue.UNKNOWN,
        PermissionStatusValue.MISSING_OPTIONAL -> Grey9A
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
                style = BodyLarge,
            )

            Text(
                text = stringResource(permission.permission.descriptionRes),
                modifier = Modifier.padding(top = 2.dp),
                color = Grey9A,
                style = BodySmall,
            )
        }

        OutlinedButton(onClick = onOpen) {
            Text(text = stringResource(R.string.action_open))
        }
    }
}
